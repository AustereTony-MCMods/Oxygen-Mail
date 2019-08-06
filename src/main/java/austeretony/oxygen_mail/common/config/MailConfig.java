package austeretony.oxygen_mail.common.config;

import java.util.List;

import austeretony.oxygen.common.api.config.AbstractConfigHolder;
import austeretony.oxygen.common.api.config.ConfigValue;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailConfig extends AbstractConfigHolder {

    public static final ConfigValue
    MAILBOX_SIZE = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "mailbox_size"),

    SERVICE_LETTER_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "service_letter_expire_time_hours"),
    LETTER_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "letter_expire_time_hours"),
    SERVICE_REMITTANCE_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "service_remittance_expire_time_hours"),
    REMITTANCE_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "remittance_expire_time_hours"),
    SERVICE_PACKAGE_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "service_package_expire_time_hours"),
    PACKAGE_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_expire_time_hours"),
    PACKAGE_WITH_COD_EXPIRE_TIME = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_with_cod_expire_time_hours"),

    MAIL_SENDING_DELAY = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "mail_sending_delay_seconds"),
    REMITTANCE_MAX_VALUE = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "remittance_max_value"),
    PACKAGE_MAX_AMOUNT = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_max_amount"),
    PACKAGE_WITH_COD_MAX_VALUE = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_with_cod_max_value"),
            
    LETTER_POSTAGE_VALUE = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "letter_postage_value"),
    REMITTANCE_POSTAGE_PERCENT = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "remittance_postage_percent"),
    PACKAGE_POSTAGE_VALUE = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_postage_value"),
    PACKAGE_WITH_COD_POSTAGE_PERCENT = new ConfigValue(ConfigValue.EnumValueType.INT, "main", "package_with_cod_postage_percent");

    @Override
    public String getModId() {
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
        values.add(MAILBOX_SIZE);

        values.add(SERVICE_LETTER_EXPIRE_TIME);
        values.add(LETTER_EXPIRE_TIME);
        values.add(SERVICE_REMITTANCE_EXPIRE_TIME);
        values.add(REMITTANCE_EXPIRE_TIME);
        values.add(SERVICE_PACKAGE_EXPIRE_TIME);
        values.add(PACKAGE_EXPIRE_TIME);
        values.add(PACKAGE_WITH_COD_EXPIRE_TIME);

        values.add(MAIL_SENDING_DELAY);
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
