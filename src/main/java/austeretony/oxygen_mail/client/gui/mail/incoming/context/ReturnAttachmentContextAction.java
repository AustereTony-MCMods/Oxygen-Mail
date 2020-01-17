package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_mail.client.gui.mail.IncomingMailSection;
import austeretony.oxygen_mail.common.EnumMail;

public class ReturnAttachmentContextAction implements OxygenContextMenuAction {

    private final IncomingMailSection section;

    public ReturnAttachmentContextAction(IncomingMailSection section) {
        this.section = section;
    }

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
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
