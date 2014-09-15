package me.spywhere.Physix;

import org.bukkit.World;
import org.bukkit.block.Block;

public class EventThread extends Thread {

	private Physix plugin;
	Block block;
	World world;
	
	protected EventThread(Physix instance, Block ib, World iw)
	{
		plugin=instance;
		block=ib;
		world=iw;
	}
		
	public void run() {
		plugin.checkBreak(block,world,false);
	}

}
