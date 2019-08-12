package austeretony.oxygen_mail.common;

import austeretony.oxygen.common.OxygenLoaderServer;
import austeretony.oxygen.common.api.IPersistentData;

public class MailLoaderServer {

    public static void loadPersistentDataDelegated(IPersistentData persistentData) {
        MailManagerServer.instance().getIOThread().addTask(()->{
            OxygenLoaderServer.loadPersistentData(persistentData);
            MailManagerServer.instance().processExpiredMail();
        });
    }


    public static void savePersistentDataDelegated(IPersistentData persistentData) {
        MailManagerServer.instance().getIOThread().addTask(()->OxygenLoaderServer.savePersistentData(persistentData));
    }
}