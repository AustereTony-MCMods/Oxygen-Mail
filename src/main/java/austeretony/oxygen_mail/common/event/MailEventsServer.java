package austeretony.oxygen_mail.common.event;

import austeretony.oxygen.common.api.event.OxygenPlayerLoadedEvent;
import austeretony.oxygen.common.api.event.OxygenPrivilegesLoadedEvent;
import austeretony.oxygen.common.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_mail.common.MailManagerServer;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MailEventsServer {

    @SubscribeEvent
    public void onPrivilegesLoaded(OxygenPrivilegesLoadedEvent event) {
        MailMain.addDefaultPrivileges();
    }

    @SubscribeEvent
    public void onWorldLoaded(OxygenWorldLoadedEvent event) {
        MailManagerServer.instance().loadMailboxes();
    }

    @SubscribeEvent
    public void onPlayerLoaded(OxygenPlayerLoadedEvent event) {
        MailManagerServer.instance().playerLoaded(event.player);
    }
}