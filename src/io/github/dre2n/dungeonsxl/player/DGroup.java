package io.github.dre2n.dungeonsxl.player;

import io.github.dre2n.dungeonsxl.DungeonsXL;
import io.github.dre2n.dungeonsxl.config.MessageConfig;
import io.github.dre2n.dungeonsxl.config.MessageConfig.Messages;
import io.github.dre2n.dungeonsxl.config.WorldConfig;
import io.github.dre2n.dungeonsxl.dungeon.Dungeon;
import io.github.dre2n.dungeonsxl.event.dgroup.DGroupStartFloorEvent;
import io.github.dre2n.dungeonsxl.event.requirement.RequirementDemandEvent;
import io.github.dre2n.dungeonsxl.event.reward.RewardAdditionEvent;
import io.github.dre2n.dungeonsxl.game.Game;
import io.github.dre2n.dungeonsxl.game.GameType;
import io.github.dre2n.dungeonsxl.game.GameTypeDefault;
import io.github.dre2n.dungeonsxl.game.GameWorld;
import io.github.dre2n.dungeonsxl.global.GroupSign;
import io.github.dre2n.dungeonsxl.requirement.Requirement;
import io.github.dre2n.dungeonsxl.reward.Reward;
import io.github.dre2n.dungeonsxl.util.messageutil.MessageUtil;
import io.github.dre2n.dungeonsxl.util.playerutil.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

public class DGroup {
	
	static DungeonsXL plugin = DungeonsXL.getPlugin();
	static MessageConfig messageConfig = plugin.getMessageConfig();
	
	private String name;
	private Player captain;
	private List<Player> players = new CopyOnWriteArrayList<Player>();
	private List<UUID> invitedPlayers = new ArrayList<UUID>();
	private String dungeonName;
	private String mapName;
	private List<String> unplayedFloors = new ArrayList<String>();
	private GameWorld gameWorld;
	private boolean playing;
	private int floorCount;
	private int waveCount;
	private List<Reward> rewards = new ArrayList<Reward>();
	
	public DGroup(String name, Player player) {
		plugin.getDGroups().add(this);
		this.name = name;
		
		captain = player;
		players.add(player);
		
		playing = false;
		floorCount = 0;
	}
	
	public DGroup(Player player, String identifier, boolean multiFloor) {
		plugin.getDGroups().add(this);
		name = "Group_" + plugin.getDGroups().size();
		
		captain = player;
		players.add(player);
		
		Dungeon dungeon = plugin.getDungeons().getDungeon(identifier);
		if (multiFloor && dungeon != null) {
			dungeonName = identifier;
			mapName = dungeon.getConfig().getStartFloor();
			unplayedFloors = dungeon.getConfig().getFloors();
			
		} else {
			mapName = identifier;
		}
		playing = false;
		floorCount = 0;
	}
	
	public DGroup(String name, Player player, String identifier, boolean multiFloor) {
		plugin.getDGroups().add(this);
		this.name = name;
		
		captain = player;
		players.add(player);
		
		Dungeon dungeon = plugin.getDungeons().getDungeon(identifier);
		if (multiFloor && dungeon != null) {
			dungeonName = identifier;
			mapName = dungeon.getConfig().getStartFloor();
			unplayedFloors = dungeon.getConfig().getFloors();
			
		} else {
			mapName = identifier;
		}
		playing = false;
		floorCount = 0;
	}
	
	public DGroup(String name, Player captain, List<Player> players, String identifier, boolean multiFloor) {
		plugin.getDGroups().add(this);
		this.name = name;
		
		this.captain = captain;
		this.players = players;
		
		Dungeon dungeon = plugin.getDungeons().getDungeon(identifier);
		if (multiFloor && dungeon != null) {
			dungeonName = identifier;
			mapName = dungeon.getConfig().getStartFloor();
			unplayedFloors = dungeon.getConfig().getFloors();
			
		} else {
			mapName = identifier;
		}
		playing = false;
		floorCount = 0;
	}
	
	// Getters and setters
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 * the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the captain
	 */
	public Player getCaptain() {
		return captain;
	}
	
	/**
	 * @param captain
	 * the captain to set
	 */
	public void setCaptain(Player captain) {
		this.captain = captain;
	}
	
	/**
	 * @return the players
	 */
	public List<Player> getPlayers() {
		return players;
	}
	
	/**
	 * @param player
	 * the player to add
	 */
	public void addPlayer(Player player) {
		sendMessage(plugin.getMessageConfig().getMessage(Messages.GROUP_PLAYER_JOINED, player.getName()));
		MessageUtil.sendMessage(player, plugin.getMessageConfig().getMessage(Messages.PLAYER_JOIN_GROUP));
		
		players.add(player);
	}
	
	/**
	 * @param player
	 * the player to remove
	 */
	public void removePlayer(Player player) {
		players.remove(player);
		GroupSign.updatePerGroup(this);
		
		// Send message
		sendMessage(plugin.getMessageConfig().getMessage(Messages.PLAYER_LEFT_GROUP, player.getName()));
		
		// Check group
		if (isEmpty()) {
			remove();
		}
	}
	
