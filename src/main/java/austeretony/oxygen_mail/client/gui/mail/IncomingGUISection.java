package austeretony.oxygen_mail.client.gui.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import austeretony.alternateui.screen.browsing.GUIScroller;
import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.button.GUISlider;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.contextmenu.GUIContextMenu;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.panel.GUIButtonPanel;
import austeretony.alternateui.screen.text.GUITextBoxLabel;
import austeretony.alternateui.screen.text.GUITextField;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.alternateui.util.EnumGUIOrientation;
import austeretony.oxygen.client.api.OxygenGUIHelper;
import austeretony.oxygen.client.api.SoundEventHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen.util.MathUtils;
import austeretony.oxygen.util.OxygenUtils;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.incoming.IncomingMessageGUIButton;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.RemoveMessageGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.ReturnAttachmentGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.callback.TakeAttachmentGUICallback;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.RemoveMessageContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.ReturnAttachmentContextAction;
import austeretony.oxygen_mail.client.gui.mail.incoming.context.TakeAttachmentContextAction;
import austeretony.oxygen_mail.client.input.MailKeyHandler;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMail;
import austeretony.oxygen_mail.common.main.Mail;

public class IncomingGUISection extends AbstractGUISection {

    private final MailMenuGUIScreen screen;

    private GUIButton sendSectionButton, searchButton, refreshButton, sortUpButton, sortDownButton;

    private GUIButtonPanel profilesPanel;

    private GUITextLabel profilesAmountTextLabel;

    private GUITextField searchField;

    private IncomingMessageGUIButton currentMessageButton;

    private Mail currentMessage;

    private AbstractGUICallback takeAttachmentCallback, returnAttachmentCallback, removeMessageCallback;

    //message content

    private GUITextLabel senderTextLabel, receiveTimeTextLabel, expireTimeTextLabel, messageSubjectTextLabel, 
    attachmentTitleTextLabel, codCostTextLabel;

    private GUITextBoxLabel messageTextBoxLabel;

    private AttachmentGUIElement attachmentElement;

