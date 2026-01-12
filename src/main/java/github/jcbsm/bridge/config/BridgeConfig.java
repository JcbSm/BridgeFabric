package github.jcbsm.bridge.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import github.jcbsm.bridge.Bridge;
import github.jcbsm.bridge.config.files.ChatRelayConfig;
import github.jcbsm.bridge.config.files.WhitelistConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeConfig {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfig.class.getName());

    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Bridge.MOD_ID);

    public static final ConfigFile<WhitelistConfig> WHITELIST =
            new ConfigFile<>(
                    CONFIG_DIR.resolve("cfg_whitelist.json5"),
                    WhitelistConfig.class
            );

    public static final ConfigFile<ChatRelayConfig> CHAT_RELAY =
            new ConfigFile<>(
                    CONFIG_DIR.resolve("cfg_chat_relay.json5"),
                    ChatRelayConfig.class
            );

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

    /** Call this once during mod initialization */
    public static void init() {
        // Forces class loading & file creation
        WHITELIST.get();
        CHAT_RELAY.get();
    }
}
