package austeretony.oxygen_mail.common.mail;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import io.netty.buffer.ByteBuf;

public class AttachmentDummy implements Attachment {

    @Override
    public int getItemAmount() {
        return 0;
    }

    @Override
    public long getCurrencyValue() {
        return 0L;
    }

    @Override
    public int getCurrencyIndex() {
        return 0;
    }

    @Nullable
    @Override
    public ItemStackWrapper getStackWrapper() {
        return null;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {}

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public String toString() {
        return "[dummy attachment]";
    }
}
