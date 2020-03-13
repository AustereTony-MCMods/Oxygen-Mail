package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class AttachmentRemittance implements Attachment {

    private final int currencyIndex;

    private final long amount;

    public AttachmentRemittance(int currencyIndex, long amount) {
        this.currencyIndex = currencyIndex;
        this.amount = amount;
    }

    @Override
    public int getItemAmount() {
        return 0;
    }

    @Override
    public long getCurrencyValue() {
        return this.amount;
    }

    @Override
    public int getCurrencyIndex() {
        return this.currencyIndex;
    }

    @Nullable
    @Override
    public ItemStackWrapper getStackWrapper() {
        return null;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((byte) this.currencyIndex, bos);
        StreamUtils.write(this.amount, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentRemittance(StreamUtils.readByte(bis), StreamUtils.readLong(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeByte(this.currencyIndex);
        buffer.writeLong(this.amount);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentRemittance(buffer.readByte(), buffer.readLong());
    }

    @Override
    public String toString() {
        return String.format("[currency index: %d, value: %d]",
                this.currencyIndex, 
                this.amount);
    }
}
