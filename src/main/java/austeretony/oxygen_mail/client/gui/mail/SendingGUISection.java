package austeretony.oxygen_mail.client.gui.mail;

import java.util.HashSet;
import java.util.Set;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUICheckBoxButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextBoxField;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIOrientation;
import austeretony.oxygen.client.api.WatcherHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.BalanceGUIElement;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.common.itemstack.InventoryHelper;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryStackGUIButton;
import austeretony.oxygen_mail.client.gui.mail.sending.callback.SendMessageGUICallback;
import austeretony.oxygen_mail.client.input.MailKeyHandler;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.Message;
import austeretony.oxygen_mail.common.main.Parcel;
import net.minecraft.item.ItemStack;

public class SendingGUISection extends AbstractGUISection {

    private final MailMenuGUIScreen screen;

    private GUIButton incomingSectionButton, sendButton;

    private GUITextField addresseeTextField, subjectTextField, currencyTextField, packageAmountTextField;

    private GUITextLabel addresseeStatusTextLabel;

    private GUITextBoxField messageTextBoxField;

    private GUICheckBoxButton enableRemittanceButton, enableCODButton, enablePackageButton;

    private BalanceGUIElement balanceElement;

    private GUIButtonPanel itemsPanel;

    private InventoryStackGUIButton currentStackButton;

    private AbstractGUICallback sendMessageCallback;

