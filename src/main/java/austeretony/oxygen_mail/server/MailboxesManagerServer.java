package austeretony.oxygen_mail.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.notification.SimpleNotification;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.network.client.CPAttachmentReceived;
import austeretony.oxygen_mail.common.network.client.CPMessageRemoved;
import austeretony.oxygen_mail.common.network.client.CPMessageSent;
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
            MailMain.LOGGER.info("Expired mail processed. Removed {} messages in total", removed);
        });
    }

    private void processExpiredMessage(Mail message) {
        if (message.getType() == EnumMail.REMITTANCE 
                || message.getType() == EnumMail.PACKAGE
                || message.getType() == EnumMail.PACKAGE_WITH_COD) {
            if (message.getType() == EnumMail.REMITTANCE)
                this.sendSystemRemittance(message.getSenderUUID(), "mail.sender.sys", "mail.subject.return", "mail.message.remittanceReturn", message.getCurrency(), true);
            if (message.getType() == EnumMail.PACKAGE 
                    || message.getType() == EnumMail.PACKAGE_WITH_COD)
                this.sendSystemPackage(message.getSenderUUID(), "mail.sender.sys", "mail.subject.return", "mail.message.packageReturn", message.getParcel(), true);
        }
    }

    private void sendNewMessageNotification(UUID playerUUID) {
        if (OxygenHelperServer.isPlayerOnline(playerUUID))
            OxygenHelperServer.addNotification(CommonReference.playerByUUID(playerUUID), new SimpleNotification(MailMain.INCOMING_MESSAGE_NOTIFICATION_ID, "oxygen_mail.incoming"));
    }

    public void informPlayer(EntityPlayerMP playerMP, EnumMailStatusMessage status) {
        OxygenHelperServer.sendStatusMessage(playerMP, MailMain.MAIL_MOD_INDEX, status.ordinal());
    }

    public void sendMail(EntityPlayerMP senderMP, EnumMail type, String addressee, String subject, String message, long currency, Parcel parcel) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(senderMP), EnumMailPrivilege.ALLOW_MAIL_SENDING.id(), true))
            this.mailSendingQueue.offer(new QueuedMailSending(CommonReference.getPersistentUUID(senderMP), type, addressee, subject, message, currency, parcel));
    }

    void processMailSendingQueue() {
        while (!this.mailSendingQueue.isEmpty()) {
            final QueuedMailSending queued = this.mailSendingQueue.poll();
            if (queued != null) {
                final EntityPlayerMP senderMP = CommonReference.playerByUUID(queued.playerUUID);
                if (senderMP != null)
                    OxygenHelperServer.addRoutineTask(()->this.sendMailQueue(senderMP, queued.type, queued.addressee, queued.subject, queued.message, queued.currency, queued.parcel));
            }
        }
    }

    private void sendMailQueue(EntityPlayerMP senderMP, EnumMail type, String addressee, String subject, String message, long currency, Parcel parcel) {
        PlayerSharedData sharedData = OxygenHelperServer.getPlayerSharedData(addressee);
        if (sharedData == null) {
            this.informPlayer(senderMP, EnumMailStatusMessage.PLAYER_NOT_FOUND);
            return;
        }
        if (this.processPlayerMailSending(senderMP, type, sharedData.getPlayerUUID(), subject, message, currency, parcel, false))
            this.informPlayer(senderMP, EnumMailStatusMessage.MESSAGE_SENT);
        else
            this.informPlayer(senderMP, EnumMailStatusMessage.MESSAGE_SENDING_FAILED);
    }

    private boolean processPlayerMailSending(EntityPlayerMP senderMP, EnumMail type, UUID addresseeUUID, String subject, String message, long currency, Parcel parcel, boolean isReturn) {
        subject = subject.trim();
        if (subject.isEmpty())
            return false;
        if (subject.length() > Mail.MESSAGE_SUBJECT_MAX_LENGTH)
            subject = subject.substring(0, Mail.MESSAGE_SUBJECT_MAX_LENGTH);
        message = message.trim();
        if (message.length() > Mail.MESSAGE_MAX_LENGTH)
            message = message.substring(0, Mail.MESSAGE_MAX_LENGTH);
        UUID senderUUID = CommonReference.getPersistentUUID(senderMP);
        if (!addresseeUUID.equals(senderUUID)) {
            Mailbox senderMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(senderUUID);
            if (senderMailbox.canSendMessage()) {
                Mailbox targetMailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
                if (targetMailbox.canAcceptMessages()) {
                    switch (type) {
                    case LETTER:
                        currency = 0L;
                        parcel = null;
                        if (message.isEmpty() || !this.processLetter(senderMP, senderUUID))
                            return false;
                        break;
                    case REMITTANCE:
                        if (currency <= 0L)
                            return false;
                        parcel = null;
                        if (!isReturn && !this.processRemittance(senderMP, senderUUID, currency))
                            return false;
                        break;
                    case PACKAGE:
                        if (parcel == null)
                            return false;
                        if (!this.validateParcel(senderMP, parcel))
                            return false;
                        currency = 0L;
                        if (!isReturn && !this.processPackage(senderMP, senderUUID, parcel))
                            return false;
                        break;
                    case PACKAGE_WITH_COD:
                        if (currency <= 0L || parcel == null)
                            return false;
                        if (!this.validateParcel(senderMP, parcel))
                            return false;
                        if (!isReturn && !this.processPackageWithCOD(senderMP, senderUUID, currency, parcel))
                            return false;
                        break;
                    default:
                        return false;
                    }                 
                    senderMailbox.applySendingCooldown();
                    this.addMessage(targetMailbox, type, senderUUID, CommonReference.getName(senderMP), subject, message, parcel, currency);
                    OxygenMain.network().sendTo(new CPMessageSent(parcel, CurrencyHelperServer.getCurrency(senderUUID, OxygenMain.COMMON_CURRENCY_INDEX)), senderMP);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateParcel(EntityPlayerMP playerMP, Parcel parcel) {
        if (parcel.stackWrapper.itemId == Item.getIdFromItem(Items.AIR) || parcel.amount <= 0) {
            this.informPlayer(playerMP, EnumMailStatusMessage.PARCEL_DAMAGED);
            return false;
        }
        if (this.manager.getItemsBlackList().isBlackListed(Item.getItemById(parcel.stackWrapper.itemId))) {
            this.informPlayer(playerMP, EnumMailStatusMessage.ITEM_BLACKLISTED);
            return false;
        }
        return true;
    }

    private void addMessage(Mailbox targetMailbox, EnumMail type, UUID senderUUID, String senderName, String subject, String message, Parcel parcel, long currency) {
        targetMailbox.addMessage(new Mail(targetMailbox.getNewId(System.currentTimeMillis()), type, senderUUID, senderName, subject, message, currency, parcel));
        this.sendNewMessageNotification(targetMailbox.playerUUID);
        this.manager.getMailboxesContainer().setChanged(true);
    }

    private boolean processLetter(EntityPlayerMP senderMP, UUID senderUUID) {
        long letterPostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
        boolean postageExist = letterPostage > 0L;//to avoid dummy currency operations
        if (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, letterPostage, OxygenMain.COMMON_CURRENCY_INDEX)) {
            if (postageExist) {
                CurrencyHelperServer.removeCurrency(senderUUID, letterPostage, OxygenMain.COMMON_CURRENCY_INDEX);
                SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.SELL.id);
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
                SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.SELL.id);
                return true;
            }
        }
        return false;
    }

    private boolean processPackage(EntityPlayerMP senderMP, UUID senderUUID, Parcel parcel) {
        int maxAmount = PrivilegesProviderServer.getAsInt(senderUUID, EnumMailPrivilege.PACKAGE_MAX_AMOUNT.id(), MailConfig.PACKAGE_MAX_AMOUNT.asInt());
        final ItemStack itemStack = parcel.stackWrapper.getItemStack();
        if (maxAmount < 0) 
            maxAmount = itemStack.getMaxStackSize();
        if (parcel.amount <= maxAmount) {
            long packagePostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
            boolean postageExist = packagePostage > 0;
            if (InventoryHelper.getEqualStackAmount(senderMP, itemStack) >= parcel.amount
                    && (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX))) {
                final int amount = parcel.amount;
                CommonReference.delegateToServerThread(()->InventoryHelper.removeEqualStack(senderMP, itemStack, amount));
                if (postageExist) {
                    CurrencyHelperServer.removeCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX);
                    SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.SELL.id);
                }
                return true;
            }
        }
        return false;
    }

    private boolean processPackageWithCOD(EntityPlayerMP senderMP, UUID senderUUID, long currency, Parcel parcel) {
        if (currency <= PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.id(), MailConfig.PACKAGE_WITH_COD_MAX_VALUE.asLong())) {
            int maxAmount = PrivilegesProviderServer.getAsInt(senderUUID, EnumMailPrivilege.PACKAGE_MAX_AMOUNT.id(), MailConfig.PACKAGE_MAX_AMOUNT.asInt());
            final ItemStack itemStack = parcel.stackWrapper.getItemStack();
            if (maxAmount < 0) 
                maxAmount = itemStack.getMaxStackSize();
            if (parcel.amount <= maxAmount) {
                long packagePostage = PrivilegesProviderServer.getAsLong(senderUUID, EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
                boolean postageExist = packagePostage > 0;
                if (InventoryHelper.getEqualStackAmount(senderMP, itemStack) >= parcel.amount
                        && (!postageExist || CurrencyHelperServer.enoughCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX))) {
                    final int amount = parcel.amount;
                    CommonReference.delegateToServerThread(()->InventoryHelper.removeEqualStack(senderMP, itemStack, amount));
                    if (postageExist) {
                        CurrencyHelperServer.removeCurrency(senderUUID, packagePostage, OxygenMain.COMMON_CURRENCY_INDEX);
                        SoundEventHelperServer.playSoundClient(senderMP, OxygenSoundEffects.SELL.id);
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

    void processMailOperationsQueue() {
        while (!this.mailOperationsQueue.isEmpty()) {
            final QueuedMailOperation queued = this.mailOperationsQueue.poll();
            if (queued != null) {
                final EntityPlayerMP senderMP = CommonReference.playerByUUID(queued.playerUUID);
                if (senderMP != null)
                    OxygenHelperServer.addRoutineTask(()->this.processMessageOperationQueue(senderMP, queued.messageId, queued.operation));
            }
        }
    }

    private void processMessageOperationQueue(EntityPlayerMP playerMP, long messageId, EnumMessageOperation operation) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(playerUUID);
        Mail message = mailbox.getMessage(messageId);
        if (message != null) {
            switch (operation) {
            case TAKE_ATTACHMENT:
                if (message.isPending() 
                        && this.takeAttachment(playerMP, playerUUID, message)) {
                    Parcel parcel = message.getParcel();
                    mailbox.removeMessage(messageId);
                    message.setId(mailbox.getNewId(messageId));
                    message.setPending(false);   
                    mailbox.addMessage(message);
                    this.manager.getMailboxesContainer().setChanged(true);
                    OxygenMain.network().sendTo(new CPAttachmentReceived(messageId, parcel, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
                    this.informPlayer(playerMP, EnumMailStatusMessage.ATTACHMENT_RECEIVED);
                }  
                break;
            case RETURN:
                if (message.isPending()
                        && this.returnAttachmentToSender(playerMP, message)) {
                    mailbox.removeMessage(messageId);
                    this.manager.getMailboxesContainer().setChanged(true);
                    OxygenMain.network().sendTo(new CPMessageRemoved(messageId), playerMP);
                    this.informPlayer(playerMP, EnumMailStatusMessage.MESSAGE_RETURNED);
                }
                break;
            case REMOVE_MESSAGE:
                if (!message.isPending()) {
                    mailbox.removeMessage(messageId);
                    this.manager.getMailboxesContainer().setChanged(true);
                    OxygenMain.network().sendTo(new CPMessageRemoved(messageId), playerMP);
                    this.informPlayer(playerMP, EnumMailStatusMessage.MESSAGE_REMOVED);
                }
                break;
            }
        }
    }

    private boolean takeAttachment(EntityPlayerMP playerMP, UUID playerUUID, Mail message) {
        if (message.getType() == EnumMail.PACKAGE 
                || message.getType() == EnumMail.SYSTEM_PACKAGE 
                || message.getType() == EnumMail.PACKAGE_WITH_COD) {
            final ItemStack itemStack = message.getParcel().stackWrapper.getItemStack();
            if (!InventoryHelper.haveEnoughSpace(playerMP, message.getParcel().amount, itemStack.getMaxStackSize()))
                return false;
            if (message.getType() == EnumMail.PACKAGE_WITH_COD) {
                if (!CurrencyHelperServer.enoughCurrency(playerUUID, message.getCurrency(), OxygenMain.COMMON_CURRENCY_INDEX))
                    return false;
                CurrencyHelperServer.removeCurrency(playerUUID, message.getCurrency(), OxygenMain.COMMON_CURRENCY_INDEX);
                long codPostage = MathUtils.percentValueOf(message.getCurrency(), 
                        PrivilegesProviderServer.getAsInt(OxygenHelperServer.getPlayerUUID(message.getSenderUsername()), EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.id(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.asInt()));
                this.sendSystemRemittance(
                        OxygenHelperServer.getPlayerUUID(message.getSenderUsername()), 
                        CommonReference.getName(playerMP), 
                        "mail.cod.pay.s", 
                        "mail.cod.pay.m", 
                        message.getCurrency() - codPostage,
                        true);
            }
            final int amount = message.getParcel().amount;
            CommonReference.delegateToServerThread(()->InventoryHelper.addItemStack(playerMP, itemStack, amount)); 
            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.INVENTORY.id);
            return true;
        } else if (message.getType() == EnumMail.REMITTANCE 
                || message.getType() == EnumMail.SYSTEM_REMITTANCE) {
            CurrencyHelperServer.addCurrency(playerUUID, message.getCurrency(), OxygenMain.COMMON_CURRENCY_INDEX);
            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);
            return true;
        }
        return false;
    }

    private boolean returnAttachmentToSender(EntityPlayerMP playerMP, Mail message) {
        if (message.getType() == EnumMail.REMITTANCE 
                || message.getType() == EnumMail.PACKAGE
                || message.getType() == EnumMail.PACKAGE_WITH_COD) {
            if (OxygenHelperServer.getPlayerUUID(message.getSenderUsername()) != null 
                    && !message.getSenderUsername().equals(CommonReference.getName(playerMP))) {
                EnumMail type = message.getType();
                if (message.getType() == EnumMail.PACKAGE_WITH_COD)
                    type = EnumMail.PACKAGE;
                if (this.processPlayerMailSending(playerMP, type, message.getSenderUUID(), "mail.subject.return", message.getMessage(), message.getCurrency(), message.getParcel(), true))
                    return true;
            }
        }
        return false;
    }

    public boolean sendSystemLetter(UUID addresseeUUID, String senderName, String subject, String message, boolean ignoreMailBoxCapacity) {
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
        if (mailbox.canAcceptMessages() || ignoreMailBoxCapacity) {
            this.addMessage(mailbox, EnumMail.SYSTEM_LETTER, Mail.SYSTEM_UUID, senderName, subject, message, null, 0L);
            return true;
        }
        return false;
    }

    public boolean sendSystemRemittance(UUID addresseeUUID, String senderName, String subject, String message, long remittanceValue, boolean ignoreMailBoxCapacity) {
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
        if (mailbox.canAcceptMessages() || ignoreMailBoxCapacity) {
            this.addMessage(mailbox, EnumMail.SYSTEM_REMITTANCE, Mail.SYSTEM_UUID, senderName, subject, message, null, remittanceValue);
            return true;
        }
        return false;
    }

    public boolean sendSystemPackage(UUID addresseeUUID, String senderName, String subject, String message, Parcel parcel, boolean ignoreMailBoxCapacity) {
        Mailbox mailbox = this.manager.getMailboxesContainer().getPlayerMailbox(addresseeUUID);
        if (mailbox.canAcceptMessages() || ignoreMailBoxCapacity) {
            this.addMessage(mailbox, EnumMail.SYSTEM_PACKAGE, Mail.SYSTEM_UUID, senderName, subject, message, parcel, 0L);
            return true;
        }
        return false;
    }
}
