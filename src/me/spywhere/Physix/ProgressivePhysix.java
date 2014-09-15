package me.spywhere.Physix;

import org.bukkit.World;
import org.bukkit.block.Block;

public class ProgressivePhysix extends Thread {

	private Physix plugin;
	Block block;
	World world;
	boolean bypass;
	
	protected ProgressivePhysix(Physix instance,Block block,World world,boolean bypass)
	{
		plugin=instance;
		this.block=block;
		this.world=world;
		this.bypass=bypass;
	}
		
	public void run() {
		plugin.checkPhysix(block,world,bypass);
	}

}
