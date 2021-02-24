package ollymonger.ollclans;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import java.util.*;

public final class Ollclans extends JavaPlugin implements Listener {

    HashMap<Player, Boolean> awaitingDisbandConfirm = new HashMap<Player, Boolean>();
    TreeMap<String, List<String>> awaitingInviteConfirm = new TreeMap<>();
    TreeMap<String, List<Integer>> clanMap = new TreeMap<>();

    private static Plugin plugin;

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Plugin: Ollclans now enabled");
        getLogger().info("Plugin Version: " + getDescription().getVersion());
        getLogger().info("Last Updated: (07/02/21)");
        getLogger().info("Most Recent Update: cmd: /clan create/disband/leave.");
        plugin = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new OllclansConfig(), this);
        OllclansConfig config = new OllclansConfig();
        ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
        assert clans != null;
        int sizeoflist = 0;
        getLogger().info(String.valueOf(this.getConfig().getStringList("clans").size()));
        if(clans.getKeys(false).size() >= 1){
            for (String key : clans.getKeys(false)) {
                ArrayList<Integer> clanObjects = new ArrayList<>(3);
                clanObjects.add(0, this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").size());
                clanObjects.add(1, this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getInt(".balance"));
                int isInviteOnly = this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getInt(".invite-only");
                clanObjects.add(2, isInviteOnly);
                clanMap.put(key, clanObjects);
                sizeoflist++;
            }
            getLogger().info("Loaded: "+sizeoflist+" clans.");
            SetUpClanSpawns();
        }
        if(clans.getKeys(false).size() == 0){
            getLogger().info("No clans have been created!");
        }
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        SetUpAndSendChat(p, event);
    }

    public void ShowLeaderboard(Player p, int page){
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String string = cfg.getString("prefix");
        SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());

        // Update hashmap with recent values...
        UpdateClanHashmap();

        // Loop through  hashmap and display leaderboard to player.
        int i = 0;
        String lastSection = ChatColor.GREEN+"Open"+ChatColor.GRAY;
        for(Map.Entry<String, List<Integer>> key : clanMap.entrySet()) {
            int membersSize = Math.addExact(key.getValue().get(0), 1); // adding one due to the owner being in the group.

            if(key.getValue().get(2).equals(1)){
                lastSection =  membersSize + " members" + " ("+ChatColor.RED + "Invite-Only"+ChatColor.GRAY+")";
            }
            if(key.getValue().get(2).equals(2))
            {
                lastSection = "0 members" + " ("+ChatColor.DARK_GRAY + "Disbanded"+ChatColor.GRAY+")";
            }
            if(key.getValue().get(2).equals(0)){

                lastSection = membersSize + " members" + " ("+ChatColor.GREEN + "Open"+ChatColor.GRAY+")";
            }

            map.put(i, ChatColor.RED + string + ChatColor.GOLD + " CLAN"+ ChatColor.RED+": "+ ChatColor.GOLD + key.getKey() + ChatColor.RED +" /" + ChatColor.AQUA+" CASH:"+ChatColor.GREEN+" $" + ChatColor.AQUA +key.getValue().get(1) +ChatColor.RED+" / "
                    + ChatColor.GRAY + lastSection);
            i++;
        }
        int mypage = page;
        paginate(p, map, mypage, 5);
    }

    public  void paginate(CommandSender sender, SortedMap<Integer, String> map, int page, int pageLength) {
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String string = cfg.getString("prefix");
        sender.sendMessage(ChatColor.DARK_PURPLE + "*-- ollclans leaderboard (page " + String.valueOf(page) + " of " + (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength) + 1) + ") --*");
        int i = 0, k = 0;
        page--;
        for (final Map.Entry<Integer, String> e : map.entrySet()) {
            k++;
            if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1))) {
                i++;
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + e.getValue());
            }
        }
        if(page < (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength))){
            //checks to see if there is a next page available
            sender.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Next page >> /clan leaderboard page " + Math.addExact(page, 2));
        }
    }

    public  void paginatemembers(CommandSender sender, SortedMap<Integer, String> map, int page, int pageLength) {
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String string = cfg.getString("prefix");
        sender.sendMessage(ChatColor.DARK_PURPLE + "*-- Clan Members (page " + String.valueOf(page) + " of " + (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength) + 1) + ") --*");
        int i = 0, k = 0;
        page--;
        for (final Map.Entry<Integer, String> e : map.entrySet()) {
            k++;
            if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1))) {
                i++;
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + e.getValue());
            }
        }
        if(page < (((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength))){
            //checks to see if there is a next page available
            sender.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Next page >> /clan members page " + Math.addExact(page, 2));
        }
    }


    public void UpdateClanHashmap(){
        getLogger().info("Clan hashmap is being cleared using leaderboard.");
        clanMap.clear();
        if(clanMap.isEmpty()){
            getLogger().info("Clan hashmap is empty! (SUCCESS)");
            ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
            int sizeoflist = 0;
                for (String key : clans.getKeys(false)) {
                    List<Integer> clanObjects = new ArrayList<>();
                    clanObjects.add(0, this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").size());
                    clanObjects.add(1, this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getInt(".balance"));
                    int isInviteOnly = this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getInt(".invite-only");
                    clanObjects.add(2, isInviteOnly);
                    clanMap.put(key, clanObjects);
                    sizeoflist++;
                }
                getLogger().info("Reloaded: "+sizeoflist+" clans using Leaderboard.");

        } else {
            getLogger().info("Clan hashmap is not empty! (FAIL)");
        }

    }

    public void SetUpAndSendChat(Player p, AsyncPlayerChatEvent event){
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String string = cfg.getString("prefix");
        ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
        String selectedKey = "null";
        List<String> clanOwners = new ArrayList<String>();
        List<String> clanMembers = new ArrayList<String>();
        assert clans != null;
        for (String key : clans.getKeys(false)) {
            if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), p.getName())){
                selectedKey = key;
                clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"));
            }
            if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(p.getName())){
                selectedKey = key;
                clanMembers.add(p.getName());
            }
        }
        if (clanOwners.contains(p.getName()) || clanMembers.contains(p.getName())) {
            String coloredTag = ChatColor.translateAlternateColorCodes('&', this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".clantag"));
            String coloredFirst = ChatColor.translateAlternateColorCodes('&', this.getConfig().getConfigurationSection("settings").getString(".clans-tagfirstchar"));
            String coloredLast = ChatColor.translateAlternateColorCodes('&', this.getConfig().getConfigurationSection("settings").getString(".clans-taglastchar"));
            event.setFormat(coloredFirst + coloredTag + coloredLast + " " + p.getDisplayName() + ChatColor.DARK_GRAY + " : " + ChatColor.WHITE + event.getMessage());
        } else {
            event.setFormat(p.getDisplayName() + ChatColor.DARK_GRAY + " : " + ChatColor.WHITE + event.getMessage());
        }
    }


    public boolean SendClanCMDS(CommandSender sender){
        String name = sender.getName();
        ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
        String selectedClan = "";
        List<String> clanOwners = new ArrayList<String>();
        assert clans != null;
        for (String key : clans.getKeys(false)) {
            clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"));
            selectedClan = key;
        }
        List<String> clanMembers = this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedClan).getStringList(".members");
        if(clanOwners.contains(name)){
            sender.sendMessage(ChatColor.GRAY +" - /clan <inviteonly> (Toggle whether invite only is required!)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <invite> <name> (Invite a player to join your clan!)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <setflag> (Set your clan's home point)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <disband> (Disband your clan)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <home> (Visit your clan home)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <members> (View your clan teammates)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <leaderboard> (View the clans leaderboard)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <stats> (View your clan stats)");
        }
        if(clanMembers.contains(name)){
            sender.sendMessage(ChatColor.GRAY +" - /clan <leave> (Leave your current clan)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <home> (Visit your clan home)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <members> (View your clan teammates)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <leaderboard> (View the clans leaderboard)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <stats> (View your clan stats)");
        }
        if(!clanMembers.contains(name) && !clanOwners.contains(name)){
            sender.sendMessage(ChatColor.GRAY +" - /clan <create> <name> <tag> (Create a clan)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <join> <name> (Join an open clan)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <accept/decline> <name> (Accept or decline a clan invite!)");
            sender.sendMessage(ChatColor.GRAY +" - /clan <leaderboard> (View the clans leaderboard)");
        }
        return true;
    }

    public void SetUpClanSpawns(){

        ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

        int i = 0;
        for(String key : clans.getKeys(false)){
            if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getConfigurationSection(".flag").getBoolean(".flagSet")){
                World getWorld = this.getServer().getWorld(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getConfigurationSection(".flag").getString(".flagWorld"));
                double x = this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getConfigurationSection(".flag").getDouble(".flagX");
                double y = this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getConfigurationSection(".flag").getDouble(".flagY");
                double z = this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getConfigurationSection(".flag").getDouble(".flagZ");

                Location locOnTop = new Location(getWorld, x+ 1f, y + 2f, z);

                ArmorStand armorStand = getWorld.spawn(locOnTop, ArmorStand.class);
                armorStand.setMarker(true);
                armorStand.setCustomName(key);
                armorStand.setCustomNameVisible(true);
                armorStand.setInvisible(true);
                i++;
            }
        }

        getLogger().info("Set up: " + i + " clan spawn points");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration cfg = Ollclans.getPlugin().getConfig();
        String name = sender.getName();
        Player player = this.getServer().getPlayer(name);
        if(cmd.getName().equalsIgnoreCase("clan")){
            if(args.length == 0){
                player.sendMessage(ChatColor.DARK_PURPLE +" *-------- ollclans commands --------*");
                SendClanCMDS(sender);
                return true;
            } else {
                if(args[0].equalsIgnoreCase("create")){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    List<String> clanOwners = new ArrayList<String>();
                    ArrayList<List<String>> clanMembers = new ArrayList<>();
                    assert clans != null;
                    for(String key : clans.getKeys(false)) {
                        clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"));
                        clanMembers.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members"));

                    }

                    if(args.length < 2){
                        assert player != null;
                        if(!clanOwners.contains(name) || !clanMembers.contains(name)){
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" /clan create <name> <tag>");
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" COST: $500!");
                            return true;
                        }

                        if(clanOwners.contains(name)){
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You are already the leader of a clan!");
                            return true;
                        }

                        if(clanMembers.contains(name)){
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You need to leave your current clan (/leaveclan)!");
                            return true;
                        }

                    } else {
                        // create a clan if the name does not already exist. Ask player for a 3 letter tag
                        if(!this.getConfig().getConfigurationSection("settings").getStringList(".banned-tags").contains(args[2]) && !this.getConfig().getConfigurationSection("settings").getStringList(".banned-tags").contains(args[1])){
                            if(!clanOwners.contains(name)){
                                if(!clans.contains(args[1])){
                                    ConfigurationSection newClan = clans.createSection(args[1]);
                                    newClan.set("clantag", args[2]);
                                    newClan.set("owner", name);
                                    newClan.set("level", 1);
                                    newClan.set("balance", 300);
                                    int inviteonly = 0;

                                    boolean inviteOnlyDefault = this.getConfig().getConfigurationSection("settings").getBoolean(".clans-defaultinviteonlystatus");
                                    if(inviteOnlyDefault) {
                                        inviteonly = 1;
                                    }

                                    newClan.set("invite-only", inviteonly);
                                    newClan.set("disbanded", false);
                                    ConfigurationSection newClanMembers = newClan.createSection("members");
                                    ConfigurationSection newClanFlag = newClan.createSection("flag");
                                    newClanFlag.set("flagSet", false);
                                    newClanFlag.set("flagX", 1);
                                    newClanFlag.set("flagY", 1);
                                    newClanFlag.set("flagZ", 1);
                                    saveConfig();

                                    Player pp = (Player) sender;

                                    Economy econ = null;
                                    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                                    if (economyProvider != null) {
                                        econ = economyProvider.getProvider();
                                    }

                                    econ.withdrawPlayer(player.getName(), 500);
                                    getServer().broadcastMessage(ChatColor.RED + string + ChatColor.GRAY + " "+ player.getName() + " has created the clan: "+args[1]+" ("+args[2]+")!");

                                } else {
                                    player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" This clan already exists!");
                                }
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You are already an owner of a clan!");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You have used a banned clan-name/clan-tag!");
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" Choose another!");
                        }

                    }
                }

                if(args[0].equalsIgnoreCase("leave")){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    List<String> clanMembers;
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"));
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }

                    }
                    clanMembers = this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getStringList(".members");
                    assert player != null;
                    if (clanOwners.contains(name)) {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You must disband the clan to leave!");
                        return true;
                    }
                    if(!clanMembers.contains(name)) {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You are not in a clan!");
                        return true;
                    }
                    if (clanMembers.contains(name)) {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY +" You have left this clan!");
                        clanMembers.remove(name);
                        this.getConfig().set("clans."+selectedKey+".members", clanMembers);
                        saveConfig();
                        return true;
                    }
                }

                if(args[0].equalsIgnoreCase("disband") && args.length < 2){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));
                    if(clanOwners.contains(name)){
                        // perhaps ask to confirm...? /clan disband confirm ??
                        // broadcast this?
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Are you sure you'd like to disband this clan?");
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " /clan disband confirm/decline to chose!");
                        awaitingDisbandConfirm.put(player, true);
                        /**/
                        return true;
                    }
                    if(!clanOwners.contains(name)){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("disband") && args[1].equalsIgnoreCase("confirm")){
                    String string = cfg.getString("prefix");

                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));

                    if(clanOwners.contains(name)){
                        if(awaitingDisbandConfirm.containsKey(player)){
                            getServer().broadcastMessage(ChatColor.RED + string + ChatColor.GRAY +" "+ selectedKey + " has disbanded!");
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".owner", "null");
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".members", "null");
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".disbanded", true);
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".invite-only", 2);
                            saveConfig();
                            Economy econ = null;
                            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                            if (economyProvider != null) {
                                econ = economyProvider.getProvider();
                            }
                            Double doubl = Double.parseDouble(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".balance"));
                            Double newdb = doubl / 1.75; // gives player money dependant on size? this will encourage more independant clans.
                            econ.depositPlayer(player.getName(), newdb);
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have disbanded your clan and been awarded: $" + newdb + "!");
                            awaitingDisbandConfirm.remove(player);
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        }
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("disband") && args[1].equalsIgnoreCase("decline")){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));
                    if(clanOwners.contains(sender.getName())){
                        if(awaitingDisbandConfirm.containsKey(getServer().getPlayer(sender.getName()))){
                            awaitingDisbandConfirm.remove(getServer().getPlayer(sender.getName()));
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have chosen to not disband your clan!");
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        }
                    }
                }

                if(args[0].equalsIgnoreCase("leaderboard") && args.length < 2){
                    ShowLeaderboard(player, 1);
                    return true;
                }
                if(args[0].equalsIgnoreCase("leaderboard") && args[1].equalsIgnoreCase("page")){
                    int page = 1;
                    if(args.length != 3){
                        ShowLeaderboard(player, page);
                    } else {
                        page = Integer.parseInt(args[2]);
                        if(page == 0) {
                            ShowLeaderboard(player, 1);
                        } else {
                            ShowLeaderboard(player, page);
                        }
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("inviteonly")  && args.length < 2){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));
                    if(clanOwners.contains(name)){
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getInt(".invite-only") == 0) {// clan is open
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".invite-only", 1); // now invite only
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Your clan has been set to invite only!");
                            saveConfig();
                            return true;
                        } else {
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).set(".invite-only", 0); // now open
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Your clan can now be joined!");
                            saveConfig();
                            return true;
                        }
                    }
                    if(!clanOwners.contains(name)){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }

                if(args[0].equalsIgnoreCase("invite") && args.length < 2){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));
                    if(clanOwners.contains(name)){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " /clan invite <playername> (Must be online)!");
                         return true;
                    }
                    if(!clanOwners.contains(name)){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("invite") && args.length > 1){
                    String string = cfg.getString("prefix");
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String selectedKey = "";
                    List<String> clanOwners = new ArrayList<String>();
                    assert clans != null;
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                    }
                    assert player != null;
                    clanOwners.add(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getString(".owner"));
                    if(clanOwners.contains(name)){
                        Player getTarget = Bukkit.getPlayerExact(args[1]);

                        if(getTarget != null){
                            boolean dontAllowTargetToJoin = true;
                            for (String key : clans.getKeys(false)) {
                                    if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), args[1])){
                                        selectedKey = key;
                                        dontAllowTargetToJoin = false;
                                    }
                                    if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(args[1])){
                                        selectedKey = key;
                                        dontAllowTargetToJoin = false;
                                    }
                                }
                            if(!dontAllowTargetToJoin){
                                player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Player: " + args[1] + " must leave their clan ("+selectedKey+") to join yours!");
                            } else {
                                List<String> targetInvite = new ArrayList<>();
                                targetInvite.add(0, getTarget.getName());
                                targetInvite.add(1, selectedKey);
                                targetInvite.add(2, name);
                                awaitingInviteConfirm.put(getTarget.getName(), targetInvite);
                                player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have invited: "+ args[1] + " to join: "+selectedKey+"!");
                                getTarget.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have been invited to join: " + selectedKey + " by: " + name);
                            }
                            return true;
                        }
                        if(getTarget == null){
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " This player is not connected!");
                            return true;
                        }
                    }
                    if(!clanOwners.contains(name)){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("acceptinv")){
                    String string = cfg.getString("prefix");
                    if(awaitingInviteConfirm.containsKey(player.getName())){
                        // You have accepted an invite from: %owner-name% of %new_clan_name% clan.
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + "You have accepted an invite from: " + awaitingInviteConfirm.get(player.getName()).get(2) +
                                " of " + awaitingInviteConfirm.get(player.getName()).get(1) + " clan!");

                        List<String> membersList = this.getConfig().getConfigurationSection("clans").
                                getConfigurationSection(awaitingInviteConfirm.get(player.getName()).get(1)).
                                getStringList("members");
                        membersList.add(awaitingInviteConfirm.get(player.getName()).get(0));
                        getConfig().getConfigurationSection("clans").getConfigurationSection(awaitingInviteConfirm.get(player.getName()).get(1)).set(".members", membersList);
                        saveConfig();

                        player.sendMessage(String.valueOf(this.getConfig().getConfigurationSection("clans").
                                getConfigurationSection(awaitingInviteConfirm.get(player.getName()).get(1)).
                                getStringList("members")));

                        for(Player all : Bukkit.getOnlinePlayers()){
                            //loop through all
                            if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(awaitingInviteConfirm.get(player.getName()).get(1)).getStringList(".members").contains(all.getName())){
                                all.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " * Player: "+ player.getName() + " has joined your clan!");
                            }
                        }

                        //player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have accepted the invite to join: " +awaitingInviteConfirm.get(player).toString());
                        awaitingInviteConfirm.remove(player.getName());
                    }
                }
                if(args[0].equalsIgnoreCase("declineinv")){
                    String string = cfg.getString("prefix");
                    if(awaitingInviteConfirm.containsKey(player.getName())){
                        // You have declined an invite from: %owner-name% of %new_clan_name% clan.
                        player.sendMessage(awaitingInviteConfirm.get(player.getName()).get(0) + " declined invite to join: " +
                                awaitingInviteConfirm.get(player.getName()).get(1) + " from: " + awaitingInviteConfirm.get(player.getName()).get(2));
                        //player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have accepted the invite to join: " +awaitingInviteConfirm.get(player).toString());
                        awaitingInviteConfirm.remove(player.getName());
                    }
                }

                if(args[0].equalsIgnoreCase("members") && args.length < 2){
                    String string = cfg.getString("prefix");
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }
                    int i = 0;
                    if(!selectedKey.equals("")){
                        for(String key : this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getStringList(".members")){

                            map.put(i, ChatColor.GRAY+key);
                            i++;
                        }
                        paginatemembers(sender, map, 1, 5);
                    } else {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("members") && args[1].equalsIgnoreCase("page") && args.length > 2){
                    String string = cfg.getString("prefix");
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }
                    int i = 0;
                    if(!selectedKey.equals("")){
                        for(String key : this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getStringList(".members")){

                            map.put(i, ChatColor.GRAY+key);
                            i++;
                        }


                        int page = 1;
                        if(args.length != 3) {
                            paginatemembers(sender, map, 1, 5);
                        } else {
                            page = Integer.parseInt(args[2]);
                            if (page == 0) {
                                page = 1;
                            }
                            paginatemembers(sender, map, page, 5);
                        }

                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("join") && args.length < 2){
                    String string = cfg.getString("prefix");
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }

                    if(selectedKey.equalsIgnoreCase("")){
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " /clan join <clanname>!");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You are already in a clan!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("join") && args.length == 2){
                    String string = cfg.getString("prefix");
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }

                    if(selectedKey.equalsIgnoreCase("")){
                        if(this.getConfig().getConfigurationSection("clans").isConfigurationSection(args[1])){
                            if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(args[1]).getInt(".invite-only") == 0){
                                List<String> membersList = this.getConfig().getConfigurationSection("clans").
                                        getConfigurationSection(args[1]).
                                        getStringList("members");
                                membersList.add(player.getName());
                                this.getConfig().getConfigurationSection("clans").getConfigurationSection(args[1]).set("members", membersList);
                                saveConfig();
                                player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have successfully joined: " + args[1] + " clan.");

                            } else {
                                player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " This clan cannot be joined (Either invite only or disbanded)!");
                            }
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " This clan does not exist!!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You are already in a clan!");
                        return true;
                    }
                }
                if(args[0].equalsIgnoreCase("setflag")){
                    String string = cfg.getString("prefix");
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    String clanOwner = "";

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                            clanOwner = name;
                        }
                    }

                    if(clanOwner.equals(name)){

                        int x = player.getLocation().getBlockX(), y = player.getLocation().getBlockY(), z = player.getLocation().getBlockZ();
                        Location loc = new Location(player.getWorld(), x, y, z);
                        Location locOnTop = new Location(player.getWorld(), x+ 1f, y + 2f, z);
                        if(!this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getBoolean(".flagSet")){
                            Block block = loc.getBlock();
                            List<Material> randomBanner = new ArrayList<>();
                            randomBanner.add(Material.ORANGE_BANNER);
                            randomBanner.add(Material.RED_BANNER);
                            randomBanner.add(Material.BLUE_BANNER);
                            randomBanner.add(Material.YELLOW_BANNER);
                            randomBanner.add(Material.BLACK_BANNER);

                            block.setType(randomBanner.get((int)(Math.random() * randomBanner.size())));

                            ArmorStand armorStand = player.getWorld().spawn(locOnTop, ArmorStand.class);
                            armorStand.setMarker(true);
                            armorStand.setCustomName(selectedKey);
                            armorStand.setCustomNameVisible(true);
                            armorStand.setInvisible(true);


                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You have set your flag position!");
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").set(".flagSet", true);
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").set(".flagWorld", player.getWorld().getName());
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").set(".flagX", x);
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").set(".flagY", y);
                            this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").set(".flagZ", z);
                            saveConfig();
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Your clan-spawn has already been set (contact an admin)!");
                            return true;
                        }
                    }
                }

                if(args[0].equalsIgnoreCase("home")){
                    String string = cfg.getString("prefix");
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");
                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }
                    if(!selectedKey.equalsIgnoreCase("")){
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getBoolean(".flagSet")){
                            World world= this.getServer().getWorld(this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getString(".flagWorld"));
                            double x = this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getDouble(".flagX");
                            double y = this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getDouble(".flagY");
                            double z = this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getDouble(".flagZ");

                            Location location = new Location(world, x, y, z);
                            player.teleport(location);

                            // Teleport if not hit by anyone! if so, cancel?

                            PlayerTeleportInSeconds(player, location, time);

                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Your clan has not set it's flag! (Contact clan owner)");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " You cannot use this command!");
                        return true;
                    }
                }

                if(args[0].equalsIgnoreCase("stats")){
                    String string = cfg.getString("prefix");
                    String selectedKey = "";
                    ConfigurationSection clans = Ollclans.getPlugin().getConfig().getConfigurationSection("clans");

                    for (String key : clans.getKeys(false)) {
                        if(Objects.equals(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getString(".owner"), name)){
                            selectedKey = key;
                        }
                        if(this.getConfig().getConfigurationSection("clans").getConfigurationSection(key).getStringList(".members").contains(name)){
                            selectedKey = key;
                        }
                    }

                    if(!selectedKey.equalsIgnoreCase("")){
                        player.sendMessage(ChatColor.DARK_PURPLE +" *-------- "+ ChatColor.GRAY + "CLAN: " + selectedKey + " stats " +ChatColor.DARK_PURPLE+"--------*");
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Level: " + ChatColor.AQUA + this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getInt(".level"));
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Balance: " + ChatColor.GREEN + "$" + this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getInt(".balance"));
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Members: " + this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getStringList(".members").size());
                        player.sendMessage(ChatColor.RED + string + ChatColor.GRAY + " Home set: " + this.getConfig().getConfigurationSection("clans").getConfigurationSection(selectedKey).getConfigurationSection(".flag").getBoolean(".flagSet"));

                    }
                }

            }

        }

        // disband clan cmd (owner of clan only) done

        // leaderboard cmd (show all clans, its level and member coumt?

        // flag will just be a banner of a rand color flag used as telelport point
        // /clan <args> (example: flag, stats, members, home) to get a new flag to plant only if flag has not already been set? done
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getServer().dispatchCommand(Bukkit.getConsoleSender(), "minecraft:kill @e[type=armor_stand]");
    }
}
