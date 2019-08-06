package austeretony.oxygen_mail.client.command;

import java.util.Set;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.common.api.command.AbstractArgumentExecutor;
import austeretony.oxygen.common.api.command.ArgumentParameter;
import austeretony.oxygen.common.command.IArgumentParameter;
import austeretony.oxygen_mail.client.MailManagerClient;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MailArgumentExecutorClient extends AbstractArgumentExecutor {

    public static final String ACTION_RESET_DATA = "reset-data";

    public MailArgumentExecutorClient(String argument, boolean hasParams) {
        super(argument, hasParams);
    }

    @Override
    public void getParams(Set<IArgumentParameter> params) {        
        params.add(new ArgumentParameter(ACTION_RESET_DATA));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Set<IArgumentParameter> params) throws CommandException {
        for (IArgumentParameter param : params) {
            if (param.getBaseName().equals(ACTION_RESET_DATA)) {
                MailManagerClient.instance().reset();
                ClientReference.showMessage("oxygen_mail.command.dataReset");
            }
        }
    }
}