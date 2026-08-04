package io.github.jason13official.shades.impl.network;

import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.ShadesClient;
import io.github.jason13official.shades.impl.common.registry.ModComponents;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import io.github.jason13official.shades.platform.Services;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/// sent whenever the wearer presses [ShadesClient#CYCLE_PRISM_KEY] (`reverse` true if Shift was
/// held); server validates prism_shades is still worn, then advances/retreats
/// [ModComponents#PRISM_CYCLE_INDEX] on the real equipped stack. Since that component is
/// `networkSynchronized`, the mutation reaches other tracking players via vanilla's normal
/// equipment-sync path, and the wearer via the normal container-slot sync
public record CyclePrismC2SPacket(boolean reverse) implements CustomPacketPayload {

  public static final Type<CyclePrismC2SPacket> TYPE = new Type<>(Shades.identifier("cycle_prism"));

  public static final StreamCodec<io.netty.buffer.ByteBuf, CyclePrismC2SPacket> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.BOOL, CyclePrismC2SPacket::reverse, CyclePrismC2SPacket::new);

  @Override
  public Type<CyclePrismC2SPacket> type() {
    return TYPE;
  }

  public static void handle(CyclePrismC2SPacket packet, ServerPlayer player) {

    ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
    if (headStack.getItem() == ModItems.PRISM_SHADES) {
      int current = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
      int next = Math.floorMod(current + (packet.reverse() ? -1 : 1), ShadesClient.prismCycleSize());
      headStack.set(ModComponents.PRISM_CYCLE_INDEX, next);
      return;
    }

    // not in the real head slot -> check an accessory slot (Trinkets/Curios, if installed);
    // those libraries own their own slot storage, so the mutated stack has to be written back
    // through setAccessoryShadesItem rather than mutated in place, unlike the real head slot
    ItemStack accessoryStack = Services.PLATFORM.getAccessoryShadesItem(player);
    if (accessoryStack.getItem() != ModItems.PRISM_SHADES) {
      return;
    }

    int current = accessoryStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    int next = Math.floorMod(current + (packet.reverse() ? -1 : 1), ShadesClient.prismCycleSize());
    ItemStack updated = accessoryStack.copy();
    updated.set(ModComponents.PRISM_CYCLE_INDEX, next);
    Services.PLATFORM.setAccessoryShadesItem(player, updated);
  }
}
