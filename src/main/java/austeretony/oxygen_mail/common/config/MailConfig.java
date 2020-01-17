package austeretony.oxygen_mail.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_MAIL_MENU_KEY = ConfigValueUtils.getValue("client", "enable_mail_menu_key", true),

    MAILBOX_SIZE = ConfigValueUtils.getValue("server", "mailbox_size", 30, true),

    SYSTEM_LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_letter_expire_time_hours", - 1, true),
    LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "letter_expire_time_hours", 240, true),
    SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_remittance_expire_time_hours", - 1, true),
    REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "remittance_expire_time_hours", 24, true),
    SYSTEM_PACKAGE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_package_expire_time_hours", - 1, true),
    PACKAGE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "package_expire_time_hours", 24, true),
    PACKAGE_WITH_COD_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "package_with_cod_expire_time_hours", 1, true),

    MAIL_SENDING_COOLDOWN_SECONDS = ConfigValueUtils.getValue("server", "mail_sending_cooldown_seconds", 30),
    REMITTANCE_MAX_VALUE = ConfigValueUtils.getValue("server", "remittance_max_value", 100000L, true),
    PACKAGE_MAX_AMOUNT = ConfigValueUtils.getValue("server", "package_max_amount", - 1, true),
    PACKAGE_WITH_COD_MAX_VALUE = ConfigValueUtils.getValue("server", "package_with_cod_max_value", 50000L, true),

    LETTER_POSTAGE_VALUE = ConfigValueUtils.getValue("server", "letter_postage_value", 0L, true),
    REMITTANCE_POSTAGE_PERCENT = ConfigValueUtils.getValue("server", "remittance_postage_percent", 5, true),
    PACKAGE_POSTAGE_VALUE = ConfigValueUtils.getValue("server", "package_postage_value", 100L, true),
    PACKAGE_WITH_COD_POSTAGE_PERCENT = ConfigValueUtils.getValue("server", "package_with_cod_postage_percent", 5, true);

    @Override
    public String getDomain() {
        return MailMain.MODID;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/mail.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_MAIL_MENU_KEY);

        values.add(MAILBOX_SIZE);

        values.add(SYSTEM_LETTER_EXPIRE_TIME_HOURS);
        values.add(LETTER_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_PACKAGE_EXPIRE_TIME_HOURS);
        values.add(PACKAGE_EXPIRE_TIME_HOURS);
        values.add(PACKAGE_WITH_COD_EXPIRE_TIME_HOURS);

        values.add(MAIL_SENDING_COOLDOWN_SECONDS);
        values.add(REMITTANCE_MAX_VALUE);
        values.add(PACKAGE_MAX_AMOUNT);
        values.add(PACKAGE_WITH_COD_MAX_VALUE);

        values.add(REMITTANCE_POSTAGE_PERCENT);
        values.add(PACKAGE_POSTAGE_VALUE);
        values.add(PACKAGE_WITH_COD_POSTAGE_PERCENT);
    }
}
