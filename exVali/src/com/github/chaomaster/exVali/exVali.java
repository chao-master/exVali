package com.github.chaomaster.exVali;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class exVali extends JavaPlugin implements TabExecutor, Listener {

	HashMap<String, String> fromDisplay = null;

	@Override
	public void onEnable() {
		fromDisplay = new HashMap<String, String>();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	void onPlayerLoginEvent(PlayerLoginEvent event) {
		// Check with the server
		try {
			URL checkURL = new URL(
					"http://mc.ukofequestria.co.uk/auth?username="
							+ event.getPlayer().getName());
			BufferedReader checkIn = new BufferedReader(new InputStreamReader(
					checkURL.openStream()));
			String[] checkResult = checkIn.readLine().split("\000", 4);
			this.getLogger().info("DB:" + checkResult.length); // DEBUG
			// Unverified account, displays validation code
			if (checkResult[0] == "unverified") {
				event.setResult(Result.KICK_WHITELIST);
				event.setKickMessage("This account has not been linked with a forum account let.\n"
						+ "To link the account to to the validation system and enter the code "
						+ checkResult[3]);

				// Verified account let unevaulated
			} else if (checkResult[0] == "notwhitelisted") {
				event.setResult(Result.KICK_WHITELIST);
				event.setKickMessage("Your account is pending manual whitlist aproval.\n"
						+ "The server ops will take care of this shortly");

				// Verified let unwhitlisted account
			} else if (checkResult[0] == "whitelistdeclined") {
				event.setResult(Result.KICK_WHITELIST);
				event.setKickMessage("Your account was declined access to the server.\n"
						+ checkResult[3] == "" ? "This is normally due to your forum account having a low number of posts or otherise looking suspicious"
						: ("Given reason" + checkResult[3]));

				// Verified let banned account
			} else if (checkResult[0] == "banned") {
				event.setResult(Result.KICK_BANNED);
				event.setKickMessage("Your account has been banned from the server.\n"
						+ checkResult[3] == "" ? "No reason was given for the ban, contact the server ops for more infomation on it"
						: ("For: " + checkResult[3]));

				// Verified and whitelisted account access granted.
			} else if (checkResult[0] == "whitelisted") {
				event.setResult(Result.ALLOWED);
				Player player = event.getPlayer();
				player.setDisplayName(checkResult[2]);
				player.setPlayerListName(checkResult[2].substring(0, 16));
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

	@EventHandler
	void onPlayerJoinEvnet(PlayerJoinEvent event) {
		fromDisplay.put(event.getPlayer().getDisplayName(), event.getPlayer()
				.getName());
	}

	@EventHandler
	void onPlayerQuitEvent(PlayerQuitEvent event) {
		fromDisplay.remove(event.getPlayer().getDisplayName());
	}

	@EventHandler
	void onPlayerKickEvent(PlayerKickEvent event) {
		fromDisplay.remove(event.getPlayer().getDisplayName());
	}

	@EventHandler
	void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		String oldMsg = event.getMessage();
		StringBuilder newMsg = new StringBuilder(oldMsg);
		Matcher names = Pattern.compile("\b~([^ ])*").matcher(oldMsg);
		int mod = 0;
		while (names.find()) {
			String display = names.group(1);
			String user = null;
			for (Player p : this.getServer().getOnlinePlayers()) {
				if (p.getDisplayName() == display) {
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
