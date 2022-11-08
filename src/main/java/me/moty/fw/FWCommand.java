package me.moty.fw;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class FWCommand implements CommandExecutor {
	private FactoryWorker m;
	private HashMap<String, String> delete = new HashMap<>();

	public FWCommand(FactoryWorker m) {
		this.m = m;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player p = null;
		if (sender instanceof Player)
			p = (Player) sender;
		if (args.length == 0) {
			for (String s : m.getMessage().help)
				sender.sendMessage(m.colorize(s));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			int fws = m.getFactories().size();
			int nowPage = args.length < 2 ? 1 : Integer.parseInt(args[1]);
			int allPages = 1;
			if (fws % 6 == 0)
				allPages = fws / 6;
			else
				allPages = (fws / 6) + 1;
			if (nowPage > allPages)
				nowPage = allPages;
			TextComponent tc = new TextComponent("");
			TextComponent tc2 = new TextComponent("");
			if (fws == 0) {
				sender.sendMessage(m.colorize(m.getMessage().nothing_to_show));
				return false;
			}
			sender.sendMessage(
					m.colorize("&e-----< Page &6" + nowPage + " &e/&6 " + allPages + " &e(&6" + fws + "&e) >-----"));
			for (int i = (nowPage * 6) - 6; i < (nowPage * 6); i++) {
				if (i >= m.getFactories().size())
					break;
				String name = m.getFactories().stream().toList().get(i);
				FWFactory fac = m.getFactory(name);
				tc = new TextComponent(m.colorize(" &a" + String.valueOf(i + 1) + ". &e" + name + " &a[Teleport]"));
				String itemName = fac.getItem().hasItemMeta() && fac.getItem().getItemMeta().hasDisplayName()
						? fac.getItem().getItemMeta().getDisplayName()
						: StringUtils.capitalize(fac.getItem().getType().name().toLowerCase().replace("_", " "));
				if (m.isNewerVer) {
					tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							(new Text(m.colorize("&eCoords: &eX:&6" + fac.getLocation().getX() + " &eY:&6"
									+ fac.getLocation().getY() + " &eZ:&6" + fac.getLocation().getZ() + "\n"
									+ "&ePeriod: &6" + fac.getPeriod() + " second(s)" + "\n" + "&eItem: &6" + itemName
									+ "\n" + "&eAmount: &6x" + fac.getCount())))));
				} else {
					tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							(new ComponentBuilder(m.colorize("&eCoords: &eX:&6" + fac.getLocation().getX() + " &eY:&6"
									+ fac.getLocation().getY() + " &eZ:&6" + fac.getLocation().getZ() + "\n"
									+ "&ePeriod: &6" + fac.getPeriod() + " second(s)" + "\n" + "&eItem: &6" + itemName
									+ "\n" + "&eAmount: &6x" + fac.getCount()))).create()));
				}
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fw tp " + name));
				sender.spigot().sendMessage(tc);
			}
			tc = new TextComponent(m.colorize("&e------< &6Prev "));
			tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fw list " + String.valueOf(nowPage - 1)));
			if (m.isNewerVer)
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(m.colorize("&a<<"))));
			else
				tc.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(m.colorize("&a<<")).create()));
			tc2 = new TextComponent(m.colorize("&6Next &e>------"));
			tc2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fw list " + String.valueOf(nowPage + 1)));
			if (m.isNewerVer)
				tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(m.colorize("&a>>"))));
			else
				tc2.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(m.colorize("&a>>")).create()));
			tc.addExtra(m.colorize(" &7&l| "));
			tc.addExtra(tc2);
			sender.spigot().sendMessage(tc);
			return true;
		} else if (args[0].equalsIgnoreCase("create")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (args.length < 5)
				return false;
			if (m.getFactory(args[1]) != null) {
				sender.sendMessage(m.colorize(m.getMessage().name_exists));
				return false;
			}
			if (args[3].equalsIgnoreCase("hand") && (p.getInventory().getItemInMainHand() == null
					|| p.getInventory().getItemInMainHand().getType().isAir()))
				return false;
			final Location loc = p.getLocation().getBlock().getLocation();
			if (m.getFactories().stream().map(name -> m.getFactory(name).getLocation())
					.anyMatch(locf -> locf.getWorld().equals(loc.getWorld()) && loc.distance(locf) <= 2.9D)) {
				sender.sendMessage(m.colorize(m.getMessage().create_Overlap));
				return false;
			}
			ItemStack item = args[3].equalsIgnoreCase("hand") ? p.getInventory().getItemInMainHand()
					: new ItemStack(Material.matchMaterial(args[3]));
			if (item.getAmount() > 1)
				item.setAmount(1);
			FWFactory fac = new FWFactory(item, loc.clone(), Integer.parseInt(args[2]), Integer.parseInt(args[4]));
			loc.getBlock().setType(Material.FURNACE);
			loc.add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.CHEST);
			loc.add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.HOPPER);
			m.addFactory(args[1], fac);
			sender.sendMessage(m.colorize(m.getMessage().create_successed.replace("%name%", args[1])));
		} else if (args[0].equalsIgnoreCase("show")) {
			sender.sendMessage(m.colorize(m.getMessage().showFactoriesInSight));
			for (String key : m.getFactories()) {
				FWFactory fac = m.getFactory(key);
				Location loc = fac.getLocation().add(1, 1, 0);
				if (loc.getWorld().equals(p.getWorld()) && loc.distance(p.getLocation()) <= 100.0D)
					for (int i = 0; i <= 30; i++) {
						m.getServer().getScheduler().runTaskLater(this.m, () -> {
							for (int x = -1; x <= 1; x++)
								for (int y = -1; y <= 1; y++)
									for (int z = -1; z <= 1; z++)
										loc.getWorld().playEffect(loc.clone().add(x, y, z), Effect.SMOKE, 10);
						}, i * 5L);
					}
			}
		} else if (args[0].equalsIgnoreCase("info")) {
			if (args.length == 2) {
				if (m.getFactory(args[1]) == null) {
					sender.sendMessage(m.colorize(m.getMessage().name_doesNotExists));
					return true;
				}
				String name = args[1];
				FWFactory fac = m.getFactory(name);
				String itemName = fac.getItem().hasItemMeta() && fac.getItem().getItemMeta().hasDisplayName()
						? fac.getItem().getItemMeta().getDisplayName()
						: StringUtils.capitalize(fac.getItem().getType().name().toLowerCase().replace("_", " "));
				for (String s : m.getMessage().info)
					sender.sendMessage(m
							.colorize(s.replace("%name%", name).replace("%x%", String.valueOf(fac.getLocation().getX()))
									.replace("%y%", String.valueOf(fac.getLocation().getY()))
									.replace("%z%", String.valueOf(fac.getLocation().getZ()))
									.replace("%period%", String.valueOf(fac.getPeriod())).replace("%item%", itemName)
									.replace("%amount%", String.valueOf(fac.getCount()))));
				return true;
			} else {
				for (String key : m.getFactories()) {
					FWFactory fac = m.getFactory(key);
					final Location loc = fac.getCentralLocation();
					String itemName = fac.getItem().hasItemMeta() && fac.getItem().getItemMeta().hasDisplayName()
							? fac.getItem().getItemMeta().getDisplayName()
							: StringUtils.capitalize(fac.getItem().getType().name().toLowerCase().replace("_", " "));
					if (loc.distance(p.getLocation()) <= 1.9D) {
						for (String s : m.getMessage().info)
							sender.sendMessage(m.colorize(s.replace("%name%", key)
									.replace("%x%", String.valueOf(fac.getLocation().getX()))
									.replace("%y%", String.valueOf(fac.getLocation().getY()))
									.replace("%z%", String.valueOf(fac.getLocation().getZ()))
									.replace("%period%", String.valueOf(fac.getPeriod())).replace("%item%", itemName)
									.replace("%amount%", String.valueOf(fac.getCount()))));
						return true;
					}
				}
				sender.sendMessage(m.colorize(m.getMessage().noNearbyFactory));
				return true;
			}
		} else if (args[0].equalsIgnoreCase("rename")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (args.length < 3)
				return true;
			if (m.getFactory(args[1]) == null) {
				sender.sendMessage(m.colorize(m.getMessage().name_doesNotExists));
				return true;
			}
			if (m.getFactory(args[2]) != null) {
				sender.sendMessage(m.colorize(m.getMessage().name_exists));
				return true;
			}
			sender.sendMessage(m.colorize(
					m.getMessage().rename_Factory.replace("%oldName%", args[1]).replace("%newName%", args[2])));
			if (m.bossBar.containsKey(args[1])) {
				m.bossBar.get(args[1]).setVisible(false);
				m.bossBar.remove(args[1]);
			}
		} else if (args[0].equalsIgnoreCase("modify")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (args.length < 5)
				return false;
			if (m.getFactory(args[1]) == null) {
				sender.sendMessage(m.colorize(m.getMessage().name_doesNotExists));
				return true;
			}
			FWFactory fac = m.getFactory(args[1]);
			String itemName = fac.getItem().hasItemMeta() && fac.getItem().getItemMeta().hasDisplayName()
					? fac.getItem().getItemMeta().getDisplayName()
					: StringUtils.capitalize(fac.getItem().getType().name().toLowerCase().replace("_", " "));
			int count = Integer.parseInt(args[4]);
			ItemStack item = args[3].equalsIgnoreCase("hand") ? p.getInventory().getItemInMainHand()
					: new ItemStack(Material.matchMaterial(args[3]), count);
			String newName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
					? item.getItemMeta().getDisplayName()
					: StringUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));
			int period = Integer.parseInt(args[2]);
			for (String s : m.getMessage().modify) {
				sender.sendMessage(m.colorize(s.replace("%period%", String.valueOf(fac.getPeriod()))
						.replace("%item%", itemName).replace("%amount%", String.valueOf(fac.getCount()))
						.replace("%newPeriod%", String.valueOf(period)).replace("%newItem%", newName)
						.replace("%newAmount%", String.valueOf(count))));
			}
			fac.setCount(count);
			fac.setPeriod(period);
			fac.setItem(item);
		} else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")
				|| args[0].equalsIgnoreCase("rev") || args[0].equalsIgnoreCase("remove")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (args.length < 2)
				return false;
			if (m.getFactory(args[1]) == null) {
				sender.sendMessage(m.colorize(m.getMessage().name_doesNotExists));
				return true;
			}
			if (!this.delete.containsKey(sender.getName())) {
				TextComponent tc = new TextComponent(
						m.colorize(m.getMessage().delete_confirm.replace("%name%", args[1])));
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fw confirm"));
				if (m.isNewerVer)
					tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(m.colorize("&aSubmit"))));
				else
					tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(m.colorize("&aSubmit")).create()));
				sender.spigot().sendMessage(tc);
				this.delete.put(sender.getName(), args[1]);
				m.getServer().getScheduler().runTaskLater(this.m, () -> {
					if (delete.containsKey(sender.getName()) && delete.get(sender.getName()) == args[1])
						delete.remove(sender.getName());
				}, 20 * 5);
			}
		} else if (args[0].equalsIgnoreCase("tp")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (!(sender instanceof Player))
				return false;
			if (args.length < 2)
				return false;
			if (m.getFactory(args[1]) == null) {
				sender.sendMessage(m.colorize(m.getMessage().name_doesNotExists));
				return true;
			}
			FWFactory fac = m.getFactory(args[1]);
			p.teleport(fac.getLocation());
		} else if (args[0].equalsIgnoreCase("confirm")) {
			if (!sender.hasPermission("fw.admin"))
				return false;
			if (!this.delete.containsKey(sender.getName()))
				return false;
			String name = this.delete.get(sender.getName());
			this.delete.remove(sender.getName());
			m.deleteFactory(name);
			if (m.bossBar.containsKey(name)) {
				m.bossBar.get(name).setVisible(false);
				m.bossBar.remove(name);
			}
			sender.sendMessage(m.colorize(m.getMessage().delete_successed.replace("%name%", name)));
		} else if (args[0].equalsIgnoreCase("report")) {
			if (m.notify.contains(p.getUniqueId())) {
				m.notify.remove(p.getUniqueId());
				sender.sendMessage(m.colorize(m.getMessage().turnOn_Notification));
			} else {
				m.notify.add(p.getUniqueId());
				sender.sendMessage(m.colorize(m.getMessage().turnOff_Notification));
			}
		} else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("fw.admin")) {
			m.reloadConfiguration();
			m.getMessage().reloadMessages();
			sender.sendMessage(m.colorize("&aReloaded Successfully!"));
		}
		return true;
	}
}
