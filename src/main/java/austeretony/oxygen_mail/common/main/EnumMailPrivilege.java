package austeretony.oxygen_mail.common.main;

import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;

public enum EnumMailPrivilege {

    MAILBOX_SIZE(":mailboxSize"),

    MAIL_SENDING_DELAY(":mailSendingDelay"),
    REMITTANCE_MAX_VALUE(":remittanceMaxValue"),
    PACKAGE_MAX_AMOUNT(":packageMaxAmount"),
    PACKAGE_WITH_COD_MAX_VALUE(":packageWithCODMaxValue"),

    LETTER_POSTAGE_VALUE(":letterPostageValue"),
    REMITTANCE_POSTAGE_PERCENT(":remittancePostagePercent"),
    PACKAGE_POSTAGE_VALUE(":packagePostageValue"),
    PACKAGE_WITH_COD_POSTAGE_PERCENT(":packageWithCODPostagePercent");

    private final String name;

    EnumMailPrivilege(String name) {
        this.name = name;
        PrivilegeProviderServer.registerPrivilege(MailMain.MODID + name, MailMain.NAME);
    }

    @Override
    public String toString() {
        return MailMain.MODID + this.name;
    }
}
