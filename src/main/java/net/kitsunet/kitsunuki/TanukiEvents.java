package net.kitsunet.kitsunuki;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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
}
