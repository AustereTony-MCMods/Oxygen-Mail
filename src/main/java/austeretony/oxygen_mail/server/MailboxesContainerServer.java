package austeretony.oxygen_mail.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailboxesContainerServer extends AbstractPersistentData {

    private final Map<UUID, Mailbox> mailboxes = new ConcurrentHashMap<>();

    public int getMailboxesAmount() {
        return this.mailboxes.size();
    }

    public Collection<Mailbox> getMailboxes() {
        return this.mailboxes.values();
    }

    public void createMailboxForPlayer(UUID playerUUID) {
        this.mailboxes.put(playerUUID, new Mailbox(playerUUID));
    }

    public Mailbox getPlayerMailbox(UUID playerUUID) {
        Mailbox mailbox = this.mailboxes.get(playerUUID);
        if (mailbox == null) {
            mailbox = new Mailbox(playerUUID);
            this.mailboxes.put(playerUUID, mailbox);
        }
        return mailbox;
    }

    @Override
    public String getDisplayName() {
        return "mailboxes";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/mail/mailboxes.dat";
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
        MailManagerServer.instance().getMailboxesManager().processExpiredMail();
    }

    @Override
    public void reset() {
        this.mailboxes.clear();
    }
}
