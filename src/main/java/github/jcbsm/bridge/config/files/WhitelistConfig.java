package github.jcbsm.bridge.config.files;

import blue.endless.jankson.Comment;
import github.jcbsm.bridge.config.ConfigFile;

public class WhitelistConfig {

    @Comment("Config file version. Do not edit.")
    public String config_version = "1.0.0";

    @Comment("Enable or disable the whitelist system")
    public boolean enabled = false;

    @Comment("Account-related settings")
    public Accounts accounts = new Accounts();

    public static class Accounts {

        @Comment("Maximum number of linked accounts per user")
        public int max_accounts_per_user = 1;
    }
}
