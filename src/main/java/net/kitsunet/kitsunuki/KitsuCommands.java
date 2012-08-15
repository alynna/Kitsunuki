package net.kitsunet.kitsunuki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

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
			int mode=0;
			String want = args[0];
			if (args[0].equalsIgnoreCase("price")) {
				mode = 1; want = args[1];
			}
			// lets do some buying...
			String item = yip.getConfig().getString("buy."+want+".item");
			Material itemid = Material.AIR;
			Double itemcost = yip.getConfig().getDouble("buy."+want+".for",1.0);
			Integer itemcount = yip.getConfig().getInt("buy."+want+".count",1);
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
				yip.at(player,"The item '"+conv+"' cannot completed as dialed.");
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
					count = 65535;
				else if (args[1].equalsIgnoreCase("hand"))
					count = -1;
				else if (Integer.parseInt(args[1]) > 0)
					count = Integer.parseInt(args[1]);
				else count = 0;
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
					if (!inventory.contains(fromid, take)) break;
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
				yip.xp.adjXP(player, -(xp));
				yip.tails.withdrawPlayer(player.getName(), cost);
				yip.at(player, response+" Item repaired!");
			} else {
				yip.at(player, response+" &6use &e/rep c&6 to do the repair.");				
			}
		}
		return true;
	}
	public boolean selldrops(Player player, String[] args) {
		List<String> items = yip.getConfig().getStringList("selldrops");
		PlayerInventory inventory = player.getInventory();
		if (args.length == 0) { // normal command to sell all drops without parameters
			Double total = 0.0;
			Double prev = yip.tails.getBalance(player.getName());
			String x = "";
			ItemStack itemstack = new ItemStack(Material.AIR,1);
			for (String item: items) {
				try {
					if (yip.ess.getItemDb().get(item) != null) {
						itemstack = yip.ess.getItemDb().get(item);
						itemstack.setAmount(1);
					} else continue;
				} catch (Exception e) {
					continue;
				}
				Integer count = 0;
				Double subtotal = 0.0;
				while (inventory.contains(itemstack.getType(), 1)) {
					inventory.removeItem(itemstack);
					yip.tails.depositPlayer(player.getName(), yip.ess.getWorth().getPrice(itemstack));
					count++; subtotal += yip.ess.getWorth().getPrice(itemstack);
					total += yip.ess.getWorth().getPrice(itemstack);
				}
				if (count>0) yip.at(player,"&6Sold &e"+count.toString()+" &b"+itemstack.getType().toString().toLowerCase()+"&6 for &a"+yip.currency(subtotal));						
			}
			yip.at(player,"&6Grand total: &a"+yip.currency(total)+"&6 earned [&a"+yip.currency(prev)+"&6 -> &a"+
					yip.currency(yip.tails.getBalance(player.getName()))+"&6]");						
		} else {
			String x = "";
			for (String item: items) {
				try {
					if (!(yip.ess.getItemDb().get(item) == null))
						x += yip.ess.getItemDb().get(item).getType().toString().toLowerCase()+" ";
					else
						yip.log.info("Item "+item+" failed to be looked up.");
				} catch (Exception e) {
					yip.log.severe(e.toString());
				}
			}
			yip.at(player, "&6Liquidatable drops: &b"+x);
		}
		return true;
	}
	public boolean xp(Player player, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				yip.userData().set(player.getName()+".xp.reporting", true);
				yip.at(player, "&d[&eXP&d] XP reporting is now &aON&r.");
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				yip.userData().set(player.getName()+".xp.reporting", false);
				yip.at(player, "&d[&eXP&d] XP reporting is now &aOFF&r.");				
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
						yip.dbl2(pos.getX())+", "+yip.dbl2(pos.getY())+", "+yip.dbl2(pos.getZ())+
						"] &6in &a"+wld.getName()+"&6.");
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
				yip.log.info("Kitsunuki Extension Plugin v1.5");		
			else {
				yip.at(player, "Kitsunuki Extension Plugin v1.5");
				yip.at(player, "/kn reload -- Reload configuration");
				yip.at(player, "/kn save -- Save configuration now");
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			yip.reloadConfig();
			yip.reloadEnchantConfig();
			yip.reloadUserConfig();
			yip.reloadWorldConfig();
			yip.at(player, "Kitsunuki configs reloaded from disk.");
		} else if (args[0].equalsIgnoreCase("save")) {
			yip.saveConfig();
			yip.saveEnchantConfig();
			yip.saveUserConfig();
			yip.saveWorldConfig();
			yip.at(player, "Kitsunuki configs dumped to disk.");			
		}
		return true;
	}
	public boolean tps(Player player, String[] args) {
		return false;
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
		if (cmd.getName().equalsIgnoreCase("selldrops")) {
			handled = true;
			if (player != null)
				handled = this.selldrops(player, args);
			else
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
		if (cmd.getName().equalsIgnoreCase("tps")) {
			handled = true;
			if (player != null)
				handled = this.wp(player, args);
			else
				yip.log.info("What does GOD need with "+cmd.getName()+"?");				
		}
		return handled;
	}

}
