package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.common.main.EnumMail;

public class ReturnAttachmentContextAction extends AbstractContextAction {

    private final IncomingGUISection section;

    public ReturnAttachmentContextAction(IncomingGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_mail.gui.context.return");
    }

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        if (this.section.getCurrentMessage().isPending()) {
            EnumMail type = this.section.getCurrentMessage().type;
            if ((type == EnumMail.REMITTANCE 
                    || type == EnumMail.PACKAGE 
                    || type == EnumMail.PACKAGE_WITH_COD) 
                    && !this.section.getCurrentMessage().senderName.equals(ClientReference.getClientPlayer().getName())) {
                return true;
            }         
        }
        return false;
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        this.section.openReturnAttachmentCallback();
    }
}