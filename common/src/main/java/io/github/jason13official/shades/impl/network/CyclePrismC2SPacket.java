package io.github.jason13official.shades.impl.network;

import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.ShadesClient;
import io.github.jason13official.shades.impl.common.registry.ModComponents;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/// sent whenever the wearer presses [ShadesClient#CYCLE_PRISM_KEY]; server validates prism_shades
/// is still worn, then advances [ModComponents#PRISM_CYCLE_INDEX] on the real equipped stack.
///
/// Since we use `networkSynchronized`, that mutation reaches other tracking players via vanilla's
/// normal equipment-sync path, and reaches the wearer's own client via the normal container-slot sync
public record CyclePrismC2SPacket() implements CustomPacketPayload {

  public static final Type<CyclePrismC2SPacket> TYPE = new Type<>(Shades.identifier("cycle_prism"));

  public static final StreamCodec<io.netty.buffer.ByteBuf, CyclePrismC2SPacket> STREAM_CODEC =
      StreamCodec.unit(new CyclePrismC2SPacket());

  @Override
  public Type<CyclePrismC2SPacket> type() {
    return TYPE;
  }

  public static void handle(CyclePrismC2SPacket packet, ServerPlayer player) {

    ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
    if (headStack.getItem() != ModItems.PRISM_SHADES) {
      return;
    }

    int current = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    int next = (current + 1) % ShadesClient.prismCycleSize();
    headStack.set(ModComponents.PRISM_CYCLE_INDEX, next);
  }
}
