package me.spywhere.Physix;

import org.bukkit.World;
import org.bukkit.entity.Item;

public class DropEvent extends Thread {

	private Physix plugin;
	Item item;
	World world;
	
	protected DropEvent(Physix instance, Item id, World iw)
	{
		plugin=instance;
		item=id;
		world=iw;
	}

	public void run() {
		plugin.autoPlant(item,world);
	}

}
