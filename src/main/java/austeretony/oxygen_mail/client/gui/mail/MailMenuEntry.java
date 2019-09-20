package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen_core.client.gui.menu.AbstractMenuEntry;
import austeretony.oxygen_mail.client.MailManagerClient;

public class MailMenuEntry extends AbstractMenuEntry {

    @Override
    public String getName() {
        return "oxygen_mail.gui.mail.title";
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() {
        MailManagerClient.instance().getMailMenuManager().openMailMenu();
    }
}
