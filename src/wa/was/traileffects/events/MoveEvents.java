package wa.was.traileffects.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import wa.was.traileffects.TrailEffects;
import wa.was.traileffects.tasks.ElytraFlight;
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

public class MoveEvents implements Listener {

	private static MoveEvents instance;

	public static MoveEvents getInstance() {
		return instance;
	}

	private List<DyeColor> colors;
	private FileConfiguration config;
	public ElytraFlight flight;
	private List<Material> invalidBlocks;
	private Map<UUID, Map<Location, Material>> lastBlock;

	private Map<UUID, Integer> lastInt;

	public MoveEvents() {

		instance = this;
		TrailEffects plugin = TrailEffects.getInstance();

		this.config = plugin.getConfig();

		lastBlock = new HashMap<UUID, Map<Location, Material>>();
		lastInt = new HashMap<UUID, Integer>();

		if (config.getBoolean("elytra-trail-effects", true)) {
			flight = new ElytraFlight(config);
			flight.runTaskTimer(plugin, 0L, 2L);
		}

		invalidBlocks = new ArrayList<Material>();
		invalidBlocks.add(Material.WHEAT);
		invalidBlocks.add(Material.CARROT);
		invalidBlocks.add(Material.YELLOW_FLOWER);
		invalidBlocks.add(Material.POTATO);
		invalidBlocks.add(Material.BEETROOT);
		invalidBlocks.add(Material.CHORUS_PLANT);
		invalidBlocks.add(Material.DOUBLE_PLANT);
		invalidBlocks.add(Material.SAPLING);
		invalidBlocks.add(Material.LONG_GRASS);
		invalidBlocks.add(Material.DEAD_BUSH);
		invalidBlocks.add(Material.RED_MUSHROOM);
		invalidBlocks.add(Material.BROWN_MUSHROOM);
		invalidBlocks.add(Material.RED_ROSE);
		invalidBlocks.add(Material.FIRE);
		invalidBlocks.add(Material.SIGN);
		invalidBlocks.add(Material.SIGN_POST);
		invalidBlocks.add(Material.MELON_STEM);
		invalidBlocks.add(Material.PUMPKIN_STEM);

		colors = new ArrayList<DyeColor>() {
			private static final long serialVersionUID = -4359563150089969767L;
			{
				add(DyeColor.RED);
				add(DyeColor.ORANGE);
				add(DyeColor.YELLOW);
				add(DyeColor.GREEN);
				add(DyeColor.BLUE);
				add(DyeColor.PURPLE);
				add(DyeColor.PINK);
			}
		};

		if (config.getBoolean("do-rainbow-effect", false)) {
			if (config.contains("wool-dye-colors") && config.getStringList("wool-dye-colors").size() > 0) {
				List<DyeColor> configColors = new ArrayList<DyeColor>();
				for (String dc : config.getStringList("wool-dye-colors")) {
					if (DyeColor.valueOf(dc) != null) {
						configColors.add(DyeColor.valueOf(dc));
					}
				}
				if (configColors.size() > 0) {
					colors.clear();
					colors.addAll(configColors);
				}
			}
		}

	}

	public boolean addInvalidBlock(Material mat) {
		return invalidBlocks.add(mat);
	}

	public boolean contains(UUID uuid) {
		return lastBlock.containsKey(uuid);
	}

	public Map<Location, Material> getEntry(UUID uuid) {
		if (lastBlock.containsKey(uuid)) {
			return lastBlock.get(uuid);
		}
		return null;
	}

