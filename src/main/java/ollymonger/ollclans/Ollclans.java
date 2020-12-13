package ollymonger.ollclans;

import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class Ollclans extends JavaPlugin implements Listener {

    private static Plugin plugin;
    private OllclansConfig config;

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
