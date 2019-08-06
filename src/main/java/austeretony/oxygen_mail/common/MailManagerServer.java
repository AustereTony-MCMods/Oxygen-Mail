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
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.Message;
import austeretony.oxygen_mail.common.main.Parcel;
import austeretony.oxygen_mail.common.main.PlayerMailbox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MailManagerServer implements IPersistentData {

    private static MailManagerServer instance;

    private final OxygenThread ioThread;

    private final Map<UUID, PlayerMailbox> mailboxes = new ConcurrentHashMap<UUID, PlayerMailbox>();

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

    public Collection<PlayerMailbox> getMailboxes() {
        return this.mailboxes.values();
    }

    public boolean isMailboxExist(UUID playerUUID) {
        return this.mailboxes.containsKey(playerUUID);
    }

    public void createMailboxForPlayer(UUID playerUUID) {
        this.mailboxes.put(playerUUID, new PlayerMailbox(playerUUID));
    }

    public PlayerMailbox getPlayerMailbox(UUID playerUUID) {
        return this.mailboxes.get(playerUUID);
    }

    private void sendNewMessageNotification(UUID playerUUID) {
        if (OxygenHelperServer.isOnline(playerUUID))
            OxygenHelperServer.addNotification(CommonReference.playerByUUID(playerUUID), 
                    new SimpleNotification(MailMain.INCOMING_MESSAGE_NOTIFICATION_ID, "oxygen_mail.incoming"));
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, Message message) {
        this.sendMail(senderMP, addresseeUsername, message.type, message.subject, message.message, message.getCurrency(), message.getParcel());
    }

    public void sendMail(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, int currency, Parcel parcel) {
        if (this.processPlayerMessageSending(senderMP, addresseeUsername, type, subject, message, currency, parcel, false))
            OxygenHelperServer.sendMessage(senderMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.MESSAGE_SENT.ordinal());
        else
            OxygenHelperServer.sendMessage(senderMP, MailMain.MAIL_MOD_INDEX, EnumMailChatMessage.MESSAGE_NOT_SENT.ordinal());
    }

    private boolean processPlayerMessageSending(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, int currency, Parcel parcel, boolean isReturn) {
        if (OxygenHelperServer.isValidUsername(addresseeUsername)) {
            UUID 
            senderUUID = CommonReference.getPersistentUUID(senderMP),
            addresseeUUID = OxygenHelperServer.getPlayerUUID(addresseeUsername);
            if (this.isMailboxExist(addresseeUUID)) { 
                //&& !targetUUID.equals(senderUUID)) {//TODO DEBUG allows players send messages to themselves
                PlayerMailbox senderMailbox = this.getPlayerMailbox(senderUUID);
                if (senderMailbox.canSendMessage()) {
                    PlayerMailbox targetMailbox = this.getPlayerMailbox(addresseeUUID);
                    if (targetMailbox.canAcceptMessages()) {
                        switch (type) {
                        case LETTER:
                            currency = 0;
                            parcel = null;
                            if (!this.processLetter(senderUUID)) {
                                //TODO debug
                                MailMain.LOGGER.error("SENDING. Sender doesn't have enough money to pay postage for LETTER sending! GUI verifications breached! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
                                return false;
                            }
                            break;
                        case REMITTANCE:
                            if (currency <= 0)
                                return false;
                            parcel = null;
                            if (!isReturn && !this.processRemittance(senderUUID, currency)) {
                                //TODO debug
                                MailMain.LOGGER.error("SENDING. Sender doesn't have enough money to pay postage for REMITTANCE sending! GUI verifications breached! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);                            
                                return false;
                            }
                            break;
                        case PACKAGE:
                            if (parcel == null)
                                return false;
                            if (parcel.amount <= 0)
                                return false;
                            currency = 0;
                            if (!isReturn && !this.processPackage(senderMP, senderUUID, parcel)) {
                                //TODO debug
                                MailMain.LOGGER.error("SENDING. Sender doesn't have enough money to pay postage for PACKAGE sending! GUI verifications breached! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
                                return false;
                            }
                            break;
                        case PACKAGE_WITH_COD:
                            if (currency <= 0 || parcel == null)
                                return false;
                            if (parcel.amount <= 0)
                                return false;
                            if (!isReturn && !this.processPackageWithCOD(senderMP, senderUUID, currency, parcel)) {
                                //TODO debug
                                MailMain.LOGGER.error("SENDING. Sender doesn't have enough money to pay postage for COD sending! GUI verifications breached! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);                          
                                return false;
                            }
                            break;
                        default://service mail
                            //TODO debug
                            MailMain.LOGGER.error("SENDING. Player attempted to send SERVICE message! GUI verifications breached! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);                          
                            return false;
                        }                 
                        this.sendMessage(
                                targetMailbox, 
                                System.currentTimeMillis(), 
                                type, 
                                CommonReference.getName(senderMP), 
                                subject, 
                                message, 
                                parcel, 
                                currency, 
                                true);
                        senderMailbox.updateLastMessageSendingTime();
                        return true;
                    } else//TODO debug
                        MailMain.LOGGER.error("SENDING. Addressee mailbox is full! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
                } else//TODO debug
                    MailMain.LOGGER.error("SENDING. Sender have sending cooldown! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
            } else//TODO debug
                MailMain.LOGGER.error("SENDING. Addressee mailbox not exist (impossible)! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
        } else//TODO debug
            MailMain.LOGGER.error("SENDING. Invalid addressee username! Sender: {}, Addressee: {}.", CommonReference.getName(senderMP), addresseeUsername);
        return false;
    }

    private void sendMessage(PlayerMailbox mailbox, long messageId, EnumMail type, String sender, String subject, String message, Parcel parcel, int currency, boolean save) {
        Message msg = new Message(type, sender, subject, message);
        msg.setId(messageId);
        msg.setCurrency(currency);
        msg.setParcel(parcel);
        if (currency > 0 || parcel != null)
            msg.setPending(true);
        mailbox.addMessage(msg);
        if (save)
            this.saveMailboxes();
        this.sendNewMessageNotification(mailbox.playerUUID);
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
        PlayerMailbox mailbox = this.getPlayerMailbox(playerUUID);
        if (mailbox.messageExist(messageId)) {
            Message message = mailbox.getMessage(messageId);
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
                } else//TODO debug
                    MailMain.LOGGER.error("MESSAGE REMOVING. Attempt to remove pending message! GUI verification breach! Player: {}.", CommonReference.getName(playerMP));                           
                break;
            }
        } else//TODO debug
            MailMain.LOGGER.error("MESSAGE OPERATION. Player attempted to interact with unknown message! Cheating or synchronization failure! Player: {}.", CommonReference.getName(playerMP));                          
    }

    private boolean takeAttachment(EntityPlayerMP playerMP, UUID playerUUID, Message message) {
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
                //TODO Send currency to cod sender (take cod postage)
                int codPostage = MathUtils.percentValueOf(message.getCurrency(), 
                        PrivilegeProviderServer.getPrivilegeValue(OxygenHelperServer.getPlayerUUID(message.sender), EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.getIntValue()));
                this.sendServiceRemittance(
                        OxygenHelperServer.getPlayerUUID(message.sender), 
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
        //TODO debug
        MailMain.LOGGER.error("ATTACHMENT RECEIVING. Player have not enought inventory space or money to pay C.O.D! GUI verification breach! Player: {}.", CommonReference.getName(playerMP));                          
        return false;
    }

    private boolean returnAttachmentToSender(EntityPlayerMP playerMP, Message message) {
        if (message.type == EnumMail.REMITTANCE 
                || message.type == EnumMail.PACKAGE
                || message.type == EnumMail.PACKAGE_WITH_COD) {
            if (OxygenHelperServer.isValidUsername(message.sender) 
                    && !message.sender.equals(CommonReference.getName(playerMP))) {
                EnumMail type = message.type;
                if (message.type == EnumMail.PACKAGE_WITH_COD)
                    type = EnumMail.PACKAGE;
                if (this.processPlayerMessageSending(playerMP, message.sender, type, "mail.subject.return", message.message, message.getCurrency(), message.getParcel(), true))
                    return true;
            }
        }
        //TODO debug
        MailMain.LOGGER.error("ATTACHMENT RETURNING. Addressee username is invalid (impossible) or returning to yourself! Player: {}, Addressee: {}", CommonReference.getName(playerMP), message.sender);                          
        return false;
    }

    public void processExpiredMail() {
        int removed = 0;
        for (PlayerMailbox playerMailbox : this.mailboxes.values()) {
            Iterator<Message> iterator = playerMailbox.getMessages().iterator();
            Message message;
            while (iterator.hasNext()) {
                message = iterator.next();
                if (message.isExpired()) {
                    if (message.isPending())
                        this.processExpiredMessage(message);
                    iterator.remove();
                    removed++;
                }
            }
        }
        this.saveMailboxes();
        MailMain.LOGGER.info("Expired mail processed. Removed {} messages in total", removed);
    }

    private void processExpiredMessage(Message message) {
        if (message.type == EnumMail.REMITTANCE 
                || message.type == EnumMail.PACKAGE
                || message.type == EnumMail.PACKAGE_WITH_COD) {
            if (OxygenHelperServer.isValidUsername(message.sender)) {
                UUID playerUUID = OxygenHelperServer.getPlayerUUID(message.sender);
                if (message.type == EnumMail.REMITTANCE)
                    this.sendServiceRemittance(playerUUID, "mail.sender.sys", "mail.subject.return", "mail.message.remittanceReturn", message.getCurrency(), false);
                if (message.type == EnumMail.PACKAGE 
                        || message.type == EnumMail.PACKAGE_WITH_COD)
                    this.sendServicePackage(playerUUID, "mail.sender.sys", "mail.subject.return", "mail.message.packageReturn", message.getParcel(), false);
            }
        }
    }

    public void sendServiceLetter(UUID playerUUID, String sender, String subject, String message, boolean save) {
        PlayerMailbox mailbox = this.getPlayerMailbox(playerUUID);
        if (mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue()) 
            this.sendMessage(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),//dumb protection from duplicated identifiers
                    EnumMail.SERVICE_LETTER, 
                    sender, 
                    subject, 
                    message,
                    null, 
                    0, 
                    save);
    }

    public void sendServiceRemittance(UUID playerUUID, String sender, String subject, String message, int remittanceValue, boolean save) {
        PlayerMailbox mailbox = this.getPlayerMailbox(playerUUID);
        if (mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue())
            this.sendMessage(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),
                    EnumMail.SERVICE_REMITTANCE, 
                    sender, 
                    subject, 
                    message,
                    null, 
                    remittanceValue, 
                    save);
    }

    public void sendServicePackage(UUID playerUUID, String sender, String subject, String message, Parcel parcel, boolean save) {
        PlayerMailbox mailbox = this.getPlayerMailbox(playerUUID);
        if (mailbox.getMessagesAmount() < MailConfig.MAILBOX_SIZE.getIntValue())
            this.sendMessage(
                    mailbox, 
                    System.currentTimeMillis() + OxygenHelperServer.getRandom().nextInt(10_000),
                    EnumMail.SERVICE_PACKAGE, 
                    sender, 
                    subject, 
                    message,
                    parcel, 
                    0, 
                    save);
    }

    //TODO onPlayerLoaded()
    public void onPlayerLoaded(EntityPlayer player) {
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
        for (PlayerMailbox mailbox : this.mailboxes.values())
            mailbox.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        PlayerMailbox mailbox;
        for (int i = 0; i < amount; i++) {
            mailbox = PlayerMailbox.read(bis);
            this.mailboxes.put(mailbox.playerUUID, mailbox);
        }
    }

    public void reset() {
        this.mailboxes.clear();
    }
}
