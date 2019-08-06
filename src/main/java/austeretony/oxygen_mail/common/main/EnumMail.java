package austeretony.oxygen_mail.common.main;

import austeretony.oxygen.client.core.api.ClientReference;

public enum EnumMail {

    SERVICE_LETTER,
    LETTER,
    SERVICE_REMITTANCE,
    REMITTANCE,
    SERVICE_PACKAGE,
    PACKAGE,
    PACKAGE_WITH_COD;

    public String localizedName() {
        return ClientReference.localize("oxygen_mail.mail." + this.toString().toLowerCase());
    }
}
