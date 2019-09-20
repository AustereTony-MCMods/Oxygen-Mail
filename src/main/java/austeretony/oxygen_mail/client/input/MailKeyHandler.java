package austeretony.oxygen_mail.client.input;

import org.lwjgl.input.Keyboard;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_mail.client.MailManagerClient;
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
            MailManagerClient.instance().getMailMenuManager().openMailMenu();
    }
}
