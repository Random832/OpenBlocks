package openblocks.data;

import com.google.gson.JsonElement;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import openblocks.OpenBlocks;
import openblocks.common.block.BigButtonBlock;
import openblocks.lib.block.LegacyBlockRotationMode;
import openblocks.lib.geometry.Orientation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class OpenBlocksBlockStates extends BlockStateProvider {
	private Map<ResourceLocation, Supplier<JsonElement>> vanillaMap = new HashMap<>();
	private BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = vanillaMap::put;
	private final PackOutput.PathProvider modelPathProvider;


	public OpenBlocksBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
		super(output, OpenBlocks.MODID, exFileHelper);
		this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
	}

	@Override
	protected void registerStatesAndModels() {
		ModelFile.ExistingModelFile tankModel = models().getExistingFile(OpenBlocks.modLoc("block/tank"));
		ModelFile.ExistingModelFile drainModel = models().getExistingFile(OpenBlocks.modLoc("block/xp_drain"));
		simpleBlock(OpenBlocks.TANK_BLOCK.get(), tankModel);
		horizontalBlock(OpenBlocks.DRAIN_BLOCK.get(), drainModel);
		trapdoorBlock(OpenBlocks.LADDER.get(), OpenBlocks.modLoc("block/jaded_ladder"), true);
		simpleBlock(OpenBlocks.XP_JUICE_BLOCK.get(), models().getBuilder(key(OpenBlocks.XP_JUICE_BLOCK)).texture("particle", OpenBlocks.modLoc("block/xp_juice_still")));
		makeGuide(OpenBlocks.GUIDE_BLOCK, modLoc("block/guide_center_normal"));
		makeGuide(OpenBlocks.BUILDER_GUIDE_BLOCK, modLoc("block/guide_center_ender"));
		makeImaginary("crayon", OpenBlocks.CRAYON_BLOCK.get(), OpenBlocks.CRAYON_STAIRS.get(), OpenBlocks.CRAYON_PANEL.get());
		makeImaginary("pencil", OpenBlocks.PENCIL_BLOCK.get(), OpenBlocks.PENCIL_STAIRS.get(), OpenBlocks.PENCIL_PANEL.get());
		simpleBlockItem(OpenBlocks.DRAIN_BLOCK.get(), drainModel);
		simpleBlockItem(OpenBlocks.LADDER.get(), models().getBuilder(key(OpenBlocks.LADDER) + "_bottom"));
		makeBigButton(OpenBlocks.BIG_STONE_BUTTON, "stone");
		makeBigButton(OpenBlocks.BIG_OAK_BUTTON, "oak_planks");
		makeBigButton(OpenBlocks.BIG_SPRUCE_BUTTON, "spruce_planks");
		makeBigButton(OpenBlocks.BIG_BIRCH_BUTTON, "birch_planks");
		makeBigButton(OpenBlocks.BIG_JUNGLE_BUTTON, "jungle_planks");
		makeBigButton(OpenBlocks.BIG_ACACIA_BUTTON, "acacia_planks");
		makeBigButton(OpenBlocks.BIG_CHERRY_BUTTON, "cherry_planks");
		makeBigButton(OpenBlocks.BIG_DARK_OAK_BUTTON, "dark_oak_planks");
		makeBigButton(OpenBlocks.BIG_MANGROVE_BUTTON, "mangrove_planks");
		makeBigButton(OpenBlocks.BIG_BAMBOO_BUTTON, "bamboo_planks");
	}

	private void simpleBlockItem(DeferredBlock<?> block) {
		simpleBlockItem(block.get(), models().getBuilder(key(block)));
	}

	private void makeBigButton(DeferredBlock<BigButtonBlock> block, String texture) {
		ResourceLocation textureRL = mcLoc("block/" + texture);
		BlockModelBuilder model = models()
				.withExistingParent(key(block), modLoc("big_button_inactive"))
				.texture("all", textureRL);
		BlockModelBuilder modelP = models()
				.withExistingParent(key(block)+"_pressed", modLoc("big_button_active"))
				.texture("all", textureRL);
		buttonBlock(block.get(), model, modelP);
		itemModels()
				.withExistingParent(key(block), modLoc("block/big_button_inventory"))
				.texture("all", textureRL);
	}

	private void makeImaginary(String kind, Block block, Block stair, Block panel) {
		BlockModelBuilder stairModel = models()
				.withExistingParent(kind + "_stairs", modLoc("imaginary_stairs"))
				.texture("half_panel", "block/" + kind + "_half_panel");
		BlockModelBuilder upperPanelModel = models()
				.withExistingParent(kind + "_panel", modLoc("imaginary_panel"))
				.texture("full_panel", "block/" + kind + "_panel");
		BlockModelBuilder lowerPanelModel = models()
				.withExistingParent(kind + "_panel", modLoc("imaginary_half"))
				.texture("full_panel", "block/" + kind + "_panel");
		BlockModelBuilder blockModel = models()
				.withExistingParent(kind + "_block", modLoc("imaginary_block"))
				.texture("block", "block/" + kind + "_block");
		horizontalBlock(stair, stairModel, 0);
		getVariantBuilder(panel).forAllStates(state -> ConfiguredModel.builder()
				.modelFile(state.getValue(BlockStateProperties.HALF) == Half.TOP ? upperPanelModel : lowerPanelModel) // TODO make proper top model
				.build());
		simpleBlock(block, blockModel);
	}

	private void makeGuide(DeferredBlock<?> block, final ResourceLocation center) {
		BlockModelBuilder topModel = models()
				.withExistingParent(key(block), OpenBlocks.modLoc("block/template_guide"))
				.texture("side", "block/guide_side")
				.texture("end", "block/guide_top")
				.texture("torch", center);

		BlockModelBuilder sideModel = models()
				.withExistingParent(key(block) + "_horizontal", OpenBlocks.modLoc("block/template_guide_horizontal"))
				.texture("side", "block/guide_side")
				.texture("end", "block/guide_top")
				.texture("torch", center);

		simpleBlock(block.get(), topModel);
		//createThreeFourDispatch(block.get(), topModel, sideModel);
		simpleBlockItem(block.get(), topModel);
	}

	private void createThreeFourDispatch(Block block, ModelFile topModel, ModelFile sideModel) {
		getVariantBuilder(block).forAllStates(state -> {
			Orientation o = state.getValue(LegacyBlockRotationMode.THREE_FOUR_DIRECTIONS.getProperty());
			final ConfiguredModel.Builder<?> builder = ConfiguredModel.builder();
			return (switch(o) {
				case XP_YP -> builder.modelFile(topModel);
				case XN_YP -> builder.modelFile(topModel).rotationY(180);
				case ZP_YP -> builder.modelFile(topModel).rotationY(90);
				case ZN_YP -> builder.modelFile(topModel).rotationY(270);
				case YP_XN -> builder.modelFile(sideModel).rotationY(180).rotationX(270);
				case YN_XN -> builder.modelFile(sideModel).rotationY(180).rotationX(90);
				case ZP_XN -> builder.modelFile(sideModel).rotationY(180);
				case ZN_XN -> builder.modelFile(sideModel).rotationY(180).rotationX(180);
				case XP_ZN -> builder.modelFile(sideModel).rotationY(90);
				case XN_ZN -> builder.modelFile(sideModel).rotationY(90).rotationX(180);
				case YP_ZN -> builder.modelFile(sideModel).rotationY(90).rotationX(270);
				case YN_ZN -> builder.modelFile(sideModel).rotationY(90).rotationX(90);
                default -> throw new RuntimeException("unexpected orientation " + o + " in block " + block);
            }).build();});
	}

	private String key(DeferredHolder<?, ?> obj) {
		return obj.getKey().location().getPath();
	}
}
