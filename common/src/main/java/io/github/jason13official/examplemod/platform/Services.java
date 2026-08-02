package io.github.jason13official.examplemod.platform;

import io.github.jason13official.examplemod.Constants;
import io.github.jason13official.examplemod.platform.services.IPlatformHelper;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/// @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html">ServiceLoader</a> Oracle's Javadoc
public class Services {

  public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

  /// modified from MultiLoader-Template original to enable merged service loader files i.e. our merged `io.github.jason13official.examplemod.platform.services.IPlatformHelper` might contain:
  ///
  /// ```
  /// io.github.jason13official.examplemod.platform.FabricPlatformHelper
  /// io.github.jason13official.examplemod.platform.ForgePlatformHelper
  /// ```
  private static <T> T load(Class<T> clazz) {

    boolean inDevHelperLoaded = PLATFORM != null && PLATFORM.isDevelopmentEnvironment();

    if (inDevHelperLoaded) {
      Constants.LOG.info("Loading service {} on {}", clazz.getName(), PLATFORM.getPlatformName());
    }

    for (T helper : ServiceLoader.load(clazz)) {
      try {

        if (PLATFORM == null && (helper instanceof IPlatformHelper platform && platform.isDevelopmentEnvironment())) {
          Constants.LOG.info("Trying helper {} for service {} on {}", helper, clazz.getName(), platform.getPlatformName());
        }

        return helper;
      } catch (NoClassDefFoundError | ServiceConfigurationError e) {

        String s = e instanceof NoClassDefFoundError ? "helper definition not found; ignore this warning for merged mod files." : "helper service has malformed configuration.";
        Constants.LOG.info("Skipping {}, {}", helper.getClass().getName(), s);
      }
    }

    throw new IllegalStateException("Failed to load service for " + clazz.getName());
  }
}