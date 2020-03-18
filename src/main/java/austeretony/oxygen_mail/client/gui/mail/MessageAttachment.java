package austeretony.oxygen_mail.client.gui.mail;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class MessageAttachment extends GUISimpleElement<MessageAttachment> {

    private ItemStack itemStack;

    private int mode, amount;

    private long value;

    private String amountStr, valueStr, costLabelStr;

    private CurrencyProperties currencyProperties;

    private final boolean enableDurabilityBar;

    public MessageAttachment(int xPosition, int yPosition) {             
        this.setPosition(xPosition, yPosition);   
        this.setSize(16, 16);       

        this.costLabelStr = ClientReference.localize("oxygen_mail.gui.mail.attachment.cost");

        this.enableDurabilityBar = EnumBaseClientSetting.ENABLE_ITEMS_DURABILITY_BAR.get().asBoolean();
        this.setTextScale(EnumBaseGUISetting.TEXT_SUB_SCALE.get().asFloat() - 0.05F);
        this.setEnabledTextColor(EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt());
        this.setStaticBackgroundColor(EnumBaseGUISetting.INACTIVE_TEXT_COLOR.get().asInt()); 
        this.enableFull();
    }

    @Override
    public void draw(int mouseX, int mouseY) {          
        if (this.isVisible()) { 
            if (this.mode == 1 || this.mode == 2) {
                RenderHelper.enableGUIStandardItemLighting();            
                GlStateManager.enableDepth();
                this.itemRender.renderItemAndEffectIntoGUI(this.itemStack, this.getX(), this.getY());   

                if (this.enableDurabilityBar) {
                    FontRenderer font = this.itemStack.getItem().getFontRenderer(this.itemStack);
                    if (font == null) 
                        font = this.mc.fontRenderer;
                    this.itemRender.renderItemOverlayIntoGUI(font, this.itemStack, this.getX(), this.getY(), null);
                }

                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();
            }

            GlStateManager.pushMatrix();           
            GlStateManager.translate(this.getX(), this.getY(), 0.0F);            
            GlStateManager.scale(this.getScale(), this.getScale(), 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

            if (this.mode == 0) {
                this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
                GUIAdvancedElement.drawCustomSizedTexturedRect(this.currencyProperties.getXOffset(), (this.getHeight() - this.currencyProperties.getIconHeight()) / 2 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());            
                GlStateManager.disableBlend();

                GlStateManager.pushMatrix();           
                GlStateManager.translate(10.0F, ((float) this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.valueStr), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   
            } else if (this.mode == 1) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(14.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.amountStr), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   
            } else if (this.mode == 2) {
                GlStateManager.pushMatrix();           
                GlStateManager.translate(14.0F, 10.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.amountStr), 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();   

                GlStateManager.pushMatrix();           
                GlStateManager.translate(30.0F, 2.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(this.costLabelStr, 0, 0, this.getEnabledTextColor(), false); 
                GlStateManager.popMatrix();  

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

                GlStateManager.enableBlend(); 
                this.mc.getTextureManager().bindTexture(this.currencyProperties.getIcon());
                GUIAdvancedElement.drawCustomSizedTexturedRect(30 + this.currencyProperties.getXOffset(), 4 + (this.getHeight() - this.currencyProperties.getIconHeight()) / 2 + this.currencyProperties.getYOffset(), 0, 0, this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight(), this.currencyProperties.getIconWidth(), this.currencyProperties.getIconHeight());            
                GlStateManager.disableBlend();

                GlStateManager.pushMatrix();           
                GlStateManager.translate(41.0F, 4.0F + ((float) this.getHeight() - this.textHeight(this.getTextScale())) / 2.0F, 0.0F);            
                GlStateManager.scale(this.getTextScale(), this.getTextScale(), 0.0F);   
                this.mc.fontRenderer.drawString(String.valueOf(this.valueStr), 0, 0, this.isEnabled() ? this.getEnabledTextColor() : this.getStaticBackgroundColor(), false); 
                GlStateManager.popMatrix();   
            }

            GlStateManager.popMatrix();
        }
    }

    public void load(EnumMail type, Attachment attachment) {
        this.mode = - 1; 
        this.itemStack = null;

        this.currencyProperties = OxygenHelperClient.getCurrencyProperties(attachment.getCurrencyIndex());
        switch (type) {
        case REMITTANCE:
            this.value = attachment.getCurrencyValue();
            this.valueStr = OxygenUtils.formatCurrencyValue(String.valueOf(this.value));
            this.mode = 0; 
            break;
        case PARCEL:
            this.itemStack = attachment.getStackWrapper().getCachedItemStack();
            this.amount = attachment.getItemAmount();
            this.amountStr = String.valueOf(this.amount);
            this.mode = 1; 
            break;
        case COD:
            this.itemStack = attachment.getStackWrapper().getCachedItemStack();
            this.amount = attachment.getItemAmount();
            this.amountStr = String.valueOf(this.amount);
            this.value = attachment.getCurrencyValue();
            this.valueStr = OxygenUtils.formatCurrencyValue(String.valueOf(this.value));
            this.mode = 2; 
            break;
        default:
            break;  
        }
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY) {
        if (this.isEnabled() && this.itemStack != null && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight())
            this.screen.drawToolTip(this.itemStack, mouseX + 6, mouseY);
    }
}
