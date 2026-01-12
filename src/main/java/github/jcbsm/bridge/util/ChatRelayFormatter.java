package github.jcbsm.bridge.util;

import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

import github.jcbsm.bridge.Bridge;
import github.jcbsm.bridge.db.DatabaseClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;


import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRelayFormatter {

    /**
     * Regex Patterns
     */
    private final static String
            escapeRegex = "(?<!\\\\)",
            username = escapeRegex + "%username%",
            name = escapeRegex + "%name%",
            message = escapeRegex + "%message%",
            channel = escapeRegex + "%channel%",
            guild = escapeRegex + "%guild%",
            world = escapeRegex +"%world%",
            advancementTitle = escapeRegex + "%title%",
            advancementDesc = escapeRegex + "%description%";

    /**
     * Replaces all instances matching a regex pattern with a replacement string
     * @param input The input string
     * @param regex RegEx to match
     * @param replacement String to replace each match with
     * @return Modified string
     */
    private static String replaceAll(String input, String regex, String replacement) {
        Pattern p = Pattern.compile(regex);
        return p.matcher(input).replaceAll(replacement);
    }

    /**
     * Replaces all matches with replacement keys with respective values
     * @param input Input string
     * @param replacement Map of \<Pattern, replacement\>
     * @return Modified string
     */
    private static String replaceAll(String input, Map<String, String> replacement) {
        for (Map.Entry<String, String> entry : replacement.entrySet()) {
            input = replaceAll(input, entry.getKey(), entry.getValue());
        }

        return input;
    }

//    /**
//     * Produces a webhook message from a Minecraft chat event.
//     * @param event Event ot process
//     * @return The message to be broadcast
//     */
//    public static WebhookMessage playerChatWebhook( event) {
//        String content = playerChatPlaceholders(ConfigHandler.getHandler().getString("ChatRelay.WebhookMessages.ContentFormat"), event);
//
//        return new WebhookMessageBuilder()
//                .setContent(parseMentions(content))
//                .setUsername(playerChatPlaceholders(ConfigHandler.getHandler().getString("ChatRelay.WebhookMessages.UsernameFormat"), event))
//                .setAvatarUrl(playerChatPlaceholders(ConfigHandler.getHandler().getString("ChatRelay.WebhookMessages.AvatarURL"), event))
//                .build();
//    }

    /**
     * Searches a string
     * @param message String to be searched
     * @return String with all mentions formatted correctly for Discord.
     */
    public static String parseMentions(String message){

        ArrayList<String> usernames = new ArrayList<>();

        // Compile regex pattern for an @ followed by 2-32 word characters
        Pattern regex = Pattern.compile("@(\\w{2,32})");
        Matcher matcher = regex.matcher(message);

        // Finds all the sequences of characters that follow the @ symbol
        // 1 takes the second group - ignores @ symbol
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }

        // Iterate through usernames
        for (String username : usernames) {
            try {

                // Do nothing if player is online
                if (Arrays.stream(Bridge.SERVER.getPlayerNames()).toList().contains(username)) continue;

                // Get UUID
                String uuid = MojangRequest.usernameToUUID(username);

                // Search for player in Database
                Long discordUser = DatabaseClient.getDatabase().getLinked(uuid);

                // If the user is not found in the database, do not mention
                if (discordUser == null) continue;

                // Replace usernames with mentions
                message = replaceAll(message, "@" + username, "<@" + discordUser + ">");


            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return message;
    }
}
