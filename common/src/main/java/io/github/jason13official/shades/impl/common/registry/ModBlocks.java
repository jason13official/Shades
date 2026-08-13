package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.impl.common.block.BrightsandBlock;
import io.github.jason13official.shades.impl.common.block.SilimaticExposerBlock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

  public static Block BRIGHTSAND;
  public static Block BRIGHTGLASS;

  public static Block SILIMATIC_EXPOSER;

  public static void register(BiConsumer<Block, Identifier> consumer) {

    // SAND = register("sand", (p) -> new SandBlock(new ColorRGBA(14406560), p),
    // Properties.of()
    // .mapColor(MapColor.SAND)
    // .instrument(NoteBlockInstrument.SNARE)
    // .strength(0.5F)
    // .sound(SoundType.SAND));

    BRIGHTSAND = register(
        "brightsand",
        BrightsandBlock::new,
        Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.5F).sound(SoundType.SAND)
            .lightLevel(s -> 7),
        consumer);

//    BRIGHTSAND_GLASS = register(
//        "brightsand_glass",
//        BrightsandGlassBlock::new,
//        Properties.of()
//            .instrument(NoteBlockInstrument.HAT)
//            .strength(0.3F)
//            .sound(SoundType.GLASS)
//            .noOcclusion()
//            .isValidSpawn(ModBlocks::never)
//            .isRedstoneConductor(ModBlocks::never)
//            .isSuffocating(ModBlocks::never)
//            .isViewBlocking(ModBlocks::never)
//            .lightLevel(s -> 7),
//        consumer);

    BRIGHTGLASS = registerStainedGlass("brightglass", DyeColor.LIGHT_BLUE, consumer);

    SILIMATIC_EXPOSER = register("silimatic_exposer", SilimaticExposerBlock::new, consumer);
  }

  /// @see Blocks
  private static Block registerStainedGlass(String id, DyeColor color, BiConsumer<Block, Identifier> consumer) {

    return register(id, (p) -> new StainedGlassBlock(color, p), Properties.of()
        .mapColor(color).instrument(NoteBlockInstrument.HAT).strength(0.3F)
        .sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 7)
        .isValidSpawn(ModBlocks::never).isRedstoneConductor(ModBlocks::never).isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never), consumer);
  }

  private static Block register(String id, BiConsumer<Block, Identifier> consumer) {
    return register(id, Properties.of(), consumer);
  }

  private static Block register(String id, Properties properties, BiConsumer<Block, Identifier> consumer) {
    return register(id, Block::new, properties, consumer);
  }

  private static Block register(String id, Function<Properties, Block> constructor, BiConsumer<Block, Identifier> consumer) {
    return register(id, constructor, Properties.of(), consumer);
  }

  private static Block register(String id, Function<Properties, Block> constructor, Properties properties, BiConsumer<Block, Identifier> consumer) {

    Identifier fullId = Shades.identifier(id);
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, fullId);
    properties = properties.setId(key);
    Block block = constructor.apply(properties);
    consumer.accept(block, fullId);

    return block;
  }

  /// @see Blocks
  private static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
    return false;
  }

  /// @see Blocks
  private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos blockPos) {
    return false;
  }
}
