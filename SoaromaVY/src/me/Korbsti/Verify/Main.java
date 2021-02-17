package me.Korbsti.Verify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
	private int charnum = 0;
	String osName = System.getProperty("os.name");
	String players = "players.yml";
	String dir = System.getProperty("user.dir");
	private String playersPathFile = dir + File.separator + "plugins" + File.separator + "SoaromaVY" + File.separator
			+ players;
	private int kickNum;
	HashMap<Player, StringBuilder> codeMap = new HashMap<>();
	HashMap<String, Boolean> verified = new HashMap<>();
	HashMap<String, Integer> wrongCounter = new HashMap<>();
	@Override
	public void onEnable() {

		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			verified.put(allPlayers.getName(), true);
		}
		getServer().getPluginManager().registerEvents(this, this);
		loadConfiguration();
	}

	@Override
	public void onDisable() {
		saveConfig();

	}

	public void checkIfInFile(Player player) {
		Player playerUser = (Player) player;
		boolean t = false;
		String name = playerUser.getName();
		String line = null;
		File f = new File(playersPathFile);
		try {
			if (f.createNewFile()) {
				;
			} else {
				;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			FileReader fileReader1 = new FileReader(playersPathFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader1);

			while ((line = bufferedReader.readLine()) != null) {
				if (line.contentEquals(name)) {
					verified.put(playerUser.getName(), true);
					t = true;
					if (getConfig().getBoolean("captcha-each-time-on-login") == true) {
						verified.put(playerUser.getName(), true);
					}
				}
			}
			if (t != true) {
				verified.put(playerUser.getName(), false);
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadConfiguration() {
		reloadConfig();
		getConfig().addDefault("char-length", 5);
		getConfig().addDefault("captchaTrylimit", 5);
		getConfig().addDefault("captcha-each-time-on-login", false);
		getConfig().addDefault("enableCaptcha", true);
		getConfig().addDefault("captchaKickIfFail", true);
		getConfig().addDefault("disableMoveWhenVerifying", true);
		getConfig().addDefault("playerChatDisableWhenVerifying", true);
		getConfig().addDefault("captchaKickIfFail", true);
		getConfig().addDefault("captchaBanIfFail", false);
		getConfig().addDefault("chars-on-verify", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
		getConfig().addDefault("messageJoinIn", "&8[&3VY&8] &3Please type in /captcha %s to play");
		getConfig().addDefault("messageWhenVerified", "&8[&3VY&8] &3Thank you for verifying :D");
		getConfig().addDefault("enterCaptchaPrecommand", "&8[&3VY&8] &3Please type in the captcha");
		getConfig().addDefault("messageWhenCaptchaWrongWithLimit",
				"&8[&3VY&8] &3Wrong code, you have %s tries remaining until you get kicked");
		getConfig().addDefault("messageWhenCaptchaWrongWithoutLimit", "&8[&3VY&8] &3Wrong code, please try again");
		getConfig().addDefault("captchaWhenCaptchaVerifiedCommand", "&8[&3VY&8] &3You are already verified");
		getConfig().addDefault("kickMessage", "&8[&3VY&8] &3Failing verification");
		getConfig().addDefault("captchaWhenDisabled", "&8[&3VY&8] &3Captcha is disabled");
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	@EventHandler
	public void commandPre(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		if (e.getMessage().startsWith("/captcha")) {
			return;
		}
		if ((verified.get(e.getPlayer().getName())) == true) {
			return;
		} else {
			e.setCancelled(true);
			String message = String.format(getConfig().getString("messageJoinIn"), codeMap.get(player));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {

		if (getConfig().getBoolean("captcha-each-time-on-login") == true) {
			File f = new File(playersPathFile);
			f.deleteOnExit();
			verified.clear();
		}
		Player player = (Player) event.getPlayer();
		if (getConfig().getBoolean("enableCaptcha") == false) {
			verified.put(player.getName(), true);
			return;
		}
		if (player.hasPermission("SoaromaVY.bypass")) {
			verified.put(player.getName(), true);
			return;
		}
		verified.put(player.getName(), false);
		StringBuilder code = new StringBuilder();
		while (charnum < getConfig().getInt("char-length")) {
			charnum++;
			String randomChar;
			String chars = getConfig().getString("chars-on-verify");
			int random = (int) (Math.random() * (chars.length() - 1) + 1);
			randomChar = Character.toString(chars.charAt(random));
			code.append(randomChar);
			codeMap.put(player, code);
		}
		charnum = 0;
		checkIfInFile(event.getPlayer());
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getPlayer();
		if (verified.get(player.getName()).equals(false)
				&& getConfig().getBoolean("playerChatDisableWhenVerifying") == true) {
			String message = String.format(getConfig().getString("messageJoinIn"), codeMap.get(player));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			event.setCancelled(true);
			return;
		}
		if (verified.get(player.getName()).equals(false)
				&& getConfig().getBoolean("playerChatDisableWhenVerifying") == false) {
			String message = String.format(getConfig().getString("messageJoinIn"), codeMap.get(player));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			return;
		}
		return;
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getPlayer();
		if (verified.get(player.getName()).equals(false)
				&& getConfig().getBoolean("disableMoveWhenVerifying") == true) {
			String message = String.format(getConfig().getString("messageJoinIn"), codeMap.get(player));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			event.setCancelled(true);
			return;
		} else if (verified.get(player.getName()).equals(false)) {
			String message = String.format(getConfig().getString("messageJoinIn"), codeMap.get(player));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		return;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (!(label.equalsIgnoreCase("captcha") && args.length >= 0)) {
				return false;
			}
			Player player = (Player) sender;
			String playername = sender.getName();
			if (label.equalsIgnoreCase("captcha") && args.length >= 2 || args.length == 0) {
				player.sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&8[&3VY&8] &3Proper usage, /captcha <code>"));
				return true;
			}
			if (player.hasPermission("SoaromaVY.bypass")) {
				verified.put(playername, true);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&8[&3VY&8] &3You have bypass permissions, captcha does not apply to you"));
				return true;
			}
			if (label.equalsIgnoreCase("captcha") && args.length == 1) {
				if (getConfig().getBoolean("enableCaptcha") == false) {
					player.sendMessage(
							ChatColor.translateAlternateColorCodes('&', getConfig().getString("captchaWhenDisabled")));
					verified.put(player.getName(), true);
					return true;
				}
				if (verified.get(player.getName()).equals(true)) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							getConfig().getString("captchaWhenCaptchaVerifiedCommand")));
					return true;
				}
				StringBuilder playerCaptchaCode = new StringBuilder();
				for (int i = 0; i < args.length; i++) {
					playerCaptchaCode.append(args[i]);
				}
				try {
					if (codeMap.get(player).toString().equals(playerCaptchaCode.toString())) {
						FileWriter writer = new FileWriter(playersPathFile, true);
						writer.write(playername + System.getProperty("line.separator"));
						writer.close();
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								getConfig().getString("messageWhenVerified")));
						checkIfInFile(player);
						if (getConfig().getBoolean("captcha-each-time-on-login") == true) {
							File f = new File(playersPathFile);
							f.delete();
						}
						return true;
					} else {
						if(wrongCounter.get(playername) == null) {
							wrongCounter.put(playername, 0);
						}
						kickNum = getConfig().getInt("captchaTrylimit");
						if (getConfig().getBoolean("captchaBanIfFail") == true) {
							wrongCounter.put(playername, wrongCounter.get(playername) + 1);
							String message = String.format(getConfig().getString("messageWhenCaptchaWrongWithLimit"), kickNum - wrongCounter.get(playername));
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							if (wrongCounter.get(playername) >= getConfig().getInt("captchaTrylimit")) {
								String banner = null;
								BanList banList = Bukkit.getBanList(BanList.Type.NAME);
								banList.addBan(playername, getConfig().getString("kickMessage"), null, banner);
								player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
										getConfig().getString("kickMessage")));
								wrongCounter.put(playername, 0);
							}
							return true;
						} else if (getConfig().getBoolean("captchaKickIfFail") == true) {
							wrongCounter.put(playername, wrongCounter.get(playername) + 1);
							String message = String.format(getConfig().getString("messageWhenCaptchaWrongWithLimit"),  kickNum - wrongCounter.get(playername));
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
							if (wrongCounter.get(playername) >= getConfig().getInt("captchaTrylimit")) {
								player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
										getConfig().getString("kickMessage")));
								wrongCounter.put(playername, 0);
							}
							return true;
						}

					}
				} catch (IOException e) {
					;
				}
				return true;
			}
		}
		return false;
	}

}