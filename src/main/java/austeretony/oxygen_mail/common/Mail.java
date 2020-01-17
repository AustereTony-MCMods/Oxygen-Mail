package austeretony.oxygen_mail.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import io.netty.buffer.ByteBuf;

public class Mail implements PersistentEntry, SynchronousEntry {

    public static final int 
    MESSAGE_SUBJECT_MAX_LENGTH = 24,
    MESSAGE_MAX_LENGTH = 800;

    public static final UUID SYSTEM_UUID = UUID.fromString("d10d07f6-ae3c-4ec6-a055-1160c4cf848a");

    private long messageId;

    private EnumMail type;

    private UUID senderUUID;

    private String senderName, subject, message;

    private long currency;

    private Parcel parcel;

    private boolean pending;

    public Mail() {}

    public Mail(long messageId, EnumMail type, UUID senderUUID, String senderName, String subject, String message, long currency, Parcel parcel) {
        this.messageId = messageId;
        this.type = type;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.subject = subject;
        this.message = message;
        this.currency = currency;
        this.parcel = parcel;
        if (currency > 0L || parcel != null)
            this.pending = true;
    }

    @Override
    public long getId() {              
        return this.messageId;
    }

    public void setId(long messageId) {
        this.messageId = messageId;
    }

    public EnumMail getType() {
        return this.type;
    }

    public UUID getSenderUUID() {
        return this.senderUUID;
    }

    public String getSenderUsername() {
        return this.senderName;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getMessage() {
        return this.message;
    }

    public long getCurrency() {
        return this.currency;
    }

    public Parcel getParcel() {
        return this.parcel;
    }

    public boolean isPending() {
        return this.pending;
    }  

    public void setPending(boolean flag) {
        this.pending = flag;
        if (!flag) {
            this.currency = 0L;
            this.parcel = null;
        }
    }

    public boolean isExpired() {
        int expiresInHours = - 1;
        switch (this.type) {
        case SYSTEM_LETTER:
            expiresInHours = MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case LETTER:
            expiresInHours = MailConfig.LETTER_EXPIRE_TIME_HOURS.asInt();
            break;
        case SYSTEM_REMITTANCE:
            expiresInHours = MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case REMITTANCE:
            expiresInHours = MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.asInt();
            break;
        case SYSTEM_PACKAGE:
            expiresInHours = MailConfig.SYSTEM_PACKAGE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PACKAGE:
            expiresInHours = MailConfig.PACKAGE_EXPIRE_TIME_HOURS.asInt();
            break;
        case PACKAGE_WITH_COD:
            expiresInHours = MailConfig.PACKAGE_WITH_COD_EXPIRE_TIME_HOURS.asInt();
            break;  
        }
        if (expiresInHours < 0)
            return false;
        return System.currentTimeMillis() - this.messageId > expiresInHours * 3_600_000L;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.messageId, bos);
        StreamUtils.write((byte) this.type.ordinal(), bos);
        StreamUtils.write(this.senderUUID, bos);
        StreamUtils.write(this.senderName, bos);
        StreamUtils.write(this.subject, bos);
        StreamUtils.write(this.message, bos);
        StreamUtils.write(this.currency, bos);
        StreamUtils.write(this.parcel == null ? false : true, bos);
        if (this.parcel != null)
            this.parcel.write(bos);
        StreamUtils.write(this.pending, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {        
        this.messageId = StreamUtils.readLong(bis);
        this.type = EnumMail.values()[bis.read()]; 
        this.senderUUID = StreamUtils.readUUID(bis);
        this.senderName = StreamUtils.readString(bis);
        this.subject = StreamUtils.readString(bis);
        this.message = StreamUtils.readString(bis);
        this.currency = StreamUtils.readLong(bis);
        if (StreamUtils.readBoolean(bis))
            this.parcel = Parcel.read(bis);
        this.pending = StreamUtils.readBoolean(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.messageId);
        buffer.writeByte(this.type.ordinal());
        ByteBufUtils.writeUUID(this.senderUUID, buffer);
        ByteBufUtils.writeString(this.senderName, buffer);
        ByteBufUtils.writeString(this.subject, buffer);
        ByteBufUtils.writeString(this.message, buffer);
        buffer.writeLong(this.currency);
        buffer.writeBoolean(this.parcel == null ? false : true);
        if (this.parcel != null)
            this.parcel.write(buffer);
        buffer.writeBoolean(this.pending);
    }

    @Override
    public void read(ByteBuf buffer) {
        this.messageId = buffer.readLong();
        this.type = EnumMail.values()[buffer.readByte()]; 
        this.senderUUID = ByteBufUtils.readUUID(buffer);
        this.senderName = ByteBufUtils.readString(buffer);
        this.subject = ByteBufUtils.readString(buffer);
        this.message = ByteBufUtils.readString(buffer);
        this.currency = buffer.readLong();
        if (buffer.readBoolean())
            this.parcel = Parcel.read(buffer);
        this.pending = buffer.readBoolean();
    }
}
