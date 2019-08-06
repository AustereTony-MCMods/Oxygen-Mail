package austeretony.oxygen_mail.client.gui.mail.incoming.callback;

import austeretony.alternateui.screen.button.GUIButton;
import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.text.GUITextLabel;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen.common.main.OxygenSoundEffects;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.common.EnumMessageOperation;

public class TakeAttachmentGUICallback extends AbstractGUICallback {

    private final MailMenuGUIScreen screen;

    private final IncomingGUISection section;

    private GUIButton confirmButton, cancelButton;

    public TakeAttachmentGUICallback(MailMenuGUIScreen screen, IncomingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new MessageCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new GUITextLabel(2, 2).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.callback.takeAttachment"), true, GUISettings.instance().getTitleScale()));
        this.addElement(new GUITextLabel(2, 14).setDisplayText(ClientReference.localize("oxygen_mail.gui.mail.callback.takeAttachment.request"), true, GUISettings.instance().getTextScale()));

        this.addElement(this.confirmButton = new GUIButton(15, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.confirmButton"), true, GUISettings.instance().getButtonTextScale()));
        this.addElement(this.cancelButton = new GUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10).setSound(OxygenSoundEffects.BUTTON_CLICK.soundEvent).enableDynamicBackground().setDisplayText(ClientReference.localize("oxygen.gui.cancelButton"), true, GUISettings.instance().getButtonTextScale()));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                MailManagerClient.instance().processMessageOperationSynced(this.section.getCurrentMessage().getId(), EnumMessageOperation.TAKE_ATTACHMENT);
                this.section.attachmentTaken();
                this.close();
            }
        }
    }
}
