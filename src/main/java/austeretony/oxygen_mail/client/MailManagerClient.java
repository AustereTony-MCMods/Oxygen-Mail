package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.server.api.OxygenHelperServer;

public final class MailManagerClient {

    private static MailManagerClient instance;

    private final MailboxContainerClient mailboxContainer = new MailboxContainerClient();

    private final MailboxManagerClient mailboxManager;

    private final MailMenuManager mailMenuManager = new MailMenuManager();

    private MailManagerClient() {
        this.mailboxManager = new MailboxManagerClient(this);
        OxygenHelperClient.registerPersistentData(this.mailboxContainer);
    }

    public static void create() {
        if (instance == null)
            instance = new MailManagerClient();
    }

    public static MailManagerClient instance() {
        return instance;
    }

    public MailboxContainerClient getMailboxContainer() {
        return this.mailboxContainer;
    }

    public MailboxManagerClient getMailboxManager() {
        return this.mailboxManager;
    }

    public MailMenuManager getMailMenuManager() {
        return this.mailMenuManager;
    }

    public void init() {
        OxygenHelperServer.loadPersistentDataAsync(this.mailboxContainer);
    }
}