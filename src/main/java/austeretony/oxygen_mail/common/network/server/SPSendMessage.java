package austeretony.oxygen_mail.common.network.server;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.server.MailManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

public class SPSendMessage extends Packet {

    private String addresseeUsername, subject, message;

    private int ordinal;

    private Attachment attachment;

    public SPSendMessage() {}

    public SPSendMessage(String addresseeUsername, EnumMail type, String subject, String message, Attachment attachment) {
        this.addresseeUsername = addresseeUsername;
        this.ordinal = type.ordinal();
        this.subject = subject;
        this.message = message;
        this.attachment = attachment;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        buffer.writeByte(this.ordinal); 
        ByteBufUtils.writeString(this.addresseeUsername, buffer);
        ByteBufUtils.writeString(this.subject, buffer);
        ByteBufUtils.writeString(this.message, buffer);
        this.attachment.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final EntityPlayerMP playerMP = getEntityPlayerMP(netHandler);
        if (OxygenHelperServer.isNetworkRequestAvailable(CommonReference.getPersistentUUID(playerMP), MailMain.MESSAGE_OPERATION_REQUEST_ID)) {
            final int ordinal = buffer.readByte();
            final String 
            addresseeUsername = ByteBufUtils.readString(buffer),
            subject = ByteBufUtils.readString(buffer),
            message = ByteBufUtils.readString(buffer);
            if (ordinal >= 0 && ordinal < EnumMail.values().length) {
                final EnumMail type = EnumMail.values()[ordinal];
                final Attachment attachment = type.readAttachment(buffer);
                OxygenHelperServer.addRoutineTask(()->MailManagerServer.instance().getMailboxesManager().sendMail(playerMP, addresseeUsername, type, subject, message, attachment));
            }
        }
    }
}
