package net.kitsunet.kitsunuki;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

public class Kitsune extends JavaPlugin {
 	public Logger log = Bukkit.getLogger();
 	
 	public KitsuCommands yerf = null;
 	public TanukiEvents pon = null;
 	public XP xp = null;
 	
 	private FileConfiguration users = null; // User variables
 	private File usersF = null;
 	
 	private FileConfiguration worlds = null; // World variables
 	private File worldsF = null;

 	private FileConfiguration enchantdb = null; // Enchantment database
 	private File enchantDBf = null;

 	public Economy tails = null;
 	public Essentials ess = null;
 	
 	public void reloadEnchantConfig() {
 	    if (enchantDBf == null) {
 	    	enchantDBf = new File(getDataFolder(), "enchant.yml");
 	    }
 	    enchantdb = YamlConfiguration.loadConfiguration(enchantDBf);
 	    InputStream defConfigStream = this.getResource("enchant.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        enchantdb.setDefaults(defConfig);
 	    }
 	    enchantdb.options().copyDefaults(false);
 	    try {
			enchantdb.save(enchantDBf);
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
 	public void reloadUserConfig() {
 	    if (usersF == null) {
 	    	usersF = new File(getDataFolder(), "users.yml");
 	    }
 	    users = YamlConfiguration.loadConfiguration(usersF);
 	}
 	public void reloadWorldConfig() {
 	    if (worldsF == null) {
 	    	worldsF = new File(getDataFolder(), "worlds.yml");
 	    }
 	    worlds = YamlConfiguration.loadConfiguration(worldsF);
 	}
 	public FileConfiguration EnchantDB() {
 	    if (enchantdb == null) {
 	        this.reloadEnchantConfig();
 	    }
 	    return enchantdb;
 	}
 	public FileConfiguration userData() {
 	    if (users == null) {
 	        this.reloadUserConfig();
 	    }
 	    return users;
 	}
 	public FileConfiguration worldData() {
 	    if (worlds == null) {
 	        this.reloadWorldConfig();
 	    }
 	    return worlds;
 	}
 	public void saveEnchantConfig() {
 	    if (enchantdb == null || enchantDBf == null) return;
 	    try {
 	        EnchantDB().save(enchantDBf);
 	    } catch (IOException ex) {
 	        this.log.severe("Could not save config to " + enchantDBf);
 	    }
 	}
 	public void saveUserConfig() {
 	    if (users == null || usersF == null) return;
 	    try {
 	        userData().save(usersF);
 	    } catch (IOException ex) {
 	        this.log.severe("Could not save config to " + usersF);
 	    }
 	}
 	public void saveWorldConfig() {
 	    if (worlds == null || worldsF == null) return;
 	    try {
 	        EnchantDB().save(worldsF);
 	    } catch (IOException ex) {
 	        this.log.severe("Could not save config to " + worldsF);
 	    }
 	}
 	private boolean setupEconomy()
    {
 	   if (tails == null) {
 		   if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			   log.info("Vault not detected.");
 		   } else {
 			   RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 			   if (economyProvider != null) {
 				   tails = economyProvider.getProvider();
 			       log.info("Kitsunuki successfully patched into Vault.");
 			   } else 
 			       log.info("Kitsunuki FAILED to patch into Vault.");
 		   }
 	   }
 	   return (tails != null);
    }
 	private boolean hookIntoEssentials()
 	{
 		boolean success = true;
 		Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
 		if (essentials != null && essentials instanceof Essentials) {
 			ess = (Essentials) essentials;
 	 		log.info("Kitsunuki has successfully linked into essentials.  JOY.");
 		} else {
 	 		log.severe("Awwwww shit.  Essentials not detected.  This plugin will probably fail horribly.");
 	 		success = false;
 		}
 		return success;
 	}
 	public static String Ranebo(String target) {
		String clr = new String();
		clr = ChatColor.translateAlternateColorCodes('&', target);
		return clr;
	}
 	public void at(Player player, String target) {
		String clr = new String();
		clr = ChatColor.translateAlternateColorCodes('&', target);
		player.sendMessage(clr);
	}
 	@Override
 	public void onEnable() {
 		if (yerf == null)
 			yerf = new KitsuCommands(this);
 		if (pon == null)
 			pon = new TanukiEvents(this);
 		if (xp == null)
 			xp = new XP(this);
   	    this.setupEconomy();
   	    this.hookIntoEssentials();
   	    getCommand("kn").setExecutor(yerf);
   	    getCommand("convert").setExecutor(yerf);
   	    getCommand("selldrops").setExecutor(yerf);
   	    getCommand("buy").setExecutor(yerf);
   	    getCommand("rep").setExecutor(yerf);
   	    getCommand("xp").setExecutor(yerf);
   	    getCommand("wp").setExecutor(yerf);
   	    getCommand("tps").setExecutor(yerf);
		getServer().getPluginManager().registerEvents(pon, this);
   	    this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(false);
		this.reloadEnchantConfig();
		this.reloadUserConfig();
		this.reloadWorldConfig();
		log.info("Kitsunuki plugin has been enabled.");
	}
 	@Override
 	public void onDisable() {
 		this.saveConfig();
 		this.saveEnchantConfig();
 		this.saveUserConfig();
 		this.saveWorldConfig();
		log.info("Kitsunuki plugin has been disabled.");	 
	}
	public Player isOnline(String plr) {
		return (Bukkit.getServer().getPlayer(plr));
	}
	public String currency(Double x) {
		return String.format("$%.2f", x);
	}
	public String dbl2(Double x) {
		return String.format("%.2f", x);
	}	
	public static void main (String[] args) {
		System.out.println("This is a plugin for bukkit.  Please run it as such.");	
	}
}
