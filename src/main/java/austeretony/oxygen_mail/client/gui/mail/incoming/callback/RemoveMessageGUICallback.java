package austeretony.oxygen_mail.client.gui.mail.incoming.callback;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.common.EnumMessageOperation;

public class RemoveMessageGUICallback extends AbstractGUICallback {

    private final MailMenuGUIScreen screen;

    private final IncomingGUISection section;

    private OxygenGUIButton confirmButton, cancelButton;

    public RemoveMessageGUICallback(MailMenuGUIScreen screen, IncomingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_mail.gui.mail.callback.removeMessage"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));
        this.addElement(new OxygenGUIText(6, 17, ClientReference.localize("oxygen_mail.gui.mail.callback.removeMessage.request"), GUISettings.get().getSubTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.confirmButton = new OxygenGUIButton(15, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.confirmButton")));
        this.addElement(this.cancelButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.cancelButton")));
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) { 
            if (element == this.cancelButton)
                this.close();
            else if (element == this.confirmButton) {
                MailManagerClient.instance().getMailboxManager().processMessageOperationSynced(this.section.getCurrentMessage().getId(), EnumMessageOperation.REMOVE_MESSAGE);
                this.close();
            }
        }
    }
}
