package austeretony.oxygen_mail.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.WatcherHelperServer;
import austeretony.oxygen.common.api.notification.SimpleNotification;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.currency.CurrencyHelperServer;
import austeretony.oxygen.common.delegate.OxygenThread;
import austeretony.oxygen.common.itemstack.InventoryHelper;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMail;
import austeretony.oxygen_mail.common.main.EnumMailChatMessage;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.Mail;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.Mailbox;
import austeretony.oxygen_mail.common.main.Parcel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MailManagerServer implements IPersistentData {

    private static MailManagerServer instance;

    private final OxygenThread ioThread;

    private final Map<UUID, Mailbox> mailboxes = new ConcurrentHashMap<UUID, Mailbox>();

    public MailManagerServer() {
        this.ioThread = new OxygenThread("Mail IO Thread");
        this.ioThread.start();
    }

    public static void create() {
        if (instance == null)
            instance = new MailManagerServer();
    }

    public static MailManagerServer instance() {
        return instance;
    }

    public OxygenThread getIOThread() {
        return this.ioThread;
    }

    public void loadMailboxes() {
        this.reset();
        MailLoaderServer.loadPersistentDataDelegated(this);
    }

    public void saveMailboxes() {
        MailLoaderServer.savePersistentDataDelegated(this);
    }

    public int getMailboxesAmount() {
        return this.mailboxes.size();
    }

    public Collection<Mailbox> getMailboxes() {
        return this.mailboxes.values();
    }

    public boolean isMailboxExist(UUID playerUUID) {
        return this.mailboxes.containsKey(playerUUID);
    }

    public void createMailboxForPlayer(UUID playerUUID) {
        this.mailboxes.put(playerUUID, new Mailbox(playerUUID));
    }

    public Mailbox getPlayerMailbox(UUID playerUUID) {
        return this.mailboxes.get(playerUUID);
    }

    private void sendNewMessageNotification(UUID playerUUID) {
        if (OxygenHelperServer.isOnline(playerUUID))
            OxygenHelperServer.addNotification(CommonReference.playerByUUID(playerUUID), 
                    new SimpleNotification(MailMain.INCOMING_MESSAGE_NOTIFICATION_ID, "oxygen_mail.incoming"));
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, Mail message) {
        this.sendMail(senderMP, addresseeUsername, message.type, message.subject, message.message, message.getCurrency(), message.getParcel());
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, int currency, Parcel parcel) {
        UUID addresseeUUID = OxygenHelperServer.getPlayerUUID(addresseeUsername);
        if (addresseeUUID != null && this.processPlayerMailSending(senderMP, type, addresseeUUID, subject, message, currency, parcel, false))
            OxygenHelperServer.sendMessage(senderMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.MESSAGE_SENT.ordinal());
        else
            OxygenHelperServer.sendMessage(senderMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.MESSAGE_NOT_SENT.ordinal());
    }

    private boolean processPlayerMailSending(EntityPlayerMP senderMP, EnumMail type, UUID addresseeUUID, String subject, String message, int currency, Parcel parcel, boolean isReturn) {
        UUID senderUUID = CommonReference.getPersistentUUID(senderMP);
        if (!this.isMailboxExist(addresseeUUID))
            this.createMailboxForPlayer(addresseeUUID);
        if (!addresseeUUID.equals(senderUUID)) {
            Mailbox senderMailbox = this.getPlayerMailbox(senderUUID);
            if (senderMailbox.canSendMessage()) {
                Mailbox targetMailbox = this.getPlayerMailbox(addresseeUUID);
                if (targetMailbox.canAcceptMessages()) {
                    switch (type) {
                    case LETTER:
                        currency = 0;
                        parcel = null;
                        if (!this.processLetter(senderUUID))
                            return false;
                        break;
                    case REMITTANCE:
                        if (currency <= 0)
                            return false;
                        parcel = null;
                        if (!isReturn && !this.processRemittance(senderUUID, currency))
                            return false;
                        break;
                    case PACKAGE:
                        if (parcel == null)
                            return false;
                        if (parcel.amount <= 0)
                            return false;
                        currency = 0;
                        if (!isReturn && !this.processPackage(senderMP, senderUUID, parcel))
                            return false;
                        break;
                    case PACKAGE_WITH_COD:
                        if (currency <= 0 || parcel == null)
                            return false;
                        if (parcel.amount <= 0)
                            return false;
                        if (!isReturn && !this.processPackageWithCOD(senderMP, senderUUID, currency, parcel))
                            return false;
                        break;
                    default://service mail
                        return false;
                    }                 
                    this.sendMail(
                            targetMailbox, 
                            System.currentTimeMillis(), 
                            type, 
                            senderUUID,
                            CommonReference.getName(senderMP), 
                            subject, 
                            message, 
                            parcel, 
                            currency, 
                            true);
                    senderMailbox.updateLastMessageSendingTime();
                    return true;
                }
            }
        }
        return false;
    }

    private void sendMail(Mailbox targetMailbox, long mailId, EnumMail type, UUID senderUUID, String senderName, String subject, String message, Parcel parcel, int currency, boolean save) {
        Mail msg = new Mail(type, senderUUID, senderName, subject, message);
        msg.setId(mailId);
        msg.setCurrency(currency);
        msg.setParcel(parcel);
        if (currency > 0 || parcel != null)
            msg.setPending(true);
        targetMailbox.addMessage(msg);
        if (save)
            this.saveMailboxes();
        this.sendNewMessageNotification(targetMailbox.playerUUID);
    }

    private boolean processLetter(UUID playerUUID) {
        int letterPostage = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.LETTER_POSTAGE_VALUE.toString(), MailConfig.LETTER_POSTAGE_VALUE.getIntValue());
        boolean postageExist = letterPostage > 0;//avoid dummy currency operations
        if (!postageExist || CurrencyHelperServer.enoughCurrency(playerUUID, letterPostage)) {
            if (postageExist) {
                CurrencyHelperServer.removeCurrency(playerUUID, letterPostage);
                WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
                CurrencyHelperServer.save(playerUUID);
            }
            return true;
        }
        return false;
    }

    private boolean processRemittance(UUID playerUUID, int remittanceValue) {
        if (remittanceValue <= PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.REMITTANCE_MAX_VALUE.toString(), MailConfig.REMITTANCE_MAX_VALUE.getIntValue())) {
            int remittancePostage = MathUtils.percentValueOf(remittanceValue, 
                    PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.toString(), MailConfig.REMITTANCE_POSTAGE_PERCENT.getIntValue()));
            if (CurrencyHelperServer.enoughCurrency(playerUUID, remittanceValue + remittancePostage)) {
                CurrencyHelperServer.removeCurrency(playerUUID, remittanceValue + remittancePostage);
                WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
                CurrencyHelperServer.save(playerUUID);
                return true;
            }
        }
        return false;
    }

    private boolean processPackage(EntityPlayerMP senderMP, UUID playerUUID, Parcel parcel) {
        int maxAmount = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), MailConfig.PACKAGE_MAX_AMOUNT.getIntValue());
        if (!ItemsBlackList.instance().isBlackListed(Item.getItemById(parcel.stackWrapper.itemId))) {
            ItemStack itemStack = parcel.stackWrapper.getItemStack();
            if (maxAmount < 0) 
                maxAmount = itemStack.getMaxStackSize();
            if (parcel.amount <= maxAmount) {
                int packagePostage = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getIntValue());
                boolean postageExist = packagePostage > 0;
                if (InventoryHelper.getEqualStackAmount(senderMP, itemStack) >= parcel.amount
                        && (!postageExist || CurrencyHelperServer.enoughCurrency(playerUUID, packagePostage))) {
                    InventoryHelper.removeEqualStack(senderMP, itemStack, parcel.amount);
                    if (postageExist) {
                        CurrencyHelperServer.removeCurrency(playerUUID, packagePostage);
                        WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
                        CurrencyHelperServer.save(playerUUID);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processPackageWithCOD(EntityPlayerMP senderMP, UUID playerUUID, int currency, Parcel parcel) {
        if (currency <= PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.toString(), MailConfig.PACKAGE_WITH_COD_MAX_VALUE.getIntValue())) {
            if (!ItemsBlackList.instance().isBlackListed(Item.getItemById(parcel.stackWrapper.itemId))) {
                int maxAmount = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), MailConfig.PACKAGE_MAX_AMOUNT.getIntValue());
                ItemStack itemStack = parcel.stackWrapper.getItemStack();
                if (maxAmount < 0) 
                    maxAmount = itemStack.getMaxStackSize();
                if (parcel.amount <= maxAmount) {
                    int packagePostage = PrivilegeProviderServer.getPrivilegeValue(playerUUID, EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getIntValue());
                    boolean postageExist = packagePostage > 0;
                    if (InventoryHelper.getEqualStackAmount(senderMP, itemStack) >= parcel.amount
                            && (!postageExist || CurrencyHelperServer.enoughCurrency(playerUUID, packagePostage))) {
                        InventoryHelper.removeEqualStack(senderMP, itemStack, parcel.amount);
                        if (postageExist) {
                            CurrencyHelperServer.removeCurrency(playerUUID, packagePostage);
                            WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
                            CurrencyHelperServer.save(playerUUID);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void processMessageOperation(EntityPlayerMP playerMP, long messageId, EnumMessageOperation operation) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        Mailbox mailbox = this.getPlayerMailbox(playerUUID);
        if (mailbox.messageExist(messageId)) {
            Mail message = mailbox.getMessage(messageId);
            switch (operation) {
            case TAKE_ATTACHMENT:
                if (message.isPending() 
                        && this.takeAttachment(playerMP, playerUUID, message)) {
                    mailbox.removeMessage(messageId);
                    message.setId(messageId + 1L);
                    message.setPending(false);   
                    mailbox.addMessage(message);
                    this.saveMailboxes();
                    OxygenHelperServer.sendMessage(playerMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.ATTACHMENT_RECEIVED.ordinal());
                }  
                break;
            case RETURN:
                if (message.isPending()
                        && this.returnAttachmentToSender(playerMP, message)) {
                    mailbox.removeMessage(messageId);
                    this.saveMailboxes();
                    OxygenHelperServer.sendMessage(playerMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.ATTACHMENT_RETURNED.ordinal());
                }
                break;
            case REMOVE_MESSAGE:
                if (!message.isPending()) {
                    mailbox.removeMessage(messageId);
                    this.saveMailboxes();
                    OxygenHelperServer.sendMessage(playerMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.MESSAGE_REMOVED.ordinal());
                }
                break;
            }
        }
    }

    private boolean takeAttachment(EntityPlayerMP playerMP, UUID playerUUID, Mail message) {
        if (message.type == EnumMail.PACKAGE 
                || message.type == EnumMail.SERVICE_PACKAGE 
                || message.type == EnumMail.PACKAGE_WITH_COD) {
            if (!InventoryHelper.haveEnoughSpace(playerMP, message.getParcel().amount))
                return false;
            if (message.type == EnumMail.PACKAGE_WITH_COD) {
                if (!CurrencyHelperServer.enoughCurrency(playerUUID, message.getCurrency()))
                    return false;
                CurrencyHelperServer.removeCurrency(playerUUID, message.getCurrency());
                WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
                CurrencyHelperServer.save(playerUUID);
                int codPostage = MathUtils.percentValueOf(message.getCurrency(), 
                        PrivilegeProviderServer.getPrivilegeValue(OxygenHelperServer.getPlayerUUID(message.senderName), EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.getIntValue()));
                this.sendServiceRemittance(
                        OxygenHelperServer.getPlayerUUID(message.senderName), 
                        CommonReference.getName(playerMP), 
                        "mail.cod.pay.s", 
                        "mail.cod.pay.m", 
                        message.getCurrency() - codPostage,
                        true);
            }
            InventoryHelper.addItemStack(playerMP, message.getParcel().stackWrapper.getItemStack(), message.getParcel().amount); 
            return true;
        } else if (message.type == EnumMail.REMITTANCE 
                || message.type == EnumMail.SERVICE_REMITTANCE) {
            CurrencyHelperServer.addCurrency(playerUUID, message.getCurrency());
            WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, (int) CurrencyHelperServer.getCurrency(playerUUID));
            CurrencyHelperServer.save(playerUUID);
            return true;
        }
        return false;
    }

    private boolean returnAttachmentToSender(EntityPlayerMP playerMP, Mail message) {
        if (message.type == EnumMail.REMITTANCE 
                || message.type == EnumMail.PACKAGE
                || message.type == EnumMail.PACKAGE_WITH_COD) {
            if (OxygenHelperServer.getPlayerUUID(message.senderName) != null 
                    && !message.senderName.equals(CommonReference.getName(playerMP))) {
                EnumMail type = message.type;
                if (message.type == EnumMail.PACKAGE_WITH_COD)
                    type = EnumMail.PACKAGE;
                if (this.processPlayerMailSending(playerMP, type, message.senderUUID, "mail.subject.return", message.message, message.getCurrency(), message.getParcel(), true))
                    return true;
            }
        }
        return false;
    }

    public void processExpiredMail() {
        int removed = 0;
        for (Mailbox mailbox : this.mailboxes.values()) {
            Iterator<Mail> iterator = mailbox.getMessages().iterator();
            Mail mail;
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
        this.saveMailboxes();
        MailMain.LOGGER.info("Expired mail processed. Removed {} messages in total", removed);
    }

    private void processExpiredMessage(Mail message) {
        if (message.type == EnumMail.REMITTANCE 
                || message.type == EnumMail.PACKAGE
                || message.type == EnumMail.PACKAGE_WITH_COD) {
            if (message.type == EnumMail.REMITTANCE)
                this.sendServiceRemittance(message.senderUUID, "mail.sender.sys", "mail.subject.return", "mail.message.remittanceReturn", message.getCurrency(), false);
            if (message.type == EnumMail.PACKAGE 
                    || message.type == EnumMail.PACKAGE_WITH_COD)
                this.sendServicePackage(message.senderUUID, "mail.sender.sys", "mail.subject.return", "mail.message.packageReturn", message.getParcel(), false);
        }
    }

    public void sendServiceLetter(UUID addresseeUUID, String senderName, String subject, String message, boolean save) {
        Mailbox mailbox = this.getPlayerMailbox(addresseeUUID);
        if (mailbox != null && mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue()) 
            this.sendMail(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),//dumb protection from duplicated identifiers
                    EnumMail.SERVICE_LETTER, 
                    Mail.SYSTEM_UUID,
                    senderName, 
                    subject, 
                    message,
                    null, 
                    0, 
                    save);
    }

    public void sendServiceRemittance(UUID addresseeUUID, String senderName, String subject, String message, int remittanceValue, boolean save) {
        Mailbox mailbox = this.getPlayerMailbox(addresseeUUID);
        if (mailbox != null && mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue())
            this.sendMail(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),
                    EnumMail.SERVICE_REMITTANCE, 
                    Mail.SYSTEM_UUID,
                    senderName, 
                    subject, 
                    message,
                    null, 
                    remittanceValue, 
                    save);
    }

    public void sendServicePackage(UUID addresseeUUID, String senderName, String subject, String message, Parcel parcel, boolean save) {
        Mailbox mailbox = this.getPlayerMailbox(addresseeUUID);
        if (mailbox != null && mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue())
            this.sendMail(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),
                    EnumMail.SERVICE_PACKAGE, 
                    Mail.SYSTEM_UUID,
                    senderName, 
                    subject, 
                    message,
                    parcel, 
                    0, 
                    save);
    }

    public void playerLoaded(EntityPlayer player) {
        UUID playerUUID = CommonReference.getPersistentUUID(player);
        if (!this.isMailboxExist(playerUUID))
            this.createMailboxForPlayer(playerUUID);
    }

    @Override
    public String getName() {
        return "mailboxes";
    }

    @Override
    public String getModId() {
        return MailMain.MODID;
    }

    @Override
    public String getPath() {
        return "world/mail/mailboxes.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.mailboxes.size(), bos);
        for (Mailbox mailbox : this.mailboxes.values())
            mailbox.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        Mailbox mailbox;
        for (int i = 0; i < amount; i++) {
            mailbox = Mailbox.read(bis);
            this.mailboxes.put(mailbox.playerUUID, mailbox);
        }
        MailMain.LOGGER.info("Loaded {} mailboxes.", amount);
    }

    public void reset() {
        this.mailboxes.clear();
    }
}