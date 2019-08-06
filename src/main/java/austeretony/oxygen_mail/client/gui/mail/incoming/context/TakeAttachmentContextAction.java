package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.contextmenu.AbstractContextAction;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen.client.api.WatcherHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.common.itemstack.InventoryHelper;
import austeretony.oxygen.common.main.OxygenPlayerData;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.common.main.EnumMail;

public class TakeAttachmentContextAction extends AbstractContextAction {

    private final IncomingGUISection section;

    public TakeAttachmentContextAction(IncomingGUISection section) {
        this.section = section;
    }

    @Override
    protected String getName(GUIBaseElement currElement) {
        return this.section.getCurrentMessage().type == EnumMail.PACKAGE_WITH_COD ? ClientReference.localize("oxygen_mail.gui.context.pay") : ClientReference.localize("oxygen_mail.gui.context.take");
    }

    @Override
    protected boolean isValid(GUIBaseElement currElement) {
        if (this.section.getCurrentMessage().isPending()) {
            EnumMail type = this.section.getCurrentMessage().type;
            if (type == EnumMail.PACKAGE 
                    || type == EnumMail.SERVICE_PACKAGE 
                    || type == EnumMail.PACKAGE_WITH_COD) {
                if (type == EnumMail.PACKAGE_WITH_COD 
                        && WatcherHelperClient.getInt(OxygenPlayerData.CURRENCY_COINS_WATCHER_ID) < this.section.getCurrentMessage().getCurrency())
                    return false;
                if (InventoryHelper.haveEnoughSpace(ClientReference.getClientPlayer(), this.section.getCurrentMessage().getParcel().amount))
                    return true;
            } else//remittance
                return true;
        }
        return false;
    }

    @Override
    protected void execute(GUIBaseElement currElement) {
        this.section.openTakeAttachmentCallback();
    }
}