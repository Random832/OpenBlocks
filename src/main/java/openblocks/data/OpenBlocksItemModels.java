package openblocks.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import openblocks.OpenBlocks;

public class OpenBlocksItemModels extends ItemModelProvider {

    public static final ResourceLocation GENERATED = ResourceLocation.withDefaultNamespace("item/generated");
    public static final ResourceLocation HANDHELD = ResourceLocation.withDefaultNamespace("item/handheld");

    public OpenBlocksItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OpenBlocks.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        getBuilder(key(OpenBlocks.XP_BUCKET))
                .parent(getExistingFile(ResourceLocation.parse("neoforge:item/bucket_drip")))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(OpenBlocks.XP_JUICE_STILL.value())
                .end();
        generated(OpenBlocks.PENCIL, HANDHELD);
        generated(OpenBlocks.CRAYON, HANDHELD, modLoc("item/crayon_1"), modLoc("item/crayon_2"));
        generated(OpenBlocks.PENCIL_GLASSES);
        generated(OpenBlocks.CRAYON_GLASSES);
        generated(OpenBlocks.TECHNICOLOR_GLASSES);
        generated(OpenBlocks.BASTARD_GLASSES);
        generated(OpenBlocks.TASTY_CLAY, GENERATED, modLoc("item/yum_yum"));
        generated(OpenBlocks.WRENCH, HANDHELD);
        generated(OpenBlocks.CURSOR);
        generated(OpenBlocks.SPONGE_STICK, HANDHELD, modLoc("item/sponge_on_a_stick"));
        generated(OpenBlocks.LAVA_SPONGE_STICK, HANDHELD, modLoc("item/lava_sponge_on_a_stick"));
        generated(OpenBlocks.DEBUG_PROBE, HANDHELD);
    }

    private String key(DeferredItem<?> holder) {
        return holder.getKey().location().getPath();
    }

    private ItemModelBuilder generated(DeferredItem<?> item) {
        return generated(item, GENERATED);
    }

    private ItemModelBuilder generated(DeferredItem<?> item, ResourceLocation parent, ResourceLocation... layers) {
        assert item.getKey().location().getNamespace().equals(modid);
        ItemModelBuilder m = withExistingParent(key(item), parent);
        if(layers.length == 0) {
            m.texture("layer0", "item/" + key(item));
        } else {
            for (int i = 0; i < layers.length; i++)
                m.texture("layer" + i, layers[i]);
        }
        return m;
    }
}