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
import java.util.List;
import java.util.logging.Logger;

public class OllclansConfig implements Listener {

    FileConfiguration config = Ollclans.getPlugin().getConfig();
    Logger getLog = Ollclans.getPlugin().getLogger();

    public OllclansConfig(){
        File config = new File("pluins/Ollclans", "config.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(config);
        try{
            if(!config.exists()){
                cfg.save(config);
                updateConfig();
            } else {
                getLog.info("Config found! Proceeding to load...");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConfig(){
        config.createSection("prefix");
        List<String> prefix = config.getStringList("prefix");
        prefix.add("spawn");
        config.set("prefix", prefix);
        config.createSection("spawn");
        ConfigurationSection spawn = config.getConfigurationSection("spawn");
        World world = Bukkit.getServer().getWorld("world");
        Double x = Bukkit.getWorld("world").getSpawnLocation().getX();
        spawn.set("world", world);
        spawn.set("x", x);
    }

}
