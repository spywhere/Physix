package me.spywhere.Physix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Physix extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	PluginDescriptionFile pdf = this.getDescription();
	//Per world
	public final HashMap<Integer, String> worldName = new HashMap<Integer, String>();
	public final HashMap<Integer, Boolean> worldApply = new HashMap<Integer, Boolean>();
	//Per world settings?
	//public final HashMap<String,Boolean> worldTreeCut = new HashMap<String, Boolean>();
	//public final HashMap<String,Boolean> worldAutoPlant = new HashMap<String, Boolean>();
	//public final HashMap<String,Integer> worldPlantDelay = new HashMap<String, Integer>();
	//public final HashMap<String,Integer> worldMinimumConnected = new HashMap<String, Integer>();
	//public final HashMap<String,Integer> worldCheckRadius = new HashMap<String, Integer>();
	//Per area
	public final HashMap<String, String> areaWorld = new HashMap<String, String>();
	public final HashMap<String, Location> areaLoc1 = new HashMap<String, Location>();
	public final HashMap<String, Location> areaLoc2 = new HashMap<String, Location>();
	String pcloc1 = "";
	String pcloc2 = "";
	Player pcplayer = null;
	String ignorelist = "0:8:9:10:11:104:105:59:6:333:328:92:32:51:90:111:106:78:79";   //Another block can drop to these blocks
	String droppedlist = "63:68:321:50:76:75:55:104:105:59:6:333:328:354:77:69";   //Another block can drop to these blocks and it will drop
	String nonphysixlist = "63:68:321:50:75:76:55:69:77:79";   //No physic apply to these block
	String glueblocklist = "30"; //Cobweb
	String areafile = "Area.txt";
	int cubeCreated = 0;
	String cubeName = "";
	String cubeLoc1 = "";
	String creator = "";
	boolean confirm = false;
	boolean wait = false;
	//	boolean treecut=true;
	//	boolean autoplant=true;
	boolean entityphysix = false; //Beta -> Still have error
	boolean progressive = false;
	boolean progressivePhysix = false;
	//	int plantdelay=10;
	int minimumconnected = 2;
	int checkradius = 15;
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new MyListener(this), this);
		pdf = this.getDescription();
		log.info("[" + pdf.getName() + "] v" + pdf.getVersion() + " successfully enabled.");
		initVars();
	}
	
	public Location str2loc(String str) {
		Location loc = new Location(this.getServer().getWorlds().get(0), 0, 0, 0);
		String str2loc[] = str.split("\\:");
		loc.setX(Double.parseDouble(str2loc[0]));
		loc.setY(Double.parseDouble(str2loc[1]));
		loc.setZ(Double.parseDouble(str2loc[2]));
		return loc;
	}
	
	public String loc2str(Location loc) {
		return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
	}
	
	private void initVars() {
		if(!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		File conf = new File(this.getDataFolder().toString(), "config.yml");
		FileConfiguration myConfig = this.getConfig();
		if(conf.exists()){
			try{
				myConfig.load(conf);
				progressive = myConfig.getBoolean("Physix.ProgressivePhysix", progressive);
				minimumconnected = myConfig.getInt("Physix.MinimumBlockConnected");
				checkradius = myConfig.getInt("Physix.CheckRadius", checkradius);
				entityphysix = myConfig.getBoolean("Physix.CheckEntity", entityphysix);
				glueblocklist = myConfig.getString("Physix.GlueBlockList", glueblocklist);
				//				treecut = myConfig.getBoolean("Physix.TreeCut",treecut);
				//				autoplant = myConfig.getBoolean("Physix.AutoPlant",autoplant);
				//				plantdelay = myConfig.getInt("Physix.PlantDelay",plantdelay);
				droppedlist = myConfig.getString("Physix.DroppedBlockList", droppedlist);
				ignorelist = myConfig.getString("Physix.IgnoredBlockList", ignorelist);
				nonphysixlist = myConfig.getString("Physix.NonPhysixBlockList", nonphysixlist);
				//Load world
				if(myConfig.contains("Physix.ApplyWorlds")){
					Set<String> keyl = myConfig.getConfigurationSection("Physix.ApplyWorlds").getKeys(false);
					if(keyl.size() > 0){
						Object[] key = keyl.toArray();
						for(int i = 0;i < key.length;i++){
							worldName.put(i, (String) key[i]);
							worldApply.put(i, myConfig.getBoolean("Physix.ApplyWorlds." + (String) key[i]));
						}
					}
				}
				int total = loadArea();
				if(total > 0){
					log.info("[" + pdf.getName() + "] Total " + total + " areas loaded.");
				}
			}catch (FileNotFoundException e){
				log.info("Error Occured: onInitVars|FileNotFoundException|" + e.getMessage());
			}catch (IOException e){
				log.info("Error Occured: onInitVars|IOException|" + e.getMessage());
			}catch (InvalidConfigurationException e){
				log.info("Error Occured: onInitVars|InvalidConfigurationException|" + e.getMessage());
			}
		}
		myConfig.set("Physix.ProgressivePhysix", progressive);
		myConfig.set("Physix.MinimumBlockConnected", minimumconnected);
		myConfig.set("Physix.CheckRadius", checkradius);
		myConfig.set("Physix.CheckEntity", entityphysix);
		myConfig.set("Physix.GlueBlockList", glueblocklist);
		myConfig.set("Physix.DroppedBlockList", droppedlist);
		myConfig.set("Physix.IgnoredBlockList", ignorelist);
		myConfig.set("Physix.NonPhysixBlockList", nonphysixlist);
		//		myConfig.set("Physix.TreeCut",treecut);
		//		myConfig.set("Physix.AutoPlant",autoplant);
		//		myConfig.set("Physix.PlantDelay",plantdelay);
		for(int i = 0;i < getServer().getWorlds().size();i++){
			if(!myConfig.contains("Physix.ApplyWorlds." + getServer().getWorlds().get(i).getName())){
				myConfig.set("Physix.ApplyWorlds." + getServer().getWorlds().get(i).getName(), false);
				worldName.put(worldName.size(), getServer().getWorlds().get(i).getName());
				worldApply.put(worldName.size(), false);
			}
		}
		try{
			myConfig.save(conf);
		}catch (IOException e){
			log.info("Error Occured: onInitVars|IOException|" + e.getMessage());
		}
	}
	
	private int loadArea() {
		File conf = new File(getDataFolder().toString(), areafile);
		int i, total = 0;
		if(conf.exists()){
			FlatFile config = new FlatFile(conf.getPath(), this);
			total = config.getInt("t");
			for(i = 0;i < total;i++){
				String key = config.getString(i + "n");
				areaWorld.put(key.toLowerCase(), config.getString(i + "w"));
				areaLoc1.put(key.toLowerCase(), str2loc(config.getString(i + "l1")));
				areaLoc2.put(key.toLowerCase(), str2loc(config.getString(i + "l2")));
			}
		}
		return total;
	}
	
	private int saveArea() {
		if(!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		if(!areaWorld.isEmpty()){
			File conf = new File(this.getDataFolder().toString(), areafile);
			if(conf.exists()){
				conf.delete();
			}
			int i;
			FlatFile config = new FlatFile(conf.getPath(), this);
			config.setNumber("t", areaWorld.size());
			ArrayList<String> keys = new ArrayList<String>(areaWorld.keySet());
			for(i = 0;i < keys.size();i++){
				String id = keys.get(i);
				config.setString(i + "n", id.toLowerCase());
				config.setString(i + "w", areaWorld.get(id));
				config.setString(i + "l1", loc2str(areaLoc1.get(id)));
				config.setString(i + "l2", loc2str(areaLoc2.get(id)));
			}
			config.save();
			return areaWorld.size();
		}
		return 0;
	}
	
	public void onDisable() {
		int total = saveArea();
		if(total > 0){
			log.info("[" + pdf.getName() + "] Total " + total + " areas saved.");
		}
		log.info("[" + pdf.getName() + "] v" + pdf.getVersion() + " successfully disabled.");
	}
	
	//
	//	public void autoPlant(Item drop,World world){
	//		if((drop.getItemStack().getType()==Material.SEEDS||drop.getItemStack().getType()==Material.PUMPKIN_SEEDS||drop.getItemStack().getType()==Material.MELON_SEEDS)&&autoplant&&(isApplyWorld(world)||isApplyArea(drop.getLocation(),world))){
	//			if(world.getBlockAt(drop.getLocation().getBlockX(), drop.getLocation().getBlockY()-1, drop.getLocation().getBlockZ()).getType()==Material.SOIL){
	//				if(world.getBlockAt(drop.getLocation()).getType()==Material.AIR){
	//					if(drop.getItemStack().getType()==Material.SEEDS){world.getBlockAt(drop.getLocation()).setType(Material.CROPS);drop.remove();}
	//					if(drop.getItemStack().getType()==Material.PUMPKIN_SEEDS){world.getBlockAt(drop.getLocation()).setType(Material.PUMPKIN_STEM);drop.remove();}
	//					if(drop.getItemStack().getType()==Material.MELON_SEEDS){world.getBlockAt(drop.getLocation()).setType(Material.MELON_STEM);drop.remove();}
	//				}
	//			}
	//		}
	//
	//		if(drop.getItemStack().getType()==Material.SAPLING&&autoplant&&(isApplyWorld(world)||isApplyArea(drop.getLocation(),world))){
	//			if(world.getBlockAt(drop.getLocation().getBlockX(), drop.getLocation().getBlockY()-1, drop.getLocation().getBlockZ()).getType()==Material.DIRT||world.getBlockAt(drop.getLocation().getBlockX(), drop.getLocation().getBlockY()-1, drop.getLocation().getBlockZ()).getType()==Material.GRASS){
	//				if(world.getBlockAt(drop.getLocation()).getType()==Material.AIR){
	//					if(drop.getItemStack().getType()==Material.SAPLING){world.getBlockAt(drop.getLocation()).setType(drop.getItemStack().getType());world.getBlockAt(drop.getLocation()).setData(drop.getItemStack().getData().getData());drop.remove();}
	//				}
	//			}
	//		}
	//	}
	private boolean isApplyArea(Location loc, World world) {
		ArrayList<String> keys = new ArrayList<String>(areaWorld.keySet());
		for(int i = 0;i < keys.size();i++){
			String name = keys.get(i);
			if(world.getName().equalsIgnoreCase(areaWorld.get(name))){
				Location loc1 = areaLoc1.get(name);
				Location loc2 = areaLoc2.get(name);
				int pass = 0;
				if(loc1.getBlockX() < loc2.getBlockX()){
					if(loc.getBlockX() >= loc1.getBlockX() && loc.getBlockX() <= loc2.getBlockX()){
						pass++;
					}
				}else{
					if(loc.getBlockX() <= loc1.getBlockX() && loc.getBlockX() >= loc2.getBlockX()){
						pass++;
					}
				}
				if(loc1.getBlockY() < loc2.getBlockY()){
					if(loc.getBlockY() >= loc1.getBlockY() && loc.getBlockY() <= loc2.getBlockY()){
						pass++;
					}
				}else{
					if(loc.getBlockY() <= loc1.getBlockY() && loc.getBlockY() >= loc2.getBlockY()){
						pass++;
					}
				}
				if(loc1.getBlockZ() < loc2.getBlockZ()){
					if(loc.getBlockZ() >= loc1.getBlockZ() && loc.getBlockZ() <= loc2.getBlockZ()){
						pass++;
					}
				}else{
					if(loc.getBlockZ() <= loc1.getBlockZ() && loc.getBlockZ() >= loc2.getBlockZ()){
						pass++;
					}
				}
				if(pass >= 3){ return true; }
			}
		}
		return false;
	}
	
	private boolean isApplyWorld(World world) {
		ArrayList<Integer> keys = new ArrayList<Integer>(worldApply.keySet());
		for(int i = 0;i < keys.size();i++){
			int id = keys.get(i);
			if(world.getName().equalsIgnoreCase(worldName.get(id))){ return worldApply.get(id); }
		}
		return false;
	}
	
	private boolean isGlueBlock(Material mat) {
		String[] dat = glueblocklist.split(":");
		for(int i = 0;i < dat.length;i++){
			if(mat == Material.getMaterial(Integer.parseInt(dat[i]))){ return true; }
		}
		return false;
	}
	
	private boolean isDroppedBlock(Material mat) {
		//Another block can drop to these blocks and it will drop
		//63:68:321:50:76:75:55:104:105:59:6:333:328:354:77:69
		String[] dat = droppedlist.split(":");
		for(int i = 0;i < dat.length;i++){
			if(mat == Material.getMaterial(Integer.parseInt(dat[i]))){ return true; }
		}
		return false;
	}
	
	private boolean isIgnoredBlock(Material mat) {
		//Another block can drop to these blocks
		//0:8:9:10:11:104:105:59:6:333:328:92:32:51:90:111:106:78
		String[] dat = ignorelist.split(":");
		for(int i = 0;i < dat.length;i++){
			if(mat == Material.getMaterial(Integer.parseInt(dat[i]))){ return true; }
		}
		return false;
	}
	
	private boolean isNonPhysixBlock(Material mat) {
		//No physic apply to these block
		//63:68:321:50:75:76:55:69:77 GLUEBLOCK
		String[] dat = nonphysixlist.split(":");
		for(int i = 0;i < dat.length;i++){
			if(mat == Material.getMaterial(Integer.parseInt(dat[i]))){ return true; }
		}
		return isGlueBlock(mat);
	}
	
	private boolean isAroundGlueBlock(Block block) {
		for(int x = -1;x <= 1;x++){
			for(int y = -1;y <= 1;y++){
				for(int z = -1;z <= 1;z++){
					if(x != 0 || y != 0 || z != 0){
						if(isGlueBlock(block.getRelative(x, y, z).getType())){ return true; }
					}
				}
			}
		}
		return false;
	}
	
	public int isTree(Block block, World world) {
		int treeheight = 0;
		for(int i = 0;i > -block.getLocation().getBlockY();i--){
			if(block.getRelative(0, i, 0).getType() != Material.LOG && block.getRelative(0, i, 0).getType() != Material.DIRT){
				return 0;
			}else{
				if(block.getRelative(0, i, 0).getType() == Material.DIRT){
					break;
				}
			}
		}
		for(int i = 0;i < world.getMaxHeight() - block.getLocation().getBlockY();i++){
			if(block.getRelative(0, i, 0).getType() == Material.LOG){
				treeheight++;
			}else{
				if(block.getRelative(0, i, 0).getType() == Material.LEAVES){
					return treeheight;
				}else{
					return 0;
				}
			}
		}
		return 0;
	}
	
	//
	//	private void cutTree(Block block,World world,int height){
	//		for(int i=0;i>-block.getLocation().getBlockY();i--){
	//			if(block.getRelative(0, i, 0).getType()==Material.DIRT){
	//				Block lowest=block.getRelative(0, i, 0);
	//				for(int x=-5;x<=5;x++){
	//					for(int y=0;y<=height+2;y++){
	//						for(int z=-5;z<=5;z++){
	//							Block rem=lowest.getRelative(x, y, z);
	//							if(rem.getType()==Material.LOG||rem.getType()==Material.LEAVES){
	//								rem.breakNaturally();
	//							}
	//						}	
	//					}
	//				}
	//			}
	//		}
	//	}
	//
	//	public void checkTree(Block block,World world){
	//		if(block.getType()==Material.LOG&&treecut&&(isApplyWorld(world)||isApplyArea(block.getLocation(),world))){
	//			int height=isTree(block,world);
	//			if(height>0){
	//				cutTree(block,world,height);
	//			}
	//		}
	//	}
	public void checkBreak(Block block, World world, boolean bypass) {
		if(isApplyWorld(world) || isApplyArea(block.getLocation(), world) || bypass){
			checkRadius(block, world, bypass);
			for(int r = 1;r <= checkradius;r++){
				checkRadius(block.getRelative(0, r, 0), world, bypass);
				checkRadius(block.getRelative(0, -r, 0), world, bypass);
			}
		}
	}
	
	public void checkRadius(Block block, World world, boolean bypass) {
		for(int r = 0;r <= checkradius;r++){
			for(int x = -r;x <= r;x++){
				if(x == -r || x == r){
					for(int y = -r;y <= r;y++){
						if(block.getRelative(x, 0, y).getType() != Material.AIR){
							checkPhysix(block.getRelative(x, 0, y), world, bypass);
						}
					}
				}else{
					if(block.getRelative(x, 0, -r).getType() != Material.AIR){
						checkPhysix(block.getRelative(x, 0, -r), world, bypass);
					}
					if(block.getRelative(x, 0, r).getType() != Material.AIR){
						checkPhysix(block.getRelative(x, 0, r), world, bypass);
					}
				}
			}
		}
		for(int r = checkradius;r >= 0;r--){
			for(int x = -r;x <= r;x++){
				if(x == -r || x == r){
					for(int y = -r;y <= r;y++){
						if(block.getRelative(x, 0, y).getType() != Material.AIR){
							checkPhysix(block.getRelative(x, 0, y), world, bypass);
						}
					}
				}else{
					if(block.getRelative(x, 0, -r).getType() != Material.AIR){
						checkPhysix(block.getRelative(x, 0, -r), world, bypass);
					}
					if(block.getRelative(x, 0, r).getType() != Material.AIR){
						checkPhysix(block.getRelative(x, 0, r), world, bypass);
					}
				}
			}
		}
	}
	
	public void applyStepPhysix(Block block, World world) {
		Block low = block;
		Block set = world.getBlockAt(low.getLocation());
		if(isIgnoredBlock(low.getType()) || isDroppedBlock(low.getType())){
			if((isIgnoredBlock(low.getType()) || isDroppedBlock(low.getType())) && (low.getLocation().getBlockY() >= 1) && (!isAroundGlueBlock(low))){
				set = low.getRelative(0, -1, 0);
			}
			if(isDroppedBlock(set.getType())){
				ItemStack drop = new ItemStack(set.getTypeId(), 1, (short) 0);
				drop.getData().setData(set.getData());
				world.dropItemNaturally(set.getLocation(), drop);
			}
			set.setType(block.getType());
			set.setData(block.getData());
			block.setType(Material.AIR);
		}
		if(low.getLocation() == set.getLocation()){ return; }
		getServer().getScheduler().scheduleSyncDelayedTask(this, new ProgressiveDrop(this, set, world), 0);
	}
	
	private void applyPhysix(Block block, World world) {
		if(progressivePhysix){
			getServer().getScheduler().scheduleSyncDelayedTask(this, new ProgressiveDrop(this, block, world), 0);
			return;
		}
		Block low = block.getRelative(0, -1, 0);
		if(isIgnoredBlock(low.getType()) || isDroppedBlock(low.getType())){
			Block set = world.getBlockAt(getLowestAir(block, world));
			if(isDroppedBlock(set.getType())){
				ItemStack drop = new ItemStack(set.getTypeId(), 1, (short) 0);
				drop.getData().setData(set.getData());
				world.dropItemNaturally(set.getLocation(), drop);
			}
			set.setType(block.getType());
			set.setData(block.getData());
			block.setType(Material.AIR); //Ploblem here!
		}
	}
	
	private Location getLowestAir(Block block, World world) {
		Block low = block;
		while (true){
			if(isIgnoredBlock(low.getRelative(0, -1, 0).getType()) || isDroppedBlock(low.getRelative(0, -1, 0).getType())){
				if(isDroppedBlock(low.getRelative(0, -1, 0).getType())){
					ItemStack drop = new ItemStack(low.getRelative(0, -1, 0).getTypeId(), 1, (short) 0);
					drop.getData().setData(low.getRelative(0, -1, 0).getData());
					world.dropItemNaturally(low.getRelative(0, -1, 0).getLocation(), drop);
				}
				low.getRelative(0, -1, 0).setType(Material.AIR);
				if(low.getLocation().getBlockY() <= -100 || isAroundGlueBlock(low)){ return low.getLocation(); }
				low = low.getRelative(0, -1, 0);
			}else{
				return low.getLocation();
			}
		}
	}
	
	private Location getLowestAirByPass(Block block, World world) {
		Block low = block;
		while (true){
			if(low.getRelative(0, -1, 0).getType() == Material.AIR){
				low.getRelative(0, -1, 0).setType(Material.AIR);
				if(low.getLocation().getBlockY() <= -100){ return low.getLocation(); }
				low = low.getRelative(0, -1, 0);
			}else{
				return low.getLocation();
			}
		}
	}
	
	private boolean checkBlockConnection(Block block) {
		int samex = 0;
		int samez = 0;
		//Connected atlease # blocks
		if(!isIgnoredBlock(block.getRelative(0, 0, -1).getType()) && !isNonPhysixBlock(block.getRelative(0, 0, -1).getType()) && !isDroppedBlock(block.getRelative(0, 0, -1).getType())){
			samex++;
		}
		if(!isIgnoredBlock(block.getRelative(0, 0, 1).getType()) && !isNonPhysixBlock(block.getRelative(0, 0, 1).getType()) && !isDroppedBlock(block.getRelative(0, 0, 1).getType())){
			samex++;
		}
		if(!isIgnoredBlock(block.getRelative(-1, 0, 0).getType()) && !isNonPhysixBlock(block.getRelative(-1, 0, 0).getType()) && !isDroppedBlock(block.getRelative(-1, 0, 0).getType())){
			samez++;
		}
		if(!isIgnoredBlock(block.getRelative(1, 0, 0).getType()) && !isNonPhysixBlock(block.getRelative(1, 0, 0).getType()) && !isDroppedBlock(block.getRelative(1, 0, 0).getType())){
			samez++;
		}
		if(samex < minimumconnected && samez < minimumconnected && !isAroundGlueBlock(block)){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean checkPhysix(Block block, World world, boolean bypass) {
		if(!checkBlockConnection(block) && !isNonPhysixBlock(block.getType()) && !isIgnoredBlock(block.getType()) && (isApplyWorld(world) || isApplyArea(block.getLocation(), world)) || bypass){
			applyPhysix(block, world);
			return true;
		}
		return false;
	}
	
	int ptotal = 0;
	int pdone = 0;
	int px = 0; //X
	int py = 0; //Y
	int pz = 0; //Z 
	
	public void progressiveInstantArea() {
		Material mat = pcplayer.getWorld().getBlockAt(px, py, pz).getType();
		if(mat != Material.AIR){
			byte data = pcplayer.getWorld().getBlockAt(px, py, pz).getData();
			pcplayer.getWorld().getBlockAt(px, py, pz).setType(Material.AIR);
			Block set = pcplayer.getWorld().getBlockAt(getLowestAirByPass(pcplayer.getWorld().getBlockAt(px, py, pz), pcplayer.getWorld()));
			set.setType(mat);
			set.setData(data);
		}
		pdone++;
		px++;
		if(px > str2loc(pcloc2).getBlockX()){
			px = str2loc(pcloc1).getBlockX();
			py++;
		}
		if(py > str2loc(pcloc2).getBlockY()){
			py = str2loc(pcloc1).getBlockY();
			pz++;
		}
		if(pz > str2loc(pcloc2).getBlockZ()){
			//Done
			confirm = false;
			log.info("[" + pdf.getName() + "] Instant Physic Done.");
			pcplayer.sendMessage(ChatColor.GREEN + "Instant physic done.");
			return;
		}
		getServer().getScheduler().scheduleSyncDelayedTask(this, new ProgressiveInstantArea(this), 0);
	}
	
	public void instantArea(Location loc1, Location loc2, Player player) {
		if(!confirm){
			if(loc1.getBlockX() > loc2.getBlockX()){
				Location tmp = loc1.clone();
				loc1.setX(loc2.getBlockX());
				loc2.setX(tmp.getBlockX());
			}
			if(loc1.getBlockY() > loc2.getBlockY()){
				Location tmp = loc1.clone();
				loc1.setY(loc2.getBlockY());
				loc2.setY(tmp.getBlockY());
			}
			if(loc1.getBlockZ() > loc2.getBlockZ()){
				Location tmp = loc1.clone();
				loc1.setZ(loc2.getBlockZ());
				loc2.setZ(tmp.getBlockZ());
			}
			//Calculate block
			int tx = loc2.getBlockX() - loc1.getBlockX();
			int ty = loc2.getBlockY() - loc1.getBlockY();
			int tz = loc2.getBlockZ() - loc1.getBlockZ();
			ptotal = (tx * ty * tz);
			player.sendMessage(ChatColor.YELLOW + "" + ptotal + " blocks will be process...");
			player.sendMessage(ChatColor.YELLOW + "Are you sure you want to continue?");
			player.sendMessage(ChatColor.YELLOW + "Type \"/physix y\" or \"/physix n\" to confirm...");
			pcloc1 = loc2str(loc1);
			pcloc2 = loc2str(loc2);
			pcplayer = player;
			wait = true;
		}else{
			log.info("[" + pdf.getName() + "] Instant Physic Started.");
			if(progressive){
				pdone = 0;
				px = str2loc(pcloc1).getBlockX();
				py = str2loc(pcloc1).getBlockY();
				pz = str2loc(pcloc1).getBlockZ();
				getServer().getScheduler().scheduleSyncDelayedTask(this, new ProgressiveInstantArea(this), 0);
			}else{
				loc1 = str2loc(pcloc1);
				loc2 = str2loc(pcloc2);
				player = pcplayer;
				int x, y, z;
				for(x = loc1.getBlockX();x <= loc2.getBlockX();x++){
					for(y = loc1.getBlockY();y <= loc2.getBlockY();y++){
						for(z = loc1.getBlockZ();z <= loc2.getBlockZ();z++){
							Material mat = player.getWorld().getBlockAt(x, y, z).getType();
							if(mat != Material.AIR){
								byte data = player.getWorld().getBlockAt(x, y, z).getData();
								player.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
								Block set = player.getWorld().getBlockAt(getLowestAirByPass(player.getWorld().getBlockAt(x, y, z), player.getWorld()));
								set.setType(mat);
								set.setData(data);
							}
						}
					}
				}
				confirm = false;
				log.info("[" + pdf.getName() + "] Instant Physic Done.");
				player.sendMessage(ChatColor.GREEN + "Instant physic done.");
			}
		}
	}
	
	private boolean isNumber(String str) {
		return str.matches("-?\\d+(.\\d+)?");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(sender.getName().compareTo("CONSOLE") != 0){
			if(((Player) sender).isOp()){
				if(args.length == 1){
					if(wait){
						if(args[0].equalsIgnoreCase("y")){
							wait = false;
							confirm = true;
							sender.sendMessage(ChatColor.GREEN + "Selected area will now apply physic to it. Expecting some lags...");
							instantArea(null, null, null);
							return true;
						}
						if(args[0].equalsIgnoreCase("n")){
							wait = false;
							log.info("[" + pdf.getName() + "] Instant Physic Cancelled.");
							sender.sendMessage(ChatColor.GREEN + "Instant physic cancelled.");
							return true;
						}
					}
					if(!wait){
						if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("all")){
							sender.sendMessage(ChatColor.GREEN + "Physix applied area:");
							ArrayList<String> keys = new ArrayList<String>(areaWorld.keySet());
							if(keys.size() > 0){
								for(int i = 0;i < keys.size();i++){
									String name = keys.get(i);
									sender.sendMessage(ChatColor.AQUA + "\"" + ChatColor.YELLOW + name + ChatColor.AQUA + "\"" + ChatColor.GREEN + " @ " + ChatColor.YELLOW + areaWorld.get(name) + " " + areaLoc1.get(name).getBlockX() + ChatColor.AQUA + ":" + ChatColor.YELLOW + areaLoc1.get(name).getBlockY() + ChatColor.AQUA + ":" + ChatColor.YELLOW + areaLoc1.get(name).getBlockZ() + ChatColor.GREEN + " to " + ChatColor.YELLOW + areaLoc2.get(name).getBlockX() + ChatColor.AQUA + ":" + ChatColor.YELLOW + areaLoc2.get(name).getBlockY() + ChatColor.AQUA + ":" + ChatColor.YELLOW + areaLoc2.get(name).getBlockZ());
								}
							}else{
								sender.sendMessage(ChatColor.YELLOW + "No area created.");
							}
							return true;
						}
					}
				}
				if(args.length >= 1){
					if(!wait){
						if(args[0].equalsIgnoreCase("instant")){
							if(!creator.isEmpty()){
								sender.sendMessage("Please wait... Another OP is creating a new area...");
								return true;
							}
							if(args[1].equalsIgnoreCase("area")){
								cubeCreated = 3;
								sender.sendMessage(ChatColor.AQUA + "Left Click 1st corner...");
								return true;
							}
							if(args[1].equalsIgnoreCase("around")){
								if(isNumber(args[2])){
									instantArea(((Player) sender).getLocation().subtract(Integer.parseInt(args[2]), 0, Integer.parseInt(args[2])), ((Player) sender).getLocation().add(Integer.parseInt(args[2]), Integer.parseInt(args[2]), Integer.parseInt(args[2])), ((Player) sender));
									return true;
								}else{
									sender.sendMessage(ChatColor.YELLOW + "Radius must be an integer.");
								}
							}
						}
						if(args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("create")){
							if(args.length == 2){
								if(!creator.isEmpty()){
									sender.sendMessage("Please wait... Another OP is creating a new area...");
									return true;
								}
								cubeCreated = 1;
								cubeName = args[1].toLowerCase();
								creator = sender.getName().toLowerCase();
								sender.sendMessage(ChatColor.AQUA + "Left Click 1st corner...");
								sender.sendMessage(ChatColor.YELLOW + "Right Click to cancel...");
								return true;
							}
						}
						if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")){
							if(args.length == 2){
								if(!creator.isEmpty()){
									sender.sendMessage("Please wait... Another OP is creating a new area...");
									return true;
								}
								if(areaWorld.containsKey(args[1].toLowerCase())){
									areaWorld.remove(args[1].toLowerCase());
									areaLoc1.remove(args[1].toLowerCase());
									areaLoc2.remove(args[1].toLowerCase());
									sender.sendMessage(ChatColor.LIGHT_PURPLE + "Area " + ChatColor.AQUA + "\"" + ChatColor.YELLOW + args[1] + ChatColor.AQUA + "\"" + ChatColor.LIGHT_PURPLE + " deleted.");
								}else{
									sender.sendMessage(ChatColor.YELLOW + "Area " + ChatColor.AQUA + "\"" + ChatColor.YELLOW + args[1] + ChatColor.AQUA + "\"" + ChatColor.YELLOW + " not found.");
								}
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
