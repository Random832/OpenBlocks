package openblocks.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import openblocks.OpenBlocks;
import openblocks.common.block.BigButtonBlock;
import openblocks.lib.Log;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OpenBlocksRecipes extends RecipeProvider {
	public OpenBlocksRecipes(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
		super(pOutput, pRegistries);
	}

	Set<Item> uncraftableItems = new HashSet<>();
	Map<ResourceLocation, Recipe<?>> builtRecipes = new HashMap<>();

	void uncraftable(ItemLike item) {
		uncraftableItems.add(item.asItem());
	}

	@Override
	protected CompletableFuture<?> run(CachedOutput pOutput, HolderLookup.Provider pRegistries) {
		CompletableFuture<?> value = super.run(pOutput, pRegistries);
		Set<Item> craftableItems = new HashSet<>();
		for (Map.Entry<ResourceLocation, Recipe<?>> entry : builtRecipes.entrySet()) {
			Recipe<?> recipe = entry.getValue();
			ItemStack item = recipe.getResultItem(pRegistries);
			if(uncraftableItems.contains(item.getItem()))
				Log.warn("Recipe " + entry.getKey() + " has output " + item + " marked as uncraftable");
			craftableItems.add(item.getItem());
		}
		for (DeferredHolder<Item, ? extends Item> entry : OpenBlocks.ITEMS.getEntries()) {
			if(!(uncraftableItems.contains(entry.value()) || craftableItems.contains(entry.value())))
				Log.warn("No recipe found for" + entry.getKey());
		}
		for (DeferredHolder<Block, ? extends Block> entry : OpenBlocks.BLOCKS.getEntries()) {
			Item item = entry.get().asItem();
			if(item != Items.AIR && !(uncraftableItems.contains(item) || craftableItems.contains(item)))
				Log.warn("No recipe found for" + entry.getKey());
		}
		return value;
	}

	@Override
	protected void buildRecipes(RecipeOutput pConsumer) {
		RecipeOutput consumer = new RecipeOutput() {
			@Override
			public Advancement.Builder advancement() {
				return pConsumer.advancement();
			}

			@Override
			public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
				pConsumer.accept(id, recipe, advancement, conditions);
				builtRecipes.put(id, recipe);
			}
		};

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.TANK_BLOCK)
				.pattern("ogo")
				.pattern("ggg")
				.pattern("ogo")
				.define('o', Blocks.OBSIDIAN)
				.define('g', Tags.Items.GLASS_BLOCKS_CHEAP)
				.unlockedBy(getHasName(Blocks.GLASS), has(Blocks.GLASS))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.BUILDER_GUIDE_BLOCK) // builder guide
				.pattern("grg")
				.pattern("ete")
				.pattern("grg")
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('t', Items.TORCH)
				.define('e', Items.ENDER_PEARL)
				.define('g', Tags.Items.GLASS_BLOCKS_CHEAP)
				.unlockedBy(getHasName(OpenBlocks.GUIDE_BLOCK), has(OpenBlocks.GUIDE_BLOCK))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.GUIDE_BLOCK)
				.pattern("grg")
				.pattern("gtg")
				.pattern("grg")
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('t', Items.TORCH)
				.define('g', Tags.Items.GLASS_BLOCKS_CHEAP)
				.unlockedBy(getHasName(Items.REDSTONE), has(Tags.Items.DUSTS_REDSTONE))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.SLIMALYZER)
				.pattern("igi")
				.pattern("isi")
				.pattern("iri")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('g', Tags.Items.GLASS_PANES)
				.define('s', Tags.Items.SLIMEBALLS)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy(getHasName(Items.SLIME_BALL), has(Tags.Items.SLIMEBALLS))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.PEDOMETER)
				.pattern("www")
				.pattern("rcr")
				.pattern("www")
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('c', Items.CLOCK)
				.define('w', ItemTags.PLANKS)
				.unlockedBy(getHasName(Items.CLOCK), has(Items.CLOCK))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.WRENCH)
				.pattern(" ii")
				.pattern("iii")
				.pattern("ii ")
				.define('i', Tags.Items.INGOTS_IRON)
				.unlockedBy(getHasName(Items.IRON_INGOT), has(Tags.Items.INGOTS_IRON))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.DRAIN_BLOCK)
				.pattern("iii")
				.pattern("iii")
				.pattern("iii")
				.define('i', Items.IRON_BARS)
				.unlockedBy(getHasName(Items.IRON_BARS), has(Items.IRON_BARS))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OpenBlocks.CURSOR)
				.pattern("w  ")
				.pattern("www")
				.pattern("www")
				.define('w', ItemTags.WOOL)
				.unlockedBy("has_wool", has(ItemTags.WOOL))
				.save(consumer);

		ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, OpenBlocks.LADDER)
				.requires(Blocks.LADDER)
				.requires(ItemTags.WOODEN_TRAPDOORS)
				.unlockedBy("has_trapdoor", has(ItemTags.WOODEN_TRAPDOORS))
				.save(consumer);

		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, OpenBlocks.TASTY_CLAY, 2)
				.requires(Items.MILK_BUCKET)
				.requires(Items.CLAY_BALL)
				.requires(Items.COCOA_BEANS)
				.unlockedBy(getHasName(Items.CLAY_BALL), has(Items.CLAY_BALL))
				.save(consumer);

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, OpenBlocks.PENCIL)
				.requires(OpenBlocks.PENCIL)
				.requires(ItemTags.COALS)
				.requires(Tags.Items.RODS_WOODEN)
				.requires(Items.ENDER_EYE)
				.requires(Tags.Items.SLIMEBALLS)
				.unlockedBy(getHasName(Items.ENDER_EYE), has(Items.ENDER_EYE))
				.save(consumer);

		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, OpenBlocks.PENCIL_GLASSES)
				.requires(OpenBlocks.PENCIL)
				.requires(Items.PAPER)
				.unlockedBy(getHasName(OpenBlocks.PENCIL), has(OpenBlocks.PENCIL))
				.save(consumer);

		bigButton(consumer, OpenBlocks.BIG_STONE_BUTTON , Blocks.STONE_BUTTON );
		bigButton(consumer, OpenBlocks.BIG_OAK_BUTTON , Blocks.OAK_BUTTON );
		bigButton(consumer, OpenBlocks.BIG_SPRUCE_BUTTON , Blocks.SPRUCE_BUTTON );
		bigButton(consumer, OpenBlocks.BIG_BIRCH_BUTTON, Blocks.BIRCH_BUTTON);
		bigButton(consumer, OpenBlocks.BIG_JUNGLE_BUTTON, Blocks.JUNGLE_BUTTON);
		bigButton(consumer, OpenBlocks.BIG_ACACIA_BUTTON, Blocks.ACACIA_BUTTON);
		bigButton(consumer, OpenBlocks.BIG_CHERRY_BUTTON , Blocks.CHERRY_BUTTON );
		bigButton(consumer, OpenBlocks.BIG_DARK_OAK_BUTTON, Blocks.DARK_OAK_BUTTON);
		bigButton(consumer, OpenBlocks.BIG_MANGROVE_BUTTON, Blocks.MANGROVE_BUTTON);
		bigButton(consumer, OpenBlocks.BIG_BAMBOO_BUTTON , Blocks.BAMBOO_BUTTON );

		for (DyeColor color : DyeColor.values()) {
			Optional<Item> dye = BuiltInRegistries.ITEM.stream().filter(i -> i instanceof DyeItem d && d.getDyeColor() == color).findFirst();
			if(dye.isEmpty()) throw new RuntimeException("missing dye?");

			final ItemStack crayon = new ItemStack(OpenBlocks.CRAYON.get());
			crayon.set(OpenBlocks.IMAGINARY_COLOR, color);
			final ItemStack glasses = new ItemStack(OpenBlocks.CRAYON_GLASSES.get());
			glasses.set(OpenBlocks.IMAGINARY_COLOR, color);
			final Ingredient crayonIngredient = DataComponentIngredient.of(false, DataComponentPredicate.builder().expect(OpenBlocks.IMAGINARY_COLOR.get(), color).build(), OpenBlocks.CRAYON.get());

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, crayon)
					.requires(dye.get())
					.requires(Items.PAPER)
					.requires(Items.ENDER_EYE)
					.requires(Tags.Items.SLIMEBALLS)
					.unlockedBy(getHasName(Items.ENDER_EYE), has(Items.ENDER_EYE))
					.save(consumer, makeName(OpenBlocks.CRAYON, color.getSerializedName()));

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, glasses)
					.requires(crayonIngredient)
					.requires(Items.PAPER)
					.unlockedBy(getHasName(OpenBlocks.CRAYON), has(OpenBlocks.CRAYON))
					.save(consumer, makeName(OpenBlocks.CRAYON_GLASSES, color.getSerializedName()));
		}

		uncraftable(OpenBlocks.TECHNICOLOR_GLASSES);
		uncraftable(OpenBlocks.BASTARD_GLASSES);
		uncraftable(OpenBlocks.XP_BUCKET);
	}

	private void bigButton(RecipeOutput consumer, ItemLike bigButton, ItemLike button) {
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, bigButton)
				.pattern("bb")
				.pattern("bb")
				.define('b', button)
				.unlockedBy("has_button", has(button))
				.save(consumer);
	}

	private ResourceLocation makeName(DeferredItem<?> item, String suffix) {
		return OpenBlocks.modLoc(getSimpleRecipeName(item.get()) + "_" + suffix);
	}
}
