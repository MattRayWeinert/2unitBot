package main;
import java.io.File;
import java.util.Scanner;

/*
 * Main bot class, loads the original bot into Twitch.tv/2UNITbot's IRC chat
 * and then monitors it for anyone wanting the bot to be added to their IRC channel.
 * Also handles the bot's original startup and will rejoin channels that were already
 * added to.
 */
public class Bot {

	public static void main(String[] args) throws Exception {
		
		/*
		 * Creates a new unitBot and patrols 2UNITbot's channel
		 * for new users.
		 */
		UnitBot unitBot = new UnitBot();
		unitBot.setVerbose(true);
		unitBot.connect("irc.twitch.tv", 6667, "oauth:11cn1oshp2e9x3530cwx3atyc4d3gr");
		unitBot.joinChannel("#2unitbot");
		unitBot.getName();

		/*
		 * Opens the channels file, setting up to join all the channels added
		 * to the list. The list is created by individuals manually adding themselves to 
		 * the list via twitch.tv/2unitbot's IRC chat.
		 */
		File file = new File("channels");
		Scanner scanner = new Scanner(file);
		
		/*
		 * Scans through the channel list text file, then will join the channels
		 * that are listed within the channel list.
		 */
		while (scanner.hasNext()) {
			
			String line = scanner.nextLine();
			
			TwitchBot bot = new TwitchBot();

			bot.setVerbose(true);
			bot.connect("irc.twitch.tv", 6667, "oauth:11cn1oshp2e9x3530cwx3atyc4d3gr");
			bot.joinChannel("#" + line);
			bot.getName();
		}
		
		scanner.close();
	}
}