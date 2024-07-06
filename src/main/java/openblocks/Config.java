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

	// Tank stuff
	public static int bucketsPerTank = 16;
	public static boolean shouldTanksUpdate = true;
	public static boolean displayAllFilledTanks = true;
	public static int tankFluidUpdateThreshold = 0;

	// XP fluid stuff [static info lives in FluidXpUtils]

	// TODO Not configurable yet [need to also figure out why fading doesn't work for pencil blocks]
	public static float imaginaryFadingSpeed = 0.0075f;

	// old default was actually 8 because it's squared distance, but I like having more distance now that my menu opening feature works
	public static double cursorDistanceLimit = 64;
	public static int cursorDurabilityBar = 100;
	public static double cursorCostPerMeter = 1;
	public static boolean enableCursorMenus = true;

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

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

	// a list of strings that are treated as resource locations for items
	private static final ModConfigSpec.ConfigValue<List<? extends String>> XP_FLUIDS = BUILDER
			.comment("Fluids on this list will be used as XP fluids before the ones in the tag.",
					"Using the tag at 20 mb per xp is hardcoded afterwards")
			.defineListAllowEmpty("xpFluids", List.of("openblocks:xp_juice:20", "#c:experience:20"), Config::validateXpFluidConversion);

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

	static final ModConfigSpec SPEC = BUILDER.build();

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

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent event) {
		bucketsPerTank = BUCKETS_PER_TANK.getAsInt();
		shouldTanksUpdate = SHOULD_TANKS_UPDATE.getAsBoolean();
		displayAllFilledTanks = DISPLAY_ALL_FLUIDS.getAsBoolean();
		tankFluidUpdateThreshold = UPDATE_THRESHOLD.getAsInt();
		cursorDistanceLimit = CURSOR_MAX_DISTANCE.getAsDouble();
		cursorCostPerMeter = CURSOR_XP_COST.getAsDouble();
		cursorDurabilityBar = CURSOR_BAR.getAsInt();
		enableCursorMenus = CURSOR_MENU_EXPERIMENTAL.getAsBoolean();
		FluidXpUtils.initializeFromConfig(XP_FLUIDS.get());
	}
}