    private Message latestMessage;

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
        this.addElement(new GUITextLabel(2, 4).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.title"), false, GUISettings.instance().getTitleScale()));

        this.addElement(this.incomingSectionButton = new GUIButton(this.getWidth() - 28, 1, 10, 10).setTexture(OxygenGUITextures.ENVELOPE_ICONS, 10, 10).initSimpleTooltip(ClientReference.localize("oxygen_mail.gui.mail.incoming"), GUISettings.instance().getTooltipScale())); 
        this.addElement(new GUIButton(this.getWidth() - 12, 1, 10, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.PENCIL_ICONS, 10, 10).initSimpleTooltip(ClientReference.localize("oxygen_mail.gui.mail.sending"), GUISettings.instance().getTooltipScale()).toggle());

        this.addElement(new GUITextLabel(2, 14).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.sendTo"), false, GUISettings.instance().getSubTextScale()));
        this.addElement(this.addresseeTextField = new GUITextField(2, 22, 120, 9, 20).enableDynamicBackground().setDisplayText("...", false, GUISettings.instance().getSubTextScale()).setLineOffset(3).cancelDraggedElementLogic());
        this.addElement(this.addresseeStatusTextLabel = new GUITextLabel(2, 31).setTextScale(GUISettings.instance().getSubTextScale()).setEnabledTextColor(GUISettings.instance().getEnabledTextColorDark()).disableFull());

        this.addElement(new GUITextLabel(2, 40).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.subject"), false, GUISettings.instance().getSubTextScale()));
        this.addElement(this.subjectTextField = new GUITextField(2, 48, 120, 9, Message.MESSAGE_TITLE_MAX_LENGTH).enableDynamicBackground().setDisplayText("...", false, GUISettings.instance().getSubTextScale()).setLineOffset(3).cancelDraggedElementLogic());

        this.addElement(this.messageTextBoxField = new GUITextBoxField(2, 60, 120, 90, Message.MESSAGE_MAX_LENGTH).setLineOffset(2).setTextScale(GUISettings.instance().getSubTextScale()).enableDynamicBackground().cancelDraggedElementLogic());

        this.addElement(new GUITextLabel(127, 15).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.attachment"), false, GUISettings.instance().getTextScale()));
        this.addElement(this.enableRemittanceButton = new GUICheckBoxButton(127, 25, 6).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor()));        
        this.addElement(new GUITextLabel(136, 24).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.remittance"), false, GUISettings.instance().getSubTextScale()));
        this.addElement(this.enableCODButton = new GUICheckBoxButton(127, 35, 6).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor()));
        this.addElement(new GUITextLabel(136, 34).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.cod"), false, GUISettings.instance().getSubTextScale()));
        this.addElement(this.currencyTextField = new GUITextField(127, 45, 50, 9, 10).enableDynamicBackground().setText("0").setTextScale(GUISettings.instance().getSubTextScale()).setLineOffset(3).cancelDraggedElementLogic().disable());

        this.addElement(this.enablePackageButton = new GUICheckBoxButton(127, 63, 6).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor()));        
        this.addElement(new GUITextLabel(136, 62).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.package"), false, GUISettings.instance().getSubTextScale()));
        this.addElement(this.packageAmountTextField = new GUITextField(127, 73, 50, 9, 4).enableDynamicBackground().setText("0").setTextScale(GUISettings.instance().getSubTextScale()).setLineOffset(3).cancelDraggedElementLogic().disable());

        this.itemsPanel = new GUIButtonPanel(EnumGUIOrientation.VERTICAL, 125, 86, 75, 16).setButtonsOffset(1).setTextScale(GUISettings.instance().getSubTextScale());
        this.addElement(this.itemsPanel);       
        GUIScroller scroller = new GUIScroller(36, 4);
        this.itemsPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(201, 86, 2, 64);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider);

        this.addElement(this.sendButton = new GUIButton(4, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent)
                .enableDynamicBackground(GUISettings.instance().getEnabledButtonColor(), GUISettings.instance().getDisabledButtonColor(), GUISettings.instance().getHoveredButtonColor())
                .setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.sendButton"), true, GUISettings.instance().getButtonTextScale()).disable());     

        this.addElement(this.balanceElement = new BalanceGUIElement(this.getWidth() - 7, this.getHeight() - 10).setEnabledTextColor(GUISettings.instance().getEnabledTextColor())
                .setBalance(WatcherHelperClient.getInt(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID))); 

        this.sendMessageCallback = new SendMessageGUICallback(this.screen, this, 140, 76).enableDefaultBackground();

        this.loadInventoryItems();
    }

    private void loadInventoryItems() {
        this.itemsPanel.reset();
        Set<String> stacks = new HashSet<String>();//is there something better? such filter seems expensive
        InventoryStackGUIButton button;
        for (ItemStack itemStack : ClientReference.getClientPlayer().inventory.mainInventory) {
            if (!itemStack.isEmpty() 
                    && !stacks.contains(this.getKey(itemStack))) {
                button = new InventoryStackGUIButton(itemStack);
                button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
                button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());

                this.itemsPanel.addButton(button);
                stacks.add(this.getKey(itemStack));
            }              
        }
        this.itemsPanel.getScroller().resetPosition();
        this.itemsPanel.getScroller().getSlider().reset();
    }

    //TODO rewrite it for some numeric key value generation
    private String getKey(ItemStack itemStack) {
        //this lacks NBT tag check
        String nbtStr = itemStack.hasTagCompound() ? itemStack.getTagCompound().toString() : "";
        return itemStack.getItem().getRegistryName().toString() + "_" + itemStack.getMetadata() + "_" + itemStack.getItemDamage() + "_" + nbtStr;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (!this.addresseeTextField.getTypedText().isEmpty() 
                    && !this.subjectTextField.getTypedText().isEmpty())
                this.sendButton.enable();
            else
                this.sendButton.disable();
            if (element == this.incomingSectionButton)
                this.screen.getIncomingSection().open();
            if (element == this.sendButton)
                this.sendMessageCallback.open();
            else if (element == this.enableRemittanceButton) {
                if (this.enableRemittanceButton.isToggled()) {
                    this.currencyTextField.enableNumberFieldMode(MathUtils.lesserOfTwo(
                            this.balanceElement.getBalance(), 
                            PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.REMITTANCE_MAX_VALUE.toString(), MailConfig.REMITTANCE_MAX_VALUE.getIntValue())));
                    this.enableCODButton.setToggled(false);
                    this.currencyTextField.enable();
                } else
                    this.currencyTextField.disable();
                this.currencyTextField.setText("0");
                this.packageAmountTextField.setText("0");
                if (this.enablePackageButton.isToggled()) {
                    this.enablePackageButton.setToggled(false);
                    this.packageAmountTextField.disable();
                    if (this.currentStackButton != null)
                        this.currentStackButton.setToggled(false);
                    this.currentStackButton = null;
                }
            } else if (element == this.enableCODButton) {
                if (this.enableCODButton.isToggled()) {
                    this.currencyTextField.enableNumberFieldMode(PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.toString(), MailConfig.PACKAGE_WITH_COD_MAX_VALUE.getIntValue()));
                    this.enableRemittanceButton.setToggled(false);
                    this.currencyTextField.enable();
                } else
                    this.currencyTextField.disable();
                this.currencyTextField.setText("0");
            } else if (element == this.enablePackageButton) {
                if (this.enablePackageButton.isToggled()) {
                    if (this.currentStackButton != null)
                        this.packageAmountTextField.enable();
                } else {
                    if (this.currentStackButton != null)
                        this.currentStackButton.setToggled(false);
                    this.currentStackButton = null;
                    this.packageAmountTextField.disable();
                }
                this.packageAmountTextField.setText("0");
            } else if (element instanceof InventoryStackGUIButton) {
                if (this.enablePackageButton.isToggled()) {
                    InventoryStackGUIButton button = (InventoryStackGUIButton) element;
                    if (this.currentStackButton != button) {
                        if (this.currentStackButton != null)
                            this.currentStackButton.setToggled(false);
                        button.toggle();                    
                        this.currentStackButton = button;
                        int maxAmount = PrivilegeProviderClient.getPrivilegeValue(EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), MailConfig.PACKAGE_MAX_AMOUNT.getIntValue());
                        ItemStack itemStack = this.currentStackButton.index;
                        if (maxAmount < 0) 
                            maxAmount = itemStack.getMaxStackSize();
                        this.packageAmountTextField.enableNumberFieldMode(MathUtils.lesserOfTwo(InventoryHelper.getEqualStackAmount(this.mc.player, itemStack), maxAmount));
                        this.packageAmountTextField.setText("0");
                        this.packageAmountTextField.enable();
                        if (this.enableRemittanceButton.isToggled()) {
                            this.enableRemittanceButton.setToggled(false);
                            this.currencyTextField.disable();
                        } 
                    }
                }
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        boolean flag = super.keyTyped(typedChar, keyCode);   
        if (keyCode == MailKeyHandler.MAIL.getKeyCode() 
                && !this.addresseeTextField.isDragged() 
                && !this.subjectTextField.isDragged() 
                && !this.messageTextBoxField.isDragged() 
                && !this.hasCurrentCallback())
            this.screen.close();
        if (this.addresseeTextField.isDragged()) {
            if (!this.addresseeTextField.getTypedText().isEmpty()) {
                this.addresseeStatusTextLabel.enableFull();
                if (MailManagerClient.isPlayerAvailable(this.addresseeTextField.getTypedText()))
                    this.addresseeStatusTextLabel.setDisplayText(this.playerFoundStr);
                else
                    this.addresseeStatusTextLabel.setDisplayText(this.playerNotFoundStr);
            } else
                this.addresseeStatusTextLabel.disableFull();
        }
        return flag; 
    }

