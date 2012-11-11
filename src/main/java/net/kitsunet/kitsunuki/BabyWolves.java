package net.kitsunet.kitsunuki;

import org.bukkit.World;
import org.bukkit.entity.Player;

// This is for the Kitsunuki periodic scheduler
//NOTE: This is not guaranteed to run at any particular interval.
//      Do not depend on it to keep time.  It is in fact configurable.
//      The default is every 5 seconds.


public class BabyWolves implements Runnable {
	private Kitsune yip;
	private KitsuCommands yerf;
	private TanukiEvents pon;
	
	public Long beat = 0L;
	
	public BabyWolves(Kitsune yip) {
		this.yip = yip;
		this.pon = yip.pon;
		this.yerf = yip.yerf;
	}
	
	public void timeSkew(World wld) {
		//yip.log.info("Timeskew directive: "+wld);
		if (yip.worldData().getString(wld.getName()+".time.from","") == "" || yip.worldData().getString(wld.getName()+".time.to","") == "")
			return;
		long ti = wld.getTime();
		long from = yip.time2t(yip.worldData().getString(wld.getName()+".time.from",""));
		long to = yip.time2t(yip.worldData().getString(wld.getName()+".time.to",""));
		//yip.log.info(wld.toString()+": "+ti+" -- "+from+" -- "+to);
		if (from == to) { // from and to identical times invoke 'at' mode, where time remains the same and days advance every minute.
			wld.setTime(from);
		} else if (from < to) {  
			if (ti > to || ti < from)
				wld.setTime(from);
		} else if (from > to) { 
			if (ti < from && ti > to)   
				wld.setTime(from);
		}
	}

	@Override
	public void run() { 
		for (Player player: yip.getServer().getOnlinePlayers()) {
			if (pon.lastkilltime.get(player) == null) continue;
			if (pon.lastkillcount.get(player) <= 5) continue;
			if (pon.lastkilltime.get(player)+(yip.getConfig().getLong("combo.time",10)*1000) < System.currentTimeMillis()) {
				yip.bc("&b["+player.getDisplayName()+"] &c"+
						yip.getConfig().getString("combo.msg.final", "Final combo #!").replace("#", pon.lastkillcount.get(player).toString()),"Grind");
				pon.lastkillcount.put(player, 0);
			}				
		}
		for (World wld: yip.getServer().getWorlds()) {
			if (yip.worldData().isConfigurationSection(wld.getName()))
				timeSkew(wld);
		}
		beat++;
	}
}
