package austeretony.oxygen_mail.common;

import austeretony.oxygen_core.client.api.ClientReference;

public enum EnumMail {

    SYSTEM_LETTER("system_letter"),
    LETTER("letter"),
    SYSTEM_REMITTANCE("system_remittance"),
    REMITTANCE("remittance"),
    SYSTEM_PACKAGE("system_package"),
    PACKAGE("package"),
    PACKAGE_WITH_COD("package_with_cod");

    private final String name;

    EnumMail(String name) {
        this.name = name;
    }

    public String localizedName() {
        return ClientReference.localize("oxygen_mail.mail." + this.name);
    }
}