package net.kitsunet.kitsunuki;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.Item;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import net.sourceforge.jeval.Evaluator;

import com.earth2me.essentials.user.Inventory;
import com.google.common.base.Joiner;

public class KitsuCommands implements CommandExecutor {
	private Kitsune yip;
	private TanukiEvents pon;
	
	public KitsuCommands(Kitsune yip) {
		this.yip = yip;
		this.pon = yip.pon;
	}
	
	public boolean buy(Player player, String[] args) {
		ConfigurationSection items = yip.getConfig().getConfigurationSection("buy");
		if (args.length == 0) {
			String x = "";
			for (String item: items.getKeys(false)) {
				x = x+item+" ";
			}
			yip.at(player,"&3Things available to buy: &b"+x);
		} else {
			int mode = 0;
			int units = 1;
			int tmp = 0;
			String want = args[0];
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("price")) {
					mode = 1; want = args[1];
				} else {
					units = Integer.valueOf(args[1]);
					if (units < 1) units = 1;
					if (units > 64) units = 64;
				}
			} 
			
			// lets do some buying...
			String item = yip.getConfig().getString("buy."+want+".item");
			Material itemid = Material.AIR;
			Double itemcost = 0.0;
			Integer itemcount = 0;
			while (tmp < units) {
				itemcost += yip.getConfig().getDouble("buy."+want+".for",1.0);
				itemcount += yip.getConfig().getInt("buy."+want+".count",1);
				if (itemcount+yip.getConfig().getInt("buy."+want+".count",1) > 64)
					break;
				tmp++;
			}
			if (item == null) {
				yip.at(player,"The item '"+want+"' cannot be bought from the server." );
				return true;
			}
			if (item.split(":").length == 0) {
				yip.at(player,"The item '"+want+"' cannot be bought from the server.");
				yip.log.warning("config.yml at buy."+want+".item: no item id!");
				return true;				
			} else if (item.split(":").length >= 1) {
				itemid = Material.getMaterial(Integer.parseInt(item.split(":")[0]));
			} else if (item.split(":").length >= 2) {
				itemid.getNewData((byte)Integer.parseInt(item.split(":")[1]));
			}
			ItemStack itemstack = new ItemStack(itemid, itemcount);
			String response = "&6WTB: &e"+Integer.toString(itemstack.getAmount())+"&fx &b"+itemstack.getType().toString().toLowerCase()+"&f for &2"+yip.currency(itemcost)+"&f";
			if (mode == 0) {
				if (yip.tails.has(player.getName(), itemcost)) {
					PlayerInventory inventory = player.getInventory();
					inventory.addItem(itemstack);
					yip.tails.withdrawPlayer(player.getName(), itemcost);
					response += " -- &aSold!";
				} else 
					response += " -- &4Not enough money";
			}
			yip.at(player, response);
		}
		return true;
	}
	public boolean convert(Player player, String[] args) {
		ConfigurationSection items = yip.getConfig().getConfigurationSection("convert");
		if (args.length == 0) {
			String x = "";
			for (String item: items.getKeys(false)) {
				x = x+item+" ";
			}
			yip.at(player,"&3Convertable resources: &b"+x);
		}
		if (args.length >= 1) {
			String conv = args[0];
			Material fromid = Material.AIR;  Material toid = Material.AIR;	        
			String from = yip.getConfig().getString("convert."+conv+".from", "0");
			String to = yip.getConfig().getString("convert."+conv+".to", "0");
			Integer take = yip.getConfig().getInt("convert."+conv+".take", 1);
			Integer give = yip.getConfig().getInt("convert."+conv+".give", 1);
			Double cost = yip.getConfig().getDouble("convert."+conv+".cost", 1);
			if (from.split(":").length == 0) {
				yip.at(player,"The item '"+conv+"' cannot completed as dialed.");
				yip.log.warning("config.yml at conv."+conv+".from: no item id!");
				return true;				
			} else if (from.split(":").length >= 1) {
				fromid = Material.getMaterial(Integer.parseInt(from.split(":")[0]));
			} else if (from.split(":").length >= 2) {
				fromid.getNewData((byte)Integer.parseInt(from.split(":")[1]));
			}
			if (to.split(":").length == 0) {
				yip.at(player,"The item '"+conv+"' cannot be completed as dialed.");
				yip.log.warning("config.yml at conv."+conv+".from: no item id!");
				return true;				
			} else if (to.split(":").length >= 1) {
				toid = Material.getMaterial(Integer.parseInt(to.split(":")[0]));
			} else if (to.split(":").length >= 2) {
				fromid.getNewData((byte)Integer.parseInt(to.split(":")[1]));
			}
			String response = "&6"+conv+"&f converts &2"+fromid.toString().toLowerCase()+"&f to &2"+toid.toString().toLowerCase()+"&f at a &e"+take.toString()+":"+give.toString()+"&f rate.";
			if (cost > 0)
				response += "  Each conversion costs &4"+yip.currency(cost)+"&f.";
			yip.at(player, response);
			if (args.length >= 2) {
				Integer count=0;
				if (args[1].equalsIgnoreCase("all"))
					count = 65536;
				else if (args[1].equalsIgnoreCase("hand"))
					count = -1;
				else {
					try {
						if (Integer.parseInt(args[1]) > 0)
							count = Integer.parseInt(args[1]);
					} catch (Exception e) {
						count = 0;
					}				
				}
				if (count == 0) {
					yip.at(player, "Invalid count for conversion specified.");
					return true;
				}
				Integer taken = 0;
				Integer given = 0;
				PlayerInventory inventory = player.getInventory();
				ItemStack fromstack = new ItemStack(fromid, take);
				ItemStack tostack = new ItemStack(toid, give);
				while (taken < count) {
					if (!yip.tails.has(player.getName(), cost)) break;
					if (!yip.invContains(inventory, fromstack)) break;
					inventory.removeItem(fromstack);
					inventory.addItem(tostack);
					if (cost>0)
						yip.tails.withdrawPlayer(player.getName(), cost);
					taken += take;
					given += give;
					}
				if (taken == 0)
					yip.at(player, "&4You didn't have enough money or &e"+fromid.toString().toLowerCase()+"&4 to give.");
				else
					yip.at(player,"&6Converted &e"+taken.toString()+" &b"+fromid.toString().toLowerCase()+"&f to &e"+given.toString()+" &b"+toid.toString().toLowerCase()+
						(cost<=0 ? "&f." : "&f for &a"+yip.currency(cost*taken)));	
			}
		}
		return true;
	}
	public boolean repair(Player player, String[] args) {
		if (args.length == 0) { // help
			yip.at(player, "&6/rep&f: Repair the item in your hand.  See below:");
			yip.at(player, "&6/rep e[stimate]&f: Get an estimate in money/xp for the repair.");
			yip.at(player, "&6/rep c[onfirm]&f: Confirm the estimate and repair your item in hand.");
			yip.at(player, "&6/rep d[isenchant]&f: Disenchant and repair your item in hand.");
		} else if (args.length >= 1) {
			boolean confirmed = args[0].startsWith("c");
			// Formula for XP cost is: (enchantment weight*factor)+(enchantment level*factor)
			PlayerInventory inventory = player.getInventory();
			ItemStack held = inventory.getItemInHand();
			if (args[0].startsWith("test")) {
				yip.at(player, "Held item "+held.getType().toString()+" has durability "+String.valueOf(held.getDurability()));
				return true;
			}
			if (held.getType() == Material.AIR) {
				yip.at(player, "&cWe cannot repair air, Keptin!");
				return true;
			}
			if (held.getDurability() == 0) {
				if (!args[0].startsWith("d")) {
					yip.at(player, "&eThis item is in as good shape as it's going to get.");
					return true;
				}
			}
			// Simple stuff first, cost to repair the mundane properties.
			Double cost = yip.getConfig().getDouble("repair.regular.base", 100)+
					(held.getDurability()*yip.getConfig().getDouble("repair.regular.perdamage", 1.0));
			// Now aggregate the enchantments if any
			String[] lvls = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
			Integer eWeight = 0; Integer eLevel = 0;
			String enchants = ""; Integer xp = 0;
			if (held.getEnchantments().entrySet().size() >= 0) {
				enchants = "";
				for (Map.Entry<Enchantment, Integer> x : held.getEnchantments().entrySet()) {
					eWeight += (10-yip.EnchantDB().getInt(x.getKey().getName()+".weight"));
					eLevel += x.getValue();
					enchants += yip.EnchantDB().getString(x.getKey().getName()+".name")+" "+lvls[x.getValue()]+", ";
				}
				if (enchants.length() > 0)
					enchants=enchants.substring(0, enchants.length()-1);
				xp=((eWeight*yip.getConfig().getInt("repair.enchant.weight", 1))+
				    (eLevel*yip.getConfig().getInt("repair.enchant.level", 1)))*
				     yip.getConfig().getInt("repair.enchant.xp", 1);						   
			}
			String response = "";
			yip.at(player, "&6Repair cost of mundane properties: &e"+((Integer)(held.getDurability()+0)).toString() +" damage, &a"+
			   yip.currency(cost));
			if (enchants != "" && !args[0].startsWith("d"))
				yip.at(player, "&6Preservation cost of enchantments: &d"+enchants+" &e"+eWeight.toString() +" weight, "+
						eLevel.toString()+" power.  &b"+xp.toString()+"xp, ~"+((Integer)yip.xp.xp2level(xp)).toString()+" XP levels");
			response += "&6You have &a"+yip.currency(yip.tails.getBalance(player.getName()))+" and &b"+((Integer)player.getTotalExperience()).toString()+"xp";
			if (yip.tails.has(player.getName(), cost) && ((player.getTotalExperience() >= xp) || args[0].startsWith("d"))) {
				response += "&6 -- &a You can afford this.";
			} else {
				response += "&6 -- &4 You cannot afford this.";
				yip.at(player, response);
				return true;				
			}
			// actually do the repair now.
			if (args[0].startsWith("d")) { // disenfranchise this item
				held.setDurability((short) 0);
				for (Enchantment enc: held.getEnchantments().keySet()) 
					held.removeEnchantment(enc);
				yip.tails.withdrawPlayer(player.getName(), cost);
				yip.at(player, response+" Item repaired and disenchanted!");
				return true;
			}
			if (confirmed) {
				held.setDurability((short) 0);
				try {
					yip.xp.adjXP(player, -(xp));
				} catch (Exception e) {
					yip.xp.setXP(player, 0);
				}
				yip.tails.withdrawPlayer(player.getName(), cost);
				yip.at(player, response+" Item repaired!");
			} else {
				yip.at(player, response+" &6use &e/rep c&6 to do the repair.");				
			}
		}
		return true;
	}
	public boolean liquidate(Player player, String[] args) {
		ConfigurationSection categories = yip.getConfig().getConfigurationSection("liquidate");
		if (args.length == 0) {
			yip.at(player, "&b=== &6Liquidatable assets &b===");
			for (String category: categories.getKeys(false)) {
				String x = "&6"+category+"&r: &a";
				List<String> items = yip.getConfig().getStringList("liquidate."+category);
				for (String item: items) {
					try {
						if (!(yip.ess.getItemDb().get(item) == null))
							x += yip.ess.getItemDb().get(item).getType().toString().toLowerCase()+" ";
					} catch (Exception e) {
						yip.log.severe(e.toString());
					}
				}
				yip.at(player, x);
			}
			return true;
		}
		PlayerInventory inventory = player.getInventory();
		if (args.length == 1) { // normal command to sell all drops without parameters
			List<String> items = yip.getConfig().getStringList("liquidate."+args[0]);
			if (items.size() == 0) {
				yip.at(player, "&4Not a valid category.  Try '/liquidate' alone for a list of categories.");
				return true;
			}
			Double total = 0.0;
			Double prev = yip.tails.getBalance(player.getName());
			ItemStack itemstack = new ItemStack(Material.AIR,1);
			for (String item: items) {
				try {
					if (yip.ess.getItemDb().get(item) != null) {
						itemstack = yip.ess.getItemDb().get(item).clone();
						itemstack.setAmount(1);
					} else continue;
				} catch (Exception e) {
					continue;
				}
				Integer count = 0;
				Double subtotal = 0.0;
				while (inventory.removeItem(itemstack).size()==0) {					
					yip.tails.depositPlayer(player.getName(), yip.ess.getWorth().getPrice(itemstack));
					count++; subtotal += yip.ess.getWorth().getPrice(itemstack);
					total += yip.ess.getWorth().getPrice(itemstack);
					if (count>=2560) {
						yip.log.warning("Grand Overflow in liquidate while selling: "+itemstack);
						break;
					}
				}
				if (count>0) yip.at(player,"&6Sold &e"+count.toString()+" &b"+itemstack.getType().toString().toLowerCase()+"&6 for &a"+yip.currency(subtotal));						
			}
			yip.at(player,"&6Grand total: &a"+yip.currency(total)+"&6 earned [&a"+yip.currency(prev)+"&6 -> &a"+
					yip.currency(yip.tails.getBalance(player.getName()))+"&6]");						
		}
		return true;
	}
	public boolean xp(Player player, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("on")) {
				yip.userData().set(player.getName()+".xp.reporting", true);
				yip.at(player, "&d[&eXP&d] XP reporting is now &aON&r.");
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				yip.userData().set(player.getName()+".xp.reporting", false);
				yip.at(player, "&d[&eXP&d] XP reporting is now &aOFF&r.");				
				return true;
			} else if (args[0].equalsIgnoreCase("bottle")) {
				PlayerInventory inventory = player.getInventory();
				ItemStack itemstack = new ItemStack(Material.EXP_BOTTLE,1);
				Integer xpstore = 17;
				if (args.length == 2) {
					try {
						xpstore = Integer.valueOf(args[1]);
					} catch (Exception e) {
						xpstore = yip.xp.getTotalExperience(player);
					}
				} else xpstore = yip.xp.getTotalExperience(player);
				if (xpstore < 17) xpstore = 17;
				if (xpstore > yip.xp.getTotalExperience(player)) xpstore = yip.xp.getTotalExperience(player);
				if (yip.xp.getTotalExperience(player) < xpstore) {
					yip.at(player, "You do not have enough XP to create that experience potion.");
					return true;
				}
				itemstack.setDurability((short)(xpstore % 65536));
				yip.xp.adjXP(player, -(xpstore));
				inventory.addItem(itemstack);
				yip.at(player, "&6You have created an experience potion containing &a"+xpstore+"XP&6.");
			} else if (args[0].equalsIgnoreCase("drink")) {
				PlayerInventory inventory = player.getInventory();
				ItemStack bottle = inventory.getItemInHand();
				if (bottle.getType() != Material.EXP_BOTTLE) {
					yip.at(player, "&4That is not an experience bottle!  You are unable to drink &6"+bottle.getType().name()+"&4.");
					return true;
				}
				Integer xpstore = 0;
				if (bottle.getDurability() < 17) {
					xpstore = new Random().nextInt(10)+10;
				} else {
					xpstore = (Integer)(bottle.getDurability() % 65536);
				}
				bottle.setAmount(bottle.getAmount()-1);
				inventory.setItemInHand(bottle);
				yip.xp.adjXP(player, xpstore);
				yip.at(player, "&6You have drunk an experience potion containing &a"+xpstore+"XP&6.  Are you buzzin' yet?");					
			} else if (args[0].equalsIgnoreCase("check")) {
				PlayerInventory inventory = player.getInventory();
				ItemStack bottle = inventory.getItemInHand();
				if (bottle.getType() != Material.EXP_BOTTLE) {
					yip.at(player, "&4That is not an experience bottle!  You are unable to drink &6"+bottle.getType().name()+"&4.");
					return true;
				}
				if (bottle.getDurability() < 17)
					yip.at(player, "&6This experience potion seems to have &bbetween 10 and 20XP&6 in it.");
				else
					yip.at(player, "&6This experience potion seems to have &b"+bottle.getDurability()+"XP&6 in it.");
				return true;
			}
		}
		yip.at(player, "&d[&eXP&d] &r"+player.getDisplayName()+"&6 has &b"+Integer.toString(yip.xp.getXP(player))+
				"&6xp (L&b"+player.getLevel()+"&6), &b"+
				((Integer)Math.round(player.getExpToLevel()-(player.getExp()*player.getExpToLevel())))
				+"&6xp to next level.");
		// Provide a fix for the XP bar not updating bug.
		if (args.length == 0)
			player.setExp(player.getExp());					
		return true;
	}
	public boolean wp(Player player, String[] args) {
		ConfigurationSection wps = yip.userData().getConfigurationSection(player.getName()+".wp");
		World wld = null;
		String wpname = "";
		if (args.length == 0) { // list personal waypoints
			String x = "";
			if (wps != null && (wps.getKeys(false).size() > 0)) {
				for (String item: wps.getKeys(false)) {
					x=x+item+" ";
				}
			} else {
				x = "<none>";
			}
			yip.at(player, "&d[&eWP&d] &6Your waypoints: "+x);
		} else if (args.length == 1) {
			wpname = args[0].toLowerCase(); 
			if (wpname.equalsIgnoreCase("help")) {
				yip.at(player, "&6/wp           &rList your personal waypoints");
				yip.at(player, "&6/wp help      &rYou're looking at it.");
				yip.at(player, "&6/wp get <wp>  &rGet the coords of a waypoint");
				yip.at(player, "&6/wp set <wp>  &rSet a waypoint to your current position");
				yip.at(player, "&6/wp del <wp>  &rDelete a waypoint.");
				return true;
			}
			if (yip.userData().getString(player.getName()+".wp."+wpname+".world") != "") {
				try {
					wld = yip.getServer().getWorld(yip.userData().getString(player.getName()+".wp."+wpname+".world"));
				} catch (Exception e) {
					yip.at(player, "&d[&eWP&d] &4Unknown waypoint.");
					return true;
				}
				Vector pos = yip.userData().getVector(player.getName()+".wp."+wpname+".pos");
				Double yaw = yip.userData().getDouble(player.getName()+".wp."+wpname+".yaw");
				Double pitch = yip.userData().getDouble(player.getName()+".wp."+wpname+".pitch");
				Location wploc = new Location(wld, pos.getX(), pos.getY(), pos.getZ());
				wploc.setYaw(yaw.floatValue());
				wploc.setPitch(pitch.floatValue());
				player.teleport(wploc);
				Double cost = yip.getConfig().getDouble("wp.usecost");
				if (cost > 0)
					yip.tails.withdrawPlayer(player.getName(), cost);
				yip.at(player, "&d[&eWP&d] &6Teleported to Waypoint &e"+wpname+"&6 located at &b["+
						yip.dbl2(pos.getX())+", "+yip.dbl2(pos.getY())+", "+yip.dbl2(pos.getZ())+
						"] &6in &a"+yip.userData().getString(player.getName()+".wp."+wpname+".world")+"&6"+
						(cost == 0 ? "." : " for &c"+yip.currency(cost)+"&6."));				
			}
		} else if (args.length == 2) {
			wpname = args[1].toLowerCase();
			if (args[0].equalsIgnoreCase("get")) {
				try {
					wld = yip.getServer().getWorld(yip.userData().getString(player.getName()+".wp."+wpname+".world"));
				} catch (Exception e) {
					yip.at(player, "&d[&eWP&d] &4Unknown waypoint.");
					return true;
				}
				Vector pos = yip.userData().getVector(player.getName()+".wp."+wpname+".pos");
				yip.at(player, "&d[&eWP&d] &6Waypoint &e"+wpname+"&6 located at &b["+
						yip.dbl2(pos.getX())+", "+yip.dbl2(pos.getY())+", "+yip.dbl2(pos.getZ()));
			} else if (args[0].equalsIgnoreCase("set")) {
				yip.userData().set(player.getName()+".wp."+wpname+".world",player.getLocation().getWorld().getName());
				yip.userData().set(player.getName()+".wp."+wpname+".pos",player.getLocation().toVector());
				yip.userData().set(player.getName()+".wp."+wpname+".yaw",player.getLocation().getYaw());
				yip.userData().set(player.getName()+".wp."+wpname+".pitch",player.getLocation().getPitch());
				Double cost = yip.getConfig().getDouble("wp.setcost");
				if (cost > 0)
					yip.tails.withdrawPlayer(player.getName(), cost);
				yip.at(player, "&d[&eWP&d] &6Waypoint &e"+wpname+"&6 set to &b["+
						yip.dbl2(player.getLocation().getX())+", "+
						yip.dbl2(player.getLocation().getY())+", "+
						yip.dbl2(player.getLocation().getZ())+"] &6in &a"+player.getLocation().getWorld().getName()+"&6"+
						(cost == 0 ? "." : " for &c"+yip.currency(cost)+"&6."));
			} else if (args[0].equalsIgnoreCase("del")) {
				yip.userData().set(player.getName()+".wp."+wpname+".world", null);
				yip.userData().set(player.getName()+".wp."+wpname+".face", null);
				yip.userData().set(player.getName()+".wp."+wpname+".pos", null);
				yip.userData().set(player.getName()+".wp."+wpname+".yaw", null);
				yip.userData().set(player.getName()+".wp."+wpname+".pitch", null);
				yip.userData().set(player.getName()+".wp."+wpname, null);
				yip.at(player, "&d[&eWP&d] &6Waypoint &e"+wpname+" &4Dereted.");
			} else {
				yip.at(player, "&6/wp           &rList your personal waypoints");
				yip.at(player, "&6/wp help      &rYou're looking at it.");
				yip.at(player, "&6/wp get <wp>  &rGet the coords of a waypoint");
				yip.at(player, "&6/wp set <wp>  &rSet a waypoint to your current position");
				yip.at(player, "&6/wp del <wp>  &rDelete a waypoint");
			}
		}
		return true;
	}
	public boolean kn(Player player, String[] args) {
		if (args.length == 0) {
			if (player == null)
				yip.log.info("Kitsunuki Extension Plugin v"+yip.getDescription().getVersion());		
			else {
				yip.at(player, "Kitsunuki Extension Plugin v"+yip.getDescription().getVersion());
				yip.at(player, "/kn reload -- Reload configuration");
				yip.at(player, "/kn save -- Save configuration");
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!player.hasPermission("kitsunuki.admin")) { yip.at(player, "&4Permission denied."); return true; }
			yip.reloadConfig();
			yip.reloadEnchantConfig();
			yip.reloadUserConfig();
			yip.reloadWorldConfig();
			yip.at(player, "Kitsunuki configs reloaded from disk.");
		} else if (args[0].equalsIgnoreCase("save")) {
			if (!player.hasPermission("kitsunuki.admin")) { yip.at(player, "&4Permission denied."); return true; }
			yip.saveConfig();
			yip.saveEnchantConfig();
			yip.saveUserConfig();
			yip.saveWorldConfig();
			yip.at(player, "Kitsunuki configs dumped to disk.");			
		}
		return true;
	}
	public boolean stack(Player player, String[] args) {
		int count = 64;
		Double cost = yip.getConfig().getDouble("stack.cost", 8);
		if (args.length == 1)
			try {
				count = Integer.valueOf(args[0]);
			} catch (Exception e) {
				count = 64;
			}
		else {
			yip.at(player, "&6/stack <count|all> -- &rCreates overloaded stacks (up to 64 items)");
			yip.at(player, "&aHold the item in your hand then use /stack.  Other items in your inventory will be placed into the stack to the amount given.");
			yip.at(player, "&6Every item above the normal stack size for the item will cost &c"+yip.currency(cost)+"&6.");
			return true;
		}
		if (count < 0 || count > 64) count = 64;
		PlayerInventory inventory = player.getInventory();
		ItemStack give = new ItemStack(inventory.getItemInHand());
		if (give.getType()==Material.EXP_BOTTLE) {
			yip.at(player, "&eCowardly refusing to allow you to stack XP potions to save you from losing the extra XP stored in them.  "+
		                   "To create a new stack of XP potions, it's recommended you drink the potions you want to stack, then create a new "+
					       "stack using &d/xp bottle [amount]&e a few times.");
			return true;
		}
		if (give.getAmount() >= count) {
			yip.at(player, "&6Your stack is already at least that large.");
			return true;
		}
		if (!give.getEnchantments().isEmpty()) {
			yip.at(player, "&4Sorry, you cannot stack items that are enchanted.");
			return true;
		}			
		ItemStack held = new ItemStack(give); held.setAmount(1);
		inventory.setItemInHand(new ItemStack(Material.AIR, 0));
		if (!yip.invContains(inventory, held, 1)) {
			yip.at(player, "&6Could not find items to stack.  Remember the items should be identical.");
			inventory.setItemInHand(give);
			return true;			
		}
		while (yip.invContains(inventory, held, 1)) {
			inventory.removeItem(held);
			give.setAmount(give.getAmount()+1);
			if (give.getAmount() >= count)
				break;
		}
		inventory.setItemInHand(give);
		
		if (give.getAmount() > give.getMaxStackSize())
			cost = (cost*(give.getAmount()-give.getMaxStackSize()));
		else
			cost = 0.0;
		if (cost > 0.0) {
			if (yip.tails.has(player.getName(), cost)) {
				yip.tails.withdrawPlayer(player.getName(), cost);
			} else {
				yip.tails.withdrawPlayer(player.getName(), yip.tails.getBalance(player.getName()));
				cost = yip.tails.getBalance(player.getName());
			}
			yip.at(player, "&6You have overloaded a stack of &e"+give.getAmount()+" &b"+give.getType().toString()+"&6 for &a"+yip.currency(cost));
		} else {
			yip.at(player, "&6You have consolidated a stack of &e"+give.getAmount()+" &b"+give.getType().toString()+"&6.");			
		}
		return true;
	}
	public boolean ww(Player player, String[] args) {
		yip.at(player, "&e==[ &a"+yip.getServer().getOnlinePlayers().length+"&6/&a"+
				yip.getServer().getMaxPlayers()+"&6 players online &e]=="
				);
		for (Player target: yip.getServer().getOnlinePlayers()) {
			if (args.length > 0)
				if (!(target.getName().toLowerCase().contains(args[0].toLowerCase()) ||
					yip.DeRanebo(target.getDisplayName()).toLowerCase().contains(args[0].toLowerCase())))
						continue;
				yip.at(player,
					"&6"+(yip.ess.getUser(target).isAfk() ? "&dAFK    " : "&aACTIVE ")+ 
					"&6"+String.format("&a%16s&6 @ &b%-16s", target.getName(),target.getWorld().getName())+
					"&6 as &e"+target.getDisplayName()+
					""
					);
		}
		return true;
	}
	public boolean roll(Player player, String[] args) {
		String die = "";
		StringBuilder msg = new StringBuilder();
		Random rnd = new Random();
		int dice = 0;
		int count = 0;
		if (args.length == 0) {
			player.sendMessage("/roll ndx[+ndx...[+n...]] -- Roll a dice to the current channel");
			return true;
		} else {
			die = args[0];
		}
		int total = 0;
		int subtotal = 0;
		msg.append("rolls &6");
		String[] rolls = die.split("[+]");
		for (String roll: rolls) {
			try {
				subtotal = 0;
				if (roll.toLowerCase().contains("d")) {
					count = Integer.valueOf(roll.toLowerCase().split("d")[0]);
					dice = Integer.valueOf(roll.toLowerCase().split("d")[1]);
				} else {
					count = 0;
					dice = Integer.valueOf(roll);
				}
			} catch (Exception e) {
				player.sendMessage("There was an error in your dice pool.");
				return true;
			}
			if (count == 0) {
				subtotal = dice;
				count = 1;
				msg.append("&b"+subtotal+"&6+");
			} else {
				if (count < 0 || count > 20)
					count = 1;
				if (dice < 1) dice = 1;
				if (dice > 1000) dice = 1000;
				msg.append("&6"+count+"d"+dice+"&b{");
				for (int j=0; j<count; j++) {
					int x = rnd.nextInt(dice)+1;
					if (x==1) msg.append("&4");
					if (x==dice) msg.append("&a");
					msg.append(x+"&b ");
					subtotal += x;							
				}
				msg.setCharAt(msg.length()-1,'=');
				msg.append(subtotal+"}&e+");						
			}
			total += subtotal;
		}
		msg.setCharAt(msg.length()-1,'=');
		msg.append("&a"+total);
		player.performCommand("me "+msg);
		return true;
	}
	public boolean calc(Player player, String[] args) {
		Evaluator calc = new Evaluator();
		calc.putVariable("e", String.valueOf(Math.E));
		calc.putVariable("pi", String.valueOf(Math.PI));
		calc.putVariable("c", String.valueOf("299792458"));
		calc.putVariable("gm", "1.61803398874989484820458683436563811");
		if (args.length == 0) {
			yip.at(player, "/calc <expression> -- have the server do your math homework");
			return true;
		}
		try {
			yip.at(player, "&a[CALC] &6"+args[0]+"&r = &b"+calc.evaluate((args[0])));
		} catch (Exception e) {
			yip.at(player, "&a[CALC] &4Your calculation cannot be completed as dialed.  "+e.getCause().getMessage());
		}
		return true;
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		boolean handled = false;
		if (sender instanceof Player) {
			player = (Player) sender;
	    }
		if (cmd.getName().equalsIgnoreCase("kn")) {
			handled = kn(player, args);
		}
		if (cmd.getName().equalsIgnoreCase("ww")) {
			handled = ww(player, args);
		}
		if (cmd.getName().equalsIgnoreCase("buy")) {
			handled = true;
			if (player != null)
				handled = this.buy(player, args);
			else
				yip.log.info("What does GOD need with a starship?");				
		}
		if (cmd.getName().equalsIgnoreCase("convert")) {
			handled = true;
			if (player != null)
				handled = this.convert(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("liquidate")) {
			handled = true;
			if (player != null)
				handled = this.liquidate(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("selldrops")) {
			handled = true;
			if (player != null) {
				String[] xx = {"drops"};
				handled = this.liquidate(player, xx);
			} else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("rep")) {
			handled = true;
			if (player != null)
				handled = this.repair(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("xp")) {
			handled = true;
			if (player != null)
				handled = this.xp(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("wp")) {
			handled = true;
			if (player != null)
				handled = this.wp(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		if (cmd.getName().equalsIgnoreCase("roll")) {
			if (player != null)
				handled = this.roll(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");								
		}
		if (cmd.getName().equalsIgnoreCase("calc")) {
			if (player != null)
				handled = this.calc(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");								
		}
		if (cmd.getName().equalsIgnoreCase("stack")) {
			handled = true;
			if (player != null)
				handled = this.stack(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		return handled;
	}

}
