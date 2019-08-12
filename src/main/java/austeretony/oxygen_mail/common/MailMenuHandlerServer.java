package austeretony.oxygen_mail.common;

import java.util.Set;
import java.util.UUID;

import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen.common.sync.gui.api.IAdvancedGUIHandlerServer;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.Mailbox;
import net.minecraft.network.PacketBuffer;

public class MailMenuHandlerServer implements IAdvancedGUIHandlerServer {

    @Override
    public OxygenNetwork getNetwork() {
        return MailMain.network();
    }

    @Override
    public Set<Long> getValidIdentifiers(UUID playerUUID) {
        return MailManagerServer.instance().getPlayerMailbox(playerUUID).getMessagesIds();
    }

    @Override
    public void writeEntries(UUID playerUUID, PacketBuffer buffer, long[] entriesIds) {
        Mailbox mailbox = MailManagerServer.instance().getPlayerMailbox(playerUUID);
        for (long entryId : entriesIds)
            mailbox.getMessage(entryId).write(buffer);
        mailbox.mailboxSynchronized();
    }
}
