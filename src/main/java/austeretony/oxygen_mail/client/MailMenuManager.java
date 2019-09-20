package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.common.Parcel;

public class MailMenuManager {

    public void openMailMenu() {
        ClientReference.displayGuiScreen(new MailMenuGUIScreen());
    }

    public void sharedDataSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuGUIScreen) ClientReference.getCurrentScreen()).sharedDataSynchronized();
        });
    }

    public void mailSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuGUIScreen) ClientReference.getCurrentScreen()).mailSynchronized();
        });
    }

    public void messageSent(Parcel parcel, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuGUIScreen) ClientReference.getCurrentScreen()).messageSent(parcel, balance);
        });
    }

    public void messageRemoved(long messageId) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuGUIScreen) ClientReference.getCurrentScreen()).messageRemoved(messageId);
        });
    }

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuGUIScreen) ClientReference.getCurrentScreen()).attachmentReceived(oldMessageId, parcel, balance);
        });
    }

    public static boolean isMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof MailMenuGUIScreen;
    }
}
