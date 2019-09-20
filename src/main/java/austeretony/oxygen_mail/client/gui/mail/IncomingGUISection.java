package austeretony.oxygen_mail.client.gui.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextBoxLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.PrivilegeProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.gui.elements.CurrencyValueGUIElement;
import austeretony.oxygen_core.client.gui.elements.InventoryLoadGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenu;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement;
import austeretony.oxygen_core.client.gui.elements.OxygenSorterGUIElement.EnumSorting;
import austeretony.oxygen_core.client.gui.elements.SectionsGUIDDList;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_core.server.OxygenPlayerData;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.AttachmentGUIElement;
import austeretony.oxygen_mail.client.gui.mail.incoming.IncomingBackgroundGUIFiller;
import austeretony.oxygen_mail.client.gui.mail.incoming.MessageGUIButton;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.RemoveMessageGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.ReturnAttachmentGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.TakeAttachmentGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.RemoveMessageContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReturnAttachmentContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.TakeAttachmentContextAction;
import austeretony.oxygen_mail.client.input.MailKeyHandler;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Mail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;

public class IncomingGUISection extends AbstractGUISection {

    private final MailMenuGUIScreen screen;

    private OxygenSorterGUIElement timeSorterElement, subjectSorterElement;

    private OxygenGUIButtonPanel messagesPanel;

    private OxygenGUIText messagesAmountLabel;

    private InventoryLoadGUIElement inventoryLoadElement;

    private CurrencyValueGUIElement balanceElement;

    private AbstractGUICallback takeAttachmentCallback, returnAttachmentCallback, removeMessageCallback;

    //message content

    private OxygenGUIText senderTextLabel, receiveTimeTextLabel, expireTimeTextLabel, messageSubjectTextLabel, 
    attachmentTitleTextLabel, codCostTextLabel;

    private GUITextBoxLabel messageTextBoxLabel;

    private AttachmentGUIElement attachmentElement;

    //cache

    private MessageGUIButton currentMessageButton;

    private Mail currentMessage;

