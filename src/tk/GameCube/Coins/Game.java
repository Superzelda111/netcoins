package tk.GameCube.Coins;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.garbagemule.MobArena.events.ArenaCompleteEvent;
import com.garbagemule.MobArena.events.ArenaKillEvent;

public class Game extends JavaPlugin implements Listener {

	private static Connection connection;

	public void onDisable() {
		try {
			if (connection != null && connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		
		Player p = e.getPlayer();
		openConnection();
			try {
				PreparedStatement sql = connection
						.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
				sql.setString(1, (p.getName()));
				ResultSet result = sql.executeQuery();
				result.next();

				int deaths = result.getInt("deaths");
				
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				Scoreboard board = manager.getNewScoreboard();
				 
				Objective objective = board.registerNewObjective("lives", "dummy");
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				objective.setDisplayName("§6§lCoins");
				  Score score = objective.getScore(Bukkit.getOfflinePlayer("§6Your Coins:"));
				  score.setScore(deaths); //Example
					 p.setScoreboard(board);

				connection.close();
				sql.close();
				result.close();
			} catch (Exception el) {
				el.printStackTrace();
			} finally {
				closeConnection();
			}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = (Player) sender;
		if(label.equalsIgnoreCase("coins")){
			if( args.length == 1 && args[0].equalsIgnoreCase("50")){
			openConnection();
			try {
				int previousLogins = 0;

				if (playerDataContainsPlayer(player)) {
					PreparedStatement sql = connection
							.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
					sql.setString(1, (player.getName()));
					ResultSet result = sql.executeQuery();
					result.next();

					previousLogins = result.getInt("deaths");

					PreparedStatement loginsUpdate = connection
							.prepareStatement("UPDATE `player_data` SET deaths=? WHERE player=?;");
					loginsUpdate.setInt(1, previousLogins + 50);
					loginsUpdate.setString(2,
							(player.getName()));
					loginsUpdate.executeUpdate();

					loginsUpdate.close();
					sql.close();
					result.close();
					player.sendMessage("§650 Coins added. Please rejoin to see results.");
				} else {
					PreparedStatement newPlayer = connection
							.prepareStatement("INSERT INTO `player_data` values(?,0,1,0);");
					newPlayer.setString(1,
							(player.getName()));
					newPlayer.execute();
					newPlayer.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		}
			if(args.length == 2 && args[1].equalsIgnoreCase("100") ){
				Player p = Bukkit.getPlayer(args[0]);
				openConnection();
				try {
					int previousLogins = 0;

					if (playerDataContainsPlayer(p)) {
						PreparedStatement sql = connection
								.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
						sql.setString(1, (p.getName()));
						ResultSet result = sql.executeQuery();
						result.next();

						previousLogins = result.getInt("deaths");

						PreparedStatement loginsUpdate = connection
								.prepareStatement("UPDATE `player_data` SET deaths=? WHERE player=?;");
						loginsUpdate.setInt(1, previousLogins + 100);
						loginsUpdate.setString(2,
								(p.getName()));
						loginsUpdate.executeUpdate();

						loginsUpdate.close();
						sql.close();
						result.close();
						player.sendMessage("Added 100 coins to " + p.getName() + "!");
						p.sendMessage("§6100 Coins added. Please rejoin to see results.");
					} else {
						PreparedStatement newPlayer = connection
								.prepareStatement("INSERT INTO `player_data` values(?,0,1,0);");
						newPlayer.setString(1,
								(p.getName()));
						newPlayer.execute();
						newPlayer.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					closeConnection();
				}
		}
		}
		return false;
	}
	
	
	
	
	

	public synchronized static void openConnection() {
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://198.20.118.226:3306/mc62385", "mc62385",
					"1132498a46");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static void closeConnection() {
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static boolean playerDataContainsPlayer(Player player) {
		try {
			PreparedStatement sql = connection
					.prepareStatement("SELECT * FROM `player_data` WHERE player=?;");
			sql.setString(1, player.getName());
			ResultSet resultSet = sql.executeQuery();
			boolean containsPlayer = resultSet.next();
			sql.close();
			resultSet.close();
			return containsPlayer;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		openConnection();
		try {
			int previousLogins = 0;

			if (playerDataContainsPlayer(event.getPlayer())) {
				PreparedStatement sql = connection
						.prepareStatement("SELECT logins FROM `player_data` WHERE player=?;");
				sql.setString(1, event.getPlayer().getName());
				ResultSet result = sql.executeQuery();
				result.next();

				previousLogins = result.getInt("logins");

				PreparedStatement loginsUpdate = connection
						.prepareStatement("UPDATE `player_data` SET logins=? WHERE player=?;");
				loginsUpdate.setInt(1, previousLogins + 1);
				loginsUpdate.setString(2, event.getPlayer().getName());
				loginsUpdate.executeUpdate();

				loginsUpdate.close();
				sql.close();
				result.close();
			} else {
				PreparedStatement newPlayer = connection
						.prepareStatement("INSERT INTO `player_data` values(?,0,0,1);");
				newPlayer.setString(1, event.getPlayer().getName());
				newPlayer.execute();
				newPlayer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		openConnection();
		try {
			int previousLogins = 0;

			if (playerDataContainsPlayer(event.getEntity().getPlayer())) {
				PreparedStatement sql = connection
						.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
				sql.setString(1, (event.getEntity().getPlayer().getName()));
				ResultSet result = sql.executeQuery();
				result.next();

				previousLogins = result.getInt("deaths");

				PreparedStatement loginsUpdate = connection
						.prepareStatement("UPDATE `player_data` SET deaths=? WHERE player=?;");
				loginsUpdate.setInt(1, previousLogins + 1);
				loginsUpdate.setString(2,
						(event.getEntity().getPlayer().getName()));
				loginsUpdate.executeUpdate();

				loginsUpdate.close();
				sql.close();
				result.close();
			} else {
				PreparedStatement newPlayer = connection
						.prepareStatement("INSERT INTO `player_data` values(?,0,1,0);");
				newPlayer.setString(1,
						(event.getEntity().getPlayer().getName()));
				newPlayer.execute();
				newPlayer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}


	public int getDeaths(Player p) {
		openConnection();
		if (playerDataContainsPlayer(p)) {
			try {
				PreparedStatement sql = connection
						.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
				sql.setString(1, (p.getName()));
				ResultSet result = sql.executeQuery();
				result.next();

				result.getInt("deaths");

				connection.close();
				sql.close();
				result.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		}
		return 0;
	}
	
	
	
	@EventHandler
	public void ArenaDone(ArenaCompleteEvent event){
		Player p = (Player) event.getSurvivors();
		
		openConnection();
		try {
			int previousLogins = 0;

			if (playerDataContainsPlayer(p)) {
				PreparedStatement sql = connection
						.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
				sql.setString(1, (p.getName()));
				ResultSet result = sql.executeQuery();
				result.next();

				previousLogins = result.getInt("deaths");

				PreparedStatement loginsUpdate = connection
						.prepareStatement("UPDATE `player_data` SET deaths=? WHERE player=?;");
				loginsUpdate.setInt(1, previousLogins + 100);
				loginsUpdate.setString(2,
						(p.getName()));
				loginsUpdate.executeUpdate();

				loginsUpdate.close();
				sql.close();
				result.close();
				p.sendMessage("§6100 Coins added. Please rejoin to see results.");
			} else {
				PreparedStatement newPlayer = connection
						.prepareStatement("INSERT INTO `player_data` values(?,0,1,0);");
				newPlayer.setString(1,
						(p.getName()));
				newPlayer.execute();
				newPlayer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	@EventHandler
	public void onMobKill(ArenaKillEvent event){
		Player p = (Player) event.getPlayer();
		
		openConnection();
		try {
			int previousLogins = 0;

			if (playerDataContainsPlayer(p)) {
				PreparedStatement sql = connection
						.prepareStatement("SELECT deaths FROM `player_data` WHERE player=?;");
				sql.setString(1, (p.getName()));
				ResultSet result = sql.executeQuery();
				result.next();

				previousLogins = result.getInt("deaths");

				PreparedStatement loginsUpdate = connection
						.prepareStatement("UPDATE `player_data` SET deaths=? WHERE player=?;");
				loginsUpdate.setInt(1, previousLogins + 3);
				loginsUpdate.setString(2,
						(p.getName()));
				loginsUpdate.executeUpdate();

				loginsUpdate.close();
				sql.close();
				result.close();
			} else {
				PreparedStatement newPlayer = connection
						.prepareStatement("INSERT INTO `player_data` values(?,0,1,0);");
				newPlayer.setString(1,
						(p.getName()));
				newPlayer.execute();
				newPlayer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	
	
	
}
