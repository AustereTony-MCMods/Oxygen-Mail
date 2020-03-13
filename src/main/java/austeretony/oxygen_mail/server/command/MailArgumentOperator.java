package austeretony.oxygen_mail.server.command;

import java.util.UUID;

import austeretony.oxygen_core.common.PlayerSharedData;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.server.api.MailHelperServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class MailArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        //Usage: /oxygens mail -send <target> <mail type> <subject> <message> <args...>
        //Text specified in brackets ({}): subject MUST present, message may be empty (use empty brackets - {}).
        //
        //Letter - /oxygens mail -send @a 0 {Letter} {Some dummy message to yourself.}
        //Remittance - /oxygens mail -send AustereTony 1 {Remittance} {1000 coins for you!} 0 10000
        //Package - /oxygens mail -send -all-online 2 {Gift} {Some dummy message for anyone ONLINE.} 10 minecraft:diamond (if last argument is absent, held item will be used) 
        //property '-all' allows to send message to EVERY player ever logged in.

        if (args.length >= 3) {
            if (args[1].equals("-send")) {
                EntityPlayerMP senderPlayerMP = null;
                if (sender instanceof EntityPlayerMP)
                    senderPlayerMP = CommandBase.getCommandSenderAsPlayer(sender);
                int messageType = CommandBase.parseInt(args[3], 0, 2);

                StringBuilder builder = new StringBuilder();
                String word, subject, message;
                //subject
                int index;
                for (index = 4; index < args.length; index++) {
                    word = args[index];
                    if (index == 4) {
                        if (!word.startsWith("{"))
                            throw new WrongUsageException("Invalid subject!");
                        else
                            word = word.substring(1);
                    }
                    if (word.endsWith("}")) {
                        word = word.substring(0, word.length() - 1);
                        builder.append(word);
                        break;
                    } else
                        builder.append(word).append(' ');
                }
                subject = builder.toString();
                if (subject.isEmpty())
                    throw new WrongUsageException("Empty subject!");

                //message
                builder.delete(0, builder.length());
                boolean first = true;
                index++;
                for (; index < args.length; index++) {
                    word = args[index];
                    if (first) {
                        if (!word.startsWith("{"))
                            throw new WrongUsageException("Invalid message!");
                        else
                            word = word.substring(1);
                        first = false;
                    }
                    if (word.endsWith("}")) {
                        word = word.substring(0, word.length() - 1);
                        builder.append(word);
                        break;
                    } else
                        builder.append(word).append(' ');
                }
                message = builder.toString();

                if (args[2].equals("-all-online")) {  
                    boolean notified = false;
                    for (UUID playerUUID : OxygenHelperServer.getOnlinePlayersUUIDs()) {
                        switch (messageType) {
                        case 0:
                            MailHelperServer.sendSystemMail(
                                    playerUUID, 
                                    Mail.SYSTEM_SENDER, 
                                    EnumMail.LETTER,
                                    subject, 
                                    Attachments.dummy(),
                                    true,
                                    message);

                            if (!notified) {
                                notified = true;
                                if (sender instanceof EntityPlayerMP)
                                    senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                            "ALL-ONLINE",
                                            subject,
                                            message)); 
                                else
                                    server.sendMessage(new TextComponentString(String.format("Letter sent to <all-online> - subject: %s, message: %s", 
                                            subject,
                                            message)));
                            }
                            break;
                        case 1:
                            index++;
                            if (index < args.length) {
                                int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                                if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                    throw new WrongUsageException("Invalid currency index: %s", currencyIndex);

                                long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);                                
                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        Mail.SYSTEM_SENDER, 
                                        EnumMail.REMITTANCE,
                                        subject, 
                                        Attachments.remittance(currencyIndex, value),
                                        true,
                                        message);

                                if (!notified) {
                                    notified = true;
                                    if (sender instanceof EntityPlayerMP)
                                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                                "ALL-ONLINE",
                                                subject,
                                                message,
                                                value)); 
                                    else
                                        server.sendMessage(new TextComponentString(String.format("Remittance sent to <all-online> - subject: %s, message: %s, value: %s", 
                                                subject,
                                                message,
                                                value)));
                                }
                            } else
                                throw new WrongUsageException("Invalid remittance value!");
                            break;
                        case 2:
                            index++;
                            if (index < args.length) {
                                int amount = CommandBase.parseInt(args[index++], 0, 1000);
                                ItemStack itemStack;
                                if (index < args.length)
                                    itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                                else {
                                    if (sender instanceof MinecraftServer)
                                        throw new WrongUsageException("Invalid item registry name!");
                                    if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                        itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                    else
                                        throw new WrongUsageException("Main hand is empty!");
                                }

                                MailHelperServer.sendSystemMail(
                                        playerUUID, 
                                        Mail.SYSTEM_SENDER, 
                                        EnumMail.PARCEL,
                                        subject, 
                                        Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                        true,
                                        message);

                                if (!notified) {
                                    notified = true;
                                    if (sender instanceof EntityPlayerMP)
                                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.packageSent", 
                                                "ALL-ONLINE",
                                                subject,
                                                message,
                                                amount,
                                                itemStack.getDisplayName())); 
                                    else
                                        server.sendMessage(new TextComponentString(String.format("Package sent to <all-online> - subject: %s, message: %s, amount: %s, item: %s", 
                                                subject,
                                                message,
                                                amount,
                                                itemStack.getDisplayName())));
                                }
                            } else
                                throw new WrongUsageException("Invalid items amount!");
                            break;
                        }
                    }
                } else if (args[2].equals("-all")) {
                    boolean notified = false;
                    for (PlayerSharedData sharedData : OxygenManagerServer.instance().getSharedDataManager().getPlayersSharedData()) {
                        switch (messageType) {
                        case 0:                            
                            MailHelperServer.sendSystemMail(
                                    sharedData.getPlayerUUID(), 
                                    Mail.SYSTEM_SENDER, 
                                    EnumMail.LETTER,
                                    subject, 
                                    Attachments.dummy(),
                                    true,
                                    message);

                            if (!notified) {
                                notified = true;
                                if (sender instanceof EntityPlayerMP)
                                    senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                            "ALL",
                                            subject,
                                            message)); 
                                else
                                    server.sendMessage(new TextComponentString(String.format("Letter sent to <all> - subject: %s, message: %s", 
                                            subject,
                                            message)));
                            }
                            break;
                        case 1:
                            index++;
                            if (index < args.length) {
                                int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                                if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                    throw new WrongUsageException("Invalid currency index: %s", currencyIndex);

                                long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);
                                MailHelperServer.sendSystemMail(
                                        sharedData.getPlayerUUID(), 
                                        Mail.SYSTEM_SENDER, 
                                        EnumMail.REMITTANCE,
                                        subject, 
                                        Attachments.remittance(currencyIndex, value),
                                        true,
                                        message);

                                if (!notified) {
                                    notified = true;
                                    if (sender instanceof EntityPlayerMP)
                                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                                "ALL",
                                                subject,
                                                message,
                                                value)); 
                                    else
                                        server.sendMessage(new TextComponentString(String.format("Remittance sent to <all> - subject: %s, message: %s, value: %s", 
                                                subject,
                                                message,
                                                value)));
                                }
                            } else
                                throw new WrongUsageException("Invalid remittance value!");
                            break;
                        case 2:
                            index++;
                            if (index < args.length) {
                                int amount = CommandBase.parseInt(args[index++], 0, 1000);
                                ItemStack itemStack;
                                if (index < args.length)
                                    itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                                else {
                                    if (sender instanceof MinecraftServer)
                                        throw new WrongUsageException("Invalid item registry name!");
                                    if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                        itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                    else
                                        throw new WrongUsageException("Main hand is empty!");
                                }

                                MailHelperServer.sendSystemMail(
                                        sharedData.getPlayerUUID(), 
                                        Mail.SYSTEM_SENDER, 
                                        EnumMail.PARCEL,
                                        subject, 
                                        Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                        true,
                                        message);

                                if (!notified) {
                                    notified = true;
                                    if (sender instanceof EntityPlayerMP)
                                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.packageSent", 
                                                "ALL",
                                                subject,
                                                message,
                                                amount,
                                                itemStack.getDisplayName())); 
                                    else
                                        server.sendMessage(new TextComponentString(String.format("Package sent to <all> - subject: %s, message: %s, amount: %s, item: %s", 
                                                subject,
                                                message,
                                                amount,
                                                itemStack.getDisplayName())));
                                }
                            } else
                                throw new WrongUsageException("Invalid items amount!");
                            break;
                        }
                    }
                } else {
                    EntityPlayerMP targetPlayerMP = null;
                    try {
                        targetPlayerMP = CommandBase.getPlayer(server, sender, args[2]);
                    } catch(PlayerNotFoundException exception) {}

                    PlayerSharedData sharedData;
                    if (targetPlayerMP != null)
                        sharedData = OxygenHelperServer.getPlayerSharedData(CommonReference.getPersistentUUID(targetPlayerMP));
                    else
                        sharedData = OxygenHelperServer.getPlayerSharedData(args[2]);

                    if (sharedData == null) return;

                    switch (messageType) {
                    case 0:                        
                        MailHelperServer.sendSystemMail(
                                sharedData.getPlayerUUID(), 
                                Mail.SYSTEM_SENDER, 
                                EnumMail.LETTER,
                                subject, 
                                Attachments.dummy(),
                                true,
                                message);

                        if (sender instanceof EntityPlayerMP)
                            senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.letterSent", 
                                    sharedData.getUsername(),
                                    subject,
                                    message)); 
                        else
                            server.sendMessage(new TextComponentString(String.format("Letter sent to <%s> - subject: %s, message: %s", 
                                    sharedData.getUsername(),
                                    subject,
                                    message)));
                        break;
                    case 1:
                        index++;
                        if (index < args.length) {
                            int currencyIndex = CommandBase.parseInt(args[index], 0, 127);
                            if (CurrencyHelperServer.getCurrencyProvider(currencyIndex) == null)
                                throw new WrongUsageException("Invalid currency index: %s", currencyIndex);

                            long value = CommandBase.parseLong(args[++index], 0, Long.MAX_VALUE);
                            MailHelperServer.sendSystemMail(
                                    sharedData.getPlayerUUID(), 
                                    Mail.SYSTEM_SENDER, 
                                    EnumMail.REMITTANCE,
                                    subject, 
                                    Attachments.remittance(currencyIndex, value),
                                    true,
                                    message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.remittanceSent", 
                                        sharedData.getUsername(),
                                        subject,
                                        message,
                                        value)); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Remittance sent to <%s> - subject: %s, message: %s, value: %s", 
                                        sharedData.getUsername(),
                                        subject,
                                        message,
                                        value)));
                        } else
                            throw new WrongUsageException("Invalid remittance value!");
                        break;
                    case 2:
                        index++;
                        if (index < args.length) {
                            int amount = CommandBase.parseInt(args[index++], 0, 1000);
                            ItemStack itemStack;
                            if (index < args.length)
                                itemStack = new ItemStack(CommandBase.getItemByText(sender, args[index]));
                            else {
                                if (sender instanceof MinecraftServer)
                                    throw new WrongUsageException("Invalid item registry name!");
                                if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                                    itemStack = senderPlayerMP.getHeldItemMainhand().copy();
                                else
                                    throw new WrongUsageException("Main hand is empty!");
                            }

                            MailHelperServer.sendSystemMail(
                                    sharedData.getPlayerUUID(), 
                                    Mail.SYSTEM_SENDER, 
                                    EnumMail.PARCEL,
                                    subject, 
                                    Attachments.parcel(ItemStackWrapper.of(itemStack), amount),
                                    true,
                                    message);

                            if (sender instanceof EntityPlayerMP)
                                senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_mail.message.command.oxygens.mail.packageSent", 
                                        sharedData.getUsername(),
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())); 
                            else
                                server.sendMessage(new TextComponentString(String.format("Package sent to <%s> - subject: %s, message: %s, amount: %s, item: %s", 
                                        sharedData.getUsername(),
                                        subject,
                                        message,
                                        amount,
                                        itemStack.getDisplayName())));
                        } else
                            throw new WrongUsageException("Invalid items amount!");
                        break;
                    }
                }
            }
        }
    }
}
