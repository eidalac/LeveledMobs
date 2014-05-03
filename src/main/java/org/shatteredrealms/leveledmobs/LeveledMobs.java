package org.shatteredrealms.leveledmobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_7_R2.AttributeInstance;
import net.minecraft.server.v1_7_R2.AttributeModifier;
import net.minecraft.server.v1_7_R2.EntityInsentient;
import net.minecraft.server.v1_7_R2.GenericAttributes;

import org.bukkit.craftbukkit.v1_7_R2.entity.CraftLivingEntity;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.metadata.FixedMetadataValue;

public class LeveledMobs extends JavaPlugin implements Listener
{
	private static final UUID maxHealthUID = UUID.fromString("f8b0a945-2d6a-4bdb-9a6f-59c285bf1e5d");
	private static final UUID followRangeUID = UUID.fromString("1737400d-3c18-41ba-8314-49a158481e1e");
	private static final UUID knockbackResistanceUID = UUID.fromString("8742c557-fcd5-4079-a462-b58db99b0f2c");
	private static final UUID addknockbackResistanceUID = UUID.fromString("8742c557-fdd5-4079-a462-b58db99b0f2c");
	private static final UUID movementSpeedUID = UUID.fromString("206a89dc-ae78-4c4d-b42c-3b31db3f5a7c");
	private static final UUID attackDamageUID = UUID.fromString("7bbe3bb1-079d-4150-ac6f-669e71550776");
	
	private double damageMultiplier;
	private double speedMultiplier;
	private double knockbackResistanceMultiplier;
	private double followRangeMultiplier;

	Boolean elites;
	Boolean giants;
	Boolean Sarmor;
	Boolean Zarmor;
	Boolean dropLegendarys;
	Boolean deathmessage;
	Boolean eliteDrops;
	Boolean constantVisibility;
	Boolean worldGuard = false;
	Boolean hasSpawned = false;

	double area;
	double multiplier;
	double eliteSpawnChance;
	private final double maxLevel = 999;

	BukkitScheduler s;
	BukkitScheduler m;

	ArrayList<String>worlds = new ArrayList<String>();
	ArrayList<String>mobsThatDropLegendarys = new ArrayList<String>();
	ArrayList<ItemStack>legendaryItemList = new ArrayList<ItemStack>();
	ArrayList<ItemStack>lequipment = new ArrayList<ItemStack>();
	ArrayList<ItemStack>iequipment = new ArrayList<ItemStack>();
	ArrayList<ItemStack>gequipment = new ArrayList<ItemStack>();
	ArrayList<ItemStack>dequipment = new ArrayList<ItemStack>();
	ArrayList<String>eliteMobs = new ArrayList<String>();

