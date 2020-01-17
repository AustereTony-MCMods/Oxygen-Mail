package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.privilege.PrivilegeRegistry;

public enum EnumMailPrivilege {

    MAILBOX_SIZE("mail:mailboxSize", 800, EnumValueType.INT),
    ALLOW_MAIL_SENDING("mail:allowMailSending", 801, EnumValueType.BOOLEAN),
    MAIL_SENDING_COOLDOWN_SECONDS("mail:mailSendingCooldownSeconds", 802, EnumValueType.INT),

    REMITTANCE_MAX_VALUE("mail:remittanceMaxValue", 820, EnumValueType.LONG),
    PACKAGE_MAX_AMOUNT("mail:packageMaxAmount", 821, EnumValueType.INT),
    PACKAGE_WITH_COD_MAX_VALUE("mail:packageWithCODMaxValue", 822, EnumValueType.LONG),

    LETTER_POSTAGE_VALUE("mail:letterPostageValue", 830, EnumValueType.LONG),
    REMITTANCE_POSTAGE_PERCENT("mail:remittancePostagePercent", 831, EnumValueType.INT),
    PACKAGE_POSTAGE_VALUE("mail:packagePostageValue", 832, EnumValueType.LONG),
    PACKAGE_WITH_COD_POSTAGE_PERCENT("mail:packageWithCODPostagePercent", 833, EnumValueType.INT);

    private final String name;

    private final int id;

    private final EnumValueType type;

    EnumMailPrivilege(String name, int id, EnumValueType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public int id() {
        return this.id;
    }

    public static void register() {
        for (EnumMailPrivilege privilege : EnumMailPrivilege.values())
            PrivilegeRegistry.registerPrivilege(privilege.name, privilege.id, privilege.type);
    }
}
