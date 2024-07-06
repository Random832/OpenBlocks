package openblocks.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import openblocks.ModTags;
import openblocks.OpenBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class OpenBlocksTagProviders {

    public static class ItemTags extends ItemTagsProvider {
        public ItemTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> holderLookup, CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> tagLookup, String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput, holderLookup, tagLookup, modId, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookupProvider) {
            tag(ModTags.WRENCHES).add(OpenBlocks.WRENCH.get());
        }
    }

    public static class BlockTags extends BlockTagsProvider {
        public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, modId, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            tag(ModTags.CURSOR_ENABLE_MENU).add(
                    Blocks.CHEST, Blocks.BARREL,
                    Blocks.CRAFTING_TABLE, Blocks.STONECUTTER,
                    Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER);
        }
    }
}
