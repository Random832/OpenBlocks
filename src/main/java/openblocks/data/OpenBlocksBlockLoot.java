package openblocks.data;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import openblocks.OpenBlocks;

import java.util.Set;

public class OpenBlocksBlockLoot extends BlockLootSubProvider {
	public OpenBlocksBlockLoot(HolderLookup.Provider provider) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
	}

	@Override
	protected void generate() {
		dropSelf(OpenBlocks.TANK_BLOCK.get());
		dropSelf(OpenBlocks.DRAIN_BLOCK.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return OpenBlocks.BLOCKS.getEntries().stream().map(Holder::value).toList();
	}
}