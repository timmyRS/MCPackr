# MCPackr

*Minecraft Resource Pack Backward Compatibility Made Easy.*

If you have a 1.13 resource pack and would like to create ports down to 1.6.1, you can simply [download](https://raw.githubusercontent.com/timmyrs/MCPackr/master/MCPackr.jar) and execute the `MCPackr.jar` the directory of your resource pack and it will generate zip files for all resource pack format versions.

However, to get the best possible ports, you will need to provide version-specific files.
You can easily do that by appending `@1` to the name of the file, so that you will have `inventory.png@1`, for example.

- `1` stands for 1.6.1 - 1.8.9
- `2` stands for 1.9 - 1.10.2
- `3` stands for 1.11 - 1.12.2
- `4` stands for 1.13 - 1.13.1

Note that you don't have to provide a different `pack.mcmeta` file for each version if you would like the resource pack description to contain the compatible Minecraft versions because you can place `%mcversions%` in it, which MCPackr will replace with `1.6.1 - 1.8.9` in the 1.6.1 - 1.8.9, `1.9 - 1.10.2` in the 1.9 - 1.10.2 port, and so on.

Some files which I recommend you provide:

- Inventory without offhand:
  - `textures/gui/container/inventory.png@1`
  - `textures/gui/container/creative_inventory/tab_inventory.png@1`
- Blue water:
  - `textures/block/water_flow.png@1`
  - `textures/block/water_still.png@1`
  - `textures/block/water_flow.png@2`
  - `textures/block/water_still.png@2`
  - `textures/block/water_flow.png@3`
  - `textures/block/water_still.png@3`
- Red bed blocks:
  - `textures/block/bed_feet_end.png@1`
  - `textures/block/bed_feet_side.png@1`
  - `textures/block/bed_feet_top.png@1`
  - `textures/block/bed_head_end.png@1`
  - `textures/block/bed_head_side.png@1`
  - `textures/block/bed_head_top.png@1`
  - `textures/block/bed_feet_end.png@2`
  - `textures/block/bed_feet_side.png@2`
  - `textures/block/bed_feet_top.png@2`
  - `textures/block/bed_head_end.png@2`
  - `textures/block/bed_head_side.png@2`
  - `textures/block/bed_head_top.png@2`

The rest should be done by MCPackr, e.g. converting `clock_**.png` and `compass_**.png` into `clock.png` and `compass.png` for the 1.6.1 - 1.8 port.

If something does not work as expected, please [open an issue](https://github.com/timmyrs/MCPackr/issues/new).

## For Developers

As a developer, you can use MCPackr as a library for your Java 7+ projects.

MCPackr depends on [minimal-json](https://github.com/ralfstx/minimal-json), which is bundled in the [MCPackr.jar](https://raw.githubusercontent.com/timmyrs/MCPackr/master/MCPackr.jar).
If your project is already using minimal-json, you can use [libMCPackr.jar](https://raw.githubusercontent.com/timmyrs/MCPackr/master/libMCPackr.jar), which doesn't bundle minimal-json, instead.

Once you have MCPackr as a library, [read the docs](https://timmyrs.github.io/MCPackr/) for more information.
