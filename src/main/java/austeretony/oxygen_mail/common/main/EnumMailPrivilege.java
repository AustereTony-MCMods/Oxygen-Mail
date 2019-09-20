package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumMailPrivilege {

    MAILBOX_SIZE("mailboxSize", EnumValueType.INT),

    MAIL_SENDING_DELAY_SECONDS("mailSendingDelaySeconds", EnumValueType.INT),
    REMITTANCE_MAX_VALUE("remittanceMaxValue", EnumValueType.LONG),
    PACKAGE_MAX_AMOUNT("packageMaxAmount", EnumValueType.INT),
    PACKAGE_WITH_COD_MAX_VALUE("packageWithCODMaxValue", EnumValueType.LONG),

    LETTER_POSTAGE_VALUE("letterPostageValue", EnumValueType.LONG),
    REMITTANCE_POSTAGE_PERCENT("remittancePostagePercent", EnumValueType.INT),
    PACKAGE_POSTAGE_VALUE("packagePostageValue", EnumValueType.LONG),
    PACKAGE_WITH_COD_POSTAGE_PERCENT("packageWithCODPostagePercent", EnumValueType.INT);

    private final String name;

    private final EnumValueType type;

    EnumMailPrivilege(String name, EnumValueType type) {
        this.name = "mail:" + name;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void register() {
        for (EnumMailPrivilege privilege : EnumMailPrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.type);
    }
}
