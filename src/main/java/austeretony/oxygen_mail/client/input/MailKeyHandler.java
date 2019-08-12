package austeretony.oxygen_mail.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.sync.gui.api.AdvancedGUIHandlerClient;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class MailKeyHandler {

    public static final KeyBinding MAIL_MENU = new KeyBinding("key.oxygen_mail.openMailMenu", Keyboard.KEY_RBRACKET, "Oxygen");

    public MailKeyHandler() {
        ClientReference.registerKeyBinding(MAIL_MENU);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {        
        if (MAIL_MENU.isPressed())
            AdvancedGUIHandlerClient.openScreen(MailMain.MAIL_MENU_SCREEN_ID);
    }
}
