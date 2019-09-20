package austeretony.oxygen_mail.client.gui.mail.incoming.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIContextMenuElement.ContextMenuAction;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_mail.client.gui.mail.IncomingGUISection;
import austeretony.oxygen_mail.common.EnumMail;
import net.minecraft.item.ItemStack;

public class TakeAttachmentContextAction implements ContextMenuAction {

    private final IncomingGUISection section;

    public TakeAttachmentContextAction(IncomingGUISection section) {
        this.section = section;
    }

    @Override
    public String getName(GUIBaseElement currElement) {
        return this.section.getCurrentMessage().getType() == EnumMail.PACKAGE_WITH_COD ? 
                ClientReference.localize("oxygen_mail.gui.context.pay") : ClientReference.localize("oxygen_mail.gui.context.take");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        if (this.section.getCurrentMessage().isPending()) {
            EnumMail type = this.section.getCurrentMessage().getType();
            if (type == EnumMail.PACKAGE 
                    || type == EnumMail.SYSTEM_PACKAGE 
                    || type == EnumMail.PACKAGE_WITH_COD) {
                if (type == EnumMail.PACKAGE_WITH_COD 
                        && this.section.getBalanceElement().getValue() < this.section.getCurrentMessage().getCurrency())
                    return false;
                ItemStack itemStack = this.section.getCurrentMessage().getParcel().stackWrapper.getCachedItemStack();
                if (InventoryHelper.haveEnoughSpace(ClientReference.getClientPlayer(), this.section.getCurrentMessage().getParcel().amount, itemStack.getMaxStackSize()))
                    return true;
            } else
                return true;
        }
        return false;
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openTakeAttachmentCallback();
    }
}
