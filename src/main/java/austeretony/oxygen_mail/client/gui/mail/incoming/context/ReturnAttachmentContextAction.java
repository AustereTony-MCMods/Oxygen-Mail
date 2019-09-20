package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.common.EnumMail;

public class ReturnAttachmentContextAction implements ContextMenuAction {

    private final IncomingGUISection section;

    public ReturnAttachmentContextAction(IncomingGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_mail.gui.context.return");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        if (this.section.getCurrentMessage().isPending()) {
            EnumMail type = this.section.getCurrentMessage().getType();
            if ((type == EnumMail.REMITTANCE 
                    || type == EnumMail.PACKAGE 
                    || type == EnumMail.PACKAGE_WITH_COD) 
                    && !this.section.getCurrentMessage().getSenderUsername().equals(ClientReference.getClientPlayer().getName())) {
                return true;
            }         
        }
        return false;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openReturnAttachmentCallback();
    }
}