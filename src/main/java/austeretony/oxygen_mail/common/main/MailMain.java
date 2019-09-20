package austeretony.oxygen_mail.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeImpl;
import austeretony.oxygen_core.common.privilege.PrivilegedGroupImpl;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_mail.client.MailDataSyncHandlerClient;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.MailStatusMessagesHandler;
import austeretony.oxygen_mail.client.command.MailArgumentExecutorClient;
import austeretony.oxygen_mail.client.event.MailEventsClient;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.client.input.MailKeyHandler;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.network.client.CPAttachmentReceived;
import austeretony.oxygen_mail.common.network.client.CPMessageRemoved;
import austeretony.oxygen_mail.common.network.client.CPMessageSent;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;
import austeretony.oxygen_mail.server.MailDataSyncHandlerServer;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.event.MailEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MailMain.MODID, 
        name = MailMain.NAME, 
        version = MailMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.9.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MailMain.VERSIONS_FORGE_URL)
public class MailMain {

    public static final String 
    MODID = "oxygen_mail",
    NAME = "Oxygen: Mail",
    VERSION = "0.9.0",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Merchants/info/mod_versions_forge.json";

    public static final int 
    MAIL_MOD_INDEX = 8,

    MAIL_MENU_SCREEN_ID = 80,

    MAIL_DATA_ID = 80,

    INCOMING_MESSAGE_NOTIFICATION_ID = 80,

    MESSAGE_SENDING_REQUEST_ID = 85,
    MESSAGE_OPERATION_REQUEST_ID = 86;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new MailConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgumentExecutor(new MailArgumentExecutorClient("mail", true));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        MailManagerServer.create();
        CommonReference.registerEvent(new MailEventsServer());
        RequestsFilterHelper.registerNetworkRequest(MESSAGE_SENDING_REQUEST_ID, 1);
        RequestsFilterHelper.registerNetworkRequest(MESSAGE_OPERATION_REQUEST_ID, 1);
        OxygenHelperServer.registerDataSyncHandler(new MailDataSyncHandlerServer());
        if (event.getSide() == Side.CLIENT) {
            MailManagerClient.create();
            CommonReference.registerEvent(new MailEventsClient());
            if (!OxygenGUIHelper.isOxygenMenuEnabled())
                CommonReference.registerEvent(new MailKeyHandler());
            OxygenGUIHelper.registerOxygenMenuEntry(MailMenuGUIScreen.MAIL_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new MailStatusMessagesHandler());
            OxygenHelperClient.registerSharedDataSyncListener(MAIL_MENU_SCREEN_ID, ()->MailManagerClient.instance().getMailMenuManager().sharedDataSynchronized());
            OxygenHelperClient.registerDataSyncHandler(new MailDataSyncHandlerClient());
        }
        EnumMailPrivilege.register();
    }

    public static void addDefaultPrivileges() {
        if (!PrivilegeProviderServer.getGroup(PrivilegedGroupImpl.OPERATORS_GROUP.groupName).hasPrivilege(EnumMailPrivilege.MAILBOX_SIZE.toString())) {
            PrivilegeProviderServer.addPrivileges(PrivilegedGroupImpl.OPERATORS_GROUP.groupName, true,  
                    new PrivilegeImpl(EnumMailPrivilege.MAILBOX_SIZE.toString(), 150),

                    new PrivilegeImpl(EnumMailPrivilege.MAIL_SENDING_DELAY_SECONDS.toString(), 10),
                    new PrivilegeImpl(EnumMailPrivilege.REMITTANCE_MAX_VALUE.toString(), 1_000_000L),
                    new PrivilegeImpl(EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), 1000),
                    new PrivilegeImpl(EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.toString(), 100_000L),

                    new PrivilegeImpl(EnumMailPrivilege.LETTER_POSTAGE_VALUE.toString(), 0L),
                    new PrivilegeImpl(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.toString(), 0),
                    new PrivilegeImpl(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), 0L),
                    new PrivilegeImpl(EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), 0));
            LOGGER.info("Default <{}> group privileges added.", PrivilegedGroupImpl.OPERATORS_GROUP.groupName);
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPMessageSent.class);
        OxygenMain.network().registerPacket(CPMessageRemoved.class);
        OxygenMain.network().registerPacket(CPAttachmentReceived.class);

        OxygenMain.network().registerPacket(SPMessageOperation.class);
        OxygenMain.network().registerPacket(SPSendMessage.class);
    }
}
