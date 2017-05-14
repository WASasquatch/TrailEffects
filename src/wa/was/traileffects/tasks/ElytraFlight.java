package wa.was.traileffects.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wa.was.traileffects.utils.Utilities;

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

public class ElytraFlight extends BukkitRunnable {

	private FileConfiguration config;

	private Map<UUID, Location> players;

	public ElytraFlight(FileConfiguration config) {
		this.config = config;
		this.players = new HashMap<UUID, Location>();
	}

	public void addPlayer(UUID uuid) {
		if (Bukkit.getServer().getPlayer(uuid) == null)
			return;
		if (this.players.containsKey(uuid))
			return;
		Player p = Bukkit.getServer().getPlayer(uuid);
		this.players.put(uuid, p.getLocation());
	}
	
	public void removeAllPlayers() {
		this.players.clear();
	}

	public boolean removePlayer(UUID uuid) {
		if (!(this.players.containsKey(uuid)))
			return false;
		return this.players.remove(uuid, players.get(uuid));
	}

	@Override
	public void run() {
		if (this.players.size() > 0) {
			for (Map.Entry<UUID, Location> entry : players.entrySet()) {
				Player p = Bukkit.getServer().getPlayer(entry.getKey());
				if (p == null || !(p.isOnline())) {
					removePlayer(entry.getKey());
					continue;
				}
				if (p.getLocation().equals(entry.getValue()))
					return;
				if (config.getBoolean("enable-particle-effects", true) && p.hasPermission("traile.allow.particles")
						|| p.hasPermission("traile.allow")) {

					if (config.getStringList("particle-rgb-values").size() > 0) {

						Map<Integer, List<Float>> particleColors = new HashMap<Integer, List<Float>>();

						int cur = 0;
						for (String v : config.getStringList("particle-rgb-values")) {

							String[] split = v.trim().replaceAll("\\s", "").split(",");

							particleColors.put(cur, new ArrayList<Float>() {
								private static final long serialVersionUID = -4911421726032782551L;
								{
									add((float) (Double.parseDouble(split[0]) / 255));
									add((float) (Double.parseDouble(split[1]) / 255));
									add((float) (Double.parseDouble(split[2]) / 255));
								}
							});

							cur++;

						}

						for (int i = 0; i < config.getInt("particle-count", 5); i++) {

							List<Float> particle = particleColors
									.get(Utilities.randomInteger(0, particleColors.size() - 1));
							p.spawnParticle(Particle.SPELL_MOB, p.getLocation(), 0, particle.get(0), particle.get(1),
									particle.get(2), 1);

						}

					}

				}
			}
		}
	}

}
