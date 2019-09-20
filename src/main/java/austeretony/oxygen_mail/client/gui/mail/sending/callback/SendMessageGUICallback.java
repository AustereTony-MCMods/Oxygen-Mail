package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.client.gui.mail.SendingGUISection;
import austeretony.oxygen_mail.client.gui.mail.incoming.AttachmentGUIElement;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;

public class SendMessageGUICallback extends AbstractGUICallback {

    private final MailMenuGUIScreen screen;

    private final SendingGUISection section;

    private OxygenGUIButton confirmButton, cancelButton;

    private OxygenGUIText messageTypeTextLabel, attachmentNoticeTextLabel, addresseeTextLabel, postageTextLabel;

    private AttachmentGUIElement attachmentElement;

    private CurrencyValueGUIElement postageElement;

    //cache

    private EnumMail type;

    private String subject, message;

    private long currency;

    private Parcel parcel;

    public SendMessageGUICallback(MailMenuGUIScreen screen, SendingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_mail.gui.mail.callback.sendMessage"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.messageTypeTextLabel = new OxygenGUIText(6, 17, "", GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));
        this.addElement(this.addresseeTextLabel = new OxygenGUIText(6, 26, "", GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.attachmentNoticeTextLabel = new OxygenGUIText(6, 35, ClientReference.localize("oxygen_mail.gui.mail.noAttachment"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()).disableFull());
        this.addElement(this.attachmentElement = new AttachmentGUIElement(6, 34).disableFull()); 

        this.addElement(this.postageTextLabel = new OxygenGUIText(6, this.getHeight() - 22, ClientReference.localize("oxygen_mail.gui.mail.postage"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.postageElement = new CurrencyValueGUIElement(6, this.getHeight() - 23)); 

        this.addElement(this.confirmButton = new OxygenGUIButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.confirmButton")));
        this.addElement(this.cancelButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.cancelButton")));
    }

    @Override
    public void onOpen() {
        this.attachmentNoticeTextLabel.disableFull();
        this.attachmentElement.disableFull();
        this.confirmButton.enable();
        Mail message = this.section.createMessage();
        this.messageTypeTextLabel.setDisplayText(message.getType().localizedName());
        this.addresseeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.addressee", this.section.getAddresseeUsername()));
        if (message.getType() == EnumMail.LETTER)
            this.attachmentNoticeTextLabel.enableFull();
        else {
            this.attachmentElement.load(message);
            this.attachmentElement.enableFull();
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
            value = PrivilegeProviderClient.getValue(EnumMailPrivilege.LETTER_POSTAGE_VALUE.toString(), MailConfig.LETTER_POSTAGE_VALUE.getLongValue());
            postage = value;
            break;
        case REMITTANCE:
            if (message.getCurrency() <= 0)
                this.confirmButton.disable();
            percent = PrivilegeProviderClient.getValue(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.toString(), MailConfig.REMITTANCE_POSTAGE_PERCENT.getIntValue());
            remittance = message.getCurrency();
            postage = MathUtils.percentValueOf(message.getCurrency(), (int) percent);
            break;
        case PACKAGE:
            if (message.getParcel() == null)
                this.confirmButton.disable();
            if (message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegeProviderClient.getValue(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getLongValue());
            postage = value;
            break;
        case PACKAGE_WITH_COD:
            if (message.getCurrency() <= 0 || message.getParcel() == null)
                this.confirmButton.disable();
            if (message.getParcel().amount <= 0)
                this.confirmButton.disable();
            value = PrivilegeProviderClient.getValue(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), MailConfig.PACKAGE_POSTAGE_VALUE.getLongValue());
            percent = PrivilegeProviderClient.getValue(EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), MailConfig.PACKAGE_WITH_COD_POSTAGE_PERCENT.getIntValue());
            codComission = MathUtils.percentValueOf(message.getCurrency(), (int) percent);
            postage = value;
            break;
        default:
            break;
        }
        this.postageElement.setValue(postage + codComission);
        if (remittance + postage > this.section.getBalanceElement().getValue()) {
            this.postageElement.setRed(true);
            this.confirmButton.disable();
        }
        this.postageElement.setX(this.postageTextLabel.getX() + 4
                + this.textWidth(this.postageTextLabel.getDisplayText(), GUISettings.get().getSubTextScale()) 
                + this.textWidth(this.postageElement.getDisplayText(), GUISettings.get().getSubTextScale()));
        this.type = message.getType();
        this.subject = message.getSubject();
        this.message = message.getMessage();
        this.currency = message.getCurrency();
        this.parcel = message.getParcel();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                MailManagerClient.instance().getMailboxManager().sendMessageSynced(this.type, this.section.getAddresseeUsername(), this.subject, this.message, this.currency, this.parcel);
                this.close();
            }
        }
    }
}
