package wa.was.traileffects;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import wa.was.traileffects.events.MoveEvents;
import wa.was.traileffects.events.OnBreak;
import wa.was.traileffects.events.OnFall;
import wa.was.traileffects.events.OnJump;
import wa.was.traileffects.events.OnLanded;
import wa.was.traileffects.events.OnQuit;

/*************************
 * 
 * Copyright (c) 2017 Jordan Thompson (WASasquatch)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 *************************/

public class TrailEffects extends JavaPlugin implements Listener {

	private static TrailEffects instance;

	public static TrailEffects getInstance() {
		return instance;
	}

	private FileConfiguration config;

	public TrailEffects() {
		instance = this;
	}

	private void createConfig() {
		try {
			if (!(getDataFolder().exists())) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!(file.exists())) {
				getLogger().info("Config.yml not found, creating it for you!");
				saveDefaultConfig();
			} else {
				getLogger().info("Config.yml found, loading!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		MoveEvents move = MoveEvents.getInstance();
		move.removeAllEntries();
		move.flight.cancel();
		move.flight.removeAllPlayers();
	}

	@Override
	public void onEnable() {

		createConfig();
		this.config = getConfig();

		getServer().getPluginManager().registerEvents(new MoveEvents(), this);
		getServer().getPluginManager().registerEvents(new OnFall(), this);
		getServer().getPluginManager().registerEvents(new OnJump(), this);
		getServer().getPluginManager().registerEvents(new OnLanded(), this);
		getServer().getPluginManager().registerEvents(new OnQuit(), this);
		getServer().getPluginManager().registerEvents(new OnBreak(), this);

		MoveEvents me = MoveEvents.getInstance();
		if (config.getStringList("protected-blocks").size() > 0) {
			for (String mat : config.getStringList("protected-blocks")) {
				if (Material.valueOf(mat) != null)
					me.addInvalidBlock(Material.valueOf(mat));
			}
		}
	}

}
