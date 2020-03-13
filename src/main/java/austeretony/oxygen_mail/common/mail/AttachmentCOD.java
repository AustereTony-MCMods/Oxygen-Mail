package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class AttachmentCOD implements Attachment {

    public final ItemStackWrapper stackWrapper;

    public final int amount, currencyIndex;

    private final long price;

    public AttachmentCOD(ItemStackWrapper stackWrapper, int itemAmount, int currencyIndex, long currencyAmount) {
        this.stackWrapper = stackWrapper;
        this.amount = itemAmount;
        this.currencyIndex = currencyIndex;
        this.price = currencyAmount;
    }

    @Override
    public int getItemAmount() {
        return this.amount;
    }

    @Override
    public long getCurrencyValue() {
        return this.price;
    }

    @Override
    public int getCurrencyIndex() {
        return this.currencyIndex;
    }

    @Override
    public ItemStackWrapper getStackWrapper() {
        return this.stackWrapper;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write((byte) this.currencyIndex, bos);
        StreamUtils.write(this.price, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentCOD(ItemStackWrapper.read(bis), StreamUtils.readShort(bis), StreamUtils.readByte(bis), StreamUtils.readLong(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeByte(this.currencyIndex);
        buffer.writeLong(this.price);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentCOD(ItemStackWrapper.read(buffer), buffer.readShort(), buffer.readByte(), buffer.readLong());
    }

    @Override
    public String toString() {
        return String.format("[stack wrapper: %s, amount: %d, currency index: %d, price: %d]",
                this.stackWrapper, 
                this.amount,
                this.currencyIndex, 
                this.price);
    }
}
