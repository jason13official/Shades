package io.github.jason13official.shades.impl.common.block.tile;

import io.github.jason13official.shades.impl.common.registry.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SilimaticExposer extends BlockEntity {

  public SilimaticExposer(BlockPos worldPosition, BlockState blockState) {
    super(ModTiles.SILIMATIC_EXPOSER, worldPosition, blockState);
  }

  public static void serverTick(ServerLevel serverLevel, BlockPos pos, BlockState state, SilimaticExposer exposer) {

  }
}
