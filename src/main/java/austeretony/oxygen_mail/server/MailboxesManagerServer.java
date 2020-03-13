package austeretony.oxygen_mail.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.notification.SimpleNotification;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.client.CPAttachmentReceived;
import austeretony.oxygen_mail.common.network.client.CPMailSent;
import austeretony.oxygen_mail.common.network.client.CPMessageRemoved;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MailboxesManagerServer {

    private final MailManagerServer manager;

    private final Queue<QueuedMailSending> mailSendingQueue = new ConcurrentLinkedQueue<>();

    private final Queue<QueuedMailOperation> mailOperationsQueue = new ConcurrentLinkedQueue<>();

    public MailboxesManagerServer(MailManagerServer manager) {
        this.manager = manager;
    }

    public void processExpiredMail() {
        OxygenHelperServer.addRoutineTask(()->{
            int removed = 0;
            Iterator<Mail> iterator;
            Mail mail;
            for (Mailbox mailbox : this.manager.getMailboxesContainer().getMailboxes()) {
                iterator = mailbox.getMessages().iterator();
                while (iterator.hasNext()) {
                    mail = iterator.next();
                    if (mail.isExpired()) {
                        if (mail.isPending())
                            this.processExpiredMessage(mail);
                        iterator.remove();
                        removed++;
                    }
                }
            }
            if (removed > 0)
                this.manager.getMailboxesContainer().setChanged(true);
            OxygenMain.LOGGER.info("[Mail] Expired mail processed. Removed {} messages in total.", removed);
        });
    }

    private void processExpiredMessage(Mail message) {
        if (message.getType() == EnumMail.REMITTANCE 
                || message.getType() == EnumMail.PARCEL
                || message.getType() == EnumMail.COD)
            this.sendSystemMail(
                    message.getSenderUUID(), 
                    Mail.SYSTEM_SENDER, 
                    EnumMail.PARCEL, 
                    "mail.subject.return", 
                    message.getAttachment(), 
                    true, 
                    "mail.message.attachmentReturn");
    }

    private void sendNewMessageNotification(UUID playerUUID) {
        if (OxygenHelperServer.isPlayerOnline(playerUUID))
            OxygenHelperServer.addNotification(CommonReference.playerByUUID(playerUUID), new SimpleNotification(MailMain.INCOMING_MESSAGE_NOTIFICATION_ID, "oxygen_mail.incoming"));
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, Attachment attachment) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(senderMP), EnumMailPrivilege.ALLOW_MAIL_SENDING.id(), MailConfig.ALLOW_MAIL_SENDING.asBoolean()))
            this.mailSendingQueue.offer(new QueuedMailSending(senderMP, addresseeUsername, type, subject, message, attachment));
    }

    void process() {
        this.processMailSendingQueue();
        this.processMailOperationsQueue();
    }

    private void processMailSendingQueue() {
        Runnable task = ()->{
            while (!this.mailSendingQueue.isEmpty()) {
                final QueuedMailSending queued = this.mailSendingQueue.poll();
                if (queued != null)
                    this.sendMailQueue(queued);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void sendMailQueue(QueuedMailSending queued) {
        PlayerSharedData sharedData = OxygenHelperServer.getPlayerSharedData(queued.addresseeUsername);
        if (sharedData == null) {
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.PLAYER_NOT_FOUND);
            return;
        }
        if (this.processPlayerMailSending(queued.senderMP, sharedData.getPlayerUUID(), queued.type, queued.subject, queued.message, queued.attachment, false))
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.MESSAGE_SENT);
        else
            this.manager.sendStatusMessages(queued.senderMP, EnumMailStatusMessage.MESSAGE_SENDING_FAILED);
    }

    private boolean processPlayerMailSending(EntityPlayerMP senderMP, UUID addresseeUUID, EnumMail type, String subject, String message, Attachment attachment, boolean isReturn) {
        if (!isReturn) {
            subject = subject.trim();
            if (subject.isEmpty()) return false;
            if (subject.length() > Mail.MESSAGE_SUBJECT_MAX_LENGTH)
                subject = subject.substring(0, Mail.MESSAGE_SUBJECT_MAX_LENGTH);
            message = message.trim();
            if (message.length() > Mail.MESSAGE_MAX_LENGTH)
                message = message.substring(0, Mail.MESSAGE_MAX_LENGTH);
        }

        UUID senderUUID = CommonReference.getPersistentUUID(senderMP);
        if (!addresseeUUID.equals(senderUUID)) {
            Mailbox senderMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(senderUUID);
            if (senderMailbox.canSendMessage()) {
                Mailbox targetMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
                if (targetMailbox.canAcceptMessages()) {
                    switch (type) {
                    case LETTER:
                        attachment = Attachments.dummy();
                        if (message.isEmpty())
                            return false;
                        if (!this.processLetter(senderMP, senderUUID))
                            return false;
                        break;
                    case REMITTANCE:
                        if (attachment.getCurrencyValue() <= 0L)
                            return false;
                        if (!isReturn && !this.processRemittance(senderMP, senderUUID, attachment.getCurrencyValue()))
                            return false;
                        break;
                    case PARCEL:
                        if (!this.validateParcel(senderMP, attachment))
                            return false;
                        if (!isReturn && !this.processPackage(senderMP, senderUUID, attachment))
                            return false;
                        break;
                    case COD:
                        if (attachment.getCurrencyValue() <= 0L)
                            return false;
                        if (!this.validateParcel(senderMP, attachment))
                            return false;
                        if (!isReturn && !this.processCOD(senderMP, senderUUID, attachment))
                            return false;
                        break;
                    default:
                        return false;
                    }                 
                    senderMailbox.applySendingCooldown();
                    this.addMessage(targetMailbox, type, senderUUID, CommonReference.getName(senderMP), subject, attachment, message);
                    OxygenMain.network().sendTo(new CPMailSent(type, attachment, CurrencyHelperServer.getCurrency(senderUUID, OxygenMain.COMMON_CURRENCY_INDEX)), senderMP);

                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateParcel(EntityPlayerMP playerMP, Attachment attachment) {
        if (attachment.getStackWrapper() == null || (attachment.getStackWrapper().getItemId() == Item.getIdFromItem(Items.AIR) || attachment.getItemAmount() <= 0)) {
            this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.PARCEL_DAMAGED);
            return false;
        }
        if (this.manager.getItemsBlackList().isBlackListed(Item.getItemById(attachment.getStackWrapper().getItemId()))) {
            this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.ITEM_BLACKLISTED);
            return false;
        }
        return true;
    }

    private void addMessage(Mailbox targetMailbox, EnumMail type, UUID senderUUID, String senderName, String subject, Attachment attachment, String message, String... messageArgs) {
        targetMailbox.addMessage(new Mail(
                targetMailbox.createId(TimeHelperServer.getCurrentMillis()), 
                type, 
                senderUUID, 
                senderName, 
                subject, 
                attachment, 
                message, 
                messageArgs));
        this.sendNewMessageNotification(targetMailbox.getPlayerUUID());

        this.manager.getMailboxesContainer().setChanged(true);

        if (MailConfig.ADVANCED_LOGGING.asBoolean())
            OxygenMain.LOGGER.info("[Mail] Sender {}/{} sent mail <subject: {}, type: {}> with attachment {} to player {}.", 
                    senderName,
                    senderUUID.equals(Mail.SYSTEM_UUID) ? "SYSTEM" : senderUUID,
                            subject,
                            type,
                            attachment,
                            targetMailbox.getPlayerUUID());
    }

    private boolean processLetter(EntityPlayerMP senderMP, UUID senderUUID) {
        long letterPostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
        boolean postageExist = letterPostage > 0L;//to avoid dummy currency operations
        if (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, letterPostage, OxygenMain.COMMON_CURRENCY_INDEX)) {
            if (postageExist) {
                CurrencyHelperServer.removeCurrency(senderUUID, letterPostage, OxygenMain.COMMON_CURRENCY_INDEX);
                SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.RINGING_COINS.getId());
            }
            return true;
        }
        return false;
    }

    private boolean processRemittance(EntityPlayerMP senderMP, UUID senderUUID, long remittanceValue) {
        if (remittanceValue <= PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.REMITTANCE_MAX_VALUE.id(), MailConfig.REMITTANCE_MAX_VALUE.asLong())) {
            long remittancePostage = MathUtils.percentValueOf(remittanceValue, 
                    PrivilegesProviderServer.getAsInt(senderUUID, EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.id(), MailConfig.REMITTANCE_POSTAGE_PERCENT.asInt()));
            if (CurrencyHelperServer.enoughCurrency(senderUUID, remittanceValue + remittancePostage, OxygenMain.COMMON_CURRENCY_INDEX)) {
                CurrencyHelperServer.removeCurrency(senderUUID, remittanceValue + remittancePostage, OxygenMain.COMMON_CURRENCY_INDEX);
                SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.RINGING_COINS.getId());
                return true;
            }
        }
        return false;
    }

    private boolean processPackage(EntityPlayerMP senderMP, UUID senderUUID, Attachment attachment) {
        int maxAmount = PrivilegesProviderServer.getAsInt(senderUUID, EnumMailPrivilege.PARCEL_MAX_AMOUNT.id(), MailConfig.PACKAGE_MAX_AMOUNT.asInt());
        final ItemStack itemStack = attachment.getStackWrapper().getItemStack();
        if (maxAmount < 0) 
            maxAmount = itemStack.getMaxStackSize();
        if (attachment.getItemAmount() <= maxAmount) {
            long packagePostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
            boolean postageExist = packagePostage > 0;
            if (InventoryProviderServer.getPlayerInventory().getEqualItemAmount(senderMP, attachment.getStackWrapper()) >= attachment.getItemAmount()
                    && (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX))) {
                final int amount = attachment.getItemAmount();
                InventoryProviderServer.getPlayerInventory().removeItem(senderMP, attachment.getStackWrapper(), amount);

                if (postageExist) {
                    CurrencyHelperServer.removeCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX);
                    SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.RINGING_COINS.getId());
                }
                return true;
            }
        }
        return false;
    }

    private boolean processCOD(EntityPlayerMP senderMP, UUID senderUUID, Attachment attachment) {
        if (attachment.getCurrencyValue() <= PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.COD_MAX_VALUE.id(), MailConfig.COD_MAX_VALUE.asLong())) {
            int maxAmount = PrivilegesProviderServer.getAsInt(senderUUID, EnumMailPrivilege.PARCEL_MAX_AMOUNT.id(), MailConfig.PACKAGE_MAX_AMOUNT.asInt());
            final ItemStack itemStack = attachment.getStackWrapper().getItemStack();
            if (maxAmount < 0) 
                maxAmount = itemStack.getMaxStackSize();
            if (attachment.getItemAmount() <= maxAmount) {
                long packagePostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
                boolean postageExist = packagePostage > 0;
                if (InventoryProviderServer.getPlayerInventory().getEqualItemAmount(senderMP, attachment.getStackWrapper()) >= attachment.getItemAmount()
                        && (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX))) {
                    final int amount = attachment.getItemAmount();
                    InventoryProviderServer.getPlayerInventory().removeItem(senderMP, attachment.getStackWrapper(), amount);
                    if (postageExist) {
                        CurrencyHelperServer.removeCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX);
                        SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.RINGING_COINS.getId());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void processMessageOperation(EntityPlayerMP playerMP, long messageId, EnumMessageOperation operation) {
        this.mailOperationsQueue.offer(new QueuedMailOperation(CommonReference.getPersistentUUID(playerMP), messageId, operation));
    }

    private void processMailOperationsQueue() {
        while (!this.mailOperationsQueue.isEmpty()) {
            final QueuedMailOperation queued = this.mailOperationsQueue.poll();
            if (queued != null)
                OxygenHelperServer.addRoutineTask(()->this.processMessageOperationQueue(queued));
        }
    }

    private void processMessageOperationQueue(QueuedMailOperation queued) {
        EntityPlayerMP playerMP = CommonReference.playerByUUID(queued.playerUUID);
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(queued.playerUUID);
        Mail mail = mailbox.getMessage(queued.messageId);
        if (mail != null) {
            switch (queued.operation) {
            case TAKE_ATTACHMENT:
                if (mail.isPending() 
                        && this.takeAttachment(playerMP, queued.playerUUID, mail)) {
                    mailbox.removeMessage(queued.messageId);

                    mail.setId(mailbox.createId(queued.messageId));
                    mail.attachmentReceived();  
                    mailbox.addMessage(mail);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPAttachmentReceived(queued.messageId, mail, CurrencyHelperServer.getCurrency(queued.playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.ATTACHMENT_RECEIVED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} took attachment {} from sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getAttachment(),
                                mail.getSenderName(),
                                mail.isSystemMessage() ? "SYSTEM" : mail.getSenderUUID());
                }  
                break;
            case RETURN:
                if (!mail.isSystemMessage() 
                        && mail.isPending()
                        && this.returnAttachmentToSender(playerMP, mail)) {
                    mailbox.removeMessage(queued.messageId);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPMessageRemoved(queued.messageId), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.MESSAGE_RETURNED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} returned attachment {} to sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getAttachment(),
                                mail.getSenderName(),
                                mail.getSenderUUID());
                }
                break;
            case REMOVE_MESSAGE:
                if (!mail.isPending()) {
                    mailbox.removeMessage(queued.messageId);

                    this.manager.getMailboxesContainer().setChanged(true);

                    OxygenMain.network().sendTo(new CPMessageRemoved(queued.messageId), playerMP);
                    this.manager.sendStatusMessages(playerMP, EnumMailStatusMessage.MESSAGE_REMOVED);

                    if (MailConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Mail] Player {}/{} removed message from sender: {}/{}.", 
                                CommonReference.getName(playerMP),
                                queued.playerUUID,
                                mail.getSenderName(),
                                mail.getSenderUUID());
                }
                break;
            }
        }
    }

    private boolean takeAttachment(EntityPlayerMP playerMP, UUID playerUUID, Mail mail) {
        if (mail.getType() == EnumMail.PARCEL 
                || mail.getType() == EnumMail.COD) {
            if (!InventoryProviderServer.getPlayerInventory().haveEnoughSpace(playerMP,  mail.getAttachment().getStackWrapper(), mail.getAttachment().getItemAmount()))
                return false;
            if (mail.getType() == EnumMail.COD) {
                if (!CurrencyHelperServer.enoughCurrency(playerUUID, mail.getAttachment().getCurrencyValue(), mail.getAttachment().getCurrencyIndex()))
                    return false;
                CurrencyHelperServer.removeCurrency(playerUUID, mail.getAttachment().getCurrencyValue(), mail.getAttachment().getCurrencyIndex());
                long codPostage = MathUtils.percentValueOf(mail.getAttachment().getCurrencyValue(), 
                        PrivilegesProviderServer.getAsInt(mail.getSenderUUID(), EnumMailPrivilege.COD_POSTAGE_PERCENT.id(), MailConfig.COD_POSTAGE_PERCENT.asInt()));
                ItemStack itemStack = mail.getAttachment().getStackWrapper().getItemStack();
                this.sendSystemMail(
                        mail.getSenderUUID(),
                        CommonReference.getName(playerMP), 
                        EnumMail.REMITTANCE,
                        "mail.cod.pay.s", 
                        Attachments.remittance(mail.getAttachment().getCurrencyIndex(), mail.getAttachment().getCurrencyValue() - codPostage),
                        true,
                        "mail.cod.pay.m",
                        String.valueOf(mail.getAttachment().getItemAmount()),
                        itemStack.getDisplayName());
            }            
            InventoryProviderServer.getPlayerInventory().addItem(playerMP, mail.getAttachment().getStackWrapper(), mail.getAttachment().getItemAmount());            
            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.INVENTORY_OPERATION.getId());
            return true;
        } else if (mail.getType() == EnumMail.REMITTANCE) {
            CurrencyHelperServer.addCurrency(playerUUID, mail.getAttachment().getCurrencyValue(), mail.getAttachment().getCurrencyIndex());
            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
            return true;
        }
        return false;
    }

    private boolean returnAttachmentToSender(EntityPlayerMP playerMP, Mail message) {
        if (message.getType() == EnumMail.REMITTANCE 
                || message.getType() == EnumMail.PARCEL
                || message.getType() == EnumMail.COD) {
            EnumMail type = message.getType();
            if (message.getType() == EnumMail.COD)
                type = EnumMail.PARCEL;
            if (this.sendSystemMail(
                    message.getSenderUUID(), 
                    Mail.SYSTEM_SENDER,
                    type,
                    String.format("RE: %s", message.getSubject()), 
                    Attachments.parcel(message.getAttachment().getStackWrapper(), message.getAttachment().getItemAmount()), 
                    true,
                    "mail.message.attachmentReturn",
                    CommonReference.getName(playerMP)))
                return true;
        }
        return false;
    }

    public boolean sendSystemMail(UUID addresseeUUID, String senderName, EnumMail type, String subject, Attachment attachment, boolean ignoreMailBoxCapacity, String message, String... messageArgs) {
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
        if (mailbox.canAcceptMessages() || ignoreMailBoxCapacity) {
            this.addMessage(mailbox, type, Mail.SYSTEM_UUID, senderName, subject, attachment, message, messageArgs);
            return true;
        }
        return false;
    }
}