    public IncomingGUISection(MailMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new IncomingBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_mail.gui.mail.title"), GUISettings.get().getTitleScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(new SectionsGUIDDList(this.getWidth() - 4, 5, this, this.screen.getSendingSection()));

        this.addElement(this.messagesAmountLabel = new OxygenGUIText(0, 18, "", GUISettings.get().getSubTextScale() - 0.05F, GUISettings.get().getEnabledTextColor()));   

        this.addElement(this.timeSorterElement = new OxygenSorterGUIElement(6, 18, EnumSorting.DOWN, ClientReference.localize("oxygen_mail.sorting.receiveTime")));   

        this.timeSorterElement.setClickListener((sorting)->{
            this.subjectSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(0);
            else
                this.sortMail(1);
        });

        this.addElement(this.subjectSorterElement = new OxygenSorterGUIElement(12, 18, EnumSorting.INACTIVE, ClientReference.localize("oxygen_mail.sorting.subject")));  

        this.subjectSorterElement.setClickListener((sorting)->{
            this.timeSorterElement.reset();
            if (sorting == EnumSorting.DOWN)
                this.sortMail(2);
            else
                this.sortMail(3);
        });

        int maxRows = MathUtils.clamp(MailManagerClient.instance().getMailboxContainer().getMessagesAmount(), 11, PrivilegeProviderClient.getValue(EnumMailPrivilege.MAILBOX_SIZE.toString(), MailConfig.MAILBOX_SIZE.getIntValue()));
        this.addElement(this.messagesPanel = new OxygenGUIButtonPanel(this.screen, 6, 24, 75, 11, 1, maxRows, 11, GUISettings.get().getPanelTextScale(), true));

        this.messagesPanel.<MessageGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->{
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

        this.messagesPanel.initContextMenu(new OxygenGUIContextMenu(GUISettings.get().getContextMenuWidth(), 9, 
                new TakeAttachmentContextAction(this),
                new ReturnAttachmentContextAction(this),
                new RemoveMessageContextAction(this)));

        this.addElement(this.inventoryLoadElement = new InventoryLoadGUIElement(4, this.getHeight() - 9, EnumGUIAlignment.RIGHT));
        this.inventoryLoadElement.updateLoad();
        this.addElement(this.balanceElement = new CurrencyValueGUIElement(this.getWidth() - 10, this.getHeight() - 10));   
        this.balanceElement.setValue(WatcherHelperClient.getLong(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID));

        this.takeAttachmentCallback = new TakeAttachmentGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.returnAttachmentCallback = new ReturnAttachmentGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.removeMessageCallback = new RemoveMessageGUICallback(this.screen, this, 140, 38).enableDefaultBackground();

        this.initMessageElements();
    }

    private void initMessageElements() {
        this.addElement(this.senderTextLabel = new OxygenGUIText(87, 18, "", GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()).disableFull()); 
        this.addElement(this.receiveTimeTextLabel = new OxygenGUIText(87, 26, "", GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()).disableFull());   
        this.addElement(this.expireTimeTextLabel = new OxygenGUIText(87, 34, "", GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColorDark()).disableFull());   

        this.addElement(this.messageSubjectTextLabel = new OxygenGUIText(87, 44, "", GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()).disableFull());
        this.addElement(this.messageTextBoxLabel = new GUITextBoxLabel(88, 54, 120, 84).setEnabledTextColor(GUISettings.get().getEnabledTextColor())
                .setTextScale(GUISettings.get().getSubTextScale()).setLineOffset(2).disableFull());

        this.addElement(this.attachmentElement = new AttachmentGUIElement(87, 139).disableFull()); 
    }

    private void sortMail(int mode) {
        this.resetMessageContent();

        List<Mail> mail = new ArrayList<>(MailManagerClient.instance().getMailboxContainer().getMessages());

        if (mode == 0)
            Collections.sort(mail, (m1, m2)->(int) ((m2.getId() - m1.getId()) / 10_000L));
        else if (mode == 1)
            Collections.sort(mail, (m1, m2)->(int) ((m1.getId() - m2.getId()) / 10_000L));
        else if (mode == 2)
            Collections.sort(mail, (m1, m2)->m1.getLocalizedSubject().compareTo(m2.getLocalizedSubject()));
        else if (mode == 3)
            Collections.sort(mail, (m1, m2)->m2.getLocalizedSubject().compareTo(m1.getLocalizedSubject()));

        this.messagesPanel.reset();
        for (Mail msg : mail)
            this.messagesPanel.addButton(new MessageGUIButton(msg));

        this.messagesAmountLabel.setDisplayText(String.valueOf(MailManagerClient.instance().getMailboxContainer().getMessagesAmount()) 
                + "/" + String.valueOf(PrivilegeProviderClient.getValue(EnumMailPrivilege.MAILBOX_SIZE.toString(), MailConfig.MAILBOX_SIZE.getIntValue())));     
        this.messagesAmountLabel.setX(80 - this.textWidth(this.messagesAmountLabel.getDisplayText(), GUISettings.get().getSubTextScale() - 0.05F));

        this.messagesPanel.getScroller().resetPosition();
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {}

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuGUIScreen.MAIL_MENU_ENTRY.getIndex() + 2)
                    this.screen.close();
            } else if (keyCode == MailKeyHandler.MAIL_MENU.getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    private void loadMessageContent(long messageId) {
        this.currentMessage = MailManagerClient.instance().getMailboxContainer().getMessage(messageId);

        this.senderTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.sender", this.currentMessage.getLocalizedSenderName()));
        this.senderTextLabel.enableFull(); 
        this.expireTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.expires", getExpirationTimeLocalizedString(this.currentMessage.getType(), this.currentMessage.getId())));
        this.expireTimeTextLabel.enableFull();   
        this.receiveTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.received", OxygenUtils.getTimePassedLocalizedString(this.currentMessage.getId())));
        this.receiveTimeTextLabel.enableFull();   

        this.messageSubjectTextLabel.setDisplayText(ClientReference.localize(this.currentMessage.getSubject()));
        this.messageSubjectTextLabel.enableFull();
        this.messageTextBoxLabel.setDisplayText(ClientReference.localize(this.currentMessage.getMessage()));
        this.messageTextBoxLabel.enableFull();

        if (this.currentMessage.isPending()) {
            this.attachmentElement.load(this.currentMessage);
            this.attachmentElement.enableFull();
            if (this.currentMessage.getType() == EnumMail.PACKAGE_WITH_COD
                    && this.balanceElement.getValue() < this.currentMessage.getCurrency())
                this.attachmentElement.disable();
            if (!this.currentMessage.isPending())
                this.attachmentElement.disableFull();
        }
    }

    private void resetMessageContent() {
        this.senderTextLabel.disableFull(); 
        this.expireTimeTextLabel.disableFull();   
        this.receiveTimeTextLabel.disableFull();   

        this.messageSubjectTextLabel.disableFull();
        this.messageTextBoxLabel.disableFull();

        this.attachmentElement.disableFull();
    }

    public MessageGUIButton getCurrentMessageButton() {
        return this.currentMessageButton;
    }

    public Mail getCurrentMessage() {
        return this.currentMessage;
    }

    private static String getExpirationTimeLocalizedString(EnumMail type, long millis) {
        int expiresIn = - 1;
        switch (type) {
        case SYSTEM_LETTER:
            expiresIn = MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case LETTER:
            expiresIn = MailConfig.LETTER_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case SYSTEM_REMITTANCE:
            expiresIn = MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case REMITTANCE:
            expiresIn = MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case SYSTEM_PACKAGE:
            expiresIn = MailConfig.SYSTEM_PACKAGE_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case PACKAGE:
            expiresIn = MailConfig.PACKAGE_EXPIRE_TIME_HOURS.getIntValue();
            break;
        case PACKAGE_WITH_COD:
            expiresIn = MailConfig.PACKAGE_WITH_COD_EXPIRE_TIME_HOURS.getIntValue();
            break;  
        }
        if (expiresIn < 0)
            return ClientReference.localize("oxygen_mail.gui.neverExpires");
        return OxygenUtils.getExpirationTimeLocalizedString(expiresIn * 3_600_000L, millis);
    }

    public void sharedDataSynchronized() {}

    public void mailSynchronized() {
        this.sortMail(0);
    }

    public void messageSent(Parcel parcel, long balance) {
        this.balanceElement.setValue(balance);
        if (parcel != null) {
            InventoryHelper.removeEqualStack(this.mc.player, parcel.stackWrapper.getItemStack(), parcel.amount);
            this.inventoryLoadElement.updateLoad();
            this.screen.updateInventoryContent();
        }
    }

    public void messageRemoved(long messageId) {
        this.timeSorterElement.setSorting(EnumSorting.DOWN);
        this.subjectSorterElement.reset();
        this.sortMail(0);
    }

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.balanceElement.setValue(balance);
        if (parcel != null) {
            InventoryHelper.addItemStack(this.mc.player, parcel.stackWrapper.getItemStack(), parcel.amount);
            this.inventoryLoadElement.updateLoad();
            this.screen.updateInventoryContent();
        }

        if (this.currentMessage != null && this.currentMessage.getId() == oldMessageId) {
            this.currentMessageButton.setPending(false);
            this.currentMessage.setPending(false);
            this.attachmentElement.disableFull();
        }
    }

    public InventoryLoadGUIElement getInventoryLoadElement() {
        return this.inventoryLoadElement;
    }

    public CurrencyValueGUIElement getBalanceElement() {
        return this.balanceElement;
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
