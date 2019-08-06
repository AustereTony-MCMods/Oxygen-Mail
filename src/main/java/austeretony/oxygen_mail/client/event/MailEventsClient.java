package austeretony.oxygen_mail.client.event;

import austeretony.oxygen.client.api.event.OxygenChatMessageEvent;
import austeretony.oxygen.client.api.event.OxygenClientInitEvent;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.main.EnumMailChatMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsClient {

    @SubscribeEvent
    public void onClientInit(OxygenClientInitEvent event) {
        MailManagerClient.instance().loadMail();
    }

    @SubscribeEvent
    public void onChatMessage(OxygenChatMessageEvent event) {
        if (event.modIndex == MailMain.MAIL_MOD_INDEX)
            EnumMailChatMessage.values()[event.messageIndex].show(event.args);
    }
}
