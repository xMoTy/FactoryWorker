package me.moty.fw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class FWTabCompleter implements TabCompleter {
	private FactoryWorker m;

	public FWTabCompleter(FactoryWorker m) {
		this.m = m;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!sender.isOp())
			if (args.length == 1)
				return Arrays.asList("report");
			else
				return null;
		if (args.length == 1)
			return Arrays.asList("list", "create", "show", "info", "rename", "modify", "remove", "tp");
		if (args.length == 2) {
			if (!m.hasFactory())
				return null;
			return new ArrayList<>(m.getFactories());
		}
		return null;
	}
}
