package net.kitsunet.kitsunuki;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TanukiEvents implements Listener {
	private Kitsune yip;
	private KitsuCommands yerf;
	 
	public TanukiEvents(Kitsune yip) {
		this.yip = yip;
		this.yerf = yip.yerf;
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
	public void XPNotify(PlayerExpChangeEvent event) {
		Player player = event.getPlayer();
		int xpa = event.getAmount()+(yip.getConfig().getInt("xp.adjust."+player.getWorld().getName(), 0));
		//yip.log.info(player.getName()+"@"+player.getWorld().getName()+": "+xpa);
		event.setAmount(xpa);
		Integer xp=yip.xp.getTotalExperience(player)+event.getAmount();
		Integer tolevel=player.getExpToLevel()-((Integer)Math.round((player.getExp()*player.getExpToLevel())+event.getAmount()));
		Integer level=player.getLevel();
		if (tolevel < 0) {
			level++;
			tolevel=tolevel+player.getExpToLevel();
		}
		if (yip.userData().getBoolean(event.getPlayer().getName()+".xp.reporting"))
			yip.at(player, "&d[&eXP&d] &r"+player.getDisplayName()+"&6 has &b"+xp.toString()+"&6xp (L&b"+level.toString()+"&6), &b"+
					tolevel.toString()+"&6xp to next level.");
	}	
	@EventHandler
	public void deCreep(EntityExplodeEvent event) {
		if (event.isCancelled() || event.getEntity() == null)
			return;
		if (!(yip.worldData().getBoolean(event.getEntity().getWorld().getName()+".creeperblockdmg",true))) {
			if (event.getEntityType() == EntityType.CREEPER)
				event.setCancelled(true);
				event.getLocation().getWorld().createExplosion(event.getLocation(), 0F);
				//yip.log.info("Creeper damage cancelled in "+event.getEntity().getWorld().getName()+" "+event.getEntity().getLocation());
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
	@EventHandler
	public void InvClick(InventoryClickEvent event) {
		// Quickly return if this isn't our event.
		if (event.getCursor() == null || event.getCurrentItem() == null)
			return;
		if (!(event.getCursor().getType() == Material.EXP_BOTTLE || event.getCurrentItem().getType() == Material.EXP_BOTTLE))
			return;
		if (event.getCurrentItem().getType() == Material.EXP_BOTTLE &&
				event.getCursor().getType() == Material.EXP_BOTTLE) {
			event.setResult(Event.Result.DENY);
			event.setCancelled(true);
		} 
		if (event.isShiftClick() && (event.getCurrentItem().getType()==Material.EXP_BOTTLE)) {
			event.setResult(Event.Result.DENY);
			event.setCancelled(true);
		}
		// Prevent stacking exp bottles.
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