package austeretony.oxygen_mail.client.command;

import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.MailMenuScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MailArgumentClient implements ArgumentExecutor {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1)
            OxygenHelperClient.scheduleTask(()->this.openMenu(), 100L, TimeUnit.MILLISECONDS);
        else if (args.length == 2) {
            if (args[1].equals("-reset-data")) {
                MailManagerClient.instance().getMailboxContainer().reset();
                ClientReference.showChatMessage("oxygen_mail.command.dataReset");
            }
        }
    }

    private void openMenu() {
        ClientReference.delegateToClientThread(()->ClientReference.displayGuiScreen(new MailMenuScreen()));
    }
}
