package austeretony.oxygen_mail.server;

import java.util.UUID;

import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Parcel;

public class QueuedMailSending {

    final UUID playerUUID;

    final EnumMail type;

    final String addressee, subject, message;

    final long currency;

    final Parcel parcel;

    protected QueuedMailSending(UUID playerUUID, EnumMail type, String addressee, String subject, String message, long currency, Parcel parcel) {
        this.playerUUID = playerUUID;
        this.type = type;
        this.addressee = addressee;
        this.subject = subject;
        this.message = message;
        this.currency = currency;
        this.parcel = parcel;
    }
}
