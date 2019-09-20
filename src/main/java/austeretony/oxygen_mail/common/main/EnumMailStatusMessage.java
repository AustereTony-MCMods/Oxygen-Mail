package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumMailStatusMessage {

    PLAYER_NOT_FOUND("playerNotFound"),
    PARCEL_DAMAGED("parcelDamaged"),
    ITEM_BLACKLISTED("itemBlackListed"),
    MESSAGE_SENT("messageSent"),
    MESSAGE_SENDING_FAILED("messageSendingFailed"),
    ATTACHMENT_RECEIVED("attachmentReceived"),
    MESSAGE_RETURNED("messageReturned"),
    MESSAGE_REMOVED("messageRemoved");

    private final String status;

    EnumMailStatusMessage(String status) {
        this.status = "oxygen_mail.status." + status;
    }

    public String localizedName() {
        return ClientReference.localize(this.status);
    }
}
