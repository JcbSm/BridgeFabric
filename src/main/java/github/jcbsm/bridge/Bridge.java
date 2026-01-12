package github.jcbsm.bridge;

import github.jcbsm.bridge.config.BridgeConfig;
import github.jcbsm.bridge.db.DatabaseClient;
import github.jcbsm.bridge.discord.DiscordClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;

public class Bridge implements ModInitializer {

    public static final String MOD_ID = "bridge";
    public static final Logger logger = LoggerFactory.getLogger(Bridge.class.getSimpleName());

    public static MinecraftServer SERVER;
    public static DiscordClient discordClient;

    @Override
    public void onInitialize() {

        // Access server from anywhere
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            SERVER = null;
        });

        // Initialise config
        BridgeConfig.init();

        try {
            discordClient = new DiscordClient(BridgeConfig.CHAT_RELAY.get().bot_token, Arrays.stream(BridgeConfig.CHAT_RELAY.get().channels.chat).toList());
        } catch (InterruptedException e) {
            logger.error("Unable to start discord bot. Login unavailable. PLEASE CHECK YOUR BOT TOKEN.");
            throw new RuntimeException(e);
        }

        // Initialise database
        DatabaseClient db = DatabaseClient.getDatabase();


    }
}
