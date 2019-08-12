package austeretony.oxygen_mail.client.gui.mail.incoming;

import austeretony.oxygen.client.gui.IndexedGUIButton;
import austeretony.oxygen_mail.client.gui.mail.MailMenuGUIScreen;
import austeretony.oxygen_mail.common.main.Mail;
import net.minecraft.client.renderer.GlStateManager;

public class IncomingMessageGUIButton extends IndexedGUIButton<Long> {

    private boolean isPending, hasAttachment;

    public IncomingMessageGUIButton(Mail message) {
        super(message.getId());
        this.isPending = message.isPending();
        this.hasAttachment = message.hasAttachment();
        this.setDisplayText(message.subject);
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
        textY = (this.getHeight() - this.textHeight(this.getTextScale())) / 2, 
        iconU = 0;

        if (!this.isEnabled()) {                 
            color = this.getDisabledBackgroundColor();
            textColor = this.getDisabledTextColor();   
            iconU = 5;
        } else if (this.isHovered() || this.isToggled()) {                 
            color = this.getHoveredBackgroundColor();
            textColor = this.getHoveredTextColor();
            iconU = 10;
        }

        drawRect(0, 0, this.getWidth(), this.getHeight(), color);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(9.0F, textY, 0.0F); 
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F); 
        this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, textColor, this.isTextShadowEnabled());
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend(); 
        if (this.isPending) {
            this.mc.getTextureManager().bindTexture(MailMenuGUIScreen.EXCLAMATION_MARK_ICONS_SMALL);                        
            drawCustomSizedTexturedRect(2, 3, iconU, 0, 5, 5, 15, 5);      
        } else if (this.hasAttachment) {
            this.mc.getTextureManager().bindTexture(MailMenuGUIScreen.HOLLOW_RHOMBUS_ICONS_SMALL);                        
            drawCustomSizedTexturedRect(2, 3, iconU, 0, 5, 5, 15, 5);      
        }     
        GlStateManager.disableBlend(); 

        GlStateManager.popMatrix();
    }
}