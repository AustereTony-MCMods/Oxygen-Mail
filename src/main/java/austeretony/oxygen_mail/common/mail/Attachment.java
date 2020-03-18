package austeretony.oxygen_mail.common.mail;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import io.netty.buffer.ByteBuf;

public interface Attachment {

    int getItemAmount();

    long getCurrencyValue();

    int getCurrencyIndex();

    @Nullable
    ItemStackWrapper getStackWrapper();

    void write(BufferedOutputStream bos) throws IOException;

    void write(ByteBuf buffer);
}
