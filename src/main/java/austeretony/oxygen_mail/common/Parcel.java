package austeretony.oxygen_mail.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public class Parcel {

    public final ItemStackWrapper stackWrapper;

    public final int amount;

    private Parcel(ItemStackWrapper stackWrapper, int amount) {
        this.stackWrapper = stackWrapper;
        this.amount = amount;
    }

    public static Parcel create(ItemStack itemStack, int amount) {
        return new Parcel(ItemStackWrapper.getFromStack(itemStack), amount);
    }

    public static Parcel create(ItemStackWrapper stackWrapper, int amount) {
        return new Parcel(stackWrapper, amount);
    }

    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
    }

    public static Parcel read(BufferedInputStream bis) throws IOException {
        return new Parcel(ItemStackWrapper.read(bis), StreamUtils.readShort(bis));
    }

    public void write(ByteBuf buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
    }

    public static Parcel read(ByteBuf buffer) {
        return new Parcel(ItemStackWrapper.read(buffer), buffer.readShort());
    }
}
