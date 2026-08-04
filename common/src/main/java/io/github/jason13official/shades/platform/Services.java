package io.github.jason13official.shades.platform;

import io.github.jason13official.shades.Constants;
import io.github.jason13official.shades.platform.services.IPlatformHelper;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class Services {

  public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

  private static <T> T load(Class<T> clazz) {

    Constants.LOG.info("Loading service {}", clazz);

    // a merged multi-loader jar carries every platform's provider entry, so ServiceLoader can
    // hand back one whose backing classes aren't on this platform's classpath; touch a real
    // platform call per candidate so the wrong one throws here and we skip to the next entry
    for (T helper : ServiceLoader.load(clazz)) {
      try {
        boolean dev = helper instanceof IPlatformHelper platform && platform.isDevelopmentEnvironment();
        if (dev) {
          Constants.LOG.info("Loaded {} for service {}", helper, clazz);
        }
        return helper;
      } catch (NoClassDefFoundError | ServiceConfigurationError e) {
        Constants.LOG.debug("Skipping incompatible platform helper {}", helper.getClass().getName());
      }
    }

    throw new IllegalStateException("Failed to load service for " + clazz.getName());
  }
}