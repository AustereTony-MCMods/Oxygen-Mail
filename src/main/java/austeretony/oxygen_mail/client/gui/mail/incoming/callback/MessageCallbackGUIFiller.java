package austeretony.oxygen_mail.client.gui.mail.incoming.callback;

import austeretony.oxygen.client.gui.BackgroundGUIFiller;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;

public class MessageCallbackGUIFiller extends BackgroundGUIFiller {

    public MessageCallbackGUIFiller(int xPosition, int yPosition, int width, int height) {             
        super(xPosition, yPosition, width, height, MailMenuGUIScreen.SENDING_CALLBACK_BACKGROUND);
    }

    @Override
    public void drawDefaultBackground() {
        drawRect(- 1, - 1, this.getWidth() + 1, this.getHeight() + 1, GUISettings.instance().getBaseGUIBackgroundColor());//main background
        drawRect(0, 0, this.getWidth(), 11, GUISettings.instance().getAdditionalGUIBackgroundColor());//title background
        drawRect(0, 12, this.getWidth(), this.getHeight(), GUISettings.instance().getAdditionalGUIBackgroundColor());//rest background
    }
}