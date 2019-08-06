package austeretony.oxygen_mail.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen.client.api.OxygenGUIHelper;
import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.command.CommandOxygenClient;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.sync.gui.api.AdvancedGUIHandlerClient;
import austeretony.oxygen.common.api.OxygenHelperServer;
import austeretony.oxygen.common.api.network.OxygenNetwork;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.common.main.OxygenMain;
import austeretony.oxygen.common.privilege.api.Privilege;
import austeretony.oxygen.common.privilege.api.PrivilegeProviderServer;
import austeretony.oxygen.common.privilege.api.PrivilegedGroup;
import austeretony.oxygen.common.sync.gui.api.AdvancedGUIHandlerServer;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.MailMenuHandlerClient;
import austeretony.oxygen_mail.client.command.MailArgumentExecutorClient;
import austeretony.oxygen_mail.client.event.MailEventsClient;
import austeretony.oxygen_mail.client.input.MailKeyHandler;
import austeretony.oxygen_mail.common.ItemsBlackList;
import austeretony.oxygen_mail.common.MailManagerServer;
import austeretony.oxygen_mail.common.MailMenuHandlerServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.event.MailEventsServer;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MailMain.MODID, 
        name = MailMain.NAME, 
        version = MailMain.VERSION,
        dependencies = "required-after:oxygen@[0.8.0,);",//TODO Always check required Oxygen version before build
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MailMain.VERSIONS_FORGE_URL)
public class MailMain {

    public static final String 
    MODID = "oxygen_mail",
    NAME = "Oxygen: Mail",
    VERSION = "0.8.0",
    VERSION_CUSTOM = VERSION + ":alpha:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Merchants/info/mod_versions_forge.json";

    public static final int 
    MAIL_MOD_INDEX = 8,//Oxygen - 0, Teleportation - 1, Groups - 2, Exchange - 3, Merchants - 4, Players List - 5, Friends List - 6, Interaction - 7, Chat - 9

    MAIL_MENU_SCREEN_ID = 80,

    INCOMING_MESSAGE_NOTIFICATION_ID = 80;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static OxygenNetwork network; 

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperServer.registerConfig(new MailConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgumentExecutor(new MailArgumentExecutorClient("mail", true));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        MailManagerServer.create();
        CommonReference.registerEvent(new MailEventsServer());
        OxygenHelperServer.registerSharedDataIdentifierForScreen(MAIL_MENU_SCREEN_ID, OxygenMain.ACTIVITY_STATUS_SHARED_DATA_ID);
        AdvancedGUIHandlerServer.registerScreen(MAIL_MENU_SCREEN_ID, new MailMenuHandlerServer());
        ItemsBlackList.create(CommonReference.getGameFolder() + "/config/oxygen/mail/items_blacklist.json");
        if (event.getSide() == Side.CLIENT) {
            MailManagerClient.create();
            CommonReference.registerEvent(new MailKeyHandler());
            CommonReference.registerEvent(new MailEventsClient());
            OxygenGUIHelper.registerScreenId(MAIL_MENU_SCREEN_ID);
            AdvancedGUIHandlerClient.registerScreen(MAIL_MENU_SCREEN_ID, new MailMenuHandlerClient());
            OxygenHelperClient.registerNotificationIcon(INCOMING_MESSAGE_NOTIFICATION_ID, OxygenGUITextures.ENVELOPE_ICONS);
        }
    }

    public static void addDefaultPrivileges() {
        if (!PrivilegeProviderServer.getGroup(PrivilegedGroup.OPERATORS_GROUP.groupName).hasPrivilege(EnumMailPrivilege.MAILBOX_SIZE.toString())) {
            PrivilegeProviderServer.addPrivileges(PrivilegedGroup.OPERATORS_GROUP.groupName, true,  
                    new Privilege(EnumMailPrivilege.MAILBOX_SIZE.toString(), 150),

                    new Privilege(EnumMailPrivilege.MAIL_SENDING_DELAY.toString(), 10),
                    new Privilege(EnumMailPrivilege.REMITTANCE_MAX_VALUE.toString(), 1000000),
                    new Privilege(EnumMailPrivilege.PACKAGE_MAX_AMOUNT.toString(), 1000),
                    new Privilege(EnumMailPrivilege.PACKAGE_WITH_COD_MAX_VALUE.toString(), 100000),

                    new Privilege(EnumMailPrivilege.LETTER_POSTAGE_VALUE.toString(), 0),
                    new Privilege(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.toString(), 0),
                    new Privilege(EnumMailPrivilege.PACKAGE_POSTAGE_VALUE.toString(), 0),
                    new Privilege(EnumMailPrivilege.PACKAGE_WITH_COD_POSTAGE_PERCENT.toString(), 0));
            LOGGER.info("Default <{}> group privileges added.", PrivilegedGroup.OPERATORS_GROUP.groupName);
        }
    }

    private void initNetwork() {
        network = OxygenHelperServer.createNetworkHandler(MODID);

        network.registerPacket(SPMessageOperation.class);
        network.registerPacket(SPSendMessage.class);
    }

    public static OxygenNetwork network() {
        return network;
    }
}
