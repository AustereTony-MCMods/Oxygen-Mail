package austeretony.oxygen_mail.client.gui.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxLabel;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.elements.OxygenButton;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenCurrencyValue;
import austeretony.oxygen_core.client.gui.elements.OxygenInventoryLoad;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenSectionSwitcher;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter;
import austeretony.oxygen_core.client.gui.elements.OxygenSorter.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.IncomingMailBackgroundFiller;
import austeretony.oxygen_mail.client.gui.mail.incoming.MessagePanelEntry;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.RemoveMessageCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.ReturnAttachmentCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.TakeAttachmentCallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.RemoveMessageContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReturnAttachmentContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.TakeAttachmentContextAction;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class IncomingMailSection extends AbstractGUISection {

    private final MailMenuScreen screen;

    private OxygenButton takeAttachmentButton, removeMessageButton;

    private OxygenTextLabel messagesAmountLabel;

    private OxygenSorter timeSorterElement, subjectSorterElement;

    private OxygenScrollablePanel messagesPanel;

    private OxygenInventoryLoad inventoryLoad;

    private OxygenCurrencyValue balanceValue;

    private AbstractGUICallback takeAttachmentCallback, returnAttachmentCallback, removeMessageCallback;

    //message content

    private OxygenTextLabel senderTextLabel, receiveTimeTextLabel, expireTimeTextLabel, messageSubjectTextLabel, 
    attachmentTitleTextLabel, codCostTextLabel;

    private GUITextBoxLabel messageTextBoxLabel;

    private MessageAttachment attachment;

    //cache

    private MessagePanelEntry currentMessageButton;

    private Mail currentMessage;

    public IncomingMailSection(MailMenuScreen screen) {
        super(screen);
        this.screen = screen;
        this.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.incoming"));
    }

    @Override
    public void init() {
        this.addElement(new IncomingMailBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.title"), EnumBaseGUISetting.TEXT_TITLE_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(new OxygenSectionSwitcher(this.getWidth() - 4, 5, this, this.screen.getSendingSection()));

        this.addElement(this.messagesAmountLabel = new OxygenTextLabel(0, 22, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.timeSorterElement = new OxygenSorter(6, 18, EnumSorting.DOWN, ClientReference.localize("oxygen_mail.sorting.receiveTime")));   

        this.timeSorterElement.setClickListener((sorting)->{
            this.subjectSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(0);
            else
                this.sortMail(1);
        });

        this.addElement(this.subjectSorterElement = new OxygenSorter(12, 18, EnumSorting.INACTIVE, ClientReference.localize("oxygen_mail.sorting.subject")));  

        this.subjectSorterElement.setClickListener((sorting)->{
            this.timeSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(2);
            else
                this.sortMail(3);
        });

        this.addElement(this.messagesPanel = new OxygenScrollablePanel(this.screen, 6, 24, 75, 10, 1, 100, 12, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), true));

        this.messagesPanel.<MessagePanelEntry>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
            if (this.currentMessageButton != clicked) {       
                if (this.currentMessageButton != null)
                    this.currentMessageButton.setToggled(false);
                clicked.toggle();    
                this.currentMessageButton = clicked;
                this.resetMessageContent();
                this.loadMessageContent(clicked.index);
                if (!MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(clicked.index)) {
                    MailManagerClient.instance().getMailboxContainer().markAsRead(clicked.index);
                    MailManagerClient.instance().getMailboxContainer().setChanged(true);
                    clicked.read();
                }
            }
        });

        this.messagesPanel.initContextMenu(new OxygenContextMenu(
                new TakeAttachmentContextAction(this),
                new ReturnAttachmentContextAction(this),
                new RemoveMessageContextAction(this)));

        this.addElement(this.inventoryLoad = new OxygenInventoryLoad(6, this.getHeight() - 8));
        this.inventoryLoad.updateLoad();
        this.addElement(this.balanceValue = new OxygenCurrencyValue(this.getWidth() - 14, this.getHeight() - 10));   
        this.balanceValue.setValue(OxygenMain.COMMON_CURRENCY_INDEX, WatcherHelperClient.getLong(OxygenMain.COMMON_CURRENCY_INDEX));

        this.initMessageElements();

        this.takeAttachmentCallback = new TakeAttachmentCallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.returnAttachmentCallback = new ReturnAttachmentCallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.removeMessageCallback = new RemoveMessageCallback(this.screen, this, 140, 38).enableDefaultBackground();
    }

    private void initMessageElements() {
        this.addElement(this.senderTextLabel = new OxygenTextLabel(88, 23, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.receiveTimeTextLabel = new OxygenTextLabel(88, 31, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.expireTimeTextLabel = new OxygenTextLabel(88, 39, "", EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F, EnumBaseGUISetting.TEXT_DARK_ENABLED_COLOR.get().asInt()).disableFull()); 

        this.addElement(this.messageSubjectTextLabel = new OxygenTextLabel(88, 51, "", EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()).disableFull());
        this.addElement(this.messageTextBoxLabel = new GUITextBoxLabel(90, 54, 120, 84).setEnabledTextColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt())
                .setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat()).setLineOffset(2).disableFull());

        this.addElement(this.takeAttachmentButton = new OxygenButton(38, this.getHeight() - 11, 40, 10, ClientReference.localize("oxygen_core.gui.take")).disable());  
        this.takeAttachmentButton.setKeyPressListener(Keyboard.KEY_E, ()->this.takeAttachment());

        this.addElement(this.removeMessageButton = new OxygenButton(84, this.getHeight() - 11, 40, 10, ClientReference.localize("oxygen_core.gui.remove")).disable());  
        this.removeMessageButton.setKeyPressListener(Keyboard.KEY_X, ()->this.removeMessage());

        this.addElement(this.attachment = new MessageAttachment(88, 139).disableFull()); 
    }

    private void sortMail(int mode) {
        this.resetMessageContent();

        List<Mail> mail = new ArrayList<>(MailManagerClient.instance().getMailboxContainer().getMessages());

        if (mode == 0)
            Collections.sort(mail, (m1, m2)->m2.getId() < m1.getId() ? - 1 : m2.getId() > m1.getId() ? 1 : 0);
        else if (mode == 1)
            Collections.sort(mail, (m1, m2)->m1.getId() < m2.getId() ? - 1 : m1.getId() > m2.getId() ? 1 : 0);
        else if (mode == 2)
            Collections.sort(mail, (m1, m2)->localize(m1.getSubject()).compareTo(localize(m2.getSubject())));
        else if (mode == 3)
            Collections.sort(mail, (m1, m2)->localize(m2.getSubject()).compareTo(localize(m1.getSubject())));

        this.messagesPanel.reset();
        for (Mail msg : mail)
            this.messagesPanel.addEntry(new MessagePanelEntry(msg));

        int maxAmount = PrivilegesProviderClient.getAsInt(EnumMailPrivilege.MAILBOX_SIZE.id(), MailConfig.MAILBOX_SIZE.asInt());
        this.messagesAmountLabel.setDisplayText(String.valueOf(mail.size()) + "/" + String.valueOf(maxAmount));     
        this.messagesAmountLabel.setX(84 - this.textWidth(this.messagesAmountLabel.getDisplayText(), this.messagesAmountLabel.getTextScale()));

        this.messagesPanel.getScroller().reset();
        this.messagesPanel.getScroller().updateRowsAmount(MathUtils.clamp(mail.size(), 12, MathUtils.greaterOfTwo(mail.size(), maxAmount)));
    }

    private static String localize(String value) {
        return ClientReference.localize(value);
    }

    private void takeAttachment() {
        if (this.getCurrentMessage().isPending()) {
            boolean valid = false;
            EnumMail type = this.getCurrentMessage().getType();
            if (type == EnumMail.PACKAGE 
                    || type == EnumMail.SYSTEM_PACKAGE 
                    || type == EnumMail.PACKAGE_WITH_COD) {
                if (type == EnumMail.PACKAGE_WITH_COD 
                        && this.getBalanceValue().getValue() < this.getCurrentMessage().getCurrency())
                    valid = false;
                ItemStack itemStack = this.getCurrentMessage().getParcel().stackWrapper.getCachedItemStack();
                if (InventoryHelper.haveEnoughSpace(ClientReference.getClientPlayer(), this.getCurrentMessage().getParcel().amount, itemStack.getMaxStackSize()))
                    valid = true;
            } else
                valid = true;
            if (valid)
                this.openTakeAttachmentCallback();
        }
    }

    private void removeMessage() {
        if (!this.getCurrentMessage().isPending()) 
            this.removeMessageCallback.open();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.takeAttachmentButton)
                this.takeAttachment();
            else if (element == this.removeMessageButton)
                this.removeMessage();
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuScreen.MAIL_MENU_ENTRY.getKeyCode())
                    this.screen.close();
            } else if (MailConfig.ENABLE_MAIL_MENU_KEY.asBoolean() 
                    && keyCode == MailManagerClient.instance().getKeyHandler().getMailMenuKeybinding().getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    private void loadMessageContent(long messageId) {
        this.currentMessage = MailManagerClient.instance().getMailboxContainer().getMessage(messageId);

        this.senderTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.sender", 
                this.currentMessage.getSenderUsername().equals("mail.sender.sys") ? TextFormatting.YELLOW + localize(this.currentMessage.getSenderUsername()) : localize(this.currentMessage.getSenderUsername())));
        this.senderTextLabel.enableFull(); 

        this.receiveTimeTextLabel.setDisplayText(OxygenHelperClient.getDateFormat().format(new Date(this.currentMessage.getId())));
        this.receiveTimeTextLabel.initTooltip(ClientReference.localize("oxygen_mail.gui.mail.msg.received", OxygenUtils.getTimePassedLocalizedString(this.currentMessage.getId())));
        this.receiveTimeTextLabel.enableFull(); 

        this.expireTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.expires", getExpirationTimeLocalizedString(this.currentMessage.getType(), this.currentMessage.getId())));
        this.expireTimeTextLabel.enableFull();   

        this.messageSubjectTextLabel.setDisplayText(ClientReference.localize(this.currentMessage.getSubject()));
        this.messageSubjectTextLabel.enableFull();
        this.messageTextBoxLabel.setDisplayText(ClientReference.localize(this.currentMessage.getMessage()));
        this.messageTextBoxLabel.enableFull();

        if (this.currentMessage.isPending()) {
            this.attachment.load(this.currentMessage);
            this.attachment.enableFull();
            if (!this.currentMessage.isPending())
                this.attachment.disableFull();

            this.takeAttachmentButton.enable();
            if (this.currentMessage.getType() == EnumMail.PACKAGE_WITH_COD) {
                this.takeAttachmentButton.setDisplayText(String.format("[%s] %s", ClientReference.getGameSettings().getKeyDisplayString(Keyboard.KEY_E), 
                        ClientReference.localize("oxygen_core.gui.pay")));
                if (this.balanceValue.getValue() < this.currentMessage.getCurrency()) {
                    this.attachment.disable();
                    this.takeAttachmentButton.disable();
                }
            } else
                this.takeAttachmentButton.setDisplayText(String.format("[%s] %s", ClientReference.getGameSettings().getKeyDisplayString(Keyboard.KEY_E), 
                        ClientReference.localize("oxygen_core.gui.take")));
            if (this.inventoryLoad.isOverloaded())
                this.takeAttachmentButton.disable();
        } else
            this.removeMessageButton.enable();
    }

    private void resetMessageContent() {
        this.senderTextLabel.disableFull(); 
        this.expireTimeTextLabel.disableFull();   
        this.receiveTimeTextLabel.disableFull();   

        this.messageSubjectTextLabel.disableFull();
        this.messageTextBoxLabel.disableFull();

        this.attachment.disableFull();

        this.takeAttachmentButton.disable();  
        this.removeMessageButton.disable();
    }

    public MessagePanelEntry getCurrentMessageButton() {
        return this.currentMessageButton;
    }

    public Mail getCurrentMessage() {
        return this.currentMessage;
    }

    private static String getExpirationTimeLocalizedString(EnumMail type, long millis) {
        int expiresIn = - 1;
        switch (type) {
        case SYSTEM_LETTER:
            expiresIn = MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case LETTER:
            expiresIn = MailConfig.LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case SYSTEM_REMITTANCE:
            expiresIn = MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case REMITTANCE:
            expiresIn = MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case SYSTEM_PACKAGE:
            expiresIn = MailConfig.SYSTEM_PACKAGE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PACKAGE:
            expiresIn = MailConfig.PACKAGE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PACKAGE_WITH_COD:
            expiresIn = MailConfig.PACKAGE_WITH_COD_EXPIRE_TIME_HOURS.asInt();
            break;  
        }
        if (expiresIn < 0)
            return ClientReference.localize("oxygen_mail.gui.neverExpires");
        return OxygenUtils.getExpirationTimeLocalizedString(expiresIn * 3_600_000L, millis);
    }

    public void sharedDataSynchronized() {}

    public void mailSynchronized() {
        this.sortMail(0);
        this.initLatestMessageOnMenuOpen();
    }

    private void initLatestMessageOnMenuOpen() {
        if (!this.messagesPanel.visibleButtons.isEmpty()) {
            GUIButton button = this.messagesPanel.visibleButtons.get(0).toggle();
            this.messagesPanel.setPreviousClickedButton(button);
            this.currentMessageButton = (MessagePanelEntry) button;
            this.loadMessageContent(this.currentMessageButton.index);

            if (!MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(this.currentMessageButton.index)) {
                MailManagerClient.instance().getMailboxContainer().markAsRead(this.currentMessageButton.index);
                MailManagerClient.instance().getMailboxContainer().setChanged(true);
                this.currentMessageButton.read();
            }
        }
    }

    public void messageSent(Parcel parcel, long balance) {
        this.balanceValue.updateValue(balance);
        if (parcel != null) {
            InventoryHelper.removeEqualStack(this.mc.player, parcel.stackWrapper.getItemStack(), parcel.amount);
            this.inventoryLoad.updateLoad();
            this.screen.updateInventoryContent();
        }
    }

    public void messageRemoved(long messageId) {
        this.timeSorterElement.setSorting(EnumSorting.DOWN);
        this.subjectSorterElement.reset();
        this.sortMail(0);
        this.initLatestMessageOnMenuOpen();
    }

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.balanceValue.updateValue(balance);
        if (parcel != null) {
            InventoryHelper.addItemStack(this.mc.player, parcel.stackWrapper.getItemStack(), parcel.amount);
            this.inventoryLoad.updateLoad();
            this.screen.updateInventoryContent();
        }

        if (this.currentMessage != null && this.currentMessage.getId() == oldMessageId) {
            this.currentMessageButton.setPending(false);
            this.currentMessage.setPending(false);
            this.attachment.disableFull();
        }

        this.takeAttachmentButton.disable();
        this.removeMessageButton.enable();
    }

    public OxygenInventoryLoad getInventoryLoad() {
        return this.inventoryLoad;
    }

    public OxygenCurrencyValue getBalanceValue() {
        return this.balanceValue;
    }

    public void openTakeAttachmentCallback() {
        this.takeAttachmentCallback.open();
    }

    public void openReturnAttachmentCallback() {
        this.returnAttachmentCallback.open();
    }

    public void openRemoveMessageCallback() {
        this.removeMessageCallback.open();
    }
}
