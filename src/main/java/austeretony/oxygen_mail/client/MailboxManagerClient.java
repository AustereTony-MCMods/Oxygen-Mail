package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;

public class MailboxManagerClient {

    private final MailManagerClient manager;

    public MailboxManagerClient(MailManagerClient manager) {
        this.manager = manager;
    }

    public void sendMessageSynced(EnumMail type, String addressee, String subject, String message, long currency, Parcel parcel) {
        OxygenMain.network().sendToServer(new SPSendMessage(type, addressee, subject, message, currency, parcel));
    }

    public void messageSent(Parcel parcel, long balance) {
        this.manager.getMailMenuManager().messageSent(parcel, balance);
    }

    public void processMessageOperationSynced(long messageId, EnumMessageOperation operation) {
        OxygenMain.network().sendToServer(new SPMessageOperation(messageId, operation));
    }

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        Mail message = this.manager.getMailboxContainer().getMessage(oldMessageId);
        if (message != null)
            message.setPending(false);
        this.manager.getMailMenuManager().attachmentReceived(oldMessageId, parcel, balance);
    }

    public void messageRemoved(long messageId) {
        this.manager.getMailboxContainer().removeMessage(messageId);
        this.manager.getMailMenuManager().messageRemoved(messageId);
    }
}
