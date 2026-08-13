package io.github.jason13official.shades.impl.common.block;

import io.github.jason13official.shades.impl.common.block.tile.SilimaticExposer;
import io.github.jason13official.shades.impl.common.registry.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SilimaticExposerBlock extends Block implements EntityBlock {

  public SilimaticExposerBlock(Properties properties) {
    super(properties);
  }

  @SuppressWarnings("unchecked")
  public static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actual, BlockEntityType<E> expected,
      BlockEntityTicker<? super E> ticker) {
    return expected == actual ? (BlockEntityTicker<A>) ticker : null;
  }

  protected static <T extends BlockEntity> @Nullable BlockEntityTicker<T> createTileTicker(Level level, BlockEntityType<T> actualType, BlockEntityType<? extends SilimaticExposer> expectedType) {

    if (level instanceof ServerLevel serverLevel) {
      return createTickerHelper(actualType, expectedType, (innerLevel, pos, state, entity) -> SilimaticExposer.serverTick(serverLevel, pos, state, entity));
    }

    return null;
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return new SilimaticExposer(blockPos, blockState);
  }

  @Override
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {

    return createTileTicker(level, type, ModTiles.SILIMATIC_EXPOSER);
  }
}