    public void setBalance(int balance) {
        this.balanceElement.setBalance(balance);
    }

    public int getBalance() {
        return this.balanceElement.getBalance();
    }

    public Message createMessage() {
        EnumMail type = EnumMail.LETTER;
        if (this.enableRemittanceButton.isToggled())
            type = EnumMail.REMITTANCE;
        else if (this.enablePackageButton.isToggled()) {
            if (this.enableCODButton.isToggled())
                type = EnumMail.PACKAGE_WITH_COD;
            else
                type = EnumMail.PACKAGE;
        }   
        this.latestMessage = new Message(
                type, 
                ClientReference.getClientPlayer().getName(), 
                this.subjectTextField.getTypedText(), 
                this.messageTextBoxField.getTypedText());
        if (!this.currencyTextField.getTypedText().isEmpty())
            this.latestMessage.setCurrency(this.currencyTextField.getTypedNumber());
        if (this.currentStackButton != null)
            this.latestMessage.setParcel(Parcel.create(
                    this.currentStackButton.index, 
                    this.packageAmountTextField.getTypedNumber()));
        return this.latestMessage;
    }

    public void messageSent(int postage) {
        int delta = this.balanceElement.getBalance() - (this.latestMessage.getCurrency() + postage);
        this.setBalance(delta);
        if (this.latestMessage.getParcel() != null) {
            InventoryHelper.removeEqualStack(ClientReference.getClientPlayer(), this.latestMessage.getParcel().stackWrapper, this.latestMessage.getParcel().amount);
            this.loadInventoryItems();
        }
        this.latestMessage = null;

        this.addresseeTextField.reset();
        this.subjectTextField.reset();
        this.messageTextBoxField.reset();

        this.enableRemittanceButton.setToggled(false);
        this.enablePackageButton.setToggled(false);
        this.enableCODButton.setToggled(false);

        this.currencyTextField.setText("0");
        this.packageAmountTextField.setText("0");

        if (this.currentStackButton != null)
            this.currentStackButton.setToggled(false);
        this.currentStackButton = null;

        this.sendButton.disable();
    }

    public String getAddresseeUsername() {
        return this.addresseeTextField.getTypedText();
    }
}
