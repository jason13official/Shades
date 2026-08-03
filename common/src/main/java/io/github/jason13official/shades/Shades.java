package io.github.jason13official.shades;

import net.minecraft.resources.Identifier;

public class Shades {

  public static void init() {
  }

  public static Identifier identifier(final String path) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
  }
}