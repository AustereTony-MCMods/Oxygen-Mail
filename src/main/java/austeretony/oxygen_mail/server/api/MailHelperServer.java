package austeretony.oxygen_mail.server.api;

import java.util.UUID;

import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.server.MailManagerServer;

public class MailHelperServer {

    public static boolean sendSystemLetter(UUID addresseeUUID, String senderName, String subject, String message, boolean ignoreMailBoxCapacity) {
        return MailManagerServer.instance().getMailboxesManager().sendSystemLetter(addresseeUUID, senderName, subject, message, ignoreMailBoxCapacity);
    }

    public static boolean sendSystemRemittance(UUID addresseeUUID, String senderName, String subject, String message, long remittanceValue, boolean ignoreMailBoxCapacity) {
        return MailManagerServer.instance().getMailboxesManager().sendSystemRemittance(addresseeUUID, senderName, subject, message, remittanceValue, ignoreMailBoxCapacity);
    }

    public static boolean sendSystemPackage(UUID addresseeUUID, String senderName, String subject, String message, Parcel parcel, boolean ignoreMailBoxCapacity) {
        return MailManagerServer.instance().getMailboxesManager().sendSystemPackage(addresseeUUID, senderName, subject, message, parcel, ignoreMailBoxCapacity);
    }
}
