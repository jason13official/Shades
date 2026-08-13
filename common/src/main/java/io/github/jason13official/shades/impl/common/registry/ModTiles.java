package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.impl.common.block.tile.SilimaticExposer;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModTiles {

  public static BlockEntityType<SilimaticExposer> SILIMATIC_EXPOSER;

  public static void register(BiConsumer<BlockEntityType<?>, Identifier> consumer) {

    SILIMATIC_EXPOSER = new BlockEntityType<>(SilimaticExposer::new, Set.of(ModBlocks.SILIMATIC_EXPOSER));

    consumer.accept(SILIMATIC_EXPOSER, Shades.identifier("silimatic_exposer"));
  }
}
