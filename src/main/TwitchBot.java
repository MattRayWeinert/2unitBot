package main;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

/*
 * Handles all the features for the bot for every channel
 * that was instantiated. 
 */
public class TwitchBot extends PircBot {
	
	ArrayList<String> userList = new ArrayList<String>();
	Boolean listState = false;
	
	
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		
		/*
		 * Broadcaster can open up the list, open list let's users join the list.
		 * 
		 * @param message that is !open in the chat
		 * @param sender whose name is equivalent to the broadcaster's name
		 */
		if (message.equalsIgnoreCase("!open") && sender.contains(channel.replace("#", ""))) {
			
			listState = true;
			
			sendMessage(channel, "The list is now open.");
		}
		
		/*
		 * Broadcaster can open up the list, open list let's users join the list.
		 * 
		 * @param message that is !close in the chat
		 * @param sender whose name is equivalent to the broadcaster's name
		 */
		if (message.equalsIgnoreCase("!close") && sender.contains(channel.replace("#", ""))) {
			
			listState = false;
			
			sendMessage(channel, "The list is now closed.");
		}

		/*
		 * Adds the user's name to the list, will not add repeating names
		 * to the list. Will also confirm that the user was added to the list.
		 * 
		 * @param message that is !add in the chat
		 * @param listState controls if the list is open or closed, must be
		 *  open for this method
		 */
		if (message.equalsIgnoreCase("!add") && listState == true) {
			
			if (!userList.contains(sender)) {
				
				sendMessage(channel, "Added " + sender + " to the list.");
				
				userList.add(sender);
			} else {
				
				sendMessage(channel, sender + " is already in the list.");
			}

		}
		
		/*
		 * Simply tells the user that is trying to join that the list is closed.
		 * 
		 * @param message that is !add in the chat
		 * @param listState controls if the list is open or closed, must be
		 *  closed for this method
		 */
		if (message.equalsIgnoreCase("!add") && listState == false) {
			
			sendMessage(channel, "Sorry " + sender + ", but the list is currently closed.");
		}
		
		/* 
		 * Prints the list to the twitch chat.
		 * 
		 * @param message that is !list in the chat
		 * */
		if (message.equalsIgnoreCase("!list")) {
						
			if (userList.size() == 0) {
				
				sendMessage(channel, "There are no users in the list.");
			} else {
				
				sendMessage(channel, userList.toString().replace("[", "").replace("]", ""));
			}
		}
		
		/*
		 * Prints the next name in the queue
		 * 
		 * @param message that is !next in the chat
		 */
		if (message.equalsIgnoreCase("!next")) {
			
			sendMessage(channel, userList.get(0) + " is next.");
		}
		
		/*
		 * Prints the next name in the queue and 
		 * will remove it from the list.
		 * 
		 * @param message that is !del in the chat
		 * @param sender which is equivalent to the broadcaster's name
		 */
		if (message.equalsIgnoreCase("!del") && sender.contains(channel.replace("#", ""))) {
			
			sendMessage(channel, userList.get(0) + " has been removed from the list.");
			
			userList.remove(0);
		}
		
		/*
		 * Broadcaster can force add a user onto the end
		 * of a list.
		 * 
		 * @param message that is !forceadd in the chat
		 * @param sender which needs to be equivalent to the broadcaster's name
		 */
		if (message.contains("!forceadd ") && sender.contains(channel.replace("#", ""))) {
			
			String userAdded = message.replace("!forceadd ", "");
			
			userList.add(userAdded);
			
			sendMessage(channel, "Added " + userAdded + " to the list.");
		}
		
		/*
		 * Broadcaster can force delete a user on the list.
		 * 
		 * @param message that is !forcedel in the chat
		 * @param sender which needs to be equivalent to the broadcaster's name
		 */
		if (message.contains("!forcedel ") && sender.contains(channel.replace("#", ""))) {
			
			String userDelete = message.replace("!forcedel ", "");
			
			userList.remove(userDelete);
			
			sendMessage(channel, "Removed " + userDelete + " from the list.");
		}
		
		
		/*
		 * Broadcaster can clear the list.
		 * 
		 * @param message that is !clear in the chat
		 * @param sender which is equivalent to the broadcasters name
		 */
		if (message.equalsIgnoreCase("!clear") && sender.contains(channel.replace("#", ""))) {
			
			userList.clear();
			sendMessage(channel, "The list has been cleared.");
			
		}
		
		/*
		 * List of features the bot can do.
		 * 
		 * @param message that is !features within the chat
		 */
		if (message.equalsIgnoreCase("!features")) {
			
			sendMessage(channel, "!open !close !add, !list, !next, !del, !forceadd (user), !forcedel (user), !clear, !features, !tournaments (coming soon!)");
		}
		
		/*
		 * Lists a few online tournaments found through smash.gg.
		 * 
		 * @param message that is !tournaments within the chat
		 */
		if (message.equalsIgnoreCase("!tournaments")) {

			try {
				
				FileWriter fw = new FileWriter("tournaments", true);
				BufferedWriter bw = new BufferedWriter(fw);
								
				for (int i = 1; i < 4; i++) {
					
					bw.write(getTournaments("https://smash.gg/tournaments?per_page=30&filter=%7B%22upcoming%22%3Atrue%2C%22videogameIds%22%3A1386%2C%22online%22%3Atrue%2C%22addrState%22%3A%22%22%7D&page=" + i));
				}
				
				
				bw.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Obtains a list of tournaments on the given URL which corresponds to the first
	 * page of smash.gg tournaments for Smash U tournaments. Will return results which is specifically
	 * open online tournaments with people registered. If there are no attendees or registered individuals, that
	 * tournament will not get added to the list.
	 * 
	 * @param url that is the url of the page that will be parsed for data scraping
	 */
	public String getTournaments(String url) throws Exception {
		
		ArrayList<String> titles = new ArrayList<String>();
		
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\Unit\\Downloads\\chromedriver_win32\\chromedriver.exe");
		
		WebDriver driver = new ChromeDriver();
		
		driver.get(url);
		
		Thread.sleep(6000);
		
		String pageSource = driver.getPageSource();
		
		Document document = Jsoup.parse(pageSource);
		
		Elements items = document.select(".TournamentCardContainer:has(div.InfoList a)");
		
		for (Element item: items) {

			if(item.text().contains("Registration Open") && item.text().contains("Online")) {
				
				titles.add(item.text() + " -->" + item.attr("href") + "\n");
			}
		}
		
		driver.quit();
		
		return (finalList(titles).toString().replace("[", "").replace("]", "").replace(", ", ""));
	}
	
	/*
	 * Capable of creating the list of data and organize it then sending back to 
	 * be written in the BufferedWriter to the tournaments.txt file
	 * 
	 * @param tournaments that is an arraylist of all the tournaments to be spliced
	 * 	and sent back
	 */
	public ArrayList<String> finalList(ArrayList<String> tournaments) {
		
		ArrayList<String> finalList = new ArrayList<String>();
		String[] parts;
		String[] attendees;
		int z = 0;
		
		for (int i = 0; i < tournaments.size(); i++) {
						
			parts = tournaments.get(i).toString().replace("Online", "Online:=:").split(":=:");

			attendees = parts[1].toString().split(" ");
	
			if (!attendees[0].replace(",", "").contentEquals("")) {

				z = Integer.parseInt(attendees[0].replace(",", ""));
			}
			
			if (parts[0].indexOf("Open") != -1) {
				
				if (z > 10)
				finalList.add(z + " Entrants: " + parts[0] + "\n");
			}
		}
		
		Collections.sort(finalList);

		return finalList;
	}
}
