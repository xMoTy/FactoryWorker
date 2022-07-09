package me.moty.fw;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class FWFactory {
	private ItemStack item;
	private Location loc, center;
	private int period;
	private int count;
	private UUID certain = null;

	public FWFactory(ItemStack item, Location loc, int period, int count) {
		this.item = item;
		this.loc = loc;
		this.period = period;
		this.count = count;
		this.center = loc.clone().add(0.5, 1.5, 0.5);
	}

	public ItemStack getItem() {
		return item.clone();
	}

	public Location getLocation() {
		return loc.clone();
	}

	public Location getCentralLocation() {
		return center;
	}

	public int getPeriod() {
		return period;
	}

	public int getCount() {
		return count;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean hasUUID() {
		return certain != null;
	}

	public void setUUID(UUID uuid) {
		this.certain = uuid;
	}

	public UUID getUUID() {
		return certain;
	}

}
