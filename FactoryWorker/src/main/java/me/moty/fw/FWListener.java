package me.moty.fw;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class FWListener implements Listener {

	private FactoryWorker m;

	public FWListener(FactoryWorker m) {
		this.m = m;
	}

	@EventHandler
	public void breakevent(BlockBreakEvent e) {
		if (e.getBlock().getType() != Material.CHEST && e.getBlock().getType() != Material.FURNACE
				&& e.getBlock().getType() != Material.HOPPER)
			return;
		if (!m.hasFactory())
			return;
		m.getFactories().stream().forEach(key -> {
			FWFactory fac = m.getFactory(key);
			if (e.getBlock().getLocation().getX() == fac.getLocation().getX()
					&& e.getBlock().getLocation().getZ() == fac.getLocation().getZ()
					&& e.getBlock().getLocation().getY() >= fac.getLocation().getY()
					&& e.getBlock().getLocation().getY() <= fac.getLocation().getY() + 2.0D)
				e.setCancelled(true);
		});
	}
}
