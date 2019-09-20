package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.Parcel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPAttachmentReceived extends Packet {

    private Parcel parcel;

    private long oldMessageId, balance;

    public CPAttachmentReceived() {}

    public CPAttachmentReceived(long oldMessageId, Parcel parcel, long balance) {
        this.oldMessageId = oldMessageId;
        this.parcel = parcel;
        this.balance = balance;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeLong(this.oldMessageId);
        buffer.writeBoolean(this.parcel != null);
        if (this.parcel != null)
            this.parcel.write(buffer);
        buffer.writeLong(this.balance);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final long oldMessageId = buffer.readLong();
        final Parcel parcel = buffer.readBoolean() ? Parcel.read(buffer) : null;
        final long balance = buffer.readLong();
        OxygenHelperClient.addRoutineTask(()->MailManagerClient.instance().getMailboxManager().attachmentReceived(oldMessageId, parcel, balance));
    }
}
