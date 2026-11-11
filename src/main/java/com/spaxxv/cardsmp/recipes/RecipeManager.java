package com.spaxxv.cardsmp.recipes;


import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.events.PreCraftEvent;
import com.spaxxv.cardsmp.events.PreCraftEventListener;
import com.spaxxv.cardsmp.items.Items;
import com.spaxxv.cardsmp.util.MapBuilder;
import com.spaxxv.cardsmp.util.SetBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.UseRemainder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipeManager {
    private static final Map<InventoryType, Pattern.Type> patternTypeLUT = new MapBuilder<InventoryType, Pattern.Type>()
            .addElement(InventoryType.WORKBENCH, Pattern.Type.CRAFTING)
            .addElement(InventoryType.CRAFTING, Pattern.Type.CRAFTING)
            .addElement(InventoryType.CRAFTER, Pattern.Type.CRAFTING)
            .toMap();
    private static final HashSet<Recipe> recipes = new HashSet<>();
    private static final PreCraftEventListener alchemistCheck = event -> {
        if (!AbilityManager.isActive(event.getPlayer(), Cards.ALCHEMIST.passive())) event.setCancelled(true);
    };

    public static void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }
    public static Recipe recipeFor(Pattern pattern) {
        for (Recipe recipe : recipes) {
            if (recipe.matches(pattern)) return recipe;
        }
        return null;
    }
    public static void handleCraftEvent(CraftItemEvent event) {
        if (patternTypeLUT.containsKey(event.getInventory().getType())) {
            Pattern pattern = Pattern.builder(patternTypeLUT.get(event.getInventory().getType())).addFromMatrix(event.getInventory().getMatrix()).build();
            Recipe recipe = recipeFor(pattern);
            if (recipe != null) {
                HumanEntity viewer = event.getInventory().getViewers().getFirst();
                if (viewer instanceof Player player) {
                    PreCraftEvent preCraftEvent = new PreCraftEvent(player, recipe);
                    recipe.firePreCraftEvent(preCraftEvent);
                    if (!preCraftEvent.isCancelled()) {
                        ItemStack[] matrix = event.getInventory().getMatrix();
                        for (int i = 0; i < matrix.length; i++) {
                            ItemStack item = matrix[i];
                            if (item != null && item.getType() != Material.AIR) {
                                if (item.hasData(DataComponentTypes.USE_REMAINDER)) {
                                    item = UseRemainder.useRemainder(item).transformInto();
                                } else {
                                    item = item.subtract();
                                }
                            }
                            matrix[i] = item;
                        }
                        event.getInventory().setMatrix(matrix);
                    }
                }
            }
        }
    }
    public static boolean handleRecipeEvent(PrepareItemCraftEvent event) {
        if (patternTypeLUT.containsKey(event.getInventory().getType())) {
            Pattern pattern = Pattern.builder(patternTypeLUT.get(event.getInventory().getType())).addFromMatrix(event.getInventory().getMatrix()).build();
            Recipe recipe = recipeFor(pattern);
            if (recipe != null) {
                HumanEntity viewer = event.getInventory().getViewers().getFirst();
                if (viewer instanceof Player player) {
                    PreCraftEvent preCraftEvent = new PreCraftEvent(player, recipe);
                    recipe.firePreCraftEvent(preCraftEvent);
                    if (!preCraftEvent.isCancelled()) {
                        event.getInventory().setResult(preCraftEvent.getResult().clone());
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static ItemStack[] potionsFor(PotionType type) {
        ItemStack drink = new ItemStack(Material.POTION);
        ItemStack splash = new ItemStack(Material.SPLASH_POTION);
        ItemStack linger = new ItemStack(Material.LINGERING_POTION);
        PotionContents potionContents = PotionContents.potionContents().potion(type).build();
        drink.setData(DataComponentTypes.POTION_CONTENTS, potionContents);
        splash.setData(DataComponentTypes.POTION_CONTENTS, potionContents);
        linger.setData(DataComponentTypes.POTION_CONTENTS, potionContents);
        return new ItemStack[] {drink, splash, linger};
    }
    private static void addAlchemistRecipe(ItemStack base, Material added, ItemStack result) {
        Pattern pattern = Pattern.builder(Pattern.Type.CRAFTING_SHAPELESS).addItem(new StrictPatternItem(base)).addItem(added).build();
        RecipeManager.addRecipe(new CraftingRecipe(pattern, result).setPreCraftEvent(alchemistCheck));
    }
    private static void addAlchemistRecipes(ItemStack[] bases, Material added, ItemStack[] results) {
        for (int i = 0; i < 3; i++) {
            addAlchemistRecipe(bases[i], added, results[i]);
        }
    }
    private static void addAlchemistRecipes() {
        Set<PotionType> allTypes = new SetBuilder<PotionType>().addElements(
                PotionType.WATER, PotionType.AWKWARD, PotionType.NIGHT_VISION, PotionType.LONG_NIGHT_VISION,
                PotionType.INVISIBILITY, PotionType.LONG_INVISIBILITY, PotionType.LEAPING, PotionType.LONG_LEAPING,
                PotionType.STRONG_LEAPING, PotionType.FIRE_RESISTANCE, PotionType.LONG_FIRE_RESISTANCE, PotionType.SWIFTNESS,
                PotionType.LONG_SWIFTNESS, PotionType.STRONG_SWIFTNESS, PotionType.SLOWNESS, PotionType.LONG_SLOWNESS,
                PotionType.STRONG_SLOWNESS, PotionType.WATER_BREATHING, PotionType.LONG_WATER_BREATHING, PotionType.HEALING,
                PotionType.STRONG_HEALING, PotionType.HARMING, PotionType.STRONG_HARMING, PotionType.POISON,
                PotionType.LONG_POISON, PotionType.STRONG_POISON, PotionType.REGENERATION, PotionType.LONG_REGENERATION,
                PotionType.STRONG_REGENERATION, PotionType.STRENGTH, PotionType.LONG_STRENGTH, PotionType.STRONG_STRENGTH,
                PotionType.WEAKNESS, PotionType.LONG_WEAKNESS, PotionType.TURTLE_MASTER, PotionType.LONG_TURTLE_MASTER,
                PotionType.STRONG_TURTLE_MASTER, PotionType.SLOW_FALLING, PotionType.LONG_SLOW_FALLING, PotionType.INFESTED,
                PotionType.OOZING, PotionType.WEAVING, PotionType.WIND_CHARGED
        ).toSet();
        Map<PotionType, Material> craftablePotionTypes = new MapBuilder<PotionType, Material>()
                .addElement(PotionType.AWKWARD, Material.NETHER_WART)
                .addElement(PotionType.NIGHT_VISION, Material.GOLDEN_CARROT)
                .addElement(PotionType.INVISIBILITY, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.LONG_INVISIBILITY, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.LEAPING, Material.RABBIT_FOOT)
                .addElement(PotionType.FIRE_RESISTANCE, Material.MAGMA_CREAM)
                .addElement(PotionType.SWIFTNESS, Material.SUGAR)
                .addElement(PotionType.SLOWNESS, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.WATER_BREATHING, Material.PUFFERFISH)
                .addElement(PotionType.HEALING, Material.GLISTERING_MELON_SLICE)
                .addElement(PotionType.HARMING, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.STRONG_HARMING, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.POISON, Material.SPIDER_EYE)
                .addElement(PotionType.REGENERATION, Material.GHAST_TEAR)
                .addElement(PotionType.STRENGTH, Material.BLAZE_POWDER)
                .addElement(PotionType.WEAKNESS, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.LONG_WEAKNESS, Material.FERMENTED_SPIDER_EYE)
                .addElement(PotionType.TURTLE_MASTER, Material.TURTLE_SCUTE)
                .addElement(PotionType.SLOW_FALLING, Material.PHANTOM_MEMBRANE)
                .addElement(PotionType.INFESTED, Material.STONE)
                .addElement(PotionType.OOZING, Material.SLIME_BLOCK)
                .addElement(PotionType.WEAVING, Material.COBWEB)
                .addElement(PotionType.WIND_CHARGED, Material.BREEZE_ROD)
                .toMap();
        Map<PotionType, PotionType> longPotionTypes = new MapBuilder<PotionType, PotionType>()
                .addElement(PotionType.LONG_NIGHT_VISION, PotionType.NIGHT_VISION)
                .addElement(PotionType.LONG_INVISIBILITY, PotionType.INVISIBILITY)
                .addElement(PotionType.LONG_LEAPING, PotionType.LEAPING)
                .addElement(PotionType.LONG_FIRE_RESISTANCE, PotionType.FIRE_RESISTANCE)
                .addElement(PotionType.LONG_SWIFTNESS, PotionType.SWIFTNESS)
                .addElement(PotionType.LONG_SLOWNESS, PotionType.SLOWNESS)
                .addElement(PotionType.LONG_WATER_BREATHING, PotionType.WATER_BREATHING)
                .addElement(PotionType.LONG_POISON, PotionType.POISON)
                .addElement(PotionType.LONG_REGENERATION, PotionType.REGENERATION)
                .addElement(PotionType.LONG_STRENGTH, PotionType.STRENGTH)
                .addElement(PotionType.LONG_WEAKNESS, PotionType.WEAKNESS)
                .addElement(PotionType.LONG_TURTLE_MASTER, PotionType.TURTLE_MASTER)
                .addElement(PotionType.LONG_SLOW_FALLING, PotionType.SLOW_FALLING)
                .toMap();
        Map<PotionType, PotionType> strongPotionTypes = new MapBuilder<PotionType, PotionType>()
                .addElement(PotionType.STRONG_LEAPING, PotionType.LEAPING)
                .addElement(PotionType.STRONG_SWIFTNESS, PotionType.SWIFTNESS)
                .addElement(PotionType.STRONG_SLOWNESS, PotionType.SLOWNESS)
                .addElement(PotionType.STRONG_HEALING, PotionType.HEALING)
                .addElement(PotionType.STRONG_HARMING, PotionType.HARMING)
                .addElement(PotionType.STRONG_POISON, PotionType.POISON)
                .addElement(PotionType.STRONG_REGENERATION, PotionType.REGENERATION)
                .addElement(PotionType.STRONG_STRENGTH, PotionType.STRENGTH)
                .addElement(PotionType.STRONG_TURTLE_MASTER, PotionType.TURTLE_MASTER)
                .toMap();
        Map<PotionType, PotionType> alternateBases = new MapBuilder<PotionType, PotionType>()
                .addElement(PotionType.INVISIBILITY, PotionType.NIGHT_VISION)
                .addElement(PotionType.LONG_INVISIBILITY, PotionType.LONG_NIGHT_VISION)
                .addElement(PotionType.SLOWNESS, PotionType.LEAPING)
                .addElement(PotionType.LONG_SLOWNESS, PotionType.LONG_LEAPING)
                .addElement(PotionType.STRONG_SLOWNESS, PotionType.STRONG_LEAPING)
                .addElement(PotionType.HARMING, PotionType.HEALING)
                .addElement(PotionType.STRONG_HARMING, PotionType.STRONG_HEALING)
                .addElement(PotionType.WEAKNESS, PotionType.STRENGTH)
                .addElement(PotionType.LONG_WEAKNESS, PotionType.LONG_STRENGTH)
                .toMap();
        Map<PotionType, PotionType> alternateBases2 = new MapBuilder<PotionType, PotionType>()
                .addElement(PotionType.SLOWNESS, PotionType.SWIFTNESS)
                .addElement(PotionType.LONG_SLOWNESS, PotionType.LONG_SWIFTNESS)
                .addElement(PotionType.STRONG_SLOWNESS, PotionType.STRONG_SWIFTNESS)
                .addElement(PotionType.HARMING, PotionType.POISON)
                .addElement(PotionType.STRONG_HARMING, PotionType.STRONG_POISON)
                .addElement(PotionType.WEAKNESS, PotionType.STRONG_STRENGTH)
                .toMap();
        Map<PotionType, PotionType> alternateBases3 = new MapBuilder<PotionType, PotionType>()
                .addElement(PotionType.HARMING, PotionType.LONG_POISON)
                .toMap();
        Set<PotionType> waterBases = new SetBuilder<PotionType>()
                .addElements(PotionType.AWKWARD)
                .addElements(PotionType.WEAKNESS)
                .toSet();
        ItemStack[] waterBottles = potionsFor(PotionType.WATER);
        ItemStack[] awkwardPotions = potionsFor(PotionType.AWKWARD);
        for (Map.Entry<PotionType, Material> entry : craftablePotionTypes.entrySet()) {
            boolean flag = false;
            if (waterBases.contains(entry.getKey())) {
                addAlchemistRecipes(waterBottles, entry.getValue(), potionsFor(entry.getKey()));
                flag = true;
            } else {
                if (alternateBases.containsKey(entry.getKey())) {
                    addAlchemistRecipes(potionsFor(alternateBases.get(entry.getKey())), entry.getValue(), potionsFor(entry.getKey()));
                    // This ONLY exists for slowness and harming potions which have TWO recipes and its HORRIBLE. (And STRONG_STRENGTH -> WEAKNESS I guess)
                    if (alternateBases2.containsKey(entry.getKey())) {
                        addAlchemistRecipes(potionsFor(alternateBases2.get(entry.getKey())), entry.getValue(), potionsFor(entry.getKey()));
                    }
                    // THIS ONLY EXISTS BECAUSE OF POISON -> HARMING.
                    if (alternateBases3.containsKey(entry.getKey())) {
                        addAlchemistRecipes(potionsFor(alternateBases3.get(entry.getKey())), entry.getValue(), potionsFor(entry.getKey()));
                    }
                } else {
                    addAlchemistRecipes(awkwardPotions, entry.getValue(), potionsFor(entry.getKey()));
                }
            }
            if (flag) {
                // This ONLY exists for weakness potions which have TWO recipes and its HORRIBLE.
                if (alternateBases.containsKey(entry.getKey())) {
                    addAlchemistRecipes(potionsFor(alternateBases.get(entry.getKey())), entry.getValue(), potionsFor(entry.getKey()));
                }
            }
        }
        for (Map.Entry<PotionType, PotionType> entry : longPotionTypes.entrySet()) {
            addAlchemistRecipes(potionsFor(entry.getValue()), Material.REDSTONE, potionsFor(entry.getKey()));
        }
        for (Map.Entry<PotionType, PotionType> entry : strongPotionTypes.entrySet()) {
            addAlchemistRecipes(potionsFor(entry.getValue()), Material.GLOWSTONE_DUST, potionsFor(entry.getKey()));
        }
        for (PotionType type : allTypes) {
            ItemStack[] potions = potionsFor(type);
            addAlchemistRecipe(potions[0], Material.GUNPOWDER, potions[1]);
            addAlchemistRecipe(potions[1], Material.DRAGON_BREATH, potions[2]);
        }
    }
    public static void registerRecipes() {
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING).addItems(new Material[] {
                Material.IRON_BLOCK, Material.PAPER, Material.IRON_BLOCK,
                Material.BLAZE_ROD, Material.TOTEM_OF_UNDYING, Material.BLAZE_ROD,
                Material.IRON_BLOCK, Material.PAPER, Material.IRON_BLOCK
        }).build(), Items.SPADE_SHARD.createItemStack()));
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING).addItems(new Material[] {
                Material.GOLD_BLOCK, Material.PAPER, Material.GOLD_BLOCK,
                Material.BLAZE_ROD, Material.TOTEM_OF_UNDYING, Material.BLAZE_ROD,
                Material.GOLD_BLOCK, Material.PAPER, Material.GOLD_BLOCK
        }).build(), Items.HEART_SHARD.createItemStack()));
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING).addItems(new Material[] {
                Material.DIAMOND_BLOCK, Material.PAPER, Material.DIAMOND_BLOCK,
                Material.BLAZE_ROD, Material.TOTEM_OF_UNDYING, Material.BLAZE_ROD,
                Material.DIAMOND_BLOCK, Material.PAPER, Material.DIAMOND_BLOCK
        }).build(), Items.DIAMOND_SHARD.createItemStack()));
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING).addItems(new Material[] {
                Material.LAPIS_BLOCK, Material.PAPER, Material.LAPIS_BLOCK,
                Material.BLAZE_ROD, Material.TOTEM_OF_UNDYING, Material.BLAZE_ROD,
                Material.LAPIS_BLOCK, Material.PAPER, Material.LAPIS_BLOCK
        }).build(), Items.CLUB_SHARD.createItemStack()));
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING)
                .addItem(Items.SPADE_SHARD).addItem(Material.PAPER).addItem(Items.HEART_SHARD)
                .addItems(new Material[] {Material.NETHERITE_INGOT, Material.OMINOUS_TRIAL_KEY, Material.NETHERITE_INGOT})
                .addItem(Items.DIAMOND_SHARD).addItem(Material.PAPER).addItem(Items.CLUB_SHARD)
        .build(), Items.CARD_DECK.createItemStack()));
        RecipeManager.addRecipe(new CraftingRecipe(Pattern.builder(Pattern.Type.CRAFTING_SHAPELESS).addItem(Material.PAPER).build(), new ItemStack(Material.BOOK)));
        addAlchemistRecipes();
        CardSMP.instance.getLogger().info("Registered " + recipes.size() + " recipes.");
    }
}
