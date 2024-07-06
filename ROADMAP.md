# Current features
- Jaded Ladder
- Guide (Building Guide is untested, can't rotate)
- Tank
- Big Button
- Imaginary Blocks (Crayon, Pencil, Glasses etc)
- Slimalyzer
- XP Drain, XP Juice
- Cursor (Including experimental system to open any gui at any distance)
- Pedometer
- Tasty Clay (B keybind is not ported yet)

# New features
- Guide displays the name of the button you're pointing at on the screen.
  - Added for debug purposes but I think it's really useful. I'll add localization at some point. 
- Cursor shows info in tooltip and has a durability bar
- Changed default costs and range on cursor to be more lenient

# Roadmap

## Potential new features
- Use the Drain to collect rainwater or lava from dripstone
- Make the Tank act more like a proper multiblock
- Potion fluids with Shower
- Use potions to fuel Healer
- Pedometer gui instead of dumping everything in chat
- Craft flag by colors

## Features that were already ported to 1.16 in the repository
These will be easier to port than the others simply due to having less 1.12 jank in the code
- Elevator
- Vacuum Hopper
- Healer

## Seems simple and easy
Should be easy enough to fill out the mod content with these once I have the library reasonably well ported
- Bear Trap
- Fan
- Flag
- Golden Eye
- Path 
- Rope Ladder
- Sponge
  - This needs some thought on how it fits in with the vanilla sponge
- Tasty Clay
- XP Bottler [has a gui though]
- XP Shower

## More complex or difficult
I am not ruling out porting these, but they're going to be a sizable chunk of effort.
- Crane Backpack
- Donation Station
  - use data from mods.toml?
- Enhanced Building Guide
- Golden Egg
- Height Map Projector, Cartographer
- Item Cannon, Pointer
- Painting system
- Sky Block
- Sonic Glasses
- Sprinkler
- Trophy
- Flim Flam enchantment - seems like fun
- Last Stand enchantment - not sure about damage events
- Unstable enchantment - idk

Low priority for misc reasons
- Village Highlighter
  - Villages changed so much in 1.14 this would basically have to be rewritten from scratch, and it's not even clear how valid the concepts are.
- World Domination with OpenBlocks
  - whether it's even *worth* porting the book depends on how much other content gets ported.
  - i might just rewrite it in Mantle or Patchouli.
- Healer
  - Low priority due to creative only

Low priority due to other mods that already do the same thing
- Elevator, Grave, Hang Glider, Luggage
  - There are other mods with 1-for-1 implementations of these. I'm not *opposed* to either rolling these in (if they have compatible licenses) or trying to port forward, but they are very low priority because you can get them elsewhere
  - Kind of a paradox because these are the most iconic features of the original mod.
- Vacuum Hopper
  - Mob Grinding Utils has basically a 1-for-1 implementation, and there are lots of other mods with the same basic function.
- Target
  - Vanilla has one, though this one's model is pretty nice.
- Block Placer, Block Breaker, Auto Anvil, Auto Enchantment Table
  - Lots of other mods with these features, nothing particularly unique about this one
  - probably worth a shot for completeness if it's easy
- Item Dropper
  - Other mods have similar features, though I'm not necessarily opposed if it's easy
- Scaffolding
  - Vanilla has scaffolding, this block has a different niche but probably needs a reskin
- Sleeping Bag
  - Comforts does it better
- /dev/null
  - Might still be worth porting if it's easy, but Danks occupy this niche now with many more features.

# Where's OpenModsLib?

I didn't want to mess with getting a multiproject setup working, and I also want the freedom to move things around the package structure. Once things are more stable (or if someone else wants it to port OpenPeripheral) I might split it back out.

For now, the OpenModsLib classes will mostly live in the "lib" package, and the content (model loaders and such) is registered to the openblocks namespace.
