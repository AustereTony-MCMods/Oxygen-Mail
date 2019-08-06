package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.BalanceGUIElement;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.AttachmentGUIElement;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.client.gui.mail.SendingGUISection;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.Message;

public class SendMessageGUICallback extends AbstractGUICallback {

    private final MailMenuGUIScreen screen;

    private final SendingGUISection section;

    private GUIButton confirmButton, cancelButton;

    private GUITextLabel messageTypeTextLabel, attachmentNoticeTextLabel, addresseeTextLabel, postageTextLabel;

    private AttachmentGUIElement attachmentElement;

    private BalanceGUIElement postageElement;

    private Message message;

    private int postage;

    public SendMessageGUICallback(MailMenuGUIScreen screen, SendingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new SendMessageCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.callback.sendMessage"), true, GUISettings.instance().getTitleScale()));

        this.addElement(this.messageTypeTextLabel = new GUITextLabel(2, 15).setTextScale(GUISettings.instance().getTextScale()));
        this.addElement(this.addresseeTextLabel = new GUITextLabel(2, 23).setTextScale(GUISettings.instance().getSubTextScale()));
        this.addElement(this.attachmentNoticeTextLabel = new GUITextLabel(2, 33).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.noAttachment"), false, GUISettings.instance().getSubTextScale()).disableFull());
        this.addElement(this.attachmentElement = new AttachmentGUIElement(2, 33).setEnabledTextColor(GUISettings.instance().getEnabledTextColor()).disableFull()); 

        this.addElement(this.postageTextLabel = new GUITextLabel(2, this.getHeight() - 24).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.postage"), false, GUISettings.instance().getSubTextScale()).setEnabledTextColor(GUISettings.instance().getEnabledTextColor()));
        this.addElement(this.postageElement = new BalanceGUIElement(0, this.getHeight() - 23).setEnabledTextColor(GUISettings.instance().getEnabledTextColor())); 

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    public void onOpen() {
        this.attachmentNoticeTextLabel.disableFull();
        this.attachmentElement.disableFull();
        this.confirmButton.enable();
        this.message = this.section.createMessage();
        this.messageTypeTextLabel.setDisplayText(this.message.type.localizedName());
        this.addresseeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.addressee", this.section.getAddresseeUsername()));
        if (this.message.type == EnumMail.LETTER)
            this.attachmentNoticeTextLabel.enableFull();
        else {
            this.attachmentElement.setAttachment(this.message);
            this.attachmentElement.enableFull();
        }
        int 
        value,
        percent;
        switch (this.message.type) {
        case LETTER:
            if (this.message.message.isEmpty())
                this.confirmButton.disable();
            value = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.LETTER_POSTAGE_VALUE.toString(), MailConfig.LETTER_POSTAGE_VALUE.getIntValue());
            this.postageElement.initSimpleTooltip(ClientReference.localize("oxygen_mail.tooltip.postage.value", value), GUISettings.instance().getTooltipScale());
            this.postage = value;
            break;
        case REMITTANCE:
            if (this.message.getCurrency() <= 0)
                this.confirmButton.disable();
            percent = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.toString(), MailConfig.REMITTANCE_POSTAGE_PERCENT.getIntValue());
            this.postageElement.initSimpleTooltip(ClientReference.localize("oxygen_mail.tooltip.postage.percent", percent), GUISettings.instance().getTooltipScale());
            this.postage = MathUtils.percentValueOf(this.message.getCurrency(), percent);
            break;
        case PACKAGE:
            if (this.message.getParcel() == null)
                this.confirmButton.disable();
            if (this.message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getIntValue());
            this.postageElement.initSimpleTooltip(ClientReference.localize("oxygen_mail.tooltip.postage.value", value), GUISettings.instance().getTooltipScale());
            this.postage = value;
            break;
        case PACKAGE_WITH_COD:
            if (this.message.getCurrency() <= 0 || this.message.getParcel() == null)
                this.confirmButton.disable();
            if (this.message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getIntValue());
            percent = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.getIntValue());
            this.postageElement.initSimpleTooltip(ClientReference.localize("oxygen_mail.tooltip.postage.cod", value, percent), GUISettings.instance().getTooltipScale());
            this.postage = value + MathUtils.percentValueOf(this.message.getCurrency(), percent);
            break;
        default:
            break;
        }
        this.postageElement.setBalance(this.postage);
        if (this.message.getCurrency() + this.postage > this.section.getBalance()) {
            this.postageElement.setRed(true);
            this.confirmButton.disable();
        }
        this.postageElement.setX(this.postageTextLabel.getX() + 4
                + this.textWidth(this.postageTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()) 
                + this.textWidth(this.postageElement.getDisplayText(), GUISettings.instance().getSubTextScale()));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                MailManagerClient.instance().sendMessageSynced(this.section.getAddresseeUsername(), this.message);
                this.section.messageSent(this.postage);
                this.close();
            }
        }
    }
}