    public IncomingGUISection(MailMenuGUIScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    public void init() {
        this.addElement(new IncomingBackgroundGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 4).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.title"), false, GUISettings.instance().getTitleScale()));

        this.addElement(new GUIButton(this.getWidth() - 28, 1, 10, 10).setTexture(OxygenGUITextures.ENVELOPE_ICONS, 10, 10).initSimpleTooltip(ClientReference.localize("oxygen_mail.gui.mail.incoming"), GUISettings.instance().getTooltipScale()).toggle()); 
        this.addElement(this.sendSectionButton = new GUIButton(this.getWidth() - 12, 1, 10, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.PENCIL_ICONS, 10, 10).initSimpleTooltip(ClientReference.localize("oxygen_mail.gui.mail.sending"), GUISettings.instance().getTooltipScale())); 

        this.addElement(this.searchButton = new GUIButton(7, 15, 7, 7).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SEARCH_ICONS, 7, 7).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.search"), GUISettings.instance().getTooltipScale()));   
        this.addElement(this.sortDownButton = new GUIButton(2, 19, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_DOWN_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.sortUpButton = new GUIButton(2, 15, 3, 3).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.SORT_UP_ICONS, 3, 3).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.sort"), GUISettings.instance().getTooltipScale())); 
        this.addElement(this.refreshButton = new GUIButton(0, 14, 10, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).setTexture(OxygenGUITextures.REFRESH_ICONS, 9, 9).initSimpleTooltip(ClientReference.localize("oxygen.tooltip.refresh"), GUISettings.instance().getTooltipScale()));
        this.addElement(this.profilesAmountTextLabel = new GUITextLabel(0, 15).setTextScale(GUISettings.instance().getSubTextScale()));   

        this.profilesPanel = new GUIButtonPanel(EnumGUIOrientation.VERTICAL, 0, 24, 75, 11).setButtonsOffset(1).setTextScale(GUISettings.instance().getPanelTextScale());
        this.addElement(this.profilesPanel);
        this.addElement(this.searchField = new GUITextField(0, 14, 78, 9, Mail.MESSAGE_TITLE_MAX_LENGTH)
                .enableDynamicBackground(GUISettings.instance().getEnabledTextFieldColor(), GUISettings.instance().getDisabledTextFieldColor(), GUISettings.instance().getHoveredTextFieldColor())
                .setDisplayText("...", false, GUISettings.instance().getSubTextScale()).setLineOffset(3).cancelDraggedElementLogic().disableFull());

        this.profilesPanel.initSearchField(this.searchField);
        GUIScroller scroller = new GUIScroller(MathUtils.clamp(MailManagerClient.instance().getMessagesAmount(), 12, MailConfig.MAILBOX_SIZE.getIntValue()), 12);
        this.profilesPanel.initScroller(scroller);
        GUISlider slider = new GUISlider(76, 24, 2, 144);
        slider.setDynamicBackgroundColor(GUISettings.instance().getEnabledSliderColor(), GUISettings.instance().getDisabledSliderColor(), GUISettings.instance().getHoveredSliderColor());
        scroller.initSlider(slider); 

        GUIContextMenu menu = new GUIContextMenu(GUISettings.instance().getContextMenuWidth(), 10).setScale(GUISettings.instance().getContextMenuScale()).setTextScale(GUISettings.instance().getTextScale()).setTextAlignment(EnumGUIAlignment.LEFT, 2);
        menu.setOpenSound(OxygenSoundEffects.CONTEXT_OPEN.soundEvent);
        menu.setCloseSound(OxygenSoundEffects.CONTEXT_CLOSE.soundEvent);
        this.profilesPanel.initContextMenu(menu);
        menu.enableDynamicBackground(GUISettings.instance().getEnabledContextActionColor(), GUISettings.instance().getDisabledContextActionColor(), GUISettings.instance().getHoveredContextActionColor());
        menu.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
        menu.addElement(new TakeAttachmentContextAction(this));
        menu.addElement(new ReturnAttachmentContextAction(this));
        menu.addElement(new RemoveMessageContextAction(this));

        this.takeAttachmentCallback = new TakeAttachmentGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.returnAttachmentCallback = new ReturnAttachmentGUICallback(this.screen, this, 140, 38).enableDefaultBackground();
        this.removeMessageCallback = new RemoveMessageGUICallback(this.screen, this, 140, 38).enableDefaultBackground();

        this.initMessageElements();
    }

    private void initMessageElements() {
        this.addElement(this.senderTextLabel = new GUITextLabel(81, 15).setEnabledTextColor(GUISettings.instance().getEnabledTextColorDark()).setTextScale(GUISettings.instance().getSubTextScale()).disableFull()); 
        this.addElement(this.receiveTimeTextLabel = new GUITextLabel(81, 23).setEnabledTextColor(GUISettings.instance().getEnabledTextColorDark()).setTextScale(GUISettings.instance().getSubTextScale()).disableFull());   
        this.addElement(this.expireTimeTextLabel = new GUITextLabel(0, 15).setEnabledTextColor(GUISettings.instance().getEnabledTextColorDark()).setTextScale(GUISettings.instance().getSubTextScale()).disableFull());   

        this.addElement(this.messageSubjectTextLabel = new GUITextLabel(81, 35).setTextScale(GUISettings.instance().getTextScale()).disableFull());
        this.addElement(this.messageTextBoxLabel = new GUITextBoxLabel(80, 46, 120, 90).setTextScale(GUISettings.instance().getSubTextScale()).setLineOffset(2).disableFull());

        this.addElement(this.attachmentTitleTextLabel = new GUITextLabel(81, 141).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.attachment"), false, GUISettings.instance().getTextScale()).disableFull());
        this.addElement(this.attachmentElement = new AttachmentGUIElement(81, 150).setEnabledTextColor(GUISettings.instance().getEnabledTextColor()).disableFull()); 
    }

    public void sortMail(int mode) {
        this.resetMessageContent();

        List<Mail> mail = new ArrayList<Mail>(MailManagerClient.instance().getMessages());

        if (mode == 0)
            Collections.sort(mail, (m1, m2)->(int) ((m2.getId() - m1.getId()) / 10_000L));
        else
            Collections.sort(mail, (m1, m2)->(int) ((m1.getId() - m2.getId()) / 10_000L));

        this.profilesPanel.reset();
        IncomingMessageGUIButton button;
        for (Mail msg : mail) {
            button = new IncomingMessageGUIButton(msg);
            button.enableDynamicBackground(GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getEnabledElementColor(), GUISettings.instance().getHoveredElementColor());
            if (!MailManagerClient.instance().isMarkedAsRead(msg.getId()))
                button.setTextDynamicColor(0xFF99FFFF, 0xFF7ACCCC, 0xFFCCFFFF);
            else
                button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
            button.setDisplayText(ClientReference.localize(msg.subject));
            button.setTextAlignment(EnumGUIAlignment.LEFT, 2);

            this.profilesPanel.addButton(button);
        }

        this.profilesAmountTextLabel.setDisplayText(String.valueOf(MailManagerClient.instance().getMessagesAmount()) + " / " + String.valueOf(MailConfig.MAILBOX_SIZE.getIntValue()));     
        this.profilesAmountTextLabel.setX(75 - this.textWidth(this.profilesAmountTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));
        this.refreshButton.setX(this.profilesAmountTextLabel.getX() - 11);

        this.searchField.reset();

        this.profilesPanel.getScroller().resetPosition();
        this.profilesPanel.getScroller().getSlider().reset();

        this.sortUpButton.toggle();
        this.sortDownButton.setToggled(false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchField.isEnabled() && !this.searchField.isHovered()) {
            this.sortUpButton.enableFull();
            this.sortDownButton.enableFull();
            this.searchButton.enableFull();
            this.refreshButton.enableFull();
            this.searchField.disableFull();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);                 
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) {
            if (element == this.sendSectionButton)
                this.screen.getSendingSection().open();
            else if (element == this.refreshButton)
                this.sortMail(0);
            else if (element == this.searchButton) {
                this.searchField.enableFull();
                this.sortUpButton.disableFull();
                this.sortDownButton.disableFull();
                this.searchButton.disableFull();
                this.refreshButton.disableFull();
            } else if (element == this.sortDownButton) {
                if (!this.sortDownButton.isToggled()) {
                    this.sortMail(1);
                    this.sortUpButton.setToggled(false);
                    this.sortDownButton.toggle(); 
                }
            } else if (element == this.sortUpButton) {
                if (!this.sortUpButton.isToggled()) {
                    this.sortMail(0);
                    this.sortDownButton.setToggled(false);
                    this.sortUpButton.toggle();
                }
            }
        }
        if (element instanceof IncomingMessageGUIButton) {
            IncomingMessageGUIButton button = (IncomingMessageGUIButton) element;
            if (this.currentMessageButton != button) {       
                if (this.currentMessageButton != null)
                    this.currentMessageButton.setToggled(false);
                button.toggle();    
                this.currentMessageButton = button;
                this.resetMessageContent();
                this.loadMessageContent(button.index);
                if (!MailManagerClient.instance().isMarkedAsRead(button.index)) {
                    MailManagerClient.instance().markAsRead(button.index);
                    MailManagerClient.instance().saveMail();
                    button.setTextDynamicColor(GUISettings.instance().getEnabledTextColor(), GUISettings.instance().getDisabledTextColor(), GUISettings.instance().getHoveredTextColor());
                }
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {   
        if (!this.searchField.isDragged() && !this.hasCurrentCallback())
            if (OxygenGUIHelper.isOxygenMenuEnabled()) {
                if (keyCode == MailMenuGUIScreen.MAIL_MENU_ENTRY.index + 2)
                    this.screen.close();
            } else if (keyCode == MailKeyHandler.MAIL_MENU.getKeyCode())
                this.screen.close();
        return super.keyTyped(typedChar, keyCode); 
    }

    private void loadMessageContent(long messageId) {
        this.currentMessage = MailManagerClient.instance().getMessage(messageId);

        this.senderTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.sender", ClientReference.localize(this.currentMessage.senderName)));
        this.senderTextLabel.enableFull(); 
        this.expireTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.expires", this.getExpirationTimeLocalizedString(this.currentMessage.type, this.currentMessage.getId())));
        this.expireTimeTextLabel.setX(this.getWidth() - 2 - this.textWidth(this.expireTimeTextLabel.getDisplayText(), GUISettings.instance().getSubTextScale()));
        this.expireTimeTextLabel.enableFull();   
        this.receiveTimeTextLabel.setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.msg.received", OxygenUtils.getTimePassedLocalizedString(this.currentMessage.getId())));
        this.receiveTimeTextLabel.enableFull();   

        this.messageSubjectTextLabel.setDisplayText(ClientReference.localize(this.currentMessage.subject));
        this.messageSubjectTextLabel.enableFull();
        this.messageTextBoxLabel.setDisplayText(ClientReference.localize(this.currentMessage.message));
        this.messageTextBoxLabel.enableFull();

        if (this.currentMessage.hasAttachment()) {
            this.attachmentTitleTextLabel.enableFull();
            this.attachmentElement.setAttachment(this.currentMessage);
            this.attachmentElement.enableFull();
            if (this.currentMessage.type == EnumMail.PACKAGE_WITH_COD
                    && this.screen.getSendingSection().getBalance() < this.currentMessage.getCurrency())
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

        this.attachmentTitleTextLabel.disableFull();
        this.attachmentElement.disableFull();
    }

    public IncomingMessageGUIButton getCurrentMessageButton() {
        return this.currentMessageButton;
    }

    public Mail getCurrentMessage() {
        return this.currentMessage;
    }

    private String getExpirationTimeLocalizedString(EnumMail type, long millis) {
        long expiresIn = 0L;
        switch (type) {
        case SERVICE_LETTER:
            expiresIn = MailConfig.SERVICE_LETTER_EXPIRE_TIME.getIntValue();
            break;
        case LETTER:
            expiresIn = MailConfig.LETTER_EXPIRE_TIME.getIntValue();
            break;
        case SERVICE_REMITTANCE:
            expiresIn = MailConfig.SERVICE_REMITTANCE_EXPIRE_TIME.getIntValue();
            break;
        case REMITTANCE:
            expiresIn = MailConfig.REMITTANCE_EXPIRE_TIME.getIntValue();
            break;
        case SERVICE_PACKAGE:
            expiresIn = MailConfig.SERVICE_PACKAGE_EXPIRE_TIME.getIntValue();
            break;
        case PACKAGE:
            expiresIn = MailConfig.PACKAGE_EXPIRE_TIME.getIntValue();
            break;
        case PACKAGE_WITH_COD:
            expiresIn = MailConfig.PACKAGE_WITH_COD_EXPIRE_TIME.getIntValue();
            break;  
        }
        if (expiresIn < 0L)
            return ClientReference.localize("oxygen_mail.gui.neverExpires");
        return OxygenUtils.getExpirationTimeLocalizedString(expiresIn * 3_600_000L, millis);
    }

    public void attachmentTaken() {//TODO clean this mess
        MailManagerClient.instance().removeMessage(this.currentMessage.getId());
        this.currentMessage.setId(this.currentMessage.getId() + 1L);
        this.currentMessage.setPending(false);
        MailManagerClient.instance().addMessage(this.currentMessage);
        MailManagerClient.instance().markAsRead(this.currentMessage.getId());
        MailManagerClient.instance().saveMail();

        this.attachmentElement.disableFull();

        int balance = this.screen.getSendingSection().getBalance();
        if (this.currentMessage.type == EnumMail.REMITTANCE || this.currentMessage.type == EnumMail.SERVICE_REMITTANCE) {
            SoundEventHelperClient.playSoundClient(OxygenSoundEffects.SELL.id);
            balance += this.currentMessage.getCurrency(); 
        } else if (this.currentMessage.type == EnumMail.PACKAGE_WITH_COD) {            
            SoundEventHelperClient.playSoundClient(OxygenSoundEffects.SELL.id);
            balance -= this.currentMessage.getCurrency();
        }
        this.screen.getSendingSection().setBalance(balance);

        if (this.currentMessage.type == EnumMail.PACKAGE 
                || this.currentMessage.type == EnumMail.SERVICE_PACKAGE
                || this.currentMessage.type == EnumMail.PACKAGE_WITH_COD) {
            SoundEventHelperClient.playSoundClient(OxygenSoundEffects.INVENTORY.id);
        }
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