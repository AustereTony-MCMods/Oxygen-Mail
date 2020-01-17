package austeretony.oxygen_mail.client.gui.mail;

import java.util.UUID;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxField;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxButton;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenNumberField;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenTextBoxField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextField;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.client.gui.elements.OxygenUsernameField;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.IncomingMailBackgroundFiller;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryItemPanelEntry;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SelectItemCallback;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SendMessageCallback;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import net.minecraft.item.ItemStack;

public class SendingSection extends AbstractGUISection {

    private final MailMenuScreen screen;

    private OxygenButton selectItemButton, sendButton;

    private OxygenUsernameField addresseeField;

    private OxygenTextField subjectTextField;

    private OxygenNumberField currencyValueField, packageAmountField;

    private GUITextBoxField messageTextBoxField;

    private OxygenCheckBoxButton enableRemittanceButton, enableCODButton, enablePackageButton;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue;

    private AbstractGUICallback selectItemCallback, sendMessageCallback;

    //cache

    private InventoryItemPanelEntry currentItemButton;

    private final String 
    playerFoundStr = ClientReference.localize("oxygen_mail.gui.mail.playerOnline"),
    playerNotFoundStr = ClientReference.localize("oxygen_mail.gui.mail.playerOffline");

    public SendingSection(MailMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.sending"));
    }

