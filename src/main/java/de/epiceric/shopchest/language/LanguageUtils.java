package de.epiceric.shopchest.language;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.LanguageConfiguration;
import de.epiceric.shopchest.config.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Map;

public class LanguageUtils {

    private static ShopChest plugin = ShopChest.getInstance();
    private static LanguageConfiguration langConfig;

    private static ArrayList<ItemName> itemNames = new ArrayList<>();
    private static ArrayList<EnchantmentName> enchantmentNames = new ArrayList<>();
    private static ArrayList<EnchantmentName.EnchantmentLevelName> enchantmentLevelNames = new ArrayList<>();
    private static ArrayList<PotionEffectName> potionEffectNames = new ArrayList<>();
    private static ArrayList<EntityName> entityNames = new ArrayList<>();
    private static ArrayList<PotionName> potionNames = new ArrayList<>();
    private static ArrayList<MusicDiscName> musicDiscNames = new ArrayList<>();
    private static ArrayList<BookGenerationName> generationNames = new ArrayList<>();
    private static ArrayList<LocalizedMessage> messages = new ArrayList<>();


    public static void load() {
        langConfig = Config.langConfig;

        itemNames.clear();
        enchantmentNames.clear();
        enchantmentLevelNames.clear();
        potionEffectNames.clear();
        entityNames.clear();
        potionNames.clear();
        musicDiscNames.clear();
        generationNames.clear();
        messages.clear();

        // Add Block/Item Names
        itemNames.add(new ItemName(Material.AIR, langConfig.getString("block.minecraft.air", "Air")));
        itemNames.add(new ItemName(Material.BARRIER, langConfig.getString("block.minecraft.barrier", "Barrier")));
        itemNames.add(new ItemName(Material.STONE, langConfig.getString("block.minecraft.stone", "Stone")));
        itemNames.add(new ItemName(Material.GRANITE, langConfig.getString("block.minecraft.granite", "Granite")));
        itemNames.add(new ItemName(Material.POLISHED_GRANITE, langConfig.getString("block.minecraft.polished_granite", "Polished Granite")));
        itemNames.add(new ItemName(Material.DIORITE, langConfig.getString("block.minecraft.diorite", "Diorite")));
        itemNames.add(new ItemName(Material.POLISHED_DIORITE, langConfig.getString("block.minecraft.polished_diorite", "Polished Diorite")));
        itemNames.add(new ItemName(Material.ANDESITE, langConfig.getString("block.minecraft.andesite", "Andesite")));
        itemNames.add(new ItemName(Material.POLISHED_ANDESITE, langConfig.getString("block.minecraft.polished_andesite", "Polished Andesite")));
        itemNames.add(new ItemName(Material.HAY_BLOCK, langConfig.getString("block.minecraft.hay_block", "Hay Bale")));
        itemNames.add(new ItemName(Material.GRASS_BLOCK, langConfig.getString("block.minecraft.grass_block", "Grass Block")));
        itemNames.add(new ItemName(Material.DIRT, langConfig.getString("block.minecraft.dirt", "Dirt")));
        itemNames.add(new ItemName(Material.COARSE_DIRT, langConfig.getString("block.minecraft.coarse_dirt", "Coarse Dirt")));
        itemNames.add(new ItemName(Material.PODZOL, langConfig.getString("block.minecraft.podzol", "Podzol")));
        itemNames.add(new ItemName(Material.COBBLESTONE, langConfig.getString("block.minecraft.cobblestone", "Cobblestone")));
        itemNames.add(new ItemName(Material.OAK_PLANKS, langConfig.getString("block.minecraft.oak_planks", "Oak Planks")));
        itemNames.add(new ItemName(Material.SPRUCE_PLANKS, langConfig.getString("block.minecraft.spruce_planks", "Spruce Planks")));
        itemNames.add(new ItemName(Material.BIRCH_PLANKS, langConfig.getString("block.minecraft.birch_planks", "Birch Planks")));
        itemNames.add(new ItemName(Material.JUNGLE_PLANKS, langConfig.getString("block.minecraft.jungle_planks", "Jungle Planks")));
        itemNames.add(new ItemName(Material.ACACIA_PLANKS, langConfig.getString("block.minecraft.acacia_planks", "Acacia Planks")));
        itemNames.add(new ItemName(Material.DARK_OAK_PLANKS, langConfig.getString("block.minecraft.dark_oak_planks", "Dark Oak Planks")));
        itemNames.add(new ItemName(Material.OAK_SAPLING, langConfig.getString("block.minecraft.oak_sapling", "Oak Sapling")));
        itemNames.add(new ItemName(Material.SPRUCE_SAPLING, langConfig.getString("block.minecraft.spruce_sapling", "Spruce Sapling")));
        itemNames.add(new ItemName(Material.BIRCH_SAPLING, langConfig.getString("block.minecraft.birch_sapling", "Birch Sapling")));
        itemNames.add(new ItemName(Material.JUNGLE_SAPLING, langConfig.getString("block.minecraft.jungle_sapling", "Jungle Sapling")));
        itemNames.add(new ItemName(Material.ACACIA_SAPLING, langConfig.getString("block.minecraft.acacia_sapling", "Acacia Sapling")));
        itemNames.add(new ItemName(Material.DARK_OAK_SAPLING, langConfig.getString("block.minecraft.dark_oak_sapling", "Dark Oak Sapling")));
        itemNames.add(new ItemName(Material.OAK_DOOR, langConfig.getString("block.minecraft.oak_door", "Oak Door")));
        itemNames.add(new ItemName(Material.SPRUCE_DOOR, langConfig.getString("block.minecraft.spruce_door", "Spruce Door")));
        itemNames.add(new ItemName(Material.BIRCH_DOOR, langConfig.getString("block.minecraft.birch_door", "Birch Door")));
        itemNames.add(new ItemName(Material.JUNGLE_DOOR, langConfig.getString("block.minecraft.jungle_door", "Jungle Door")));
        itemNames.add(new ItemName(Material.ACACIA_DOOR, langConfig.getString("block.minecraft.acacia_door", "Acacia Door")));
        itemNames.add(new ItemName(Material.DARK_OAK_DOOR, langConfig.getString("block.minecraft.dark_oak_door", "Dark Oak Door")));
        itemNames.add(new ItemName(Material.BEDROCK, langConfig.getString("block.minecraft.bedrock", "Bedrock")));
        itemNames.add(new ItemName(Material.WATER, langConfig.getString("block.minecraft.water", "Water")));
        itemNames.add(new ItemName(Material.LAVA, langConfig.getString("block.minecraft.lava", "Lava")));
        itemNames.add(new ItemName(Material.SAND, langConfig.getString("block.minecraft.sand", "Sand")));
        itemNames.add(new ItemName(Material.RED_SAND, langConfig.getString("block.minecraft.red_sand", "Red Sand")));
        itemNames.add(new ItemName(Material.SANDSTONE, langConfig.getString("block.minecraft.sandstone", "Sandstone")));
        itemNames.add(new ItemName(Material.CHISELED_SANDSTONE, langConfig.getString("block.minecraft.chiseled_sandstone", "Chiseled Sandstone")));
        itemNames.add(new ItemName(Material.CUT_SANDSTONE, langConfig.getString("block.minecraft.cut_sandstone", "Cut Sandstone")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE, langConfig.getString("block.minecraft.red_sandstone", "Red Sandstone")));
        itemNames.add(new ItemName(Material.CHISELED_RED_SANDSTONE, langConfig.getString("block.minecraft.chiseled_red_sandstone", "Chiseled Red Sandstone")));
        itemNames.add(new ItemName(Material.CUT_RED_SANDSTONE, langConfig.getString("block.minecraft.cut_red_sandstone", "Cut Red Sandstone")));
        itemNames.add(new ItemName(Material.GRAVEL, langConfig.getString("block.minecraft.gravel", "Gravel")));
        itemNames.add(new ItemName(Material.GOLD_ORE, langConfig.getString("block.minecraft.gold_ore", "Gold Ore")));
        itemNames.add(new ItemName(Material.IRON_ORE, langConfig.getString("block.minecraft.iron_ore", "Iron Ore")));
        itemNames.add(new ItemName(Material.COAL_ORE, langConfig.getString("block.minecraft.coal_ore", "Coal Ore")));
        itemNames.add(new ItemName(Material.OAK_WOOD, langConfig.getString("block.minecraft.oak_wood", "Oak Wood")));
        itemNames.add(new ItemName(Material.SPRUCE_WOOD, langConfig.getString("block.minecraft.spruce_wood", "Spruce Wood")));
        itemNames.add(new ItemName(Material.BIRCH_WOOD, langConfig.getString("block.minecraft.birch_wood", "Birch Wood")));
        itemNames.add(new ItemName(Material.JUNGLE_WOOD, langConfig.getString("block.minecraft.jungle_wood", "Jungle Wood")));
        itemNames.add(new ItemName(Material.ACACIA_WOOD, langConfig.getString("block.minecraft.acacia_wood", "Acacia Wood")));
        itemNames.add(new ItemName(Material.DARK_OAK_WOOD, langConfig.getString("block.minecraft.dark_oak_wood", "Dark Oak Wood")));
        itemNames.add(new ItemName(Material.OAK_LOG, langConfig.getString("block.minecraft.oak_log", "Oak Log")));
        itemNames.add(new ItemName(Material.SPRUCE_LOG, langConfig.getString("block.minecraft.spruce_log", "Spruce Log")));
        itemNames.add(new ItemName(Material.BIRCH_LOG, langConfig.getString("block.minecraft.birch_log", "Birch Log")));
        itemNames.add(new ItemName(Material.JUNGLE_LOG, langConfig.getString("block.minecraft.jungle_log", "Jungle Log")));
        itemNames.add(new ItemName(Material.ACACIA_LOG, langConfig.getString("block.minecraft.acacia_log", "Acacia Log")));
        itemNames.add(new ItemName(Material.DARK_OAK_LOG, langConfig.getString("block.minecraft.dark_oak_log", "Dark Oak Log")));
        itemNames.add(new ItemName(Material.STRIPPED_OAK_LOG, langConfig.getString("block.minecraft.stripped_oak_log", "Stripped Oak Log")));
        itemNames.add(new ItemName(Material.STRIPPED_SPRUCE_LOG, langConfig.getString("block.minecraft.stripped_spruce_log", "Stripped Spruce Log")));
        itemNames.add(new ItemName(Material.STRIPPED_BIRCH_LOG, langConfig.getString("block.minecraft.stripped_birch_log", "Stripped Birch Log")));
        itemNames.add(new ItemName(Material.STRIPPED_JUNGLE_LOG, langConfig.getString("block.minecraft.stripped_jungle_log", "Stripped Jungle Log")));
        itemNames.add(new ItemName(Material.STRIPPED_ACACIA_LOG, langConfig.getString("block.minecraft.stripped_acacia_log", "Stripped Acacia Log")));
        itemNames.add(new ItemName(Material.STRIPPED_DARK_OAK_LOG, langConfig.getString("block.minecraft.stripped_dark_oak_log", "Stripped Dark Oak Log")));
        itemNames.add(new ItemName(Material.STRIPPED_OAK_WOOD, langConfig.getString("block.minecraft.stripped_oak_wood", "Stripped Oak Wood")));
        itemNames.add(new ItemName(Material.STRIPPED_SPRUCE_WOOD, langConfig.getString("block.minecraft.stripped_spruce_wood", "Stripped Spruce Wood")));
        itemNames.add(new ItemName(Material.STRIPPED_BIRCH_WOOD, langConfig.getString("block.minecraft.stripped_birch_wood", "Stripped Birch Wood")));
        itemNames.add(new ItemName(Material.STRIPPED_JUNGLE_WOOD, langConfig.getString("block.minecraft.stripped_jungle_wood", "Stripped Jungle Wood")));
        itemNames.add(new ItemName(Material.STRIPPED_ACACIA_WOOD, langConfig.getString("block.minecraft.stripped_acacia_wood", "Stripped Acacia Wood")));
        itemNames.add(new ItemName(Material.STRIPPED_DARK_OAK_WOOD, langConfig.getString("block.minecraft.stripped_dark_oak_wood", "Stripped Dark Oak Wood")));
        itemNames.add(new ItemName(Material.OAK_LEAVES, langConfig.getString("block.minecraft.oak_leaves", "Oak Leaves")));
        itemNames.add(new ItemName(Material.SPRUCE_LEAVES, langConfig.getString("block.minecraft.spruce_leaves", "Spruce Leaves")));
        itemNames.add(new ItemName(Material.BIRCH_LEAVES, langConfig.getString("block.minecraft.birch_leaves", "Birch Leaves")));
        itemNames.add(new ItemName(Material.JUNGLE_LEAVES, langConfig.getString("block.minecraft.jungle_leaves", "Jungle Leaves")));
        itemNames.add(new ItemName(Material.ACACIA_LEAVES, langConfig.getString("block.minecraft.acacia_leaves", "Acacia Leaves")));
        itemNames.add(new ItemName(Material.DARK_OAK_LEAVES, langConfig.getString("block.minecraft.dark_oak_leaves", "Dark Oak Leaves")));
        itemNames.add(new ItemName(Material.DEAD_BUSH, langConfig.getString("block.minecraft.dead_bush", "Dead Bush")));
        itemNames.add(new ItemName(Material.GRASS, langConfig.getString("block.minecraft.grass", "Grass")));
        itemNames.add(new ItemName(Material.FERN, langConfig.getString("block.minecraft.fern", "Fern")));
        itemNames.add(new ItemName(Material.SPONGE, langConfig.getString("block.minecraft.sponge", "Sponge")));
        itemNames.add(new ItemName(Material.WET_SPONGE, langConfig.getString("block.minecraft.wet_sponge", "Wet Sponge")));
        itemNames.add(new ItemName(Material.GLASS, langConfig.getString("block.minecraft.glass", "Glass")));
        itemNames.add(new ItemName(Material.KELP_PLANT, langConfig.getString("block.minecraft.kelp_plant", "Kelp Plant")));
        itemNames.add(new ItemName(Material.KELP, langConfig.getString("block.minecraft.kelp", "Kelp")));
        itemNames.add(new ItemName(Material.DRIED_KELP_BLOCK, langConfig.getString("block.minecraft.dried_kelp_block", "Dried Kelp Block")));
        itemNames.add(new ItemName(Material.WHITE_STAINED_GLASS, langConfig.getString("block.minecraft.white_stained_glass", "White Stained Glass")));
        itemNames.add(new ItemName(Material.ORANGE_STAINED_GLASS, langConfig.getString("block.minecraft.orange_stained_glass", "Orange Stained Glass")));
        itemNames.add(new ItemName(Material.MAGENTA_STAINED_GLASS, langConfig.getString("block.minecraft.magenta_stained_glass", "Magenta Stained Glass")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_STAINED_GLASS, langConfig.getString("block.minecraft.light_blue_stained_glass", "Light Blue Stained Glass")));
        itemNames.add(new ItemName(Material.YELLOW_STAINED_GLASS, langConfig.getString("block.minecraft.yellow_stained_glass", "Yellow Stained Glass")));
        itemNames.add(new ItemName(Material.LIME_STAINED_GLASS, langConfig.getString("block.minecraft.lime_stained_glass", "Lime Stained Glass")));
        itemNames.add(new ItemName(Material.PINK_STAINED_GLASS, langConfig.getString("block.minecraft.pink_stained_glass", "Pink Stained Glass")));
        itemNames.add(new ItemName(Material.GRAY_STAINED_GLASS, langConfig.getString("block.minecraft.gray_stained_glass", "Gray Stained Glass")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_STAINED_GLASS, langConfig.getString("block.minecraft.light_gray_stained_glass", "Light Gray Stained Glass")));
        itemNames.add(new ItemName(Material.CYAN_STAINED_GLASS, langConfig.getString("block.minecraft.cyan_stained_glass", "Cyan Stained Glass")));
        itemNames.add(new ItemName(Material.PURPLE_STAINED_GLASS, langConfig.getString("block.minecraft.purple_stained_glass", "Purple Stained Glass")));
        itemNames.add(new ItemName(Material.BLUE_STAINED_GLASS, langConfig.getString("block.minecraft.blue_stained_glass", "Blue Stained Glass")));
        itemNames.add(new ItemName(Material.BROWN_STAINED_GLASS, langConfig.getString("block.minecraft.brown_stained_glass", "Brown Stained Glass")));
        itemNames.add(new ItemName(Material.GREEN_STAINED_GLASS, langConfig.getString("block.minecraft.green_stained_glass", "Green Stained Glass")));
        itemNames.add(new ItemName(Material.RED_STAINED_GLASS, langConfig.getString("block.minecraft.red_stained_glass", "Red Stained Glass")));
        itemNames.add(new ItemName(Material.BLACK_STAINED_GLASS, langConfig.getString("block.minecraft.black_stained_glass", "Black Stained Glass")));
        itemNames.add(new ItemName(Material.WHITE_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.white_stained_glass_pane", "White Stained Glass Pane")));
        itemNames.add(new ItemName(Material.ORANGE_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.orange_stained_glass_pane", "Orange Stained Glass Pane")));
        itemNames.add(new ItemName(Material.MAGENTA_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.magenta_stained_glass_pane", "Magenta Stained Glass Pane")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.light_blue_stained_glass_pane", "Light Blue Stained Glass Pane")));
        itemNames.add(new ItemName(Material.YELLOW_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.yellow_stained_glass_pane", "Yellow Stained Glass Pane")));
        itemNames.add(new ItemName(Material.LIME_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.lime_stained_glass_pane", "Lime Stained Glass Pane")));
        itemNames.add(new ItemName(Material.PINK_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.pink_stained_glass_pane", "Pink Stained Glass Pane")));
        itemNames.add(new ItemName(Material.GRAY_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.gray_stained_glass_pane", "Gray Stained Glass Pane")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.light_gray_stained_glass_pane", "Light Gray Stained Glass Pane")));
        itemNames.add(new ItemName(Material.CYAN_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.cyan_stained_glass_pane", "Cyan Stained Glass Pane")));
        itemNames.add(new ItemName(Material.PURPLE_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.purple_stained_glass_pane", "Purple Stained Glass Pane")));
        itemNames.add(new ItemName(Material.BLUE_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.blue_stained_glass_pane", "Blue Stained Glass Pane")));
        itemNames.add(new ItemName(Material.BROWN_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.brown_stained_glass_pane", "Brown Stained Glass Pane")));
        itemNames.add(new ItemName(Material.GREEN_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.green_stained_glass_pane", "Green Stained Glass Pane")));
        itemNames.add(new ItemName(Material.RED_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.red_stained_glass_pane", "Red Stained Glass Pane")));
        itemNames.add(new ItemName(Material.BLACK_STAINED_GLASS_PANE, langConfig.getString("block.minecraft.black_stained_glass_pane", "Black Stained Glass Pane")));
        itemNames.add(new ItemName(Material.GLASS_PANE, langConfig.getString("block.minecraft.glass_pane", "Glass Pane")));
        itemNames.add(new ItemName(Material.DANDELION, langConfig.getString("block.minecraft.dandelion", "Dandelion")));
        itemNames.add(new ItemName(Material.POPPY, langConfig.getString("block.minecraft.poppy", "Poppy")));
        itemNames.add(new ItemName(Material.BLUE_ORCHID, langConfig.getString("block.minecraft.blue_orchid", "Blue Orchid")));
        itemNames.add(new ItemName(Material.ALLIUM, langConfig.getString("block.minecraft.allium", "Allium")));
        itemNames.add(new ItemName(Material.AZURE_BLUET, langConfig.getString("block.minecraft.azure_bluet", "Azure Bluet")));
        itemNames.add(new ItemName(Material.RED_TULIP, langConfig.getString("block.minecraft.red_tulip", "Red Tulip")));
        itemNames.add(new ItemName(Material.ORANGE_TULIP, langConfig.getString("block.minecraft.orange_tulip", "Orange Tulip")));
        itemNames.add(new ItemName(Material.WHITE_TULIP, langConfig.getString("block.minecraft.white_tulip", "White Tulip")));
        itemNames.add(new ItemName(Material.PINK_TULIP, langConfig.getString("block.minecraft.pink_tulip", "Pink Tulip")));
        itemNames.add(new ItemName(Material.OXEYE_DAISY, langConfig.getString("block.minecraft.oxeye_daisy", "Oxeye Daisy")));
        itemNames.add(new ItemName(Material.SUNFLOWER, langConfig.getString("block.minecraft.sunflower", "Sunflower")));
        itemNames.add(new ItemName(Material.LILAC, langConfig.getString("block.minecraft.lilac", "Lilac")));
        itemNames.add(new ItemName(Material.TALL_GRASS, langConfig.getString("block.minecraft.tall_grass", "Tall Grass")));
        itemNames.add(new ItemName(Material.TALL_SEAGRASS, langConfig.getString("block.minecraft.tall_seagrass", "Tall Seagrass")));
        itemNames.add(new ItemName(Material.LARGE_FERN, langConfig.getString("block.minecraft.large_fern", "Large Fern")));
        itemNames.add(new ItemName(Material.ROSE_BUSH, langConfig.getString("block.minecraft.rose_bush", "Rose Bush")));
        itemNames.add(new ItemName(Material.PEONY, langConfig.getString("block.minecraft.peony", "Peony")));
        itemNames.add(new ItemName(Material.SEAGRASS, langConfig.getString("block.minecraft.seagrass", "Seagrass")));
        itemNames.add(new ItemName(Material.SEA_PICKLE, langConfig.getString("block.minecraft.sea_pickle", "Sea Pickle")));
        itemNames.add(new ItemName(Material.BROWN_MUSHROOM, langConfig.getString("block.minecraft.brown_mushroom", "Brown Mushroom")));
        itemNames.add(new ItemName(Material.RED_MUSHROOM_BLOCK, langConfig.getString("block.minecraft.red_mushroom_block", "Red Mushroom Block")));
        itemNames.add(new ItemName(Material.BROWN_MUSHROOM_BLOCK, langConfig.getString("block.minecraft.brown_mushroom_block", "Brown Mushroom Block")));
        itemNames.add(new ItemName(Material.MUSHROOM_STEM, langConfig.getString("block.minecraft.mushroom_stem", "Mushroom Stem")));
        itemNames.add(new ItemName(Material.GOLD_BLOCK, langConfig.getString("block.minecraft.gold_block", "Block of Gold")));
        itemNames.add(new ItemName(Material.IRON_BLOCK, langConfig.getString("block.minecraft.iron_block", "Block of Iron")));
        itemNames.add(new ItemName(Material.SMOOTH_STONE, langConfig.getString("block.minecraft.smooth_stone", "Smooth Stone")));
        itemNames.add(new ItemName(Material.SMOOTH_SANDSTONE, langConfig.getString("block.minecraft.smooth_sandstone", "Smooth Sandstone")));
        itemNames.add(new ItemName(Material.SMOOTH_RED_SANDSTONE, langConfig.getString("block.minecraft.smooth_red_sandstone", "Smooth Red Sandstone")));
        itemNames.add(new ItemName(Material.SMOOTH_QUARTZ, langConfig.getString("block.minecraft.smooth_quartz", "Smooth Quartz")));
        itemNames.add(new ItemName(Material.STONE_SLAB, langConfig.getString("block.minecraft.stone_slab", "Stone Slab")));
        itemNames.add(new ItemName(Material.SANDSTONE_SLAB, langConfig.getString("block.minecraft.sandstone_slab", "Sandstone Slab")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE_SLAB, langConfig.getString("block.minecraft.red_sandstone_slab", "Red Sandstone Slab")));
        itemNames.add(new ItemName(Material.PETRIFIED_OAK_SLAB, langConfig.getString("block.minecraft.petrified_oak_slab", "Petrified Oak Slab")));
        itemNames.add(new ItemName(Material.COBBLESTONE_SLAB, langConfig.getString("block.minecraft.cobblestone_slab", "Cobblestone Slab")));
        itemNames.add(new ItemName(Material.BRICK_SLAB, langConfig.getString("block.minecraft.brick_slab", "Brick Slab")));
        itemNames.add(new ItemName(Material.STONE_BRICK_SLAB, langConfig.getString("block.minecraft.stone_brick_slab", "Stone Brick Slab")));
        itemNames.add(new ItemName(Material.NETHER_BRICK_SLAB, langConfig.getString("block.minecraft.nether_brick_slab", "Nether Brick Slab")));
        itemNames.add(new ItemName(Material.QUARTZ_SLAB, langConfig.getString("block.minecraft.quartz_slab", "Quartz Slab")));
        itemNames.add(new ItemName(Material.OAK_SLAB, langConfig.getString("block.minecraft.oak_slab", "Oak Slab")));
        itemNames.add(new ItemName(Material.SPRUCE_SLAB, langConfig.getString("block.minecraft.spruce_slab", "Spruce Slab")));
        itemNames.add(new ItemName(Material.BIRCH_SLAB, langConfig.getString("block.minecraft.birch_slab", "Birch Slab")));
        itemNames.add(new ItemName(Material.JUNGLE_SLAB, langConfig.getString("block.minecraft.jungle_slab", "Jungle Slab")));
        itemNames.add(new ItemName(Material.ACACIA_SLAB, langConfig.getString("block.minecraft.acacia_slab", "Acacia Slab")));
        itemNames.add(new ItemName(Material.DARK_OAK_SLAB, langConfig.getString("block.minecraft.dark_oak_slab", "Dark Oak Slab")));
        itemNames.add(new ItemName(Material.DARK_PRISMARINE_SLAB, langConfig.getString("block.minecraft.dark_prismarine_slab", "Dark Prismarine Slab")));
        itemNames.add(new ItemName(Material.PRISMARINE_SLAB, langConfig.getString("block.minecraft.prismarine_slab", "Prismarine Slab")));
        itemNames.add(new ItemName(Material.PRISMARINE_BRICK_SLAB, langConfig.getString("block.minecraft.prismarine_brick_slab", "Prismarine Brick Slab")));
        itemNames.add(new ItemName(Material.BRICKS, langConfig.getString("block.minecraft.bricks", "Bricks")));
        itemNames.add(new ItemName(Material.TNT, langConfig.getString("block.minecraft.tnt", "TNT")));
        itemNames.add(new ItemName(Material.BOOKSHELF, langConfig.getString("block.minecraft.bookshelf", "Bookshelf")));
        itemNames.add(new ItemName(Material.MOSSY_COBBLESTONE, langConfig.getString("block.minecraft.mossy_cobblestone", "Mossy Cobblestone")));
        itemNames.add(new ItemName(Material.OBSIDIAN, langConfig.getString("block.minecraft.obsidian", "Obsidian")));
        itemNames.add(new ItemName(Material.TORCH, langConfig.getString("block.minecraft.torch", "Torch")));
        itemNames.add(new ItemName(Material.WALL_TORCH, langConfig.getString("block.minecraft.wall_torch", "Wall Torch")));
        itemNames.add(new ItemName(Material.FIRE, langConfig.getString("block.minecraft.fire", "Fire")));
        itemNames.add(new ItemName(Material.SPAWNER, langConfig.getString("block.minecraft.spawner", "Spawner")));
        itemNames.add(new ItemName(Material.OAK_STAIRS, langConfig.getString("block.minecraft.oak_stairs", "Oak Stairs")));
        itemNames.add(new ItemName(Material.SPRUCE_STAIRS, langConfig.getString("block.minecraft.spruce_stairs", "Spruce Stairs")));
        itemNames.add(new ItemName(Material.BIRCH_STAIRS, langConfig.getString("block.minecraft.birch_stairs", "Birch Stairs")));
        itemNames.add(new ItemName(Material.JUNGLE_STAIRS, langConfig.getString("block.minecraft.jungle_stairs", "Jungle Stairs")));
        itemNames.add(new ItemName(Material.ACACIA_STAIRS, langConfig.getString("block.minecraft.acacia_stairs", "Acacia Stairs")));
        itemNames.add(new ItemName(Material.DARK_OAK_STAIRS, langConfig.getString("block.minecraft.dark_oak_stairs", "Dark Oak Stairs")));
        itemNames.add(new ItemName(Material.DARK_PRISMARINE_STAIRS, langConfig.getString("block.minecraft.dark_prismarine_stairs", "Dark Prismarine Stairs")));
        itemNames.add(new ItemName(Material.PRISMARINE_STAIRS, langConfig.getString("block.minecraft.prismarine_stairs", "Prismarine Stairs")));
        itemNames.add(new ItemName(Material.PRISMARINE_BRICK_STAIRS, langConfig.getString("block.minecraft.prismarine_brick_stairs", "Prismarine Brick Stairs")));
        itemNames.add(new ItemName(Material.CHEST, langConfig.getString("block.minecraft.chest", "Chest")));
        itemNames.add(new ItemName(Material.TRAPPED_CHEST, langConfig.getString("block.minecraft.trapped_chest", "Trapped Chest")));
        itemNames.add(new ItemName(Material.REDSTONE_WIRE, langConfig.getString("block.minecraft.redstone_wire", "Redstone Dust")));
        itemNames.add(new ItemName(Material.DIAMOND_ORE, langConfig.getString("block.minecraft.diamond_ore", "Diamond Ore")));
        itemNames.add(new ItemName(Material.COAL_BLOCK, langConfig.getString("block.minecraft.coal_block", "Block of Coal")));
        itemNames.add(new ItemName(Material.DIAMOND_BLOCK, langConfig.getString("block.minecraft.diamond_block", "Block of Diamond")));
        itemNames.add(new ItemName(Material.CRAFTING_TABLE, langConfig.getString("block.minecraft.crafting_table", "Crafting Table")));
        itemNames.add(new ItemName(Material.WHEAT, langConfig.getString("block.minecraft.wheat", "Wheat Crops")));
        itemNames.add(new ItemName(Material.FARMLAND, langConfig.getString("block.minecraft.farmland", "Farmland")));
        itemNames.add(new ItemName(Material.FURNACE, langConfig.getString("block.minecraft.furnace", "Furnace")));
        itemNames.add(new ItemName(Material.SIGN, langConfig.getString("block.minecraft.sign", "Sign")));
        itemNames.add(new ItemName(Material.WALL_SIGN, langConfig.getString("block.minecraft.wall_sign", "Wall Sign")));
        itemNames.add(new ItemName(Material.LADDER, langConfig.getString("block.minecraft.ladder", "Ladder")));
        itemNames.add(new ItemName(Material.RAIL, langConfig.getString("block.minecraft.rail", "Rail")));
        itemNames.add(new ItemName(Material.POWERED_RAIL, langConfig.getString("block.minecraft.powered_rail", "Powered Rail")));
        itemNames.add(new ItemName(Material.ACTIVATOR_RAIL, langConfig.getString("block.minecraft.activator_rail", "Activator Rail")));
        itemNames.add(new ItemName(Material.DETECTOR_RAIL, langConfig.getString("block.minecraft.detector_rail", "Detector Rail")));
        itemNames.add(new ItemName(Material.COBBLESTONE_STAIRS, langConfig.getString("block.minecraft.cobblestone_stairs", "Cobblestone Stairs")));
        itemNames.add(new ItemName(Material.SANDSTONE_STAIRS, langConfig.getString("block.minecraft.sandstone_stairs", "Sandstone Stairs")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE_STAIRS, langConfig.getString("block.minecraft.red_sandstone_stairs", "Red Sandstone Stairs")));
        itemNames.add(new ItemName(Material.LEVER, langConfig.getString("block.minecraft.lever", "Lever")));
        itemNames.add(new ItemName(Material.STONE_PRESSURE_PLATE, langConfig.getString("block.minecraft.stone_pressure_plate", "Stone Pressure Plate")));
        itemNames.add(new ItemName(Material.OAK_PRESSURE_PLATE, langConfig.getString("block.minecraft.oak_pressure_plate", "Oak Pressure Plate")));
        itemNames.add(new ItemName(Material.SPRUCE_PRESSURE_PLATE, langConfig.getString("block.minecraft.spruce_pressure_plate", "Spruce Pressure Plate")));
        itemNames.add(new ItemName(Material.BIRCH_PRESSURE_PLATE, langConfig.getString("block.minecraft.birch_pressure_plate", "Birch Pressure Plate")));
        itemNames.add(new ItemName(Material.JUNGLE_PRESSURE_PLATE, langConfig.getString("block.minecraft.jungle_pressure_plate", "Jungle Pressure Plate")));
        itemNames.add(new ItemName(Material.ACACIA_PRESSURE_PLATE, langConfig.getString("block.minecraft.acacia_pressure_plate", "Acacia Pressure Plate")));
        itemNames.add(new ItemName(Material.DARK_OAK_PRESSURE_PLATE, langConfig.getString("block.minecraft.dark_oak_pressure_plate", "Dark Oak Pressure Plate")));
        itemNames.add(new ItemName(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, langConfig.getString("block.minecraft.light_weighted_pressure_plate", "Light Weighted Pressure Plate")));
        itemNames.add(new ItemName(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, langConfig.getString("block.minecraft.heavy_weighted_pressure_plate", "Heavy Weighted Pressure Plate")));
        itemNames.add(new ItemName(Material.IRON_DOOR, langConfig.getString("block.minecraft.iron_door", "Iron Door")));
        itemNames.add(new ItemName(Material.REDSTONE_ORE, langConfig.getString("block.minecraft.redstone_ore", "Redstone Ore")));
        itemNames.add(new ItemName(Material.REDSTONE_TORCH, langConfig.getString("block.minecraft.redstone_torch", "Redstone Torch")));
        itemNames.add(new ItemName(Material.REDSTONE_WALL_TORCH, langConfig.getString("block.minecraft.redstone_wall_torch", "Redstone Wall Torch")));
        itemNames.add(new ItemName(Material.STONE_BUTTON, langConfig.getString("block.minecraft.stone_button", "Stone Button")));
        itemNames.add(new ItemName(Material.OAK_BUTTON, langConfig.getString("block.minecraft.oak_button", "Oak Button")));
        itemNames.add(new ItemName(Material.SPRUCE_BUTTON, langConfig.getString("block.minecraft.spruce_button", "Spruce Button")));
        itemNames.add(new ItemName(Material.BIRCH_BUTTON, langConfig.getString("block.minecraft.birch_button", "Birch Button")));
        itemNames.add(new ItemName(Material.JUNGLE_BUTTON, langConfig.getString("block.minecraft.jungle_button", "Jungle Button")));
        itemNames.add(new ItemName(Material.ACACIA_BUTTON, langConfig.getString("block.minecraft.acacia_button", "Acacia Button")));
        itemNames.add(new ItemName(Material.DARK_OAK_BUTTON, langConfig.getString("block.minecraft.dark_oak_button", "Dark Oak Button")));
        itemNames.add(new ItemName(Material.SNOW, langConfig.getString("block.minecraft.snow", "Snow")));
        itemNames.add(new ItemName(Material.WHITE_CARPET, langConfig.getString("block.minecraft.white_carpet", "White Carpet")));
        itemNames.add(new ItemName(Material.ORANGE_CARPET, langConfig.getString("block.minecraft.orange_carpet", "Orange Carpet")));
        itemNames.add(new ItemName(Material.MAGENTA_CARPET, langConfig.getString("block.minecraft.magenta_carpet", "Magenta Carpet")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_CARPET, langConfig.getString("block.minecraft.light_blue_carpet", "Light Blue Carpet")));
        itemNames.add(new ItemName(Material.YELLOW_CARPET, langConfig.getString("block.minecraft.yellow_carpet", "Yellow Carpet")));
        itemNames.add(new ItemName(Material.LIME_CARPET, langConfig.getString("block.minecraft.lime_carpet", "Lime Carpet")));
        itemNames.add(new ItemName(Material.PINK_CARPET, langConfig.getString("block.minecraft.pink_carpet", "Pink Carpet")));
        itemNames.add(new ItemName(Material.GRAY_CARPET, langConfig.getString("block.minecraft.gray_carpet", "Gray Carpet")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_CARPET, langConfig.getString("block.minecraft.light_gray_carpet", "Light Gray Carpet")));
        itemNames.add(new ItemName(Material.CYAN_CARPET, langConfig.getString("block.minecraft.cyan_carpet", "Cyan Carpet")));
        itemNames.add(new ItemName(Material.PURPLE_CARPET, langConfig.getString("block.minecraft.purple_carpet", "Purple Carpet")));
        itemNames.add(new ItemName(Material.BLUE_CARPET, langConfig.getString("block.minecraft.blue_carpet", "Blue Carpet")));
        itemNames.add(new ItemName(Material.BROWN_CARPET, langConfig.getString("block.minecraft.brown_carpet", "Brown Carpet")));
        itemNames.add(new ItemName(Material.GREEN_CARPET, langConfig.getString("block.minecraft.green_carpet", "Green Carpet")));
        itemNames.add(new ItemName(Material.RED_CARPET, langConfig.getString("block.minecraft.red_carpet", "Red Carpet")));
        itemNames.add(new ItemName(Material.BLACK_CARPET, langConfig.getString("block.minecraft.black_carpet", "Black Carpet")));
        itemNames.add(new ItemName(Material.ICE, langConfig.getString("block.minecraft.ice", "Ice")));
        itemNames.add(new ItemName(Material.FROSTED_ICE, langConfig.getString("block.minecraft.frosted_ice", "Frosted Ice")));
        itemNames.add(new ItemName(Material.PACKED_ICE, langConfig.getString("block.minecraft.packed_ice", "Packed Ice")));
        itemNames.add(new ItemName(Material.BLUE_ICE, langConfig.getString("block.minecraft.blue_ice", "Blue Ice")));
        itemNames.add(new ItemName(Material.CACTUS, langConfig.getString("block.minecraft.cactus", "Cactus")));
        itemNames.add(new ItemName(Material.CLAY, langConfig.getString("block.minecraft.clay", "Clay")));
        itemNames.add(new ItemName(Material.WHITE_TERRACOTTA, langConfig.getString("block.minecraft.white_terracotta", "White Terracotta")));
        itemNames.add(new ItemName(Material.ORANGE_TERRACOTTA, langConfig.getString("block.minecraft.orange_terracotta", "Orange Terracotta")));
        itemNames.add(new ItemName(Material.MAGENTA_TERRACOTTA, langConfig.getString("block.minecraft.magenta_terracotta", "Magenta Terracotta")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_TERRACOTTA, langConfig.getString("block.minecraft.light_blue_terracotta", "Light Blue Terracotta")));
        itemNames.add(new ItemName(Material.YELLOW_TERRACOTTA, langConfig.getString("block.minecraft.yellow_terracotta", "Yellow Terracotta")));
        itemNames.add(new ItemName(Material.LIME_TERRACOTTA, langConfig.getString("block.minecraft.lime_terracotta", "Lime Terracotta")));
        itemNames.add(new ItemName(Material.PINK_TERRACOTTA, langConfig.getString("block.minecraft.pink_terracotta", "Pink Terracotta")));
        itemNames.add(new ItemName(Material.GRAY_TERRACOTTA, langConfig.getString("block.minecraft.gray_terracotta", "Gray Terracotta")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_TERRACOTTA, langConfig.getString("block.minecraft.light_gray_terracotta", "Light Gray Terracotta")));
        itemNames.add(new ItemName(Material.CYAN_TERRACOTTA, langConfig.getString("block.minecraft.cyan_terracotta", "Cyan Terracotta")));
        itemNames.add(new ItemName(Material.PURPLE_TERRACOTTA, langConfig.getString("block.minecraft.purple_terracotta", "Purple Terracotta")));
        itemNames.add(new ItemName(Material.BLUE_TERRACOTTA, langConfig.getString("block.minecraft.blue_terracotta", "Blue Terracotta")));
        itemNames.add(new ItemName(Material.BROWN_TERRACOTTA, langConfig.getString("block.minecraft.brown_terracotta", "Brown Terracotta")));
        itemNames.add(new ItemName(Material.GREEN_TERRACOTTA, langConfig.getString("block.minecraft.green_terracotta", "Green Terracotta")));
        itemNames.add(new ItemName(Material.RED_TERRACOTTA, langConfig.getString("block.minecraft.red_terracotta", "Red Terracotta")));
        itemNames.add(new ItemName(Material.BLACK_TERRACOTTA, langConfig.getString("block.minecraft.black_terracotta", "Black Terracotta")));
        itemNames.add(new ItemName(Material.TERRACOTTA, langConfig.getString("block.minecraft.terracotta", "Terracotta")));
        itemNames.add(new ItemName(Material.SUGAR_CANE, langConfig.getString("block.minecraft.sugar_cane", "Sugar Cane")));
        itemNames.add(new ItemName(Material.JUKEBOX, langConfig.getString("block.minecraft.jukebox", "Jukebox")));
        itemNames.add(new ItemName(Material.OAK_FENCE, langConfig.getString("block.minecraft.oak_fence", "Oak Fence")));
        itemNames.add(new ItemName(Material.SPRUCE_FENCE, langConfig.getString("block.minecraft.spruce_fence", "Spruce Fence")));
        itemNames.add(new ItemName(Material.BIRCH_FENCE, langConfig.getString("block.minecraft.birch_fence", "Birch Fence")));
        itemNames.add(new ItemName(Material.JUNGLE_FENCE, langConfig.getString("block.minecraft.jungle_fence", "Jungle Fence")));
        itemNames.add(new ItemName(Material.DARK_OAK_FENCE, langConfig.getString("block.minecraft.dark_oak_fence", "Dark Oak Fence")));
        itemNames.add(new ItemName(Material.ACACIA_FENCE, langConfig.getString("block.minecraft.acacia_fence", "Acacia Fence")));
        itemNames.add(new ItemName(Material.OAK_FENCE_GATE, langConfig.getString("block.minecraft.oak_fence_gate", "Oak Fence Gate")));
        itemNames.add(new ItemName(Material.SPRUCE_FENCE_GATE, langConfig.getString("block.minecraft.spruce_fence_gate", "Spruce Fence Gate")));
        itemNames.add(new ItemName(Material.BIRCH_FENCE_GATE, langConfig.getString("block.minecraft.birch_fence_gate", "Birch Fence Gate")));
        itemNames.add(new ItemName(Material.JUNGLE_FENCE_GATE, langConfig.getString("block.minecraft.jungle_fence_gate", "Jungle Fence Gate")));
        itemNames.add(new ItemName(Material.DARK_OAK_FENCE_GATE, langConfig.getString("block.minecraft.dark_oak_fence_gate", "Dark Oak Fence Gate")));
        itemNames.add(new ItemName(Material.ACACIA_FENCE_GATE, langConfig.getString("block.minecraft.acacia_fence_gate", "Acacia Fence Gate")));
        itemNames.add(new ItemName(Material.PUMPKIN_STEM, langConfig.getString("block.minecraft.pumpkin_stem", "Pumpkin Stem")));
        itemNames.add(new ItemName(Material.ATTACHED_PUMPKIN_STEM, langConfig.getString("block.minecraft.attached_pumpkin_stem", "Attached Pumpkin Stem")));
        itemNames.add(new ItemName(Material.PUMPKIN, langConfig.getString("block.minecraft.pumpkin", "Pumpkin")));
        itemNames.add(new ItemName(Material.CARVED_PUMPKIN, langConfig.getString("block.minecraft.carved_pumpkin", "Carved Pumpkin")));
        itemNames.add(new ItemName(Material.JACK_O_LANTERN, langConfig.getString("block.minecraft.jack_o_lantern", "Jack o'Lantern")));
        itemNames.add(new ItemName(Material.NETHERRACK, langConfig.getString("block.minecraft.netherrack", "Netherrack")));
        itemNames.add(new ItemName(Material.SOUL_SAND, langConfig.getString("block.minecraft.soul_sand", "Soul Sand")));
        itemNames.add(new ItemName(Material.GLOWSTONE, langConfig.getString("block.minecraft.glowstone", "Glowstone")));
        itemNames.add(new ItemName(Material.NETHER_PORTAL, langConfig.getString("block.minecraft.nether_portal", "Nether Portal")));
        itemNames.add(new ItemName(Material.WHITE_WOOL, langConfig.getString("block.minecraft.white_wool", "White Wool")));
        itemNames.add(new ItemName(Material.ORANGE_WOOL, langConfig.getString("block.minecraft.orange_wool", "Orange Wool")));
        itemNames.add(new ItemName(Material.MAGENTA_WOOL, langConfig.getString("block.minecraft.magenta_wool", "Magenta Wool")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_WOOL, langConfig.getString("block.minecraft.light_blue_wool", "Light Blue Wool")));
        itemNames.add(new ItemName(Material.YELLOW_WOOL, langConfig.getString("block.minecraft.yellow_wool", "Yellow Wool")));
        itemNames.add(new ItemName(Material.LIME_WOOL, langConfig.getString("block.minecraft.lime_wool", "Lime Wool")));
        itemNames.add(new ItemName(Material.PINK_WOOL, langConfig.getString("block.minecraft.pink_wool", "Pink Wool")));
        itemNames.add(new ItemName(Material.GRAY_WOOL, langConfig.getString("block.minecraft.gray_wool", "Gray Wool")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_WOOL, langConfig.getString("block.minecraft.light_gray_wool", "Light Gray Wool")));
        itemNames.add(new ItemName(Material.CYAN_WOOL, langConfig.getString("block.minecraft.cyan_wool", "Cyan Wool")));
        itemNames.add(new ItemName(Material.PURPLE_WOOL, langConfig.getString("block.minecraft.purple_wool", "Purple Wool")));
        itemNames.add(new ItemName(Material.BLUE_WOOL, langConfig.getString("block.minecraft.blue_wool", "Blue Wool")));
        itemNames.add(new ItemName(Material.BROWN_WOOL, langConfig.getString("block.minecraft.brown_wool", "Brown Wool")));
        itemNames.add(new ItemName(Material.GREEN_WOOL, langConfig.getString("block.minecraft.green_wool", "Green Wool")));
        itemNames.add(new ItemName(Material.RED_WOOL, langConfig.getString("block.minecraft.red_wool", "Red Wool")));
        itemNames.add(new ItemName(Material.BLACK_WOOL, langConfig.getString("block.minecraft.black_wool", "Black Wool")));
        itemNames.add(new ItemName(Material.LAPIS_ORE, langConfig.getString("block.minecraft.lapis_ore", "Lapis Lazuli Ore")));
        itemNames.add(new ItemName(Material.LAPIS_BLOCK, langConfig.getString("block.minecraft.lapis_block", "Lapis Lazuli Block")));
        itemNames.add(new ItemName(Material.DISPENSER, langConfig.getString("block.minecraft.dispenser", "Dispenser")));
        itemNames.add(new ItemName(Material.DROPPER, langConfig.getString("block.minecraft.dropper", "Dropper")));
        itemNames.add(new ItemName(Material.NOTE_BLOCK, langConfig.getString("block.minecraft.note_block", "Note Block")));
        itemNames.add(new ItemName(Material.CAKE, langConfig.getString("block.minecraft.cake", "Cake")));
        itemNames.add(new ItemName(Material.OAK_TRAPDOOR, langConfig.getString("block.minecraft.oak_trapdoor", "Oak Trapdoor")));
        itemNames.add(new ItemName(Material.SPRUCE_TRAPDOOR, langConfig.getString("block.minecraft.spruce_trapdoor", "Spruce Trapdoor")));
        itemNames.add(new ItemName(Material.BIRCH_TRAPDOOR, langConfig.getString("block.minecraft.birch_trapdoor", "Birch Trapdoor")));
        itemNames.add(new ItemName(Material.JUNGLE_TRAPDOOR, langConfig.getString("block.minecraft.jungle_trapdoor", "Jungle Trapdoor")));
        itemNames.add(new ItemName(Material.ACACIA_TRAPDOOR, langConfig.getString("block.minecraft.acacia_trapdoor", "Acacia Trapdoor")));
        itemNames.add(new ItemName(Material.DARK_OAK_TRAPDOOR, langConfig.getString("block.minecraft.dark_oak_trapdoor", "Dark Oak Trapdoor")));
        itemNames.add(new ItemName(Material.IRON_TRAPDOOR, langConfig.getString("block.minecraft.iron_trapdoor", "Iron Trapdoor")));
        itemNames.add(new ItemName(Material.COBWEB, langConfig.getString("block.minecraft.cobweb", "Cobweb")));
        itemNames.add(new ItemName(Material.STONE_BRICKS, langConfig.getString("block.minecraft.stone_bricks", "Stone Bricks")));
        itemNames.add(new ItemName(Material.MOSSY_STONE_BRICKS, langConfig.getString("block.minecraft.mossy_stone_bricks", "Mossy Stone Bricks")));
        itemNames.add(new ItemName(Material.CRACKED_STONE_BRICKS, langConfig.getString("block.minecraft.cracked_stone_bricks", "Cracked Stone Bricks")));
        itemNames.add(new ItemName(Material.CHISELED_STONE_BRICKS, langConfig.getString("block.minecraft.chiseled_stone_bricks", "Chiseled Stone Bricks")));
        itemNames.add(new ItemName(Material.INFESTED_STONE, langConfig.getString("block.minecraft.infested_stone", "Infested Stone")));
        itemNames.add(new ItemName(Material.INFESTED_COBBLESTONE, langConfig.getString("block.minecraft.infested_cobblestone", "Infested Cobblestone")));
        itemNames.add(new ItemName(Material.INFESTED_STONE_BRICKS, langConfig.getString("block.minecraft.infested_stone_bricks", "Infested Stone Bricks")));
        itemNames.add(new ItemName(Material.INFESTED_MOSSY_STONE_BRICKS, langConfig.getString("block.minecraft.infested_mossy_stone_bricks", "Infested Mossy Stone Bricks")));
        itemNames.add(new ItemName(Material.INFESTED_CRACKED_STONE_BRICKS, langConfig.getString("block.minecraft.infested_cracked_stone_bricks", "Infested Cracked Stone Bricks")));
        itemNames.add(new ItemName(Material.INFESTED_CHISELED_STONE_BRICKS, langConfig.getString("block.minecraft.infested_chiseled_stone_bricks", "Infested Chiseled Stone Bricks")));
        itemNames.add(new ItemName(Material.PISTON, langConfig.getString("block.minecraft.piston", "Piston")));
        itemNames.add(new ItemName(Material.STICKY_PISTON, langConfig.getString("block.minecraft.sticky_piston", "Sticky Piston")));
        itemNames.add(new ItemName(Material.IRON_BARS, langConfig.getString("block.minecraft.iron_bars", "Iron Bars")));
        itemNames.add(new ItemName(Material.MELON, langConfig.getString("block.minecraft.melon", "Melon")));
        itemNames.add(new ItemName(Material.BRICK_STAIRS, langConfig.getString("block.minecraft.brick_stairs", "Brick Stairs")));
        itemNames.add(new ItemName(Material.STONE_BRICK_STAIRS, langConfig.getString("block.minecraft.stone_brick_stairs", "Stone Brick Stairs")));
        itemNames.add(new ItemName(Material.VINE, langConfig.getString("block.minecraft.vine", "Vines")));
        itemNames.add(new ItemName(Material.NETHER_BRICKS, langConfig.getString("block.minecraft.nether_bricks", "Nether Bricks")));
        itemNames.add(new ItemName(Material.NETHER_BRICK_FENCE, langConfig.getString("block.minecraft.nether_brick_fence", "Nether Brick Fence")));
        itemNames.add(new ItemName(Material.NETHER_BRICK_STAIRS, langConfig.getString("block.minecraft.nether_brick_stairs", "Nether Brick Stairs")));
        itemNames.add(new ItemName(Material.NETHER_WART, langConfig.getString("block.minecraft.nether_wart", "Nether Wart")));
        itemNames.add(new ItemName(Material.CAULDRON, langConfig.getString("block.minecraft.cauldron", "Cauldron")));
        itemNames.add(new ItemName(Material.ENCHANTING_TABLE, langConfig.getString("block.minecraft.enchanting_table", "Enchanting Table")));
        itemNames.add(new ItemName(Material.ANVIL, langConfig.getString("block.minecraft.anvil", "Anvil")));
        itemNames.add(new ItemName(Material.CHIPPED_ANVIL, langConfig.getString("block.minecraft.chipped_anvil", "Chipped Anvil")));
        itemNames.add(new ItemName(Material.DAMAGED_ANVIL, langConfig.getString("block.minecraft.damaged_anvil", "Damaged Anvil")));
        itemNames.add(new ItemName(Material.END_STONE, langConfig.getString("block.minecraft.end_stone", "End Stone")));
        itemNames.add(new ItemName(Material.END_PORTAL_FRAME, langConfig.getString("block.minecraft.end_portal_frame", "End Portal Frame")));
        itemNames.add(new ItemName(Material.MYCELIUM, langConfig.getString("block.minecraft.mycelium", "Mycelium")));
        itemNames.add(new ItemName(Material.LILY_PAD, langConfig.getString("block.minecraft.lily_pad", "Lily Pad")));
        itemNames.add(new ItemName(Material.DRAGON_EGG, langConfig.getString("block.minecraft.dragon_egg", "Dragon Egg")));
        itemNames.add(new ItemName(Material.REDSTONE_LAMP, langConfig.getString("block.minecraft.redstone_lamp", "Redstone Lamp")));
        itemNames.add(new ItemName(Material.COCOA, langConfig.getString("block.minecraft.cocoa", "Cocoa")));
        itemNames.add(new ItemName(Material.ENDER_CHEST, langConfig.getString("block.minecraft.ender_chest", "Ender Chest")));
        itemNames.add(new ItemName(Material.EMERALD_ORE, langConfig.getString("block.minecraft.emerald_ore", "Emerald Ore")));
        itemNames.add(new ItemName(Material.EMERALD_BLOCK, langConfig.getString("block.minecraft.emerald_block", "Block of Emerald")));
        itemNames.add(new ItemName(Material.REDSTONE_BLOCK, langConfig.getString("block.minecraft.redstone_block", "Block of Redstone")));
        itemNames.add(new ItemName(Material.TRIPWIRE, langConfig.getString("block.minecraft.tripwire", "Tripwire")));
        itemNames.add(new ItemName(Material.TRIPWIRE_HOOK, langConfig.getString("block.minecraft.tripwire_hook", "Tripwire Hook")));
        itemNames.add(new ItemName(Material.COMMAND_BLOCK, langConfig.getString("block.minecraft.command_block", "Command Block")));
        itemNames.add(new ItemName(Material.REPEATING_COMMAND_BLOCK, langConfig.getString("block.minecraft.repeating_command_block", "Repeating Command Block")));
        itemNames.add(new ItemName(Material.CHAIN_COMMAND_BLOCK, langConfig.getString("block.minecraft.chain_command_block", "Chain Command Block")));
        itemNames.add(new ItemName(Material.BEACON, langConfig.getString("block.minecraft.beacon", "Beacon")));
        itemNames.add(new ItemName(Material.COBBLESTONE_WALL, langConfig.getString("block.minecraft.cobblestone_wall", "Cobblestone Wall")));
        itemNames.add(new ItemName(Material.MOSSY_COBBLESTONE_WALL, langConfig.getString("block.minecraft.mossy_cobblestone_wall", "Mossy Cobblestone Wall")));
        itemNames.add(new ItemName(Material.CARROTS, langConfig.getString("block.minecraft.carrots", "Carrots")));
        itemNames.add(new ItemName(Material.POTATOES, langConfig.getString("block.minecraft.potatoes", "Potatoes")));
        itemNames.add(new ItemName(Material.DAYLIGHT_DETECTOR, langConfig.getString("block.minecraft.daylight_detector", "Daylight Detector")));
        itemNames.add(new ItemName(Material.NETHER_QUARTZ_ORE, langConfig.getString("block.minecraft.nether_quartz_ore", "Nether Quartz Ore")));
        itemNames.add(new ItemName(Material.HOPPER, langConfig.getString("block.minecraft.hopper", "Hopper")));
        itemNames.add(new ItemName(Material.QUARTZ_BLOCK, langConfig.getString("block.minecraft.quartz_block", "Block of Quartz")));
        itemNames.add(new ItemName(Material.CHISELED_QUARTZ_BLOCK, langConfig.getString("block.minecraft.chiseled_quartz_block", "Chiseled Quartz Block")));
        itemNames.add(new ItemName(Material.QUARTZ_PILLAR, langConfig.getString("block.minecraft.quartz_pillar", "Quartz Pillar")));
        itemNames.add(new ItemName(Material.QUARTZ_STAIRS, langConfig.getString("block.minecraft.quartz_stairs", "Quartz Stairs")));
        itemNames.add(new ItemName(Material.SLIME_BLOCK, langConfig.getString("block.minecraft.slime_block", "Slime Block")));
        itemNames.add(new ItemName(Material.PRISMARINE, langConfig.getString("block.minecraft.prismarine", "Prismarine")));
        itemNames.add(new ItemName(Material.PRISMARINE_BRICKS, langConfig.getString("block.minecraft.prismarine_bricks", "Prismarine Bricks")));
        itemNames.add(new ItemName(Material.DARK_PRISMARINE, langConfig.getString("block.minecraft.dark_prismarine", "Dark Prismarine")));
        itemNames.add(new ItemName(Material.SEA_LANTERN, langConfig.getString("block.minecraft.sea_lantern", "Sea Lantern")));
        itemNames.add(new ItemName(Material.END_ROD, langConfig.getString("block.minecraft.end_rod", "End Rod")));
        itemNames.add(new ItemName(Material.CHORUS_PLANT, langConfig.getString("block.minecraft.chorus_plant", "Chorus Plant")));
        itemNames.add(new ItemName(Material.CHORUS_FLOWER, langConfig.getString("block.minecraft.chorus_flower", "Chorus Flower")));
        itemNames.add(new ItemName(Material.PURPUR_BLOCK, langConfig.getString("block.minecraft.purpur_block", "Purpur Block")));
        itemNames.add(new ItemName(Material.PURPUR_PILLAR, langConfig.getString("block.minecraft.purpur_pillar", "Purpur Pillar")));
        itemNames.add(new ItemName(Material.PURPUR_STAIRS, langConfig.getString("block.minecraft.purpur_stairs", "Purpur Stairs")));
        itemNames.add(new ItemName(Material.PURPUR_SLAB, langConfig.getString("block.minecraft.purpur_slab", "Purpur Slab")));
        itemNames.add(new ItemName(Material.END_STONE_BRICKS, langConfig.getString("block.minecraft.end_stone_bricks", "End Stone Bricks")));
        itemNames.add(new ItemName(Material.BEETROOTS, langConfig.getString("block.minecraft.beetroots", "Beetroots")));
        itemNames.add(new ItemName(Material.GRASS_PATH, langConfig.getString("block.minecraft.grass_path", "Grass Path")));
        itemNames.add(new ItemName(Material.MAGMA_BLOCK, langConfig.getString("block.minecraft.magma_block", "Magma Block")));
        itemNames.add(new ItemName(Material.NETHER_WART_BLOCK, langConfig.getString("block.minecraft.nether_wart_block", "Nether Wart Block")));
        itemNames.add(new ItemName(Material.RED_NETHER_BRICKS, langConfig.getString("block.minecraft.red_nether_bricks", "Red Nether Bricks")));
        itemNames.add(new ItemName(Material.BONE_BLOCK, langConfig.getString("block.minecraft.bone_block", "Bone Block")));
        itemNames.add(new ItemName(Material.OBSERVER, langConfig.getString("block.minecraft.observer", "Observer")));
        itemNames.add(new ItemName(Material.SHULKER_BOX, langConfig.getString("block.minecraft.shulker_box", "Shulker Box")));
        itemNames.add(new ItemName(Material.WHITE_SHULKER_BOX, langConfig.getString("block.minecraft.white_shulker_box", "White Shulker Box")));
        itemNames.add(new ItemName(Material.ORANGE_SHULKER_BOX, langConfig.getString("block.minecraft.orange_shulker_box", "Orange Shulker Box")));
        itemNames.add(new ItemName(Material.MAGENTA_SHULKER_BOX, langConfig.getString("block.minecraft.magenta_shulker_box", "Magenta Shulker Box")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_SHULKER_BOX, langConfig.getString("block.minecraft.light_blue_shulker_box", "Light Blue Shulker Box")));
        itemNames.add(new ItemName(Material.YELLOW_SHULKER_BOX, langConfig.getString("block.minecraft.yellow_shulker_box", "Yellow Shulker Box")));
        itemNames.add(new ItemName(Material.LIME_SHULKER_BOX, langConfig.getString("block.minecraft.lime_shulker_box", "Lime Shulker Box")));
        itemNames.add(new ItemName(Material.PINK_SHULKER_BOX, langConfig.getString("block.minecraft.pink_shulker_box", "Pink Shulker Box")));
        itemNames.add(new ItemName(Material.GRAY_SHULKER_BOX, langConfig.getString("block.minecraft.gray_shulker_box", "Gray Shulker Box")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_SHULKER_BOX, langConfig.getString("block.minecraft.light_gray_shulker_box", "Light Gray Shulker Box")));
        itemNames.add(new ItemName(Material.CYAN_SHULKER_BOX, langConfig.getString("block.minecraft.cyan_shulker_box", "Cyan Shulker Box")));
        itemNames.add(new ItemName(Material.PURPLE_SHULKER_BOX, langConfig.getString("block.minecraft.purple_shulker_box", "Purple Shulker Box")));
        itemNames.add(new ItemName(Material.BLUE_SHULKER_BOX, langConfig.getString("block.minecraft.blue_shulker_box", "Blue Shulker Box")));
        itemNames.add(new ItemName(Material.BROWN_SHULKER_BOX, langConfig.getString("block.minecraft.brown_shulker_box", "Brown Shulker Box")));
        itemNames.add(new ItemName(Material.GREEN_SHULKER_BOX, langConfig.getString("block.minecraft.green_shulker_box", "Green Shulker Box")));
        itemNames.add(new ItemName(Material.RED_SHULKER_BOX, langConfig.getString("block.minecraft.red_shulker_box", "Red Shulker Box")));
        itemNames.add(new ItemName(Material.BLACK_SHULKER_BOX, langConfig.getString("block.minecraft.black_shulker_box", "Black Shulker Box")));
        itemNames.add(new ItemName(Material.WHITE_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.white_glazed_terracotta", "White Glazed Terracotta")));
        itemNames.add(new ItemName(Material.ORANGE_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.orange_glazed_terracotta", "Orange Glazed Terracotta")));
        itemNames.add(new ItemName(Material.MAGENTA_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.magenta_glazed_terracotta", "Magenta Glazed Terracotta")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.light_blue_glazed_terracotta", "Light Blue Glazed Terracotta")));
        itemNames.add(new ItemName(Material.YELLOW_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.yellow_glazed_terracotta", "Yellow Glazed Terracotta")));
        itemNames.add(new ItemName(Material.LIME_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.lime_glazed_terracotta", "Lime Glazed Terracotta")));
        itemNames.add(new ItemName(Material.PINK_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.pink_glazed_terracotta", "Pink Glazed Terracotta")));
        itemNames.add(new ItemName(Material.GRAY_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.gray_glazed_terracotta", "Gray Glazed Terracotta")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.light_gray_glazed_terracotta", "Light Gray Glazed Terracotta")));
        itemNames.add(new ItemName(Material.CYAN_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.cyan_glazed_terracotta", "Cyan Glazed Terracotta")));
        itemNames.add(new ItemName(Material.PURPLE_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.purple_glazed_terracotta", "Purple Glazed Terracotta")));
        itemNames.add(new ItemName(Material.BLUE_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.blue_glazed_terracotta", "Blue Glazed Terracotta")));
        itemNames.add(new ItemName(Material.BROWN_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.brown_glazed_terracotta", "Brown Glazed Terracotta")));
        itemNames.add(new ItemName(Material.GREEN_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.green_glazed_terracotta", "Green Glazed Terracotta")));
        itemNames.add(new ItemName(Material.RED_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.red_glazed_terracotta", "Red Glazed Terracotta")));
        itemNames.add(new ItemName(Material.BLACK_GLAZED_TERRACOTTA, langConfig.getString("block.minecraft.black_glazed_terracotta", "Black Glazed Terracotta")));
        itemNames.add(new ItemName(Material.BLACK_CONCRETE, langConfig.getString("block.minecraft.black_concrete", "Black Concrete")));
        itemNames.add(new ItemName(Material.RED_CONCRETE, langConfig.getString("block.minecraft.red_concrete", "Red Concrete")));
        itemNames.add(new ItemName(Material.GREEN_CONCRETE, langConfig.getString("block.minecraft.green_concrete", "Green Concrete")));
        itemNames.add(new ItemName(Material.BROWN_CONCRETE, langConfig.getString("block.minecraft.brown_concrete", "Brown Concrete")));
        itemNames.add(new ItemName(Material.BLUE_CONCRETE, langConfig.getString("block.minecraft.blue_concrete", "Blue Concrete")));
        itemNames.add(new ItemName(Material.PURPLE_CONCRETE, langConfig.getString("block.minecraft.purple_concrete", "Purple Concrete")));
        itemNames.add(new ItemName(Material.CYAN_CONCRETE, langConfig.getString("block.minecraft.cyan_concrete", "Cyan Concrete")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_CONCRETE, langConfig.getString("block.minecraft.light_gray_concrete", "Light Gray Concrete")));
        itemNames.add(new ItemName(Material.GRAY_CONCRETE, langConfig.getString("block.minecraft.gray_concrete", "Gray Concrete")));
        itemNames.add(new ItemName(Material.PINK_CONCRETE, langConfig.getString("block.minecraft.pink_concrete", "Pink Concrete")));
        itemNames.add(new ItemName(Material.LIME_CONCRETE, langConfig.getString("block.minecraft.lime_concrete", "Lime Concrete")));
        itemNames.add(new ItemName(Material.YELLOW_CONCRETE, langConfig.getString("block.minecraft.yellow_concrete", "Yellow Concrete")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_CONCRETE, langConfig.getString("block.minecraft.light_blue_concrete", "Light Blue Concrete")));
        itemNames.add(new ItemName(Material.MAGENTA_CONCRETE, langConfig.getString("block.minecraft.magenta_concrete", "Magenta Concrete")));
        itemNames.add(new ItemName(Material.ORANGE_CONCRETE, langConfig.getString("block.minecraft.orange_concrete", "Orange Concrete")));
        itemNames.add(new ItemName(Material.WHITE_CONCRETE, langConfig.getString("block.minecraft.white_concrete", "White Concrete")));
        itemNames.add(new ItemName(Material.BLACK_CONCRETE_POWDER, langConfig.getString("block.minecraft.black_concrete_powder", "Black Concrete Powder")));
        itemNames.add(new ItemName(Material.RED_CONCRETE_POWDER, langConfig.getString("block.minecraft.red_concrete_powder", "Red Concrete Powder")));
        itemNames.add(new ItemName(Material.GREEN_CONCRETE_POWDER, langConfig.getString("block.minecraft.green_concrete_powder", "Green Concrete Powder")));
        itemNames.add(new ItemName(Material.BROWN_CONCRETE_POWDER, langConfig.getString("block.minecraft.brown_concrete_powder", "Brown Concrete Powder")));
        itemNames.add(new ItemName(Material.BLUE_CONCRETE_POWDER, langConfig.getString("block.minecraft.blue_concrete_powder", "Blue Concrete Powder")));
        itemNames.add(new ItemName(Material.PURPLE_CONCRETE_POWDER, langConfig.getString("block.minecraft.purple_concrete_powder", "Purple Concrete Powder")));
        itemNames.add(new ItemName(Material.CYAN_CONCRETE_POWDER, langConfig.getString("block.minecraft.cyan_concrete_powder", "Cyan Concrete Powder")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_CONCRETE_POWDER, langConfig.getString("block.minecraft.light_gray_concrete_powder", "Light Gray Concrete Powder")));
        itemNames.add(new ItemName(Material.GRAY_CONCRETE_POWDER, langConfig.getString("block.minecraft.gray_concrete_powder", "Gray Concrete Powder")));
        itemNames.add(new ItemName(Material.PINK_CONCRETE_POWDER, langConfig.getString("block.minecraft.pink_concrete_powder", "Pink Concrete Powder")));
        itemNames.add(new ItemName(Material.LIME_CONCRETE_POWDER, langConfig.getString("block.minecraft.lime_concrete_powder", "Lime Concrete Powder")));
        itemNames.add(new ItemName(Material.YELLOW_CONCRETE_POWDER, langConfig.getString("block.minecraft.yellow_concrete_powder", "Yellow Concrete Powder")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_CONCRETE_POWDER, langConfig.getString("block.minecraft.light_blue_concrete_powder", "Light Blue Concrete Powder")));
        itemNames.add(new ItemName(Material.MAGENTA_CONCRETE_POWDER, langConfig.getString("block.minecraft.magenta_concrete_powder", "Magenta Concrete Powder")));
        itemNames.add(new ItemName(Material.ORANGE_CONCRETE_POWDER, langConfig.getString("block.minecraft.orange_concrete_powder", "Orange Concrete Powder")));
        itemNames.add(new ItemName(Material.WHITE_CONCRETE_POWDER, langConfig.getString("block.minecraft.white_concrete_powder", "White Concrete Powder")));
        itemNames.add(new ItemName(Material.TURTLE_EGG, langConfig.getString("block.minecraft.turtle_egg", "Turtle Egg")));
        itemNames.add(new ItemName(Material.PISTON_HEAD, langConfig.getString("block.minecraft.piston_head", "Piston Head")));
        itemNames.add(new ItemName(Material.MOVING_PISTON, langConfig.getString("block.minecraft.moving_piston", "Moving Piston")));
        itemNames.add(new ItemName(Material.RED_MUSHROOM, langConfig.getString("block.minecraft.red_mushroom", "Red Mushroom")));
        itemNames.add(new ItemName(Material.SNOW_BLOCK, langConfig.getString("block.minecraft.snow_block", "Snow Block")));
        itemNames.add(new ItemName(Material.ATTACHED_MELON_STEM, langConfig.getString("block.minecraft.attached_melon_stem", "Attached Melon Stem")));
        itemNames.add(new ItemName(Material.MELON_STEM, langConfig.getString("block.minecraft.melon_stem", "Melon Stem")));
        itemNames.add(new ItemName(Material.BREWING_STAND, langConfig.getString("block.minecraft.brewing_stand", "Brewing Stand")));
        itemNames.add(new ItemName(Material.END_PORTAL, langConfig.getString("block.minecraft.end_portal", "End Portal")));
        itemNames.add(new ItemName(Material.FLOWER_POT, langConfig.getString("block.minecraft.flower_pot", "Flower Pot")));
        itemNames.add(new ItemName(Material.POTTED_OAK_SAPLING, langConfig.getString("block.minecraft.potted_oak_sapling", "Potted Oak Sapling")));
        itemNames.add(new ItemName(Material.POTTED_SPRUCE_SAPLING, langConfig.getString("block.minecraft.potted_spruce_sapling", "Potted Spruce Sapling")));
        itemNames.add(new ItemName(Material.POTTED_BIRCH_SAPLING, langConfig.getString("block.minecraft.potted_birch_sapling", "Potted Birch Sapling")));
        itemNames.add(new ItemName(Material.POTTED_JUNGLE_SAPLING, langConfig.getString("block.minecraft.potted_jungle_sapling", "Potted Jungle Sapling")));
        itemNames.add(new ItemName(Material.POTTED_ACACIA_SAPLING, langConfig.getString("block.minecraft.potted_acacia_sapling", "Potted Acacia Sapling")));
        itemNames.add(new ItemName(Material.POTTED_DARK_OAK_SAPLING, langConfig.getString("block.minecraft.potted_dark_oak_sapling", "Potted Dark Oak Sapling")));
        itemNames.add(new ItemName(Material.POTTED_FERN, langConfig.getString("block.minecraft.potted_fern", "Potted Fern")));
        itemNames.add(new ItemName(Material.POTTED_DANDELION, langConfig.getString("block.minecraft.potted_dandelion", "Potted Dandelion")));
        itemNames.add(new ItemName(Material.POTTED_POPPY, langConfig.getString("block.minecraft.potted_poppy", "Potted Poppy")));
        itemNames.add(new ItemName(Material.POTTED_BLUE_ORCHID, langConfig.getString("block.minecraft.potted_blue_orchid", "Potted Blue Orchid")));
        itemNames.add(new ItemName(Material.POTTED_ALLIUM, langConfig.getString("block.minecraft.potted_allium", "Potted Allium")));
        itemNames.add(new ItemName(Material.POTTED_AZURE_BLUET, langConfig.getString("block.minecraft.potted_azure_bluet", "Potted Azure Bluet")));
        itemNames.add(new ItemName(Material.POTTED_RED_TULIP, langConfig.getString("block.minecraft.potted_red_tulip", "Potted Red Tulip")));
        itemNames.add(new ItemName(Material.POTTED_ORANGE_TULIP, langConfig.getString("block.minecraft.potted_orange_tulip", "Potted Orange Tulip")));
        itemNames.add(new ItemName(Material.POTTED_WHITE_TULIP, langConfig.getString("block.minecraft.potted_white_tulip", "Potted White Tulip")));
        itemNames.add(new ItemName(Material.POTTED_PINK_TULIP, langConfig.getString("block.minecraft.potted_pink_tulip", "Potted Pink Tulip")));
        itemNames.add(new ItemName(Material.POTTED_OXEYE_DAISY, langConfig.getString("block.minecraft.potted_oxeye_daisy", "Potted Oxeye Daisy")));
        itemNames.add(new ItemName(Material.POTTED_RED_MUSHROOM, langConfig.getString("block.minecraft.potted_red_mushroom", "Potted Red Mushroom")));
        itemNames.add(new ItemName(Material.POTTED_BROWN_MUSHROOM, langConfig.getString("block.minecraft.potted_brown_mushroom", "Potted Brown Mushroom")));
        itemNames.add(new ItemName(Material.POTTED_DEAD_BUSH, langConfig.getString("block.minecraft.potted_dead_bush", "Potted Dead Bush")));
        itemNames.add(new ItemName(Material.POTTED_CACTUS, langConfig.getString("block.minecraft.potted_cactus", "Potted Cactus")));
        itemNames.add(new ItemName(Material.SKELETON_WALL_SKULL, langConfig.getString("block.minecraft.skeleton_wall_skull", "Skeleton Wall Skull")));
        itemNames.add(new ItemName(Material.SKELETON_SKULL, langConfig.getString("block.minecraft.skeleton_skull", "Skeleton Skull")));
        itemNames.add(new ItemName(Material.WITHER_SKELETON_WALL_SKULL, langConfig.getString("block.minecraft.wither_skeleton_wall_skull", "Wither Skeleton Wall Skull")));
        itemNames.add(new ItemName(Material.WITHER_SKELETON_SKULL, langConfig.getString("block.minecraft.wither_skeleton_skull", "Wither Skeleton Skull")));
        itemNames.add(new ItemName(Material.ZOMBIE_WALL_HEAD, langConfig.getString("block.minecraft.zombie_wall_head", "Zombie Wall Head")));
        itemNames.add(new ItemName(Material.ZOMBIE_HEAD, langConfig.getString("block.minecraft.zombie_head", "Zombie Head")));
        itemNames.add(new ItemName(Material.PLAYER_WALL_HEAD, langConfig.getString("block.minecraft.player_wall_head", "Player Wall Head")));
        itemNames.add(new ItemName(Material.PLAYER_HEAD, langConfig.getString("block.minecraft.player_head", "Player Head")));
        itemNames.add(new ItemName(Material.CREEPER_WALL_HEAD, langConfig.getString("block.minecraft.creeper_wall_head", "Creeper Wall Head")));
        itemNames.add(new ItemName(Material.CREEPER_HEAD, langConfig.getString("block.minecraft.creeper_head", "Creeper Head")));
        itemNames.add(new ItemName(Material.DRAGON_WALL_HEAD, langConfig.getString("block.minecraft.dragon_wall_head", "Dragon Wall Head")));
        itemNames.add(new ItemName(Material.DRAGON_HEAD, langConfig.getString("block.minecraft.dragon_head", "Dragon Head")));
        itemNames.add(new ItemName(Material.END_GATEWAY, langConfig.getString("block.minecraft.end_gateway", "End Gateway")));
        itemNames.add(new ItemName(Material.STRUCTURE_VOID, langConfig.getString("block.minecraft.structure_void", "Structure Void")));
        itemNames.add(new ItemName(Material.STRUCTURE_BLOCK, langConfig.getString("block.minecraft.structure_block", "Structure Block")));
        itemNames.add(new ItemName(Material.VOID_AIR, langConfig.getString("block.minecraft.void_air", "Void Air")));
        itemNames.add(new ItemName(Material.CAVE_AIR, langConfig.getString("block.minecraft.cave_air", "Cave Air")));
        itemNames.add(new ItemName(Material.BUBBLE_COLUMN, langConfig.getString("block.minecraft.bubble_column", "Bubble Column")));
        itemNames.add(new ItemName(Material.DEAD_TUBE_CORAL_BLOCK, langConfig.getString("block.minecraft.dead_tube_coral_block", "Dead Tube Coral Block")));
        itemNames.add(new ItemName(Material.DEAD_BRAIN_CORAL_BLOCK, langConfig.getString("block.minecraft.dead_brain_coral_block", "Dead Brain Coral Block")));
        itemNames.add(new ItemName(Material.DEAD_BUBBLE_CORAL_BLOCK, langConfig.getString("block.minecraft.dead_bubble_coral_block", "Dead Bubble Coral Block")));
        itemNames.add(new ItemName(Material.DEAD_FIRE_CORAL_BLOCK, langConfig.getString("block.minecraft.dead_fire_coral_block", "Dead Fire Coral Block")));
        itemNames.add(new ItemName(Material.DEAD_HORN_CORAL_BLOCK, langConfig.getString("block.minecraft.dead_horn_coral_block", "Dead Horn Coral Block")));
        itemNames.add(new ItemName(Material.TUBE_CORAL_BLOCK, langConfig.getString("block.minecraft.tube_coral_block", "Tube Coral Block")));
        itemNames.add(new ItemName(Material.BRAIN_CORAL_BLOCK, langConfig.getString("block.minecraft.brain_coral_block", "Brain Coral Block")));
        itemNames.add(new ItemName(Material.BUBBLE_CORAL_BLOCK, langConfig.getString("block.minecraft.bubble_coral_block", "Bubble Coral Block")));
        itemNames.add(new ItemName(Material.FIRE_CORAL_BLOCK, langConfig.getString("block.minecraft.fire_coral_block", "Fire Coral Block")));
        itemNames.add(new ItemName(Material.HORN_CORAL_BLOCK, langConfig.getString("block.minecraft.horn_coral_block", "Horn Coral Block")));
        itemNames.add(new ItemName(Material.TUBE_CORAL, langConfig.getString("block.minecraft.tube_coral", "Tube Coral")));
        itemNames.add(new ItemName(Material.BRAIN_CORAL, langConfig.getString("block.minecraft.brain_coral", "Brain Coral")));
        itemNames.add(new ItemName(Material.BUBBLE_CORAL, langConfig.getString("block.minecraft.bubble_coral", "Bubble Coral")));
        itemNames.add(new ItemName(Material.FIRE_CORAL, langConfig.getString("block.minecraft.fire_coral", "Fire Coral")));
        itemNames.add(new ItemName(Material.HORN_CORAL, langConfig.getString("block.minecraft.horn_coral", "Horn Coral")));
        itemNames.add(new ItemName(Material.TUBE_CORAL_FAN, langConfig.getString("block.minecraft.tube_coral_fan", "Tube Coral Fan")));
        itemNames.add(new ItemName(Material.BRAIN_CORAL_FAN, langConfig.getString("block.minecraft.brain_coral_fan", "Brain Coral Fan")));
        itemNames.add(new ItemName(Material.BUBBLE_CORAL_FAN, langConfig.getString("block.minecraft.bubble_coral_fan", "Bubble Coral Fan")));
        itemNames.add(new ItemName(Material.FIRE_CORAL_FAN, langConfig.getString("block.minecraft.fire_coral_fan", "Fire Coral Fan")));
        itemNames.add(new ItemName(Material.HORN_CORAL_FAN, langConfig.getString("block.minecraft.horn_coral_fan", "Horn Coral Fan")));
        itemNames.add(new ItemName(Material.CONDUIT, langConfig.getString("block.minecraft.conduit", "Conduit")));
        itemNames.add(new ItemName(Material.NAME_TAG, langConfig.getString("item.minecraft.name_tag", "Name Tag")));
        itemNames.add(new ItemName(Material.LEAD, langConfig.getString("item.minecraft.lead", "Lead")));
        itemNames.add(new ItemName(Material.IRON_SHOVEL, langConfig.getString("item.minecraft.iron_shovel", "Iron Shovel")));
        itemNames.add(new ItemName(Material.IRON_PICKAXE, langConfig.getString("item.minecraft.iron_pickaxe", "Iron Pickaxe")));
        itemNames.add(new ItemName(Material.IRON_AXE, langConfig.getString("item.minecraft.iron_axe", "Iron Axe")));
        itemNames.add(new ItemName(Material.FLINT_AND_STEEL, langConfig.getString("item.minecraft.flint_and_steel", "Flint and Steel")));
        itemNames.add(new ItemName(Material.APPLE, langConfig.getString("item.minecraft.apple", "Apple")));
        itemNames.add(new ItemName(Material.COOKIE, langConfig.getString("item.minecraft.cookie", "Cookie")));
        itemNames.add(new ItemName(Material.BOW, langConfig.getString("item.minecraft.bow", "Bow")));
        itemNames.add(new ItemName(Material.ARROW, langConfig.getString("item.minecraft.arrow", "Arrow")));
        itemNames.add(new ItemName(Material.SPECTRAL_ARROW, langConfig.getString("item.minecraft.spectral_arrow", "Spectral Arrow")));
        itemNames.add(new ItemName(Material.TIPPED_ARROW, langConfig.getString("item.minecraft.tipped_arrow", "Tipped Arrow")));
        itemNames.add(new ItemName(Material.DRIED_KELP, langConfig.getString("item.minecraft.dried_kelp", "Dried Kelp")));
        itemNames.add(new ItemName(Material.COAL, langConfig.getString("item.minecraft.coal", "Coal")));
        itemNames.add(new ItemName(Material.CHARCOAL, langConfig.getString("item.minecraft.charcoal", "Charcoal")));
        itemNames.add(new ItemName(Material.DIAMOND, langConfig.getString("item.minecraft.diamond", "Diamond")));
        itemNames.add(new ItemName(Material.EMERALD, langConfig.getString("item.minecraft.emerald", "Emerald")));
        itemNames.add(new ItemName(Material.IRON_INGOT, langConfig.getString("item.minecraft.iron_ingot", "Iron Ingot")));
        itemNames.add(new ItemName(Material.GOLD_INGOT, langConfig.getString("item.minecraft.gold_ingot", "Gold Ingot")));
        itemNames.add(new ItemName(Material.IRON_SWORD, langConfig.getString("item.minecraft.iron_sword", "Iron Sword")));
        itemNames.add(new ItemName(Material.WOODEN_SWORD, langConfig.getString("item.minecraft.wooden_sword", "Wooden Sword")));
        itemNames.add(new ItemName(Material.WOODEN_SHOVEL, langConfig.getString("item.minecraft.wooden_shovel", "Wooden Shovel")));
        itemNames.add(new ItemName(Material.WOODEN_PICKAXE, langConfig.getString("item.minecraft.wooden_pickaxe", "Wooden Pickaxe")));
        itemNames.add(new ItemName(Material.WOODEN_AXE, langConfig.getString("item.minecraft.wooden_axe", "Wooden Axe")));
        itemNames.add(new ItemName(Material.STONE_SWORD, langConfig.getString("item.minecraft.stone_sword", "Stone Sword")));
        itemNames.add(new ItemName(Material.STONE_SHOVEL, langConfig.getString("item.minecraft.stone_shovel", "Stone Shovel")));
        itemNames.add(new ItemName(Material.STONE_PICKAXE, langConfig.getString("item.minecraft.stone_pickaxe", "Stone Pickaxe")));
        itemNames.add(new ItemName(Material.STONE_AXE, langConfig.getString("item.minecraft.stone_axe", "Stone Axe")));
        itemNames.add(new ItemName(Material.DIAMOND_SWORD, langConfig.getString("item.minecraft.diamond_sword", "Diamond Sword")));
        itemNames.add(new ItemName(Material.DIAMOND_SHOVEL, langConfig.getString("item.minecraft.diamond_shovel", "Diamond Shovel")));
        itemNames.add(new ItemName(Material.DIAMOND_PICKAXE, langConfig.getString("item.minecraft.diamond_pickaxe", "Diamond Pickaxe")));
        itemNames.add(new ItemName(Material.DIAMOND_AXE, langConfig.getString("item.minecraft.diamond_axe", "Diamond Axe")));
        itemNames.add(new ItemName(Material.STICK, langConfig.getString("item.minecraft.stick", "Stick")));
        itemNames.add(new ItemName(Material.BOWL, langConfig.getString("item.minecraft.bowl", "Bowl")));
        itemNames.add(new ItemName(Material.MUSHROOM_STEW, langConfig.getString("item.minecraft.mushroom_stew", "Mushroom Stew")));
        itemNames.add(new ItemName(Material.GOLDEN_SWORD, langConfig.getString("item.minecraft.golden_sword", "Golden Sword")));
        itemNames.add(new ItemName(Material.GOLDEN_SHOVEL, langConfig.getString("item.minecraft.golden_shovel", "Golden Shovel")));
        itemNames.add(new ItemName(Material.GOLDEN_PICKAXE, langConfig.getString("item.minecraft.golden_pickaxe", "Golden Pickaxe")));
        itemNames.add(new ItemName(Material.GOLDEN_AXE, langConfig.getString("item.minecraft.golden_axe", "Golden Axe")));
        itemNames.add(new ItemName(Material.STRING, langConfig.getString("item.minecraft.string", "String")));
        itemNames.add(new ItemName(Material.FEATHER, langConfig.getString("item.minecraft.feather", "Feather")));
        itemNames.add(new ItemName(Material.GUNPOWDER, langConfig.getString("item.minecraft.gunpowder", "Gunpowder")));
        itemNames.add(new ItemName(Material.WOODEN_HOE, langConfig.getString("item.minecraft.wooden_hoe", "Wooden Hoe")));
        itemNames.add(new ItemName(Material.STONE_HOE, langConfig.getString("item.minecraft.stone_hoe", "Stone Hoe")));
        itemNames.add(new ItemName(Material.IRON_HOE, langConfig.getString("item.minecraft.iron_hoe", "Iron Hoe")));
        itemNames.add(new ItemName(Material.DIAMOND_HOE, langConfig.getString("item.minecraft.diamond_hoe", "Diamond Hoe")));
        itemNames.add(new ItemName(Material.GOLDEN_HOE, langConfig.getString("item.minecraft.golden_hoe", "Golden Hoe")));
        itemNames.add(new ItemName(Material.WHEAT_SEEDS, langConfig.getString("item.minecraft.wheat_seeds", "Wheat Seeds")));
        itemNames.add(new ItemName(Material.PUMPKIN_SEEDS, langConfig.getString("item.minecraft.pumpkin_seeds", "Pumpkin Seeds")));
        itemNames.add(new ItemName(Material.MELON_SEEDS, langConfig.getString("item.minecraft.melon_seeds", "Melon Seeds")));
        itemNames.add(new ItemName(Material.MELON_SLICE, langConfig.getString("item.minecraft.melon_slice", "Melon Slice")));
        itemNames.add(new ItemName(Material.WHEAT, langConfig.getString("item.minecraft.wheat", "Wheat")));
        itemNames.add(new ItemName(Material.BREAD, langConfig.getString("item.minecraft.bread", "Bread")));
        itemNames.add(new ItemName(Material.LEATHER_HELMET, langConfig.getString("item.minecraft.leather_helmet", "Leather Cap")));
        itemNames.add(new ItemName(Material.LEATHER_CHESTPLATE, langConfig.getString("item.minecraft.leather_chestplate", "Leather Tunic")));
        itemNames.add(new ItemName(Material.LEATHER_LEGGINGS, langConfig.getString("item.minecraft.leather_leggings", "Leather Pants")));
        itemNames.add(new ItemName(Material.LEATHER_BOOTS, langConfig.getString("item.minecraft.leather_boots", "Leather Boots")));
        itemNames.add(new ItemName(Material.CHAINMAIL_HELMET, langConfig.getString("item.minecraft.chainmail_helmet", "Chainmail Helmet")));
        itemNames.add(new ItemName(Material.CHAINMAIL_CHESTPLATE, langConfig.getString("item.minecraft.chainmail_chestplate", "Chainmail Chestplate")));
        itemNames.add(new ItemName(Material.CHAINMAIL_LEGGINGS, langConfig.getString("item.minecraft.chainmail_leggings", "Chainmail Leggings")));
        itemNames.add(new ItemName(Material.CHAINMAIL_BOOTS, langConfig.getString("item.minecraft.chainmail_boots", "Chainmail Boots")));
        itemNames.add(new ItemName(Material.IRON_HELMET, langConfig.getString("item.minecraft.iron_helmet", "Iron Helmet")));
        itemNames.add(new ItemName(Material.IRON_CHESTPLATE, langConfig.getString("item.minecraft.iron_chestplate", "Iron Chestplate")));
        itemNames.add(new ItemName(Material.IRON_LEGGINGS, langConfig.getString("item.minecraft.iron_leggings", "Iron Leggings")));
        itemNames.add(new ItemName(Material.IRON_BOOTS, langConfig.getString("item.minecraft.iron_boots", "Iron Boots")));
        itemNames.add(new ItemName(Material.DIAMOND_HELMET, langConfig.getString("item.minecraft.diamond_helmet", "Diamond Helmet")));
        itemNames.add(new ItemName(Material.DIAMOND_CHESTPLATE, langConfig.getString("item.minecraft.diamond_chestplate", "Diamond Chestplate")));
        itemNames.add(new ItemName(Material.DIAMOND_LEGGINGS, langConfig.getString("item.minecraft.diamond_leggings", "Diamond Leggings")));
        itemNames.add(new ItemName(Material.DIAMOND_BOOTS, langConfig.getString("item.minecraft.diamond_boots", "Diamond Boots")));
        itemNames.add(new ItemName(Material.GOLDEN_HELMET, langConfig.getString("item.minecraft.golden_helmet", "Golden Helmet")));
        itemNames.add(new ItemName(Material.GOLDEN_CHESTPLATE, langConfig.getString("item.minecraft.golden_chestplate", "Golden Chestplate")));
        itemNames.add(new ItemName(Material.GOLDEN_LEGGINGS, langConfig.getString("item.minecraft.golden_leggings", "Golden Leggings")));
        itemNames.add(new ItemName(Material.GOLDEN_BOOTS, langConfig.getString("item.minecraft.golden_boots", "Golden Boots")));
        itemNames.add(new ItemName(Material.FLINT, langConfig.getString("item.minecraft.flint", "Flint")));
        itemNames.add(new ItemName(Material.PORKCHOP, langConfig.getString("item.minecraft.porkchop", "Raw Porkchop")));
        itemNames.add(new ItemName(Material.COOKED_PORKCHOP, langConfig.getString("item.minecraft.cooked_porkchop", "Cooked Porkchop")));
        itemNames.add(new ItemName(Material.CHICKEN, langConfig.getString("item.minecraft.chicken", "Raw Chicken")));
        itemNames.add(new ItemName(Material.COOKED_CHICKEN, langConfig.getString("item.minecraft.cooked_chicken", "Cooked Chicken")));
        itemNames.add(new ItemName(Material.MUTTON, langConfig.getString("item.minecraft.mutton", "Raw Mutton")));
        itemNames.add(new ItemName(Material.COOKED_MUTTON, langConfig.getString("item.minecraft.cooked_mutton", "Cooked Mutton")));
        itemNames.add(new ItemName(Material.RABBIT, langConfig.getString("item.minecraft.rabbit", "Raw Rabbit")));
        itemNames.add(new ItemName(Material.COOKED_RABBIT, langConfig.getString("item.minecraft.cooked_rabbit", "Cooked Rabbit")));
        itemNames.add(new ItemName(Material.RABBIT_STEW, langConfig.getString("item.minecraft.rabbit_stew", "Rabbit Stew")));
        itemNames.add(new ItemName(Material.RABBIT_FOOT, langConfig.getString("item.minecraft.rabbit_foot", "Rabbit's Foot")));
        itemNames.add(new ItemName(Material.RABBIT_HIDE, langConfig.getString("item.minecraft.rabbit_hide", "Rabbit Hide")));
        itemNames.add(new ItemName(Material.BEEF, langConfig.getString("item.minecraft.beef", "Raw Beef")));
        itemNames.add(new ItemName(Material.COOKED_BEEF, langConfig.getString("item.minecraft.cooked_beef", "Steak")));
        itemNames.add(new ItemName(Material.PAINTING, langConfig.getString("item.minecraft.painting", "Painting")));
        itemNames.add(new ItemName(Material.ITEM_FRAME, langConfig.getString("item.minecraft.item_frame", "Item Frame")));
        itemNames.add(new ItemName(Material.GOLDEN_APPLE, langConfig.getString("item.minecraft.golden_apple", "Golden Apple")));
        itemNames.add(new ItemName(Material.ENCHANTED_GOLDEN_APPLE, langConfig.getString("item.minecraft.enchanted_golden_apple", "Enchanted Golden Apple")));
        itemNames.add(new ItemName(Material.SIGN, langConfig.getString("item.minecraft.sign", "Sign")));
        itemNames.add(new ItemName(Material.BUCKET, langConfig.getString("item.minecraft.bucket", "Bucket")));
        itemNames.add(new ItemName(Material.WATER_BUCKET, langConfig.getString("item.minecraft.water_bucket", "Water Bucket")));
        itemNames.add(new ItemName(Material.LAVA_BUCKET, langConfig.getString("item.minecraft.lava_bucket", "Lava Bucket")));
        itemNames.add(new ItemName(Material.PUFFERFISH_BUCKET, langConfig.getString("item.minecraft.pufferfish_bucket", "Bucket of Pufferfish")));
        itemNames.add(new ItemName(Material.SALMON_BUCKET, langConfig.getString("item.minecraft.salmon_bucket", "Bucket of Salmon")));
        itemNames.add(new ItemName(Material.COD_BUCKET, langConfig.getString("item.minecraft.cod_bucket", "Bucket of Cod")));
        itemNames.add(new ItemName(Material.TROPICAL_FISH_BUCKET, langConfig.getString("item.minecraft.tropical_fish_bucket", "Bucket of Tropical Fish")));
        itemNames.add(new ItemName(Material.MINECART, langConfig.getString("item.minecraft.minecart", "Minecart")));
        itemNames.add(new ItemName(Material.SADDLE, langConfig.getString("item.minecraft.saddle", "Saddle")));
        itemNames.add(new ItemName(Material.REDSTONE, langConfig.getString("item.minecraft.redstone", "Redstone")));
        itemNames.add(new ItemName(Material.SNOWBALL, langConfig.getString("item.minecraft.snowball", "Snowball")));
        itemNames.add(new ItemName(Material.OAK_BOAT, langConfig.getString("item.minecraft.oak_boat", "Oak Boat")));
        itemNames.add(new ItemName(Material.SPRUCE_BOAT, langConfig.getString("item.minecraft.spruce_boat", "Spruce Boat")));
        itemNames.add(new ItemName(Material.BIRCH_BOAT, langConfig.getString("item.minecraft.birch_boat", "Birch Boat")));
        itemNames.add(new ItemName(Material.JUNGLE_BOAT, langConfig.getString("item.minecraft.jungle_boat", "Jungle Boat")));
        itemNames.add(new ItemName(Material.ACACIA_BOAT, langConfig.getString("item.minecraft.acacia_boat", "Acacia Boat")));
        itemNames.add(new ItemName(Material.DARK_OAK_BOAT, langConfig.getString("item.minecraft.dark_oak_boat", "Dark Oak Boat")));
        itemNames.add(new ItemName(Material.LEATHER, langConfig.getString("item.minecraft.leather", "Leather")));
        itemNames.add(new ItemName(Material.MILK_BUCKET, langConfig.getString("item.minecraft.milk_bucket", "Milk Bucket")));
        itemNames.add(new ItemName(Material.BRICK, langConfig.getString("item.minecraft.brick", "Brick")));
        itemNames.add(new ItemName(Material.CLAY, langConfig.getString("item.minecraft.clay_ball", "Clay")));
        itemNames.add(new ItemName(Material.PAPER, langConfig.getString("item.minecraft.paper", "Paper")));
        itemNames.add(new ItemName(Material.BOOK, langConfig.getString("item.minecraft.book", "Book")));
        itemNames.add(new ItemName(Material.SLIME_BALL, langConfig.getString("item.minecraft.slime_ball", "Slimeball")));
        itemNames.add(new ItemName(Material.CHEST_MINECART, langConfig.getString("item.minecraft.chest_minecart", "Minecart with Chest")));
        itemNames.add(new ItemName(Material.FURNACE_MINECART, langConfig.getString("item.minecraft.furnace_minecart", "Minecart with Furnace")));
        itemNames.add(new ItemName(Material.TNT_MINECART, langConfig.getString("item.minecraft.tnt_minecart", "Minecart with TNT")));
        itemNames.add(new ItemName(Material.HOPPER_MINECART, langConfig.getString("item.minecraft.hopper_minecart", "Minecart with Hopper")));
        itemNames.add(new ItemName(Material.COMMAND_BLOCK_MINECART, langConfig.getString("item.minecraft.command_block_minecart", "Minecart with Command Block")));
        itemNames.add(new ItemName(Material.EGG, langConfig.getString("item.minecraft.egg", "Egg")));
        itemNames.add(new ItemName(Material.COMPASS, langConfig.getString("item.minecraft.compass", "Compass")));
        itemNames.add(new ItemName(Material.FISHING_ROD, langConfig.getString("item.minecraft.fishing_rod", "Fishing Rod")));
        itemNames.add(new ItemName(Material.CLOCK, langConfig.getString("item.minecraft.clock", "Clock")));
        itemNames.add(new ItemName(Material.GLOWSTONE_DUST, langConfig.getString("item.minecraft.glowstone_dust", "Glowstone Dust")));
        itemNames.add(new ItemName(Material.COD, langConfig.getString("item.minecraft.cod", "Raw Cod")));
        itemNames.add(new ItemName(Material.SALMON, langConfig.getString("item.minecraft.salmon", "Raw Salmon")));
        itemNames.add(new ItemName(Material.PUFFERFISH, langConfig.getString("item.minecraft.pufferfish", "Pufferfish")));
        itemNames.add(new ItemName(Material.TROPICAL_FISH, langConfig.getString("item.minecraft.tropical_fish", "Tropical Fish")));
        itemNames.add(new ItemName(Material.COOKED_COD, langConfig.getString("item.minecraft.cooked_cod", "Cooked Cod")));
        itemNames.add(new ItemName(Material.COOKED_SALMON, langConfig.getString("item.minecraft.cooked_salmon", "Cooked Salmon")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_13, langConfig.getString("item.minecraft.music_disc_13", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_CAT, langConfig.getString("item.minecraft.music_disc_cat", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_BLOCKS, langConfig.getString("item.minecraft.music_disc_blocks", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_CHIRP, langConfig.getString("item.minecraft.music_disc_chirp", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_FAR, langConfig.getString("item.minecraft.music_disc_far", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_MALL, langConfig.getString("item.minecraft.music_disc_mall", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_MELLOHI, langConfig.getString("item.minecraft.music_disc_mellohi", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_STAL, langConfig.getString("item.minecraft.music_disc_stal", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_STRAD, langConfig.getString("item.minecraft.music_disc_strad", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_WARD, langConfig.getString("item.minecraft.music_disc_ward", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_11, langConfig.getString("item.minecraft.music_disc_11", "Music Disc")));
        itemNames.add(new ItemName(Material.MUSIC_DISC_WAIT, langConfig.getString("item.minecraft.music_disc_wait", "Music Disc")));
        itemNames.add(new ItemName(Material.BONE, langConfig.getString("item.minecraft.bone", "Bone")));
        itemNames.add(new ItemName(Material.INK_SAC, langConfig.getString("item.minecraft.ink_sac", "Ink Sac")));
        itemNames.add(new ItemName(Material.ROSE_RED, langConfig.getString("item.minecraft.rose_red", "Rose Red")));
        itemNames.add(new ItemName(Material.CACTUS_GREEN, langConfig.getString("item.minecraft.cactus_green", "Cactus Green")));
        itemNames.add(new ItemName(Material.COCOA_BEANS, langConfig.getString("item.minecraft.cocoa_beans", "Cocoa Beans")));
        itemNames.add(new ItemName(Material.LAPIS_LAZULI, langConfig.getString("item.minecraft.lapis_lazuli", "Lapis Lazuli")));
        itemNames.add(new ItemName(Material.PURPLE_DYE, langConfig.getString("item.minecraft.purple_dye", "Purple Dye")));
        itemNames.add(new ItemName(Material.CYAN_DYE, langConfig.getString("item.minecraft.cyan_dye", "Cyan Dye")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_DYE, langConfig.getString("item.minecraft.light_gray_dye", "Light Gray Dye")));
        itemNames.add(new ItemName(Material.GRAY_DYE, langConfig.getString("item.minecraft.gray_dye", "Gray Dye")));
        itemNames.add(new ItemName(Material.PINK_DYE, langConfig.getString("item.minecraft.pink_dye", "Pink Dye")));
        itemNames.add(new ItemName(Material.LIME_DYE, langConfig.getString("item.minecraft.lime_dye", "Lime Dye")));
        itemNames.add(new ItemName(Material.DANDELION_YELLOW, langConfig.getString("item.minecraft.dandelion_yellow", "Dandelion Yellow")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_DYE, langConfig.getString("item.minecraft.light_blue_dye", "Light Blue Dye")));
        itemNames.add(new ItemName(Material.MAGENTA_DYE, langConfig.getString("item.minecraft.magenta_dye", "Magenta Dye")));
        itemNames.add(new ItemName(Material.ORANGE_DYE, langConfig.getString("item.minecraft.orange_dye", "Orange Dye")));
        itemNames.add(new ItemName(Material.BONE_MEAL, langConfig.getString("item.minecraft.bone_meal", "Bone Meal")));
        itemNames.add(new ItemName(Material.SUGAR, langConfig.getString("item.minecraft.sugar", "Sugar")));
        itemNames.add(new ItemName(Material.BLACK_BED, langConfig.getString("block.minecraft.black_bed", "Black Bed")));
        itemNames.add(new ItemName(Material.RED_BED, langConfig.getString("block.minecraft.red_bed", "Red Bed")));
        itemNames.add(new ItemName(Material.GREEN_BED, langConfig.getString("block.minecraft.green_bed", "Green Bed")));
        itemNames.add(new ItemName(Material.BROWN_BED, langConfig.getString("block.minecraft.brown_bed", "Brown Bed")));
        itemNames.add(new ItemName(Material.BLUE_BED, langConfig.getString("block.minecraft.blue_bed", "Blue Bed")));
        itemNames.add(new ItemName(Material.PURPLE_BED, langConfig.getString("block.minecraft.purple_bed", "Purple Bed")));
        itemNames.add(new ItemName(Material.CYAN_BED, langConfig.getString("block.minecraft.cyan_bed", "Cyan Bed")));
        itemNames.add(new ItemName(Material.LIGHT_GRAY_BED, langConfig.getString("block.minecraft.light_gray_bed", "Light Gray Bed")));
        itemNames.add(new ItemName(Material.GRAY_BED, langConfig.getString("block.minecraft.gray_bed", "Gray Bed")));
        itemNames.add(new ItemName(Material.PINK_BED, langConfig.getString("block.minecraft.pink_bed", "Pink Bed")));
        itemNames.add(new ItemName(Material.LIME_BED, langConfig.getString("block.minecraft.lime_bed", "Lime Bed")));
        itemNames.add(new ItemName(Material.YELLOW_BED, langConfig.getString("block.minecraft.yellow_bed", "Yellow Bed")));
        itemNames.add(new ItemName(Material.LIGHT_BLUE_BED, langConfig.getString("block.minecraft.light_blue_bed", "Light Blue Bed")));
        itemNames.add(new ItemName(Material.MAGENTA_BED, langConfig.getString("block.minecraft.magenta_bed", "Magenta Bed")));
        itemNames.add(new ItemName(Material.ORANGE_BED, langConfig.getString("block.minecraft.orange_bed", "Orange Bed")));
        itemNames.add(new ItemName(Material.WHITE_BED, langConfig.getString("block.minecraft.white_bed", "White Bed")));
        itemNames.add(new ItemName(Material.REPEATER, langConfig.getString("block.minecraft.repeater", "Redstone Repeater")));
        itemNames.add(new ItemName(Material.COMPARATOR, langConfig.getString("block.minecraft.comparator", "Redstone Comparator")));
        itemNames.add(new ItemName(Material.MAP, langConfig.getString("item.minecraft.filled_map", "Map")));
        itemNames.add(new ItemName(Material.SHEARS, langConfig.getString("item.minecraft.shears", "Shears")));
        itemNames.add(new ItemName(Material.ROTTEN_FLESH, langConfig.getString("item.minecraft.rotten_flesh", "Rotten Flesh")));
        itemNames.add(new ItemName(Material.ENDER_PEARL, langConfig.getString("item.minecraft.ender_pearl", "Ender Pearl")));
        itemNames.add(new ItemName(Material.BLAZE_ROD, langConfig.getString("item.minecraft.blaze_rod", "Blaze Rod")));
        itemNames.add(new ItemName(Material.GHAST_TEAR, langConfig.getString("item.minecraft.ghast_tear", "Ghast Tear")));
        itemNames.add(new ItemName(Material.NETHER_WART, langConfig.getString("item.minecraft.nether_wart", "Nether Wart")));
        itemNames.add(new ItemName(Material.POTION, langConfig.getString("item.minecraft.potion", "Potion")));
        itemNames.add(new ItemName(Material.SPLASH_POTION, langConfig.getString("item.minecraft.splash_potion", "Splash Potion")));
        itemNames.add(new ItemName(Material.LINGERING_POTION, langConfig.getString("item.minecraft.lingering_potion", "Lingering Potion")));
        itemNames.add(new ItemName(Material.END_CRYSTAL, langConfig.getString("item.minecraft.end_crystal", "End Crystal")));
        itemNames.add(new ItemName(Material.GOLD_NUGGET, langConfig.getString("item.minecraft.gold_nugget", "Gold Nugget")));
        itemNames.add(new ItemName(Material.GLASS_BOTTLE, langConfig.getString("item.minecraft.glass_bottle", "Glass Bottle")));
        itemNames.add(new ItemName(Material.SPIDER_EYE, langConfig.getString("item.minecraft.spider_eye", "Spider Eye")));
        itemNames.add(new ItemName(Material.FERMENTED_SPIDER_EYE, langConfig.getString("item.minecraft.fermented_spider_eye", "Fermented Spider Eye")));
        itemNames.add(new ItemName(Material.BLAZE_POWDER, langConfig.getString("item.minecraft.blaze_powder", "Blaze Powder")));
        itemNames.add(new ItemName(Material.MAGMA_CREAM, langConfig.getString("item.minecraft.magma_cream", "Magma Cream")));
        itemNames.add(new ItemName(Material.CAULDRON, langConfig.getString("item.minecraft.cauldron", "Cauldron")));
        itemNames.add(new ItemName(Material.BREWING_STAND, langConfig.getString("item.minecraft.brewing_stand", "Brewing Stand")));
        itemNames.add(new ItemName(Material.ENDER_EYE, langConfig.getString("item.minecraft.ender_eye", "Eye of Ender")));
        itemNames.add(new ItemName(Material.GLISTERING_MELON_SLICE, langConfig.getString("item.minecraft.glistering_melon_slice", "Glistering Melon Slice")));
        itemNames.add(new ItemName(Material.BAT_SPAWN_EGG, langConfig.getString("item.minecraft.bat_spawn_egg", "Bat Spawn Egg")));
        itemNames.add(new ItemName(Material.BLAZE_SPAWN_EGG, langConfig.getString("item.minecraft.blaze_spawn_egg", "Blaze Spawn Egg")));
        itemNames.add(new ItemName(Material.CAVE_SPIDER_SPAWN_EGG, langConfig.getString("item.minecraft.cave_spider_spawn_egg", "Cave Spider Spawn Egg")));
        itemNames.add(new ItemName(Material.CHICKEN_SPAWN_EGG, langConfig.getString("item.minecraft.chicken_spawn_egg", "Chicken Spawn Egg")));
        itemNames.add(new ItemName(Material.COD_SPAWN_EGG, langConfig.getString("item.minecraft.cod_spawn_egg", "Cod Spawn Egg")));
        itemNames.add(new ItemName(Material.COW_SPAWN_EGG, langConfig.getString("item.minecraft.cow_spawn_egg", "Cow Spawn Egg")));
        itemNames.add(new ItemName(Material.CREEPER_SPAWN_EGG, langConfig.getString("item.minecraft.creeper_spawn_egg", "Creeper Spawn Egg")));
        itemNames.add(new ItemName(Material.DOLPHIN_SPAWN_EGG, langConfig.getString("item.minecraft.dolphin_spawn_egg", "Dolphin Spawn Egg")));
        itemNames.add(new ItemName(Material.DONKEY_SPAWN_EGG, langConfig.getString("item.minecraft.donkey_spawn_egg", "Donkey Spawn Egg")));
        itemNames.add(new ItemName(Material.DROWNED_SPAWN_EGG, langConfig.getString("item.minecraft.drowned_spawn_egg", "Drowned Spawn Egg")));
        itemNames.add(new ItemName(Material.ELDER_GUARDIAN_SPAWN_EGG, langConfig.getString("item.minecraft.elder_guardian_spawn_egg", "Elder Guardian Spawn Egg")));
        itemNames.add(new ItemName(Material.ENDERMAN_SPAWN_EGG, langConfig.getString("item.minecraft.enderman_spawn_egg", "Enderman Spawn Egg")));
        itemNames.add(new ItemName(Material.ENDERMITE_SPAWN_EGG, langConfig.getString("item.minecraft.endermite_spawn_egg", "Endermite Spawn Egg")));
        itemNames.add(new ItemName(Material.EVOKER_SPAWN_EGG, langConfig.getString("item.minecraft.evoker_spawn_egg", "Evoker Spawn Egg")));
        itemNames.add(new ItemName(Material.GHAST_SPAWN_EGG, langConfig.getString("item.minecraft.ghast_spawn_egg", "Ghast Spawn Egg")));
        itemNames.add(new ItemName(Material.GUARDIAN_SPAWN_EGG, langConfig.getString("item.minecraft.guardian_spawn_egg", "Guardian Spawn Egg")));
        itemNames.add(new ItemName(Material.HORSE_SPAWN_EGG, langConfig.getString("item.minecraft.horse_spawn_egg", "Horse Spawn Egg")));
        itemNames.add(new ItemName(Material.HUSK_SPAWN_EGG, langConfig.getString("item.minecraft.husk_spawn_egg", "Husk Spawn Egg")));
        itemNames.add(new ItemName(Material.LLAMA_SPAWN_EGG, langConfig.getString("item.minecraft.llama_spawn_egg", "Llama Spawn Egg")));
        itemNames.add(new ItemName(Material.MAGMA_CUBE_SPAWN_EGG, langConfig.getString("item.minecraft.magma_cube_spawn_egg", "Magma Cube Spawn Egg")));
        itemNames.add(new ItemName(Material.MOOSHROOM_SPAWN_EGG, langConfig.getString("item.minecraft.mooshroom_spawn_egg", "Mooshroom Spawn Egg")));
        itemNames.add(new ItemName(Material.MULE_SPAWN_EGG, langConfig.getString("item.minecraft.mule_spawn_egg", "Mule Spawn Egg")));
        itemNames.add(new ItemName(Material.OCELOT_SPAWN_EGG, langConfig.getString("item.minecraft.ocelot_spawn_egg", "Ocelot Spawn Egg")));
        itemNames.add(new ItemName(Material.PARROT_SPAWN_EGG, langConfig.getString("item.minecraft.parrot_spawn_egg", "Parrot Spawn Egg")));
        itemNames.add(new ItemName(Material.PIG_SPAWN_EGG, langConfig.getString("item.minecraft.pig_spawn_egg", "Pig Spawn Egg")));
        itemNames.add(new ItemName(Material.PHANTOM_SPAWN_EGG, langConfig.getString("item.minecraft.phantom_spawn_egg", "Phantom Spawn Egg")));
        itemNames.add(new ItemName(Material.POLAR_BEAR_SPAWN_EGG, langConfig.getString("item.minecraft.polar_bear_spawn_egg", "Polar Bear Spawn Egg")));
        itemNames.add(new ItemName(Material.PUFFERFISH_SPAWN_EGG, langConfig.getString("item.minecraft.pufferfish_spawn_egg", "Pufferfish Spawn Egg")));
        itemNames.add(new ItemName(Material.RABBIT_SPAWN_EGG, langConfig.getString("item.minecraft.rabbit_spawn_egg", "Rabbit Spawn Egg")));
        itemNames.add(new ItemName(Material.SALMON_SPAWN_EGG, langConfig.getString("item.minecraft.salmon_spawn_egg", "Salmon Spawn Egg")));
        itemNames.add(new ItemName(Material.SHEEP_SPAWN_EGG, langConfig.getString("item.minecraft.sheep_spawn_egg", "Sheep Spawn Egg")));
        itemNames.add(new ItemName(Material.SHULKER_SPAWN_EGG, langConfig.getString("item.minecraft.shulker_spawn_egg", "Shulker Spawn Egg")));
        itemNames.add(new ItemName(Material.SILVERFISH_SPAWN_EGG, langConfig.getString("item.minecraft.silverfish_spawn_egg", "Silverfish Spawn Egg")));
        itemNames.add(new ItemName(Material.SKELETON_SPAWN_EGG, langConfig.getString("item.minecraft.skeleton_spawn_egg", "Skeleton Spawn Egg")));
        itemNames.add(new ItemName(Material.SKELETON_HORSE_SPAWN_EGG, langConfig.getString("item.minecraft.skeleton_horse_spawn_egg", "Skeleton Horse Spawn Egg")));
        itemNames.add(new ItemName(Material.SLIME_SPAWN_EGG, langConfig.getString("item.minecraft.slime_spawn_egg", "Slime Spawn Egg")));
        itemNames.add(new ItemName(Material.SPIDER_SPAWN_EGG, langConfig.getString("item.minecraft.spider_spawn_egg", "Spider Spawn Egg")));
        itemNames.add(new ItemName(Material.SQUID_SPAWN_EGG, langConfig.getString("item.minecraft.squid_spawn_egg", "Squid Spawn Egg")));
        itemNames.add(new ItemName(Material.STRAY_SPAWN_EGG, langConfig.getString("item.minecraft.stray_spawn_egg", "Stray Spawn Egg")));
        itemNames.add(new ItemName(Material.TROPICAL_FISH_SPAWN_EGG, langConfig.getString("item.minecraft.tropical_fish_spawn_egg", "Tropical Fish Spawn Egg")));
        itemNames.add(new ItemName(Material.TURTLE_SPAWN_EGG, langConfig.getString("item.minecraft.turtle_spawn_egg", "Turtle Spawn Egg")));
        itemNames.add(new ItemName(Material.VEX_SPAWN_EGG, langConfig.getString("item.minecraft.vex_spawn_egg", "Vex Spawn Egg")));
        itemNames.add(new ItemName(Material.VILLAGER_SPAWN_EGG, langConfig.getString("item.minecraft.villager_spawn_egg", "Villager Spawn Egg")));
        itemNames.add(new ItemName(Material.VINDICATOR_SPAWN_EGG, langConfig.getString("item.minecraft.vindicator_spawn_egg", "Vindicator Spawn Egg")));
        itemNames.add(new ItemName(Material.WITCH_SPAWN_EGG, langConfig.getString("item.minecraft.witch_spawn_egg", "Witch Spawn Egg")));
        itemNames.add(new ItemName(Material.WITHER_SKELETON_SPAWN_EGG, langConfig.getString("item.minecraft.wither_skeleton_spawn_egg", "Wither Skeleton Spawn Egg")));
        itemNames.add(new ItemName(Material.WOLF_SPAWN_EGG, langConfig.getString("item.minecraft.wolf_spawn_egg", "Wolf Spawn Egg")));
        itemNames.add(new ItemName(Material.ZOMBIE_SPAWN_EGG, langConfig.getString("item.minecraft.zombie_spawn_egg", "Zombie Spawn Egg")));
        itemNames.add(new ItemName(Material.ZOMBIE_HORSE_SPAWN_EGG, langConfig.getString("item.minecraft.zombie_horse_spawn_egg", "Zombie Horse Spawn Egg")));
        itemNames.add(new ItemName(Material.ZOMBIE_PIGMAN_SPAWN_EGG, langConfig.getString("item.minecraft.zombie_pigman_spawn_egg", "Zombie Pigman Spawn Egg")));
        itemNames.add(new ItemName(Material.ZOMBIE_VILLAGER_SPAWN_EGG, langConfig.getString("item.minecraft.zombie_villager_spawn_egg", "Zombie Villager Spawn Egg")));
        itemNames.add(new ItemName(Material.EXPERIENCE_BOTTLE, langConfig.getString("item.minecraft.experience_bottle", "Bottle o' Enchanting")));
        itemNames.add(new ItemName(Material.FIRE_CHARGE, langConfig.getString("item.minecraft.fire_charge", "Fire Charge")));
        itemNames.add(new ItemName(Material.WRITABLE_BOOK, langConfig.getString("item.minecraft.writable_book", "Book and Quill")));
        itemNames.add(new ItemName(Material.WRITTEN_BOOK, langConfig.getString("item.minecraft.written_book", "Written Book")));
        itemNames.add(new ItemName(Material.FLOWER_POT, langConfig.getString("item.minecraft.flower_pot", "Flower Pot")));
        itemNames.add(new ItemName(Material.MAP, langConfig.getString("item.minecraft.map", "Empty Map")));
        itemNames.add(new ItemName(Material.CARROT, langConfig.getString("item.minecraft.carrot", "Carrot")));
        itemNames.add(new ItemName(Material.GOLDEN_CARROT, langConfig.getString("item.minecraft.golden_carrot", "Golden Carrot")));
        itemNames.add(new ItemName(Material.POTATO, langConfig.getString("item.minecraft.potato", "Potato")));
        itemNames.add(new ItemName(Material.BAKED_POTATO, langConfig.getString("item.minecraft.baked_potato", "Baked Potato")));
        itemNames.add(new ItemName(Material.POISONOUS_POTATO, langConfig.getString("item.minecraft.poisonous_potato", "Poisonous Potato")));
        itemNames.add(new ItemName(Material.SKELETON_SKULL, langConfig.getString("item.minecraft.skeleton_skull", "Skeleton Skull")));
        itemNames.add(new ItemName(Material.WITHER_SKELETON_SKULL, langConfig.getString("item.minecraft.wither_skeleton_skull", "Wither Skeleton Skull")));
        itemNames.add(new ItemName(Material.ZOMBIE_HEAD, langConfig.getString("item.minecraft.zombie_head", "Zombie Head")));
        itemNames.add(new ItemName(Material.CREEPER_HEAD, langConfig.getString("item.minecraft.creeper_head", "Creeper Head")));
        itemNames.add(new ItemName(Material.DRAGON_HEAD, langConfig.getString("item.minecraft.dragon_head", "Dragon Head")));
        itemNames.add(new ItemName(Material.CARROT_ON_A_STICK, langConfig.getString("item.minecraft.carrot_on_a_stick", "Carrot on a Stick")));
        itemNames.add(new ItemName(Material.NETHER_STAR, langConfig.getString("item.minecraft.nether_star", "Nether Star")));
        itemNames.add(new ItemName(Material.PUMPKIN_PIE, langConfig.getString("item.minecraft.pumpkin_pie", "Pumpkin Pie")));
        itemNames.add(new ItemName(Material.ENCHANTED_BOOK, langConfig.getString("item.minecraft.enchanted_book", "Enchanted Book")));
        itemNames.add(new ItemName(Material.FIREWORK_ROCKET, langConfig.getString("item.minecraft.firework_rocket", "Firework Rocket")));
        itemNames.add(new ItemName(Material.FIREWORK_STAR, langConfig.getString("item.minecraft.firework_star", "Firework Star")));
        itemNames.add(new ItemName(Material.NETHER_BRICK, langConfig.getString("item.minecraft.nether_brick", "Nether Brick")));
        itemNames.add(new ItemName(Material.QUARTZ, langConfig.getString("item.minecraft.quartz", "Nether Quartz")));
        itemNames.add(new ItemName(Material.ARMOR_STAND, langConfig.getString("item.minecraft.armor_stand", "Armor Stand")));
        itemNames.add(new ItemName(Material.IRON_HORSE_ARMOR, langConfig.getString("item.minecraft.iron_horse_armor", "Iron Horse Armor")));
        itemNames.add(new ItemName(Material.GOLDEN_HORSE_ARMOR, langConfig.getString("item.minecraft.golden_horse_armor", "Golden Horse Armor")));
        itemNames.add(new ItemName(Material.DIAMOND_HORSE_ARMOR, langConfig.getString("item.minecraft.diamond_horse_armor", "Diamond Horse Armor")));
        itemNames.add(new ItemName(Material.PRISMARINE_SHARD, langConfig.getString("item.minecraft.prismarine_shard", "Prismarine Shard")));
        itemNames.add(new ItemName(Material.PRISMARINE_CRYSTALS, langConfig.getString("item.minecraft.prismarine_crystals", "Prismarine Crystals")));
        itemNames.add(new ItemName(Material.CHORUS_FRUIT, langConfig.getString("item.minecraft.chorus_fruit", "Chorus Fruit")));
        itemNames.add(new ItemName(Material.POPPED_CHORUS_FRUIT, langConfig.getString("item.minecraft.popped_chorus_fruit", "Popped Chorus Fruit")));
        itemNames.add(new ItemName(Material.BEETROOT, langConfig.getString("item.minecraft.beetroot", "Beetroot")));
        itemNames.add(new ItemName(Material.BEETROOT_SEEDS, langConfig.getString("item.minecraft.beetroot_seeds", "Beetroot Seeds")));
        itemNames.add(new ItemName(Material.BEETROOT_SOUP, langConfig.getString("item.minecraft.beetroot_soup", "Beetroot Soup")));
        itemNames.add(new ItemName(Material.DRAGON_BREATH, langConfig.getString("item.minecraft.dragon_breath", "Dragon's Breath")));
        itemNames.add(new ItemName(Material.ELYTRA, langConfig.getString("item.minecraft.elytra", "Elytra")));
        itemNames.add(new ItemName(Material.TOTEM_OF_UNDYING, langConfig.getString("item.minecraft.totem_of_undying", "Totem of Undying")));
        itemNames.add(new ItemName(Material.SHULKER_SHELL, langConfig.getString("item.minecraft.shulker_shell", "Shulker Shell")));
        itemNames.add(new ItemName(Material.IRON_NUGGET, langConfig.getString("item.minecraft.iron_nugget", "Iron Nugget")));
        itemNames.add(new ItemName(Material.KNOWLEDGE_BOOK, langConfig.getString("item.minecraft.knowledge_book", "Knowledge Book")));
        itemNames.add(new ItemName(Material.DEBUG_STICK, langConfig.getString("item.minecraft.debug_stick", "Debug Stick")));
        itemNames.add(new ItemName(Material.TRIDENT, langConfig.getString("item.minecraft.trident", "Trident")));
        itemNames.add(new ItemName(Material.SCUTE, langConfig.getString("item.minecraft.scute", "Scute")));
        itemNames.add(new ItemName(Material.TURTLE_HELMET, langConfig.getString("item.minecraft.turtle_helmet", "Turtle Shell")));
        itemNames.add(new ItemName(Material.PHANTOM_MEMBRANE, langConfig.getString("item.minecraft.phantom_membrane", "Phantom Membrane")));
        itemNames.add(new ItemName(Material.NAUTILUS_SHELL, langConfig.getString("item.minecraft.nautilus_shell", "Nautilus Shell")));
        itemNames.add(new ItemName(Material.HEART_OF_THE_SEA, langConfig.getString("item.minecraft.heart_of_the_sea", "Heart of the Sea")));

        // Add Enchantment Names
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_ALL, langConfig.getString("enchantment.minecraft.sharpness", "Sharpness")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_UNDEAD, langConfig.getString("enchantment.minecraft.smite", "Smite")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_ARTHROPODS, langConfig.getString("enchantment.minecraft.bane_of_arthropods", "Bane of Arthropods")));
        enchantmentNames.add(new EnchantmentName(Enchantment.KNOCKBACK, langConfig.getString("enchantment.minecraft.knockback", "Knockback")));
        enchantmentNames.add(new EnchantmentName(Enchantment.FIRE_ASPECT, langConfig.getString("enchantment.minecraft.fire_aspect", "Fire Aspect")));
        enchantmentNames.add(new EnchantmentName(Enchantment.SWEEPING_EDGE, langConfig.getString("enchantment.minecraft.sweeping", "Sweeping Edge")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_ENVIRONMENTAL, langConfig.getString("enchantment.minecraft.protection", "Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_FIRE, langConfig.getString("enchantment.minecraft.fire_protection", "Fire Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_FALL, langConfig.getString("enchantment.minecraft.feather_falling", "Feather Falling")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_EXPLOSIONS, langConfig.getString("enchantment.minecraft.blast_protection", "Blast Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_PROJECTILE, langConfig.getString("enchantment.minecraft.projectile_protection", "Projectile Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.OXYGEN, langConfig.getString("enchantment.minecraft.respiration", "Respiration")));
        enchantmentNames.add(new EnchantmentName(Enchantment.WATER_WORKER, langConfig.getString("enchantment.minecraft.aqua_affinity", "Aqua Affinity")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DEPTH_STRIDER, langConfig.getString("enchantment.minecraft.depth_strider", "Depth Strider")));
        enchantmentNames.add(new EnchantmentName(Enchantment.FROST_WALKER, langConfig.getString("enchantment.minecraft.frost_walker", "Frost Walker")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DIG_SPEED, langConfig.getString("enchantment.minecraft.efficiency", "Efficiency")));
        enchantmentNames.add(new EnchantmentName(Enchantment.SILK_TOUCH, langConfig.getString("enchantment.minecraft.silk_touch", "Silk Touch")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DURABILITY, langConfig.getString("enchantment.minecraft.unbreaking", "Unbreaking")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LOOT_BONUS_MOBS, langConfig.getString("enchantment.minecraft.looting", "Looting")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LOOT_BONUS_BLOCKS, langConfig.getString("enchantment.minecraft.fortune", "Fortune")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LUCK, langConfig.getString("enchantment.minecraft.luck_of_the_sea", "Luck of the Sea")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LURE, langConfig.getString("enchantment.minecraft.lure", "Lure")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_DAMAGE, langConfig.getString("enchantment.minecraft.power", "Power")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_FIRE, langConfig.getString("enchantment.minecraft.flame", "Flame")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_KNOCKBACK, langConfig.getString("enchantment.minecraft.punch", "Punch")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_INFINITE, langConfig.getString("enchantment.minecraft.infinity", "Infinity")));
        enchantmentNames.add(new EnchantmentName(Enchantment.THORNS, langConfig.getString("enchantment.minecraft.thorns", "Thorns")));
        enchantmentNames.add(new EnchantmentName(Enchantment.MENDING, langConfig.getString("enchantment.minecraft.mending", "Mending")));
        enchantmentNames.add(new EnchantmentName(Enchantment.BINDING_CURSE, langConfig.getString("enchantment.minecraft.binding_curse", "Curse of Binding")));
        enchantmentNames.add(new EnchantmentName(Enchantment.VANISHING_CURSE, langConfig.getString("enchantment.minecraft.vanishing_curse", "Curse of Vanishing")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LOYALTY, langConfig.getString("enchantment.minecraft.loyalty", "Loyalty")));
        enchantmentNames.add(new EnchantmentName(Enchantment.IMPALING, langConfig.getString("enchantment.minecraft.impaling", "Impaling")));
        enchantmentNames.add(new EnchantmentName(Enchantment.RIPTIDE, langConfig.getString("enchantment.minecraft.riptide", "Riptide")));
        enchantmentNames.add(new EnchantmentName(Enchantment.CHANNELING, langConfig.getString("enchantment.minecraft.channeling", "Channeling")));

        // Add Enchantment Level Names
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(1, langConfig.getString("enchantment.level.1", "I")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(2, langConfig.getString("enchantment.level.2", "II")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(3, langConfig.getString("enchantment.level.3", "II")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(4, langConfig.getString("enchantment.level.4", "IV")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(5, langConfig.getString("enchantment.level.5", "V")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(6, langConfig.getString("enchantment.level.6", "VI")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(7, langConfig.getString("enchantment.level.7", "VII")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(8, langConfig.getString("enchantment.level.8", "VIII")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(9, langConfig.getString("enchantment.level.9", "IX")));
        enchantmentLevelNames.add(new EnchantmentName.EnchantmentLevelName(10, langConfig.getString("enchantment.level.10", "X")));

        // Add Potion Effect Names
        potionEffectNames.add(new PotionEffectName(PotionEffectType.SPEED, langConfig.getString("effect.minecraft.speed", "Speed")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.SLOW, langConfig.getString("effect.minecraft.slowness", "Slowness")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.FAST_DIGGING, langConfig.getString("effect.minecraft.haste", "Haste")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.SLOW_DIGGING, langConfig.getString("effect.minecraft.mining_fatigue", "Mining Fatigue")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.INCREASE_DAMAGE, langConfig.getString("effect.minecraft.strength", "Strength")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.HEAL, langConfig.getString("effect.minecraft.instant_health", "Instant Health")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.HARM, langConfig.getString("effect.minecraft.instant_damage", "Instant Damage")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.JUMP, langConfig.getString("effect.minecraft.jump_boost", "Jump Boost")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.CONFUSION, langConfig.getString("effect.minecraft.nausea", "Nausea")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.REGENERATION, langConfig.getString("effect.minecraft.regeneration", "Regeneration")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.DAMAGE_RESISTANCE, langConfig.getString("effect.minecraft.resistance", "Resistance")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.FIRE_RESISTANCE, langConfig.getString("effect.minecraft.fire_resistance", "Fire Resistance")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.WATER_BREATHING, langConfig.getString("effect.minecraft.water_breathing", "Water Breathing")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.INVISIBILITY, langConfig.getString("effect.minecraft.invisibility", "Invisibility")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.BLINDNESS, langConfig.getString("effect.minecraft.blindness", "Blindness")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.NIGHT_VISION, langConfig.getString("effect.minecraft.night_vision", "Night Vision")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.HUNGER, langConfig.getString("effect.minecraft.hunger", "Hunger")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.WEAKNESS, langConfig.getString("effect.minecraft.weakness", "Weakness")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.POISON, langConfig.getString("effect.minecraft.poison", "Poison")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.WITHER, langConfig.getString("effect.minecraft.wither", "Wither")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.HEALTH_BOOST, langConfig.getString("effect.minecraft.health_boost", "Health Boost")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.ABSORPTION, langConfig.getString("effect.minecraft.absorption", "Absorption")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.SATURATION, langConfig.getString("effect.minecraft.saturation", "Saturation")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.GLOWING, langConfig.getString("effect.minecraft.glowing", "Glowing")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.LUCK, langConfig.getString("effect.minecraft.luck", "Luck")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.UNLUCK, langConfig.getString("effect.minecraft.unluck", "Bad Luck")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.LEVITATION, langConfig.getString("effect.minecraft.levitation", "Levitation")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.SLOW_FALLING, langConfig.getString("effect.minecraft.slow_falling", "Slow Falling")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.CONDUIT_POWER, langConfig.getString("effect.minecraft.conduit_power", "Conduit Power")));
        potionEffectNames.add(new PotionEffectName(PotionEffectType.DOLPHINS_GRACE, langConfig.getString("effect.minecraft.dolphins_grace", "Dolphin's Grace")));

        // Add Potion Names
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.UNCRAFTABLE, langConfig.getString("item.minecraft.potion.effect.empty", "Uncraftable Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WATER, langConfig.getString("item.minecraft.potion.effect.water", "Water Bottle")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.MUNDANE, langConfig.getString("item.minecraft.potion.effect.mundane", "Mundane Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.THICK, langConfig.getString("item.minecraft.potion.effect.thick", "Thick Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.AWKWARD, langConfig.getString("item.minecraft.potion.effect.awkward", "Awkward Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.NIGHT_VISION, langConfig.getString("item.minecraft.potion.effect.night_vision", "Potion of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INVISIBILITY, langConfig.getString("item.minecraft.potion.effect.invisibility", "Potion of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.JUMP, langConfig.getString("item.minecraft.potion.effect.leaping", "Potion of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("item.minecraft.potion.effect.fire_resistance", "Potion of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.SPEED, langConfig.getString("item.minecraft.potion.effect.swiftness", "Potion of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.SLOWNESS, langConfig.getString("item.minecraft.potion.effect.slowness", "Potion of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WATER_BREATHING, langConfig.getString("item.minecraft.potion.effect.water_breathing", "Potion of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INSTANT_HEAL, langConfig.getString("item.minecraft.potion.effect.healing", "Potion of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("item.minecraft.potion.effect.harming", "Potion of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.POISON, langConfig.getString("item.minecraft.potion.effect.poison", "Potion of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.REGEN, langConfig.getString("item.minecraft.potion.effect.regeneration", "Potion of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.STRENGTH, langConfig.getString("item.minecraft.potion.effect.strength", "Potion of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WEAKNESS, langConfig.getString("item.minecraft.potion.effect.weakness", "Potion of Weakness")));
        //potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.LEVITATION, langConfig.getString("item.minecraft.potion.effect.levitation", "Potion of Levitation")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.LUCK, langConfig.getString("item.minecraft.potion.effect.luck", "Potion of Luck")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.TURTLE_MASTER, langConfig.getString("item.minecraft.potion.effect.turtle_master", "Potion of the Turtle Master")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.SLOW_FALLING, langConfig.getString("item.minecraft.potion.effect.slow_falling", "Potion of Slow Falling")));

        // Add Splash Potion Names
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.UNCRAFTABLE, langConfig.getString("item.minecraft.splash_potion.effect.empty", "Splash Uncraftable Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WATER, langConfig.getString("item.minecraft.splash_potion.effect.water", "Splash Water Bottle")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.MUNDANE, langConfig.getString("item.minecraft.splash_potion.effect.mundane", "Mundane Splash Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.THICK, langConfig.getString("item.minecraft.splash_potion.effect.thick", "Thick Splash Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.AWKWARD, langConfig.getString("item.minecraft.splash_potion.effect.awkward", "Awkward Splash Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.NIGHT_VISION, langConfig.getString("item.minecraft.splash_potion.effect.night_vision", "Splash Potion of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INVISIBILITY, langConfig.getString("item.minecraft.splash_potion.effect.invisibility", "Splash Potion of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.JUMP, langConfig.getString("item.minecraft.splash_potion.effect.leaping", "Splash Potion of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("item.minecraft.splash_potion.effect.fire_resistance", "Splash Potion of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.SPEED, langConfig.getString("item.minecraft.splash_potion.effect.swiftness", "Splash Potion of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.SLOWNESS, langConfig.getString("item.minecraft.splash_potion.effect.slowness", "Splash Potion of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WATER_BREATHING, langConfig.getString("item.minecraft.splash_potion.effect.water_breathing", "Splash Potion of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INSTANT_HEAL, langConfig.getString("item.minecraft.splash_potion.effect.healing", "Splash Potion of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("item.minecraft.splash_potion.effect.harming", "Splash Potion of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.POISON, langConfig.getString("item.minecraft.splash_potion.effect.poison", "Splash Potion of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.REGEN, langConfig.getString("item.minecraft.splash_potion.effect.regeneration", "Splash Potion of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.STRENGTH, langConfig.getString("item.minecraft.splash_potion.effect.strength", "Splash Potion of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WEAKNESS, langConfig.getString("item.minecraft.splash_potion.effect.weakness", "Splash Potion of Weakness")));
        //potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.LEVITATION, langConfig.getString("item.minecraft.splash_potion.effect.levitation", "Splash Potion of Levitation")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.LUCK, langConfig.getString("item.minecraft.splash_potion.effect.luck", "Splash Potion of Luck")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.TURTLE_MASTER, langConfig.getString("item.minecraft.splash_potion.effect.turtle_master", "Splash Potion of the Turtle Master")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.SLOW_FALLING, langConfig.getString("item.minecraft.splash_potion.effect.slow_falling", "Splash Potion of Slow Falling")));

        // Add Lingering Potion Names
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.UNCRAFTABLE, langConfig.getString("item.minecraft.lingering_potion.effect.empty", "Lingering Uncraftable Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WATER, langConfig.getString("item.minecraft.lingering_potion.effect.water", "Lingering Water Bottle")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.MUNDANE, langConfig.getString("item.minecraft.lingering_potion.effect.mundane", "Mundane Lingering Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.THICK, langConfig.getString("item.minecraft.lingering_potion.effect.thick", "Thick Lingering Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.AWKWARD, langConfig.getString("item.minecraft.lingering_potion.effect.awkward", "Awkward Lingering Potion")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.NIGHT_VISION, langConfig.getString("item.minecraft.lingering_potion.effect.night_vision", "Lingering Potion of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INVISIBILITY, langConfig.getString("item.minecraft.lingering_potion.effect.invisibility", "Lingering Potion of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.JUMP, langConfig.getString("item.minecraft.lingering_potion.effect.leaping", "Lingering Potion of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("item.minecraft.lingering_potion.effect.fire_resistance", "Lingering Potion of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.SPEED, langConfig.getString("item.minecraft.lingering_potion.effect.swiftness", "Lingering Potion of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.SLOWNESS, langConfig.getString("item.minecraft.lingering_potion.effect.slowness", "Lingering Potion of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WATER_BREATHING, langConfig.getString("item.minecraft.lingering_potion.effect.water_breathing", "Lingering Potion of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INSTANT_HEAL, langConfig.getString("item.minecraft.lingering_potion.effect.healing", "Lingering Potion of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("item.minecraft.lingering_potion.effect.harming", "Lingering Potion of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.POISON, langConfig.getString("item.minecraft.lingering_potion.effect.poison", "Lingering Potion of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.REGEN, langConfig.getString("item.minecraft.lingering_potion.effect.regeneration", "Lingering Potion of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.STRENGTH, langConfig.getString("item.minecraft.lingering_potion.effect.strength", "Lingering Potion of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WEAKNESS, langConfig.getString("item.minecraft.lingering_potion.effect.weakness", "Lingering Potion of Weakness")));
        //potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.LEVITATION, langConfig.getString("item.minecraft.lingering_potion.effect.levitation", "Lingering Potion of Levitation")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.LUCK, langConfig.getString("item.minecraft.lingering_potion.effect.luck", "Lingering Potion of Luck")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.TURTLE_MASTER, langConfig.getString("item.minecraft.lingering_potion.effect.turtle_master", "Lingering Potion of the Turtle Master")));
        potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.SLOW_FALLING, langConfig.getString("item.minecraft.lingering_potion.effect.slow_falling", "Lingering Potion of Slow Falling")));
        
        // Add Tipped Arrow Names
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.UNCRAFTABLE, langConfig.getString("item.minecraft.tipped_arrow.effect.empty", "Uncraftable Tipped Arrow")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WATER, langConfig.getString("item.minecraft.tipped_arrow.effect.water", "Arrow of Splashing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.MUNDANE, langConfig.getString("item.minecraft.tipped_arrow.effect.mundane", "Tipped Arrow")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.THICK, langConfig.getString("item.minecraft.tipped_arrow.effect.thick", "Tipped Arrow")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.AWKWARD, langConfig.getString("item.minecraft.tipped_arrow.effect.awkward", "Tipped Arrow")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.NIGHT_VISION, langConfig.getString("item.minecraft.tipped_arrow.effect.night_vision", "Arrow of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INVISIBILITY, langConfig.getString("item.minecraft.tipped_arrow.effect.invisibility", "Arrow of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.JUMP, langConfig.getString("item.minecraft.tipped_arrow.effect.leaping", "Arrow of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.FIRE_RESISTANCE, langConfig.getString("item.minecraft.tipped_arrow.effect.fire_resistance", "Arrow of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.SPEED, langConfig.getString("item.minecraft.tipped_arrow.effect.swiftness", "Arrow of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.SLOWNESS, langConfig.getString("item.minecraft.tipped_arrow.effect.slowness", "Arrow of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WATER_BREATHING, langConfig.getString("item.minecraft.tipped_arrow.effect.water_breathing", "Arrow of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INSTANT_HEAL, langConfig.getString("item.minecraft.tipped_arrow.effect.healing", "Arrow of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INSTANT_DAMAGE, langConfig.getString("item.minecraft.tipped_arrow.effect.harming", "Arrow of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.POISON, langConfig.getString("item.minecraft.tipped_arrow.effect.poison", "Arrow of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.REGEN, langConfig.getString("item.minecraft.tipped_arrow.effect.regeneration", "Arrow of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.STRENGTH, langConfig.getString("item.minecraft.tipped_arrow.effect.strength", "Arrow of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WEAKNESS, langConfig.getString("item.minecraft.tipped_arrow.effect.weakness", "Arrow of Weakness")));
        //potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.LEVITATION, langConfig.getString("item.minecraft.tipped_arrow.effect.levitation", "Arrow of Levitation")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.LUCK, langConfig.getString("item.minecraft.tipped_arrow.effect.luck", "Arrow of Luck")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.TURTLE_MASTER, langConfig.getString("item.minecraft.tipped_arrow.effect.turtle_master", "Arrow of the Turtle Master")));
        potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.SLOW_FALLING, langConfig.getString("item.minecraft.tipped_arrow.effect.slow_falling", "Arrow of Slow Falling")));
        
        // Add Music Disc Titles
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_13, langConfig.getString("item.minecraft.music_disc_13.desc", "C418 - 13")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_CAT, langConfig.getString("item.minecraft.music_disc_cat.desc", "C418 - cat")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_BLOCKS, langConfig.getString("item.minecraft.music_disc_blocks.desc", "C418 - blocks")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_CHIRP, langConfig.getString("item.minecraft.music_disc_chirp.desc", "C418 - chirp")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_FAR, langConfig.getString("item.minecraft.music_disc_far.desc", "C418 - far")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_MALL, langConfig.getString("item.minecraft.music_disc_mall.desc", "C418 - mall")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_MELLOHI, langConfig.getString("item.minecraft.music_disc_mellohi.desc", "C418 - mellohi")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_STAL, langConfig.getString("item.minecraft.music_disc_stal.desc", "C418 - stal")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_STRAD, langConfig.getString("item.minecraft.music_disc_strad.desc", "C418 - strad")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_WARD, langConfig.getString("item.minecraft.music_disc_ward.desc", "C418 - ward")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_11, langConfig.getString("item.minecraft.music_disc_11.desc", "C418 - 11")));
        musicDiscNames.add(new MusicDiscName(Material.MUSIC_DISC_WAIT, langConfig.getString("item.minecraft.music_disc_wait.desc", "C418 - wait")));

        // Add Book Generation Names
        generationNames.add(new BookGenerationName(BookMeta.Generation.ORIGINAL, langConfig.getString("book.generation.0", "Original")));
        generationNames.add(new BookGenerationName(BookMeta.Generation.COPY_OF_ORIGINAL, langConfig.getString("book.generation.1", "Copy of original")));
        generationNames.add(new BookGenerationName(BookMeta.Generation.COPY_OF_COPY, langConfig.getString("book.generation.2", "Copy of a copy")));
        generationNames.add(new BookGenerationName(BookMeta.Generation.TATTERED, langConfig.getString("book.generation.3", "Tattered")));

        // Add ShopChest Messages
        messages.add(new LocalizedMessage(Message.SHOP_CREATED, langConfig.getString("message.shop-created", "&6You were withdrawn &c%CREATION-PRICE% &6to create this shop.")));
        messages.add(new LocalizedMessage(Message.ADMIN_SHOP_CREATED, langConfig.getString("message.admin-shop-created", "&6You were withdrawn &c%CREATION-PRICE% &6to create this admin shop.")));
        messages.add(new LocalizedMessage(Message.CHEST_ALREADY_SHOP, langConfig.getString("message.chest-already-shop", "&cChest already shop.")));
        messages.add(new LocalizedMessage(Message.CHEST_BLOCKED, langConfig.getString("message.chest-blocked", "&cThere must not be a block above the chest.")));
        messages.add(new LocalizedMessage(Message.DOUBLE_CHEST_BLOCKED, langConfig.getString("message.double-chest-blocked", "&cThere must not be a block above the chest.")));
        messages.add(new LocalizedMessage(Message.SHOP_REMOVED, langConfig.getString("message.shop-removed", "&6Shop removed.")));
        messages.add(new LocalizedMessage(Message.ALL_SHOPS_REMOVED, langConfig.getString("message.all-shops-removed", "&6Removed all (&c%AMOUNT%&6) shop/s of &c%VENDOR%&6.")));
        messages.add(new LocalizedMessage(Message.CHEST_NO_SHOP, langConfig.getString("message.chest-no-shop", "&cChest is not a shop.")));
        messages.add(new LocalizedMessage(Message.SHOP_CREATE_NOT_ENOUGH_MONEY, langConfig.getString("message.shop-create-not-enough-money", "&cNot enough money. You need &6%CREATION-PRICE% &cto create a shop.")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_VENDOR, langConfig.getString("message.shopInfo.vendor", "&6Vendor: &e%VENDOR%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_PRODUCT, langConfig.getString("message.shopInfo.product", "&6Product: &e%AMOUNT% x %ITEMNAME%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_STOCK, langConfig.getString("message.shopInfo.stock", "&6In Stock: &e%STOCK%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_ENCHANTMENTS, langConfig.getString("message.shopInfo.enchantments", "&6Enchantments: &e%ENCHANTMENT%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_POTION_EFFECT, langConfig.getString("message.shopInfo.potion-effect", "&6Potion Effect: &e%POTION-EFFECT% %EXTENDED%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_MUSIC_TITLE, langConfig.getString("message.shopInfo.music-disc-title", "&6Music Disc Title: &e%MUSIC-TITLE%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_BOOK_GENERATION, langConfig.getString("message.shopInfo.book-generation", "&6Generation: &e%GENERATION%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_NONE, langConfig.getString("message.shopInfo.none", "&7None")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_PRICE, langConfig.getString("message.shopInfo.price", "&6Price: Buy: &e%BUY-PRICE%&6 Sell: &e%SELL-PRICE%")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_DISABLED, langConfig.getString("message.shopInfo.disabled", "&7Disabled")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_NORMAL, langConfig.getString("message.shopInfo.is-normal", "&6Type: &eNormal")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_ADMIN, langConfig.getString("message.shopInfo.is-admin", "&6Type: &eAdmin")));
        messages.add(new LocalizedMessage(Message.SHOP_INFO_EXTENDED, langConfig.getString("message.shopInfo.extended", "(Extended)")));
        messages.add(new LocalizedMessage(Message.BUY_SELL_DISABLED, langConfig.getString("message.buy-and-sell-disabled", "&cYou can't create a shop with buying and selling disabled.")));
        messages.add(new LocalizedMessage(Message.BUY_SUCCESS, langConfig.getString("message.buy-success", "&aYou bought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a from &6%VENDOR%&a.")));
        messages.add(new LocalizedMessage(Message.BUY_SUCCESS_ADMIN, langConfig.getString("message.buy-success-admin", "&aYou bought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a.")));
        messages.add(new LocalizedMessage(Message.SELL_SUCCESS, langConfig.getString("message.sell-success", "&aYou sold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a to &6%VENDOR%&a.")));
        messages.add(new LocalizedMessage(Message.SELL_SUCCESS_ADMIN, langConfig.getString("message.sell-success-admin", "&aYou sold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a.")));
        messages.add(new LocalizedMessage(Message.SOMEONE_BOUGHT, langConfig.getString("message.someone-bought", "&6%PLAYER% &abought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a from your shop.")));
        messages.add(new LocalizedMessage(Message.SOMEONE_SOLD, langConfig.getString("message.someone-sold", "&6%PLAYER% &asold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a to your shop.")));
        messages.add(new LocalizedMessage(Message.REVENUE_WHILE_OFFLINE, langConfig.getString("message.revenue-while-offline", "&6While you were offline, your shops have made a revenue of &c%REVENUE%&6.")));
        messages.add(new LocalizedMessage(Message.NOT_ENOUGH_INVENTORY_SPACE, langConfig.getString("message.not-enough-inventory-space", "&cNot enough space in inventory.")));
        messages.add(new LocalizedMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE, langConfig.getString("message.chest-not-enough-inventory-space", "&cShop is full.")));
        messages.add(new LocalizedMessage(Message.NOT_ENOUGH_MONEY, langConfig.getString("message.not-enough-money", "&cNot enough money.")));
        messages.add(new LocalizedMessage(Message.NOT_ENOUGH_ITEMS, langConfig.getString("message.not-enough-items", "&cNot enough items.")));
        messages.add(new LocalizedMessage(Message.VENDOR_NOT_ENOUGH_MONEY, langConfig.getString("message.vendor-not-enough-money", "&cVendor has not enough money.")));
        messages.add(new LocalizedMessage(Message.OUT_OF_STOCK, langConfig.getString("message.out-of-stock", "&cShop out of stock.")));
        messages.add(new LocalizedMessage(Message.VENDOR_OUT_OF_STOCK, langConfig.getString("message.vendor-out-of-stock", "&cYour shop that sells &6%AMOUNT% x %ITEMNAME% &cis out of stock.")));
        messages.add(new LocalizedMessage(Message.ERROR_OCCURRED, langConfig.getString("message.error-occurred", "&cAn error occurred: %ERROR%")));
        messages.add(new LocalizedMessage(Message.AMOUNT_PRICE_NOT_NUMBER, langConfig.getString("message.amount-and-price-not-number", "&cAmount and price must be a number.")));
        messages.add(new LocalizedMessage(Message.AMOUNT_IS_ZERO, langConfig.getString("message.amount-is-zero", "&cAmount must be greater than 0.")));
        messages.add(new LocalizedMessage(Message.PRICES_CONTAIN_DECIMALS, langConfig.getString("message.prices-contain-decimals", "&cPrices must not contain decimals.")));
        messages.add(new LocalizedMessage(Message.NO_ITEM_IN_HAND, langConfig.getString("message.no-item-in-hand", "&cNo item in hand")));
        messages.add(new LocalizedMessage(Message.CLICK_CHEST_CREATE, langConfig.getString("message.click-chest-to-create-shop", "&aClick a chest within 15 seconds to create a shop.")));
        messages.add(new LocalizedMessage(Message.CLICK_CHEST_REMOVE, langConfig.getString("message.click-chest-to-remove-shop", "&aClick a shop within 15 seconds to remove it.")));
        messages.add(new LocalizedMessage(Message.CLICK_CHEST_INFO, langConfig.getString("message.click-chest-for-info", "&aClick a shop within 15 seconds to retrieve information.")));
        messages.add(new LocalizedMessage(Message.CLICK_CHEST_OPEN, langConfig.getString("message.click-chest-to-open-shop", "&aClick a shop within 15 seconds to open it.")));
        messages.add(new LocalizedMessage(Message.CLICK_TO_CONFIRM, langConfig.getString("message.click-to-confirm", "&aClick again to confirm.")));
        messages.add(new LocalizedMessage(Message.OPENED_SHOP, langConfig.getString("message.opened-shop", "&aYou opened %VENDOR%'s shop.")));
        messages.add(new LocalizedMessage(Message.CANNOT_BREAK_SHOP, langConfig.getString("message.cannot-break-shop", "&cYou can't break a shop.")));
        messages.add(new LocalizedMessage(Message.CANNOT_SELL_BROKEN_ITEM, langConfig.getString("message.cannot-sell-broken-item", "&cYou can't sell a broken item.")));
        messages.add(new LocalizedMessage(Message.BUY_PRICE_TOO_LOW, langConfig.getString("message.buy-price-too-low", "&cThe buy price must be higher than %MIN-PRICE%.")));
        messages.add(new LocalizedMessage(Message.SELL_PRICE_TOO_LOW, langConfig.getString("message.sell-price-too-low", "&cThe sell price must be higher than %MIN-PRICE%.")));
        messages.add(new LocalizedMessage(Message.BUY_PRICE_TOO_HIGH, langConfig.getString("message.buy-price-too-high", "&cThe buy price must be lower than %MAX-PRICE%.")));
        messages.add(new LocalizedMessage(Message.SELL_PRICE_TOO_HIGH, langConfig.getString("message.sell-price-too-high", "&cThe sell price must be lower than %MAX-PRICE%.")));
        messages.add(new LocalizedMessage(Message.BUYING_DISABLED, langConfig.getString("message.buying-disabled", "&cBuying is disabled at this shop.")));
        messages.add(new LocalizedMessage(Message.SELLING_DISABLED, langConfig.getString("message.selling-disabled", "&cSelling is disabled at this shop.")));
        messages.add(new LocalizedMessage(Message.RELOADED_SHOPS, langConfig.getString("message.reloaded-shops", "&aSuccessfully reloaded %AMOUNT% shop/s.")));
        messages.add(new LocalizedMessage(Message.SHOP_LIMIT_REACHED, langConfig.getString("message.shop-limit-reached", "&cYou reached your limit of &6%LIMIT% &cshop/s.")));
        messages.add(new LocalizedMessage(Message.OCCUPIED_SHOP_SLOTS, langConfig.getString("message.occupied-shop-slots", "&6You have &c%AMOUNT%/%LIMIT% &6shop slot/s occupied.")));
        messages.add(new LocalizedMessage(Message.CANNOT_SELL_ITEM, langConfig.getString("message.cannot-sell-item", "&cYou cannot create a shop with this item.")));
        messages.add(new LocalizedMessage(Message.USE_IN_CREATIVE, langConfig.getString("message.use-in-creative", "&cYou cannot use a shop in creative mode.")));
        messages.add(new LocalizedMessage(Message.UPDATE_AVAILABLE, langConfig.getString("message.update.update-available", "&6&lVersion &c%VERSION% &6of &cShopChest &6is available &chere.")));
        messages.add(new LocalizedMessage(Message.UPDATE_CLICK_TO_DOWNLOAD, langConfig.getString("message.update.click-to-download", "Click to download")));
        messages.add(new LocalizedMessage(Message.UPDATE_NO_UPDATE, langConfig.getString("message.update.no-update", "&6&lNo new update available.")));
        messages.add(new LocalizedMessage(Message.UPDATE_CHECKING, langConfig.getString("message.update.checking", "&6&lChecking for updates...")));
        messages.add(new LocalizedMessage(Message.UPDATE_ERROR, langConfig.getString("message.update.error", "&c&lError while checking for updates.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_CREATE, langConfig.getString("message.noPermission.create", "&cYou don't have permission to create a shop.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_CREATE_ADMIN, langConfig.getString("message.noPermission.create-admin", "&cYou don't have permission to create an admin shop.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_CREATE_PROTECTED, langConfig.getString("message.noPermission.create-protected", "&cYou don't have permission to create a shop on a protected chest.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_OPEN_OTHERS, langConfig.getString("message.noPermission.open-others", "&cYou don't have permission to open this chest.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_BUY, langConfig.getString("message.noPermission.buy", "&cYou don't have permission to buy something.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_SELL, langConfig.getString("message.noPermission.sell", "&cYou don't have permission to sell something.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_BUY_HERE, langConfig.getString("message.noPermission.buy-here", "&cYou don't have permission to buy something here.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_SELL_HERE, langConfig.getString("message.noPermission.sell-here", "&cYou don't have permission to sell something here.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_REMOVE_OTHERS, langConfig.getString("message.noPermission.remove-others", "&cYou don't have permission to remove this shop.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_REMOVE_ADMIN, langConfig.getString("message.noPermission.remove-admin", "&cYou don't have permission to remove an admin shop.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_RELOAD, langConfig.getString("message.noPermission.reload", "&cYou don't have permission to reload the shops.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_UPDATE, langConfig.getString("message.noPermission.update", "&cYou don't have permission to check for updates.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_CONFIG, langConfig.getString("message.noPermission.config", "&cYou don't have permission to change configuration values.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_EXTEND_OTHERS, langConfig.getString("message.noPermission.extend-others", "&cYou don't have permission to extend this chest.")));
        messages.add(new LocalizedMessage(Message.NO_PERMISSION_EXTEND_PROTECTED, langConfig.getString("message.noPermission.extend-protected", "&cYou don't have permission to extend this chest to here.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_HEADER, langConfig.getString("message.commandDescription.header", "&6==== &c/%COMMAND% &6Help")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_FOOTER, langConfig.getString("message.commandDescription.footer", "&6==== End")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_CREATE, langConfig.getString("message.commandDescription.create", "&a/%COMMAND% create <amount> <buy-price> <sell-price> - Create a shop.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_CREATE_ADMIN, langConfig.getString("message.commandDescription.create-admin", "&a/%COMMAND% create <amount> <buy-price> <sell-price> [normal|admin] - Create a shop.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_REMOVE, langConfig.getString("message.commandDescription.remove", "&a/%COMMAND% remove - Remove a shop.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_INFO, langConfig.getString("message.commandDescription.info", "&a/%COMMAND% info - Retrieve shop information.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_REMOVEALL, langConfig.getString("message.commandDescription.removeall", "&a/%COMMAND% removeall - Remove all shops of a player.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_RELOAD, langConfig.getString("message.commandDescription.reload", "&a/%COMMAND% reload - Reload shops.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_UPDATE, langConfig.getString("message.commandDescription.update", "&a/%COMMAND% update - Check for Updates.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_LIMITS, langConfig.getString("message.commandDescription.limits", "&a/%COMMAND% limits - View shop limits.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_OPEN, langConfig.getString("message.commandDescription.open", "&a/%COMMAND% open - Open a shop.")));
        messages.add(new LocalizedMessage(Message.COMMAND_DESC_CONFIG, langConfig.getString("message.commandDescription.config", "&a/%COMMAND% config <set|get|remove> <property> <value> - Change configuration values.")));
        messages.add(new LocalizedMessage(Message.CHANGED_CONFIG_SET, langConfig.getString("message.config.set", "&6Changed &a%PROPERTY% &6to &a%VALUE%&6.")));
        messages.add(new LocalizedMessage(Message.CHANGED_CONFIG_REMOVED, langConfig.getString("message.config.removed", "&6Removed &a%VALUE% &6from &a%PROPERTY%&6.")));
        messages.add(new LocalizedMessage(Message.CHANGED_CONFIG_ADDED, langConfig.getString("message.config.added", "&6Added &a%VALUE% &6to &a%PROPERTY%&6.")));
    }

    /**
     * @param stack Item whose name to lookup
     * @return Localized Name of the Item, the custom name, or if <i>stack</i> is a book, the title of the book
     */
    public static String getItemName(ItemStack stack) {
        if (stack == null) return null;
        
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.getDisplayName() != null) {
                return meta.getDisplayName();
            } else if (meta instanceof BookMeta && ((BookMeta) meta).hasTitle()) {
                return ((BookMeta) meta).getTitle();
            } else if (meta instanceof SkullMeta) {
                if (((SkullMeta) meta).hasOwner()) {
                    return String.format(langConfig.getString("block.minecraft.player_head.named", "%s's Head"), ((SkullMeta) meta).getOwningPlayer().getName());
                }
            }
        }

        Material material = stack.getType();

        if (stack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            PotionType potionType;
            String upgradeString;

            potionType = meta.getBasePotionData().getType();
            upgradeString = (meta.getBasePotionData().isUpgraded() && Config.appendPotionLevelToItemName ? " II" : "");
            

            for (PotionName potionName : potionNames) {
                if (material == Material.POTION) {
                    if (potionName.getPotionItemType() == PotionName.PotionItemType.POTION && potionName.getPotionType() == potionType) {
                        return potionName.getLocalizedName() + upgradeString;
                    }
                } else if (material == Material.LINGERING_POTION) {
                    if (potionName.getPotionItemType() == PotionName.PotionItemType.LINGERING_POTION && potionName.getPotionType() == potionType) {
                        return potionName.getLocalizedName() + upgradeString;
                    }
                } else if (material == Material.TIPPED_ARROW) {
                    if (potionName.getPotionItemType() == PotionName.PotionItemType.TIPPED_ARROW && potionName.getPotionType() == potionType) {
                        return potionName.getLocalizedName() + upgradeString;
                    }
                } else if (material == Material.SPLASH_POTION) {
                    if (potionName.getPotionItemType() == PotionName.PotionItemType.SPLASH_POTION && potionName.getPotionType() == potionType) {
                        return potionName.getLocalizedName() + upgradeString;
                    }
                }
            }
        }

        for (ItemName itemName : itemNames) {
            if (itemName.getMaterial() == material) {
                return itemName.getLocalizedName();
            }
        }

        return formatDefaultString(material.toString());
    }

    /**
     * @param enchantment Enchantment whose name should be looked up
     * @param level       Level of the enchantment
     * @return Localized Name of the enchantment with the given level afterwards
     */
    public static String getEnchantmentName(Enchantment enchantment, int level) {
        if (enchantment == null) return null;
        String enchantmentString = formatDefaultString(enchantment.getKey().getKey());
        String levelString = langConfig.getString("enchantment.level." + level, String.valueOf(level));

        for (EnchantmentName enchantmentName : enchantmentNames) {
            if (enchantmentName.getEnchantment().equals(enchantment)) {
                enchantmentString = enchantmentName.getLocalizedName();
            }
        }

        for (EnchantmentName.EnchantmentLevelName enchantmentLevelName : enchantmentLevelNames) {
            if (enchantmentLevelName.getLevel() == level) {
                levelString = enchantmentLevelName.getLocalizedName();
            }
        }

        return enchantmentString + " " + levelString;
    }

    /**
     * @param enchantmentMap Map of enchantments of an item
     * @return Comma separated list of localized enchantments
     */
    public static String getEnchantmentString(Map<Enchantment, Integer> enchantmentMap) {
        if (enchantmentMap == null) return null;
        Enchantment[] enchantments = enchantmentMap.keySet().toArray(new Enchantment[enchantmentMap.size()]);
        StringBuilder enchantmentList = new StringBuilder();

        for (int i = 0; i < enchantments.length; i++) {
            Enchantment enchantment = enchantments[i];

            if (i == enchantments.length - 1) {
                enchantmentList.append(LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment)));
            } else {
                enchantmentList.append(LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment)));
                enchantmentList.append(", ");
            }
        }

        return enchantmentList.toString();
    }

    /**
     * @param itemStack Potion Item whose base effect name should be looked up
     * @return Localized name of the base potion effect
     */
    public static String getPotionEffectName(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (!(itemStack.getItemMeta() instanceof PotionMeta)) return "";

        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        PotionEffectType potionEffect;
        boolean upgraded;

        potionEffect = potionMeta.getBasePotionData().getType().getEffectType();
        upgraded = potionMeta.getBasePotionData().isUpgraded();

        String potionEffectString = formatDefaultString(potionEffect.toString());

        for (PotionEffectName potionEffectName : potionEffectNames) {
            if (potionEffectName.getEffect() == potionEffect) {
                potionEffectString = potionEffectName.getLocalizedName();
            }
        }

        return potionEffectString + (upgraded ? " II" : "");
    }

    /**
     * @param musicDiscMaterial Material of the Music Disc whose name should be looked up
     * @return Localized title of the Music Disc
     */
    public static String getMusicDiscName(Material musicDiscMaterial) {
        if (musicDiscMaterial == null) return null;
        for (MusicDiscName musicDiscName : musicDiscNames) {
            if (musicDiscMaterial == musicDiscName.getMusicDiscMaterial()) {
                return musicDiscName.getLocalizedName();
            }
        }

        return "";
    }

    /**
     * @param is ItemStack that should be of type {@link Material#WRITTEN_BOOK}
     * @return Localized name of the generation or {@code null} if the item is not a written book
     */
    public static String getBookGenerationName(ItemStack is) {
        if (is.getType() != Material.WRITTEN_BOOK) {
            return null;
        }

        BookMeta meta = (BookMeta) is.getItemMeta();
        BookMeta.Generation generation = BookMeta.Generation.ORIGINAL;

        if (meta.hasGeneration()) {
            generation = meta.getGeneration();
        }

        if (generation == null) return null;
        for (BookGenerationName generationName : generationNames) {
            if (generation == generationName.getGeneration()) {
                return generationName.getLocalizedName();
            }
        }

        return formatDefaultString(generation.toString());
    }

    /**
     * @param message Message which should be translated
     * @param replacements Replacements of placeholders which might be required to be replaced in the message
     * @return Localized Message
     */
    public static String getMessage(Message message, Replacement... replacements) {
        String finalMessage = ChatColor.RED + "An error occurred: Message not found: " + message.toString();

        for (LocalizedMessage localizedMessage : messages) {
            if (localizedMessage.getMessage() == message) {
                finalMessage = localizedMessage.getLocalizedString();

                for (Replacement replacement : replacements) {
                    Placeholder placeholder = replacement.getPlaceholder();
                    String toReplace = replacement.getReplacement();

                    if (placeholder == Placeholder.BUY_PRICE || placeholder == Placeholder.SELL_PRICE || placeholder == Placeholder.MIN_PRICE || placeholder == Placeholder.CREATION_PRICE || placeholder == Placeholder.REVENUE) {
                        if (!toReplace.equals(getMessage(Message.SHOP_INFO_DISABLED))) {
                            double price = Double.parseDouble(toReplace);
                            toReplace = plugin.getEconomy().format(price);
                        }
                    }

                    finalMessage = finalMessage.replace(placeholder.toString(), toReplace);
                }
            }
        }

        return finalMessage;
    }

    /**
     * Underscores will be replaced by spaces and the first letter of each word will be capitalized
     *
     * @param string String to format
     * @return Formatted String with underscores replaced by spaces and the first letter of each word capitalized
     */
    private static String formatDefaultString(String string) {
        string = string.replace("_", " ");
        String newString = "";

        if (string.contains(" ")) {
            for (int i = 0; i < string.split(" ").length; i++) {
                String part = string.split(" ")[i].toLowerCase();
                part = part.substring(0, 1).toUpperCase() + part.substring(1);
                newString = newString + part + (i == string.split(" ").length - 1 ? "" : " ");
            }

            return newString;
        } else {
            newString = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
        }

        return newString;
    }


}

