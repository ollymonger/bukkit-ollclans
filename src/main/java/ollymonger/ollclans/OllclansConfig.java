package ollymonger.ollclans;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class OllclansConfig implements Listener {

    FileConfiguration config = Ollclans.getPlugin().getConfig();
    Logger getLog = Ollclans.getPlugin().getLogger();

    public OllclansConfig(){
        File Config = new File("plugins/Ollclans", "config.yml");
        FileConfiguration Cfg = YamlConfiguration.loadConfiguration(Config);
        try {
            if(!Config.exists()) {
                Cfg.save(Config);
                updateConfig();
            } else {
                getLog.info("Config already exists!");
            }
        } catch(IOException e) {
            // Handle any IO exception here
            e.printStackTrace();
        }
    }

    public void updateConfig(){
        config.createSection("prefix");
        List<String> prefix = config.getStringList("prefix");
        prefix.add("SVR");
        config.set("prefix", prefix);
        config.createSection("settings");
        ConfigurationSection settings = config.getConfigurationSection("settings");

        settings.set(".clans-protected", false);
        settings.set(".clans-showtag", true);
        settings.set(".clans-tagfirstchar", "<");
        settings.set(".clans-taglastchar", ">");
        settings.set(".banned-tags", "null");
            List<String> bannedWords = settings.getStringList(".banned-tags");
            bannedWords.add("admin");
            bannedWords.add("moderator");
            bannedWords.add("helper");
            bannedWords.add("fuck");
            bannedWords.add("shit");

        settings.set(".banned-tags", bannedWords);

        config.createSection("clans");
        ConfigurationSection clans = config.getConfigurationSection("clans");
        Ollclans.getPlugin().saveConfig();
    }

}
