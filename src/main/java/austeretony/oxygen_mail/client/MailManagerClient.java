package austeretony.oxygen_mail.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen.common.main.EnumOxygenPrivilege;
import austeretony.oxygen.common.main.OxygenPlayerData.EnumActivityStatus;
import austeretony.oxygen.common.main.SharedPlayerData;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.Message;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;
import io.netty.util.internal.ConcurrentSet;

public class MailManagerClient implements IPersistentData {

    private static MailManagerClient instance;

    private final Map<Long, Message> mail = new ConcurrentHashMap<Long, Message>();

    private final Set<Long> read = new ConcurrentSet<Long>();

    public static void create() {
        if (instance == null)
            instance = new MailManagerClient();
    }

    public static MailManagerClient instance() {
        return instance;
    }

    public void loadMail() {
        this.reset();
        OxygenHelperClient.loadPersistentDataDelegated(this);
    }

    public void saveMail() {
        OxygenHelperClient.savePersistentDataDelegated(this);
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

    public Message getMessage(long messageId) {
        return this.mail.get(messageId);
    }

    public void addMessage(Message message) {
        this.mail.put(message.getId(), message);
    }

    public void removeMessage(long messageId) {
        this.mail.remove(messageId);
    }

    public boolean isMarkedAsRead(long messageId) {
        return this.read.contains(messageId);
    }

    public void markAsRead(long messageId) {
        this.read.add(messageId);
    }

    public void removeReadMark(long messageId) {
        this.read.remove(messageId);
    }

    public void processMessageOperationSynced(long messageId, EnumMessageOperation operation) {
        if (operation == EnumMessageOperation.RETURN 
                || operation == EnumMessageOperation.REMOVE_MESSAGE) {
            this.removeMessage(messageId);
			this.removeReadMark(messageId);
            this.saveMail();
        }
        MailMain.network().sendToServer(new SPMessageOperation(messageId, operation));
    }

    public void sendMessageSynced(String addresseeUsername, Message message) {
        MailMain.network().sendToServer(new SPSendMessage(addresseeUsername, message));
    }

    public static boolean isPlayerAvailable(String username) {
        //TODO DEBUG. Allows sending messages to yourself.
        //if (username.equals(OxygenHelperClient.getSharedClientPlayerData().getUsername()))
            //return false;
        SharedPlayerData sharedData = OxygenHelperClient.getSharedPlayerData(username);
        if (sharedData != null
                && OxygenHelperClient.getPlayerStatus(sharedData) != EnumActivityStatus.OFFLINE || PrivilegeProviderClient.getPrivilegeValue(EnumOxygenPrivilege.EXPOSE_PLAYERS_OFFLINE.toString(), false))
            return true;
        return false;
    }

    @Override
    public String getName() {
        return "mailbox";
    }

    @Override
    public String getModId() {
        return MailMain.MODID;
    }

    @Override
    public String getPath() {
        return "world/mail/mail.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((short) this.mail.size(), bos);
        for (Message message : this.mail.values())
            message.write(bos);
        StreamUtils.write((short) this.read.size(), bos);
        for (long messageId : this.read)
            StreamUtils.write(messageId, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int 
        amount = StreamUtils.readShort(bis),
        i = 0;
        Message message;
        for (; i < amount; i++) {
            message = Message.read(bis);
            this.mail.put(message.getId(), message);
        }
        amount = StreamUtils.readShort(bis);
        for (i = 0; i < amount; i++)
            this.read.add(StreamUtils.readLong(bis));
    }

    public void reset() {
        this.mail.clear();
    }
}
