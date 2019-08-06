package austeretony.oxygen_mail.common.api;

import java.util.UUID;

import austeretony.oxygen_mail.common.MailManagerServer;
import austeretony.oxygen_mail.common.main.Parcel;

public class MailHelperServer {

    public static void sendServiceLetter(UUID playerUUID, String sender, String subject, String message, boolean save) {
        MailManagerServer.instance().sendServiceLetter(playerUUID, sender, subject, message, save);
    }

    public static void sendServiceRemittance(UUID playerUUID, String sender, String subject, String message, int remittanceValue, boolean save) {
        MailManagerServer.instance().sendServiceRemittance(playerUUID, sender, subject, message, remittanceValue, save);
    }

    public static void sendServicePackage(UUID playerUUID, String sender, String subject, String message, Parcel parcel, boolean save) {
        MailManagerServer.instance().sendServicePackage(playerUUID, sender, subject, message, parcel, save);
    }
}