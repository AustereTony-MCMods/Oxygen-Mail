package austeretony.oxygen_mail.client.gui.mail.incoming;

import austeretony.alternateui.util.EnumGUIAlignment;
import austeretony.oxygen_core.client.gui.IndexedGUIButton;
import austeretony.oxygen_core.client.gui.elements.CustomRectUtils;
import austeretony.oxygen_core.client.gui.settings.GUISettings;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.Mail;
import net.minecraft.client.renderer.GlStateManager;

public class MessageGUIButton extends IndexedGUIButton<Long> {

    private boolean pending;

    public MessageGUIButton(Mail message) {
        super(message.getId());
        this.pending = message.isPending();
        this.setStaticBackgroundColor(GUISettings.get().getStatusElementColor());
        this.setDynamicBackgroundColor(GUISettings.get().getEnabledElementColor(), GUISettings.get().getDisabledElementColor(), GUISettings.get().getHoveredElementColor());
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColor(), GUISettings.get().getDisabledTextColor(), GUISettings.get().getHoveredTextColor());
        this.setDisplayText(message.getLocalizedSubject());
        if (MailManagerClient.instance().getMailboxContainer().isMarkedAsRead(message.getId()))
            this.read();
    }

    public void read() {
        this.setTextDynamicColor(GUISettings.get().getEnabledTextColorDark(), GUISettings.get().getEnabledTextColorDark(), GUISettings.get().getEnabledTextColorDark());
    }
    
    public void setPending(boolean flag) {
        this.pending = flag;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();           
        GlStateManager.translate(this.getX(), this.getY(), 0.0F);    
        GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        int 
        color = this.getEnabledBackgroundColor(), 
        textColor = this.getEnabledTextColor(), 
        textY = (this.getHeight() - this.textHeight(this.getTextScale())) / 2;

        if (!this.isEnabled()) {                 
            color = this.getDisabledBackgroundColor();
            textColor = this.getDisabledTextColor();   
        } else if (this.isHovered() || this.isToggled()) {                 
            color = this.getHoveredBackgroundColor();
            textColor = this.getHoveredTextColor();
        }

        int third = this.getWidth() / 3;
        CustomRectUtils.drawGradientRect(0.0D, 0.0D, third, this.getHeight(), 0x00000000, color, EnumGUIAlignment.RIGHT);
        drawRect(third, 0, this.getWidth() - third, this.getHeight(), color);
        CustomRectUtils.drawGradientRect(this.getWidth() - third, 0.0D, this.getWidth(), this.getHeight(), 0x00000000, color, EnumGUIAlignment.LEFT);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(0.0F, textY, 0.0F); 
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
        if (this.pending)
            this.mc.fontRenderer.drawString("!", 0, 0, this.getStaticBackgroundColor(), false);
        this.mc.fontRenderer.drawString(this.getDisplayText(), 6, 0, textColor, false);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
