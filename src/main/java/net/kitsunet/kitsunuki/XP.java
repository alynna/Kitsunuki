package net.kitsunet.kitsunuki;
// From essentials: correct calculation of XP hopefully

import org.bukkit.entity.Player;
public class XP
{
	//This method is used to update both the recorded total experience and displayed total experience.
	//We reset both types to prevent issues.
	private Kitsune yip;	
	public XP(Kitsune yip) {
		this.yip = yip;
	}

	public void setTotalExperience(Player player, int exp)
	{
		if (exp < 0)
		{
			exp = 0;
		}
		player.setExp(0);
		player.setLevel(0);
		player.setTotalExperience(0);

		//This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
		//At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
		int amount = exp;
		while (amount > 0)
		{
			final int expToLevel = getExpAtLevel(player);
			amount -= expToLevel;
			if (amount >= 0)
			{
				// give until next level
				player.giveExp(expToLevel);
			}
			else
			{
				// give the rest
				amount += expToLevel;
				player.giveExp(amount);
				amount = 0;
			}
		}
	}
	
	private int getExpAtLevel(final Player player)
	{
		return getExpAtLevel(player.getLevel());
	}

	public int getExpAtLevel(final int level)
	{
		if (level > 29)
		{
			return 62 + (level - 30) * 7;
		}
		if (level > 15)
		{
			return 17 + (level - 15) * 3;
		}
		return 17;
	}

	public int getExpToLevel(final int level)
	{
		int currentLevel = 0;
		int exp = 0;

		while (currentLevel < level)
		{
			exp += getExpAtLevel(currentLevel);
			currentLevel++;
		}
		return exp;
	}

	//This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
	//Without this people would be able to use exp and then still sell it.
	public int getTotalExperience(final Player player)
	{
		int exp = (int)Math.round(getExpAtLevel(player) * player.getExp());
		int currentLevel = player.getLevel();

		while (currentLevel > 0)
		{
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		}
		return exp;
	}

	public int getExpUntilNextLevel(final Player player)
	{
		int exp = (int)Math.round(getExpAtLevel(player) * player.getExp());		
		int nextLevel = player.getLevel();
		return getExpAtLevel(nextLevel) - exp;
	}
	public void adjustTotalExperience(final Player player, final int exp) {
		setTotalExperience(player, getTotalExperience(player)+exp);
		return;
	}
	public void adjXP(final Player player, final int exp) {
		adjustTotalExperience(player, exp);
		return;
	}
	public void setXP(final Player player, final int exp) {
		setTotalExperience(player, exp);
		return;
	}
	public int getXP(final Player player) {
		return getTotalExperience(player);
	}
 	public int xp2level(int xp) {
 		int level = 0;
 		int xpc = 17;
 		while (xpc < xp) {
 			if (level < 17) 
 				xpc += 17;
 			else
 				xpc += (17+(level-16)*3);
 			level++;
 		}
 		return level;
 	}

}