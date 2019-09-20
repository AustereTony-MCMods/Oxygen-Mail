package austeretony.oxygen_mail.client.gui.mail;

import java.util.UUID;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxField;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.InventoryLoadGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenCheckBoxGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenGUITextField;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.elements.UsernameGUITextField;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryItemGUIButton;
import austeretony.oxygen_mail.client.gui.mail.sending.SendingBackgroundGUIFiller;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SelectItemGUICallback;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SendMessageGUICallback;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import net.minecraft.item.ItemStack;

public class SendingGUISection extends AbstractGUISection {

    private final MailMenuGUIScreen screen;

    private OxygenGUIButton selectItemButton, sendButton;

    private UsernameGUITextField addresseeTextField;

    private OxygenGUITextField subjectTextField, currencyTextField, packageAmountTextField;

    private GUITextBoxField messageTextBoxField;

    private OxygenCheckBoxGUIButton enableRemittanceButton, enableCODButton, enablePackageButton;

    private InventoryLoadGUIElement inventoryLoadElement;

    private CurrencyValueGUIElement balanceElement;

    private AbstractGUICallback selectItemCallback, sendMessageCallback;

    //cache

    private InventoryItemGUIButton currentItemButton;

    private final String 
    playerFoundStr = ClientReference.localize("oxygen_mail.gui.mail.playerOnline"),
    playerNotFoundStr = ClientReference.localize("oxygen_mail.gui.mail.playerOffline");

