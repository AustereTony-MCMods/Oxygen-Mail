package austeretony.oxygen_mail.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.EnumMail;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPSendMessage extends Packet {

    private String addressee, subject, message;

    private int ordinal;

    private long currency;

    private Parcel parcel;

    public SPSendMessage() {}

    public SPSendMessage(EnumMail type, String addressee, String subject, String message, long currency, Parcel parcel) {
        this.ordinal = type.ordinal();
        this.addressee = addressee;
        this.subject = subject;
        this.message = message;
        this.currency = currency;
        this.parcel = parcel;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        ByteBufUtils.writeString(this.addressee, buffer);
        ByteBufUtils.writeString(this.subject, buffer);
        ByteBufUtils.writeString(this.message, buffer);
        buffer.writeByte(this.ordinal); 
        buffer.writeLong(this.currency);
        buffer.writeBoolean(this.parcel != null);
        if (this.parcel != null)
            this.parcel.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), MailMain.MESSAGE_OPERATION_REQUEST_ID)) {
            final String 
            addressee = ByteBufUtils.readString(buffer),
            subject = ByteBufUtils.readString(buffer),
            message = ByteBufUtils.readString(buffer);
            final int ordinal = buffer.readByte();
            final long currency = buffer.readLong();
            final Parcel parcel = buffer.readBoolean() ? Parcel.read(buffer) : null;
            if (ordinal >= 0 && ordinal < EnumMail.values().length)
                OxygenHelperServer.addRoutineTask(()->MailManagerServer.instance().getMailboxesManager()
                        .sendMail(playerMP, EnumMail.values()[ordinal], addressee, subject, message, currency, parcel));
        }
    }
}