	ItemStack lchest = new ItemStack(Material.LEATHER_CHESTPLATE);
	ItemStack lboots = new ItemStack(Material.LEATHER_BOOTS);
	ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET);
	ItemStack llegs = new ItemStack(Material.LEATHER_LEGGINGS);

	ItemStack ichest = new ItemStack(Material.IRON_CHESTPLATE);
	ItemStack iboots = new ItemStack(Material.IRON_BOOTS);
	ItemStack ihelmet = new ItemStack(Material.IRON_HELMET);
	ItemStack ilegs = new ItemStack(Material.IRON_LEGGINGS);

	ItemStack gchest = new ItemStack(Material.GOLD_CHESTPLATE);
	ItemStack gboots = new ItemStack(Material.GOLD_BOOTS);
	ItemStack ghelmet = new ItemStack(Material.GOLD_HELMET);
	ItemStack glegs = new ItemStack(Material.GOLD_LEGGINGS);

	ItemStack dchest = new ItemStack(Material.DIAMOND_CHESTPLATE);
	ItemStack dboots = new ItemStack(Material.DIAMOND_BOOTS);
	ItemStack dhelmet = new ItemStack(Material.DIAMOND_HELMET);
	ItemStack dlegs = new ItemStack(Material.DIAMOND_LEGGINGS);

	@Override
	public void onEnable() 
	{
		/**
		 * MCstats/Plugin Metrics.
		 * 
		 * The following information is collected and sent to mcstats.org:
		 * - A unique identifier
		 * - The server's version of Java
		 * - Whether the server is in offline or online mode
		 * - The plugin's version
		 * - The server's version
		 * - The OS version/name and architecture
		 * - The core count for the CPU
		 * - The number of players online
		 * - The Metrics version
		 * */
		try 
		{
			Metrics metrics = new Metrics(this);

			Graph serversThatUseLeveledMobs = metrics.createGraph("Servers and their player count");

			serversThatUseLeveledMobs.addPlotter(new Metrics.Plotter(Bukkit.getMotd()) 
			{

				@Override
				public int getValue() 
				{
					return Bukkit.getServer().getOnlinePlayers().length;
				}

			});

			metrics.start();
		} 
		catch (IOException e) 
		{
			// Failed to submit the stats.
		}

		s = Bukkit.getServer().getScheduler(); 
		getServer().getPluginManager().registerEvents(this, this);

		// read the config files.
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		console.sendMessage("[LeveledMobs] loading configuration.");
		
		loadConfiguration();
		
		console.sendMessage("[LeveledMobs] enabled.");

		s.scheduleSyncRepeatingTask(this, new Runnable() 
		{

			@Override
			public void run() 
			{	

				if(getConfig().getBoolean("constantVisibility") == true)
				{
					for(World w : Bukkit.getServer().getWorlds())
					{
						String World = w.getName().replace("CraftWorld{name=", "");
						World.replace("}", "");
				
						if(worlds.contains(World))
						{
							for(Entity ent : w.getEntities())
							{
								if (ent instanceof LivingEntity)
								{
									if (((LivingEntity) ent).getCustomName() != null)
									{
										Boolean player = false;
										for(Entity entity : ent.getNearbyEntities(getConfig().getDouble("visibilityDistance"), getConfig().getDouble("visibilityDistance"), getConfig().getDouble("visibilityDistance")))
										{
											if(entity instanceof Player)
											{
												player = true;
											}
										}
										((LivingEntity) ent).setCustomNameVisible(player);
										player = false;
									}
								}
							}
						}
					}
				}
			}
		}, 0L, 10L);
	}

	/**
	 * Moved the config loading here so we can do a reload command line
	 * -- Eidalac 4/26
	 * */
	private void loadConfiguration()
	{
		this.getConfig().options().copyDefaults(true);
		this.reloadConfig();

		damageMultiplier = getConfig().getDouble("damageMultiplier");
		speedMultiplier = getConfig().getDouble("speedMultiplier");
		knockbackResistanceMultiplier = getConfig().getDouble("knockbackResistanceMultiplier");
		followRangeMultiplier = getConfig().getDouble("followRangeMultiplier");		
		
		Zarmor = getConfig().getBoolean("zombie.armor.enabled");
		Sarmor = getConfig().getBoolean("skeleton.armor.enabled");
		giants = getConfig().getBoolean("zombie.giants.enabled");
		dropLegendarys = getConfig().getBoolean("legendaryItems.enabled");
		elites = getConfig().getBoolean("eliteMobs.enabled");
		deathmessage = getConfig().getBoolean("deathMessage");
		eliteDrops = getConfig().getBoolean("legendaryItems.elites.enabled");
		constantVisibility = getConfig().getBoolean("constantVisibility");
		hasSpawned = false;

		for(String world : getConfig().getStringList("generalSettings.worlds"))
		{
			worlds.add(world);
		}

		multiplier = getConfig().getDouble("multiplier");
		area = getConfig().getDouble("distance");		
		
		lequipment.add(lchest);
		lequipment.add(lboots);
		lequipment.add(lhelmet);
		lequipment.add(llegs);

		iequipment.add(ichest);
		iequipment.add(iboots);
		iequipment.add(ihelmet);
		iequipment.add(ilegs);

		gequipment.add(gchest);
		gequipment.add(gboots);
		gequipment.add(ghelmet);
		gequipment.add(glegs);

		dequipment.add(dchest);
		dequipment.add(dboots);
		dequipment.add(dhelmet);
		dequipment.add(dlegs);

		for(String mobsThatDropLegendarysString : getConfig().getStringList("legendaryItems.normalMobs.mobsThatDropLegendarys"))
		{
			mobsThatDropLegendarys.add(mobsThatDropLegendarysString);
		}

		/**
		 * What is this for?  Seems to be a config that' not read.
		 * Removed it for now, as I'm trying to sort out some strange item drops.
		 * -- eidalac 4/25.
		 * */
/*		
		for(String eliteMob : getConfig().getStringList("eliteMobs.includedMobs"))
		{
			eliteMobs.add(eliteMob);
		}
*/		
		Bukkit.getServer().getConsoleSender().sendMessage("[LeveledMobs] loaded.");
		this.saveConfig();
	}

	/**
	 * Shouldn't be needed with the new level saving to meta data.
	 * --eidalac 4/26
	 * */
