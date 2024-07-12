package openblocks;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import openblocks.lib.utils.FluidXpUtils;

import java.util.List;

@EventBusSubscriber(modid = OpenBlocks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {


	// TODO Not configurable yet [need to also figure out why fading doesn't work for pencil blocks]
	public static float imaginaryFadingSpeed = 0.0075f;

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //region tanks
	static {
		BUILDER.push("tanks");
	}

	public static int bucketsPerTank = 16;
	public static boolean shouldTanksUpdate = true;
	public static boolean displayAllFilledTanks = true;
	public static int tankFluidUpdateThreshold = 0;
	public static boolean tanksEmitLight = true;

	private static final ModConfigSpec.IntValue BUCKETS_PER_TANK = BUILDER
			.comment("The amount of buckets each tank can hold")
			.defineInRange("bucketsPerTank", 16, 1, 1048576);

	private static final ModConfigSpec.BooleanValue SHOULD_TANKS_UPDATE = BUILDER
			.comment("Should tanks try to balance liquid amounts with neighbours")
			.define("tankTicks", true);

	private static final ModConfigSpec.BooleanValue DISPLAY_ALL_FLUIDS = BUILDER
			.comment("Should filled tanks be searchable with creative menu")
			.define("displayAllFluids", true);

	private static final ModConfigSpec.IntValue UPDATE_THRESHOLD = BUILDER
			.comment("fluidDifferenceUpdateThreshold")
			.defineInRange("fluidDifferenceUpdateThreshold", 0, 0, Integer.MAX_VALUE);

	private static final ModConfigSpec.BooleanValue TANKS_EMIT_LIGHT = BUILDER
			.comment("Tanks will emit light when they contain a liquid that glows (eg. lava)")
			.define("emitLight", true);

	static {
		BUILDER.pop();
	}
	// endregion

	//region xp fluid
	// [static info lives in FluidXpUtils]
	private static final ModConfigSpec.ConfigValue<List<? extends String>> XP_FLUIDS = BUILDER
			.comment("Fluids on this list will be used as XP fluids before the ones in the tag.",
					"Using the tag at 20 mb per xp is hardcoded afterwards")
			.defineListAllowEmpty("xpFluids", List.of("openblocks:xp_juice:20", "#c:experience:20"), Config::validateXpFluidConversion);

	private static boolean validateXpFluidConversion(final Object obj) {
		if (!(obj instanceof String s)) return false;
		String[] split = s.split(":", 3);
		if (split.length != 3) return false;
		if (!ResourceLocation.isValidNamespace(split[0].startsWith("#") ? split[0].substring(1) : split[0]))
			return false;
		if (!ResourceLocation.isValidPath(split[1])) return false;
		try {
			Integer.parseInt(split[2]);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	// endregion

	//region cursor
	static {
		BUILDER.push("cursor");
	}

	// old default was actually 8 because it's squared distance, but I like having more distance now that my menu opening feature works
	public static double cursorDistanceLimit = 64;
	public static int cursorDurabilityBar = 100;
	public static double cursorCostPerMeter = 1;
	public static boolean enableCursorMenus = true;

	private static final ModConfigSpec.DoubleValue CURSOR_MAX_DISTANCE = BUILDER
			.comment("Maximum distance cursor can reach")
			.defineInRange("cursorMaxDistance", 64, -1, 1e8);

	private static final ModConfigSpec.DoubleValue CURSOR_XP_COST = BUILDER
			.comment("Cursor costs this many xp per block after 4 blocks")
			.defineInRange("cursorXpCost", 1.0, 0, 16);

	private static final ModConfigSpec.IntValue CURSOR_BAR = BUILDER
			.comment("Cursors display a durability bar showing how many uses are left. This is not actual tool durability, it is a measure of how much XP you can spend to operate the cursor.")
			.defineInRange("cursorDurabilityScale", 100, 1, 65535);

	private static final ModConfigSpec.BooleanValue CURSOR_MENU_EXPERIMENTAL = BUILDER
			.comment("Enable the experimental system for allowing the cursor to open containers at any distance.",
					"With this disabled, it will still send the block a click, but most menus will immediately close themselves.",
					"Certain vanilla containers are still enabled using a tag, this option enables it for any containers except those in a different tag.",
					"May cause crashes, please provide feedback.")
			.define("cursorMenusExperimental", true);

	static {
		BUILDER.pop();
	}
	//endregion

	//region sponge
	static {
		BUILDER.push("sponge");
	}

	public static boolean vanillaSpongeHack = true;
	public static boolean spongeBlockUpdate = false;
	public static boolean spongeStickBlockUpdate = false;
	public static int spongeRange = 3;
	public static int spongeStickRange = 3;
	public static boolean spongeWorksOnEverything = true;
	public static boolean spongeBurnsInAllHotFluids = true;
	public static int spongeStickMaxDamage = 256;

	private static final ModConfigSpec.IntValue SPONGE_MAX_DAMAGE = BUILDER
			.comment("SpongeOnAStick use count")
			.defineInRange("spongeStickUseCount", 256, 1, 65535);

	private static final ModConfigSpec.IntValue SPONGE_RANGE = BUILDER
			.comment("Sponge block range (distance from center)")
			.defineInRange("spongeRange", 3, 1, 16);

	private static final ModConfigSpec.IntValue SPONGE_STICK_RANGE = BUILDER
			.comment("Sponge-on-a-stick range (distance from center)")
			.defineInRange("stickRange", 3, 1, 16);

	private static final ModConfigSpec.BooleanValue SPONGE_WORKS_ON_EVERYTHING = BUILDER
			.comment("Sponges work on all fluids. If disabled, it will use a tag instead, containing water and lava by default.")
			.define("allFluids", false);

	private static final ModConfigSpec.BooleanValue SPONGE_BURNS_IN_HOT_FLUIDS = BUILDER
			.comment("Sponge burns up when absorbing any hot fluid (temperature > 800 K).")
			.comment("If disabled, it will use a tag, containing Lava by default.")
			.comment("Fluids in the tag burn the sponge whether or not they are hot.")
			.define("allHotFluidsBurn", false);

	private static final ModConfigSpec.BooleanValue SPONGE_BLOCK_UPDATE = BUILDER
			.comment("Should sponge block update neighbours after liquid removal?")
			.define("blockUpdate", false);

	private static final ModConfigSpec.BooleanValue SPONGE_STICK_BLOCK_UPDATE = BUILDER
			.comment("Should sponge-on-a-stick update neighbours after liquid removal?")
			.define("stickBlockUpdate", false);

	private static final ModConfigSpec.BooleanValue VANILLA_SPONGE_TICK_HACK = BUILDER
			.comment("Should vanilla sponge suppress fluid ticks after liquid removal?")
			.define("vanillaHack", true);

	static {
		BUILDER.pop();
	}
	// endregion

	static final ModConfigSpec SPEC = BUILDER.build();

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent event) {
		bucketsPerTank = BUCKETS_PER_TANK.getAsInt();
		shouldTanksUpdate = SHOULD_TANKS_UPDATE.getAsBoolean();
		displayAllFilledTanks = DISPLAY_ALL_FLUIDS.getAsBoolean();
		tankFluidUpdateThreshold = UPDATE_THRESHOLD.getAsInt();
		tanksEmitLight = TANKS_EMIT_LIGHT.getAsBoolean();

		cursorDistanceLimit = CURSOR_MAX_DISTANCE.getAsDouble();
		cursorCostPerMeter = CURSOR_XP_COST.getAsDouble();
		cursorDurabilityBar = CURSOR_BAR.getAsInt();
		enableCursorMenus = CURSOR_MENU_EXPERIMENTAL.getAsBoolean();

		spongeRange = SPONGE_RANGE.getAsInt();
		spongeRange = SPONGE_STICK_RANGE.getAsInt();
		spongeBlockUpdate = SPONGE_BLOCK_UPDATE.getAsBoolean();
		spongeStickBlockUpdate = SPONGE_STICK_BLOCK_UPDATE.getAsBoolean();
		spongeBurnsInAllHotFluids = SPONGE_BURNS_IN_HOT_FLUIDS.getAsBoolean();
		spongeWorksOnEverything = SPONGE_WORKS_ON_EVERYTHING.getAsBoolean();
		spongeStickMaxDamage = SPONGE_MAX_DAMAGE.getAsInt();
		vanillaSpongeHack = VANILLA_SPONGE_TICK_HACK.getAsBoolean();

		FluidXpUtils.initializeFromConfig(XP_FLUIDS.get());
	}
}