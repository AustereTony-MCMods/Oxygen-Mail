package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.Parcel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPMessageSent extends Packet {

    private Parcel parcel;

    private long balance;

    public CPMessageSent() {}

    public CPMessageSent(Parcel parcel, long balance) {
        this.parcel = parcel;
        this.balance = balance;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeBoolean(this.parcel != null);
        if (this.parcel != null)
            this.parcel.write(buffer);
        buffer.writeLong(this.balance);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final Parcel parcel = buffer.readBoolean() ? Parcel.read(buffer) : null;
        final long balance = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->MailManagerClient.instance().getMailboxManager().messageSent(parcel, balance));
    }
}
