package austeretony.oxygen_mail.common.network.server;

import austeretony.oxygen.common.network.ProxyPacket;
import austeretony.oxygen_mail.common.EnumMessageOperation;
import austeretony.oxygen_mail.common.MailManagerServer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class SPMessageOperation extends ProxyPacket {

    private long messageId;

    private int operation;

    public SPMessageOperation() {}

    public SPMessageOperation(long messageId, EnumMessageOperation operation) {
        this.messageId = messageId;
        this.operation = operation.ordinal();
    }

    @Override
    public void write(PacketBuffer buffer, INetHandler netHandler) {
        buffer.writeLong(this.messageId);
        buffer.writeByte(this.operation);
    }

    @Override
    public void read(PacketBuffer buffer, INetHandler netHandler) {
        MailManagerServer.instance().processMessageOperation(getEntityPlayerMP(netHandler), buffer.readLong(), EnumMessageOperation.values()[buffer.readByte()]);
    }
}