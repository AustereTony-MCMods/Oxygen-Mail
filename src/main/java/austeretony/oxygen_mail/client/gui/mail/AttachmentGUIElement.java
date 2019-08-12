package austeretony.oxygen_mail.client.gui.mail;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.OxygenGUITextures;
import austeretony.oxygen.client.gui.settings.GUISettings;
import austeretony.oxygen_mail.common.main.Mail;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class AttachmentGUIElement extends GUISimpleElement<AttachmentGUIElement> {

    private ItemStack itemStack;

    private int mode, amount, currency;

    public AttachmentGUIElement(int xPosition, int yPosition) {             
        this.setPosition(xPosition, yPosition);   
        this.setSize(16, 16);         
    }

    @Override
    public void draw(int mouseX, int mouseY) {          
        if (this.isVisible()) { 
            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            float textScale = GUISettings.instance().getSubTextScale();

            if (this.mode == 0) {
                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(OxygenGUITextures.COIN_ICON);
                GUIAdvancedElement.drawCustomSizedTexturedRect(5, 4, 0, 0, 6, 6, 6, 6);          
                GlStateManager.disableBlend();

                GlStateManager.pushMatrix();           
                GlStateManager.translate(13.0F, 5.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.currency), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   
            } else if (this.mode == 1) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.amount), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   
            } else if (this.mode == 2) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(16.0F, 10.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.amount), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   

                GlStateManager.pushMatrix();           
                GlStateManager.translate(35.0F, 2.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(ClientReference.localize("oxygen_mail.gui.mail.attachment.cost"), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();  

                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(OxygenGUITextures.COIN_ICON);
                GUIAdvancedElement.drawCustomSizedTexturedRect(34, 9, 0, 0, 6, 6, 6, 6);          
                GlStateManager.disableBlend();

                GlStateManager.pushMatrix();           
                GlStateManager.translate(42.0F, 10.0F, 0.0F);            
                GlStateManager.scale(textScale, textScale, 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.currency), 0, 0, this.isEnabled() ? this.getEnabledTextColor() : 0xFFCC0000, false); 
                GlStateManager.popMatrix();   
            }

            GlStateManager.popMatrix();

            if (this.mode == 1 || this.mode == 2) {
                RenderHelper.enableGUIStandardItemLighting();            
                GlStateManager.enableDepth();
                this.itemRender.renderItemAndEffectIntoGUI(this.itemStack, this.getX(), this.getY());                              
                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();
            }
        }
    }

    public void setAttachment(Mail message) {
        this.mode = - 1; 
        this.itemStack = null;
        switch (message.type) {
        case SERVICE_REMITTANCE:
        case REMITTANCE:
            this.currency = message.getCurrency();
            this.mode = 0; 
            break;
        case SERVICE_PACKAGE:
        case PACKAGE:
            this.itemStack = message.getParcel().stackWrapper.getItemStack();
            this.amount = message.getParcel().amount;
            this.mode = 1; 
            break;
        case PACKAGE_WITH_COD:
            this.currency = message.getCurrency();
            this.itemStack = message.getParcel().stackWrapper.getItemStack();
            this.amount = message.getParcel().amount;
            this.mode = 2; 
            break;
        default:
            break;  
        }
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (this.isEnabled() && this.itemStack != null && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.itemStack, mouseX, mouseY);
    }
}
