package github.jcbsm.bridge.config.files;

import github.jcbsm.bridge.config.ConfigFile;

public class WhitelistConfig extends ConfigFile {

    public WhitelistConfig() {
        super("whitelist");
    }

    public static String config_version = "1.0.0";

    public static class Accounts {
        public static int max = 1;
        public static String max_accounts_message = "You've hit the limit!";
    }

    public static class Discord {
        public static boolean email_required = false;
    }


}
