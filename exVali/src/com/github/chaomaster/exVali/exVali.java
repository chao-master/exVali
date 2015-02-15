package com.github.chaomaster.exVali;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.logging.Handler;

import au.com.bytecode.opencsv.CSVReader;
import java.util.logging.LogRecord;

public class exVali extends JavaPlugin implements TabCompleter, Listener {

	// HashMap<String, String> fromDisplay = null;

	@Override
	public void onEnable() {
		// fromDisplay = new HashMap<String, String>();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	void onPlayerLoginEvent(PlayerLoginEvent event) {
		// Check with the server
		try {/*
			 * URL checkURL = new URL(
			 * "http://mc.ukofequestria.co.uk/auth/index.php?username=" +
			 * event.getPlayer().getName()); BufferedReader checkIn = new
			 * BufferedReader(new InputStreamReader( checkURL.openStream()));
			 * String[] checkResult = checkIn.readLine().split("\000", 4);
			 * getLogger().info(StringUtils.join(checkResult, "-")); //
			 * Unverified account, displays validation code if
			 * (checkResult[0].equals("unverified")) {
			 * event.setResult(Result.KICK_WHITELIST); event.setKickMessage(
			 * "This account has not been linked with a forum account let.\n" +
			 * "To link the account to to the validation system and enter the code "
			 * + checkResult[3]);
			 * 
			 * } else // Verified account let unevaulated if
			 * (checkResult[0].equals("notwhitelisted")) {
			 * event.setResult(Result.KICK_WHITELIST); event.setKickMessage(
			 * "Your account is pending manual whitlist aproval.\n" +
			 * "The server ops will take care of this shortly");
			 * 
			 * } else // Verified let unwhitlisted account if
			 * (checkResult[0].equals("whitelistdeclined")) {
			 * event.setResult(Result.KICK_WHITELIST); event.setKickMessage(
			 * "Your account was declined access to the server.\n" +
			 * checkResult[3] == "" ?
			 * "This is normally due to your forum account having a low number of posts or otherise looking suspicious"
			 * : ("Given reason" + checkResult[3]));
			 * 
			 * } else // Verified let banned account if
			 * (checkResult[0].equals("banned")) {
			 * event.setResult(Result.KICK_BANNED); event.setKickMessage(
			 * "Your account has been banned from the server.\n" +
			 * checkResult[3] == "" ?
			 * "No reason was given for the ban, contact the server ops for more infomation on it"
			 * : ("For: " + checkResult[3])); } else // Verified and whitelisted
			 * account access granted. if (checkResult[0].equals("whitelisted"))
			 * { event.setResult(Result.ALLOWED); Player player =
			 * event.getPlayer(); player.setDisplayName(checkResult[2]);
			 * player.setPlayerListName(checkResult[2].substring(0, 16)); } else
			 * // Unknown issue { event.setResult(Result.KICK_OTHER);
			 * event.setKickMessage
			 * ("Their was an internal error whilst trying to verify your account"
			 * ); }
			 */

			// Google spreadsheet version
			URL checkURL = new URL(
					"https://docs.google.com/spreadsheet/pub?key=0AkFhkUj3a-5vdERKejdyaDhPWXlYMkp5NUktWDU0b1E&single=true&gid=4&output=csv");
			BufferedReader checkIn = new BufferedReader(new InputStreamReader(
					checkURL.openStream()));
			CSVReader reader = new CSVReader(checkIn);
			String[] nextLine = null;
			String username = event.getPlayer().getName();
            String UUID = event.getPlayer().getUniqueId().toString().replace("-","");
			boolean whiteListed = false;
			while (!whiteListed && (nextLine = reader.readNext()) != null) {
				//if (username.equalsIgnoreCase(nextLine[0])) {
                if (UUID.equals(nextLine[0])) {
					whiteListed = true;
				}
			}
			reader.close();
			if (!whiteListed) {
				event.setResult(Result.KICK_WHITELIST);
				event.setKickMessage("This account is not whitelisted on this server visit mcsrv.ukofequestria.co.uk for more info");
			} else {
				String preferedName = nextLine[1];
				getLogger().info(username+"["+UUID+"] >>"+preferedName);
				event.setResult(Result.ALLOWED);
				Player player = event.getPlayer();
				player.setDisplayName(preferedName);
				//player.setCustomName(preferedName);
				//player.setCustomNameVisible(true);
				if (preferedName.length() > 16) {
					player.setPlayerListName(preferedName.substring(0, 16));
				} else {
					player.setPlayerListName(preferedName);
				}
			}
		} catch (MalformedURLException e) {
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage("Their was an internal error whilst trying to verify your account");
			e.printStackTrace();
		} catch (IOException e) {
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage("Their was an internal error whilst trying to verify your account");
			e.printStackTrace();
		}
	}

	/*
	 * @EventHandler void onPlayerJoinEvnet(PlayerJoinEvent event) {
	 * fromDisplay.put(event.getPlayer().getDisplayName(), event.getPlayer()
	 * .getName()); }
	 * 
	 * @EventHandler void onPlayerQuitEvent(PlayerQuitEvent event) {
	 * fromDisplay.remove(event.getPlayer().getDisplayName()); }
	 * 
	 * @EventHandler void onPlayerKickEvent(PlayerKickEvent event) {
	 * fromDisplay.remove(event.getPlayer().getDisplayName()); }
	 */

	@EventHandler
	void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		String oldMsg = event.getMessage();
		StringBuilder newMsg = new StringBuilder(oldMsg);
		Matcher names = Pattern.compile("(?<=[^a-zA-Z_])~([^ ]*)").matcher(
				oldMsg);
		int mod = 0;
		while (names.find()) {
			String display = names.group(1);
			String user = null;
			for (Player p : this.getServer().getOnlinePlayers()) {
				if (p.getDisplayName().equals(display)) {
					user = p.getName();
					break;
				}
			}
			if (user != null) {
				newMsg.replace(names.start() + mod, names.end() + mod, user);
				mod += display.length() - user.length();
			}
		}
		event.setMessage(newMsg.toString());
	}

	@EventHandler
	public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
		if (event.getLastToken().startsWith("~")) {
			String opening = event.getLastToken().substring(1);
			for (Player p : this.getServer().getOnlinePlayers()) {
				if (p.getDisplayName().startsWith(opening)) {
					event.getTabCompletions().add("~" + p.getDisplayName());
				}
			}
		}
	}
}
