package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Items;

/// @see DataComponents
/// @see Items
public class ModComponents {

  /// based on [DataComponents#DAMAGE]
  public static DataComponentType<Integer> PRISM_CYCLE_INDEX;

  public static void register(BiConsumer<DataComponentType<?>, Identifier> consumer) {

    PRISM_CYCLE_INDEX = register("prism_cycle_index", p -> p.persistent(ExtraCodecs.NON_NEGATIVE_INT).ignoreSwapAnimation().networkSynchronized(ByteBufCodecs.VAR_INT), consumer);
  }

  private static <T> DataComponentType<T> register(String id, UnaryOperator<Builder<T>> builder, BiConsumer<DataComponentType<?>, Identifier> consumer) {

    DataComponentType<T> value = builder.apply(DataComponentType.builder()).build();

    consumer.accept(value, Shades.identifier(id));

    return value;
  }
}
