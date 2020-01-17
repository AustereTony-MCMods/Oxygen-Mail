package austeretony.oxygen_mail.client.gui.mail;

import java.util.LinkedHashMap;
import java.util.Map;

import austeretony.alternateui.screen.core.AbstractGUIScreen;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.alternateui.screen.core.GUIWorkspace;
import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_mail.client.gui.menu.MailMenuEntry;
import austeretony.oxygen_mail.client.settings.gui.EnumMailGUISetting;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.item.ItemStack;

public class MailMenuScreen extends AbstractGUIScreen {

    public static final OxygenMenuEntry MAIL_MENU_ENTRY = new MailMenuEntry();

    protected IncomingMailSection incomingSection;

    protected SendingSection sendingSection;

    public final Map<ItemStackWrapper, Integer> inventoryContent = new LinkedHashMap<>();

    public final boolean allowMailSending;

    public MailMenuScreen() {
        OxygenHelperClient.syncSharedData(MailMain.MAIL_MENU_SCREEN_ID);
        OxygenHelperClient.syncData(MailMain.MAIL_DATA_ID);
        this.updateInventoryContent();

        this.allowMailSending = PrivilegesProviderClient.getAsBoolean(EnumMailPrivilege.ALLOW_MAIL_SENDING.id(), true);
    }

    @Override
    protected GUIWorkspace initWorkspace() {
        EnumGUIAlignment alignment = EnumGUIAlignment.CENTER;
        switch (EnumMailGUISetting.MAIL_MENU_ALIGNMENT.get().asInt()) {
        case - 1: 
            alignment = EnumGUIAlignment.LEFT;
            break;
        case 0:
            alignment = EnumGUIAlignment.CENTER;
            break;
        case 1:
            alignment = EnumGUIAlignment.RIGHT;
            break;    
        default:
            alignment = EnumGUIAlignment.CENTER;
            break;
        }
        return new GUIWorkspace(this, 213, 170).setAlignment(alignment, 0, 0);
    }

    @Override
    protected void initSections() {
        this.getWorkspace().initSection(this.incomingSection = (IncomingMailSection) new IncomingMailSection(this).enable());    
        this.getWorkspace().initSection(this.sendingSection = (SendingSection) new SendingSection(this).enable());        
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

    public void informPlayer(EnumMailStatusMessage status) {
        OxygenManagerClient.instance().getChatMessagesManager().showStatusMessage(MailMain.MAIL_MOD_INDEX, status.ordinal());
    }

    public void updateInventoryContent() {
        this.inventoryContent.clear();
        ItemStackWrapper wrapper;
        int amount;
        for (ItemStack itemStack : ClientReference.getClientPlayer().inventory.mainInventory) {
            if (!itemStack.isEmpty()) {
                wrapper = ItemStackWrapper.getFromStack(itemStack);
                if (!this.inventoryContent.containsKey(wrapper))
                    this.inventoryContent.put(wrapper, itemStack.getCount());
                else {
                    amount = this.inventoryContent.get(wrapper);
                    amount += itemStack.getCount();
                    this.inventoryContent.put(wrapper, amount);
                }
            }
        }
    }

    public int getEqualStackAmount(ItemStackWrapper stackWrapper) {
        int amount = 0;
        for (ItemStackWrapper wrapper : this.inventoryContent.keySet())
            if (wrapper.isEquals(stackWrapper))
                amount += this.inventoryContent.get(wrapper);
        return amount;
    }

    public void sharedDataSynchronized() {
        this.incomingSection.sharedDataSynchronized();
        this.sendingSection.sharedDataSynchronized();
    }

    public void mailSynchronized() {
        this.incomingSection.mailSynchronized();
        this.sendingSection.mailSynchronized();
    }

    public void messageSent(Parcel parcel, long balance) {
        this.incomingSection.messageSent(parcel, balance);
        this.sendingSection.messageSent(parcel, balance);
    }

    public void messageRemoved(long messageId) {
        this.incomingSection.messageRemoved(messageId);
        this.sendingSection.messageRemoved(messageId);
    }

    public void attachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.incomingSection.attachmentReceived(oldMessageId, parcel, balance);
        this.sendingSection.attachmentReceived(oldMessageId, parcel, balance);
    }

    public IncomingMailSection getIncomingSection() {
        return this.incomingSection;
    }

    public SendingSection getSendingSection() {
        return this.sendingSection;
    }
}