	@EventHandler
	public void onGlideToggle(EntityToggleGlideEvent e) {

		if (e.getEntity() instanceof Player && e.isGliding() && config.getBoolean("elytra-trail-effects", true)) {

			flight.addPlayer(((Player) e.getEntity()).getUniqueId());

		} else {

			flight.removePlayer(((Player) e.getEntity()).getUniqueId());

		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent e) {

		if (e.isCancelled())
			return;
		if (e.getTo().getBlock().getLocation().equals(e.getFrom().getBlock().getLocation()))
			return;
		
		UUID uuid = e.getPlayer().getUniqueId();

		if (config.getBoolean("enable-particle-effects", true) && e.getPlayer().hasPermission("traile.allow.particles")
				|| e.getPlayer().hasPermission("traile.allow")) {

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

					List<Float> particle = particleColors.get(Utilities.randomInteger(0, particleColors.size() - 1));
					e.getPlayer().spawnParticle(Particle.SPELL_MOB, e.getPlayer().getLocation(), 0, particle.get(0),
							particle.get(1), particle.get(2), 1);

				}

			}

		}

		if (config.getBoolean("enable-block-effects", true) && e.getPlayer().hasPermission("traile.allow.blocks")
				|| e.getPlayer().hasPermission("traile.allow")) {

			if (e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).getType().equals(Material.AIR)
					|| !(e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).getType().isSolid()
							|| invalidBlocks.contains(e.getTo().getBlock().getType()))
					|| invalidBlocks.contains(e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).getType()))
				return;

			if (lastBlock.containsKey(uuid)) {
				for (Map.Entry<Location, Material> entry : lastBlock.get(uuid).entrySet()) {
					Block block = entry.getKey().getBlock();
					if (!(block.equals(entry.getValue()))) {
						block.setType(entry.getValue());
					}
				}
			}

			lastBlock.put(uuid, new HashMap<Location, Material>() {
				private static final long serialVersionUID = -2102407697342062449L;
				{
					put(e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).getLocation(),
							e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).getType());
				}
			});

			if (config.getBoolean("do-rainbow-effect", false)) {

				int cur = 0;

				if (lastInt.containsKey(uuid)) {
					cur = (lastInt.get(uuid) >= colors.size()) ? 0 : lastInt.get(uuid);
				}

				Block block = e.getTo().getBlock().getRelative(BlockFace.DOWN, 1);
				block.setType(Material.WOOL);
				MaterialData matData = block.getState().getData();
				BlockState state = block.getState();
				Wool wool = (Wool) matData;
				wool.setColor(colors.get(cur));
				state.setData(matData);
				state.update();

				lastInt.put(uuid, (cur + 1));

			} else {

				if (Material.valueOf(config.getString("replacement-material")) != null) {
					e.getTo().getBlock().getRelative(BlockFace.DOWN, 1)
							.setType(Material.valueOf(config.getString("replacement-material")));
				} else {
					e.getTo().getBlock().getRelative(BlockFace.DOWN, 1).setType(Material.GOLD_BLOCK);
				}

			}

		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleMove(VehicleMoveEvent e) {

		if (e.getTo().getBlock().getLocation().equals(e.getFrom().getBlock().getLocation())
				&& config.getBoolean("vehicle-trail-effects", true))
			return;

		Vehicle vehicle = e.getVehicle();

		if (vehicle.getPassengers().size() > 0) {

			for (Entity entity : vehicle.getPassengers()) {

				if (entity instanceof Player) {

					Player player = (Player) entity;

					if (player.hasPermission("traile.allow.particles") || player.hasPermission("traile.allow")) {

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
								player.spawnParticle(Particle.SPELL_MOB, player.getLocation(), 0, particle.get(0),
										particle.get(1), particle.get(2), 1);

							}

						}

					}

				}

			}

		}

	}

	public void removeAllEntries() {
		if (lastBlock.size() > 0) {
			for (UUID uuid : lastBlock.keySet()) {
				for (Map.Entry<Location, Material> entry : lastBlock.get(uuid).entrySet()) {
					Block block = entry.getKey().getBlock();
					if (!(block.equals(entry.getValue()))) {
						block.setType(entry.getValue());
					}
				}
			}
		}
	}

	public void removePlayerEntry(UUID uuid) {
		if (lastBlock.size() > 0) {
			for (Map.Entry<Location, Material> entry : lastBlock.get(uuid).entrySet()) {
				Block block = entry.getKey().getBlock();
				if (!(block.equals(entry.getValue()))) {
					block.setType(entry.getValue());
				}
			}
		}
	}

}
