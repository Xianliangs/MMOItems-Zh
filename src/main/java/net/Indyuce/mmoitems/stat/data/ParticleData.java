package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.particle.api.ParticleType;
import net.mmogroup.mmolib.MMOLib;

public class ParticleData extends StatData {
	private final ParticleType type;
	private final Particle particle;
	private final Map<String, Double> modifiers = new HashMap<>();

	private Color color;

	public ParticleData(JsonObject object) {
		particle = Particle.valueOf(object.get("Particle").getAsString());
		type = ParticleType.valueOf(object.get("Type").getAsString());

		if (object.has("Color")) {
			JsonObject color = object.getAsJsonObject("Color");
			setColor(color.get("Red").getAsInt(), color.get("Green").getAsInt(), color.get("Blue").getAsInt());
		}

		object.getAsJsonObject("Modifiers").entrySet().forEach(entry -> setModifier(entry.getKey(), entry.getValue().getAsDouble()));
	}

	public ParticleData(MMOItem item, ConfigurationSection config) {
		Validate.isTrue(config.contains("type") && config.contains("particle"), "Particle is missing type or selected particle.");

		String format = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
		type = ParticleType.valueOf(format);

		format = config.getString("particle").toUpperCase().replace("-", "_").replace(" ", "_");
		particle = Particle.valueOf(format);

		for (String key : config.getKeys(false)) {
			if (key.equalsIgnoreCase("color"))
				setColor(config.getInt("color.red"), config.getInt("color.green"), config.getInt("color.blue"));
			else if (!key.equalsIgnoreCase("particle") && !key.equalsIgnoreCase("type"))
				setModifier(key, config.getDouble(key));
		}
	}

	public ParticleData(ParticleType type, Particle particle) {
		this.type = type;
		this.particle = particle;
	}

	public ParticleType getType() {
		return type;
	}

	public Particle getParticle() {
		return particle;
	}

	public Color getColor() {
		return color;
	}

	public double getModifier(String path) {
		return modifiers.containsKey(path) ? modifiers.get(path) : type.getModifier(path);
	}

	public Set<String> getModifiers() {
		return modifiers.keySet();
	}

	public void setColor(int red, int green, int blue) {
		color = Color.fromRGB(red, green, blue);
	}

	public void setModifier(String path, double value) {
		modifiers.put(path, value);
	}

	public boolean isColored() {
		return color != null;
	}

	/*
	 * depending on if the particle is colorable or not, display with colors or
	 * not
	 */
	public void display(Location location, float speed) {
		display(location, 1, 0, 0, 0, speed);
	}

	public void display(Location location, int amount, float offsetX, float offsetY, float offsetZ, float speed) {
		if (isColored())
			MMOLib.plugin.getVersion().getWrapper().spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ, speed, 1, color);
		else
			location.getWorld().spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ, speed);
	}

	public void display(Location location, Vector direction, float speed) {
		if (isColored())
			MMOLib.plugin.getVersion().getWrapper().spawnParticle(particle, location, 0, direction.getX(), direction.getY(), direction.getZ(), speed, 1, color);
		else
			location.getWorld().spawnParticle(particle, location, 0, direction.getX(), direction.getY(), direction.getZ(), speed);
	}

	public ParticleRunnable start(PlayerData player) {
		ParticleRunnable runnable = type.newRunnable(this, player);
		runnable.runTaskTimer(MMOItems.plugin, 0, type.getTime());
		return runnable;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("Particle", getParticle().name());
		object.addProperty("Type", getType().name());

		if (isColored()) {
			JsonObject color = new JsonObject();
			color.addProperty("Red", getColor().getRed());
			color.addProperty("Green", getColor().getGreen());
			color.addProperty("Blue", getColor().getBlue());
			object.add("Color", color);
		}

		JsonObject modifiers = new JsonObject();
		getModifiers().forEach(name -> modifiers.addProperty(name, getModifier(name)));
		object.add("Modifiers", modifiers);
		return object;
	}

	public static boolean isColorable(Particle particle) {
		return particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT || particle == Particle.REDSTONE || particle == Particle.NOTE;
	}
}