    public SendingGUISection(MailMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new SendingBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_mail.gui.mail.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(new OxygenGUIText(6, 40, ClientReference.localize("oxygen_mail.gui.mail.subject"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.subjectTextField = new OxygenGUITextField(6, 47, 120, 9, Mail.MESSAGE_SUBJECT_MAX_LENGTH, "", 3, false, - 1));
        this.addElement(this.messageTextBoxField = new GUITextBoxField(6, 58, 120, 84, Mail.MESSAGE_MAX_LENGTH).setLineOffset(2).setTextScale(GUISettings.get().getSubTextScale()).enableDynamicBackground().cancelDraggedElementLogic(true));

        this.addElement(new OxygenGUIText(6, 18, ClientReference.localize("oxygen_mail.gui.mail.sendTo"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.addresseeTextField = new UsernameGUITextField(6, 25, 120));
        this.addresseeTextField.disable();

        this.addElement(new OxygenGUIText(130, 18, ClientReference.localize("oxygen_mail.gui.mail.attachment"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.enableRemittanceButton = new OxygenCheckBoxGUIButton(130, 27));    
        this.addElement(new OxygenGUIText(139, 28, ClientReference.localize("oxygen_mail.gui.mail.remittance"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));

        this.addElement(this.enableCODButton = new OxygenCheckBoxGUIButton(130, 37));
        this.addElement(new OxygenGUIText(139, 38, ClientReference.localize("oxygen_mail.gui.mail.cod"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));

        this.addElement(this.currencyTextField = new OxygenGUITextField(130, 46, 45, 9, 10, "0", 3, true, - 1));
        this.currencyTextField.disable();

        this.addElement(this.enablePackageButton = new OxygenCheckBoxGUIButton(130, 64));        
        this.addElement(new OxygenGUIText(139, 65, ClientReference.localize("oxygen_mail.gui.mail.package"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()));
        this.addElement(this.packageAmountTextField = new OxygenGUITextField(130, 73, 45, 9, 4, "0", 3, true, - 1));
        this.addElement(this.selectItemButton = new OxygenGUIButton(130, 85, 40, 10, ClientReference.localize("oxygen_mail.gui.mail.selectItemButton")).disable());     
        this.packageAmountTextField.disable();

        this.addElement(this.sendButton = new OxygenGUIButton(6, this.getHeight() - 26, 40, 10, ClientReference.localize("oxygen_mail.gui.mail.sendButton")).disable());     

        this.addElement(this.inventoryLoadElement = new InventoryLoadGUIElement(4, this.getHeight() - 9, EnumGUIAlignment.RIGHT));
        this.inventoryLoadElement.setLoad(this.screen.getIncomingSection().getInventoryLoadElement().getLoad());
        this.addElement(this.balanceElement = new CurrencyValueGUIElement(this.getWidth() - 10, this.getHeight() - 10));   
        this.balanceElement.setValue(this.screen.getIncomingSection().getBalanceElement().getValue());

        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getIncomingSection()));

        this.selectItemCallback = new SelectItemGUICallback(this.screen, this, 140, 96).enableDefaultBackground();
        this.sendMessageCallback = new SendMessageGUICallback(this.screen, this, 140, 76).enableDefaultBackground();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (!this.addresseeTextField.getTypedText().isEmpty() 
                    && !this.subjectTextField.getTypedText().isEmpty())
                this.sendButton.enable();
            else
                this.sendButton.disable();
            if (element == this.selectItemButton)
                this.selectItemCallback.open();
            else if (element == this.sendButton)
                this.sendMessageCallback.open();
            else if (element == this.enableRemittanceButton) {
                if (this.enableRemittanceButton.isToggled()) {
                    this.currencyTextField.enableNumberFieldMode(MathUtils.lesserOfTwo(
                            this.balanceElement.getValue(), 
                            PrivilegeProviderClient.getValue(EnumMailPrivilege.REMITTANCE_MAX_VALUE.toString(), MailConfig.REMITTANCE_MAX_VALUE.getLongValue())));
                    this.enableCODButton.setToggled(false);
                    this.currencyTextField.enable();
                } else
                    this.currencyTextField.disable();
                this.currencyTextField.setText("0");
                this.packageAmountTextField.setText("0");
                if (this.enablePackageButton.isToggled()) {
                    this.enablePackageButton.setToggled(false);
                    this.packageAmountTextField.disable();
                    if (this.currentItemButton != null)
                        this.currentItemButton.setToggled(false);
                    this.currentItemButton = null;
                }
            } else if (element == this.enableCODButton) {
                if (this.enableCODButton.isToggled()) {
                    this.selectItemButton.enable();
                    this.currencyTextField.enableNumberFieldMode(PrivilegeProviderClient.getValue(EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.toString(), MailConfig.PACKAGE_WITH_COD_MAX_VALUE.getLongValue()));
                    this.enableRemittanceButton.setToggled(false);
                    this.currencyTextField.enable();
                } else {
                    this.selectItemButton.disable();
                    this.currencyTextField.disable();
                }
                this.currencyTextField.setText("0");
            } else if (element == this.enablePackageButton) {
                if (this.enablePackageButton.isToggled()) {
                    this.selectItemButton.enable();
                    if (this.currentItemButton != null)
                        this.packageAmountTextField.enable();
                } else {
                    this.selectItemButton.disable();
                    if (this.currentItemButton != null)
                        this.currentItemButton.setToggled(false);
                    this.currentItemButton = null;
                    this.packageAmountTextField.disable();
                }
                this.packageAmountTextField.setText("0");
            }
        }
    }

    public void itemSelected(InventoryItemGUIButton clicked) {
        if (this.currentItemButton != clicked) {
            if (this.currentItemButton != null)
                this.currentItemButton.setToggled(false);
            clicked.toggle();                    
            this.currentItemButton = clicked;
            int maxAmount = PrivilegeProviderClient.getValue(EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), MailConfig.PACKAGE_MAX_AMOUNT.getIntValue());
            ItemStack itemStack = this.currentItemButton.index;
            if (maxAmount < 0) 
                maxAmount = itemStack.getMaxStackSize();
            this.packageAmountTextField.enableNumberFieldMode(MathUtils.lesserOfTwo(this.screen.getEqualStackAmount(clicked.stackWrapper), maxAmount));
            this.packageAmountTextField.setText("1");
            this.packageAmountTextField.enable();
            if (this.enableRemittanceButton.isToggled()) {
                this.enableRemittanceButton.setToggled(false);
                this.currencyTextField.disable();
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
        if (!this.currencyTextField.getTypedText().isEmpty())
            currency = this.currencyTextField.getTypedNumber();
        Parcel parcel = null;
        if (this.currentItemButton != null)
            parcel = Parcel.create(this.currentItemButton.index, (int) this.packageAmountTextField.getTypedNumber());
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
        this.addresseeTextField.load();
        this.addresseeTextField.enable();
    }

    public void mailSynchronized() {}

    public void messageSent(Parcel parcel, long balance) {
        this.balanceElement.setValue(this.screen.getIncomingSection().getBalanceElement().getValue());
        if (parcel != null) {
            this.inventoryLoadElement.setLoad(this.screen.getIncomingSection().getInventoryLoadElement().getLoad());
            ((SelectItemGUICallback) this.selectItemCallback).loadInventoryContent();
        }

        this.addresseeTextField.reset();
        this.subjectTextField.reset();
        this.messageTextBoxField.reset();

        this.enableRemittanceButton.setToggled(false);
        this.enableCODButton.setToggled(false);
        this.enablePackageButton.setToggled(false);

        this.currencyTextField.disable();
        this.currencyTextField.setText("0");
        this.packageAmountTextField.disable();
        this.packageAmountTextField.setText("0");

        if (this.currentItemButton != null)
            this.currentItemButton.setToggled(false);
        this.currentItemButton = null;

        this.selectItemButton.disable();
        this.sendButton.disable();
    }

    public void messageRemoved(long messageId) {}

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.balanceElement.setValue(this.screen.getIncomingSection().getBalanceElement().getValue());
        if (parcel != null) {
            this.inventoryLoadElement.setLoad(this.screen.getIncomingSection().getInventoryLoadElement().getLoad());
            ((SelectItemGUICallback) this.selectItemCallback).loadInventoryContent();
        }
    }

    public String getAddresseeUsername() {
        return this.addresseeTextField.getTypedText();
    }

    public InventoryLoadGUIElement getInventoryLoadElement() {
        return this.inventoryLoadElement;
    }

    public CurrencyValueGUIElement getBalanceElement() {
        return this.balanceElement;
    }
}
