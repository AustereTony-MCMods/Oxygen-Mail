package austeretony.oxygen_mail.server.api;

import java.util.UUID;

import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.server.MailManagerServer;

public class MailHelperServer {

    public static boolean canPlayerAcceptMessages(UUID playerUUID) {
        return MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID).canAcceptMessages();
    }

    public static void sendSystemLetter(UUID addresseeUUID, String senderName, String subject, String message) {
        MailManagerServer.instance().getMailboxesManager().sendSystemLetter(addresseeUUID, senderName, subject, message);
    }

    public static void sendSystemRemittance(UUID addresseeUUID, String senderName, String subject, String message, long remittanceValue) {
        MailManagerServer.instance().getMailboxesManager().sendSystemRemittance(addresseeUUID, senderName, subject, message, remittanceValue);
    }

    public static void sendSystemPackage(UUID addresseeUUID, String senderName, String subject, String message, Parcel parcel) {
        MailManagerServer.instance().getMailboxesManager().sendSystemPackage(addresseeUUID, senderName, subject, message, parcel);
    }
}