    @Override
    public void init() {
        this.addElement(new IncomingMailBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(new OxygenTextLabel(6, 24, ClientReference.localize("oxygen_mail.gui.mail.sendTo"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.addresseeField = new OxygenUsernameField(6, 25, 120));
        this.addresseeField.setInputListener((keyChar, keyCode)->this.sendButton.setEnabled(
                this.screen.allowMailSending
                && !this.addresseeField.getTypedText().isEmpty() 
                && !this.subjectTextField.getTypedText().isEmpty()));
        this.addresseeField.disable();

        this.addElement(new OxygenTextLabel(6, 46, ClientReference.localize("oxygen_mail.gui.mail.subject"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.subjectTextField = new OxygenTextField(6, 47, 120, Mail.MESSAGE_SUBJECT_MAX_LENGTH, ""));
        this.subjectTextField.setInputListener((keyChar, keyCode)->this.sendButton.setEnabled(
                this.screen.allowMailSending
                && !this.addresseeField.getTypedText().isEmpty() 
                && !this.subjectTextField.getTypedText().isEmpty()));

        this.addElement(this.messageTextBoxField = new OxygenTextBoxField(6, 58, 120, 84, Mail.MESSAGE_MAX_LENGTH));

        this.addElement(new OxygenTextLabel(130, 24, ClientReference.localize("oxygen_mail.gui.mail.attachment"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.enableRemittanceButton = new OxygenCheckBoxButton(130, 27));    
        this.addElement(new OxygenTextLabel(139, 33, ClientReference.localize("oxygen_mail.gui.mail.remittance"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        this.addElement(this.enableCODButton = new OxygenCheckBoxButton(130, 37));
        this.addElement(new OxygenTextLabel(139, 43, ClientReference.localize("oxygen_mail.gui.mail.cod"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));

        this.addElement(this.currencyValueField = new OxygenNumberField(130, 46, 45, "", 0L, false, 0, true));
        this.currencyValueField.disable();

        this.addElement(this.enablePackageButton = new OxygenCheckBoxButton(130, 64));        
        this.addElement(new OxygenTextLabel(139, 70, ClientReference.localize("oxygen_mail.gui.mail.package"), EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()));
        this.addElement(this.packageAmountField = new OxygenNumberField(130, 73, 45, "", 0L, false, 0, true));
        this.packageAmountField.disable();
        this.addElement(this.selectItemButton = new OxygenButton(130, 85, 40, 10, ClientReference.localize("oxygen_mail.gui.mail.selectItemButton")).disable());   
        this.selectItemButton.setKeyPressListener(Keyboard.KEY_E, ()->this.openSelectItemCallback());

        this.addElement(this.sendButton = new OxygenButton(6, this.getHeight() - 26, 40, 10, ClientReference.localize("oxygen_mail.gui.mail.sendButton")).disable());     
        this.sendButton.setKeyPressListener(Keyboard.KEY_R, ()->this.openSendMessageCallback());

        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, this.screen.getIncomingSection().getBalanceValue().getValue());

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getIncomingSection()));

        this.selectItemCallback = new SelectItemCallback(this.screen, this, 140, 96).enableDefaultBackground();
        this.sendMessageCallback = new SendMessageCallback(this.screen, this, 140, 85).enableDefaultBackground();
    }

    private void openSelectItemCallback() {
        if (!this.addresseeField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())          
            this.selectItemCallback.open();   
    }

    private void openSendMessageCallback() {
        if (!this.addresseeField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())          
            this.sendMessageCallback.open();  
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.selectItemButton)
                this.openSelectItemCallback();
            else if (element == this.sendButton)
                this.openSendMessageCallback();
            else if (element == this.enableRemittanceButton) {
                if (this.enableRemittanceButton.isToggled()) {
                    this.currencyValueField.setMaxNumber(MathUtils.lesserOfTwo(
                            this.balanceValue.getValue(), 
                            PrivilegesProviderClient.getAsLong(EnumMailPrivilege.REMITTANCE_MAX_VALUE.id(), MailConfig.REMITTANCE_MAX_VALUE.asLong())));
                    this.enableCODButton.setToggled(false);
                    this.currencyValueField.enable();
                } else
                    this.currencyValueField.disable();
                this.currencyValueField.setText("0");
                this.packageAmountField.setText("0");
                if (this.enablePackageButton.isToggled()) {
                    this.enablePackageButton.setToggled(false);
                    this.packageAmountField.disable();
                    if (this.currentItemButton != null)
                        this.currentItemButton.setToggled(false);
                    this.currentItemButton = null;
                }
            } else if (element == this.enableCODButton) {
                if (this.enableCODButton.isToggled()) {
                    this.selectItemButton.enable();
                    this.currencyValueField.setMaxNumber(PrivilegesProviderClient.getAsLong(EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.id(), MailConfig.PACKAGE_WITH_COD_MAX_VALUE.asLong()));
                    this.enableRemittanceButton.setToggled(false);
                    this.currencyValueField.enable();
                } else {
                    this.selectItemButton.disable();
                    this.currencyValueField.disable();
                }
                this.currencyValueField.setText("0");
            } else if (element == this.enablePackageButton) {
                if (this.enablePackageButton.isToggled()) {
                    this.selectItemButton.enable();
                    if (this.currentItemButton != null)
                        this.packageAmountField.enable();
                } else {
                    this.selectItemButton.disable();
                    if (this.currentItemButton != null)
                        this.currentItemButton.setToggled(false);
                    this.currentItemButton = null;
                    this.packageAmountField.disable();
                }
                this.packageAmountField.setText("0");
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback()
                && !this.addresseeField.isDragged()
                && !this.subjectTextField.isDragged()
                && !this.messageTextBoxField.isDragged())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuScreen.MAIL_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MailConfig.ENABLE_MAIL_MENU_KEY.asBoolean() 
                    && keyCode == MailManagerClient.instance().getKeyHandler().getMailMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    public void itemSelected(InventoryItemPanelEntry clicked) {
        if (this.currentItemButton != clicked) {
            if (this.currentItemButton != null)
                this.currentItemButton.setToggled(false);
            clicked.toggle();                    
            this.currentItemButton = clicked;
            int maxAmount = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.PACKAGE_MAX_AMOUNT.id(), MailConfig.PACKAGE_MAX_AMOUNT.asInt());
            ItemStack itemStack = this.currentItemButton.index;
            if (maxAmount < 0) 
                maxAmount = itemStack.getMaxStackSize();
            this.packageAmountField.setMaxNumber(MathUtils.lesserOfTwo(this.screen.getEqualStackAmount(clicked.stackWrapper), maxAmount));
            this.packageAmountField.setText("1");
            this.packageAmountField.enable();
            if (this.enableRemittanceButton.isToggled()) {
                this.enableRemittanceButton.setToggled(false);
                this.currencyValueField.disable();
            } 
        }
    }

    public Mail createMessage() {
        EnumMail type = EnumMail.LETTER;
        if (this.enableRemittanceButton.isToggled())
            type = EnumMail.REMITTANCE;
        else if (this.enablePackageButton.isToggled()) {
            if (this.enableCODButton.isToggled())
                type = EnumMail.PACKAGE_WITH_COD;
            else
                type = EnumMail.PACKAGE;
        }   
        long currency = 0L;
        if (!this.currencyValueField.getTypedText().isEmpty())
            currency = this.currencyValueField.getTypedNumberAsLong();
        Parcel parcel = null;
        if (this.currentItemButton != null)
            parcel = Parcel.create(this.currentItemButton.index, (int) this.packageAmountField.getTypedNumberAsLong());
        Mail message = new Mail(
                0L, 
                type, 
                UUID.randomUUID(),
                ClientReference.getClientPlayer().getName(), 
                this.subjectTextField.getTypedText(), 
                this.messageTextBoxField.getTypedText(),
                currency,
                parcel);
        return message;
    }

    public void sharedDataSynchronized() {
        this.addresseeField.load();
        this.addresseeField.enable();
    }

    public void mailSynchronized() {}

    public void messageSent(Parcel parcel, long balance) {
        this.balanceValue.updateValue(this.screen.getIncomingSection().getBalanceValue().getValue());
        if (parcel != null) {
            this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
            ((SelectItemCallback) this.selectItemCallback).loadInventoryContent();
        }

        this.addresseeField.reset();
        this.subjectTextField.reset();
        this.messageTextBoxField.reset();

        this.enableRemittanceButton.setToggled(false);
        this.enableCODButton.setToggled(false);
        this.enablePackageButton.setToggled(false);

        this.currencyValueField.disable();
        this.currencyValueField.setText("0");
        this.packageAmountField.disable();
        this.packageAmountField.setText("0");

        if (this.currentItemButton != null)
            this.currentItemButton.setToggled(false);
        this.currentItemButton = null;

        this.selectItemButton.disable();
        this.sendButton.disable();
    }

    public void messageRemoved(long messageId) {}

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.balanceValue.updateValue(this.screen.getIncomingSection().getBalanceValue().getValue());
        if (parcel != null) {
            this.inventoryLoad.setLoad(this.screen.getIncomingSection().getInventoryLoad().getLoad());
            ((SelectItemCallback) this.selectItemCallback).loadInventoryContent();
        }
    }

    public String getAddresseeUsername() {
        return this.addresseeField.getTypedText();
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }
}
