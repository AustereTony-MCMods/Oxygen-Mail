package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class AttachmentParcel implements Attachment {

    public final ItemStackWrapper stackWrapper;

    public final int amount;

    public AttachmentParcel(ItemStackWrapper stackWrapper, int amount) {
        this.stackWrapper = stackWrapper;
        this.amount = amount;
    }

    @Override
    public int getItemAmount() {
        return this.amount;
    }

    @Override
    public long getCurrencyValue() {
        return 0L;
    }

    @Override
    public int getCurrencyIndex() {
        return 0;
    }

    @Override
    public ItemStackWrapper getStackWrapper() {
        return this.stackWrapper;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentParcel(ItemStackWrapper.read(bis), StreamUtils.readShort(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentParcel(ItemStackWrapper.read(buffer), buffer.readShort());
    }

    @Override
    public String toString() {
        return String.format("[stack wrapper: %s, amount: %d]",
                this.stackWrapper, 
                this.amount);
    }
}
