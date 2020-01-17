package austeretony.oxygen_mail.server;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.item.ItemsBlackList;

public final class MailManagerServer {

    private static MailManagerServer instance;

    private final MailboxesContainerServer mailboxesContainer = new MailboxesContainerServer();

    private final MailboxesManagerServer mailboxesManager;

    private final ItemsBlackList itemsBlackList = ItemsBlackList.create("mail");

    private MailManagerServer() {
        this.mailboxesManager = new MailboxesManagerServer(this);
    }

    private void registerPersistentData() {
        OxygenHelperServer.registerPersistentData(this.mailboxesContainer);
    }

    private void scheduleRepeatableProcesses() {
        OxygenHelperServer.getSchedulerExecutorService().scheduleAtFixedRate(
                ()->{
                    this.mailboxesManager.processMailSendingQueue();
                    this.mailboxesManager.processMailOperationsQueue();
                }, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public static void create() {
        if (instance == null) {
            instance = new MailManagerServer();
            instance.registerPersistentData();
            instance.scheduleRepeatableProcesses();
        }
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
}
