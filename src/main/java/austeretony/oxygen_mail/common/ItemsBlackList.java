package austeretony.oxygen_mail.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import austeretony.oxygen.util.JsonUtils;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class ItemsBlackList {

    private static ItemsBlackList instance;

    private final Set<ResourceLocation> registryNames = new HashSet<ResourceLocation>();

    private ItemsBlackList(String jsonFilePath) {
        this.load(jsonFilePath);
    }

    private void load(String jsonFilePath) {
        Path filePath = Paths.get(jsonFilePath);      
        if (Files.exists(filePath)) {
            try {      
                JsonArray jsonArray = JsonUtils.getExternalJsonData(jsonFilePath).getAsJsonArray();
                for (JsonElement element : jsonArray)
                    this.registryNames.add(new ResourceLocation(element.getAsJsonObject().getAsString()));
            } catch (IOException exception) {  
                MailMain.LOGGER.info("Items blacklist damaged!");
                exception.printStackTrace();
            }       
        } else {                
            try {               
                Files.createDirectories(filePath.getParent());             
                try (PrintStream printStream = new PrintStream(new File(jsonFilePath))) {
                    printStream.print("[]");
                } 
                MailMain.LOGGER.info("Created empty items blacklist file.");
            } catch (IOException exception) {      
                exception.printStackTrace();
            }                     
        }
    }

    public static void create(String jsonFilePath) {
        if (instance == null)
            instance = new ItemsBlackList(jsonFilePath);
    }

    public static ItemsBlackList instance() {
        return instance;
    }

    public boolean isBlackListed(ResourceLocation registryName) {
        return this.registryNames.contains(registryName);
    }

    public boolean isBlackListed(Item item) {
        return this.isBlackListed(item.getRegistryName());
    }

    public boolean isBlackListed(ItemStack itemStack) {
        return this.isBlackListed(itemStack.getItem().getRegistryName());
    }
}
