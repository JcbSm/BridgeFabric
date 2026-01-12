package github.jcbsm.bridge.config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.jcbsm.bridge.Bridge;
import github.jcbsm.bridge.config.files.ChatRelayConfig;
import github.jcbsm.bridge.config.files.WhitelistConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeConfig {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfig.class.getName());

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Bridge.MOD_ID);

    // Create configs
    private static final ConfigFile[] configFiles = {
            new WhitelistConfig(),
            new ChatRelayConfig()
    };

    private static final Map<String, ConfigFile> config = new HashMap<>();

    private BridgeConfig() {}

    public static Path getConfigDir() {

        logger.debug("Getting config directory...");

        // Ensure it exists
        if (Files.notExists(CONFIG_DIR)) {

            logger.warn("Config directory not found. Creating one...");

            try {
                Files.createDirectory(CONFIG_DIR);
                logger.info("New mod config directory created.");

            } catch (IOException e) {

                logger.error("Unable to create config directory.");
                throw new RuntimeException(e);
            }
        }

        // Return it
        return CONFIG_DIR;
    }

    public static synchronized void load() {

        for (ConfigFile configFile : configFiles) {

            // Check if exists
            if (Files.exists(configFile.path)) {

                try {

                    // Load the config
                    configFile.load();

                } catch (IOException e) {
                    logger.error("Error loading config file: " + configFile.name + ".json, please check the file is valid. If deleted a new file will be generated with default values.");
                    continue;
                }

            } else {

                logger.debug("File not found: " + configFile.path + ". Creating default...");

                // Create default config if missing.
                createDefaultConfig(configFile);
            }

            // Add config file to the config
            config.put(configFile.name, configFile);
        }
    }

    public static synchronized void save() {

        for (ConfigFile cf : configFiles) {

            try {
                cf.save();
            } catch (IOException e) {
                logger.warn("Unable to save config file: " + cf.path);
            }
        }
    }

    private static Map<String, Object> buildDefaultConfigTree(Class<?> cls) {

        try {

            Map<String, Object> configTree = new LinkedHashMap<>();

            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {

                // Check if the field is static
                if (Modifier.isStatic(field.getModifiers())) {

                    // Add field to config tree
                    configTree.put(field.getName(), field.get(cls));

                }
            }

            // Get subtrees
            Class<?>[] innerClasses = cls.getDeclaredClasses();

            for (Class<?> innerCls : innerClasses) {

                // Check if the inner class is static
                if (Modifier.isStatic(innerCls.getModifiers())) {

                    // Build subtree
                    Map<String, Object> tree = buildDefaultConfigTree(innerCls);

                    // Add subtree to config tree
                    configTree.put(innerCls.getSimpleName().toLowerCase(), tree);
                }

            }

            return configTree;

        } catch (Exception e) {
            logger.error("Error building config tree for " + cls.getSimpleName());
            return null;
        }
    }

    private static void createDefaultConfig(ConfigFile configFile) {

        // Get file writer
        try (Writer writer = new FileWriter(configFile.path.toString())) {

            // Create the JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Generate the default config (uses static fields)
            Map<String, Object> defaultConfigTree = buildDefaultConfigTree(WhitelistConfig.class);

            // Write default config to the file
            gson.toJson(defaultConfigTree, writer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Validation
        if (Files.exists(configFile.path)) {
            configFile.validate();
        }
    }

    public static Object get(String path) {

        logger.debug("Getting config value: " + path);

        String[] pathToField = path.split("\\.", 2);
        String fileName = pathToField[0];
        String pathToVal = pathToField[1];
        ConfigFile cf = config.get(fileName);
        return cf.get(pathToVal);

    }

    public static void set(String path, Object val) {

        logger.debug("Setting config value: " + path + " = " + val.toString());

        String[] pathToField = path.split("\\.", 2);
        String fileName = pathToField[0];
        String pathToVal = pathToField[1];
        ConfigFile cf = config.get(fileName);

        // Update field
        cf.set(pathToVal, val);
    }
}
