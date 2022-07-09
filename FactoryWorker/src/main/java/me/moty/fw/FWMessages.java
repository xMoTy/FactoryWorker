package me.moty.fw;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;

public class FWMessages {

	public FileConfiguration msgs;
	private File cfile;

	public String produce_notify, create_Overlap, create_successed, delete_successed, name_exists, name_doesNotExists,
			noNearbyFactory, showFactoriesInSight, rename_Factory, delete_confirm, nothing_to_show, turnOn_Notification,
			turnOff_Notification, progressBar;
	private FactoryWorker m;

	public FWMessages(FactoryWorker m) {
		this.m = m;
		reloadMessages();
	}

	public List<String> help = new ArrayList<>(), info = new ArrayList<>(), modify = new ArrayList<>();

	public void reloadMessages() {
		cfile = new File(m.getDataFolder(), "messages.yml");
		if (!cfile.exists()) {
			m.getDataFolder().mkdir();
			m.saveResource("messages.yml", false);
		}
		msgs = YamlConfiguration.loadConfiguration(cfile);
		InputStream defConfigStream = m.getResource("messages.yml");
		if (defConfigStream == null)
			return;
		this.msgs.setDefaults(
				YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));

		produce_notify = msgs.getString("messages.produced");
		create_Overlap = msgs.getString("messages.createOverlap");
		create_successed = msgs.getString("messages.createSuccessed");
		delete_successed = msgs.getString("messages.deleteSuccessed");
		name_exists = msgs.getString("messages.nameExists");
		name_doesNotExists = msgs.getString("messages.nameDoesNotExists");
		noNearbyFactory = msgs.getString("messages.noFactoryNearby");
		showFactoriesInSight = msgs.getString("messages.showFactoriesInSight");
		rename_Factory = msgs.getString("messages.renameSuccessed");
		delete_confirm = msgs.getString("messages.deleteConfirm");
		nothing_to_show = msgs.getString("messages.nothingShow");
		turnOn_Notification = msgs.getString("messages.turnOnNotification");
		turnOff_Notification = msgs.getString("messages.turnOffNotification");
		progressBar = msgs.getString("messages.progressbar");

		info = msgs.getStringList("info");
		help = msgs.getStringList("help");
		modify = msgs.getStringList("modify");

	}

	public void save() {
		try {
			msgs.save(cfile);
			msgs.load(cfile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
