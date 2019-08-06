package austeretony.oxygen_mail.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_mail.common.config.MailConfig;

public class PlayerMailbox {

    public final UUID playerUUID;

    private final Map<Long, Message> mail = new ConcurrentHashMap<Long, Message>();

    private long lastMessageSendingTime;

    private boolean newMailExist;

    public PlayerMailbox(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getMessagesAmount() {
        return this.mail.size();
    }

    public Collection<Message> getMessages() {
        return this.mail.values();
    }

    public Set<Long> getMessagesIds() {
        return this.mail.keySet();
    }

    public boolean messageExist(long messageId) {
        return this.mail.containsKey(messageId);
    }

    public Message getMessage(long messageId) {
        return this.mail.get(messageId);
    }

    public void addMessage(Message message) {
        if (this.mail.containsKey(message.getId()))//TODO debug
            MailMain.LOGGER.error("ADDING MESSAGE TO MAILBOX. Attempt adding message with existing messageId! FATAL ISSUE! Id creation algorithm improvements required! Player UUID: {}, Sender: {}", this.playerUUID, message.sender);                           
        this.mail.put(message.getId(), message);
        this.newMailExist = true;
    }

    public void removeMessage(long messageId) {
        this.mail.remove(messageId);
    }

    public boolean canSendMessage() {
        return System.currentTimeMillis() - this.lastMessageSendingTime 
                >= PrivilegeProviderServer.getPrivilegeValue(this.playerUUID, EnumMailPrivilege.MAIL_SENDING_DELAY.toString(), MailConfig.MAIL_SENDING_DELAY.getIntValue()) * 1000;
    }

    public void updateLastMessageSendingTime() {
        this.lastMessageSendingTime = System.currentTimeMillis();
    }

    public int getMaxCapacity() {
        return PrivilegeProviderServer.getPrivilegeValue(this.playerUUID, EnumMailPrivilege.MAILBOX_SIZE.toString(), MailConfig.MAILBOX_SIZE.getIntValue());
    }

    public boolean canAcceptMessages() {
        return this.getMessagesAmount() < this.getMaxCapacity();
    }

    public boolean isNewMailExist() {
        return this.newMailExist;
    }

    public void mailboxSynchronized() {
        this.newMailExist = false;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write(this.newMailExist, bos);
        StreamUtils.write((short) this.mail.size(), bos);
        for (Message message : this.mail.values()) 
            message.write(bos);
    }

    public static PlayerMailbox read(BufferedInputStream bis) throws IOException {
        PlayerMailbox mailbox = new PlayerMailbox(StreamUtils.readUUID(bis));
        mailbox.newMailExist = StreamUtils.readBoolean(bis);
        int amount = StreamUtils.readShort(bis);
        Message message;
        for (int i = 0; i < amount; i++) {
            message = Message.read(bis);
            mailbox.mail.put(message.getId(), message);
        }
        return mailbox;
    }
}
