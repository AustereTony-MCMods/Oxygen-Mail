package austeretony.oxygen_mail.common.config;

import java.util.List;

import austeretony.oxygen_core.common.EnumValueType;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfigHolder;
import austeretony.oxygen_core.common.api.config.ConfigValueImpl;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailConfig extends AbstractConfigHolder {

    public static final ConfigValue
    MAIL_SAVE_DELAY_MINUTES = new ConfigValueImpl(EnumValueType.INT, "setup", "mail_save_delay_minutes"),

    MAILBOX_SIZE = new ConfigValueImpl(EnumValueType.INT, "main", "mailbox_size"),

    SYSTEM_LETTER_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "system_letter_expire_time_hours"),
    LETTER_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "letter_expire_time_hours"),
    SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "system_remittance_expire_time_hours"),
    REMITTANCE_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "remittance_expire_time_hours"),
    SYSTEM_PACKAGE_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "system_package_expire_time_hours"),
    PACKAGE_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "package_expire_time_hours"),
    PACKAGE_WITH_COD_EXPIRE_TIME_HOURS = new ConfigValueImpl(EnumValueType.INT, "main", "package_with_cod_expire_time_hours"),

    MAIL_SENDING_DELAY_SECONDS = new ConfigValueImpl(EnumValueType.INT, "main", "mail_sending_delay_seconds"),
    REMITTANCE_MAX_VALUE = new ConfigValueImpl(EnumValueType.LONG, "main", "remittance_max_value"),
    PACKAGE_MAX_AMOUNT = new ConfigValueImpl(EnumValueType.INT, "main", "package_max_amount"),
    PACKAGE_WITH_COD_MAX_VALUE = new ConfigValueImpl(EnumValueType.LONG, "main", "package_with_cod_max_value"),

    LETTER_POSTAGE_VALUE = new ConfigValueImpl(EnumValueType.LONG, "main", "letter_postage_value"),
    REMITTANCE_POSTAGE_PERCENT = new ConfigValueImpl(EnumValueType.INT, "main", "remittance_postage_percent"),
    PACKAGE_POSTAGE_VALUE = new ConfigValueImpl(EnumValueType.LONG, "main", "package_postage_value"),
    PACKAGE_WITH_COD_POSTAGE_PERCENT = new ConfigValueImpl(EnumValueType.INT, "main", "package_with_cod_postage_percent");

    @Override
    public String getDomain() {
        return MailMain.MODID;
    }

    @Override
    public String getVersion() {
        return MailMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/mail.json";
    }

    @Override
    public String getInternalPath() {
        return "assets/oxygen_mail/mail.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(MAIL_SAVE_DELAY_MINUTES);

        values.add(MAILBOX_SIZE);

        values.add(SYSTEM_LETTER_EXPIRE_TIME_HOURS);
        values.add(LETTER_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_PACKAGE_EXPIRE_TIME_HOURS);
        values.add(PACKAGE_EXPIRE_TIME_HOURS);
        values.add(PACKAGE_WITH_COD_EXPIRE_TIME_HOURS);

        values.add(MAIL_SENDING_DELAY_SECONDS);
        values.add(REMITTANCE_MAX_VALUE);
        values.add(PACKAGE_MAX_AMOUNT);
        values.add(PACKAGE_WITH_COD_MAX_VALUE);

        values.add(REMITTANCE_POSTAGE_PERCENT);
        values.add(PACKAGE_POSTAGE_VALUE);
        values.add(PACKAGE_WITH_COD_POSTAGE_PERCENT);
    }

    @Override
    public boolean sync() {
        return true;
    }
}