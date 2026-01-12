package github.jcbsm.bridge.discord;


import github.jcbsm.bridge.config.BridgeConfig;
import github.jcbsm.bridge.discord.commands.PingCommand;
import github.jcbsm.bridge.discord.commands.PlayerListCommand;
import github.jcbsm.bridge.discord.commands.WhitelistCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles interactions and registers application commands
 */
public class ApplicationCommandHandler {

    private final DiscordClient client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public ApplicationCommandHandler(DiscordClient client) {

        this.client = client;

        ArrayList<ApplicationCommand> activeCommands = new ArrayList<>();

        if (BridgeConfig.WHITELIST.get().enabled) {
            activeCommands.add(new WhitelistCommand());
        }

        activeCommands.add(new PingCommand());
        activeCommands.add(new PlayerListCommand());

        overwriteApplicationCommands(activeCommands);

    }

    /**
     * Register a new command + listen for interactions.
     * @param command The command to register
     */
    public void registerGuildApplicationCommand(Guild guild, ApplicationCommand command) {

        logger.info("Registering {} command to guild {}", command.getName(), guild.getName());
        // Register command
        guild.upsertCommand(command.getCommandData()).queue();
    }

    /**
     * Overwrites the client's current application commands
     * Removes all global commands
     * Deletes any old guild-commands
     * Updates the existing ones.
     * @param commands
     */
    public void overwriteApplicationCommands(ArrayList<ApplicationCommand> commands) {
        // Retrieve global commands, delete all to ensure no wrong commands
        client.getJda().retrieveCommands().queue((commandList) -> {

            // Delete each one
            for (Command command: commandList) {
                logger.info("Deleting global command: '{}'", command.getFullCommandName());
                command.delete().queue();
            }
        });

        // For each guild
        for (Guild guild : client.getJda().getGuilds()) {

            logger.info("Overwriting application commands for guild: {}", guild.getName());

            guild.retrieveCommands().queue((commandList -> {

                // Get each command
                for (Command command: commandList) {

                    // Ignore it's in the current ones
                    if (commands.stream().anyMatch(cmd -> cmd.getName().equals(command.getName())))
                        continue;

                    // Otherwise, delet it
                    logger.info("Deleting {} command in guild {}", command.getFullCommandName(), guild.getName());
                    command.delete().queue();
                }

                logger.info("Application commands reset... Registering {} new ones.", commands.size());

                // Register the command.
                for (ApplicationCommand command : commands) {
                    logger.info("Registering command: " + command.getName());
                    registerGuildApplicationCommand(guild, command);
                }
            }));
        }

        // Add listeners
        for (ApplicationCommand command : commands) {
            client.getJda().addEventListener(command);
        }
    }
}
