package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import java.util.HashSet;
import java.util.Set;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackGUIFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButton;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIButtonPanel;
import austeretony.oxygen_core.client.gui.elements.OxygenGUIText;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.client.gui.mail.SendingGUISection;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryItemGUIButton;

public class SelectItemGUICallback extends AbstractGUICallback {

    private final MailMenuGUIScreen screen;

    private final SendingGUISection section;

    private OxygenGUIButtonPanel inventoryContentPanel;

    private OxygenGUIButton closeButton;

    public SelectItemGUICallback(MailMenuGUIScreen screen, SendingGUISection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.addElement(new OxygenCallbackGUIFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenGUIText(4, 5, ClientReference.localize("oxygen_mail.gui.mail.callback.selectItem"), GUISettings.get().getTextScale(), GUISettings.get().getEnabledTextColor()));

        this.addElement(this.inventoryContentPanel = new OxygenGUIButtonPanel(this.screen, 6, 15, 128, 16, 1, 36, 4, GUISettings.get().getPanelTextScale(), false));
        this.loadInventoryContent();

        this.inventoryContentPanel.<InventoryItemGUIButton>setClickListener((previous, clicked, mouseX, mouseY, mouseButton)->this.section.itemSelected(clicked));

        this.addElement(this.closeButton = new OxygenGUIButton(this.getWidth() - 55, this.getHeight() - 12, 40, 10, ClientReference.localize("oxygen.gui.closeButton")));
    }

    public void loadInventoryContent() {
        if (this.inventoryContentPanel != null) {
            this.inventoryContentPanel.reset();
            Set<String> added = new HashSet<>();
            String key;
            for (ItemStackWrapper stackWrapper : this.screen.inventoryContent.keySet()) {
                key = getKey(stackWrapper);
                if (!added.contains(key)) {
                    this.inventoryContentPanel.addButton(new InventoryItemGUIButton(stackWrapper, this.screen.getEqualStackAmount(stackWrapper)));
                    added.add(key);
                }              
            }

            this.inventoryContentPanel.getScroller().resetPosition();
            this.inventoryContentPanel.getScroller().getSlider().reset();

            this.inventoryContentPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.screen.inventoryContent.size(), 9, 36));
        }
    }

    private static String getKey(ItemStackWrapper stackWrapper) {
        return String.valueOf(stackWrapper.itemId) + "_" + String.valueOf(stackWrapper.damage) + "_" + stackWrapper.stackNBTStr + "_" + stackWrapper.capNBTStr;
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) 
            if (element == this.closeButton)
                this.close();
    }
}
