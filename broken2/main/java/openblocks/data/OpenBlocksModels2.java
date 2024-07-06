package openblocks.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import openblocks.OpenBlocks;
import openblocks.lib.block.LegacyBlockRotationMode;
import openblocks.lib.geometry.Orientation;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenBlocksModelsOld implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public OpenBlocksModelsOld(PackOutput pOutput) {
        this.blockStatePathProvider = pOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = pOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    private static class BlockModelProvider extends BlockModelGenerators {
        private final Consumer<BlockStateGenerator> blockStateOutput;
        private final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;

        private static final ModelTemplate CUBE_ALL_TINTED = new ModelTemplate(Optional.of(OpenBlocks.modLoc("block/cube_all_tinted")), Optional.empty(), TextureSlot.ALL);
        private static final ModelTemplate CUBE_TOP_TINTED = new ModelTemplate(Optional.of(OpenBlocks.modLoc("block/cube_top_tinted")), Optional.empty(), TextureSlot.TOP, TextureSlot.SIDE);

        public BlockModelProvider(Consumer<BlockStateGenerator> stateOutput, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, Consumer<Item> itemOutput) {
            super(stateOutput, modelOutput, itemOutput);
            this.blockStateOutput = stateOutput;
            this.modelOutput = modelOutput;
        }

        @Override
        public void run() {
            //makeCustomModel(OpenBlocks.Blocks.vacuumHopper);
            //registerItem(OpenBlocks.Blocks.vacuumHopper, ModelsResourceUtil.func_240222_a_(OpenBlocks.Blocks.vacuumHopper, "_body"));
            //makeElevators();
            //makeRotatingElevators();
            //makeSimpleBlock(OpenBlocks.Blocks.heal);
        }

        private void makeSimpleBlock(final Block block) {
            ResourceLocation model = TexturedModel.CUBE.create(block, this.modelOutput);
            blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, model)));
            registerItem(block, model);
        }

        private void makeCustomModel(final Block block) {
            blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block))));
        }

        private void makeElevators() {
            TextureMapping textures = new TextureMapping().put(TextureSlot.ALL, OpenBlocks.modLoc("block/elevator"));
            ResourceLocation model = CUBE_ALL_TINTED.create(OpenBlocks.modLoc("block/elevator"), textures, modelOutput);

            //makeElevator(OpenBlocks.Blocks.whiteElevator, model);
            //makeElevator(OpenBlocks.Blocks.orangeElevator, model);
            //makeElevator(OpenBlocks.Blocks.magentaElevator, model);
            //makeElevator(OpenBlocks.Blocks.lightBlueElevator, model);
            //makeElevator(OpenBlocks.Blocks.yellowElevator, model);
            //makeElevator(OpenBlocks.Blocks.limeElevator, model);
            //makeElevator(OpenBlocks.Blocks.pinkElevator, model);
            //makeElevator(OpenBlocks.Blocks.grayElevator, model);
            //makeElevator(OpenBlocks.Blocks.lightGrayElevator, model);
            //makeElevator(OpenBlocks.Blocks.cyanElevator, model);
            //makeElevator(OpenBlocks.Blocks.purpleElevator, model);
            //makeElevator(OpenBlocks.Blocks.blueElevator, model);
            //makeElevator(OpenBlocks.Blocks.brownElevator, model);
            //makeElevator(OpenBlocks.Blocks.greenElevator, model);
            //makeElevator(OpenBlocks.Blocks.redElevator, model);
            //makeElevator(OpenBlocks.Blocks.blackElevator, model);
        }

        private void makeElevator(Block block, ResourceLocation model) {
            blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, model)));
            registerItem(block, model);
        }

        private void makeRotatingElevators() {
            TextureMapping textures = new TextureMapping()
                    .put(TextureSlot.SIDE, OpenBlocks.modLoc("block/elevator"))
                    .put(TextureSlot.TOP, OpenBlocks.modLoc("block/elevator_rot"));
            ResourceLocation model = CUBE_TOP_TINTED.create(OpenBlocks.modLoc("block/rotating_elevator"), textures, modelOutput);

            //makeRotatingElevator(OpenBlocks.Blocks.whiteRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.orangeRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.magentaRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.lightBlueRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.yellowRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.limeRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.pinkRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.grayRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.lightGrayRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.cyanRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.purpleRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.blueRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.brownRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.greenRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.redRotatingElevator, model);
            //makeRotatingElevator(OpenBlocks.Blocks.blackRotatingElevator, model);
        }

        private void makeRotatingElevator(Block block, ResourceLocation model) {
            blockStateOutput.accept(MultiVariantGenerator
                    .multiVariant(block, Variant.variant().with(VariantProperties.MODEL, model))
                    .with(createFourDirectionOrientation())
            );
            registerItem(block, model);
        }

        private void registerItem(Block block, ResourceLocation model) {
            modelOutput.accept(ModelLocationUtils.getModelLocation(block.asItem()), new DelegatedModel(model));
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        Map<Block, BlockStateGenerator> states = Maps.newHashMap();
        Map<ResourceLocation, Supplier<JsonElement>> models = Maps.newHashMap();
        Consumer<BlockStateGenerator> stateOutput = generator -> states.put(generator.getBlock(), generator);
        BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput = (loc, model) -> models.put(loc, model);
        new BlockModelProvider(stateOutput, modelOutput, item -> {}).run();
        return CompletableFuture.allOf(
                this.saveCollection(pOutput, states, block -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().location())),
                this.saveCollection(pOutput, models, this.modelPathProvider::json)
        );
    }

    private <T> CompletableFuture<?> saveCollection(CachedOutput pOutput, Map<T, ? extends Supplier<JsonElement>> pObjectToJsonMap, Function<T, Path> pResolveObjectPath) {
        return CompletableFuture.allOf(pObjectToJsonMap.entrySet().stream().map(entry -> {
            return DataProvider.saveStable(pOutput, entry.getValue().get(), pResolveObjectPath.apply(entry.getKey()));
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "OpenMods Block State Definitions";
    }
}