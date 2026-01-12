package github.jcbsm.bridge.config.files;

import github.jcbsm.bridge.config.ConfigFile;

public class ChatRelayConfig extends ConfigFile {

    public ChatRelayConfig() {
        super("chat_relay");
    }

    public static String config_version = "1.0.0";
    public static boolean enabled = false;

    public static class Channels {
        public static String[] chat = { "000000000000000000", "000000000000000000", "000000000000000000" };
    }

    public static class SERVER_CROSSCHAT {
        public static boolean enabled = true;
        public static boolean show_server_profile = true;
    }

    public static class Webhooks {
        public static boolean enabled = false;
        public static class Formats {
            public static String username = "%username%";
            public static String content = "%message%";
            public static String avatar_url = "https://mc-heads.net/avatar/%username%";
        }
    }

    public static class MESSAGE_FORMAT {

        public static class DISCORD_TO_MINECRAFT {
            public static String chat =
                    "&9&l[Discord | %guild%]&r %username% > &7%message%";
        }

        public static class MINECRAFT_TO_DISCORD {

            public static class PLAYER_EVENTS {

                public static class Chat {
                    public static String content = "**%username% >** %message%";
                    public static boolean embed_enabled = false;
                }

                public static class Death {
                    public static String content = "## :skull: %message%";
                    public static boolean embed_enabled = false;
                }

                public static class Join {
                    public static String content = "";
                    public static boolean embed_enabled = true;
                    public static String color = "#00FF00";
                    public static String author_name = "%username% joined the server";
                    public static String author_icon_url = "https://mc-heads.net/avatar/%username%";
                }

                public static class Leave {
                    public static String content = "";
                    public static boolean embed_enabled = true;
                    public static String color = "#FF0000";
                    public static String author_name = "%username% left the server";
                    public static String author_icon_url = "https://mc-heads.net/avatar/%username%";
                }

                public static class Advancement {
                    public static String content =
                            "### :trophy: %username% has made the advancement ***%title%*** \n- %description%";
                    public static boolean embed_enabled = false;
                    public static String color = "#FF00FF";
                    public static String author_name = "%username% made an advancement";
                    public static String author_icon_url = "https://mc-heads.net/avatar/%username%";
                }
            }

            public static class SERVER_EVENTS {

                public static class Startup {
                    public static String content = "# :white_check_mark: Server has started.";
                    public static boolean embed_enabled = false;
                }

                public static class Stop {
                    public static String content = "# :octagonal_sign: Server has stopped.";
                    public static boolean embed_enabled = false;
                }
            }
        }
    }
}
