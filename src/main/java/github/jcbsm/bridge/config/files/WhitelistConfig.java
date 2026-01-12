package github.jcbsm.bridge.config.files;

import github.jcbsm.bridge.config.ConfigFile;

public class WhitelistConfig extends ConfigFile {

    public WhitelistConfig() {
        super("whitelist");
    }

    public static String config_version = "1.0.0";
    public static boolean enabled = false;

    public static class Accounts {
        public static int max_accounts_per_user = 1;
    }
}
