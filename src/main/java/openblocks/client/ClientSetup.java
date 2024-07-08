package openblocks.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import openblocks.OpenBlocks;
import openblocks.client.renderer.blockentity.GuideRenderer;
import openblocks.client.renderer.blockentity.ImaginaryBlockRenderer;
import openblocks.client.renderer.blockentity.tank.TankBEWLR;
import openblocks.common.item.ImaginationGlassesItem;
import openblocks.common.item.SlimalyzerItem;
import openblocks.common.item.TankItem;
import openblocks.lib.model.textureditem.TexturedItemModelLoader;
import openblocks.lib.model.variant.VariantModelLoader;
import openblocks.client.renderer.blockentity.tank.TankRenderer;

import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = OpenBlocks.MODID)
public class ClientSetup {

	@SubscribeEvent
	public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item evt) {
		evt.register(new TankItem.ColorHandler(), OpenBlocks.TANK_BLOCK);
		evt.register((pStack, pTintIndex) -> {
			ImaginationGlassesItem.Crayon item = ((ImaginationGlassesItem.Crayon) (pStack.getItem()));
			DyeColor color = item.getGlassesColor(pStack);
			if (color != null) return color.getTextureDiffuseColor();
			else return 0xffffffff;
		}, OpenBlocks.CRAYON_GLASSES);
		evt.register((pStack, pTintIndex) -> {
			if (pTintIndex != 1) return 0xffffffff;
			DyeColor color = pStack.get(OpenBlocks.IMAGINARY_COLOR);
			if (color != null) return color.getTextureDiffuseColor();
			else return 0; // hehe transparent uncraftable crayon
		}, OpenBlocks.CRAYON);
	}

	@SubscribeEvent
	public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(OpenBlocks.TANK_BE.get(), c -> TankBEWLR.tankRenderer = new TankRenderer(c));
		event.registerBlockEntityRenderer(OpenBlocks.GUIDE_BE.get(), c -> new GuideRenderer<>(c, ClientProxy.guideModelHolder));
		event.registerBlockEntityRenderer(OpenBlocks.IMAGINARY_BE.get(), ImaginaryBlockRenderer::new);
	}

	@SubscribeEvent
	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders evt) {
		evt.register(OpenBlocks.modLoc("variant"), new VariantModelLoader());
		evt.register(OpenBlocks.modLoc("textured_item"), new TexturedItemModelLoader());
	}

	@SubscribeEvent
	public static void registerModels(ModelEvent.RegisterAdditional evt) {
		ClientProxy.guideModelHolder.onModelRegister(evt);
	}

	@SubscribeEvent
	public static void onModelBake(ModelEvent.BakingCompleted evt) {
		ClientProxy.guideModelHolder.onModelBake(evt);
	}

	@SuppressWarnings("deprecation")
    @SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		//ClientRegistry.bindTileEntityRenderer();

		event.enqueueWork(() -> {
			ItemBlockRenderTypes.setRenderLayer(OpenBlocks.TANK_BLOCK.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(OpenBlocks.DRAIN_BLOCK.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(OpenBlocks.LADDER.get(), RenderType.cutout());
			ItemBlockRenderTypes.setRenderLayer(OpenBlocks.GUIDE_BLOCK.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(OpenBlocks.BUILDER_GUIDE_BLOCK.get(), RenderType.translucent());
		});

		event.enqueueWork(() -> {
			//ScreenManager.registerFactory(OpenBlocks.Containers.vacuumHopper, GuiVacuumHopper::new);
			// TODO fix the models that aren't using namespaces
			ItemProperties.register(OpenBlocks.TANK_BLOCK.asItem(), ResourceLocation.withDefaultNamespace("level"), (stack, level, entity, seed) -> {
				final FluidTank tank = TankItem.readTank(stack);
				return 16.0f * tank.getFluidAmount() / tank.getCapacity();
			});
			ItemProperties.register(OpenBlocks.SLIMALYZER.get(), OpenBlocks.modLoc("active"), (stack, level, entity, seed) ->
					SlimalyzerItem.isActive(stack) ? 2 : 0);
			ItemProperties.register(OpenBlocks.PEDOMETER.get(), ResourceLocation.withDefaultNamespace("speed"), (stack, level, entity, seed) ->
					entity == null ? 0 : (float) entity.getDeltaMovement().length());
		});
	}


	@SubscribeEvent
	public static void loadResources(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(ClientProxy.hitboxManager);
	}

	@SubscribeEvent
	public static void addPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
			var resourcePath = ModList.get().getModFileById(OpenBlocks.MODID).getFile().findResource("classic_tank");
			var supplier = new PathPackResources.PathResourcesSupplier(resourcePath);

            event.addRepositorySource(packConsumer -> {
				final PackLocationInfo packInfo = new PackLocationInfo(
						"builtin/classic_tank",
						Component.literal("Classic Tank Model"),
						PackSource.BUILT_IN,
						Optional.of(new KnownPack(OpenBlocks.MODID, "builtin/classic_tank", "1.0")));
				final PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
				packConsumer.accept(Pack.readMetaAndCreate(packInfo, supplier, PackType.CLIENT_RESOURCES, selectionConfig));
			});
		}
	}
}