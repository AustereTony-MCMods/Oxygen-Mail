package austeretony.oxygen_mail.common.main;

import austeretony.oxygen.client.core.api.ClientReference;
import net.minecraft.util.text.TextComponentTranslation;

public enum EnumMailChatMessage {

    MESSAGE_SENT,
    MESSAGE_NOT_SENT,
    ATTACHMENT_RECEIVED,
    ATTACHMENT_RETURNED,
    MESSAGE_REMOVED;

    public void show(String... args) {
        switch (this) {
        case MESSAGE_SENT:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_mail.chat_message.messageSent"));
            break;
        case MESSAGE_NOT_SENT:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_mail.chat_message.messageNotSent"));
            break;
        case ATTACHMENT_RECEIVED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_mail.chat_message.attachmentReceived"));
            break;
        case ATTACHMENT_RETURNED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_mail.chat_message.attachmentReturned"));
            break;
        case MESSAGE_REMOVED:
            ClientReference.showMessage(new TextComponentTranslation("oxygen_mail.chat_message.messageRemoved"));
            break;
        }
    }
}
