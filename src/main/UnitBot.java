package main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.jibble.pircbot.PircBot;

/*
 * Monitors twitch.tv/2UNITbot IRC chat and will be on standby
 * for anyone that commands the bot to join their channel.
 */
public class UnitBot extends PircBot {

	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		
		/*
		 * Adds user to the channel text file, repeated users
		 * will not get added to the list. After they get added to
		 * the list, a new chatbot object will be instantiated for
		 * toe new user's chat allowing them to use it.
		 * 
		 * @param message is !join in the chat
		 */
		if (message.equalsIgnoreCase("!join")) {
			/*
			 * Opens filewriter for the channels text file and uses
			 * the bufferedwriter to append and write to the file.
			 */
			try (FileWriter writer = new FileWriter("channels", true);
				 BufferedWriter bw = new BufferedWriter(writer); Scanner scanner = new Scanner(new File("channels"))) {
				
				/*
				 * Scans through the channel list text file.
				 */
				while (scanner.hasNext()) {
					
					String line = scanner.nextLine();
					
					/*
					 * Checks if the username is already in the channel list text file,
					 * if it already exists the user will not be added to the channel list.
					 */
					if (line.equalsIgnoreCase(sender)) {

						sendMessage(channel, sender + ", you already have 2UNITbot in your stream.");
						
						break;
					}
					
					/*
					 * Successfully scanned through the EOF meaning the user is not
					 * on the channel list and will be added to it while also getting the 
					 * chatbot object to their twitch chat.
					 */
					if (!scanner.hasNextLine()) {
						
						bw.write(sender.toLowerCase() + "\n");

						try {
							TwitchBot bot = new TwitchBot();

							bot.setVerbose(true);
							bot.connect("irc.twitch.tv", 6667, "oauth:11cn1oshp2e9x3530cwx3atyc4d3gr");
							bot.joinChannel("#" + sender);
							bot.getName();	
							
						} catch (Exception e) {
							
							log("TwitchBot cannot join the channel. " + e);
						}
												
						sendMessage(channel, "Joining " + sender + "'s channel.");
					}
				}

				scanner.close();
				bw.close();
				writer.close();

			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		
		/*
		 * List of features the bot can do.
		 * 
		 * @param message that is !features within the chat
		 */
		if (message.equalsIgnoreCase("!features")) {
			sendMessage(channel, "!add, !list, !next, !nextdel, !forceadd (user), !clear, !features");
		}
	}
}