	/**
	 * @return the players
	 */
	public List<Player> getInvitedPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (UUID uuid : invitedPlayers) {
			players.add(plugin.getServer().getPlayer(uuid));
		}
		
		return players;
	}
	
	/**
	 * @param player
	 * the player to add
	 */
	public void addInvitedPlayer(Player player, boolean silent) {
		if (player == null) {
			return;
		}
		
		if (DGroup.getByPlayer(player) != null) {
			if ( !silent) {
				MessageUtil.sendMessage(captain, plugin.getMessageConfig().getMessage(Messages.ERROR_IN_GROUP, player.getName()));
			}
			return;
		}
		
		if ( !silent) {
			MessageUtil.sendMessage(player, plugin.getMessageConfig().getMessage(Messages.PLAYER_INVITED, captain.getName(), name));
		}
		
		// Send message
		if ( !silent) {
			sendMessage(plugin.getMessageConfig().getMessage(Messages.GROUP_INVITED_PLAYER, captain.getName(), player.getName(), name));
		}
		
		// Add player
		invitedPlayers.add(player.getUniqueId());
	}
	
	/**
	 * @param player
	 * the player to remove
	 */
	public void removeInvitedPlayer(Player player, boolean silent) {
		if (player == null) {
			return;
		}
		
		if (DGroup.getByPlayer(player) != this) {
			if ( !silent) {
				MessageUtil.sendMessage(captain, plugin.getMessageConfig().getMessage(Messages.ERROR_NOT_IN_GROUP, player.getName(), name));
			}
			return;
		}
		
		if ( !silent) {
			MessageUtil.sendMessage(player, plugin.getMessageConfig().getMessage(Messages.PLAYER_UNINVITED, player.getName(), name));
		}
		
		// Send message
		if ( !silent) {
			for (Player groupPlayer : getPlayers()) {
				MessageUtil.sendMessage(groupPlayer, plugin.getMessageConfig().getMessage(Messages.GROUP_UNINVITED_PLAYER, captain.getName(), player.getName(), name));
			}
		}
		
		invitedPlayers.remove(player.getUniqueId());
	}
	
	/**
	 * Remove all invitations for players who are not online
	 */
	public void clearOfflineInvitedPlayers() {
		ArrayList<UUID> toRemove = new ArrayList<UUID>();
		
		for (UUID uuid : invitedPlayers) {
			if (plugin.getServer().getPlayer(uuid) == null) {
				toRemove.add(uuid);
			}
		}
		
		invitedPlayers.removeAll(toRemove);
	}
	
	/**
	 * @return the gameWorld
	 */
	public GameWorld getGameWorld() {
		return gameWorld;
	}
	
	/**
	 * @param gameWorld
	 * the gameWorld to set
	 */
	public void setGameWorld(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	
	/**
	 * @return the dungeonName
	 */
	public String getDungeonName() {
		return dungeonName;
	}
	
	/**
	 * @param dungeonName
	 * the dungeonName to set
	 */
	public void setDungeonName(String dungeonName) {
		this.dungeonName = dungeonName;
	}
	
	/**
	 * @return the dungeon (saved by name only)
	 */
	public Dungeon getDungeon() {
		return plugin.getDungeons().getDungeon(dungeonName);
	}
	
	/**
	 * @param dungeon
	 * the dungeon to set (saved by name only)
	 */
	public void setDungeon(Dungeon dungeon) {
		dungeonName = dungeon.getName();
	}
	
	/**
	 * @return if the group is playing
	 */
	public String getMapName() {
		return mapName;
	}
	
	/**
	 * @param name
	 * the name to set
	 */
	public void setMapName(String name) {
		mapName = name;
	}
	
	/**
	 * @return the unplayedFloors
	 */
	public List<String> getUnplayedFloors() {
		return unplayedFloors;
	}
	
	/**
	 * @param unplayedFloor
	 * the unplayedFloor to add
	 */
	public void addUnplayedFloor(String unplayedFloor) {
		unplayedFloors.add(unplayedFloor);
	}
	
	/**
	 * @param unplayedFloor
	 * the unplayedFloor to add
	 */
	public void removeUnplayedFloor(String unplayedFloor) {
		if (getDungeon().getConfig().getRemoveWhenPlayed()) {
			unplayedFloors.remove(unplayedFloor);
		}
	}
	
	/**
	 * @return if the group is playing
	 */
	public boolean isPlaying() {
		return playing;
	}
	
	/**
	 * @param playing
	 * set if the group is playing
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	/**
	 * @return the floorCount
	 */
	public int getFloorCount() {
		return floorCount;
	}
	
	/**
	 * @param floorCount
	 * the floorCount to set
	 */
	public void setFloorCount(int floorCount) {
		this.floorCount = floorCount;
	}
	
	/**
	 * @return the waveCount
	 */
	public int getWaveCount() {
		return waveCount;
	}
	
	/**
	 * @param waveCount
	 * the waveCount to set
	 */
	public void setWaveCount(int waveCount) {
		this.waveCount = waveCount;
	}
	
	/**
	 * @return the rewards
	 */
	public List<Reward> getRewards() {
		return rewards;
	}
	
	/**
	 * @param reward
	 * the rewards to add
	 */
	public void addReward(Reward reward) {
		RewardAdditionEvent event = new RewardAdditionEvent(reward, this);
		
		if (event.isCancelled()) {
			return;
		}
		
		rewards.add(reward);
	}
	
	/**
	 * @param reward
	 * the rewards to remove
	 */
	public void removeReward(Reward reward) {
		rewards.remove(reward);
	}
	
	/**
	 * @return whether there are players in the group
	 */
	public boolean isEmpty() {
		return players.isEmpty();
	}
	
	public void remove() {
		plugin.getDGroups().remove(this);
		GroupSign.updatePerGroup(this);
	}
	
	public void startGame(Game game) {
		if (game == null) {
			return;
		}
		
		for (DGroup dGroup : game.getDGroups()) {
			if (dGroup == null) {
				continue;
			}
			
			for (Player player : dGroup.getPlayers()) {
				DPlayer dPlayer = DPlayer.getByPlayer(player);
				if (dPlayer == null) {
					continue;
				}
				
				if ( !dPlayer.isReady()) {
					return;
				}
			}
		}
		
		gameWorld.setGame(game);
		
		DGroupStartFloorEvent event = new DGroupStartFloorEvent(this, gameWorld);
		
		if (event.isCancelled()) {
			return;
		}
		
		playing = true;
		gameWorld.startGame();
		floorCount++;
		
		for (Player player : getPlayers()) {
			DPlayer dPlayer = DPlayer.getByPlayer(player);
			dPlayer.respawn();
			if (dungeonName != null) {
				MessageUtil.sendScreenMessage(player, "&b&l" + dungeonName.replaceAll("_", " "), "&4&l" + mapName.replaceAll("_", " "));
				
			} else {
				MessageUtil.sendScreenMessage(player, "&4&l" + mapName.replaceAll("_", " "));
			}
			
			WorldConfig config = gameWorld.getConfig();
			if (config != null) {
				for (Requirement requirement : config.getRequirements()) {
					RequirementDemandEvent requirementDemandEvent = new RequirementDemandEvent(requirement, player);
					
					if (requirementDemandEvent.isCancelled()) {
						continue;
					}
					
					requirement.demand(player);
				}
				
				GameType gameType = game.getType();
				if (gameType == GameTypeDefault.DEFAULT) {
					player.setGameMode(config.getGameMode());
					
				} else {
					player.setGameMode(gameType.getGameMode());
				}
			}
		}
		
		GroupSign.updatePerGroup(this);
	}
	
	public void finishWave(double mobCountIncreaseRate) {
		for (DGroup dGroup : DGroup.getByGameWorld(gameWorld)) {
			dGroup.sendMessage(messageConfig.getMessage(Messages.GROUP_WAVE_FINISHED, String.valueOf(dGroup.getWaveCount()) + "TIME"));// TODO
			
			for (Player player : dGroup.getPlayers()) {
				PlayerUtil.secureTeleport(player, gameWorld.getLocStart());
			}
		}
	}
	
	/**
	 * Send a message to all players in the group
	 */
	public void sendMessage(String message) {
		for (Player player : players) {
			if (player.isOnline()) {
				MessageUtil.sendMessage(player, message);
			}
		}
	}
	
	/**
	 * Send a message to all players in the group
	 *
	 * @param except
	 * Players who do not receive the message
	 */
	public void sendMessage(String message, Player... except) {
		for (Player player : players) {
			if (player.isOnline() && !player.equals(except)) {
				MessageUtil.sendMessage(player, message);
			}
		}
	}
	
	// Statics
	
	public static DGroup getByName(String name) {
		for (DGroup dGroup : plugin.getDGroups()) {
			if (dGroup.getName().equals(name)) {
				return dGroup;
			}
		}
		
		return null;
	}
	
	public static DGroup getByPlayer(Player player) {
		for (DGroup dGroup : plugin.getDGroups()) {
			if (dGroup.getPlayers().contains(player)) {
				return dGroup;
			}
		}
		
		return null;
	}
	
	public static void leaveGroup(Player player) {
		for (DGroup dGroup : plugin.getDGroups()) {
			if (dGroup.getPlayers().contains(player)) {
				dGroup.getPlayers().remove(player);
			}
		}
	}
	
	/**
	 * @param gameWorld
	 * the GameWorld to check
	 * @return a List of DGroups in this GameWorld
	 */
	public static List<DGroup> getByGameWorld(GameWorld gameWorld) {
		List<DGroup> dGroups = new ArrayList<DGroup>();
		for (DGroup dGroup : plugin.getDGroups()) {
			if (dGroup.getGameWorld().equals(gameWorld)) {
				dGroups.add(dGroup);
			}
		}
		
		return dGroups;
	}
	
}
