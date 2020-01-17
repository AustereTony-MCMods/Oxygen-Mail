package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.MailMenuScreen;
import austeretony.oxygen_mail.client.gui.mail.MessageAttachment;
import austeretony.oxygen_mail.client.gui.mail.SendingSection;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;

public class SendMessageCallback extends AbstractGUICallback {

    private final MailMenuScreen screen;

    private final SendingSection section;

    private OxygenButton confirmButton, cancelButton;

    private OxygenTextLabel messageTypeTextLabel, attachmentNoticeTextLabel, addresseeTextLabel, postageTextLabel;

    private MessageAttachment attachment;

    private OxygenCurrencyValue postageValue;

    //cache

    private EnumMail type;

    private String subject, message;

    private long currency;

    private Parcel parcel;

    public SendMessageCallback(MailMenuScreen screen, SendingSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.callback.sendMessage"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.messageTypeTextLabel = new OxygenTextLabel(6, 23, "", EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));
        this.addElement(this.addresseeTextLabel = new OxygenTextLabel(6, 32, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.attachmentNoticeTextLabel = new OxygenTextLabel(6, 41, ClientReference.localize("oxygen_mail.gui.mail.noAttachment"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.attachment = new MessageAttachment(6, 34).disableFull()); 

        this.addElement(this.postageTextLabel = new OxygenTextLabel(6, this.getHeight() - 26, ClientReference.localize("oxygen_mail.gui.mail.postage"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.postageValue = new OxygenCurrencyValue(6, this.getHeight() - 24)); 
        this.postageValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, 0L);

        this.addElement(this.confirmButton = new OxygenButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_core.gui.confirm")));
        this.confirmButton.setKeyPressListener(Keyboard.KEY_R, ()->this.confirm());

        this.addElement(this.cancelButton = new OxygenButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen_core.gui.cancel")));
        this.cancelButton.setKeyPressListener(Keyboard.KEY_X, ()->this.close());
    }

    @Override
    public void onOpen() {
        this.attachmentNoticeTextLabel.disableFull();
        this.attachment.disableFull();
        this.confirmButton.enable();
        Mail message = this.section.createMessage();
        this.messageTypeTextLabel.setDisplayText(message.getType().localizedName());
        this.addresseeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.addressee", this.section.getAddresseeUsername()));
        if (message.getType() == EnumMail.LETTER)
            this.attachmentNoticeTextLabel.enableFull();
        else {
            this.attachment.load(message);
            this.attachment.enableFull();
        }
        long 
        postage = 0L,
        codComission = 0L,
        remittance = 0L,
        value,
        percent;
        switch (message.getType()) {
        case LETTER:
            if (message.getMessage().isEmpty())
                this.confirmButton.disable();
            value = PrivilegesProviderClient.getAsLong(EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
            postage = value;
            break;
        case REMITTANCE:
            if (message.getCurrency() <= 0)
                this.confirmButton.disable();
            percent = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.id(), MailConfig.REMITTANCE_POSTAGE_PERCENT.asInt());
            remittance = message.getCurrency();
            postage = MathUtils.percentValueOf(message.getCurrency(), (int) percent);
            break;
        case PACKAGE:
            if (message.getParcel() == null)
                this.confirmButton.disable();
            if (message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegesProviderClient.getAsLong(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
            postage = value;
            break;
        case PACKAGE_WITH_COD:
            if (message.getCurrency() <= 0 || message.getParcel() == null)
                this.confirmButton.disable();
            if (message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegesProviderClient.getAsLong(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.id(), MailConfig.PACKAGE_POSTAGE_VALUE.asLong());
            percent = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.id(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.asInt());
            codComission = MathUtils.percentValueOf(message.getCurrency(), (int) percent);
            postage = value;
            break;
        default:
            break;
        }
        this.postageValue.updateValue(postage + codComission);
        if (remittance + postage > this.section.getBalanceValue().getValue()) {
            this.postageValue.setRed(true);
            this.confirmButton.disable();
        }
        this.postageValue.setX(this.getX() + 8 + this.textWidth(this.postageValue.getDisplayText(), this.postageValue.getTextScale()));
        this.type = message.getType();
        this.subject = message.getSubject();
        this.message = message.getMessage();
        this.currency = message.getCurrency();
        this.parcel = message.getParcel();
    }

    private void confirm() {
        MailManagerClient.instance().getMailboxManager().sendMessageSynced(this.type, this.section.getAddresseeUsername(), this.subject, this.message, this.currency, this.parcel);
        this.close();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton)
                this.confirm();
        }
    }
}
