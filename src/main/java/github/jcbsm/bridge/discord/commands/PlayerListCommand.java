package github.jcbsm.bridge.discord.commands;

import github.jcbsm.bridge.Bridge;
import github.jcbsm.bridge.discord.ApplicationCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;


public class PlayerListCommand extends ApplicationCommand {

    public PlayerListCommand() {
        super("playerlist");
    }

    /**
     * Runs the interaction reply
     * @param event Event
     */
    @Override
    public void run(SlashCommandInteractionEvent event) {

        // If server is empty, reply with appropriate message
        if (Bridge.SERVER.getCurrentPlayerCount() == 0) {
            event.reply("No players are currently online.").setEphemeral(true).queue();

        // Otherwise, display player list.
        } else {

            StringBuilder msg = new StringBuilder();

            // title
            msg.append("## " + Bridge.SERVER.getCurrentPlayerCount() + " Player(s) Online:" + System.lineSeparator());

            // Append each player to the list
            for (String player : Bridge.SERVER.getPlayerNames()) {
                msg.append("- " + (player + System.lineSeparator()));
            }

            event.reply(msg.toString()).setEphemeral(true).queue();

        }

    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "List online players");
    }
}
