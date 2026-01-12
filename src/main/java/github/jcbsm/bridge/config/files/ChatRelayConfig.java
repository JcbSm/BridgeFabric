package github.jcbsm.bridge.config.files;

import blue.endless.jankson.Comment;

public class ChatRelayConfig {

    @Comment("Config file version")
    public String config_version = "1.0.0";

    @Comment("Enable chat relay")
    public boolean enabled = false;

    @Comment("Bot token. DO NOT SHOW THIS TO ANYONE!")
    public String bot_token = "TOKEN_HERE";

    @Comment("Discord channel configuration")
    public Channels channels = new Channels();

    @Comment("Webhook settings")
    public Webhooks webhooks = new Webhooks();

    @Comment("Message formatting rules")
    public MessageFormat message_format = new MessageFormat();

    public static class Channels {
        @Comment("Discord channel IDs used for chat relay")
        public String[] chat = {
                "000000000000000000",
                "000000000000000000",
                "000000000000000000"
        };
    }

    public static class Webhooks {
        @Comment("Enable webhook-based message sending")
        public boolean enabled = false;

        public Formats formats = new Formats();

        public static class Formats {
            public String username = "%username%";
            public String content = "%message%";
            public String avatar_url = "https://mc-heads.net/avatar/%username%";
        }
    }

    public static class MessageFormat {

        @Comment("Handles chat relay from Discord to Minecraft")
        public DiscordToMinecraft discord_to_minecraft = new DiscordToMinecraft();

        @Comment("Handles chat relay from Minecraft to Discord")
        public MinecraftToDiscord minecraft_to_discord = new MinecraftToDiscord();

        public static class DiscordToMinecraft {

            @Comment("How chat messages from Discord show up in Minecraft\nSupports colour formatting.")
            public String chat =
                    "&9&l[Discord | %guild%]&r %username% > &7%message%";
        }

        public static class MinecraftToDiscord {

            public PlayerEvents player_events = new PlayerEvents();
            public ServerEvents server_events = new ServerEvents();

            public static class PlayerEvents {
                public Chat chat = new Chat();
                public Death death = new Death();
                public Join join = new Join();
                public Leave leave = new Leave();

                public static class Chat {
                    public String content = "**%username% >** %message%";
                    public boolean embed_enabled = false;
                }

                public static class Death {
                    public String content = "## :skull: %message%";
                    public boolean embed_enabled = false;
                }

                public static class Join {
                    public String content = "";
                    public boolean embed_enabled = true;
                    public String color = "#00FF00";
                    public String author_name = "%username% joined the server";
                    public String author_icon_url = "https://mc-heads.net/avatar/%username%";
                }

                public static class Leave {
                    public String content = "";
                    public boolean embed_enabled = true;
                    public String color = "#FF0000";
                    public String author_name = "%username% left the server";
                    public String author_icon_url = "https://mc-heads.net/avatar/%username%";
                }
            }

            public static class ServerEvents {
                public Startup startup = new Startup();
                public Stop stop = new Stop();

                public static class Startup {
                    public String content = "# :white_check_mark: Server has started.";
                    public boolean embed_enabled = false;
                }

                public static class Stop {
                    public String content = "# :octagonal_sign: Server has stopped.";
                    public boolean embed_enabled = false;
                }
            }
        }
    }
}
