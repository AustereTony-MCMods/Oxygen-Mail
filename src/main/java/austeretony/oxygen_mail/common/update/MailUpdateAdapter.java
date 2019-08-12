package austeretony.oxygen_mail.common.update;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import austeretony.oxygen.common.api.update.AbstractUpdateAdapter;
import austeretony.oxygen.common.core.api.CommonReference;
import austeretony.oxygen.util.StreamUtils;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailUpdateAdapter extends AbstractUpdateAdapter {

    private boolean patched;

    @Override
    public String getModId() {
        return MailMain.MODID;
    }

    @Override
    public String getVersion() {
        return "0.8.1:beta:0";
    }

    @Override
    public void apply() {
        if (!this.patched) {
            this.patched = true;
            MailMain.LOGGER.info("Removing old mailboxes...");
            try (Stream<Path> paths = Files.walk(Paths.get(CommonReference.getGameFolder() + "/oxygen/worlds"), 1)) {
                paths
                .filter(Files::isDirectory)
                .forEach((p)->this.checkPath(p));
            } catch (IOException exception) {
                exception.printStackTrace();
            }       
            MailMain.LOGGER.info("Old mailboxes removed.");
        }
    }

    private void checkPath(Path path) {
        String pathStr = path.toString();
        this.process(pathStr + "/server/world/mail/mailboxes.dat");
        this.process(pathStr + "/client/world/mail/mailboxes.dat");
    }

    private void process(String folder) {
        if (Files.exists(Paths.get(folder))) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder))) {   
                StreamUtils.write((short) 0, bos);//just removing all data
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            MailMain.LOGGER.info("Processed: {}.", folder);
        }
    }
}