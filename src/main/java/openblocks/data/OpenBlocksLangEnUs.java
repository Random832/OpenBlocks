package openblocks.data;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import openblocks.OpenBlocks;
import openblocks.common.block.ImaginaryBlock;

public class OpenBlocksLangEnUs extends OpenBlocksLangProviderBase {
    public OpenBlocksLangEnUs(PackOutput output) {
        super(output, "en_US");
    }

    void addWithDescription(ItemLike block, String name, String description) {
        add(block.asItem().getDescriptionId(), name);
        add(block.asItem().getDescriptionId() + ".description", description);
    }

    @Override
    protected void addTranslations() {
        //add("itemGroup.openblocks", "OpenBlocks");
        addWithDescription(OpenBlocks.LADDER, "Jaded Ladder", "The jaded ladder solves that annoying problem of jumping out of a trapdoor at the top of a ladder. The jaded ladder acts as both a trap door and a ladder at the same time.");
        addWithDescription(OpenBlocks.GUIDE_BLOCK, "Building Guide", "The building guide, once powered with redstone, will give you an outline of ghost blocks in different shapes and sizes that'll help you plan out rooms.\nUse touch-buttons on block to change outline dimensions and shapes.\nColor of markers can be changed by using dye on central block.");
        addWithDescription(OpenBlocks.BUILDER_GUIDE_BLOCK, "Enhanced Building Guide", "This enhanced version of building guide not only displays ghost blocks to make building easier, but also allows you to place actual blocks. Just click central block with any block stack!\nWhen in creative mode you can place an obsidian block above, then hit the building guide with any block you like to automatically create the shape.");
        //addWithDescription("block.openblocks.vacuum_hopper", "Vacuum Hopper", "block.openblocks.vacuum_hopper.description", "The vacuum hopper will suck up items and XP orbs around it. You can use the tabs on the right of the interface to select which sides to output to.");
        //addWithDescription("block.openblocks.elevator.name", "Elevator", "Place one elevator directly three or more blocks above another with no blocks inbetween and you'll be able to either 'jump' to move up, or 'crouch' to move down. Elevators can be dyed by clicking on them with dye, however, you can only teleport between elevators of the same color.");
        //add("block.openblocks.rotating_elevator.name", "Rotating Elevator");
        //add("block.openblocks.heal", "Healer");
        addWithDescription(OpenBlocks.TANK_BLOCK, "Tank", "Tanks can hold liquids. If you place tanks next to each other and they'll distribute their liquid between the tanks. You can place liquid into the tanks using buckets. If the tank contains XP Juice you can click on them with an empty hand to give yourself some of the XP. When broken tanks will retain their liquid.");
        add("block.openblocks.tank.filled", "%s Tank");
        addWithDescription(OpenBlocks.SLIMALYZER, "Slimalyzer", "Walk around with the slimalyzer in your hand and it'll light up whenever you're in a slime spawning chunk.");
        addWithDescription(OpenBlocks.DRAIN_BLOCK, "XP Drain", "Place the XP drain above an OpenBlocks tank and stand on it. Your XP will drain into the tank, which can then be used for blocks such as the auto enchantment table or the auto anvil.");
        addWithDescription(OpenBlocks.WRENCH, "Big Metal Bar", "This is extremely sophisticated tool used to perform transformations of objects with octahedral symmetry.\n\n\nOr just big, dumb piece of metal you can use to rotate cubes and stuff.\n\nSo yeah, it's yet another wrench. I'm just trying to be original here, ok?");
        addWithDescription(OpenBlocks.CURSOR, "Cursor", "The cursor will let you click on blocks remotely.\nSimply shift-click onto a block to link the cursor, then use the cursor wherever you like to click on the target block. Be warned, by default the cursor uses up your XP relative to how far you are from the block you're clicking.");
        addWithDescription(OpenBlocks.SPONGE, "Sponge", "The sponge is a replacement for the vanilla sponge. Perfect for removing small pockets of lava or water. Be careful when using it on large areas of liquid though as it'll start to get quite messy.");
        addWithDescription(OpenBlocks.SPONGE_STICK, "Sponge", "The sponge on a stick is a tool for cleaning up liquids. Works just like the sponge, but is more mobile at the expense of wearing out.");
        add(OpenBlocks.LAVA_SPONGE.get(), "Lava Sponge");
        add(OpenBlocks.LAVA_SPONGE_STICK.get(), "Lava Sponge");
        add(OpenBlocks.XP_BUCKET.get(), "XP Bucket");
        add(OpenBlocks.TASTY_CLAY.get(), "Tasty Clay");
        add(OpenBlocks.CRAYON.get(), "Magic Crayon");
        add(OpenBlocks.PENCIL.get(), "Magic Pencil");
        add(OpenBlocks.PEDOMETER.get(), "Pedometer");
        add(OpenBlocks.BASTARD_GLASSES.get(), "Badass Glasses");
        add(OpenBlocks.CRAYON_GLASSES.get(), "Crayon Glasses");
        add(OpenBlocks.PENCIL_GLASSES.get(), "Pencil Glasses");
        add(OpenBlocks.TECHNICOLOR_GLASSES.get(), "Amazing Technicolor Glasses");
        add(OpenBlocks.XP_JUICE_STILL.value(), "Liquid XP");
        addImaginary(OpenBlocks.CRAYON_BLOCK, "Crayon", "Block");
        addImaginary(OpenBlocks.CRAYON_PANEL, "Crayon", "Panel");
        addImaginary(OpenBlocks.CRAYON_STAIRS, "Crayon", "Stairs");
        addImaginary(OpenBlocks.PENCIL_BLOCK, "Pencil", "Block");
        addImaginary(OpenBlocks.PENCIL_PANEL, "Pencil", "Panel");
        addImaginary(OpenBlocks.PENCIL_STAIRS, "Pencil", "Stairs");
        add(OpenBlocks.BIG_STONE_BUTTON.get(), "Big Stone Button");
        add(OpenBlocks.BIG_OAK_BUTTON.get(), "Big Oak Button");
        add(OpenBlocks.BIG_SPRUCE_BUTTON.get(), "Big Spruce Button");
        add(OpenBlocks.BIG_BIRCH_BUTTON.get(), "Big Birch Button");
        add(OpenBlocks.BIG_JUNGLE_BUTTON.get(), "Big Jungle Button");
        add(OpenBlocks.BIG_ACACIA_BUTTON.get(), "Big Acacia Button");
        add(OpenBlocks.BIG_CHERRY_BUTTON.get(), "Big Cherry Button");
        add(OpenBlocks.BIG_DARK_OAK_BUTTON.get(), "Big Dark Oak Button");
        add(OpenBlocks.BIG_MANGROVE_BUTTON.get(), "Big Mangrove Button");
        add(OpenBlocks.BIG_BAMBOO_BUTTON.get(), "Big Bamboo Button");
        //add("openblocks.gui.vacuumhopper", "Vacuum Hopper");
        //add("openblocks.gui.xp_outputs", "XP Outputs");
        //add("openblocks.gui.item_outputs", "Item Outputs:");
        add("openblocks.misc.box", "Dimensions: (%s,%s,%s):(%s,%s,%s)");
        add("openblocks.misc.mode", "Placement mode: %s");
        add("openblocks.misc.type", "Type: %s");
        add("openblocks.misc.uses", "Uses: %s");
        add("openblocks.misc.color", "Color: %s");
        add("openblocks.misc.shape", "Shape: %s");
        add("openblocks.misc.shape.sphere", "Sphere");
        add("openblocks.misc.shape.cylinder", "Cylinder");
        add("openblocks.misc.shape.cuboid", "Cuboid");
        add("openblocks.misc.shape.full_cuboid", "Full Cuboid");
        add("openblocks.misc.shape.dome", "Dome");
        add("openblocks.misc.shape.triangle", "Triangle");
        add("openblocks.misc.shape.pentagon", "Pentagon");
        add("openblocks.misc.shape.hexagon", "Hexagon");
        add("openblocks.misc.shape.octagon", "Octagon");
        add("openblocks.misc.shape.axes", "Axes");
        add("openblocks.misc.shape.planes", "Planes");
        add("openblocks.misc.change_mode", "Changing to %s mode");
        add("openblocks.misc.change_size", "Changing size to %sx%sx%s");
        add("openblocks.misc.change_box_size", "Changing size to (%s,%s,%s):(%s,%s,%s)");
        add("openblocks.misc.total_blocks", "Total block count: %s");
        add("openblocks.misc.pedometer.tracking_reset", "Tracking reset");
        add("openblocks.misc.pedometer.tracking_started", "Tracking started");
        add("openblocks.misc.pedometer.start_point", "Start point: %s");
        add("openblocks.misc.pedometer.speed", "Speed: %s");
        add("openblocks.misc.pedometer.avg_speed", "Average speed: %s");
        add("openblocks.misc.pedometer.total_distance", "Total distance: %s");
        add("openblocks.misc.pedometer.straight_line_distance", "Straight line distance: %s");
        add("openblocks.misc.pedometer.straight_line_speed", "Straight line speed: %s");
        add("openblocks.misc.pedometer.last_check_distance", "Distance from last check: %s");
        add("openblocks.misc.pedometer.last_check_speed", "Last check speed: %s");
        add("openblocks.misc.pedometer.last_check_time", "Time from last check: %s ticks");
        add("openblocks.misc.pedometer.total_time", "Total time: %s ticks");
        add("openblocks.misc.target_pos", "Position: %s");
        add("openblocks.misc.target_dim", "Dimension: %s");
        add("openblocks.misc.target_face", "Side: %s");
        add("openblocks.misc.target_name", "Name: %s");
        add("openblocks.misc.side.east", "East side");
        add("openblocks.misc.side.west", "West side");
        add("openblocks.misc.side.north", "North side");
        add("openblocks.misc.side.south", "South side");
        add("openblocks.misc.side.up", "Top side");
        add("openblocks.misc.side.down", "Bottom side");
        add("openblocks.misc.xp_cost", "Cost: %s XP (%s Levels)");
        add("openblocks.misc.dimension.overworld", "Overworld");
        add("openblocks.misc.dimension.the_nether", "The Nether");
        add("openblocks.misc.dimension.the_End", "The End");

        validate();
    }

    private void addImaginary(DeferredBlock<ImaginaryBlock> block, String kind, String shape) {
        add(block.get(), kind + " " + shape);
        add(block.get().getDescriptionId() + ".inverted", "Inverted " + kind + " " + shape);
        add(block.get().getDescriptionId() + ".placement", shape);
        add(block.get().getDescriptionId() + ".placement.inverted", "Inverted " + shape);
    }

    private void add(Fluid value, String name) {
        add(value.getFluidType().getDescriptionId(), name);
        add(value.defaultFluidState().createLegacyBlock().getBlock(), name);
    }
}