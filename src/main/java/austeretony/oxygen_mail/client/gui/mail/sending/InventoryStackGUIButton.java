package austeretony.oxygen_mail.client.gui.mail.sending;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.gui.IndexedGUIButton;
import austeretony.oxygen.common.itemstack.InventoryHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class InventoryStackGUIButton extends IndexedGUIButton<ItemStack> {

    private String stock;

    public InventoryStackGUIButton(ItemStack itemStack) {
        super(itemStack);
        this.stock = String.valueOf(InventoryHelper.getEqualStackAmount(ClientReference.getClientPlayer(), itemStack));
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();           
        GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
        GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        int color;                      
        if (!this.isEnabled())                  
            color = this.getDisabledBackgroundColor();
        else if (this.isHovered() || this.isToggled())                  
            color = this.getHoveredBackgroundColor();
        else                    
            color = this.getEnabledBackgroundColor();                                   
        drawRect(0, 0, this.getWidth(), this.getHeight(), color);

        if (!this.isEnabled())                  
            color = this.getDisabledTextColor();           
        else if (this.isHovered() || this.isToggled())                                          
            color = this.getHoveredTextColor();
        else                    
            color = this.getEnabledTextColor();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        GlStateManager.pushMatrix();           
        GlStateManager.translate(20.0F, 10.0F, 0.0F);            
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
        this.mc.fontRenderer.drawString(this.stock, 0, 0, color, this.isTextShadowEnabled()); 
        GlStateManager.popMatrix();   

        //TODO stack name very long, need some trick to display it
        /*GlStateManager.pushMatrix();       
        GlStateManager.translate(22.0F, (this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
        GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);           
        this.mc.fontRenderer.drawString(this.getDisplayText(), 0, 0, color, this.isTextShadowEnabled());
        GlStateManager.popMatrix();*/

        GlStateManager.popMatrix();

        RenderHelper.enableGUIStandardItemLighting();            
        GlStateManager.enableDepth();
        this.itemRender.renderItemAndEffectIntoGUI(this.index, this.getX() + 2, this.getY());                              
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + 24 && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.index, 
                    mouseX - this.textWidth(this.index.getDisplayName(), 1.0F) - (ClientReference.getGameSettings().advancedItemTooltips ? 75 : 25), mouseY);
    }
}