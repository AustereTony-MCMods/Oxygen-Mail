package austeretony.oxygen_mail.common.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen.common.itemstack.ItemStackWrapper;
import austeretony.oxygen.util.StreamUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class Parcel {

    public final ItemStackWrapper stackWrapper;

    public final int amount;

    public Parcel(ItemStackWrapper stackWrapper, int amount) {
        this.stackWrapper = stackWrapper;
        this.amount = amount;
    }

    public static Parcel create(ItemStack itemStack, int amount) {
        return new Parcel(ItemStackWrapper.getFromStack(itemStack), amount);
    }

    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
    }

    public static Parcel read(BufferedInputStream bis) throws IOException {
        return new Parcel(ItemStackWrapper.read(bis), StreamUtils.readShort(bis));
    }

    public void write(PacketBuffer buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
    }

    public static Parcel read(PacketBuffer buffer) {
        return new Parcel(ItemStackWrapper.read(buffer), buffer.readShort());
    }
}
