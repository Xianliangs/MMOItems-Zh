package net.Indyuce.mmoitems.api.event.crafting;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class PlayerUseCraftingStationEvent extends PlayerDataEvent {
	private final Recipe recipe;
	private final RecipeInfo recipeInfo;
	private final CraftingStation station;
	private final StationAction action;

	private static final HandlerList handlers = new HandlerList();

	/**
	 * Called when a player directly interacts with a recipe in the crafting
	 * station GUI. The recipe is either instant and the item is given
	 * instaneously, or the item is sent in the crafting queue
	 * 
	 * @param playerData
	 *            The player interacting with the crafting station
	 * @param station
	 *            The crafting station being used
	 * @param recipeInfo
	 *            The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, RecipeInfo recipeInfo) {
		super(playerData);

		this.recipeInfo = recipeInfo;
		this.recipe = recipeInfo.getRecipe();
		this.station = station;
		this.action = StationAction.INTERACT_WITH_RECIPE;
	}

	/**
	 * Called when a player claims an item from the crafting queue.
	 * 
	 * @param playerData
	 *            The player interacting with the crafting station
	 * @param station
	 *            The crafting station being used
	 * @param recipeInfo
	 *            The recipe being used to craft the item
	 */
	public PlayerUseCraftingStationEvent(PlayerData playerData, CraftingStation station, Recipe recipe) {
		super(playerData);

		this.recipeInfo = null;
		this.recipe = recipe;
		this.station = station;
		this.action = StationAction.CRAFTING_QUEUE;
	}

	public CraftingStation getStation() {
		return station;
	}

	/**
	 * @return The corresponding recipe info IF AND ONLY IF the player is
	 *         interacting with a recipe. This method cannot be used when a
	 *         player claims an item from the crafting queue.
	 */
	public RecipeInfo getRecipeInfo() {
		Validate.notNull(recipeInfo, "No recipe info is provided when a player claims an item in the crafting queue");
		return recipeInfo;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public StationAction getInteraction() {
		return action;
	}

	@Deprecated
	public boolean isInstant() {
		return recipe instanceof CraftingRecipe && ((CraftingRecipe) recipe).isInstant();
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum StationAction {

		/**
		 * Called when a player places an item in the crafting queue when the
		 * recipe is not instantaneous.
		 */
		INTERACT_WITH_RECIPE,

		/**
		 * Called when a player claims the item either in the crafting queue or
		 * because the recipe is instantaneous
		 */
		CRAFTING_QUEUE;
	}
}