package io.github.jason13official.shades;

import net.fabricmc.api.ClientModInitializer;

public class ShadesClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    ShadesClient.init();
  }
}
