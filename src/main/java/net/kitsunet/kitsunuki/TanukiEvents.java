package net.kitsunet.kitsunuki;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Joiner;

public class TanukiEvents implements Listener {
	private Kitsune yip;
	private KitsuCommands yerf;
	private TanukiEvents pon;
	
	public HashMap<Player,Long> lastkilltime;
	public HashMap<Player,Integer> lastkillcount;

	
	public TanukiEvents(Kitsune yip) {
		this.yip = yip;
		this.yerf = yip.yerf;
		this.pon = yip.pon;
		this.lastkillcount = new HashMap<Player,Integer>();
		this.lastkilltime = new HashMap<Player,Long>();		
	}
	/*
	@EventHandler(priority = EventPriority.MONITOR)
    public void chatWatcher(AsyncPlayerChatEvent event) {
		yip.log.info(
				"Kitsunuki "+
				event.getEventName()+" "+
				event.getPlayer()+" "+
				Joiner.on(" ").join(event.getRecipients())+" "+
				event.getFormat()+" "+
				event.getMessage()
				);
		return;
	}
	*/
	@EventHandler
	public void welcome(PlayerLoginEvent event) {
		if (!yip.getServer().getOnlineMode())
			yip.at(event.getPlayer(),
					yip.getConfig().getString("config.offlinemsg", 
							"&4The server is currently in OFFLINE mode, probably due to minecraft.net being down."+
							"  Your skin will not be loaded and we will restart to go back into ONLINE mode when minecraft.net is back up."
					));
	}
	
	@EventHandler
	public void HurtMePlenty(EntityRegainHealthEvent event) {
		if (event.getRegainReason() == RegainReason.SATIATED)
			if (yip.worldData().getBoolean(event.getEntity().getWorld().getName()+".noregen.food", false))
					event.setCancelled(true);
	}
	
