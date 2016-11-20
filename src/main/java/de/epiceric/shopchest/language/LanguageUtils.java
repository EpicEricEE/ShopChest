package de.epiceric.shopchest.language;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.LanguageConfiguration;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.nms.SpawnEggMeta;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

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
    private static ArrayList<LocalizedMessage> messages = new ArrayList<>();


    public static void load() {
        langConfig = plugin.getShopChestConfig().getLanguageConfig();

        itemNames.clear();
        enchantmentNames.clear();
        enchantmentLevelNames.clear();
        potionEffectNames.clear();
        entityNames.clear();
        potionNames.clear();
        musicDiscNames.clear();
        messages.clear();

        // Add Block Names
        itemNames.add(new ItemName(Material.STONE, langConfig.getString("tile.stone.stone.name", "Stone")));
        itemNames.add(new ItemName(Material.STONE, 1, langConfig.getString("tile.stone.granite.name", "Granite")));
        itemNames.add(new ItemName(Material.STONE, 2, langConfig.getString("tile.stone.graniteSmooth.name", "Polished Granite")));
        itemNames.add(new ItemName(Material.STONE, 3, langConfig.getString("tile.stone.diorite.name", "Diorite")));
        itemNames.add(new ItemName(Material.STONE, 4, langConfig.getString("tile.stone.dioriteSmooth.name", "Polished Diorite")));
        itemNames.add(new ItemName(Material.STONE, 5, langConfig.getString("tile.stone.andesite.name", "Andesite")));
        itemNames.add(new ItemName(Material.STONE, 6, langConfig.getString("tile.stone.andesiteSmooth.name", "Polished Andesite")));
        itemNames.add(new ItemName(Material.GRASS, langConfig.getString("tile.grass.name", "Grass Block")));
        itemNames.add(new ItemName(Material.DIRT, langConfig.getString("tile.dirt.default.name", "Dirt")));
        itemNames.add(new ItemName(Material.DIRT, 1, langConfig.getString("tile.dirt.coarse.name", "Coarse Dirt")));
        itemNames.add(new ItemName(Material.DIRT, 2, langConfig.getString("tile.dirt.podzol.name", "Podzol")));
        itemNames.add(new ItemName(Material.COBBLESTONE, langConfig.getString("tile.stonebrick.name", "Cobblestone")));
        itemNames.add(new ItemName(Material.WOOD, langConfig.getString("tile.wood.oak.name", "Oak Wood Planks")));
        itemNames.add(new ItemName(Material.WOOD, 1, langConfig.getString("tile.wood.spruce.name", "Spruce Wood Planks")));
        itemNames.add(new ItemName(Material.WOOD, 2, langConfig.getString("tile.wood.birch.name", "Birch Wood Planks")));
        itemNames.add(new ItemName(Material.WOOD, 3, langConfig.getString("tile.wood.jungle.name", "Jungle Wood Planks")));
        itemNames.add(new ItemName(Material.WOOD, 4, langConfig.getString("tile.wood.acacia.name", "Acacia Wood Planks")));
        itemNames.add(new ItemName(Material.WOOD, 5, langConfig.getString("tile.wood.big_oak.name", "Dark Oak Wood Planks")));
        itemNames.add(new ItemName(Material.SAPLING, langConfig.getString("tile.sapling.oak.name", "Oak Sapling")));
        itemNames.add(new ItemName(Material.SAPLING, 1, langConfig.getString("tile.sapling.spruce.name", "Spruce Sapling")));
        itemNames.add(new ItemName(Material.SAPLING, 2, langConfig.getString("tile.sapling.birch.name", "Birch Sapling")));
        itemNames.add(new ItemName(Material.SAPLING, 3, langConfig.getString("tile.sapling.jungle.name", "Jungle Sapling")));
        itemNames.add(new ItemName(Material.SAPLING, 4, langConfig.getString("tile.sapling.acacia.name", "Acacia Sapling")));
        itemNames.add(new ItemName(Material.SAPLING, 5, langConfig.getString("tile.sapling.big_oak.name", "Dark Oak Sapling")));
        itemNames.add(new ItemName(Material.BEDROCK, langConfig.getString("tile.bedrock.name", "Bedrock")));
        itemNames.add(new ItemName(Material.WATER, langConfig.getString("tile.water.name", "Water")));
        itemNames.add(new ItemName(Material.LAVA, langConfig.getString("tile.lava.name", "Lava")));
        itemNames.add(new ItemName(Material.SAND, langConfig.getString("tile.sand.default.name", "Sand")));
        itemNames.add(new ItemName(Material.SAND, 1, langConfig.getString("tile.sand.red.name", "Red Sand")));
        itemNames.add(new ItemName(Material.GRAVEL, langConfig.getString("tile.gravel.name", "Gravel")));
        itemNames.add(new ItemName(Material.GOLD_ORE, langConfig.getString("tile.oreGold.name", "Gold Ore")));
        itemNames.add(new ItemName(Material.IRON_ORE, langConfig.getString("tile.oreIron.name", "Iron Ore")));
        itemNames.add(new ItemName(Material.COAL_ORE, langConfig.getString("tile.oreCoal.name", "Coal Ore")));
        itemNames.add(new ItemName(Material.LOG, langConfig.getString("tile.log.oak.name", "Oak Wood")));
        itemNames.add(new ItemName(Material.LOG, 1, langConfig.getString("tile.log.spruce.name", "Spruce Wood")));
        itemNames.add(new ItemName(Material.LOG, 2, langConfig.getString("tile.log.birch.name", "Birch Wood")));
        itemNames.add(new ItemName(Material.LOG, 3, langConfig.getString("tile.log.jungle.name", "Jungle Wood")));
        itemNames.add(new ItemName(Material.LEAVES, langConfig.getString("tile.leaves.oak.name", "Oak Leaves")));
        itemNames.add(new ItemName(Material.LEAVES, 1, langConfig.getString("tile.leaves.spruce.name", "Spruce Leaves")));
        itemNames.add(new ItemName(Material.LEAVES, 2, langConfig.getString("tile.leaves.birch.name", "Birch Leaves")));
        itemNames.add(new ItemName(Material.LEAVES, 3, langConfig.getString("tile.leaves.jungle.name", "Jungle Leaves")));
        itemNames.add(new ItemName(Material.SPONGE, langConfig.getString("tile.sponge.dry.name", "Sponge")));
        itemNames.add(new ItemName(Material.SPONGE, 1, langConfig.getString("tile.sponge.wet.name", "Wet Sponge")));
        itemNames.add(new ItemName(Material.GLASS, langConfig.getString("tile.glass.name", "Glass")));
        itemNames.add(new ItemName(Material.LAPIS_ORE, langConfig.getString("tile.oreLapis.name", "Lapis Lazuli Ore")));
        itemNames.add(new ItemName(Material.LAPIS_BLOCK, langConfig.getString("tile.blockLapis.name", "Lapis Lazuli Block")));
        itemNames.add(new ItemName(Material.DISPENSER, langConfig.getString("tile.dispenser.name", "Dispenser")));
        itemNames.add(new ItemName(Material.SANDSTONE, langConfig.getString("tile.sandStone.default.name", "Sandstone")));
        itemNames.add(new ItemName(Material.SANDSTONE, 1, langConfig.getString("tile.sandStone.chiseled.name", "Chiseled Sandstone")));
        itemNames.add(new ItemName(Material.SANDSTONE, 2, langConfig.getString("tile.sandStone.smooth.name", "Smooth Sandstone")));
        itemNames.add(new ItemName(Material.NOTE_BLOCK, langConfig.getString("tile.musicBlock.name", "Note Block")));
        itemNames.add(new ItemName(Material.BED, langConfig.getString("tile.bed.name", "Bed")));
        itemNames.add(new ItemName(Material.POWERED_RAIL, langConfig.getString("tile.goldenRail.name", "Powered Rail")));
        itemNames.add(new ItemName(Material.DETECTOR_RAIL, langConfig.getString("tile.detectorRail.name", "Detector Rail")));
        itemNames.add(new ItemName(Material.PISTON_STICKY_BASE, langConfig.getString("tile.pistonStickyBase.name", "Sticky Piston")));
        itemNames.add(new ItemName(Material.WEB, langConfig.getString("tile.web.name", "Web")));
        itemNames.add(new ItemName(Material.LONG_GRASS, langConfig.getString("tile.tallgrass.shrub.name", "Shrub")));
        itemNames.add(new ItemName(Material.LONG_GRASS, 1, langConfig.getString("tile.tallgrass.grass.name", "Grass")));
        itemNames.add(new ItemName(Material.LONG_GRASS, 2, langConfig.getString("tile.tallgrass.fern.name", "Fern")));
        itemNames.add(new ItemName(Material.DEAD_BUSH, langConfig.getString("tile.deadbush.name", "Dead Bush")));
        itemNames.add(new ItemName(Material.PISTON_BASE, langConfig.getString("tile.pistonBase.name", "Piston")));
        itemNames.add(new ItemName(Material.WOOL, langConfig.getString("tile.cloth.white.name", "White Wool")));
        itemNames.add(new ItemName(Material.WOOL, 1, langConfig.getString("tile.cloth.orange.name", "Orange Wool")));
        itemNames.add(new ItemName(Material.WOOL, 2, langConfig.getString("tile.cloth.magenta.name", "Magenta Wool")));
        itemNames.add(new ItemName(Material.WOOL, 3, langConfig.getString("tile.cloth.lightBlue.name", "Light Blue Wool")));
        itemNames.add(new ItemName(Material.WOOL, 4, langConfig.getString("tile.cloth.yellow.name", "Yellow Wool")));
        itemNames.add(new ItemName(Material.WOOL, 5, langConfig.getString("tile.cloth.lime.name", "Lime Wool")));
        itemNames.add(new ItemName(Material.WOOL, 6, langConfig.getString("tile.cloth.pink.name", "Pink Wool")));
        itemNames.add(new ItemName(Material.WOOL, 7, langConfig.getString("tile.cloth.gray.name", "Gray Wool")));
        itemNames.add(new ItemName(Material.WOOL, 8, langConfig.getString("tile.cloth.silver.name", "Light Gray Wool")));
        itemNames.add(new ItemName(Material.WOOL, 9, langConfig.getString("tile.cloth.cyan.name", "Cyan Wool")));
        itemNames.add(new ItemName(Material.WOOL, 10, langConfig.getString("tile.cloth.purple.name", "Purple Wool")));
        itemNames.add(new ItemName(Material.WOOL, 11, langConfig.getString("tile.cloth.blue.name", "Blue Wool")));
        itemNames.add(new ItemName(Material.WOOL, 12, langConfig.getString("tile.cloth.brown.name", "Brown Wool")));
        itemNames.add(new ItemName(Material.WOOL, 13, langConfig.getString("tile.cloth.green.name", "Green Wool")));
        itemNames.add(new ItemName(Material.WOOL, 14, langConfig.getString("tile.cloth.red.name", "Red Wool")));
        itemNames.add(new ItemName(Material.WOOL, 15, langConfig.getString("tile.cloth.black.name", "Black Wool")));
        itemNames.add(new ItemName(Material.YELLOW_FLOWER, langConfig.getString("tile.flower1.dandelion.name", "Dandelion")));
        itemNames.add(new ItemName(Material.RED_ROSE, langConfig.getString("tile.flower2.poppy.name", "Poppy")));
        itemNames.add(new ItemName(Material.RED_ROSE, 1, langConfig.getString("tile.flower2.blueOrchid.name", "Blue Orchid")));
        itemNames.add(new ItemName(Material.RED_ROSE, 2, langConfig.getString("tile.flower2.allium.name", "Allium")));
        itemNames.add(new ItemName(Material.RED_ROSE, 3, langConfig.getString("tile.flower2.houstonia.name", "Azure Bluet")));
        itemNames.add(new ItemName(Material.RED_ROSE, 4, langConfig.getString("tile.flower2.tulipRed.name", "Red Tulip")));
        itemNames.add(new ItemName(Material.RED_ROSE, 5, langConfig.getString("tile.flower2.tulipOrange.name", "Orange Tulip")));
        itemNames.add(new ItemName(Material.RED_ROSE, 6, langConfig.getString("tile.flower2.tulipWhite.name", "White Tulip")));
        itemNames.add(new ItemName(Material.RED_ROSE, 7, langConfig.getString("tile.flower2.tulipPink.name", "Pink Tulip")));
        itemNames.add(new ItemName(Material.RED_ROSE, 8, langConfig.getString("tile.flower2.oxeyeDaisy.name", "Oxeye Daisy")));
        itemNames.add(new ItemName(Material.BROWN_MUSHROOM, langConfig.getString("tile.mushroom.name", "Mushroom")));
        itemNames.add(new ItemName(Material.RED_MUSHROOM, langConfig.getString("tile.mushroom.name", "Mushroom")));
        itemNames.add(new ItemName(Material.GOLD_BLOCK, langConfig.getString("tile.blockGold.name", "Block of Gold")));
        itemNames.add(new ItemName(Material.IRON_BLOCK, langConfig.getString("tile.blockIron.name", "Block of Iron")));
        itemNames.add(new ItemName(Material.STEP, langConfig.getString("tile.stoneSlab.stone.name", "Stone Slab")));
        itemNames.add(new ItemName(Material.STEP, 1, langConfig.getString("tile.stoneSlab.sand.name", "Sandstone Slab")));
        itemNames.add(new ItemName(Material.STEP, 2, langConfig.getString("tile.stoneSlab.wood.name", "Wooden Slab")));
        itemNames.add(new ItemName(Material.STEP, 3, langConfig.getString("tile.stoneSlab.cobble.name", "Cobblestone Slab")));
        itemNames.add(new ItemName(Material.STEP, 4, langConfig.getString("tile.stoneSlab.brick.name", "Brick Slab")));
        itemNames.add(new ItemName(Material.STEP, 5, langConfig.getString("tile.stoneSlab.smoothStoneBrick.name", "Stone Brick Slab")));
        itemNames.add(new ItemName(Material.STEP, 6, langConfig.getString("tile.stoneSlab.netherBrick.name", "Nether Brick Slab")));
        itemNames.add(new ItemName(Material.STEP, 7, langConfig.getString("tile.stoneSlab.quartz.name", "Quartz Slab")));
        itemNames.add(new ItemName(Material.BRICK, langConfig.getString("tile.brick.name", "Brick")));
        itemNames.add(new ItemName(Material.TNT, langConfig.getString("tile.tnt.name", "TNT")));
        itemNames.add(new ItemName(Material.BOOKSHELF, langConfig.getString("tile.bookshelf.name", "Bookshelf")));
        itemNames.add(new ItemName(Material.MOSSY_COBBLESTONE, langConfig.getString("tile.stoneMoss.name", "Moss Stone")));
        itemNames.add(new ItemName(Material.OBSIDIAN, langConfig.getString("tile.obsidian.name", "Obsidian")));
        itemNames.add(new ItemName(Material.TORCH, langConfig.getString("tile.torch.name", "Torch")));
        itemNames.add(new ItemName(Material.FIRE, langConfig.getString("tile.fire.name", "Fire")));
        itemNames.add(new ItemName(Material.MOB_SPAWNER, langConfig.getString("tile.mobSpawner.name", "Mob Spawner")));
        itemNames.add(new ItemName(Material.WOOD_STAIRS, langConfig.getString("tile.stairsWood.name", "Oak Wood Stairs")));
        itemNames.add(new ItemName(Material.CHEST, langConfig.getString("tile.chest.name", "Chest")));
        itemNames.add(new ItemName(Material.DIAMOND_ORE, langConfig.getString("tile.oreDiamond.name", "Diamond Ore")));
        itemNames.add(new ItemName(Material.DIAMOND_BLOCK, langConfig.getString("tile.blockDiamond.name", "Block of Diamond")));
        itemNames.add(new ItemName(Material.WORKBENCH, langConfig.getString("tile.workbench.name", "Crafting Table")));
        itemNames.add(new ItemName(Material.SOIL, langConfig.getString("tile.farmland.name", "Farmland")));
        itemNames.add(new ItemName(Material.FURNACE, langConfig.getString("tile.furnace.name", "Furnace")));
        itemNames.add(new ItemName(Material.LADDER, langConfig.getString("tile.ladder.name", "Ladder")));
        itemNames.add(new ItemName(Material.RAILS, langConfig.getString("tile.rail.name", "Rail")));
        itemNames.add(new ItemName(Material.COBBLESTONE_STAIRS, langConfig.getString("tile.stairsStone.name", "Stone Stairs")));
        itemNames.add(new ItemName(Material.LEVER, langConfig.getString("tile.lever.name", "Lever")));
        itemNames.add(new ItemName(Material.STONE_PLATE, langConfig.getString("tile.pressurePlateStone.name", "Stone Pressure Plate")));
        itemNames.add(new ItemName(Material.WOOD_PLATE, langConfig.getString("tile.pressurePlateWood.name", "Wooden Pressure Plate")));
        itemNames.add(new ItemName(Material.REDSTONE_ORE, langConfig.getString("tile.oreRedstone.name", "Redstone Ore")));
        itemNames.add(new ItemName(Material.REDSTONE_TORCH_ON, langConfig.getString("tile.notGate.name", "Redstone Torch")));
        itemNames.add(new ItemName(Material.SNOW, langConfig.getString("tile.snow.name", "Snow")));
        itemNames.add(new ItemName(Material.ICE, langConfig.getString("tile.ice.name", "Ice")));
        itemNames.add(new ItemName(Material.SNOW_BLOCK, langConfig.getString("tile.snow.name", "Snow")));
        itemNames.add(new ItemName(Material.CACTUS, langConfig.getString("tile.cactus.name", "Cactus")));
        itemNames.add(new ItemName(Material.CLAY, langConfig.getString("tile.clay.name", "Clay")));
        itemNames.add(new ItemName(Material.JUKEBOX, langConfig.getString("tile.jukebox.name", "Jukebox")));
        itemNames.add(new ItemName(Material.FENCE, langConfig.getString("tile.fence.name", "Oak Fence")));
        itemNames.add(new ItemName(Material.PUMPKIN, langConfig.getString("tile.pumpkin.name", "Pumpkin")));
        itemNames.add(new ItemName(Material.NETHERRACK, langConfig.getString("tile.hellrock.name", "Netherrack")));
        itemNames.add(new ItemName(Material.SOUL_SAND, langConfig.getString("tile.hellsand.name", "Soul Sand")));
        itemNames.add(new ItemName(Material.GLOWSTONE, langConfig.getString("tile.lightgem.name", "Glowstone")));
        itemNames.add(new ItemName(Material.PORTAL, langConfig.getString("tile.portal.name", "Portal")));
        itemNames.add(new ItemName(Material.JACK_O_LANTERN, langConfig.getString("tile.litpumpkin.name", "Jack o'Lantern")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, langConfig.getString("tile.stainedGlass.white.name", "White Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 1, langConfig.getString("tile.stainedGlass.orange.name", "Orange Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 2, langConfig.getString("tile.stainedGlass.magenta.name", "Magenta Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 3, langConfig.getString("tile.stainedGlass.lightBlue.name", "Light Blue Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 4, langConfig.getString("tile.stainedGlass.yellow.name", "Yellow Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 5, langConfig.getString("tile.stainedGlass.lime.name", "Lime Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 6, langConfig.getString("tile.stainedGlass.pink.name", "Pink Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 7, langConfig.getString("tile.stainedGlass.gray.name", "Gray Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 8, langConfig.getString("tile.stainedGlass.silver.name", "Light Gray Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 9, langConfig.getString("tile.stainedGlass.cyan.name", "Cyan Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 10, langConfig.getString("tile.stainedGlass.purple.name", "Purple Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 11, langConfig.getString("tile.stainedGlass.blue.name", "Blue Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 12, langConfig.getString("tile.stainedGlass.brown.name", "Brown Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 13, langConfig.getString("tile.stainedGlass.green.name", "Green Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 14, langConfig.getString("tile.stainedGlass.red.name", "Red Stained Glass")));
        itemNames.add(new ItemName(Material.STAINED_GLASS, 15, langConfig.getString("tile.stainedGlass.black.name", "Black Stained Glass")));
        itemNames.add(new ItemName(Material.TRAP_DOOR, langConfig.getString("tile.trapdoor.name", "Wooden Trapdoor")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, langConfig.getString("tile.monsterStoneEgg.stone.name", "Stone Monster Egg")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, 1, langConfig.getString("tile.monsterStoneEgg.cobble.name", "Cobblestone Monster Egg")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, 2, langConfig.getString("tile.monsterStoneEgg.brick.name", "Stone Brick Monster Egg")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, 3, langConfig.getString("tile.monsterStoneEgg.mossybrick.name", "Mossy Stone Brick Monster Egg")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, 4, langConfig.getString("tile.monsterStoneEgg.crackedbrick.name", "Cracked Stone Brick Monster Egg")));
        itemNames.add(new ItemName(Material.MONSTER_EGGS, 5, langConfig.getString("tile.monsterStoneEgg.chiseledbrick.name", "Chiseled Stone Brick Monster Egg")));
        itemNames.add(new ItemName(Material.SMOOTH_BRICK, langConfig.getString("tile.stonebricksmooth.default.name", "Stone Bricks")));
        itemNames.add(new ItemName(Material.SMOOTH_BRICK, 1, langConfig.getString("tile.stonebricksmooth.mossy.name", "Mossy Stone Bricks")));
        itemNames.add(new ItemName(Material.SMOOTH_BRICK, 2, langConfig.getString("tile.stonebricksmooth.cracked.name", "Cracked Stone Bricks")));
        itemNames.add(new ItemName(Material.SMOOTH_BRICK, 3, langConfig.getString("tile.stonebricksmooth.chiseled.name", "Chiseled Stone Bricks")));
        itemNames.add(new ItemName(Material.HUGE_MUSHROOM_1, langConfig.getString("tile.mushroom.name", "Mushroom")));
        itemNames.add(new ItemName(Material.HUGE_MUSHROOM_2, langConfig.getString("tile.mushroom.name", "Mushroom")));
        itemNames.add(new ItemName(Material.IRON_FENCE, langConfig.getString("tile.fenceIron.name", "Iron Bars")));
        itemNames.add(new ItemName(Material.THIN_GLASS, langConfig.getString("tile.thinGlass.name", "Glass Pane")));
        itemNames.add(new ItemName(Material.MELON_BLOCK, langConfig.getString("tile.melon.name", "Melon")));
        itemNames.add(new ItemName(Material.VINE, langConfig.getString("tile.vine.name", "Vines")));
        itemNames.add(new ItemName(Material.FENCE_GATE, langConfig.getString("tile.fenceGate.name", "Oak Fence Gate")));
        itemNames.add(new ItemName(Material.BRICK_STAIRS, langConfig.getString("tile.stairsBrick.name", "Brick Stairs")));
        itemNames.add(new ItemName(Material.SMOOTH_STAIRS, langConfig.getString("tile.stairsStoneBrickSmooth.name", "Stone Brick Stairs")));
        itemNames.add(new ItemName(Material.MYCEL, langConfig.getString("tile.mycel.name", "Mycelium")));
        itemNames.add(new ItemName(Material.WATER_LILY, langConfig.getString("tile.waterlily.name", "Lily Pad")));
        itemNames.add(new ItemName(Material.NETHER_BRICK, langConfig.getString("tile.netherBrick.name", "Nether Brick")));
        itemNames.add(new ItemName(Material.NETHER_FENCE, langConfig.getString("tile.netherFence.name", "Nether Brick Fence")));
        itemNames.add(new ItemName(Material.NETHER_BRICK_STAIRS, langConfig.getString("tile.stairsNetherBrick.name", "Nether Brick Stairs")));
        itemNames.add(new ItemName(Material.ENCHANTMENT_TABLE, langConfig.getString("tile.enchantmentTable.name", "Enchantment Table")));
        itemNames.add(new ItemName(Material.ENDER_PORTAL_FRAME, langConfig.getString("tile.endPortalFrame.name", "End Portal Frame")));
        itemNames.add(new ItemName(Material.ENDER_STONE, langConfig.getString("tile.whiteStone.name", "End Stone")));
        itemNames.add(new ItemName(Material.DRAGON_EGG, langConfig.getString("tile.dragonEgg.name", "Dragon Egg")));
        itemNames.add(new ItemName(Material.REDSTONE_LAMP_OFF, langConfig.getString("tile.redstoneLight.name", "Redstone Lamp")));
        itemNames.add(new ItemName(Material.WOOD_STEP, langConfig.getString("tile.woodSlab.oak.name", "Oak Wood Slab")));
        itemNames.add(new ItemName(Material.WOOD_STEP, 1, langConfig.getString("tile.woodSlab.spruce.name", "Spruce Wood Slab")));
        itemNames.add(new ItemName(Material.WOOD_STEP, 2, langConfig.getString("tile.woodSlab.birch.name", "Birch Wood Slab")));
        itemNames.add(new ItemName(Material.WOOD_STEP, 3, langConfig.getString("tile.woodSlab.jungle.name", "Jungle Wood Slab")));
        itemNames.add(new ItemName(Material.WOOD_STEP, 4, langConfig.getString("tile.woodSlab.acacia.name", "Acacia Wood Slab")));
        itemNames.add(new ItemName(Material.WOOD_STEP, 5, langConfig.getString("tile.woodSlab.big_oak.name", "Dark Oak Wood Slab")));
        itemNames.add(new ItemName(Material.SANDSTONE_STAIRS, langConfig.getString("tile.stairsSandStone.name", "Mycelium")));
        itemNames.add(new ItemName(Material.EMERALD_ORE, langConfig.getString("tile.oreEmerald.name", "Emerald Ore")));
        itemNames.add(new ItemName(Material.ENDER_CHEST, langConfig.getString("tile.enderChest.name", "Ender Chest")));
        itemNames.add(new ItemName(Material.TRIPWIRE_HOOK, langConfig.getString("tile.tripWireSource.name", "Tripwire Hook")));
        itemNames.add(new ItemName(Material.EMERALD_BLOCK, langConfig.getString("tile.blockEmerald.name", "Block of Emerald")));
        itemNames.add(new ItemName(Material.SPRUCE_WOOD_STAIRS, langConfig.getString("tile.stairsWoodSpruce.name", "Spruce Wood Stairs")));
        itemNames.add(new ItemName(Material.BIRCH_WOOD_STAIRS, langConfig.getString("tile.stairsWoodBirch.name", "Birch Wood Stairs")));
        itemNames.add(new ItemName(Material.JUNGLE_WOOD_STAIRS, langConfig.getString("tile.stairsWoodJungle.name", "Jungle Wood Stairs")));
        itemNames.add(new ItemName(Material.COMMAND, langConfig.getString("tile.commandBlock.name", "Command Block")));
        itemNames.add(new ItemName(Material.BEACON, langConfig.getString("tile.beacon.name", "Beacon")));
        itemNames.add(new ItemName(Material.COBBLE_WALL, langConfig.getString("tile.cobbleWall.normal.name", "Cobblestone Wall")));
        itemNames.add(new ItemName(Material.COBBLE_WALL, 1, langConfig.getString("tile.cobbleWall.mossy.name", "Mossy Cobblestone Wall")));
        itemNames.add(new ItemName(Material.WOOD_BUTTON, langConfig.getString("tile.button.name", "Button")));
        itemNames.add(new ItemName(Material.ANVIL, langConfig.getString("tile.anvil.intact.name", "Anvil")));
        itemNames.add(new ItemName(Material.ANVIL, 1, langConfig.getString("tile.anvil.slightlyDamaged.name", "Slightly Damaged Anvil")));
        itemNames.add(new ItemName(Material.ANVIL, 2, langConfig.getString("tile.anvil.veryDamaged.name", "Very Damaged Anvil")));
        itemNames.add(new ItemName(Material.TRAPPED_CHEST, langConfig.getString("tile.chestTrap.name", "Trapped Chest")));
        itemNames.add(new ItemName(Material.GOLD_PLATE, langConfig.getString("tile.weightedPlate_light.name", "Weighted Pressure Plate (Light)")));
        itemNames.add(new ItemName(Material.IRON_PLATE, langConfig.getString("tile.weightedPlate_heavy.name", "Weighted Pressure Plate (Heavy)")));
        itemNames.add(new ItemName(Material.DAYLIGHT_DETECTOR, langConfig.getString("tile.daylightDetector.name", "Daylight Sensor")));
        itemNames.add(new ItemName(Material.REDSTONE_BLOCK, langConfig.getString("tile.blockRedstone.name", "Block of Redstone")));
        itemNames.add(new ItemName(Material.QUARTZ_ORE, langConfig.getString("tile.netherquartz.name", "Nether Quartz Ore")));
        itemNames.add(new ItemName(Material.HOPPER, langConfig.getString("tile.hopper.name", "Hopper")));
        itemNames.add(new ItemName(Material.QUARTZ_BLOCK, langConfig.getString("tile.quartzBlock.default.name", "Block of Quartz")));
        itemNames.add(new ItemName(Material.QUARTZ_BLOCK, langConfig.getString("tile.quartzBlock.chiseled.name", "Chiseled Quartz Block")));
        itemNames.add(new ItemName(Material.QUARTZ_BLOCK, langConfig.getString("tile.quartzBlock.lines.name", "Pillar Quartz Block")));
        itemNames.add(new ItemName(Material.QUARTZ_STAIRS, langConfig.getString("tile.stairsQuartz.name", "Quartz Stairs")));
        itemNames.add(new ItemName(Material.ACTIVATOR_RAIL, langConfig.getString("tile.activatorRail.name", "Activator Rail")));
        itemNames.add(new ItemName(Material.DROPPER, langConfig.getString("tile.dropper.name", "Dropper")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, langConfig.getString("tile.clayHardenedStained.white.name", "White Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 1, langConfig.getString("tile.clayHardenedStained.orange.name", "Orange Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 2, langConfig.getString("tile.clayHardenedStained.magenta.name", "Magenta Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 3, langConfig.getString("tile.clayHardenedStained.lightBlue.name", "Light Blue Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 4, langConfig.getString("tile.clayHardenedStained.yellow.name", "Yellow Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 5, langConfig.getString("tile.clayHardenedStained.lime.name", "Lime Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 6, langConfig.getString("tile.clayHardenedStained.pink.name", "Pink Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 7, langConfig.getString("tile.clayHardenedStained.gray.name", "Gray Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 8, langConfig.getString("tile.clayHardenedStained.silver.name", "Light Gray Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 9, langConfig.getString("tile.clayHardenedStained.cyan.name", "Cyan Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 10, langConfig.getString("tile.clayHardenedStained.purple.name", "Purple Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 11, langConfig.getString("tile.clayHardenedStained.blue.name", "Blue Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 12, langConfig.getString("tile.clayHardenedStained.brown.name", "Brown Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 13, langConfig.getString("tile.clayHardenedStained.green.name", "Green Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 14, langConfig.getString("tile.clayHardenedStained.red.name", "Red Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_CLAY, 15, langConfig.getString("tile.clayHardenedStained.black.name", "Black Hardened Clay")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, langConfig.getString("tile.thinStainedGlass.white.name", "White Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 1, langConfig.getString("tile.thinStainedGlass.orange.name", "Orange Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 2, langConfig.getString("tile.thinStainedGlass.magenta.name", "Magenta Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 3, langConfig.getString("tile.thinStainedGlass.lightBlue.name", "Light Blue Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 4, langConfig.getString("tile.thinStainedGlass.yellow.name", "Yellow Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 5, langConfig.getString("tile.thinStainedGlass.lime.name", "Lime Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 6, langConfig.getString("tile.thinStainedGlass.pink.name", "Pink Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 7, langConfig.getString("tile.thinStainedGlass.gray.name", "Gray Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 8, langConfig.getString("tile.thinStainedGlass.silver.name", "Light Gray Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 9, langConfig.getString("tile.thinStainedGlass.cyan.name", "Cyan Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 10, langConfig.getString("tile.thinStainedGlass.purple.name", "Purple Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 11, langConfig.getString("tile.thinStainedGlass.blue.name", "Blue Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 12, langConfig.getString("tile.thinStainedGlass.brown.name", "Brown Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 13, langConfig.getString("tile.thinStainedGlass.green.name", "Green Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 14, langConfig.getString("tile.thinStainedGlass.red.name", "Red Stained Glass Pane")));
        itemNames.add(new ItemName(Material.STAINED_GLASS_PANE, 15, langConfig.getString("tile.thinStainedGlass.black.name", "Black Stained Glass Pane")));
        itemNames.add(new ItemName(Material.LEAVES_2, langConfig.getString("tile.leaves.acacia.name", "Acacia Leaves")));
        itemNames.add(new ItemName(Material.LEAVES_2, 1, langConfig.getString("tile.leaves.big_oak.name", "Dark Oak Leaves")));
        itemNames.add(new ItemName(Material.LOG_2, langConfig.getString("tile.log.acacia.name", "Acacia Wood")));
        itemNames.add(new ItemName(Material.LOG_2, 1, langConfig.getString("tile.log.big_oak.name", "Dark Oak Wood")));
        itemNames.add(new ItemName(Material.ACACIA_STAIRS, langConfig.getString("tile.stairsWoodAcacia.name", "Acacia Wood Stairs")));
        itemNames.add(new ItemName(Material.DARK_OAK_STAIRS, langConfig.getString("tile.stairsWoodDarkOak.name", "Dark Oak Wood Stairs")));
        itemNames.add(new ItemName(Material.SLIME_BLOCK, langConfig.getString("tile.slime.name", "Slime Block")));
        itemNames.add(new ItemName(Material.BARRIER, langConfig.getString("tile.barrier.name", "Barrier")));
        itemNames.add(new ItemName(Material.IRON_TRAPDOOR, langConfig.getString("tile.ironTrapdoor.name", "Iron Trapdoor")));
        itemNames.add(new ItemName(Material.PRISMARINE, langConfig.getString("tile.prismarine.rough.name", "Prismarine")));
        itemNames.add(new ItemName(Material.PRISMARINE, 1, langConfig.getString("tile.prismarine.bricks.name", "Prismarine Bricks")));
        itemNames.add(new ItemName(Material.PRISMARINE, 2, langConfig.getString("tile.prismarine.dark.name", "Dark Prismarine")));
        itemNames.add(new ItemName(Material.SEA_LANTERN, langConfig.getString("tile.seaLantern.name", "Sea Lantern")));
        itemNames.add(new ItemName(Material.HAY_BLOCK, langConfig.getString("tile.hayBlock.name", "Hay Bale")));
        itemNames.add(new ItemName(Material.CARPET, langConfig.getString("tile.woolCarpet.white.name", "White Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 1, langConfig.getString("tile.woolCarpet.orange.name", "Orange Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 2, langConfig.getString("tile.woolCarpet.magenta.name", "Magenta Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 3, langConfig.getString("tile.woolCarpet.lightBlue.name", "Light Blue Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 4, langConfig.getString("tile.woolCarpet.yellow.name", "Yellow Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 5, langConfig.getString("tile.woolCarpet.lime.name", "Lime Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 6, langConfig.getString("tile.woolCarpet.pink.name", "Pink Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 7, langConfig.getString("tile.woolCarpet.gray.name", "Gray Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 8, langConfig.getString("tile.woolCarpet.silver.name", "Light Gray Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 9, langConfig.getString("tile.woolCarpet.cyan.name", "Cyan Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 10, langConfig.getString("tile.woolCarpet.purple.name", "Purple Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 11, langConfig.getString("tile.woolCarpet.blue.name", "Blue Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 12, langConfig.getString("tile.woolCarpet.brown.name", "Brown Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 13, langConfig.getString("tile.woolCarpet.green.name", "Green Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 14, langConfig.getString("tile.woolCarpet.red.name", "Red Carpet")));
        itemNames.add(new ItemName(Material.CARPET, 15, langConfig.getString("tile.woolCarpet.black.name", "Black Carpet")));
        itemNames.add(new ItemName(Material.HARD_CLAY, langConfig.getString("tile.clayHardened.name", "Hardened Clay")));
        itemNames.add(new ItemName(Material.COAL_BLOCK, langConfig.getString("tile.blockCoal.name", "Block of Coal")));
        itemNames.add(new ItemName(Material.PACKED_ICE, langConfig.getString("tile.icePacked.name", "Packed Ice")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, langConfig.getString("tile.doublePlant.sunflower.name", "Sunflower")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, 1, langConfig.getString("tile.doublePlant.syringa.name", "Lilac")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, 2, langConfig.getString("tile.doublePlant.grass.name", "Double Tallgrass")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, 3, langConfig.getString("tile.doublePlant.fern.name", "Large Fern")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, 4, langConfig.getString("tile.doublePlant.rose.name", "Rose Bush")));
        itemNames.add(new ItemName(Material.DOUBLE_PLANT, 5, langConfig.getString("tile.doublePlant.paeonia.name", "Peony")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE, langConfig.getString("tile.redSandStone.default.name", "Red Sandstone")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE, 1, langConfig.getString("tile.redSandStone.chiseled.name", "Chiseled Red Sandstone")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE, 2, langConfig.getString("tile.redSandStone.smooth.name", "Smooth Red Sandstone")));
        itemNames.add(new ItemName(Material.RED_SANDSTONE_STAIRS, langConfig.getString("tile.stairsRedSandStone.name", "Red Sandstone Stairs")));
        itemNames.add(new ItemName(Material.STONE_SLAB2, langConfig.getString("tile.stoneSlab2.red_sandstone.name", "Red Sandstone Slab")));
        itemNames.add(new ItemName(Material.SPRUCE_FENCE_GATE, langConfig.getString("tile.spruceFenceGate.name", "Spruce Fence Gate")));
        itemNames.add(new ItemName(Material.BIRCH_FENCE_GATE, langConfig.getString("tile.birchFenceGate.name", "Birch Fence Gate")));
        itemNames.add(new ItemName(Material.JUNGLE_FENCE_GATE, langConfig.getString("tile.jungleFenceGate.name", "Jungle Fence Gate")));
        itemNames.add(new ItemName(Material.DARK_OAK_FENCE_GATE, langConfig.getString("tile.darkOakFenceGate.name", "Dark Oak Fence Gate")));
        itemNames.add(new ItemName(Material.ACACIA_FENCE_GATE, langConfig.getString("tile.acaciaFenceGate.name", "Acacia Fence Gate")));
        itemNames.add(new ItemName(Material.SPRUCE_FENCE, langConfig.getString("tile.spruceFence.name", "Spruce Fence")));
        itemNames.add(new ItemName(Material.BIRCH_FENCE, langConfig.getString("tile.birchFence.name", "Birch Fence")));
        itemNames.add(new ItemName(Material.JUNGLE_FENCE, langConfig.getString("tile.jungleFence.name", "Jungle Fence")));
        itemNames.add(new ItemName(Material.DARK_OAK_FENCE, langConfig.getString("tile.darkOakFence.name", "Dark Oak Fence")));
        itemNames.add(new ItemName(Material.ACACIA_FENCE, langConfig.getString("tile.acaciaFence.name", "Acacia Fence")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Block Names of 1.9
            itemNames.add(new ItemName(Material.END_ROD, langConfig.getString("tile.endRod.name", "End Rod")));
            itemNames.add(new ItemName(Material.CHORUS_PLANT, langConfig.getString("tile.chorusPlant.name", "Chorus Plant")));
            itemNames.add(new ItemName(Material.CHORUS_FLOWER, langConfig.getString("tile.chorusFlower.name", "Chorus Flower")));
            itemNames.add(new ItemName(Material.PURPUR_BLOCK, langConfig.getString("tile.purpurBlock.name", "Purpur Block")));
            itemNames.add(new ItemName(Material.PURPUR_PILLAR, langConfig.getString("tile.purpurPillar.name", "Purpur Pillar")));
            itemNames.add(new ItemName(Material.PURPUR_STAIRS, langConfig.getString("tile.stairsPurpur.name", "Purpur Stairs")));
            itemNames.add(new ItemName(Material.PURPUR_SLAB, langConfig.getString("tile.purpurSlab.name", "Purpur Slab")));
            itemNames.add(new ItemName(Material.END_BRICKS, langConfig.getString("tile.endBricks.name", "End Stone Bricks")));
            itemNames.add(new ItemName(Material.GRASS_PATH, langConfig.getString("tile.grassPath.name", "Grass Path")));
            itemNames.add(new ItemName(Material.COMMAND_REPEATING, langConfig.getString("tile.repeatingCommandBlock.name", "Repeating Command Block")));
            itemNames.add(new ItemName(Material.COMMAND_CHAIN, langConfig.getString("tile.chainCommandBlock.name", "Chain Command Block")));
            itemNames.add(new ItemName(Material.STRUCTURE_BLOCK, langConfig.getString("tile.structureBlock.name", "Structure Block")));
        }

        if (Utils.getMajorVersion() >= 10) {
            // Add Block Names of 1.10
            itemNames.add(new ItemName(Material.MAGMA, langConfig.getString("tile.magma.name", "Magma Block")));
            itemNames.add(new ItemName(Material.NETHER_WART_BLOCK, langConfig.getString("tile.netherWartBlock.name", "Nether Wart Block")));
            itemNames.add(new ItemName(Material.RED_NETHER_BRICK, langConfig.getString("tile.redNetherBrick.name", "Red Nether Brick")));
            itemNames.add(new ItemName(Material.BONE_BLOCK, langConfig.getString("tile.boneBlock.name", "Bone Block")));
            itemNames.add(new ItemName(Material.STRUCTURE_VOID, langConfig.getString("tile.structureVoid.name", "Structure Void")));
        }

        if (Utils.getMajorVersion() >= 11) {
            // Add Block Names of 1.11
            itemNames.add(new ItemName(Material.OBSERVER, langConfig.getString("tile.observer.name", "Observer")));
            itemNames.add(new ItemName(Material.WHITE_SHULKER_BOX, langConfig.getString("tile.shulkerBoxWhite.name", "White Shulker Box")));
            itemNames.add(new ItemName(Material.ORANGE_SHULKER_BOX, langConfig.getString("tile.shulkerBoxOrange.name", "Orange Shulker Box")));
            itemNames.add(new ItemName(Material.MAGENTA_SHULKER_BOX, langConfig.getString("tile.shulkerBoxMagenta.name", "Magenta Shulker Box")));
            itemNames.add(new ItemName(Material.LIGHT_BLUE_SHULKER_BOX, langConfig.getString("tile.shulkerBoxLightBlue.name", "Light Blue Shulker Box")));
            itemNames.add(new ItemName(Material.YELLOW_SHULKER_BOX, langConfig.getString("tile.shulkerBoxYellow.name", "Yellow Shulker Box")));
            itemNames.add(new ItemName(Material.LIME_SHULKER_BOX, langConfig.getString("tile.shulkerBoxLime.name", "Lime Shulker Box")));
            itemNames.add(new ItemName(Material.PINK_SHULKER_BOX, langConfig.getString("tile.shulkerBoxPink.name", "Pink Shulker Box")));
            itemNames.add(new ItemName(Material.GRAY_SHULKER_BOX, langConfig.getString("tile.shulkerBoxGray.name", "Gray Shulker Box")));
            itemNames.add(new ItemName(Material.SILVER_SHULKER_BOX, langConfig.getString("tile.shulkerBoxSilver.name", "Light Gray Shulker Box")));
            itemNames.add(new ItemName(Material.CYAN_SHULKER_BOX, langConfig.getString("tile.shulkerBoxCyan.name", "Cyan Shulker Box")));
            itemNames.add(new ItemName(Material.PURPLE_SHULKER_BOX, langConfig.getString("tile.shulkerBoxPurple.name", "Purple Shulker Box")));
            itemNames.add(new ItemName(Material.BLUE_SHULKER_BOX, langConfig.getString("tile.shulkerBoxBlue.name", "Blue Shulker Box")));
            itemNames.add(new ItemName(Material.BROWN_SHULKER_BOX, langConfig.getString("tile.shulkerBoxBrown.name", "Brown Shulker Box")));
            itemNames.add(new ItemName(Material.GREEN_SHULKER_BOX, langConfig.getString("tile.shulkerBoxGreen.name", "Green Shulker Box")));
            itemNames.add(new ItemName(Material.RED_SHULKER_BOX, langConfig.getString("tile.shulkerBoxRed.name", "Red Shulker Box")));
            itemNames.add(new ItemName(Material.BLACK_SHULKER_BOX, langConfig.getString("tile.shulkerBoxBlack.name", "Black Shulker Box")));
        }

        // Add Item Names
        itemNames.add(new ItemName(Material.IRON_SPADE, langConfig.getString("item.shovelIron.name", "Iron Shovel")));
        itemNames.add(new ItemName(Material.IRON_PICKAXE, langConfig.getString("item.pickaxeIron.name", "Iron Pickaxe")));
        itemNames.add(new ItemName(Material.IRON_AXE, langConfig.getString("item.hatchetIron.name", "Iron Axe")));
        itemNames.add(new ItemName(Material.FLINT_AND_STEEL, langConfig.getString("item.flintAndSteel.name", "Flint and Steel")));
        itemNames.add(new ItemName(Material.APPLE, langConfig.getString("item.apple.name", "Apple")));
        itemNames.add(new ItemName(Material.BOW, langConfig.getString("item.bow.name", "Bow")));
        itemNames.add(new ItemName(Material.ARROW, langConfig.getString("item.arrow.name", "Arrow")));
        itemNames.add(new ItemName(Material.COAL, langConfig.getString("item.coal.name", "Coal")));
        itemNames.add(new ItemName(Material.COAL, 1, langConfig.getString("item.charcoal.name", "Charcoal")));
        itemNames.add(new ItemName(Material.DIAMOND, langConfig.getString("item.diamond.name", "Diamond")));
        itemNames.add(new ItemName(Material.IRON_INGOT, langConfig.getString("item.ingotIron.name", "Iron Ingot")));
        itemNames.add(new ItemName(Material.GOLD_INGOT, langConfig.getString("item.ingotGold.name", "Gold Ingot")));
        itemNames.add(new ItemName(Material.IRON_SWORD, langConfig.getString("item.swordIron.name", "Iron Sword")));
        itemNames.add(new ItemName(Material.WOOD_SWORD, langConfig.getString("item.swordWood.name", "Wooden Sword")));
        itemNames.add(new ItemName(Material.WOOD_SPADE, langConfig.getString("item.shovelWood.name", "Wooden Shovel")));
        itemNames.add(new ItemName(Material.WOOD_PICKAXE, langConfig.getString("item.pickaxeWood.name", "Wooden Pickaxe")));
        itemNames.add(new ItemName(Material.WOOD_AXE, langConfig.getString("item.hatchetWood.name", "Wooden Axe")));
        itemNames.add(new ItemName(Material.STONE_SWORD, langConfig.getString("item.swordStone.name", "Stone Sword")));
        itemNames.add(new ItemName(Material.STONE_SPADE, langConfig.getString("item.shovelStone.name", "Stone Shovel")));
        itemNames.add(new ItemName(Material.STONE_PICKAXE, langConfig.getString("item.pickaxeStone.name", "Stone Pickaxe")));
        itemNames.add(new ItemName(Material.STONE_AXE, langConfig.getString("item.hatchetStone.name", "Stone Axe")));
        itemNames.add(new ItemName(Material.DIAMOND_SWORD, langConfig.getString("item.swordDiamond.name", "Diamond Sword")));
        itemNames.add(new ItemName(Material.DIAMOND_SPADE, langConfig.getString("item.shovelDiamond.name", "Diamond Shovel")));
        itemNames.add(new ItemName(Material.DIAMOND_PICKAXE, langConfig.getString("item.pickaxeDiamond.name", "Diamond Pickaxe")));
        itemNames.add(new ItemName(Material.DIAMOND_AXE, langConfig.getString("item.hatchetDiamond.name", "Diamond Axe")));
        itemNames.add(new ItemName(Material.STICK, langConfig.getString("item.stick.name", "Stick")));
        itemNames.add(new ItemName(Material.BOWL, langConfig.getString("item.bowl.name", "Bowl")));
        itemNames.add(new ItemName(Material.MUSHROOM_SOUP, langConfig.getString("item.mushroomStew.name", "Mushroom Stew")));
        itemNames.add(new ItemName(Material.GOLD_SWORD, langConfig.getString("item.swordGold.name", "Golden Sword")));
        itemNames.add(new ItemName(Material.GOLD_SPADE, langConfig.getString("item.shovelGold.name", "Golden Shovel")));
        itemNames.add(new ItemName(Material.GOLD_PICKAXE, langConfig.getString("item.pickaxeGold.name", "Golden Pickaxe")));
        itemNames.add(new ItemName(Material.GOLD_AXE, langConfig.getString("item.hatchetGold.name", "Golden Axe")));
        itemNames.add(new ItemName(Material.STRING, langConfig.getString("item.string.name", "String")));
        itemNames.add(new ItemName(Material.FEATHER, langConfig.getString("item.feather.name", "Feather")));
        itemNames.add(new ItemName(Material.SULPHUR, langConfig.getString("item.sulphur.name", "Gunpowder")));
        itemNames.add(new ItemName(Material.WOOD_HOE, langConfig.getString("item.hoeWood.name", "Wooden Hoe")));
        itemNames.add(new ItemName(Material.STONE_HOE, langConfig.getString("item.hoeStone.name", "Stone Hoe")));
        itemNames.add(new ItemName(Material.IRON_HOE, langConfig.getString("item.hoeIron.name", "Iron Hoe")));
        itemNames.add(new ItemName(Material.DIAMOND_HOE, langConfig.getString("item.hoeDiamond.name", "Diamond Hoe")));
        itemNames.add(new ItemName(Material.GOLD_HOE, langConfig.getString("item.hoeGold.name", "Golden Hoe")));
        itemNames.add(new ItemName(Material.SEEDS, langConfig.getString("item.seeds.name", "Seeds")));
        itemNames.add(new ItemName(Material.WHEAT, langConfig.getString("item.wheat.name", "Wheat")));
        itemNames.add(new ItemName(Material.BREAD, langConfig.getString("item.bread.name", "Bread")));
        itemNames.add(new ItemName(Material.LEATHER_HELMET, langConfig.getString("item.helmetCloth.name", "Leather Cap")));
        itemNames.add(new ItemName(Material.LEATHER_CHESTPLATE, langConfig.getString("item.chestplateCloth.name", "Leather Tunic")));
        itemNames.add(new ItemName(Material.LEATHER_LEGGINGS, langConfig.getString("item.leggingsCloth.name", "Leather Pants")));
        itemNames.add(new ItemName(Material.LEATHER_BOOTS, langConfig.getString("item.bootsCloth.name", "Leather Boots")));
        itemNames.add(new ItemName(Material.CHAINMAIL_HELMET, langConfig.getString("item.helmetChain.name", "Chain Helmet")));
        itemNames.add(new ItemName(Material.CHAINMAIL_CHESTPLATE, langConfig.getString("item.chestplateChain.name", "Chain Chestplate")));
        itemNames.add(new ItemName(Material.CHAINMAIL_LEGGINGS, langConfig.getString("item.leggingsChain.name", "Chain Leggings")));
        itemNames.add(new ItemName(Material.CHAINMAIL_BOOTS, langConfig.getString("item.bootsChain.name", "Chain Boots")));
        itemNames.add(new ItemName(Material.IRON_HELMET, langConfig.getString("item.helmetIron.name", "Iron Helmet")));
        itemNames.add(new ItemName(Material.IRON_CHESTPLATE, langConfig.getString("item.chestplateIron.name", "Iron Chestplate")));
        itemNames.add(new ItemName(Material.IRON_LEGGINGS, langConfig.getString("item.leggingsIron.name", "Iron Leggings")));
        itemNames.add(new ItemName(Material.IRON_BOOTS, langConfig.getString("item.bootsIron.name", "Iron Boots")));
        itemNames.add(new ItemName(Material.DIAMOND_HELMET, langConfig.getString("item.helmetDiamond.name", "Diamond Helmet")));
        itemNames.add(new ItemName(Material.DIAMOND_CHESTPLATE, langConfig.getString("item.chestplateDiamond.name", "Diamond Chestplate")));
        itemNames.add(new ItemName(Material.DIAMOND_LEGGINGS, langConfig.getString("item.leggingsDiamond.name", "Diamond Leggings")));
        itemNames.add(new ItemName(Material.DIAMOND_BOOTS, langConfig.getString("item.bootsDiamond.name", "Diamond Boots")));
        itemNames.add(new ItemName(Material.GOLD_HELMET, langConfig.getString("item.helmetGold.name", "Golden Helmet")));
        itemNames.add(new ItemName(Material.GOLD_CHESTPLATE, langConfig.getString("item.chestplateGold.name", "Golden Chestplate")));
        itemNames.add(new ItemName(Material.GOLD_LEGGINGS, langConfig.getString("item.leggingsGold.name", "Golden Leggings")));
        itemNames.add(new ItemName(Material.GOLD_BOOTS, langConfig.getString("item.bootsGold.name", "Golden Boots")));
        itemNames.add(new ItemName(Material.FLINT, langConfig.getString("item.flint.name", "Flint")));
        itemNames.add(new ItemName(Material.PORK, langConfig.getString("item.porkchopRaw.name", "Raw Porkchop")));
        itemNames.add(new ItemName(Material.GRILLED_PORK, langConfig.getString("item.porkchopCooked.name", "Cooked Porkchop")));
        itemNames.add(new ItemName(Material.PAINTING, langConfig.getString("item.painting.name", "Painting")));
        itemNames.add(new ItemName(Material.GOLDEN_APPLE, langConfig.getString("item.appleGold.name", "Golden Apple")));
        itemNames.add(new ItemName(Material.GOLDEN_APPLE, 1, langConfig.getString("item.appleGold.name", "Golden Apple")));
        itemNames.add(new ItemName(Material.SIGN, langConfig.getString("item.sign.name", "Sign")));
        itemNames.add(new ItemName(Material.WOOD_DOOR, langConfig.getString("item.doorOak.name", "Oak Door")));
        itemNames.add(new ItemName(Material.BUCKET, langConfig.getString("item.bucket.name", "Bucket")));
        itemNames.add(new ItemName(Material.WATER_BUCKET, langConfig.getString("item.bucketWater.name", "Water Bucket")));
        itemNames.add(new ItemName(Material.LAVA_BUCKET, langConfig.getString("item.bucketLava.name", "Lava Bucket")));
        itemNames.add(new ItemName(Material.MINECART, langConfig.getString("item.minecart.name", "Minecart")));
        itemNames.add(new ItemName(Material.SADDLE, langConfig.getString("item.saddle.name", "Saddle")));
        itemNames.add(new ItemName(Material.IRON_DOOR, langConfig.getString("item.doorIron.name", "Iron Door")));
        itemNames.add(new ItemName(Material.REDSTONE, langConfig.getString("item.redstone.name", "Redstone")));
        itemNames.add(new ItemName(Material.SNOW_BALL, langConfig.getString("item.snowball.name", "Snowball")));
        itemNames.add(new ItemName(Material.BOAT, langConfig.getString("item.boat.oak.name", "Oak Boat")));
        itemNames.add(new ItemName(Material.LEATHER, langConfig.getString("item.leather.name", "Leather")));
        itemNames.add(new ItemName(Material.MILK_BUCKET, langConfig.getString("item.milk.name", "Milk")));
        itemNames.add(new ItemName(Material.BRICK, langConfig.getString("item.brick.name", "Brick")));
        itemNames.add(new ItemName(Material.CLAY_BALL, langConfig.getString("item.clay.name", "Clay")));
        itemNames.add(new ItemName(Material.SUGAR_CANE, langConfig.getString("item.reeds.name", "Sugar Canes")));
        itemNames.add(new ItemName(Material.PAPER, langConfig.getString("item.paper.name", "Paper")));
        itemNames.add(new ItemName(Material.BOOK, langConfig.getString("item.book.name", "Book")));
        itemNames.add(new ItemName(Material.SLIME_BALL, langConfig.getString("item.slimeball.name", "Slimeball")));
        itemNames.add(new ItemName(Material.STORAGE_MINECART, langConfig.getString("item.minecartChest.name", "Minecart with Chest")));
        itemNames.add(new ItemName(Material.POWERED_MINECART, langConfig.getString("item.minecartFurnace.name", "Minecart with Furnace")));
        itemNames.add(new ItemName(Material.EGG, langConfig.getString("item.egg.name", "Egg")));
        itemNames.add(new ItemName(Material.COMPASS, langConfig.getString("item.compass.name", "Compass")));
        itemNames.add(new ItemName(Material.FISHING_ROD, langConfig.getString("item.fishingRod.name", "Fishing Rod")));
        itemNames.add(new ItemName(Material.WATCH, langConfig.getString("item.clock.name", "Clock")));
        itemNames.add(new ItemName(Material.GLOWSTONE_DUST, langConfig.getString("item.yellowDust.name", "Glowstone Dust")));
        itemNames.add(new ItemName(Material.RAW_FISH, langConfig.getString("item.fish.cod.raw.name", "Raw Fish")));
        itemNames.add(new ItemName(Material.RAW_FISH, 1, langConfig.getString("item.fish.salmon.raw.name", "Raw Salmon")));
        itemNames.add(new ItemName(Material.RAW_FISH, 2, langConfig.getString("item.fish.clownfish.raw.name", "Clownfish")));
        itemNames.add(new ItemName(Material.RAW_FISH, 3, langConfig.getString("item.fish.pufferfish.raw.name", "Pufferfish")));
        itemNames.add(new ItemName(Material.COOKED_FISH, langConfig.getString("item.fish.cod.cooked.name", "Cooked Fish")));
        itemNames.add(new ItemName(Material.COOKED_FISH, 1, langConfig.getString("item.fish.salmon.cooked.name", "Cooked Salmon")));
        itemNames.add(new ItemName(Material.INK_SACK, langConfig.getString("item.dyePowder.black.name", "Ink Sac")));
        itemNames.add(new ItemName(Material.INK_SACK, 1, langConfig.getString("item.dyePowder.red.name", "Rose Red")));
        itemNames.add(new ItemName(Material.INK_SACK, 2, langConfig.getString("item.dyePowder.green.name", "Cactus Green")));
        itemNames.add(new ItemName(Material.INK_SACK, 3, langConfig.getString("item.dyePowder.brown.name", "Cocoa Beans")));
        itemNames.add(new ItemName(Material.INK_SACK, 4, langConfig.getString("item.dyePowder.blue.name", "Lapis Lazuli")));
        itemNames.add(new ItemName(Material.INK_SACK, 5, langConfig.getString("item.dyePowder.purple.name", "Purple Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 6, langConfig.getString("item.dyePowder.cyan.name", "Cyan Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 7, langConfig.getString("item.dyePowder.silver.name", "Light Gray Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 8, langConfig.getString("item.dyePowder.gray.name", "Gray Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 9, langConfig.getString("item.dyePowder.pink.name", "Pink Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 10, langConfig.getString("item.dyePowder.lime.name", "Lime Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 11, langConfig.getString("item.dyePowder.yellow.name", "Dandelion Yellow")));
        itemNames.add(new ItemName(Material.INK_SACK, 12, langConfig.getString("item.dyePowder.lightBlue.name", "Light Blue Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 13, langConfig.getString("item.dyePowder.magenta.name", "Magenta Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 14, langConfig.getString("item.dyePowder.orange.name", "Orange Dye")));
        itemNames.add(new ItemName(Material.INK_SACK, 15, langConfig.getString("item.dyePowder.white.name", "Bone Meal")));
        itemNames.add(new ItemName(Material.BONE, langConfig.getString("item.bone.name", "Bone")));
        itemNames.add(new ItemName(Material.SUGAR, langConfig.getString("item.sugar.name", "Sugar")));
        itemNames.add(new ItemName(Material.CAKE, langConfig.getString("item.cake.name", "Cake")));
        itemNames.add(new ItemName(Material.BED, langConfig.getString("item.bed.name", "Bed")));
        itemNames.add(new ItemName(Material.DIODE, langConfig.getString("item.diode.name", "Redstone Repeater")));
        itemNames.add(new ItemName(Material.COOKIE, langConfig.getString("item.cookie.name", "Cookie")));
        itemNames.add(new ItemName(Material.MAP, langConfig.getString("item.map.name", "Map")));
        itemNames.add(new ItemName(Material.SHEARS, langConfig.getString("item.shears.name", "Shears")));
        itemNames.add(new ItemName(Material.MELON, langConfig.getString("item.melon.name", "Melon")));
        itemNames.add(new ItemName(Material.PUMPKIN_SEEDS, langConfig.getString("item.seeds_pumpkin.name", "Pumpkin Seeds")));
        itemNames.add(new ItemName(Material.MELON_SEEDS, langConfig.getString("item.seeds_melon.name", "Melon Seeds")));
        itemNames.add(new ItemName(Material.RAW_BEEF, langConfig.getString("item.beefRaw.name", "Raw Beef")));
        itemNames.add(new ItemName(Material.COOKED_BEEF, langConfig.getString("item.beefCooked.name", "Steak")));
        itemNames.add(new ItemName(Material.RAW_CHICKEN, langConfig.getString("item.chickenRaw.name", "Raw Chicken")));
        itemNames.add(new ItemName(Material.COOKED_CHICKEN, langConfig.getString("item.chickenCooked.name", "Cooked Chicken")));
        itemNames.add(new ItemName(Material.ROTTEN_FLESH, langConfig.getString("item.rottenFlesh.name", "Rotten Flesh")));
        itemNames.add(new ItemName(Material.ENDER_PEARL, langConfig.getString("item.enderPearl.name", "Ender Pearl")));
        itemNames.add(new ItemName(Material.BLAZE_ROD, langConfig.getString("item.blazeRod.name", "Blaze Rod")));
        itemNames.add(new ItemName(Material.GHAST_TEAR, langConfig.getString("item.ghastTear.name", "Ghast Tear")));
        itemNames.add(new ItemName(Material.GOLD_NUGGET, langConfig.getString("item.goldNugget.name", "Gold Nugget")));
        itemNames.add(new ItemName(Material.NETHER_WARTS, langConfig.getString("item.netherStalkSeeds.name", "Nether Wart")));
        itemNames.add(new ItemName(Material.POTION, langConfig.getString("item.potion.name", "Potion")));
        itemNames.add(new ItemName(Material.GLASS_BOTTLE, langConfig.getString("item.glassBottle.name", "Glass Bottle")));
        itemNames.add(new ItemName(Material.SPIDER_EYE, langConfig.getString("item.spiderEye.name", "Spider Eye")));
        itemNames.add(new ItemName(Material.FERMENTED_SPIDER_EYE, langConfig.getString("item.fermentedSpiderEye.name", "Fermented Spider Eye")));
        itemNames.add(new ItemName(Material.BLAZE_POWDER, langConfig.getString("item.blazePowder.name", "Blaze Powder")));
        itemNames.add(new ItemName(Material.MAGMA_CREAM, langConfig.getString("item.magmaCream.name", "Magma Cream")));
        itemNames.add(new ItemName(Material.BREWING_STAND_ITEM, langConfig.getString("item.brewingStand.name", "Brewing Stand")));
        itemNames.add(new ItemName(Material.CAULDRON_ITEM, langConfig.getString("item.cauldron.name", "Cauldron")));
        itemNames.add(new ItemName(Material.EYE_OF_ENDER, langConfig.getString("item.eyeOfEnder.name", "Eye of Ender")));
        itemNames.add(new ItemName(Material.SPECKLED_MELON, langConfig.getString("item.speckledMelon.name", "Glistering Melon")));
        itemNames.add(new ItemName(Material.MONSTER_EGG, langConfig.getString("item.monsterPlacer.name", "Spawn")));
        itemNames.add(new ItemName(Material.EXP_BOTTLE, langConfig.getString("item.expBottle.name", "Bottle o' Enchanting")));
        itemNames.add(new ItemName(Material.FIREWORK_CHARGE, langConfig.getString("item.fireball.name", "Fire Charge")));
        itemNames.add(new ItemName(Material.BOOK_AND_QUILL, langConfig.getString("item.writingBook.name", "Book and Quill")));
        itemNames.add(new ItemName(Material.WRITTEN_BOOK, langConfig.getString("item.writtenBook.name", "Written Book")));
        itemNames.add(new ItemName(Material.EMERALD, langConfig.getString("item.emerald.name", "Emerald")));
        itemNames.add(new ItemName(Material.ITEM_FRAME, langConfig.getString("item.frame.name", "Item Frame")));
        itemNames.add(new ItemName(Material.FLOWER_POT_ITEM, langConfig.getString("item.flowerPot.name", "Flower Pot")));
        itemNames.add(new ItemName(Material.CARROT_ITEM, langConfig.getString("item.carrots.name", "Carrot")));
        itemNames.add(new ItemName(Material.POTATO_ITEM, langConfig.getString("item.potato.name", "Potato")));
        itemNames.add(new ItemName(Material.BAKED_POTATO, langConfig.getString("item.potatoBaked.name", "Baked Potato")));
        itemNames.add(new ItemName(Material.POISONOUS_POTATO, langConfig.getString("item.potatoPoisonous.name", "Poisonous Potato")));
        itemNames.add(new ItemName(Material.EMPTY_MAP, langConfig.getString("item.emptyMap.name", "Empty Map")));
        itemNames.add(new ItemName(Material.GOLDEN_CARROT, langConfig.getString("item.carrotGolden.name", "Golden Carrot")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, langConfig.getString("item.skull.skeleton.name", "Skeleton Skull")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, 1, langConfig.getString("item.skull.wither.name", "Wither Skeleton Skull")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, 2, langConfig.getString("item.skull.zombie.name", "Zombie Head")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, 3, langConfig.getString("item.skull.char.name", "Head")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, 4, langConfig.getString("item.skull.creeper.name", "Creeper Head")));
        itemNames.add(new ItemName(Material.SKULL_ITEM, 5, langConfig.getString("item.skull.dragon.name", "Creeper Head")));
        itemNames.add(new ItemName(Material.CARROT_STICK, langConfig.getString("item.carrotOnAStick.name", "Carrot on a Stick")));
        itemNames.add(new ItemName(Material.NETHER_STAR, langConfig.getString("item.netherStar.name", "Nether Star")));
        itemNames.add(new ItemName(Material.PUMPKIN_PIE, langConfig.getString("item.pumpkinPie.name", "Pumpkin Pie")));
        itemNames.add(new ItemName(Material.FIREWORK, langConfig.getString("item.fireworks.name", "Firework Rocket")));
        itemNames.add(new ItemName(Material.FIREWORK_CHARGE, langConfig.getString("item.fireworksCharge.name", "Firework Star")));
        itemNames.add(new ItemName(Material.ENCHANTED_BOOK, langConfig.getString("item.enchantedBook.name", "Enchanted Book")));
        itemNames.add(new ItemName(Material.REDSTONE_COMPARATOR, langConfig.getString("item.comparator.name", "Redstone Comparator")));
        itemNames.add(new ItemName(Material.NETHER_BRICK_ITEM, langConfig.getString("item.netherbrick.name", "Nether Brick")));
        itemNames.add(new ItemName(Material.QUARTZ, langConfig.getString("item.netherquartz.name", "Nether Quartz")));
        itemNames.add(new ItemName(Material.EXPLOSIVE_MINECART, langConfig.getString("item.minecartTnt.name", "Minecart with TNT")));
        itemNames.add(new ItemName(Material.HOPPER_MINECART, langConfig.getString("item.minecartHopper.name", "Minecart with Hopper")));
        itemNames.add(new ItemName(Material.PRISMARINE_SHARD, langConfig.getString("item.prismarineShard.name", "Prismarine Shard")));
        itemNames.add(new ItemName(Material.PRISMARINE_CRYSTALS, langConfig.getString("item.prismarineCrystals.name", "Prismarine Crystals")));
        itemNames.add(new ItemName(Material.RABBIT, langConfig.getString("item.rabbitRaw.name", "Raw Rabbit")));
        itemNames.add(new ItemName(Material.COOKED_RABBIT, langConfig.getString("item.rabbitCooked.name", "Cooked Rabbit")));
        itemNames.add(new ItemName(Material.RABBIT_STEW, langConfig.getString("item.rabbitStew.name", "Rabbit Stew")));
        itemNames.add(new ItemName(Material.RABBIT_FOOT, langConfig.getString("item.rabbitFoot.name", "Rabbit's Foot")));
        itemNames.add(new ItemName(Material.RABBIT_HIDE, langConfig.getString("item.rabbitHide.name", "Rabbit Hide")));
        itemNames.add(new ItemName(Material.ARMOR_STAND, langConfig.getString("item.armorStand.name", "Armor Stand")));
        itemNames.add(new ItemName(Material.IRON_BARDING, langConfig.getString("item.horsearmormetal.name", "Iron Horse Armor")));
        itemNames.add(new ItemName(Material.GOLD_BARDING, langConfig.getString("item.horsearmorgold.name", "Gold Horse Armor")));
        itemNames.add(new ItemName(Material.DIAMOND_BARDING, langConfig.getString("item.horsearmordiamond.name", "Diamond Horse Armor")));
        itemNames.add(new ItemName(Material.LEASH, langConfig.getString("item.leash.name", "Lead")));
        itemNames.add(new ItemName(Material.NAME_TAG, langConfig.getString("item.nameTag.name", "Name Tag")));
        itemNames.add(new ItemName(Material.COMMAND_MINECART, langConfig.getString("item.minecartCommandBlock.name", "Minecart with Command Block")));
        itemNames.add(new ItemName(Material.MUTTON, langConfig.getString("item.muttonRaw.name", "Raw Mutton")));
        itemNames.add(new ItemName(Material.COOKED_MUTTON, langConfig.getString("item.muttonCooked.name", "Cooked Mutton")));
        itemNames.add(new ItemName(Material.BANNER, langConfig.getString("item.banner.black.name", "Black Banner")));
        itemNames.add(new ItemName(Material.BANNER, 1, langConfig.getString("item.banner.red.name", "Red Banner")));
        itemNames.add(new ItemName(Material.BANNER, 2, langConfig.getString("item.banner.green.name", "Green Banner")));
        itemNames.add(new ItemName(Material.BANNER, 3, langConfig.getString("item.banner.brown.name", "Brown Banner")));
        itemNames.add(new ItemName(Material.BANNER, 4, langConfig.getString("item.banner.blue.name", "Blue Banner")));
        itemNames.add(new ItemName(Material.BANNER, 5, langConfig.getString("item.banner.purple.name", "Purple Banner")));
        itemNames.add(new ItemName(Material.BANNER, 6, langConfig.getString("item.banner.cyan.name", "Cyan Banner")));
        itemNames.add(new ItemName(Material.BANNER, 7, langConfig.getString("item.banner.silver.name", "Light Gray Banner")));
        itemNames.add(new ItemName(Material.BANNER, 8, langConfig.getString("item.banner.gray.name", "Gray Banner")));
        itemNames.add(new ItemName(Material.BANNER, 9, langConfig.getString("item.banner.pink.name", "Pink Banner")));
        itemNames.add(new ItemName(Material.BANNER, 10, langConfig.getString("item.banner.lime.name", "Lime Banner")));
        itemNames.add(new ItemName(Material.BANNER, 11, langConfig.getString("item.banner.yellow.name", "Yellow Banner")));
        itemNames.add(new ItemName(Material.BANNER, 12, langConfig.getString("item.banner.lightBlue.name", "Light Blue Banner")));
        itemNames.add(new ItemName(Material.BANNER, 13, langConfig.getString("item.banner.magenta.name", "Magenta Banner")));
        itemNames.add(new ItemName(Material.BANNER, 14, langConfig.getString("item.banner.orange.name", "Orange Banner")));
        itemNames.add(new ItemName(Material.BANNER, 15, langConfig.getString("item.banner.white.name", "White Banner")));
        itemNames.add(new ItemName(Material.SPRUCE_DOOR_ITEM, langConfig.getString("item.doorSpruce.name", "Spruce Door")));
        itemNames.add(new ItemName(Material.BIRCH_DOOR_ITEM, langConfig.getString("item.doorBirch.name", "Birch Door")));
        itemNames.add(new ItemName(Material.JUNGLE_DOOR_ITEM, langConfig.getString("item.doorJungle.name", "Jungle Door")));
        itemNames.add(new ItemName(Material.ACACIA_DOOR_ITEM, langConfig.getString("item.doorAcacia.name", "Acacia Door")));
        itemNames.add(new ItemName(Material.DARK_OAK_DOOR_ITEM, langConfig.getString("item.doorDarkOak.name", "Dark Oak Door")));
        itemNames.add(new ItemName(Material.GOLD_RECORD, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.GREEN_RECORD, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_3, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_4, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_5, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_6, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_7, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_8, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_9, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_10, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_11, langConfig.getString("item.record.name", "Music Disc")));
        itemNames.add(new ItemName(Material.RECORD_12, langConfig.getString("item.record.name", "Music Disc")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Item names of 1.9
            itemNames.add(new ItemName(Material.END_CRYSTAL, langConfig.getString("item.end_crystal.name", "End Crystal")));
            itemNames.add(new ItemName(Material.CHORUS_FRUIT, langConfig.getString("item.chorusFruit.name", "Chorus Fruit")));
            itemNames.add(new ItemName(Material.CHORUS_FRUIT_POPPED, langConfig.getString("item.chorusFruitPopped.name", "Popped Chorus Fruit")));
            itemNames.add(new ItemName(Material.BEETROOT, langConfig.getString("item.beetroot.name", "Beetroot")));
            itemNames.add(new ItemName(Material.BEETROOT_SEEDS, langConfig.getString("item.beetroot_seeds.name", "Beetroot Seeds")));
            itemNames.add(new ItemName(Material.BEETROOT_SOUP, langConfig.getString("item.beetroot_soup.name", "Beetroot Soup")));
            itemNames.add(new ItemName(Material.DRAGONS_BREATH, langConfig.getString("item.dragon_breath.name", "Dragon's Breath")));
            itemNames.add(new ItemName(Material.SPECTRAL_ARROW, langConfig.getString("item.spectral_arrow.name", "Spectral Arrow")));
            itemNames.add(new ItemName(Material.TIPPED_ARROW, langConfig.getString("item.tipped_arrow.name", "Tipped Arrow")));
            itemNames.add(new ItemName(Material.SHIELD, langConfig.getString("item.shield.name", "Shield"))); //TODO ADD SHIELD DESCRIPTIONS
            itemNames.add(new ItemName(Material.ELYTRA, langConfig.getString("item.elytra.name", "Elytra")));
            itemNames.add(new ItemName(Material.BOAT_SPRUCE, langConfig.getString("item.boat.spruce.name", "Spruce Boat")));
            itemNames.add(new ItemName(Material.BOAT_BIRCH, langConfig.getString("item.boat.birch.name", "Birch Boat")));
            itemNames.add(new ItemName(Material.BOAT_JUNGLE, langConfig.getString("item.boat.jungle.name", "Jungle Boat")));
            itemNames.add(new ItemName(Material.BOAT_ACACIA, langConfig.getString("item.boat.acacia.name", "Acacia Boat")));
            itemNames.add(new ItemName(Material.BOAT_DARK_OAK, langConfig.getString("item.boat.dark_oak.name", "Dark Oak Boat")));
        }

        if (Utils.getMajorVersion() >= 11) {
            // Add Item Names of 1.11
            itemNames.add(new ItemName(Material.TOTEM, langConfig.getString("item.totem.name", "Totem of Undying")));
            itemNames.add(new ItemName(Material.SHULKER_SHELL, langConfig.getString("item.shulkerShell.name", "Shulker Shell")));
        }

        // Add Enchantment Names
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_DAMAGE, langConfig.getString("enchantment.arrowDamage", "Power")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_FIRE, langConfig.getString("enchantment.arrowFire", "Flame")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_INFINITE, langConfig.getString("enchantment.arrowInfinite", "Infinity")));
        enchantmentNames.add(new EnchantmentName(Enchantment.ARROW_KNOCKBACK, langConfig.getString("enchantment.arrowKnockback", "Punch")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_ALL, langConfig.getString("enchantment.damage.all", "Sharpness")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_ARTHROPODS, langConfig.getString("enchantment.damage.arthropods", "Bane of Arthropods")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DAMAGE_UNDEAD, langConfig.getString("enchantment.damage.undead", "Smite")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DIG_SPEED, langConfig.getString("enchantment.digging", "Efficiency")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DURABILITY, langConfig.getString("enchantment.durability", "Unbreaking")));
        enchantmentNames.add(new EnchantmentName(Enchantment.FIRE_ASPECT, langConfig.getString("enchantment.fire", "Fire Aspect")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LURE, langConfig.getString("enchantment.fishingSpeed", "Lure")));
        enchantmentNames.add(new EnchantmentName(Enchantment.KNOCKBACK, langConfig.getString("enchantment.knockback", "Knockback")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LOOT_BONUS_MOBS, langConfig.getString("enchantment.lootBonus", "Looting")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LOOT_BONUS_BLOCKS, langConfig.getString("enchantment.lootBonusDigger", "Fortune")));
        enchantmentNames.add(new EnchantmentName(Enchantment.LUCK, langConfig.getString("enchantment.lootBonusFishing", "Luck of the Sea")));
        enchantmentNames.add(new EnchantmentName(Enchantment.OXYGEN, langConfig.getString("enchantment.oxygen", "Respiration")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_ENVIRONMENTAL, langConfig.getString("enchantment.protect.all", "Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_EXPLOSIONS, langConfig.getString("enchantment.protect.explosion", "Blast Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_FALL, langConfig.getString("enchantment.protect.fall", "Feather Falling")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_FIRE, langConfig.getString("enchantment.protect.fire", "Fire Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.PROTECTION_PROJECTILE, langConfig.getString("enchantment.protect.projectile", "Projectile Protection")));
        enchantmentNames.add(new EnchantmentName(Enchantment.THORNS, langConfig.getString("enchantment.thorns", "Thorns")));
        enchantmentNames.add(new EnchantmentName(Enchantment.SILK_TOUCH, langConfig.getString("enchantment.untouching", "Silk Touch")));
        enchantmentNames.add(new EnchantmentName(Enchantment.DEPTH_STRIDER, langConfig.getString("enchantment.waterWalker", "Depth Strider")));
        enchantmentNames.add(new EnchantmentName(Enchantment.WATER_WORKER, langConfig.getString("enchantment.waterWorker", "Aqua Affinity")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Enchantment Names of 1.9
            enchantmentNames.add(new EnchantmentName(Enchantment.FROST_WALKER, langConfig.getString("enchantment.frostWalker", "Frost Walker")));
            enchantmentNames.add(new EnchantmentName(Enchantment.MENDING, langConfig.getString("enchantment.mending", "Mending")));
        }

        if (Utils.getMajorVersion() >= 11) {
            // Add Enchantment Names of 1.11
            enchantmentNames.add(new EnchantmentName(Enchantment.BINDING_CURSE, langConfig.getString("enchantment.binding_curse", "Curse of Binding")));
            enchantmentNames.add(new EnchantmentName(Enchantment.VANISHING_CURSE, langConfig.getString("enchantment.vanishing_curse", "Curse of Vanishing")));
        }

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

        // Add Entity Names
        String horseName = (Utils.getMajorVersion() >= 11 ? "entity.Horse.name" : "entity.EntityHorse.name");
        entityNames.add(new EntityName(EntityType.CREEPER, langConfig.getString("entity.Creeper.name", "Creeper")));
        entityNames.add(new EntityName(EntityType.SKELETON, langConfig.getString("entity.Skeleton.name", "Skeleton")));
        entityNames.add(new EntityName(EntityType.SPIDER, langConfig.getString("entity.Spider.name", "Spider")));
        entityNames.add(new EntityName(EntityType.ZOMBIE, langConfig.getString("entity.Zombie.name", "Zombie")));
        entityNames.add(new EntityName(EntityType.SLIME, langConfig.getString("entity.Slime.name", "Slime")));
        entityNames.add(new EntityName(EntityType.GHAST, langConfig.getString("entity.Ghast.name", "Ghast")));
        entityNames.add(new EntityName(EntityType.PIG_ZOMBIE, langConfig.getString("entity.PigZombie.name", "Zombie Pigman")));
        entityNames.add(new EntityName(EntityType.ENDERMAN, langConfig.getString("entity.Enderman.name", "Enderman")));
        entityNames.add(new EntityName(EntityType.CAVE_SPIDER, langConfig.getString("entity.CaveSpider.name", "Cave Spider")));
        entityNames.add(new EntityName(EntityType.SILVERFISH, langConfig.getString("entity.Silverfish.name", "Silverfish")));
        entityNames.add(new EntityName(EntityType.BLAZE, langConfig.getString("entity.Blaze.name", "Blaze")));
        entityNames.add(new EntityName(EntityType.MAGMA_CUBE, langConfig.getString("entity.LavaSlime.name", "Magma Cube")));
        entityNames.add(new EntityName(EntityType.BAT, langConfig.getString("entity.Bat.name", "Bat")));
        entityNames.add(new EntityName(EntityType.WITCH, langConfig.getString("entity.Witch.name", "Witch")));
        entityNames.add(new EntityName(EntityType.ENDERMITE, langConfig.getString("entity.Endermite.name", "Endermite")));
        entityNames.add(new EntityName(EntityType.GUARDIAN, langConfig.getString("entity.Guardian.name", "Guardian")));
        entityNames.add(new EntityName(EntityType.PIG, langConfig.getString("entity.Pig.name", "Pig")));
        entityNames.add(new EntityName(EntityType.SHEEP, langConfig.getString("entity.Sheep.name", "Sheep")));
        entityNames.add(new EntityName(EntityType.COW, langConfig.getString("entity.Cow.name", "Cow")));
        entityNames.add(new EntityName(EntityType.CHICKEN, langConfig.getString("entity.Chicken.name", "Chicken")));
        entityNames.add(new EntityName(EntityType.SQUID, langConfig.getString("entity.Squid.name", "Squid")));
        entityNames.add(new EntityName(EntityType.WOLF, langConfig.getString("entity.Wolf.name", "Wolf")));
        entityNames.add(new EntityName(EntityType.MUSHROOM_COW, langConfig.getString("entity.MushroomCow.name", "Mooshroom")));
        entityNames.add(new EntityName(EntityType.OCELOT, langConfig.getString("entity.Ozelot.name", "Ocelot")));
        entityNames.add(new EntityName(EntityType.HORSE, langConfig.getString(horseName, "Horse")));
        entityNames.add(new EntityName(EntityType.RABBIT, langConfig.getString("entity.Rabbit.name", "Rabbit")));
        entityNames.add(new EntityName(EntityType.VILLAGER, langConfig.getString("entity.Villager.name", "Villager")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Entity Names of 1.9
            entityNames.add(new EntityName(EntityType.SHULKER, langConfig.getString("entity.Shulker.name", "Shulker")));
        }

        if (Utils.getMajorVersion() >= 10) {
            // Add Entity Names of 1.10
            entityNames.add(new EntityName(EntityType.POLAR_BEAR, langConfig.getString("entity.PolarBear.name", "Polar Bear")));
        }

        if (Utils.getMajorVersion() >= 11) {
            // Add Entity Names of 1.11
            entityNames.add(new EntityName(EntityType.ZOMBIE_VILLAGER, langConfig.getString("entity.ZombieVillager.name", "Zombie Villager")));
            entityNames.add(new EntityName(EntityType.ELDER_GUARDIAN, langConfig.getString("entity.ElderGuardian.name", "Elder Guardian")));
            entityNames.add(new EntityName(EntityType.EVOKER, langConfig.getString("entity.EvocationIllager.name", "Evoker")));
            entityNames.add(new EntityName(EntityType.VEX, langConfig.getString("entity.Vex.name", "Vex")));
            entityNames.add(new EntityName(EntityType.VINDICATOR, langConfig.getString("entity.VindicationIllager.name", "Vindicator")));
            entityNames.add(new EntityName(EntityType.LLAMA, langConfig.getString("entity.Llama.name", "Llama")));
            entityNames.add(new EntityName(EntityType.WITHER_SKELETON, langConfig.getString("entity.WitherSkeleton.name", "Wither Skeleton")));
            entityNames.add(new EntityName(EntityType.STRAY, langConfig.getString("entity.Stray.name", "Stray")));
            entityNames.add(new EntityName(EntityType.ZOMBIE_HORSE, langConfig.getString("entity.ZombieHorse.name", "Zombie Horse")));
            entityNames.add(new EntityName(EntityType.SKELETON_HORSE, langConfig.getString("entity.SkeletonHorse.name", "Skeleton Horse")));
            entityNames.add(new EntityName(EntityType.DONKEY, langConfig.getString("entity.Donkey.name", "Donkey")));
            entityNames.add(new EntityName(EntityType.MULE, langConfig.getString("entity.Mule.name", "Mule")));
            entityNames.add(new EntityName(EntityType.HUSK, langConfig.getString("entity.Husk.name", "Husk")));
        }

        // Add Potion Effect Names
        potionEffectNames.add(new PotionEffectName(PotionType.FIRE_RESISTANCE, langConfig.getString("effect.fireResistance", "Fire Resistance")));
        potionEffectNames.add(new PotionEffectName(PotionType.INSTANT_DAMAGE, langConfig.getString("effect.harm", "Instant Damage")));
        potionEffectNames.add(new PotionEffectName(PotionType.INSTANT_HEAL, langConfig.getString("effect.heal", "Instant Health")));
        potionEffectNames.add(new PotionEffectName(PotionType.INVISIBILITY, langConfig.getString("effect.invisibility", "Invisibility")));
        potionEffectNames.add(new PotionEffectName(PotionType.JUMP, langConfig.getString("effect.jump", "Jump Boost")));
        potionEffectNames.add(new PotionEffectName(PotionType.NIGHT_VISION, langConfig.getString("effect.nightVision", "Night Vision")));
        potionEffectNames.add(new PotionEffectName(PotionType.POISON, langConfig.getString("effect.poison", "Poison")));
        potionEffectNames.add(new PotionEffectName(PotionType.REGEN, langConfig.getString("effect.regeneration", "Regeneration")));
        potionEffectNames.add(new PotionEffectName(PotionType.SLOWNESS, langConfig.getString("effect.moveSlowdown", "Slowness")));
        potionEffectNames.add(new PotionEffectName(PotionType.SPEED, langConfig.getString("effect.moveSpeed", "Speed")));
        potionEffectNames.add(new PotionEffectName(PotionType.STRENGTH, langConfig.getString("effect.damageBoost", "Strength")));
        potionEffectNames.add(new PotionEffectName(PotionType.WATER_BREATHING, langConfig.getString("effect.waterBreathing", "Water Breathing")));
        potionEffectNames.add(new PotionEffectName(PotionType.WEAKNESS, langConfig.getString("effect.weakness", "Weakness")));
        potionEffectNames.add(new PotionEffectName(PotionType.WATER, langConfig.getString("effect.none", "No Effects")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Potion Effect Names of 1.9
            potionEffectNames.add(new PotionEffectName(PotionType.AWKWARD, langConfig.getString("effect.none", "No Effects")));
            potionEffectNames.add(new PotionEffectName(PotionType.LUCK, langConfig.getString("effect.luck", "Luck")));
            potionEffectNames.add(new PotionEffectName(PotionType.MUNDANE, langConfig.getString("effect.none", "No Effects")));
            potionEffectNames.add(new PotionEffectName(PotionType.THICK, langConfig.getString("effect.none", "No Effects")));
            potionEffectNames.add(new PotionEffectName(PotionType.UNCRAFTABLE, langConfig.getString("effect.none", "No Effects")));
        }

        // Add Potion Names
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("potion.effect.fire_resistance", "Potion of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("potion.effect.harming", "Potion of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INSTANT_HEAL, langConfig.getString("potion.effect.healing", "Potion of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.INVISIBILITY, langConfig.getString("potion.effect.invisibility", "Potion of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.JUMP, langConfig.getString("potion.effect.leaping", "Potion of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.NIGHT_VISION, langConfig.getString("potion.effect.night_vision", "Potion of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.POISON, langConfig.getString("potion.effect.poison", "Potion of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.REGEN, langConfig.getString("potion.effect.regeneration", "Potion of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.SLOWNESS, langConfig.getString("potion.effect.slowness", "Potion of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.SPEED, langConfig.getString("potion.effect.swiftness", "Potion of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.STRENGTH, langConfig.getString("potion.effect.strength", "Potion of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WATER_BREATHING, langConfig.getString("potion.effect.water_breathing", "Potion of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WEAKNESS, langConfig.getString("potion.effect.weakness", "Potion of Weakness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.WATER, langConfig.getString("potion.effect.water", "Water Bottle")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Potion Names of 1.9
            potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.AWKWARD, langConfig.getString("potion.effect.awkward", "Awkward Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.LUCK, langConfig.getString("potion.effect.luck", "Potion of Luck")));
            potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.MUNDANE, langConfig.getString("potion.effect.mundane", "Mundane Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.THICK, langConfig.getString("potion.effect.thick", "Thick Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.POTION, PotionType.UNCRAFTABLE, langConfig.getString("potion.effect.empty", "Uncraftable Potion")));
        }

        if (Utils.getMajorVersion() >= 9) {
            // Add Tipped Arrow Names (implemented in Minecraft since 1.9)
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.AWKWARD, langConfig.getString("tipped_arrow.effect.awkward", "Tipped Arrow")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.FIRE_RESISTANCE, langConfig.getString("tipped_arrow.effect.fire_resistance", "Arrow of Fire Resistance")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INSTANT_DAMAGE, langConfig.getString("tipped_arrow.effect.harming", "Arrow of Harming")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INSTANT_HEAL, langConfig.getString("tipped_arrow.effect.healing", "Arrow of Healing")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.INVISIBILITY, langConfig.getString("tipped_arrow.effect.invisibility", "Arrow of Invisibility")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.JUMP, langConfig.getString("tipped_arrow.effect.leaping", "Arrow of Leaping")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.NIGHT_VISION, langConfig.getString("tipped_arrow.effect.night_vision", "Arrow of Night Vision")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.POISON, langConfig.getString("tipped_arrow.effect.poison", "Arrow of Poison")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.REGEN, langConfig.getString("tipped_arrow.effect.regeneration", "Arrow of Regeneration")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.SLOWNESS, langConfig.getString("tipped_arrow.effect.slowness", "Arrow of Slowness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.SPEED, langConfig.getString("tipped_arrow.effect.swiftness", "Arrow of Swiftness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.STRENGTH, langConfig.getString("tipped_arrow.effect.strength", "Arrow of Strength")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WATER_BREATHING, langConfig.getString("tipped_arrow.effect.water_breathing", "Arrow of Water Breathing")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WEAKNESS, langConfig.getString("tipped_arrow.effect.weakness", "Arrow of Weakness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.WATER, langConfig.getString("tipped_arrow.effect.water", "Arrow of Splashing")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.LUCK, langConfig.getString("tipped_arrow.effect.luck", "Arrow of Luck")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.MUNDANE, langConfig.getString("tipped_arrow.effect.mundane", "Tipped Arrow")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.THICK, langConfig.getString("tipped_arrow.effect.thick", "Tipped Arrow")));
            potionNames.add(new PotionName(PotionName.PotionItemType.TIPPED_ARROW, PotionType.UNCRAFTABLE, langConfig.getString("tipped_arrow.effect.empty", "Uncraftable Tipped Arrow")));
        }

        // Add Splash Potion Names
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("splash_potion.effect.fire_resistance", "Splash Potion of Fire Resistance")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("splash_potion.effect.harming", "Splash Potion of Harming")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INSTANT_HEAL, langConfig.getString("splash_potion.effect.healing", "Splash Potion of Healing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.INVISIBILITY, langConfig.getString("splash_potion.effect.invisibility", "Splash Potion of Invisibility")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.JUMP, langConfig.getString("splash_potion.effect.leaping", "Splash Potion of Leaping")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.NIGHT_VISION, langConfig.getString("splash_potion.effect.night_vision", "Splash Potion of Night Vision")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.POISON, langConfig.getString("splash_potion.effect.poison", "Splash Potion of Poison")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.REGEN, langConfig.getString("splash_potion.effect.regeneration", "Splash Potion of Regeneration")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.SLOWNESS, langConfig.getString("splash_potion.effect.slowness", "Splash Potion of Slowness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.SPEED, langConfig.getString("splash_potion.effect.swiftness", "Splash Potion of Swiftness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.STRENGTH, langConfig.getString("splash_potion.effect.strength", "Splash Potion of Strength")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WATER_BREATHING, langConfig.getString("splash_potion.effect.water_breathing", "Splash Potion of Water Breathing")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WEAKNESS, langConfig.getString("splash_potion.effect.weakness", "Splash Potion of Weakness")));
        potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.WATER, langConfig.getString("splash_potion.effect.water", "Splash Water Bottle")));

        if (Utils.getMajorVersion() >= 9) {
            // Add Splash Potion Names of 1.9
            potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.AWKWARD, langConfig.getString("splash_potion.effect.awkward", "Awkward Splash Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.LUCK, langConfig.getString("splash_potion.effect.luck", "Splash Potion of Luck")));
            potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.MUNDANE, langConfig.getString("splash_potion.effect.mundane", "Mundane Splash Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.THICK, langConfig.getString("splash_potion.effect.thick", "Thick Splash Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.SPLASH_POTION, PotionType.UNCRAFTABLE, langConfig.getString("splash_potion.effect.empty", "Splash Uncraftable Potion")));
        }

        if (Utils.getMajorVersion() >= 9) {
            // Add Lingering Potion Names (implemented in Minecraft since 1.9)
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.AWKWARD, langConfig.getString("lingering_potion.effect.awkward", "Awkward Lingering Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.FIRE_RESISTANCE, langConfig.getString("lingering_potion.effect.fire_resistance", "Lingering Potion of Fire Resistance")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INSTANT_DAMAGE, langConfig.getString("lingering_potion.effect.harming", "Lingering Potion of Harming")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INSTANT_HEAL, langConfig.getString("lingering_potion.effect.healing", "Lingering Potion of Healing")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.INVISIBILITY, langConfig.getString("lingering_potion.effect.invisibility", "Lingering Potion of Invisibility")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.JUMP, langConfig.getString("lingering_potion.effect.leaping", "Lingering Potion of Leaping")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.NIGHT_VISION, langConfig.getString("lingering_potion.effect.night_vision", "Lingering Potion of Night Vision")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.POISON, langConfig.getString("lingering_potion.effect.poison", "Lingering Potion of Poison")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.REGEN, langConfig.getString("lingering_potion.effect.regeneration", "Lingering Potion of Regeneration")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.SLOWNESS, langConfig.getString("lingering_potion.effect.slowness", "Lingering Potion of Slowness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.SPEED, langConfig.getString("lingering_potion.effect.swiftness", "Lingering Potion of Swiftness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.STRENGTH, langConfig.getString("lingering_potion.effect.strength", "Lingering Potion of Strength")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WATER_BREATHING, langConfig.getString("lingering_potion.effect.water_breathing", "Lingering Potion of Water Breathing")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WEAKNESS, langConfig.getString("lingering_potion.effect.weakness", "Lingering Potion of Weakness")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.WATER, langConfig.getString("lingering_potion.effect.water", "Lingering Water Bottle")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.LUCK, langConfig.getString("lingering_potion.effect.luck", "Lingering Potion of Luck")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.MUNDANE, langConfig.getString("lingering_potion.effect.mundane", "Mundane Lingering Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.THICK, langConfig.getString("lingering_potion.effect.thick", "Thick Lingering Potion")));
            potionNames.add(new PotionName(PotionName.PotionItemType.LINGERING_POTION, PotionType.UNCRAFTABLE, langConfig.getString("lingering_potion.effect.empty", "Lingering Uncraftable Potion")));
        }

        // Add Music Disc Titles
        musicDiscNames.add(new MusicDiscName(Material.GOLD_RECORD, langConfig.getString("item.record.13.desc", "C418 - 13")));
        musicDiscNames.add(new MusicDiscName(Material.GREEN_RECORD, langConfig.getString("item.record.cat.desc", "C418 - cat")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_3, langConfig.getString("item.record.blocks.desc", "C418 - blocks")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_4, langConfig.getString("item.record.chirp.desc", "C418 - chirp")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_5, langConfig.getString("item.record.far.desc", "C418 - far")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_6, langConfig.getString("item.record.mall.desc", "C418 - mall")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_7, langConfig.getString("item.record.mellohi.desc", "C418 - mellohi")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_8, langConfig.getString("item.record.stal.desc", "C418 - stal")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_9, langConfig.getString("item.record.strad.desc", "C418 - strad")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_10, langConfig.getString("item.record.ward.desc", "C418 - ward")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_11, langConfig.getString("item.record.11.desc", "C418 - 11")));
        musicDiscNames.add(new MusicDiscName(Material.RECORD_12, langConfig.getString("item.record.wait.desc", "C418 - wait")));

        // Add ShopChest Messages
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_CREATED, langConfig.getString("message.shop-created", "&6Shop created.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHEST_ALREADY_SHOP, langConfig.getString("message.chest-already-shop", "&cChest already shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHEST_BLOCKED, langConfig.getString("message.chest-blocked", "&cThere must not be a block above the chest.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.DOUBLE_CHEST_BLOCKED, langConfig.getString("message.double-chest-blocked", "&cThere must not be a block above the chest.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_REMOVED, langConfig.getString("message.shop-removed", "&6Shop removed.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHEST_NO_SHOP, langConfig.getString("message.chest-no-shop", "&cChest is not a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_CREATE_NOT_ENOUGH_MONEY, langConfig.getString("message.shop-create-not-enough-money", "&cNot enough money. You need &6%CREATION-PRICE% &cto create a shop."), Regex.CREATION_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_VENDOR, langConfig.getString("message.shopInfo.vendor", "&6Vendor: &e%VENDOR%"), Regex.VENDOR));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_PRODUCT, langConfig.getString("message.shopInfo.product", "&6Product: &e%AMOUNT% x %ITEMNAME%"), Regex.AMOUNT, Regex.ITEM_NAME));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_STOCK, langConfig.getString("message.shopInfo.stock", "&6In Stock: &e%AMOUNT%"), Regex.AMOUNT));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_ENCHANTMENTS, langConfig.getString("message.shopInfo.enchantments", "&6Enchantments: &e%ENCHANTMENT%"), Regex.ENCHANTMENT));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_POTION_EFFECT, langConfig.getString("message.shopInfo.potion-effect", "&6Potion Effect: &e%POTION-EFFECT% %EXTENDED%"), Regex.POTION_EFFECT, Regex.EXTENDED));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_MUSIC_TITLE, langConfig.getString("message.shopInfo.music-disc-title", "&6Music Disc Title: &e%MUSIC-TITLE%"), Regex.MUSIC_TITLE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_NONE, langConfig.getString("message.shopInfo.none", "&7None")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_PRICE, langConfig.getString("message.shopInfo.price", "&6Price: Buy: &e%BUY-PRICE%&6 Sell: &e%SELL-PRICE%"), Regex.BUY_PRICE, Regex.SELL_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_DISABLED, langConfig.getString("message.shopInfo.disabled", "&7Disabled")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_NORMAL, langConfig.getString("message.shopInfo.is-normal", "&6Type: &eNormal")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_ADMIN, langConfig.getString("message.shopInfo.is-admin", "&6Type: &eAdmin")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_INFO_EXTENDED, langConfig.getString("message.shopInfo.extended", "(Extended)")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.BUY_SELL_DISABLED, langConfig.getString("message.buy-and-sell-disabled", "&cYou can't create a shop with buying and selling disabled.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.BUY_SUCCESS, langConfig.getString("message.buy-success", "&aYou bought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a from &6%VENDOR%&a."), Regex.AMOUNT, Regex.ITEM_NAME, Regex.BUY_PRICE, Regex.VENDOR));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.BUY_SUCESS_ADMIN, langConfig.getString("message.buy-success-admin", "&aYou bought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a."), Regex.AMOUNT, Regex.ITEM_NAME, Regex.BUY_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SELL_SUCESS, langConfig.getString("message.sell-success", "&aYou sold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a to &6%VENDOR%&a."), Regex.AMOUNT, Regex.ITEM_NAME, Regex.SELL_PRICE, Regex.VENDOR));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SELL_SUCESS_ADMIN, langConfig.getString("message.sell-success-admin", "&aYou sold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a."), Regex.AMOUNT, Regex.ITEM_NAME, Regex.SELL_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SOMEONE_BOUGHT, langConfig.getString("message.someone-bought", "&6%PLAYER% &abought &6%AMOUNT% x %ITEMNAME%&a for &6%BUY-PRICE%&a from your shop."), Regex.PLAYER, Regex.AMOUNT, Regex.ITEM_NAME, Regex.BUY_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SOMEONE_SOLD, langConfig.getString("message.someone-sold", "&6%PLAYER% &asold &6%AMOUNT% x %ITEMNAME%&a for &6%SELL-PRICE%&a to your shop."), Regex.PLAYER, Regex.AMOUNT, Regex.ITEM_NAME, Regex.SELL_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NOT_ENOUGH_INVENTORY_SPACE, langConfig.getString("message.not-enough-inventory-space", "&cNot enough space in inventory.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE, langConfig.getString("message.chest-not-enough-inventory-space", "&cShop is full.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NOT_ENOUGH_MONEY, langConfig.getString("message.not-enough-money", "&cNot enough money.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NOT_ENOUGH_ITEMS, langConfig.getString("message.not-enough-items", "&cNot enough items.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.VENDOR_NOT_ENOUGH_MONEY, langConfig.getString("message.vendor-not-enough-money", "&cVendor has not enough money.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.OUT_OF_STOCK, langConfig.getString("message.out-of-stock", "&cShop out of stock.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.ERROR_OCCURRED, langConfig.getString("message.error-occurred", "&cAn error occurred: %ERROR%"), Regex.ERROR));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.AMOUNT_PRICE_NOT_NUMBER, langConfig.getString("message.amount-and-price-not-number", "&cAmount and price must be a number.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.AMOUNT_IS_ZERO, langConfig.getString("message.amount-is-zero", "&cAmount must be greater than 0.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.PRICES_CONTAIN_DECIMALS, langConfig.getString("message.prices-contain-decimals", "&cPrices must not contain decimals.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_ITEM_IN_HAND, langConfig.getString("message.no-item-in-hand", "&cNo item in hand")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CLICK_CHEST_CREATE, langConfig.getString("message.click-chest-to-create-shop", "&aClick a chest to create a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CLICK_CHEST_REMOVE, langConfig.getString("message.click-chest-to-remove-shop", "&aClick a shop-chest to remove the shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CLICK_CHEST_INFO, langConfig.getString("message.click-chest-for-info", "&aClick a shop to retrieve information.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.OPENED_SHOP, langConfig.getString("message.opened-shop", "&aYou opened %VENDOR%'s shop."), Regex.VENDOR));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CANNOT_BREAK_SHOP, langConfig.getString("message.cannot-break-shop", "&cYou can't break a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CANNOT_SELL_BROKEN_ITEM, langConfig.getString("message.cannot-sell-broken-item", "&cYou can't sell a broken item.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.BUY_PRICE_TOO_LOW, langConfig.getString("message.buy-price-too-low", "&cThe buy price must be higher than %MIN-PRICE%."), Regex.MIN_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SELL_PRICE_TOO_LOW, langConfig.getString("message.sell-price-too-low", "&cThe sell price must be higher than %MIN-PRICE%."), Regex.MIN_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.BUYING_DISABLED, langConfig.getString("message.buying-disabled", "&cBuying is disabled at this shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SELLING_DISABLED, langConfig.getString("message.selling-disabled", "&cSelling is disabled at this shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.RELOADED_SHOPS, langConfig.getString("message.reloaded-shops", "&aSuccessfully reloaded %AMOUNT% shop/s."), Regex.AMOUNT));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.SHOP_LIMIT_REACHED, langConfig.getString("message.shop-limit-reached", "&cYou reached your limit of &6%LIMIT% &cshop/s."), Regex.LIMIT));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.OCCUPIED_SHOP_SLOTS, langConfig.getString("message.occupied-shop-slots", "&6You have &c%AMOUNT%/%LIMIT% &6shop slot/s occupied."), Regex.AMOUNT, Regex.LIMIT));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CANNOT_SELL_ITEM, langConfig.getString("message.cannot-sell-item", "&cYou cannot create a shop with this item.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, langConfig.getString("message.update.update-available", "&6&lVersion &c%VERSION% &6of &cShopChest &6is available &chere."), Regex.VERSION));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD, langConfig.getString("message.update.click-to-download", "Click to download")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.UPDATE_NO_UPDATE, langConfig.getString("message.update.no-update", "&6&lNo new update available.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.UPDATE_CHECKING, langConfig.getString("message.update.checking", "&6&lChecking for updates...")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.UPDATE_ERROR, langConfig.getString("message.update.error", "&c&lError while checking for updates.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.HOLOGRAM_FORMAT, langConfig.getString("message.hologram.format", "%AMOUNT% * %ITEMNAME%"), Regex.AMOUNT, Regex.ITEM_NAME));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.HOLOGRAM_BUY_SELL, langConfig.getString("message.hologram.buy-and-sell", "Buy %BUY-PRICE% | %SELL-PRICE% Sell"), Regex.BUY_PRICE, Regex.SELL_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.HOLOGRAM_BUY, langConfig.getString("message.hologram.only-buy", "Buy %BUY-PRICE%"), Regex.BUY_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.HOLOGRAM_SELL, langConfig.getString("message.hologram.only-sell", "Sell %SELL-PRICE%"), Regex.SELL_PRICE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE, langConfig.getString("message.noPermission.create", "&cYou don't have permission to create a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_ADMIN, langConfig.getString("message.noPermission.create-admin", "&cYou don't have permission to create an admin shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_PROTECTED, langConfig.getString("message.noPermission.create-protected", "&cYou don't have permission to create a shop on a protected chest.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_OPEN_OTHERS, langConfig.getString("message.noPermission.open-others", "&cYou don't have permission to open this chest.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_BUY, langConfig.getString("message.noPermission.buy", "&cYou don't have permission to buy something.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_SELL, langConfig.getString("message.noPermission.sell", "&cYou don't have permission to sell something.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_WG_BUY, langConfig.getString("message.noPermission.worldguard-buy", "&cYou don't have permission to buy something here.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_WG_SELL, langConfig.getString("message.noPermission.worldguard-sell", "&cYou don't have permission to sell something here.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_REMOVE_OTHERS, langConfig.getString("message.noPermission.remove-others", "&cYou don't have permission to remove this shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_RELOAD, langConfig.getString("message.noPermission.reload", "&cYou don't have permission to reload the shops.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_UPDATE, langConfig.getString("message.noPermission.update", "&cYou don't have permission to check for updates.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_CONFIG, langConfig.getString("message.noPermission.config", "&cYou don't have permission to change configuration values.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_EXTEND_OTHERS, langConfig.getString("message.noPermission.extend-others", "&cYou don't have permission to extend this chest.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.NO_PERMISSION_EXTEND_PROTECTED, langConfig.getString("message.noPermission.extend-protected", "&cYou don't have permission to extend this chest to here.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE, langConfig.getString("message.commandDescription.create", "Create a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_REMOVE, langConfig.getString("message.commandDescription.remove", "Remove a shop.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_INFO, langConfig.getString("message.commandDescription.info", "Retrieve shop information.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_RELOAD, langConfig.getString("message.commandDescription.reload", "Reload shops.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_UPDATE, langConfig.getString("message.commandDescription.update", "Check for Updates.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_LIMITS, langConfig.getString("message.commandDescription.limits", "View shop limits.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.COMMAND_DESC_CONFIG, langConfig.getString("message.commandDescription.config", "Change configuration values.")));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHANGED_CONFIG_SET, langConfig.getString("message.config.set", "&6Changed &a%PROPERTY% &6to &a%VALUE%&6."), Regex.PROPERTY, Regex.VALUE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHANGED_CONFIG_REMOVED, langConfig.getString("message.config.removed", "&6Removed &a%VALUE% &6from &a%PROPERTY%&6."), Regex.PROPERTY, Regex.VALUE));
        messages.add(new LocalizedMessage(LocalizedMessage.Message.CHANGED_CONFIG_ADDED, langConfig.getString("message.config.added", "&6Added &a%VALUE% &6to &a%PROPERTY%&6."), Regex.PROPERTY, Regex.VALUE));
    }

    /**
     * @param stack Item whose name to lookup
     * @return Localized Name of the Item, the custom name, or if <i>stack</i> is a book, the title of the book
     */
    public static String getItemName(ItemStack stack) {
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.getDisplayName() != null) {
                return meta.getDisplayName();
            } else if (meta instanceof BookMeta && ((BookMeta) meta).hasTitle()) {
                return ((BookMeta) meta).getTitle();
            } else if (meta instanceof SkullMeta) {
                if (((SkullMeta) meta).hasOwner()) {
                    return String.format(langConfig.getString("item.skull.player.name", "%s's Head"), ((SkullMeta) meta).getOwner());
                }
            }
        }

        Material material = stack.getType();
        int subID = (int) stack.getDurability();

        if (stack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            PotionType potionType;
            String upgradeString;

            if (Utils.getMajorVersion() < 9) {
                potionType = Potion.fromItemStack(stack).getType();
                upgradeString = (Potion.fromItemStack(stack).getLevel() == 2 && plugin.getShopChestConfig().append_potion_level_to_item_name ? " II" : "");
            } else {
                potionType = meta.getBasePotionData().getType();
                upgradeString = (meta.getBasePotionData().isUpgraded() && plugin.getShopChestConfig().append_potion_level_to_item_name ? " II" : "");
            }

            for (PotionName potionName : potionNames) {
                if (material == Material.POTION) {
                    if (Utils.getMajorVersion() < 9) {
                        if (Potion.fromItemStack(stack).isSplash()) {
                            if (potionName.getPotionItemType() == PotionName.PotionItemType.SPLASH_POTION && potionName.getPotionType() == potionType) {
                                return potionName.getLocalizedName() + upgradeString;
                            }
                        } else {
                            if (potionName.getPotionItemType() == PotionName.PotionItemType.POTION && potionName.getPotionType() == potionType) {
                                return potionName.getLocalizedName() + upgradeString;
                            }
                        }
                    } else {
                        if (potionName.getPotionItemType() == PotionName.PotionItemType.POTION && potionName.getPotionType() == potionType) {
                            return potionName.getLocalizedName() + upgradeString;
                        }
                    }
                } else {
                    if (Utils.getMajorVersion() >= 9) {
                        if (material == Material.LINGERING_POTION) {
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
            }
        }

        for (ItemName itemName : itemNames) {
            if (itemName.getMaterial() == Material.MONSTER_EGG && material == Material.MONSTER_EGG) {
                EntityType spawnedType = SpawnEggMeta.getEntityTypeFromItemStack(plugin, stack);

                for (EntityName entityName : entityNames) {
                    if (entityName.getEntityType() == spawnedType) {
                        return itemName.getLocalizedName() + " " + entityName.getLocalizedName();
                    }
                }

                return itemName.getLocalizedName() + " " + formatDefaultString(String.valueOf(spawnedType));

            }

            if ((itemName.getSubID() == subID) && (itemName.getMaterial() == material)) {
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
        String enchantmentString = formatDefaultString(enchantment.getName());
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
     * @param itemStack Potion Item whose base effect name should be looked up
     * @return Localized Name of the Base Potion Effect
     */
    public static String getPotionEffectName(ItemStack itemStack) {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        PotionType potionType;
        boolean upgraded;

        if (Utils.getMajorVersion() < 9) {
            potionType = Potion.fromItemStack(itemStack).getType();
            upgraded = Potion.fromItemStack(itemStack).getLevel() == 2;
        } else {
            potionType = potionMeta.getBasePotionData().getType();
            upgraded = potionMeta.getBasePotionData().isUpgraded();
        }

        String potionEffectString = formatDefaultString(potionType.toString());
        String upgradeString = upgraded ? "II" : "";

        for (PotionEffectName potionEffectName : potionEffectNames) {
            if (potionEffectName.getEffect() == potionType) {
                potionEffectString = potionEffectName.getLocalizedName();
            }
        }

        return potionEffectString + (upgradeString.length() > 0 ? " " + upgradeString : "");
    }

    /**
     * @param musicDiscMaterial Material of the Music Disc whose name should be looked up
     * @return Localized title of the Music Disc
     */
    public static String getMusicDiscName(Material musicDiscMaterial) {
        for (MusicDiscName musicDiscName : musicDiscNames) {
            if (musicDiscMaterial == musicDiscName.getMusicDiscMaterial()) {
                return musicDiscName.getLocalizedName();
            }
        }

        return "";
    }

    /**
     * @param message Message which should be translated
     * @param replacedRegexes Regexes which might be required to be replaced in the message
     * @return Localized Message
     */
    public static String getMessage(LocalizedMessage.Message message, LocalizedMessage.ReplacedRegex... replacedRegexes) {
        String _message = ChatColor.RED + "An error occurred: Message not found: " + message.toString();

        ArrayList<Regex> neededRegexes = new ArrayList<>();
        ArrayList<Regex> usedRegexes = new ArrayList<>();

        for (LocalizedMessage localizedMessage : messages) {
            if (localizedMessage.getMessage() == message) {
                _message = localizedMessage.getLocalizedString();
                for (LocalizedMessage.ReplacedRegex replacedRegex : replacedRegexes) {
                    neededRegexes.add(replacedRegex.getRegex());
                    for (int i = 0; i < localizedMessage.getRegexes().length; i++) {
                        if (localizedMessage.getRegexes()[i] == replacedRegex.getRegex()) {
                            Regex regex = replacedRegex.getRegex();
                            String toReplace = replacedRegex.getReplace();
                            if (regex == Regex.BUY_PRICE || regex == Regex.SELL_PRICE || regex == Regex.MIN_PRICE || regex == Regex.CREATION_PRICE) {
                                if (!toReplace.equals(getMessage(LocalizedMessage.Message.SHOP_INFO_DISABLED))) {
                                    double price = Double.parseDouble(toReplace);
                                    toReplace = plugin.getEconomy().format(price);
                                }
                            }
                            _message = _message.replace(regex.getName(), toReplace);
                            usedRegexes.add(regex);
                            break;
                        }
                    }
                }
            }
        }

        if (!neededRegexes.containsAll(usedRegexes)) {
            for (Regex regex : usedRegexes) {
                if (!neededRegexes.contains(regex)) {
                    plugin.getLogger().warning("Regex '" + regex.toString() + "' was not used in message '" + message.toString() + "'");
                }
            }
        }

        return _message;
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

