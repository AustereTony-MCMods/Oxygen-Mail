package austeretony.oxygen_mail.client.gui.mail;

import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen.client.gui.SynchronizedGUIScreen;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.util.ResourceLocation;

public class MailMenuGUIScreen extends SynchronizedGUIScreen {

    public static final ResourceLocation 
    INCOMING_BACKGROUND_TEXTURE = new ResourceLocation(MailMain.MODID, "textures/gui/mail/mail_incoming.png"),
    SENDING_BACKGROUND_TEXTURE = new ResourceLocation(MailMain.MODID, "textures/gui/mail/mail_sending.png"),
    
    //
    
    EXCLAMATION_MARK_ICONS_SMALL = new ResourceLocation(MailMain.MODID, "textures/gui/mail/exclamation_mark_icons_small.png"),
    HOLLOW_RHOMBUS_ICONS_SMALL = new ResourceLocation(MailMain.MODID, "textures/gui/mail/hollow_rhombus_icons_small.png"),
    
    //
    
    MESSAGE_CALLBACK_BACKGROUND = new ResourceLocation(MailMain.MODID, "textures/gui/mail/message_callback.png"),
    SENDING_CALLBACK_BACKGROUND = new ResourceLocation(MailMain.MODID, "textures/gui/mail/dending_callback.png");

    protected IncomingGUISection incomingSection;

    protected SendingGUISection sendingSection;

    public MailMenuGUIScreen() {
        super(MailMain.MAIL_MENU_SCREEN_ID);
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        return new GUIWorkspace(this, 203, 168).setAlignment(EnumGUIAlignment.RIGHT, - 10, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.incomingSection = new IncomingGUISection(this));    
        this.getWorkspace().initSection(this.sendingSection = new SendingGUISection(this));        
    }

    @Override
    protected AbstractGUISection getDefaultSection() {
        return this.incomingSection;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element) {}

    @Override
    protected boolean doesGUIPauseGame() {
        return false;
    }

    @Override
    public void loadData() {
        this.incomingSection.sortMail(0);
    }

    public IncomingGUISection getIncomingSection() {
        return this.incomingSection;
    }

    public SendingGUISection getSendingSection() {
        return this.sendingSection;
    }
}