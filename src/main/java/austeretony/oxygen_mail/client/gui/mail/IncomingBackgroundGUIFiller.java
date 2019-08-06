package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;

public class IncomingBackgroundGUIFiller extends BackgroundGUIFiller {

    public IncomingBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height, MailMenuGUIScreen.INCOMING_BACKGROUND_TEXTURE);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
        drawRect(0, 0, this.getWidth(), 13, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
        drawRect(0, 14, 78, 23, GUISettings.instance().getAdditionalGUIBackgroundColor());//search panel background
        drawRect(0, 24, 75, this.getHeight(), GUISettings.instance().getPanelGUIBackgroundColor());//panel background
        drawRect(76, 34, 78, this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor());//slider background
        drawRect(79, 14, this.getWidth(), 32, GUISettings.instance().getAdditionalGUIBackgroundColor());//message header background
        drawRect(79, 33, this.getWidth(), 138, GUISettings.instance().getAdditionalGUIBackgroundColor());//message background
        drawRect(79, 139, this.getWidth(), this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor());//attachment background
    }
}
