package openblocks.data;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import openblocks.OpenBlocks;
import openblocks.lib.Log;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

public class OpenBlocksBlockLoot extends BlockLootSubProvider {
	public OpenBlocksBlockLoot(HolderLookup.Provider provider) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
	}

	@Override
	protected void generate() {
		dropSelf(OpenBlocks.TANK_BLOCK.get());
		dropSelf(OpenBlocks.DRAIN_BLOCK.get());
		dropSelf(OpenBlocks.GUIDE_BLOCK.get());
		dropSelf(OpenBlocks.BUILDER_GUIDE_BLOCK.get());
		dropSelf(OpenBlocks.LADDER.get());
		dropSelf(OpenBlocks.BIG_STONE_BUTTON.get());
		dropSelf(OpenBlocks.BIG_OAK_BUTTON.get());
		dropSelf(OpenBlocks.BIG_SPRUCE_BUTTON.get());
		dropSelf(OpenBlocks.BIG_BIRCH_BUTTON.get());
		dropSelf(OpenBlocks.BIG_JUNGLE_BUTTON.get());
		dropSelf(OpenBlocks.BIG_ACACIA_BUTTON.get());
		dropSelf(OpenBlocks.BIG_CHERRY_BUTTON.get());
		dropSelf(OpenBlocks.BIG_DARK_OAK_BUTTON.get());
		dropSelf(OpenBlocks.BIG_MANGROVE_BUTTON.get());
		dropSelf(OpenBlocks.BIG_BAMBOO_BUTTON.get());
		dropSelf(OpenBlocks.SPONGE.get());
		dropSelf(OpenBlocks.LAVA_SPONGE.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return OpenBlocks.BLOCKS.getEntries().stream().map(Holder::value).toList();
	}

	// warn instead of throwing
	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
		this.generate();
		Set<ResourceKey<LootTable>> set = new HashSet<>();

		for (Block block : getKnownBlocks()) {
			ResourceKey<LootTable> resourcekey = block.getLootTable();
			if (resourcekey == BuiltInLootTables.EMPTY || !set.add(resourcekey)) continue;
			LootTable.Builder builder = this.map.remove(resourcekey);
			if (builder == null)
				Log.warn(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourcekey.location(), BuiltInRegistries.BLOCK.getKey(block)));
			else
				consumer.accept(resourcekey, builder);
		}
	}
}