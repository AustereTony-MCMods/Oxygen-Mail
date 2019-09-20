package austeretony.oxygen_mail.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;

public class Mailbox {

    public final UUID playerUUID;

    private final Map<Long, Mail> mail = new ConcurrentHashMap<>();

    private long nextSendingTimeMillis;

    private boolean newMailExist;

    public Mailbox(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getMessagesAmount() {
        return this.mail.size();
    }

    public Collection<Mail> getMessages() {
        return this.mail.values();
    }

    public Set<Long> getMessagesIds() {
        return this.mail.keySet();
    }

    public boolean messageExist(long messageId) {
        return this.mail.containsKey(messageId);
    }

    public Mail getMessage(long messageId) {
        return this.mail.get(messageId);
    }

    public void addMessage(Mail message) {                       
        this.mail.put(message.getId(), message);
        this.newMailExist = true;
    }

    public void removeMessage(long messageId) {
        this.mail.remove(messageId);
    }

    public int getMaxCapacity() {
        return PrivilegeProviderServer.getValue(this.playerUUID, EnumMailPrivilege.MAILBOX_SIZE.toString(), MailConfig.MAILBOX_SIZE.getIntValue());
    }

    public boolean canAcceptMessages() {
        return this.getMessagesAmount() < this.getMaxCapacity();
    }

    public boolean canSendMessage() {
        return System.currentTimeMillis() >= this.nextSendingTimeMillis;
    }

    public void updateLastMessageSendingTime() {
        this.nextSendingTimeMillis = System.currentTimeMillis() 
                + PrivilegeProviderServer.getValue(this.playerUUID, EnumMailPrivilege.MAIL_SENDING_DELAY_SECONDS.toString(), MailConfig.MAIL_SENDING_DELAY_SECONDS.getIntValue()) * 1000;
    }

    public boolean isNewMailExist() {
        return this.newMailExist;
    }

    public void mailboxSynchronized() {
        this.newMailExist = false;
    }

    public  long getNewId(long messageId) {
        while (this.mail.containsKey(messageId))
            messageId += 1L;
        return messageId;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write(this.newMailExist, bos);
        StreamUtils.write((short) this.mail.size(), bos);
        for (Mail message : this.mail.values()) 
            message.write(bos);
    }

    public static Mailbox read(BufferedInputStream bis) throws IOException {
        Mailbox mailbox = new Mailbox(StreamUtils.readUUID(bis));
        mailbox.newMailExist = StreamUtils.readBoolean(bis);
        int amount = StreamUtils.readShort(bis);
        Mail message;
        for (int i = 0; i < amount; i++) {
            message = new Mail();
            message.read(bis);
            mailbox.mail.put(message.getId(), message);
        }
        return mailbox;
    }
}