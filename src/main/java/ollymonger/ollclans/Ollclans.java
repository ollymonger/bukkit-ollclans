package ollymonger.ollclans;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class Ollclans extends JavaPlugin implements Listener {

    private static Plugin plugin;

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Plugin: Ollclans now enabled");
        getLogger().info("Plugin Version: " + getDescription().getVersion());
        getLogger().info("Last Updated: (20/08/19)");
        getLogger().info("Most Recent Update: Players can no longer move whilst playing minigame.");
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new OllclansConfig(), this);
        OllclansConfig config = new OllclansConfig();

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String name = sender.getName();
        Player player = this.getServer().getPlayer(name);
        if(cmd.getName().equalsIgnoreCase("createclan")){
            if(args.length == 0){
                String string = cfg.getString("prefix");
                assert player != null;
                player.sendMessage(string + " | You have not specified a clan name!");
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