/*	@EventHandler
	public void EntityTameEvent(EntityTameEvent e)
	{
		e.getEntity().setCustomName(e.getOwner().getName() + "`s " + e.getEntityType().toString().toLowerCase());
	}
*/
	/**
	 * Refresh mobs on the server.
	 * 
	 * Moved to it's own function.
	 * -- eidalac 4/26.
	 * **/
	private void doRefreshMobs()
	{
		// Kill them ALL.
		for (World w : Bukkit.getServer().getWorlds())
		{
			for (Entity ent : w.getEntities())
			{
				if (ent instanceof LivingEntity)
				{
					String entWorld = ent.getWorld().getName().replace("CraftWorld{name=", "");
					entWorld.replace("}", "");
					String entityTypeName = ent.getClass().getName().toString().substring(ent.getClass().getName().toString().indexOf(".", 31) + 6, ent.getClass().getName().toString().length()).toLowerCase();
						
					// No levels?  Ignore it.
					if (getCreatureLevel((LivingEntity) ent) == 0)
						continue;
	
					// Exempt, ignore it, unless it had levels.
					if(getConfig().getStringList("generalSettings.worldLocations." + entWorld + ".exemptedMobs").contains(entityTypeName))
					{
						// To prevent a possible issue (set a chicken to levels, then exempt it and you can't clear it with a refresh.  Now you can.
						if (getCreatureLevel((LivingEntity) ent) == 0)
							continue;
					}								
	
					// Player, ignore it.
					if (ent instanceof Player)
						continue;
					
					// Tameable, is it owned?
					if (ent instanceof Tameable)
					{
						// Yes, ignore it
						if (((Tameable) ent).getOwner() != null)
							continue;						
					}
	
					// Die!
					ent.remove();
				}
			}
		}
	}
	
	/**
	 * Command line parser for the plugin
	 * 
	 * Parses /lm commands.
	 * 
	 * Updated to use Tameable rather than special case for every tameable mod type.
	 * --edialac 4/25
	 * */
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) 
	{
		boolean ok = false;

		// Is this a LM command?
		if (! (command.getLabel().equalsIgnoreCase("lm") || (command.getLabel().equalsIgnoreCase("leveledmobs"))))
			return ok;
		
		// Setup our message recpeients (console or player)
		Player p = null;
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		String msg = ChatColor.DARK_RED + "[LeveledMobs]" + ChatColor.GREEN;

		if (sender instanceof Player)
			p = (Player) sender;
		
		// Do we have args?
		if (args.length > 0)
		{
			// check for /lm refreshmobs:
			if (args[0].equalsIgnoreCase("refreshmobs"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.refreshmobs"))
				{
					this.doRefreshMobs();
					msg += " All mobs have been reset!";
					ok = true;
				}
								
			}
			// /lm setup
			else if(args[0].equalsIgnoreCase("setup"))
			{
				//permission?
				if (sender.hasPermission("LeveledMobs.setup"))
				{
					// Seems a bit odd, never seen a plugin with this, but I'll leave it for now.  If it ain't broke and all.
					this.saveDefaultConfig();
					this.loadConfiguration();

					msg += " LeveledMobs default configuration has been saved and loaded.!";
					ok = true;
				}
			}
			// check for /lm reload:
			else if (args[0].equalsIgnoreCase("reload"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.reload"))
				{
					// reload the config files.
					this.loadConfiguration();
					msg += " LeveledMobs config has been Reloaded!";
					ok = true;
				}
			}
			else if (args[0].equalsIgnoreCase("spawnmob"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.spawnmob"))
				{
					if (args.length < 3)
					{
						msg += " Usage /lm spawnmob <mob> <level> [amount]";
						ok = true;
					}
					else if (p == null)
					{
						msg += " spawnmob command must be run as a player.";
						ok = true;
					}
					else
					{
			            try
			            {
			            	int x;
			            	
			            	if (args.length < 4)
			            		x = 0;
			            	else
			            		x = Integer.parseInt(args[3]);
			            		
			            	x = Math.max(1, x);
			            	x = Math.min(x, 10);
			            	
			            	msg += " spawning level " + args[2] + " " + args[1];
			            	
			            	while (x > 0)
			            	{
				                EntityType entityType = EntityType.valueOf(args[1].toUpperCase());
				                Entity ent = p.getWorld().spawnEntity(p.getLocation(), entityType);
				                
				                if (ent == null)
				                	msg += " Faild to spawn " + args[1];
				                
				                this.doSetLevel((LivingEntity) ent, Double.parseDouble(args[2]));
				                x--;
			            	}

			            }
			            catch (IllegalArgumentException ex)
			            {
			            	msg += " Unknown EntityType:" + args[1];
			            }
			            ok = true;
					}
					
				}
			}
			// check for /lm addspawn:
			else if (args[0].equalsIgnoreCase("addspawn"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.addspawn"))
				{
					if (p == null)
					{
						msg += " addspawn command must be run as a player.";
						ok = true;
					}
					else if (args.length < 2)
					{
						msg += " Usage /lm addspawn <spawnname>";
						ok = true;
					}
					else
					{
						msg += doAddSpawn(p, args[1]);
						ok = true;
					}
				}
			}
			// check for /lm deletespawn:
			else if (args[0].equalsIgnoreCase("deletespawn"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.deletespawn"))
				{
					if (p == null)
					{
						msg += " deletespawn command must be run as a player.";
						ok = true;
					}
					else if (args.length < 2)
					{
						msg += " Usage /lm deletespawn <spawnname>";
						ok = true;
					}
					else
					{
						msg += doDeleteSpawn(p, args[1]);
						ok = true;
					}
				}
			}
			// check for /lm listspawns:
			else if (args[0].equalsIgnoreCase("listspawns"))
			{
				// Permission?
				if (sender.hasPermission("LeveledMobs.listspawns"))
				{
					if (p == null)
					{
						msg += " listspawns command must be run as a player.";
						ok = true;
					}
					else
					{
						msg += doListSpawn(p);
						ok = true;
					}
				}
			}
			else
			{
				msg += " Commands:\n"
						+ "/lm refreshmobs\n"
						+ "/lm reload\n"
						+ "/lm setup\n"
						+ "/lm spawnmob <mob> <level> [amount]\n"
						+ "/lm addspawn <name>\n"
						+ "/lm deletespawn <name>\n"
						+ "/lm listspawns\n";
				ok = true;
			}
		}
		else
		{
			msg += " Commands:\n"
					+ "/lm refreshmobs\n"
					+ "/lm reload\n"
					+ "/lm setup\n"
					+ "/lm spawnmob <mob> <level> [amount]\n"
					+ "/lm addspawn <name>\n"
					+ "/lm deletespawn <name>\n"
					+ "/lm listspawns\n";
			ok = true;
		}
		
		if (!ok)
			msg += " Insufficient Permissions";

		// send the message.
		if (p != null)
			p.sendMessage(msg);
		else
			console.sendMessage(msg);

		return ok;
	}

	/**
	 * Lists the current spawns for this world.
	 * 
	 * -- Eidalac 4/27
	 * */
	private String doListSpawn(Player p)
	{
		String worldName = p.getWorld().getName().replace("CraftWorld{name=", "");
		worldName.replace("}", "");
		
		// Is this world in the enabled list?
		if (! (worlds.contains(worldName)))
			return " Not enabled for world " + worldName + ".";
		
		List<String> centralSpawns = getConfig().getStringList("generalSettings.worldLocations." + worldName + ".centralSpawns");
		ListIterator<String> spawnsList = centralSpawns.listIterator();
		String nameList = " Spawn Locations:\n";

		// Walk through the list, checking if this location is closer than spawn1.
		while (spawnsList.hasNext())
		{
			String spawn = spawnsList.next();
			int x = getConfig().getInt("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".x");
			int y = getConfig().getInt("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".y");
			int z = getConfig().getInt("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".z");
			
			nameList += spawn + " X: " + x + " Y: " + y + "  Z: " + z + ".\n";
		}
		return nameList;
	}
	
	/**
	 * Deletes a spawn location from the config file.
	 * 
	 * -- Eidalac 4/27
	 * */
	private String doDeleteSpawn(Player p, String name)
	{
		String worldName = p.getWorld().getName().replace("CraftWorld{name=", "");
		worldName.replace("}", "");

		// Is this world in the enabled list?
		if (! (worlds.contains(worldName)))
			return " Not enabled for world " + worldName + ".";
		
		if (! getConfig().contains("generalSettings.worldLocations." + worldName + ".spawnLocations." + name))
			return " Spawn name " + name + " not present in world " + worldName;
		
		if (name.equalsIgnoreCase("spawn1"))
			return " Spawn1 can't be deleted.";
	
//		List<String> spawnLocations = getConfig().getStringList("generalSettings.worldLocations." + worldName + ".spawnLocations");
//		spawnLocations.remove(name);
		
		List<String> centralSpawns = getConfig().getStringList("generalSettings.worldLocations." + worldName + ".centralSpawns");
		centralSpawns.remove(name);

		getConfig().set("generalSettings.worldLocations." + worldName + ".spawnLocations." + name, null);
		getConfig().set("generalSettings.worldLocations." + worldName + ".centralSpawns", centralSpawns);
		
		Bukkit.getServer().getConsoleSender().sendMessage("[LeveledMobs] " + p.getName() + " remvoing spawn point " + name + " from world " + worldName + ".");
		
//		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
//		this.reloadConfig();
		return " Spawn point removed.";
	}
	
	/**
	 * Adds a new spawn location to the config.
	 * 
	 * -- Eidalac 4/27
	 * */
	private String doAddSpawn(Player p, String name)
	{
		String worldName = p.getWorld().getName().replace("CraftWorld{name=", "");
		worldName.replace("}", "");
		Location l = p.getLocation();

		// Is this world in the enabled list?
		if (! (worlds.contains(worldName)))
			return " Not enabled for world " + worldName + ".";
		
		if (getConfig().contains("generalSettings.worldLocations." + worldName + ".spawnLocations." + name))
			return " Spawn name " + name + " already exists in world " + worldName;
		
		Location nl = getNearestLocation(p, worlds, worldName);
		double near = nl.distance(l);
		
		// Is this within a single 'level' distance of the nearest spawn?  That's too close.
		if (near < area)
			return " Spawn location is less than " + (int) near + " blocks from nearest spawn.  Must be at least " + (int) area + " blocks away.";

		getConfig().set("generalSettings.worldLocations." + worldName + ".spawnLocations." + name + ".x", l.getBlockX());
		getConfig().set("generalSettings.worldLocations." + worldName + ".spawnLocations." + name + ".y", l.getBlockY());
		getConfig().set("generalSettings.worldLocations." + worldName + ".spawnLocations." + name + ".z", l.getBlockZ());
		
		List<String> centralSpawns = getConfig().getStringList("generalSettings.worldLocations." + worldName + ".centralSpawns");
		centralSpawns.add(name);

		getConfig().set("generalSettings.worldLocations." + worldName + ".centralSpawns", centralSpawns);
		
		Bukkit.getServer().getConsoleSender().sendMessage("[LeveledMobs] " + p.getName() + " adding spawn point " + name + " to world " + worldName + ".");
		
//		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
//		this.reloadConfig();
		return " New Spawn point added.";
	}
	
	/**
	 * Spawns in a mob and gives it levels, if applicable.
	 * 
	 * Updated to save levels in metadata fields rather than in the names and tweaked the default names.
	 * Should prevent conflicts with other plugins and give more options for configurable names.
	 * -- Eidalac 4/25
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void EntitySpawnEvent(CreatureSpawnEvent e)
	{
		if (e.isCancelled())
			return;
		
		LivingEntity ent = e.getEntity();

		//eliteSpawnChance = Double.valueOf(getConfig().getString("eliteMobs.chance").replace("%", ""));
		//Tameable te = null;

		/**
		 * Removed:  With the meta data used for levels and the name changed, this should not be needed.
		 * leaving in comments for now in case I missed something.
		 * 
		 * Also, how can something be owned on a spawn event?  That would have to happen after spawnning it.
		 * */
/*			if(ent instanceof Tameable)
			{
				te = (Tameable) ent;
				if(te.getOwner() != null)
				{
					e.getEntity().setCustomName(te.getOwner().getName() + "`s " + e.getEntityType().toString().toLowerCase());
				}
			}
*/
		String entityTypeName = ent.getClass().getName().toString().substring(ent.getClass().getName().toString().indexOf(".", 31) + 6, ent.getClass().getName().toString().length()).toLowerCase();
		String entWorld = ent.getWorld().getName().replace("CraftWorld{name=", "");
		entWorld.replace("}", "");		

		// Not enabled in this world; Ignore.
		if (! (worlds.contains(entWorld)))
			return;
		
		// Is it a player?  Ignore it.
		if (ent instanceof Player)
			return;

		// Exempt mob?  Ignore it.
		if (getConfig().getStringList("generalSettings.worldLocations." + entWorld + ".exemptedMobs").contains(entityTypeName))
			return;

		// Tamed with an owner?  Ignore it.
		// Should not be possible on a spawn event.
		if (ent instanceof Tameable)
		{
			if (((Tameable) ent).getOwner() != null)
				return;
		}

		// Villager/NPC?  Ignore it.	
		if (ent.hasMetadata("Npc"))
			return;
			
		//if(elites == true && eliteMobs.contains(entityTypeName) && trueeliteSpawnChance >= eliteSpawnChance)
		//	EliteMobs.updateMobList(e.getEntity().getUniqueId());

		Location cregion = getNearestLocation(ent, worlds, entWorld);

		double dist = cregion.distance(ent.getLocation());
		dist = Math.min(Math.ceil(dist/area), maxLevel);
		final double trueDistance = dist;  
		
		doSetLevel(ent, trueDistance);
	}

	/**
	 * Set up armor/equpiment for creatures based on level and config options.
	 * 
	 * Moved to it's own function.
	 * - Eidalac 5/3.
	 * */
	private void doSetArmor(LivingEntity ent, double level)
	{
		Random r = new Random();
		ItemStack[] equipment = null;

		// Is it a zombol?
		if (ent instanceof Zombie)
		{
			Zombie z = (Zombie)ent;
			
			// Are giants permitted?
			if (giants == true)
			{
				// We far enough out?
				if (level > getConfig().getInt("zombie.giants.level"))
				{
					// Check % to spawn a giant instead.
					int chance = Integer.valueOf(getConfig().getString("zombie.giants.chance").replace("%", ""));

					if (r.nextInt(100) <= chance)
						ent = (LivingEntity) ent.getWorld().spawnEntity(ent.getLocation(), EntityType.GIANT);
				}
			}
				
			// Do they get armor?	
			if (Zarmor == true)
			{			
				double armorLevel = getConfig().getDouble("zombie.armor.level");
				armorLevel = Math.ceil(level/armorLevel);
				int spawnChance = Integer.valueOf(getConfig().getString("zombie.armor.chance").replace("%", ""));

				if(r.nextInt(100) <= spawnChance)
				{
					switch ((int) armorLevel)
					{
						case 1: 
							break;
						case 2:
							equipment = lequipment.toArray(new ItemStack[lequipment.size()]);
							z.getEquipment().setArmorContents(equipment);
							z.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));					
							break;
						case 3:
							equipment = iequipment.toArray(new ItemStack[iequipment.size()]);
							z.getEquipment().setArmorContents(equipment);
							z.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
							break;
						case 4:
							equipment = gequipment.toArray(new ItemStack[gequipment.size()]);
							z.getEquipment().setArmorContents(equipment);
							z.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
							break;
						case 5:
							equipment = dequipment.toArray(new ItemStack[dequipment.size()]);
							z.getEquipment().setArmorContents(equipment);
							z.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
							break;
						default:
							equipment = dequipment.toArray(new ItemStack[dequipment.size()]);
							z.getEquipment().setArmorContents(equipment);
							z.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
					}
				}
			}
		}
		else if(ent instanceof Skeleton)
		{					
			Skeleton s = (Skeleton)ent;

			// /lm spawnmob has an issue of spawning skellies withotu bows, so make sure it has a type and bow:
			if (s.getSkeletonType() == SkeletonType.WITHER)
			{
				s.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
				s.setSkeletonType(SkeletonType.WITHER);
			}
			else if (s.getSkeletonType() == SkeletonType.NORMAL)
			{
				s.getEquipment().setItemInHand(new ItemStack(Material.BOW));
				s.setSkeletonType(SkeletonType.NORMAL);
			} else {
				s.getEquipment().setItemInHand(new ItemStack(Material.BOW));
				s.setSkeletonType(SkeletonType.NORMAL);
			}
			
			if(Sarmor == true)
			{			
				double armorLevel = getConfig().getDouble("skeleton.armor.level");
				armorLevel = Math.ceil(level/armorLevel);
				int spawnChance = Integer.valueOf(getConfig().getString("skeleton.armor.chance").replace("%", ""));

				if(r.nextInt(100) <= spawnChance)
				{
					switch ((int) armorLevel)
					{
						case 1: 
							break;
						case 2:
							equipment = lequipment.toArray(new ItemStack[lequipment.size()]);
							s.getEquipment().setArmorContents(equipment);
							break;
						case 3:
							equipment = iequipment.toArray(new ItemStack[iequipment.size()]);
							s.getEquipment().setArmorContents(equipment);
							break;
						case 4:
							equipment = gequipment.toArray(new ItemStack[gequipment.size()]);
							s.getEquipment().setArmorContents(equipment);
							break;
						case 5:
							equipment = dequipment.toArray(new ItemStack[dequipment.size()]);
							s.getEquipment().setArmorContents(equipment);
							break;
						default:
							equipment = dequipment.toArray(new ItemStack[dequipment.size()]);
							s.getEquipment().setArmorContents(equipment);
					}							
				}
			}
		}		
	}
	
	/**
	 * Sets the level to the mob, seperate so we can spawn with /lm spawnmob
	 * which we are using to spawn mobs at a specific level.
	 * -- eidalac 4/26
	 * */
	private void doSetLevel(LivingEntity ent, double level)
	{
		// Record the level to metadata:
		ent.setMetadata("hasLevel", new FixedMetadataValue(this, level));
		
		// check for armor:
		doSetArmor(ent, level);

		// Set the attributes:
		setHealthMultiplier((level * multiplier), ent);
		setSpeedMultiplier(level * speedMultiplier, ent);
		setKnockbackResistanceMultiplier(level * knockbackResistanceMultiplier, ent);
		setFollowRangeMultiplier(level * followRangeMultiplier, ent);
		
		// It seems ghasts don't work with this attribute, as they do not have a melee attack at all.
//		if (!(ent instanceof Ghast))
		try {
			setDamageMultiplier((level*damageMultiplier), ent);
		} catch (NullPointerException x) {
			// Slimes and ghasts don't work with this.
			// Bukkit.getServer().getConsoleSender().sendMessage("[LeveledMobs] Failed to set Damage for mob type " + ent.getType() + ".");
		}

		ent.setHealth(ent.getMaxHealth());

		// set the name:
		/**
		 * May add a config flag for this, to give some more options.
		 * For now it's just level ## creature.
		 * */
		ent.setCustomName(capitalizeFirstLetter(ent.getType().toString().toLowerCase()) + ChatColor.GOLD + " [Lvl " + (int) level + "]" + ChatColor.RESET);
		ent.setCustomNameVisible(getConfig().getBoolean("constantVisibility"));
	}

	public String capitalizeFirstLetter(String original)
	{
	    if(original.length() == 0)
	        return original;
	    return original.substring(0, 1).toUpperCase() + original.substring(1);
	}	
	
	public void setHealthMultiplier(double multiplier, LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.a);
		AttributeModifier modifier = new AttributeModifier(maxHealthUID, "LeveledMobs health multiplier", multiplier, 1);
		
		attributes.b(modifier);
		attributes.a(modifier);
	}
	
	public void clearHealthMultiplier(LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.a);
		AttributeModifier modifier = new AttributeModifier(maxHealthUID, "LeveledMobs health multiplier", 1.0d, 1);
		
		attributes.b(modifier);
	}	
		
	public void setFollowRangeMultiplier(double multiplier, LivingEntity entity)
	{
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.b);
		AttributeModifier modifier = new AttributeModifier(followRangeUID, "LeveledMobs follow range multiplier", multiplier, 1);

		attributes.b(modifier);
		attributes.a(modifier);
	}

	public void clearFollowRangeMultiplier(LivingEntity entity)
	{
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.b);
		AttributeModifier modifier = new AttributeModifier(followRangeUID, "LeveledMobs follow range multiplier", 1.0d, 1);

		attributes.b(modifier);
	}

	public void setKnockbackResistanceMultiplier(double multiplier, LivingEntity entity)
	{
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.c);
		AttributeModifier modifier = new AttributeModifier(knockbackResistanceUID, "LeveledMobs knockback resistance multiplier", multiplier, 1);
		AttributeModifier modifieradd = new AttributeModifier(addknockbackResistanceUID, "LeveledMobs knockback addition", 0.005, 0);
		
		if (attributes.getValue() == 0)
		{
			attributes.b(modifieradd);
			attributes.a(modifieradd);
		}
		
		attributes.b(modifier);
		attributes.a(modifier);
	}

	public void clearKnockbackResistanceMultiplier(LivingEntity entity)
	{
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.c);
		AttributeModifier modifier = new AttributeModifier(knockbackResistanceUID, "LeveledMobs knockback resistance multiplier", 1.0d, 1);
		AttributeModifier modifieradd = new AttributeModifier(addknockbackResistanceUID, "LeveledMobs knockback addition", 0, 0);

		attributes.b(modifier);
		attributes.b(modifieradd);
	}
	
	public void setSpeedMultiplier(double multiplier, LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);
		AttributeModifier modifier = new AttributeModifier(movementSpeedUID, "LeveledMobs movement speed multiplier", multiplier, 1);
		
		attributes.b(modifier);
		attributes.a(modifier);
	}
	
	public void clearSpeedMultiplier(LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);
		AttributeModifier modifier = new AttributeModifier(movementSpeedUID, "LeveledMobs movement speed multiplier", 1.0d, 1);
		
		attributes.b(modifier);
	}	

	public void setDamageMultiplier(double multiplier, LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.e);
		AttributeModifier modifier = new AttributeModifier(attackDamageUID, "LeveledMobs damage multiplier", multiplier, 1);
		
		attributes.b(modifier);
		attributes.a(modifier);
	}
	
	public void clearDamageMultiplier(LivingEntity entity)
	{
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.e);
		AttributeModifier modifier = new AttributeModifier(attackDamageUID, "LeveledMobs damage multiplier", 1.0d, 1);
		
		attributes.b(modifier);
	}		

	
 	/**
	 * Deals level based damaged
	 * 
	 * setDamageMultiplier() will only affect melee attacks, so we need to catch projectile damage here to
	 * boost it as well.
	 * 
	 * Slimes have the same issue not using the attibute for damage (due to the way it scales on size), so 
	 * have to use a special case for them as well.
	 * 
	 * -- eidalac 5/3.
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void EntityDamageEvent(EntityDamageEvent e)
	{
		if (! (e instanceof EntityDamageByEntityEvent))
			return;

		// Was this an projectile?  THats the only case we care about now.
		if (e.getCause() != DamageCause.PROJECTILE)
			return;
		
		// Not alive?  Ignore.
		if (!(e.getEntity() instanceof LivingEntity))
			return;

		LivingEntity ent = (LivingEntity)e.getEntity();	
		String entWorld = ent.getWorld().getName().replace("CraftWorld{name=", "");
		entWorld.replace("}", "");

		// Not in the list of leveled mobs worlds?  Ignore.
		if (! worlds.contains(entWorld))
			return;

		String entityTypeName = ent.getClass().getName().toString().substring(ent.getClass().getName().toString().indexOf(".", 31) + 6, ent.getClass().getName().toString().length()).toLowerCase();

		EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

		// Did a player do it?  Ignore it.
		if (event.getDamager() instanceof Player)
			return;

		// Did a villager get hit?  Ignore it.
		if (e.getEntity() instanceof Villager)
			return;

		// Does it have no level (and is not a player)?  Ignore it.
		if (!(e.getEntity() instanceof Player))
		{
			if (getCreatureLevel((LivingEntity) e.getEntity()) == 0) 
				return;
		}

		//is the critter on the exempt list?  Ignore it.
		if (getConfig().getStringList("generalSettings.worldLocations." + entWorld + ".exemptedMobs").contains(entityTypeName))
			return;

		// If it was an Projectile, get the shooters level for damage.
		if (event.getDamager() instanceof Projectile)
		{
			Projectile r = (Projectile) event.getDamager();

			if (r.getShooter() == null)
				return;

			if (!(r.getShooter() instanceof LivingEntity))
				return;

			e.setDamage(e.getDamage() + (e.getDamage() * (getCreatureLevel((LivingEntity) r.getShooter()) * damageMultiplier)));
		}
		else if (event.getDamager() instanceof Slime)
		{
			// Slimes don't use the damage attribue, so have to do them this way:
			e.setDamage(e.getDamage() + (e.getDamage() * (getCreatureLevel((LivingEntity) event.getDamager()) * damageMultiplier)));
		}
	}

	
	
	/**
	 * Handles special loot drops on death.
	 * 
	 * Tiddied up the code for my own readability.  No mechanical changes.
	 * -- eidalac 4/26.
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void EntityDeathEvent(EntityDeathEvent e)
	{
		Entity ent = e.getEntity();
		
		// Not living?  Ignore it.
		if (! (ent instanceof LivingEntity))
			return;

		// Player?  Ignore it.
		if (ent instanceof Player)
			return;
		
		String entWorld = ent.getWorld().getName().replace("CraftWorld{name=", "");
		entWorld.replace("}", "");
		String entityTypeName = ent.getClass().getName().toString().substring(ent.getClass().getName().toString().indexOf(".", 31) + 6, ent.getClass().getName().toString().length()).toLowerCase();

		// Exempt  mob?  Ignore it.
		if (getConfig().getStringList("generalSettings.worldLocations." + entWorld + ".exemptedMobs").contains(entityTypeName))
			return;
		
		// Drops not on?  Ignore it.
		if (dropLegendarys != true)
			return;

		double creatureLevel = getCreatureLevel(e.getEntity());
				
		if (eliteMobs.contains(ent.getUniqueId()))
		{
			if (eliteDrops == true)
			{
				Random r = new Random();
				for(String legendaryItem : getConfig().getStringList("legendaryItems.legendaryNames"))
				{
					if(getConfig().getInt("legendaryItems." + legendaryItem + ".dropLevel") <= creatureLevel)
					{
						legendaryItemList.add(createLegendary(legendaryItem));
					}
				}
				if(legendaryItemList.size() >= 1)
				{
					e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), legendaryItemList.get(r.nextInt(legendaryItemList.size())));
				}
			}
			eliteMobs.remove(ent.getUniqueId());
		}
		else if (mobsThatDropLegendarys.contains(entityTypeName))
		{
			int chance = Integer.valueOf(getConfig().getString("legendaryItems.normalMobs.dropChance").replace("%", ""));
			Random r = new Random();
			if (r.nextInt(100) <= chance)
			{
				for(String legendaryItem : getConfig().getStringList("legendaryItems.legendaryNames"))
				{
					if(getConfig().getInt("legendaryItems." + legendaryItem + ".dropLevel") <= creatureLevel)
					{
						legendaryItemList.add(createLegendary(legendaryItem));
					}
				}
				if(legendaryItemList.size() >= 1)
				{
					e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), legendaryItemList.get(r.nextInt(legendaryItemList.size())));
				}
			}
		}
	}

	/**
	 * Finds the nearest 'spawn' or '0' distance point, for calculating distance for levels.
	 * 
	 * Updated to use a List<String> and ListIterator<String>, as the old code had it reading the array twice,
	 * when you can get it directly as a List from the config file. 
	 * -- Eidalac 4/27
	 * */
	public Location getNearestLocation(LivingEntity ent, ArrayList<String> worlds, String worldName)
	{
		Location primaryLoc = ent.getLocation();
		Location tempLoc = ent.getLocation();

		// Gets the list of spawn locations.
		List<String> centralSpawns = getConfig().getStringList("generalSettings.worldLocations." + worldName + ".centralSpawns");
		ListIterator<String> spawnsList = centralSpawns.listIterator();
		
		// Gets the default 'spawn1' location.
		primaryLoc.setWorld(ent.getWorld());
		primaryLoc.setX(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations.spawn1.x"));
		primaryLoc.setY(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations.spawn1.y"));
		primaryLoc.setZ(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations.spawn1.z"));

		// Walk through the list, checking if this location is closer than spawn1.
		while (spawnsList.hasNext())
		{
			String spawn = spawnsList.next();

			tempLoc.setX(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".x"));
			tempLoc.setY(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".y"));
			tempLoc.setZ(getConfig().getDouble("generalSettings.worldLocations." + worldName + ".spawnLocations." + spawn + ".z"));

			if (tempLoc.distance(ent.getLocation()) < primaryLoc.distance(ent.getLocation()))
			{
				primaryLoc = tempLoc;
			}
		}
		return primaryLoc;
	}

	/**
	 * Returns the level of the entity, if any.  0 by default.
	 * 
	 * Now doing a reverse calculation based on attribute.
	 * Using damage mod, but can use any, really, since the meta data isn't saved on a restart.
	 * 
	 * If there is not an attribute modifier, returns 0.
	 * Otheriwise caculates level from the modifier and returns it.
	 * 
	 * I'd prefere to use damage, since health could be otherwise modified by other plugins/settings,
	 * but I found that Ghasts throw an error on setting damage attribute modifiers, so that's not a good
	 * option.
	 *  
	 * -- Eidalac 5/3
	 * */
	public int getCreatureLevel(LivingEntity entity)
	{
		// First, see if there is a valid meta data level stored:
        if (entity.hasMetadata("hasLevel"))
            return entity.getMetadata("hasLevel").get(0).asInt();
   
        // No meta data, so calculate it:
		EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.a);
		AttributeModifier mod = null;
		
		mod = attributes.a(maxHealthUID);
		
		// We haven't set an attribute value on this entity
		if (mod == null)
			return 0;

		double m = mod.d();
		
		// Is it the default value?  treat it as level 0.
		if (m == 1.0d)
			return 0; 
		
		double l = (m/this.multiplier);
		
		// Store it to meta data to save time on the next lookup:
		entity.setMetadata("hasLevel", new FixedMetadataValue(this, l));
		
		return (int) l;
	}

	public ItemStack createLegendary(String itemName)
	{
		ArrayList<String>lore = new ArrayList<String>();
		ItemStack legendary = new ItemStack(Material.getMaterial(getConfig().getString("legendaryItems." + itemName + ".material")));
		ItemMeta legendaryMeta = legendary.getItemMeta();
		legendaryMeta.setDisplayName(getConfig().getString("legendaryItems." + itemName + ".name"));

		for(String loretobeadded : getConfig().getStringList("legendaryItems." + itemName + ".lore"))
		{
			lore.add("" + loretobeadded);
		}

		legendaryMeta.setLore(lore);
		legendary.setItemMeta(legendaryMeta);

		for(String enchantment : getConfig().getStringList("legendaryItems." + itemName + ".enchantments"))
		{
			int enchantLevel = getConfig().getIntegerList("legendaryItems." + itemName + ".enchantmentLevels").get(getConfig().getStringList("legendaryItems." + itemName + ".enchantments").indexOf(enchantment));
			legendary.addUnsafeEnchantment(Enchantment.getByName(enchantment), enchantLevel);
		}

		return legendary;
	}
}
