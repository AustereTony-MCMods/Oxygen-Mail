package austeretony.oxygen_mail.client.gui.menu;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.settings.EnumMailClientSetting;

public class MailMenuEntry implements OxygenMenuEntry {

    @Override
    public int getId() {
        return 80;
    }

    @Override
    public String getLocalizedName() {
        return ClientReference.localize("oxygen_mail.gui.mail.title");
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_M;
    }

    @Override
    public boolean isValid() {
        return EnumMailClientSetting.ADD_MAIL_MENU.get().asBoolean();
    }

    @Override
    public void open() {
        MailManagerClient.instance().getMailMenuManager().openMailMenu();
    }
}
