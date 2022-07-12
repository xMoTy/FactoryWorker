package me.moty.fw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class FactoryWorker extends JavaPlugin {

	private static FactoryWorker instance;

	private HashMap<String, Long> time = new HashMap<>();
	private HashMap<String, Long> differenceTime = new HashMap<>();
	public HashMap<String, BossBar> bossBar = new HashMap<>();

	private HashMap<String, FWFactory> factories = new HashMap<>();
	public List<UUID> notify = new ArrayList<>();
	private FWMessages msgs;
	private FileConfiguration config;
	private File cfile;
	public boolean isNewerVer = false;

	@Override
	public void onEnable() {
		instance = this;
		int version = Integer.parseInt(getServer().getClass().getPackage().getName().split("_")[1]);
		if (version < 13) {
			getLogger().info(colorize("&cThe plugin doesn't support the version under 1.13!"));
			getServer().getPluginManager().disablePlugin(this);
		}
		if (version > 16)
			isNewerVer = true;
		getLogger().info(colorize(""));
		getLogger().info(colorize("&3FactoryWorker &fEnabled"));
		getLogger().info(colorize("&fPowered by xMoTy#3812 | Version. " + getDescription().getVersion()));
		getLogger().info(colorize(""));

		msgs = new FWMessages(this);
		reloadConfiguration();

		PluginCommand cmd = getCommand("factoryworker");
		cmd.setAliases(Arrays.asList("fw"));
		cmd.setExecutor(new FWCommand(this));
		cmd.setTabCompleter(new FWTabCompleter(this));

		getServer().getPluginManager().registerEvents(new FWListener(this), this);

		new Metrics(this, 8573);

		runTask();
	}

	@Override
	public void onDisable() {
		save();
		for (String str : bossBar.keySet()) {
			BossBar bossbar = bossBar.get(str);
			bossbar.setVisible(false);
			bossbar.removeAll();
		}
	}

	public void reloadConfiguration() {
		cfile = new File(getDataFolder().getAbsolutePath() + "/config.yml");
		if (!cfile.exists()) {
			getDataFolder().mkdir();
			try {
				cfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		reloadConfig();
		config = getConfig();
		if (config.isSet("report"))
			notify.addAll(config.getStringList("report").stream().map(str -> UUID.fromString(str)).toList());

		if (!config.isSet("fw"))
			return;
		ConfigurationSection cs = config.getConfigurationSection("fw");
		if (cs == null)
			return;
		cs.getKeys(false).stream().forEach(f -> {
			int count = cs.getInt(f + ".count");
			ItemStack item = cs.getItemStack(f + ".item") == null
					? new ItemStack(Material.matchMaterial(cs.getString(f + ".item")))
					: cs.getItemStack(f + ".item");
			if (item.getAmount() > 1)
				item.setAmount(1);
			Location loc = new Location(getServer().getWorld(cs.getString(f + ".w")), cs.getDouble(f + ".x"),
					cs.getDouble(f + ".y"), cs.getDouble(f + ".z"));
			int period = cs.getInt(f + ".period");
			FWFactory fac = new FWFactory(item, loc, period, count);
			factories.put(f, fac);
		});
	}

	public void save() {
		config.set("fw", null);
		factories.keySet().stream().forEach(str -> {
			FWFactory fac = factories.get(str);
			config.set("fw." + str + ".w", fac.getLocation().getWorld().getName());
			config.set("fw." + str + ".x", fac.getLocation().getX());
			config.set("fw." + str + ".y", fac.getLocation().getY());
			config.set("fw." + str + ".z", fac.getLocation().getZ());
			config.set("fw." + str + ".count", fac.getCount());
			config.set("fw." + str + ".period", fac.getPeriod());
			config.set("fw." + str + ".item",
					fac.getItem().hasItemMeta() ? fac.getItem() : fac.getItem().getType().name());
		});
		if (!notify.isEmpty())
			config.set("report", notify.stream().toList());

		try {
			config.save(cfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FWFactory getFactory(String name) {
		return factories.get(name);
	}

	public void renameFactory(String from, String to) {
		factories.put(to, factories.get(from));
		factories.remove(from);
	}

	public void addFactory(String name, FWFactory fac) {
		factories.put(name, fac);
	}

	public boolean hasFactory() {
		return !factories.isEmpty();
	}

	public void deleteFactory(String name) {
		factories.remove(name);
	}

	public Set<String> getFactories() {
		return factories.keySet();
	}

	public static FactoryWorker getInstance() {
		return instance;
	}

	public void runTask() {
		this.getServer().getScheduler().runTaskTimer(this, () -> {
			if (factories.isEmpty())
				return;
			factories.keySet().stream().forEach(name -> {
				FWFactory fac = factories.get(name);
				Player p = !fac.hasUUID() || !isPlayerInArea(fac.getCentralLocation(), fac.getUUID())
						? getActivePlayerInArea(fac.getCentralLocation())
						: getServer().getPlayer(fac.getUUID());
				if (p == null) {
					if (!time.containsKey(name))
						return;
					if (bossBar.containsKey(name)) {
						bossBar.get(name).setVisible(false);
						bossBar.remove(name);
					}
					differenceTime.put(name, time.get(name) - System.currentTimeMillis());
					time.remove(name);
					fac.setUUID(null);
					return;
				}
				if (!fac.hasUUID())
					fac.setUUID(p.getUniqueId());
				if (!time.containsKey(name) && differenceTime.containsKey(name)) {
					time.put(name, System.currentTimeMillis() + differenceTime.get(name));
					differenceTime.remove(name);
				}
				if (!time.containsKey(name) || time.get(name).longValue() < System.currentTimeMillis()) {
					String itemName = fac.getItem().hasItemMeta() && fac.getItem().getItemMeta().hasDisplayName()
							? fac.getItem().getItemMeta().getDisplayName()
							: StringUtils.capitalize(fac.getItem().getType().name().toLowerCase().replace("_", " "));
					if (!notify.contains(p.getUniqueId()))
						p.sendMessage(colorize(msgs.produce_notify.replace("%name%", name)
								.replace("%amount%", String.valueOf(fac.getCount())).replace("%item%", itemName)));
					time.put(name, System.currentTimeMillis() + fac.getPeriod() * 1000);
					ItemStack item = fac.getItem();
					if (item.getAmount() != fac.getCount())
						item.setAmount(fac.getCount());
					fac.getLocation().getWorld().dropItem(fac.getLocation().add(0.5d, 2.8d, 0.5d), item);
				}
				String title = colorize(msgs.progressBar
						.replace("%min%",
								String.valueOf(fac.getPeriod()
										- (((time.get(name).longValue() - System.currentTimeMillis()) / 1000))))
						.replace("%max%", String.valueOf(fac.getPeriod())));
				double progress = (fac.getPeriod() - (time.get(name).longValue() - System.currentTimeMillis()) / 1000L)
						/ ((double) fac.getPeriod());
				BossBar bossbar = bossBar.containsKey(name) ? bossBar.get(name)
						: getServer().createBossBar(title, BarColor.YELLOW, BarStyle.SOLID);
				if (!bossbar.isVisible())
					bossbar.setVisible(true);
				bossbar.setTitle(title);
				bossbar.setProgress(progress);
				if (!notify.contains(p.getUniqueId()) && !bossbar.getPlayers().contains(p))
					bossbar.addPlayer(p);
				if (!bossBar.containsKey(name))
					bossBar.put(name, bossbar);
			});
		}, 20, 20);

	}

	public Player getActivePlayerInArea(Location loc) {
		Player p = null;
		int players = 0;
		for (double x = (loc.getBlockX() - 1); x <= (loc.getBlockX() + 1); x++)
			for (double y = (loc.getBlockY() - 1); y <= (loc.getBlockY() + 1); y++)
				for (double z = (loc.getBlockZ() - 1); z <= (loc.getBlockZ() + 1); z++) {
					Location n = new Location(loc.getWorld(), x + 0.5, y + 0.5, z + 0.5);
					for (Entity e : loc.getWorld().getNearbyEntities(n, 0.2D, 0.1D, 0.2D)) {
						if (!(e instanceof Player))
							continue;
						if (p != null && p == (Player) e)
							continue;
						p = (Player) e;
						players++;
					}
				}
		return players == 1 ? p : null;
	}

	public boolean isPlayerInArea(Location loc, UUID uuid) {
		if (!getServer().getOfflinePlayer(uuid).isOnline())
			return false;
		return getServer().getPlayer(uuid).getLocation().distanceSquared(loc) <= 2.2d * 2.2d;
	}

	public FWMessages getMessage() {
		return msgs;
	}

	public String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public List<String> colorize(List<String> msgs) {
		for (String s : msgs) {
			msgs.remove(s);
			msgs.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		return msgs;
	}
}
