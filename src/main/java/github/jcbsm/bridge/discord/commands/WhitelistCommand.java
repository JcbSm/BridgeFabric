package github.jcbsm.bridge.discord.commands;


import github.jcbsm.bridge.Bridge;
import github.jcbsm.bridge.config.BridgeConfig;
import github.jcbsm.bridge.db.DatabaseClient;
import github.jcbsm.bridge.discord.ApplicationCommand;
import github.jcbsm.bridge.util.MojangRequest;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// TODO: sanamorii - add option to remove username (subcommands)
public class WhitelistCommand extends ApplicationCommand {

    private final DatabaseClient database = DatabaseClient.getDatabase();

    public WhitelistCommand() {
        super("whitelist");
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Manage whitelisted accounts")
                .addSubcommands(
                        // Link command
                        new SubcommandData("add", "Link a new Minecraft account")
                                .addOption(OptionType.STRING, "username", "The username of the Minecraft account to link", true),

                        // Unlink command
                        new SubcommandData("remove", "Remove an existing Minecraft account")
                                .addOption(OptionType.STRING, "username", "The username of the Minecraft account to unlink", true, true),
                        new SubcommandData("list", "View currently linked Minecraft accounts")
                );
    }

    /**
     * Handles autocompletion
     */
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

        String[] usernames;

        try {
            usernames = database.getLinkedAccounts(event.getUser()).values().toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            event.replyChoices().queue();
            return;
        }

        if (event.getSubcommandName().equals("remove") && event.getFocusedOption().getName().equals("username")) {
            List<Command.Choice> options = Stream.of(usernames)
                    .filter(username -> username.startsWith(event.getFocusedOption().getValue()))
                    .map(username -> new Command.Choice(username, username))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

    public void run(SlashCommandInteractionEvent event) {

        event.deferReply().setEphemeral(true).queue();

        switch (event.getSubcommandName()) {
            case "add":
                runLink(event);
                break;

            case "remove":
                runUnlink(event);
                break;

            case "list":
                runList(event);
                break;
        }
    }

    private void runLink(SlashCommandInteractionEvent event) {
        String username = event.getOption("username").getAsString();

        try {
            String uuid = MojangRequest.usernameToUUID(username);

            if (uuid == null) {
                event.getHook().sendMessage("No minecraft account exists with this username.").queue();
                return;
            }

            // Ensure not exceeding max accounts.
            if (database.countLinkedAccounts(event.getUser()) >= BridgeConfig.WHITELIST.get().accounts.max_accounts_per_user) {
                event.getHook().sendMessage("You already have the maximum number of allowed linked accounts. Please remove one before attempting to link any others.").queue();
                return;
            }

            Long linked;
            // Check not already linked

            // If not currently linked
            if ((linked = database.getLinked(uuid)) == null) {

                // Link on database side
                database.linkAccount(event.getUser(), uuid);

                // OLD BUKKIT IMPLEMENTATION
                // WILL BE REMOVED LATER LMAO
//                // Run mc command
//                if (ConfigHandler.getHandler().getBoolean("AccountLinking.ModifiesWhitelist")) {
//                    Bukkit.getScheduler().runTask(Bridge.getPlugin(), () -> Bukkit.getServer().dispatchCommand(
//                            Bukkit.getServer().getConsoleSender(),
//                            "whitelist add " + username
//                    ));
//                }

                ServerCommandSource source = Bridge.SERVER.getCommandSource();
                String command = "whitelist add " + username;

                Bridge.SERVER.execute(() -> {
                    Bridge.SERVER.getCommandManager().parseAndExecute(source, command);
                });

                event.getHook().sendMessage("Account " + username + " successfully linked.").queue();

            // If it is linked, check if linked to current user
            } else if (linked.equals(event.getUser().getIdLong())) {
                event.getHook().sendMessage("You are already linked to the Minecraft account \"" + username + "\".").queue();

            // Assume linked to someone else.
            } else {
                event.getHook().sendMessage("Someone else is already linked to the Minecraft account \"" + username + "\".").queue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("An unexpected error occurred").queue();
        }
    }

    private void runUnlink(SlashCommandInteractionEvent event) {
        String username = event.getOption("username").getAsString();

        try {
            // Get UUID
            String uuid = MojangRequest.usernameToUUID(username);

            // If no UUID found.
            if (uuid == null) {
                event.getHook().sendMessage("No Minecraft account exists with this username.").queue();
                return;
            }

            // Ensure account is linked to that user
            Map<String, String> accounts = database.getLinkedAccounts(event.getUser());

            if (accounts.keySet().contains(uuid)) {
                database.unlinkAccount(uuid);

//                // Run mc command
//                if (ConfigHandler.getHandler().getBoolean("AccountLinking.ModifiesWhitelist")) {
//                    Bukkit.getScheduler().runTask(Bridge.getPlugin(), () -> Bukkit.getServer().dispatchCommand(
//                            Bukkit.getServer().getConsoleSender(),
//                            "whitelist remove " + username
//                    ));
//
//                    Bukkit.getScheduler().runTask(Bridge.getPlugin(), () -> {
//                        try {
//                            Bukkit.getServer().dispatchCommand(
//                                    Bukkit.getServer().getConsoleSender(),
//                                    "kick " + username + " This account has been unlinked."
//                            );
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }

                ServerCommandSource source = Bridge.SERVER.getCommandSource();
                String command_whitelist = "whitelist remove " + username;
                String command_kick = "kick " + username + " This account has been unlinked.";

                Bridge.SERVER.execute(() -> {
                    Bridge.SERVER.getCommandManager().parseAndExecute(source, command_whitelist);
                    Bridge.SERVER.getCommandManager().parseAndExecute(source, command_kick);
                });

                event.getHook().sendMessage("Successfully unlinked \"" + username + "\"").queue();
            } else {
                event.getHook().sendMessage("You are not currently linked to the Minecraft account \"" + username + "\"").queue();
            }


        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("An unexpected error occurred...").queue();
        }
    }

    private void runList(SlashCommandInteractionEvent event) {

        try {
            // Get all linked accounts
            Map<String, String> accounts = database.getLinkedAccounts(event.getUser());

            // If no accounts
            if (accounts.isEmpty()) {
                event.getHook().sendMessage("You don't currently have any account(s) whitelisted.").queue();

                // Otherwise, form response
            } else {
                StringBuilder content = new StringBuilder("### Whitelisted accounts:\n");

                for (String username : accounts.values()) {
                    content.append("- " + username + "\n");
                }

                event.getHook().sendMessage(content.toString()).queue();
            }

            // Unexpected error...
        } catch (Exception e){
            e.printStackTrace();
            event.getHook().sendMessage("An unexpected error has occurred.").queue();
        }
    }
}