	@EventHandler
	public void blockDrops(BlockBreakEvent event) {
		Block busted = event.getBlock();
		ItemStack item = new ItemStack(Material.AIR,0);
		String take = busted.getType().toString().toLowerCase();
		String give = yip.getConfig().getString("drops."+take+".to", "").toLowerCase();
		if (give != "") {
			PlayerInventory inventory = event.getPlayer().getInventory();
			try {
				item = yip.ess.getItemDb().get(
						yip.getConfig().getString("drops."+take+".to","0"),
						yip.getConfig().getInt("drops."+take+".factor",1));
			} catch (Exception e) {
				yip.log.info("config.yml: drops."+yip.getConfig().getString("drops."+take+".to")+".to; couldn't resolve item ID");
			}
			if (item.getType() != Material.AIR)
				inventory.addItem(item);
		}
	//yip.log.info("broke: "+busted.toString()+" to: "+item.toString()+" take: "+take+"   give: "+give);
	}
	@EventHandler
	public void Grieve(PlayerDeathEvent event) {
		if (event.getEntity() == null) return;
		if (event.getEntity().getKiller() == null) return;
		if (event.getEntityType() == EntityType.PLAYER) { // This is an MDK
			yip.bc("&c"+event.getEntity().getDisplayName()+"&4 was murdered by &c"+event.getEntity().getKiller().getDisplayName()+"&4!", "MDK");
		}			
	}
	@EventHandler
	public void DeathHandler(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.WOLF) {
			// Killing a fox doesn't continue your combo, lol
			event.setDroppedExp(0);
			event.getEntity().getKiller().setExp(0);
			yip.xp.adjXP(event.getEntity().getKiller(),0); // this just updates the display..
			this.lastkillcount.put(event.getEntity().getKiller(), 0);
			yip.at(event.getEntity().getKiller(), "&4[OMG] You killed a fox!  The fox steals your level and breaks your combo!");
			return;
		}
		Player player = event.getEntity().getKiller();
		if (player == null)
			return;
		Long killtime = yip.getConfig().getLong("kill.time", 10);
		Integer killcount = 0;
		if (lastkilltime.get(player) == null) lastkilltime.put(player, System.currentTimeMillis());
		if (lastkillcount.get(player) != null) killcount = lastkillcount.get(player);
		if (lastkilltime.get(player) > System.currentTimeMillis()-(killtime*1000)) {
			killcount++;
			if (killcount < 3) {
				// do nothing
			} else if (killcount == 3) {
				yip.bc("&b["+player.getDisplayName()+"] &c"+yip.getConfig().getString("combo.msg.tri", "Triple kill!").replace("#", killcount.toString()),"Grind");
			} else if (killcount == 4) {
				yip.bc("&b["+player.getDisplayName()+"] &c"+yip.getConfig().getString("combo.msg.quad", "Quadra kill!").replace("#", killcount.toString()),"Grind");
			} else if (killcount == 5) {
				yip.bc("&b["+player.getDisplayName()+"] &c"+yip.getConfig().getString("combo.msg.spree", "Killing spree! (#)").replace("#", killcount.toString()),"Grind");
			} else if (killcount >= 5 && ((killcount % 5) == 0)) {
				yip.bc("&b["+player.getDisplayName()+"] &c"+yip.getConfig().getString("combo.msg.combo", "# combo!").replace("#", killcount.toString()),"Grind");
			}
		} else {
			if (lastkillcount.get(player) > 5)
				yip.bc("&b["+player.getDisplayName()+"] &c"+
					yip.getConfig().getString("combo.msg.final", "Final combo #!").replace("#", lastkillcount.get(player).toString()),"Grind");
			killcount = 1;
		}
		lastkilltime.put(player, System.currentTimeMillis());
		lastkillcount.put(player, killcount);
		// Extra XP for combos
		/*
		if (killcount/(event.getDroppedExp() <= 0 ? 1 : event.getDroppedExp()) > event.getDroppedExp()) {
			event.setDroppedExp(killcount/(event.getDroppedExp() <= 0 ? 1 : event.getDroppedExp()));
			if (event.getDroppedExp()>10) event.setDroppedExp(10);
		}
		*/
		//yip.log.info("PreAdjust -- "+player.getName()+" killed "+event.getEntityType()+" time "+lastkilltime.get(player)+
		//		" count "+lastkillcount.get(player)+" dropped "+event.getDroppedExp());		
		event.setDroppedExp(event.getDroppedExp()+(killcount/5));
		//yip.log.info("PostAdjust -- "+player.getName()+" killed "+event.getEntityType()+" time "+lastkilltime.get(player)+
		//		" count "+lastkillcount.get(player)+" dropped "+event.getDroppedExp());		
		return;
	}
	@EventHandler
	public void LevNotify(PlayerLevelChangeEvent event) {
		if (event.getNewLevel() > event.getOldLevel())
			yip.bc("&a"+event.getPlayer().getDisplayName()+"&a leveled up to level &d"+event.getNewLevel()+"&a!", "Level");
		else if (event.getNewLevel() < event.getOldLevel())
			yip.bc("&c"+event.getPlayer().getDisplayName()+"&c de-leveled to level &d"+event.getNewLevel()+"&c!", "Level");
		else
			yip.bc("&e"+event.getPlayer().getDisplayName()+"&c changed to level &d"+event.getNewLevel()+"&c!", "Level");						
	}
	@EventHandler
	public void XPNotify(PlayerExpChangeEvent event) {
		Player player = event.getPlayer();
		int xpa = event.getAmount()+(yip.getConfig().getInt("xp.adjust."+player.getWorld().getName(), 0));
		//yip.log.info(player.getName()+"@"+player.getWorld().getName()+": "+xpa);
		event.setAmount(xpa);
		Integer xp = yip.xp.getTotalExperience(player)+event.getAmount();
		Integer tolevel = player.getExpToLevel()-((Integer)Math.round((player.getExp()*player.getExpToLevel())+event.getAmount()));
		Integer level = player.getLevel();
		if (tolevel < 0) {
			level++;
			tolevel=tolevel+player.getExpToLevel();
		}
		if (yip.userData().getBoolean(event.getPlayer().getName()+".xp.reporting"))
			yip.at(player, "&d[&eXP&d] &r"+player.getDisplayName()+"&6 got +&b"+event.getAmount()+"&6xp (&b"+xp.toString()+"&6xp/L&b"+level.toString()+"&6), &b"+
					tolevel.toString()+"&6xp to next level.");
	}	
	@EventHandler
	public void deCreep(EntityExplodeEvent event) {
		if (event.isCancelled() || event.getEntity() == null)
			return;
		if (yip.worldData().getBoolean(event.getEntity().getWorld().getName()+".blockdmg.creeper",false)) {
			if (event.getEntityType() == EntityType.CREEPER)
				event.setCancelled(true);
				event.getLocation().getWorld().createExplosion(event.getLocation(), 0F);
				//yip.log.info("Creeper damage cancelled in "+event.getEntity().getWorld().getName()+" "+event.getEntity().getLocation());
		}
	}
	@EventHandler
	public void deEnder(EntityChangeBlockEvent event) {
		if (event.isCancelled() || event.getEntity() == null)
			return;
		if (yip.worldData().getBoolean(event.getEntity().getWorld().getName()+".blockdmg.enderman",false)) {
			if (event.getEntityType() == EntityType.ENDERMAN)
				event.setCancelled(true);
				//yip.log.info("Ender damage cancelled in "+event.getEntity().getWorld().getName()+" "+event.getEntity().getLocation());
		}
	}
	@EventHandler
	public void unStackExpBottle(PlayerPickupItemEvent event) {
		if (event.getItem().getItemStack().getType() == Material.EXP_BOTTLE)
			event.getPlayer().getInventory().setMaxStackSize(1);
		else {
			if (event.getPlayer().getInventory().getMaxStackSize() != 64)
					event.getPlayer().getInventory().setMaxStackSize(64);
		}
	}
	// All of the inventory related stuff is to prevent EXP bottles from being stacked.
	@EventHandler
	public void InvClick(InventoryClickEvent event) {
		// Quickly return if this isn't our event.
		if (event.getCursor() == null || event.getCurrentItem() == null)
			return;
		if (!(event.getCursor().getType() == Material.EXP_BOTTLE || event.getCurrentItem().getType() == Material.EXP_BOTTLE))
			return;
		if (event.getCurrentItem().getType() == Material.EXP_BOTTLE &&
				event.getCursor().getType() == Material.EXP_BOTTLE) {
			if (event.getCurrentItem().getDurability() != event.getCursor().getDurability()) {
				event.setResult(Event.Result.DENY);
				event.setCancelled(true);
			}
		} 
		if (event.isShiftClick() && (event.getCurrentItem().getType()==Material.EXP_BOTTLE)) {
			event.setResult(Event.Result.DENY);
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void InvOpen(InventoryOpenEvent event) {
		if (event.getPlayer().getInventory().getMaxStackSize() != 64)
			event.getPlayer().getInventory().setMaxStackSize(64);
	}
	@EventHandler
	public void InvClose(InventoryCloseEvent event) {
		if (event.getPlayer().getInventory().getMaxStackSize() != 64)
			event.getPlayer().getInventory().setMaxStackSize(64);
	}

	// Exp bottles are precious and should be used, not smashed.
	@EventHandler
	public void useXPPotion(PlayerInteractEvent event) {
		if (!event.hasItem())
			return;
		if (event.getItem().getType() != Material.EXP_BOTTLE)
			return;
		if (event.hasBlock()) {
			event.setUseItemInHand(Result.DENY);
			return;
		}
		event.setCancelled(true);
		String[] temp = {"drink"};
		yerf.xp(event.getPlayer(),temp);
	}
}