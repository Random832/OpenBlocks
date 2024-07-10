package openblocks;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.*;
import openblocks.common.GuideActionHandler;
import openblocks.common.PedometerHandler;
import openblocks.common.block.*;
import openblocks.common.blockentity.*;
import openblocks.common.item.*;
import openblocks.common.support.ImaginaryPlacementMode;
import openblocks.data.*;
import openblocks.events.GuideActionEvent;
import openblocks.events.PlayerActionEvent;
import openblocks.lib.item.ICreativeVariantsItem;
import openblocks.rubbish.BrickManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(OpenBlocks.MODID)
@EventBusSubscriber(modid = OpenBlocks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class OpenBlocks {
	public static final String MODID = "openblocks";

	public static DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
	static DeferredRegister<CreativeModeTab> CREATIVE = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	public static DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
	static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
	static DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
	static DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MODID);
	static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
	static DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

	//region Blocks
	public static final DeferredBlock<TankBlock> TANK_BLOCK = BLOCKS.register("tank", () ->
			new TankBlock(Block.Properties.ofFullCopy(Blocks.GLASS).noOcclusion()));
	public static final DeferredBlock<XpDrainBlock> DRAIN_BLOCK = BLOCKS.registerBlock("xp_drain", XpDrainBlock::new, Block.Properties.ofFullCopy(Blocks.GLASS).noOcclusion());
	public static final DeferredBlock<BlockGuide> GUIDE_BLOCK = BLOCKS.registerBlock("guide", BlockGuide::new, Block.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
	public static final DeferredBlock<BlockBuilderGuide> BUILDER_GUIDE_BLOCK = BLOCKS.registerBlock("building_guide", BlockBuilderGuide::new, Block.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
	public static final DeferredBlock<JadedLadderBlock> LADDER = BLOCKS.registerBlock("ladder", JadedLadderBlock::new, Block.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR));
	public static final DeferredBlock<ImaginaryBlock> CRAYON_BLOCK = BLOCKS.registerBlock("crayon_block", ImaginaryBlock::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<ImaginaryBlock> CRAYON_STAIRS = BLOCKS.registerBlock("crayon_stairs", ImaginaryBlock.Stair::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<ImaginaryBlock> CRAYON_PANEL = BLOCKS.registerBlock("crayon_panel", ImaginaryBlock.Panel::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<ImaginaryBlock> PENCIL_BLOCK = BLOCKS.registerBlock("pencil_block", ImaginaryBlock::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<ImaginaryBlock> PENCIL_STAIRS = BLOCKS.registerBlock("pencil_stairs", ImaginaryBlock.Stair::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<ImaginaryBlock> PENCIL_PANEL = BLOCKS.registerBlock("pencil_panel", ImaginaryBlock.Panel::new, ImaginaryBlock.makeProperties());
	public static final DeferredBlock<BigButtonBlock> BIG_STONE_BUTTON = BLOCKS.registerBlock(BlockSetType.STONE.name() + "_big_button", p -> new BigButtonBlock(BlockSetType.STONE, 20, p), Block.Properties.ofFullCopy(Blocks.STONE_BUTTON));
	public static final DeferredBlock<BigButtonBlock> BIG_OAK_BUTTON = woodBigButton(BlockSetType.OAK);
	public static final DeferredBlock<BigButtonBlock> BIG_SPRUCE_BUTTON = woodBigButton(BlockSetType.SPRUCE);
	public static final DeferredBlock<BigButtonBlock> BIG_BIRCH_BUTTON = woodBigButton(BlockSetType.BIRCH);
	public static final DeferredBlock<BigButtonBlock> BIG_JUNGLE_BUTTON = woodBigButton(BlockSetType.JUNGLE);
	public static final DeferredBlock<BigButtonBlock> BIG_ACACIA_BUTTON = woodBigButton(BlockSetType.ACACIA);
	public static final DeferredBlock<BigButtonBlock> BIG_CHERRY_BUTTON = woodBigButton(BlockSetType.CHERRY);
	public static final DeferredBlock<BigButtonBlock> BIG_DARK_OAK_BUTTON = woodBigButton(BlockSetType.DARK_OAK);
	public static final DeferredBlock<BigButtonBlock> BIG_MANGROVE_BUTTON = woodBigButton(BlockSetType.MANGROVE);
	public static final DeferredBlock<BigButtonBlock> BIG_BAMBOO_BUTTON = woodBigButton(BlockSetType.BAMBOO);
	public static final DeferredBlock<LiquidBlock> XP_JUICE_BLOCK = BLOCKS.register("xp_juice", () ->
			new LiquidBlock((FlowingFluid) OpenBlocks.XP_JUICE_STILL.value(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_LIGHT_GREEN)));

	public static final DeferredBlock<SpongeBlock> SPONGE = BLOCKS.registerBlock("sponge", SpongeBlock::makeWaterSponge, Block.Properties.ofFullCopy(Blocks.SPONGE));
	public static final DeferredBlock<SpongeBlock> LAVA_SPONGE = BLOCKS.registerBlock("lava_sponge", SpongeBlock::makeLavaSponge, Block.Properties.ofFullCopy(Blocks.SPONGE));

	private static DeferredBlock<BigButtonBlock> woodBigButton(BlockSetType type) {
		return BLOCKS.registerBlock(type.name() + "_big_button", p -> new BigButtonBlock(type, 30, p), Block.Properties.ofFullCopy(Blocks.OAK_BUTTON));
	}
	//endregion

	//region Block Entities
	private static @NotNull <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(BlockEntityType.BlockEntitySupplier<T> factory, DeferredBlock<?>... block) {
		//noinspection DataFlowIssue
		return BLOCK_ENTITIES.register(block[0].getKey().location().getPath(),
				() -> BlockEntityType.Builder.of(factory, Arrays.stream(block).map(Supplier::get).toArray(Block[]::new)).build(null));
	}

	public static final Supplier<BlockEntityType<TankBlockEntity>> TANK_BE = registerBlockEntity(TankBlockEntity::new, TANK_BLOCK);
	public static final Supplier<BlockEntityType<XpDrainBlockEntity>> DRAIN_BE = registerBlockEntity(XpDrainBlockEntity::new, DRAIN_BLOCK);
	public static final Supplier<BlockEntityType<TileEntityGuide>> GUIDE_BE = registerBlockEntity(TileEntityGuide::new, GUIDE_BLOCK);
	public static final Supplier<BlockEntityType<TileEntityBuilderGuide>> BUILDER_GUIDE_BE = registerBlockEntity(TileEntityBuilderGuide::new, BUILDER_GUIDE_BLOCK);
	public static final Supplier<BlockEntityType<ImaginaryBlockEntity>> IMAGINARY_BE = registerBlockEntity(ImaginaryBlockEntity::new,
			CRAYON_BLOCK, CRAYON_STAIRS, CRAYON_PANEL, PENCIL_BLOCK, PENCIL_STAIRS, PENCIL_PANEL);
	//endregion

	//region Components
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE_COMPONENT = DATA_COMPONENTS.registerComponentType(
			"active", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<DyeColor>> IMAGINARY_COLOR = DATA_COMPONENTS.registerComponentType(
			"crayon_color", b -> b.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC));
	public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID_COMPONENT = DATA_COMPONENTS.registerComponentType(
			"fluid", b -> b.persistent(SimpleFluidContent.CODEC));
	public static final Supplier<DataComponentType<ImaginaryPlacementMode>> IMAGINARY_MODE = DATA_COMPONENTS.registerComponentType(
			"placement_mode", b -> b.persistent(ImaginaryPlacementMode.CODEC));
	public static final Supplier<DataComponentType<GlobalPos>> TARGET_POS_COMPONENT = DATA_COMPONENTS.registerComponentType(
			"target_pos", b -> b.persistent(GlobalPos.CODEC));
	public static final Supplier<DataComponentType<Direction>> TARGET_FACE_COMPONENT = DATA_COMPONENTS.registerComponentType(
			"target_face", b -> b.persistent(Direction.CODEC));
	public static final Supplier<DataComponentType<Component>> TARGET_NAME_COMPONENT = DATA_COMPONENTS.registerComponentType(
			"target_name", b -> b.persistent(ComponentSerialization.CODEC));
	//endregion

	//region Items
	public static final DeferredItem<SlimalyzerItem> SLIMALYZER = ITEMS.registerItem("slimalyzer", SlimalyzerItem::new);
	public static final DeferredItem<Item> TASTY_CLAY = ITEMS.registerItem("tasty_clay", TastyClayItem::new, new Item.Properties()
			.food(new FoodProperties.Builder()
					.nutrition(1).saturationModifier(0.1f)
					.fast().alwaysEdible().build()));
	public static final DeferredItem<ImaginationGlassesItem> PENCIL_GLASSES = registerNonStackingItem("glasses_pencil", ImaginationGlassesItem.Pencil::new);

	private static <T extends Item> DeferredItem<T> registerNonStackingItem(String name, Function<Item.Properties, T> factory) {
		return ITEMS.registerItem(name, factory, new Item.Properties().stacksTo(1));
	}

	public static final DeferredItem<ImaginationGlassesItem> CRAYON_GLASSES = registerNonStackingItem("glasses_crayon", ImaginationGlassesItem.Crayon::new);
	public static final DeferredItem<ImaginationGlassesItem> TECHNICOLOR_GLASSES = registerNonStackingItem("glasses_technicolor", ImaginationGlassesItem.Technicolor::new);
	public static final DeferredItem<ImaginationGlassesItem> BASTARD_GLASSES = registerNonStackingItem("glasses_admin", ImaginationGlassesItem.Bastard::new);
	public static final DeferredItem<PedometerItem> PEDOMETER = registerNonStackingItem("pedometer", PedometerItem::new);
	public static final DeferredItem<WrenchItem> WRENCH = registerNonStackingItem("wrench", WrenchItem::new);
	public static final DeferredItem<CursorItem> CURSOR = registerNonStackingItem("cursor", CursorItem::new);
	public static final DeferredItem<BucketItem> XP_BUCKET =
			ITEMS.register("xp_bucket", () -> new BucketItem(OpenBlocks.XP_JUICE_STILL.value(), new Item.Properties()));
	// todo can we make durability configurable?
	public static final DeferredItem<ImaginaryDrawItem> PENCIL = ITEMS.registerItem("pencil",
			p -> new ImaginaryDrawItem(p, PENCIL_BLOCK, PENCIL_PANEL, PENCIL_STAIRS),
			new Item.Properties().durability(40));
	public static final DeferredItem<ImaginaryDrawItem> CRAYON = ITEMS.registerItem("crayon",
			p -> new ImaginaryDrawItem(p, CRAYON_BLOCK, CRAYON_PANEL, CRAYON_STAIRS),
			new Item.Properties().durability(40));

	public static final DeferredItem<SpongeStickItem> SPONGE_STICK = ITEMS.registerItem("sponge_stick", SpongeStickItem::makeWaterSponge, new Item.Properties()
			.durability(Config.spongeStickMaxDamage));
	public static final DeferredItem<SpongeStickItem> LAVA_SPONGE_STICK = ITEMS.registerItem("lava_sponge_stick", SpongeStickItem::makeLavaSponge, new Item.Properties()
			.durability(Config.spongeStickMaxDamage)
			.fireResistant());

	public static final DeferredItem<DebugProbeItem> DEBUG_PROBE = ITEMS.registerItem("debug_probe", DebugProbeItem::new, new Item.Properties());

	static {
		// we don't need fields for these
		registerSimpleBlockItem(TANK_BLOCK, (b, p) -> new TankItem(b, p
				.component(FLUID_COMPONENT, SimpleFluidContent.EMPTY)));
		registerSimpleBlockItem(GUIDE_BLOCK, ItemGuide::new);
		registerSimpleBlockItem(BUILDER_GUIDE_BLOCK, ItemGuide::new);
		registerSimpleBlockItem(DRAIN_BLOCK);
		registerSimpleBlockItem(LADDER);
		registerSimpleBlockItem(BIG_STONE_BUTTON);
		registerSimpleBlockItem(BIG_OAK_BUTTON);
		registerSimpleBlockItem(BIG_SPRUCE_BUTTON);
		registerSimpleBlockItem(BIG_BIRCH_BUTTON);
		registerSimpleBlockItem(BIG_JUNGLE_BUTTON);
		registerSimpleBlockItem(BIG_ACACIA_BUTTON);
		registerSimpleBlockItem(BIG_CHERRY_BUTTON);
		registerSimpleBlockItem(BIG_DARK_OAK_BUTTON);
		registerSimpleBlockItem(BIG_MANGROVE_BUTTON);
		registerSimpleBlockItem(BIG_BAMBOO_BUTTON);
		registerSimpleBlockItem(SPONGE);
		registerSimpleBlockItem(LAVA_SPONGE, BlockItem::new, new Item.Properties().fireResistant());
	} // block items

	private static <T extends Block> void registerSimpleBlockItem(DeferredBlock<T> block, BiFunction<T, Item.Properties, Item> factory, Item.Properties properties) {
		ITEMS.register(block.getId().getPath(), () -> factory.apply(block.get(), properties));
	}

	private static <T extends Block> void registerSimpleBlockItem(DeferredBlock<T> block, BiFunction<T, Item.Properties, Item> factory) {
		registerSimpleBlockItem(block, factory, new Item.Properties());
	}

	private static void registerSimpleBlockItem(DeferredBlock<?> block) {
		registerSimpleBlockItem(block, BlockItem::new, new Item.Properties());
	}
	// endregion

	//region Creative Tab
	static {
		CREATIVE.register(MODID, () ->
			CreativeModeTab.builder().title(Component.literal("OpenBlocks")).displayItems((pParameters, pOutput) -> {
				Set<Item> visited = new HashSet<>();
				for (DeferredHolder<Block, ? extends Block> entry : BLOCKS.getEntries()) {
					Item item = entry.get().asItem();
					if (item != Items.AIR) {
						if (item instanceof ICreativeVariantsItem vi)
							vi.fillItemGroup(pOutput);
						else
							pOutput.accept(item);
						visited.add(item);
					}
				}
				for (DeferredHolder<Item, ? extends Item> entry : ITEMS.getEntries()) {
					Item item = entry.get();
					if (visited.contains(item))
						continue;
					if (item instanceof ICreativeVariantsItem vi)
						vi.fillItemGroup(pOutput);
					else
						pOutput.accept(item);
					visited.add(item);
				}
			}).icon(() -> TASTY_CLAY.get().getDefaultInstance()).build());
		}
	// endregion

	//region Attachments
	public static final Supplier<AttachmentType<BrickManager.BowelContents>> BRICK_ATTACHMENT = ATTACHMENTS.register("bowels", () ->
			AttachmentType.builder(BrickManager.BowelContents::new).serialize(BrickManager.BowelContents.CODEC).build());
	public static final Supplier<AttachmentType<PedometerHandler.PedometerState>> PEDOMETER_ATTACHMENT = ATTACHMENTS.register("pedometer", () ->
			AttachmentType.builder(PedometerHandler.PedometerState::new).build());
	//endregion

	//region Sounds
	//public static final Supplier<SoundEvent> SOUND_TARGET_OPEN = registerSound("target.open");
	//public static final Supplier<SoundEvent> SOUND_TARGET_CLOSE = registerSound("target.close");
	//public static final Supplier<SoundEvent> SOUND_SQUEEGEE_USE = registerSound("squeegee.use");
	public static final Supplier<SoundEvent> SOUND_SLIMALYZER_SIGNAL = registerSound("slimalyzer.signal");
	public static final Supplier<SoundEvent> SOUND_PEDOMETER_USE = registerSound("pedometer.use");
	//public static final Supplier<SoundEvent> SOUND_LUGGAGE_WALK = registerSound("luggage.walk");
	//public static final Supplier<SoundEvent> SOUND_LUGGAGE_EAT_ITEM = registerSound("luggage.eat.item");
	//public static final Supplier<SoundEvent> SOUND_LUGGAGE_EAT_FOOD = registerSound("luggage.eat.food");
	//public static final Supplier<SoundEvent> SOUND_GRAVE_ROB = registerSound("grave.rob");
	//public static final Supplier<SoundEvent> SOUND_ELEVATOR_ACTIVATE = registerSound("elevator.activate");
	public static final Supplier<SoundEvent> SOUND_CRAYON_PLACE = registerSound("crayon.place");
	//public static final Supplier<SoundEvent> SOUND_CANNON_ACTIVATE = registerSound("cannon.activate");
	//public static final Supplier<SoundEvent> SOUND_BOTTLER_SIGNAL = registerSound("bottler.signal");
	public static final Supplier<SoundEvent> SOUND_BEST_FEATURE_EVER_FART = registerSound("best.feature.ever.fart");
	//public static final Supplier<SoundEvent> SOUND_BEARTRAP_OPEN = registerSound("beartrap.open");
	//public static final Supplier<SoundEvent> SOUND_BEARTRAP_CLOSE = registerSound("beartrap.close");
	//public static final Supplier<SoundEvent> SOUND_ANNOYING_VIBRATE = registerSound("annoying.vibrate");
	//public static final Supplier<SoundEvent> SOUND_ANNOYING_MOSQUITO = registerSound("annoying.mosquito");
	//public static final Supplier<SoundEvent> SOUND_ANNOYING_ALARMCLOCK = registerSound("annoying.alarmclock");

	private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
		return SOUND_EVENTS.register(name, SoundEvent::createVariableRangeEvent);
	}
	// endregion

	public static ResourceLocation modLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	//region Fluids
	public static Holder<FluidType> XP_JUICE_TYPE = FLUID_TYPES.register("xp_juice", () -> new FluidType(FluidType.Properties.create().lightLevel(15)) {
		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				private static final ResourceLocation TEXTURE_STILL = modLoc("block/xp_juice_still");
				private static final ResourceLocation TEXTURE_FLOWING = modLoc("block/xp_juice_flowing");

				@Override
				public ResourceLocation getStillTexture() {
					return TEXTURE_STILL;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return TEXTURE_FLOWING;
				}
			});
		}
	});

	// there is no elegant way to avoid repeating this twice.
	public static Holder<Fluid> XP_JUICE_STILL = FLUIDS.register("xp_juice",
			() -> new BaseFlowingFluid.Source(new BaseFlowingFluid.Properties(
					XP_JUICE_TYPE::value,
					OpenBlocks.XP_JUICE_STILL::value,
					OpenBlocks.XP_JUICE_FLOWING::value)
					.block(XP_JUICE_BLOCK)
					.bucket(XP_BUCKET)));
	public static Holder<Fluid> XP_JUICE_FLOWING = FLUIDS.register("xp_juice_flowing",
			() -> new BaseFlowingFluid.Flowing(new BaseFlowingFluid.Properties(
					XP_JUICE_TYPE::value,
					OpenBlocks.XP_JUICE_STILL::value,
					OpenBlocks.XP_JUICE_FLOWING::value)
					.block(XP_JUICE_BLOCK)
					.bucket(XP_BUCKET)));
	// endregion

	public OpenBlocks(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		CREATIVE.register(modEventBus);
		DATA_COMPONENTS.register(modEventBus);
		ATTACHMENTS.register(modEventBus);
		BLOCK_ENTITIES.register(modEventBus);
		FLUIDS.register(modEventBus);
		FLUID_TYPES.register(modEventBus);
		SOUND_EVENTS.register(modEventBus);
	}

	@SubscribeEvent
	public static void registerGenerators(final GatherDataEvent event) {
		final DataGenerator generator = event.getGenerator();
		final PackOutput packOutput = event.getGenerator().getPackOutput();
		final CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
		OpenBlocksTagProviders.BlockTags blockTagProvider;

		generator.addProvider(event.includeServer(), new OpenBlocksRecipes(packOutput, registries));
		generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(OpenBlocksBlockLoot::new, LootContextParamSets.BLOCK)), registries));
		generator.addProvider(event.includeServer(), blockTagProvider = new OpenBlocksTagProviders.BlockTags(packOutput, registries, event.getExistingFileHelper()));
		generator.addProvider(event.includeServer(), new OpenBlocksTagProviders.ItemTags(packOutput, registries, blockTagProvider.contentsGetter(), event.getExistingFileHelper()));
		generator.addProvider(event.includeServer(), new OpenBlocksTagProviders.FluidTags(packOutput, registries, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new OpenBlocksBlockStates(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new OpenBlocksItemModels(packOutput, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new OpenBlocksLangEnUs(packOutput));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TANK_BE.get(), (be, side) -> be.new ColumnFluidHandler());
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) -> new TankItem.FluidHandler(stack), TANK_BLOCK.asItem());
	}

	@SubscribeEvent
	public static void registerNetworkEvents(RegisterPayloadHandlersEvent event) {
		event.registrar("0")
				.commonToServer(GuideActionEvent.TYPE, GuideActionEvent.STREAM_CODEC, GuideActionHandler::onEvent)
				.commonToServer(PlayerActionEvent.TYPE, PlayerActionEvent.STREAM_CODEC, BrickManager::onPlayerScared);
	}
}