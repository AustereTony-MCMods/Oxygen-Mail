package austeretony.oxygen_mail.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.item.ItemsBlackList;
import net.minecraft.entity.player.EntityPlayerMP;

public final class MailManagerServer {

    private static MailManagerServer instance;

    private final MailboxesContainerServer mailboxesContainer = new MailboxesContainerServer();

    private final MailboxesManagerServer mailboxesManager;

    private final ItemsBlackList itemsBlackList = ItemsBlackList.create("mail");

    private MailManagerServer() {
        this.mailboxesManager = new MailboxesManagerServer(this);
        OxygenHelperServer.registerPersistentData(this.mailboxesContainer);
    }

    public static void create() {
        if (instance == null)
            instance = new MailManagerServer();
    }

    public static MailManagerServer instance() {
        return instance;
    }

    public MailboxesContainerServer getMailboxesContainer() {
        return this.mailboxesContainer;
    }

    public MailboxesManagerServer getMailboxesManager() {
        return this.mailboxesManager;
    }

    public ItemsBlackList getItemsBlackList() {
        return this.itemsBlackList;
    }

    public void worldLoaded() {
        OxygenHelperServer.loadPersistentDataAsync(this.mailboxesContainer);
    }

    public void playerLoggedIn(EntityPlayerMP playerMP) {
        this.mailboxesManager.checkMailbox(CommonReference.getPersistentUUID(playerMP));
    }    
}
