package net.kitsunet.kitsunuki;

import org.bukkit.World;

// This is for the 1 minute scheduler

public class BabyWolves implements Runnable {
	private Kitsune yip;
	private KitsuCommands yerf;
	private TanukiEvents pon;
	
	public Integer min = 0;
	
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
		//yip.log.info("BabyWolves entered.");
		for (World wld: yip.getServer().getWorlds()) {
			if (yip.worldData().isConfigurationSection(wld.getName()))
				timeSkew(wld);
		}
		min++;
	}
}
