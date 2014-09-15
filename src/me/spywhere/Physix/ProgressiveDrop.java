package me.spywhere.Physix;

import org.bukkit.World;
import org.bukkit.block.Block;

public class ProgressiveDrop extends Thread {

	private Physix plugin;
	Block block;
	World world;
	
	protected ProgressiveDrop(Physix instance,Block block,World world)
	{
		plugin=instance;
		this.block=block;
		this.world=world;
	}
		
	public void run() {
		plugin.applyStepPhysix(block,world);
	}

}
