package austeretony.oxygen_mail.common;

import austeretony.oxygen.common.OxygenLoaderServer;
import austeretony.oxygen.common.api.IPersistentData;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailLoaderServer {

    public static void loadPersistentDataDelegated(IPersistentData persistentData) {
        MailManagerServer.instance().getIOThread().addTask(()->{
            OxygenLoaderServer.loadPersistentData(persistentData);
            MailMain.LOGGER.info("Loaded {} mailboxes.", MailManagerServer.instance().getMailboxesAmount());
            MailManagerServer.instance().processExpiredMail();
        });
    }


    public static void savePersistentDataDelegated(IPersistentData persistentData) {
        MailManagerServer.instance().getIOThread().addTask(()->OxygenLoaderServer.savePersistentData(persistentData));
    }
}