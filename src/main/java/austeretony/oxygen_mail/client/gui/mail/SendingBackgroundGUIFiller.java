package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;

public class SendingBackgroundGUIFiller extends BackgroundGUIFiller {

    public SendingBackgroundGUIFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height, MailMenuGUIScreen.SENDING_BACKGROUND_TEXTURE);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
        drawRect(0, 0, this.getWidth(), 13, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
        drawRect(0, 14, 124, this.getHeight() - 15, GUISettings.instance().getAdditionalGUIBackgroundColor());//message background
        drawRect(0, this.getHeight() - 14, this.getWidth(), this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor());//send button background
        drawRect(125, 14, this.getWidth(), 85, GUISettings.instance().getAdditionalGUIBackgroundColor());//attachemnt background
        drawRect(125, 86, this.getWidth() - 3, this.getHeight() - 15, GUISettings.instance().getPanelGUIBackgroundColor());//items panel background
        drawRect(this.getWidth() - 2, 86, this.getWidth(), this.getHeight() - 15, GUISettings.instance().getAdditionalGUIBackgroundColor());//items panel background
    }
}
