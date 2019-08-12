package austeretony.oxygen_mail.client.gui;

import austeretony.oxygen.client.gui.AbstractMenuEntry;
import austeretony.oxygen.client.sync.gui.api.AdvancedGUIHandlerClient;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.util.ResourceLocation;

public class MailMenuEntry extends AbstractMenuEntry {

    @Override
    public String getName() {
        return "oxygen_mail.gui.mail.title";
    }

    @Override
    public ResourceLocation getIcon() {
        //TODO
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() {
        AdvancedGUIHandlerClient.openScreen(MailMain.MAIL_MENU_SCREEN_ID);
    }
}
