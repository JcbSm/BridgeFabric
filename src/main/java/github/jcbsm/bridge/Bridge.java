package github.jcbsm.bridge;

import github.jcbsm.bridge.config.BridgeConfig;
import github.jcbsm.bridge.db.DatabaseClient;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class Bridge implements ModInitializer {

    public static final String MOD_ID = "bridge";
    public static final Logger logger = LoggerFactory.getLogger(Bridge.class.getSimpleName());

    @Override
    public void onInitialize() {



        DatabaseClient db = DatabaseClient.getDatabase();
        BridgeConfig.load();

        BridgeConfig.set("whitelist.accounts.max", 2);

        BridgeConfig.save();

    }
}
