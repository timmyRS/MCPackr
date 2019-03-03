# MCPackr

*Minecraft Resource Pack Porting Made Easy.*

If you have any resource pack and would like to create ports for every version from 1.6.1 onwards, you can simply download and execute the latest MCPackr build in the directory of your resource pack and it will generate zip files for all resource pack format versions.

However, to get the best possible ports, you will need to provide version-specific files.
You can easily do that by appending `@1` to the name of the file, so that you will have `inventory.png@1`, for example.

- `1` stands for 1.6.1 - 1.8.9
- `2` stands for 1.9 - 1.10.2
- `3` stands for 1.11 - 1.12.2
- `4` stands for 1.13+

Note that you don't have to provide a different `pack.mcmeta` file for each version — if you would like the resource pack description to contain the compatible Minecraft versions, you can place `%mcversions%` in it, and MCPackr will replace it with `1.6.1 - 1.8.9` in the 1.6.1 - 1.8.9 port, etc.

Some files which I recommend you provide, if your resource pack is 1.13+ — which it should be:

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
- Brewing stand without blaze powder:
  - `textures/gui/container/brewing_stand.png@1`

The rest should be done by MCPackr.

If something does not work as expected, please [open an issue](https://github.com/timmyrs/MCPackr/issues/new).

## For Developers

As a developer, you can use MCPackr as a library for your Java 7+ projects.

There are some dependencies to MCPackr, so either you download and use the binary any other user would use, as the libraries are bundled with it, or you use Maven:

    <repositories>
        <repository>
            <id>hellsh</id>
            <url>https://mvn2.hell.sh</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>de.timmyrs</groupId>
            <artifactId>mcpackr</artifactId>
            <version>[1.1.3,2.0.0)</version>
        </dependency>
    </dependencies>

Once you have MCPackr as a library, [the docs](https://timmyrs.github.io/MCPackr/) can tell you what it can do for you.
