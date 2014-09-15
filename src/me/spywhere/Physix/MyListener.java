package me.spywhere.Physix;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MyListener implements Listener {
	public static Physix plugin;

	public MyListener(Physix instance) {
		plugin = instance;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(!event.isCancelled()){
			plugin.checkTree(event.getBlock(),event.getPlayer().getWorld());
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EventThread(plugin, event.getBlock(),event.getBlock().getWorld()), 0);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(!event.isCancelled()){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PhysixPlace(plugin,event.getBlockPlaced(),event.getBlockPlaced().getWorld()),0);
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event){
		if(!event.isCancelled()){
			if(plugin.entityphysix){
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EventThread(plugin, event.getBlock(),event.getBlock().getWorld()), 0);
			}
		}
	}

	//@EventHandler
	//public void onPlayerBukketFill(PlayerBucketFillEvent event){
	//plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EventThread(plugin, event.getBlockClicked(),event.getPlayer().getWorld()), 0);
	//event.getPlayer().sendMessage("Fill");
	//}

	//@EventHandler
	//public void onPlayerBukketEmpty(PlayerBucketEmptyEvent event){
	//plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EventThread(plugin, event.getBlockClicked(),event.getPlayer().getWorld()), 0);
	//event.getPlayer().sendMessage("Empty");
	//}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if(!event.isCancelled()){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DropEvent(plugin, event.getItemDrop(),event.getItemDrop().getWorld()), plugin.plantdelay);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction()==Action.LEFT_CLICK_BLOCK){
			if(event.getPlayer().getName().equalsIgnoreCase(plugin.creator)){
				//Create area
				if(plugin.cubeCreated==2||plugin.cubeCreated==4){
					if(plugin.cubeCreated==2){
						plugin.areaWorld.put(plugin.cubeName.toLowerCase(), event.getPlayer().getWorld().getName());
						plugin.areaLoc1.put(plugin.cubeName.toLowerCase(), plugin.str2loc(plugin.cubeLoc1));
						plugin.areaLoc2.put(plugin.cubeName.toLowerCase(), event.getClickedBlock().getLocation());
						event.getPlayer().sendMessage(ChatColor.GREEN+"Area "+ChatColor.AQUA+"\""+ChatColor.YELLOW+plugin.cubeName+ChatColor.AQUA+"\""+ChatColor.GREEN+" created.");
						plugin.creator="";
					}
					if(plugin.cubeCreated==4){
						event.getPlayer().sendMessage(ChatColor.GREEN+"Selected area will now apply physic to it. Expecting some lags...");
						plugin.instantArea(plugin.str2loc(plugin.cubeLoc1), event.getClickedBlock().getLocation(),event.getPlayer());
						plugin.creator="";
					}
					plugin.cubeCreated=0;
				}
				if(plugin.cubeCreated==1||plugin.cubeCreated==3){
					plugin.cubeLoc1=plugin.loc2str(event.getClickedBlock().getLocation());
					if(plugin.cubeCreated==1){
						plugin.cubeCreated=2;
					}
					if(plugin.cubeCreated==3){
						plugin.cubeCreated=4;
					}
					event.getPlayer().sendMessage(ChatColor.AQUA+"Left Click 2nd corner...");
				}
			}
		}
		if(event.getAction()==Action.RIGHT_CLICK_BLOCK||event.getAction()==Action.RIGHT_CLICK_AIR){
			if(event.getPlayer().getName().equalsIgnoreCase(plugin.creator)){
				//Cancel creation
				if(plugin.cubeCreated!=0){
					plugin.cubeCreated=0;
					event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"Area "+ChatColor.AQUA+"\""+ChatColor.YELLOW+plugin.cubeName+ChatColor.AQUA+"\""+ChatColor.LIGHT_PURPLE+" cancelled.");
					plugin.creator="";
				}
			}
		}
	}

}
