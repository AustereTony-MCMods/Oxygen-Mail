package austeretony.oxygen_mail.server;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen_core.common.sync.SynchronizedData;
import austeretony.oxygen_core.server.sync.DataSyncHandlerServer;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailDataSyncHandlerServer implements DataSyncHandlerServer {

    @Override
    public int getDataId() {
        return MailMain.MAIL_DATA_ID;
    }

    @Override
    public boolean allowSync(UUID playerUUID) {
        return true;
    }

    @Override
    public Set getIds(UUID playerUUID) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID).getMessagesIds();
    }

    @Override
    public SynchronizedData getEntry(UUID playerUUID, long entryId) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID).getMessage(entryId);
    }
}
