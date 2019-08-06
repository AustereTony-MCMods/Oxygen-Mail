package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;

public class RemoveMessageContextAction extends AbstractContextAction {

    private final IncomingGUISection section;

    public RemoveMessageContextAction(IncomingGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_mail.gui.context.remove");
    }

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        return !this.section.getCurrentMessage().isPending();
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        this.section.openRemoveMessageCallback();
    }
}