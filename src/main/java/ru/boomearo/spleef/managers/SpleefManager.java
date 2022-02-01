package ru.boomearo.spleef.managers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.GameControlException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.managers.GameManager;
import ru.boomearo.gamecontrol.objects.IGameManager;
import ru.boomearo.gamecontrol.objects.defactions.IDefaultAction;
import ru.boomearo.gamecontrol.objects.states.IGameState;

import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.playertype.IPlayerType;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;
import ru.boomearo.spleef.objects.state.SpectatorFirst;

public final class SpleefManager implements IGameManager {

    private final ConcurrentMap<String, SpleefArena> arenas = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, SpleefPlayer> players = new ConcurrentHashMap<>();

    private final SpleefStatistics stats = new SpleefStatistics();

    public static final ChatColor mainColor = GameManager.backgroundTextColor;
    public static final ChatColor variableColor = ChatColor.of(new Color(196, 255, 254));
    public static final ChatColor otherColor = ChatColor.of(new Color(145, 255, 254));

    public static final String gameNameDys = "§8[" + variableColor + "Spleef§8]";
    public static final String prefix = gameNameDys + ": " + mainColor;

    public static final double winReward = 10;

    public SpleefManager() {
        loadArenas();
    }

    @Override
    public String getGameName() {
        return "Spleef";
    }

    @Override
    public String getGameDisplayName() {
        return gameNameDys;
    }

    @Override
    public ChatColor getMainColor() {
        return mainColor;
    }

    @Override
    public ChatColor getVariableColor() {
        return variableColor;
    }

    @Override
    public ChatColor getOtherColor() {
        return otherColor;
    }

    @Override
    public JavaPlugin getPlugin() {
        return Spleef.getInstance();
    }

    @Override
    public SpleefPlayer join(Player pl, String arena, IDefaultAction action) throws ConsoleGameException, PlayerGameException {
        if (pl == null || arena == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        SpleefPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer != null) {
            throw new ConsoleGameException("Игрок уже в игре!");
        }

        SpleefArena tmpArena = this.arenas.get(arena);
        if (tmpArena == null) {
            throw new PlayerGameException("Карта " + mainColor + "'" + variableColor + arena + mainColor + "' не найдена!");
        }

        int count = tmpArena.getAllPlayers().size();
        if (count >= tmpArena.getMaxPlayers()) {
            throw new PlayerGameException("Карта " + mainColor + "'" + variableColor + arena + mainColor + "' переполнена!");
        }

        SpleefTeam team = tmpArena.getFreeTeam();
        if (team == null) {
            throw new ConsoleGameException("Не найдено свободных команд!");
        }

        action.performDefaultJoinAction(pl);

        IGameState state = tmpArena.getState();

        IPlayerType type;

        boolean isSpec;

        //Если статус игры реализует это, значит добавляем игрока в наблюдатели сначала
        if (state instanceof SpectatorFirst) {
            type = new LosePlayer();
            isSpec = true;
        }
        else {
            type = new PlayingPlayer();
            isSpec = false;
        }

        //Создаем игрока
        SpleefPlayer newTp = new SpleefPlayer(pl.getName(), pl, type, tmpArena, team);

        //Добавляем в команду
        team.setPlayer(newTp);

        //Добавляем в арену
        tmpArena.addPlayer(newTp);

        //Добавляем в список играющих
        this.players.put(pl.getName(), newTp);

        //Обрабатываем игрока
        type.preparePlayer(newTp);

        if (isSpec) {
            newTp.sendBoard(1);

            pl.sendMessage(prefix + "Вы присоединились к карте " + mainColor + "'" + variableColor + arena + mainColor + "' как наблюдатель.");
            pl.sendMessage(prefix + "Чтобы покинуть игру, используйте несколько раз " + variableColor + "кнопку " + mainColor + "'" + variableColor + "1" + mainColor + "' или " + variableColor + "телепортируйтесь к любому игроку " + mainColor + "используя возможность наблюдателя.");

            tmpArena.sendMessages(prefix + pl.getDisplayName() + mainColor + " присоединился к игре как наблюдатель!");
        }
        else {
            newTp.sendBoard(0);

            pl.sendMessage(prefix + "Вы присоединились к карте " + mainColor + "'" + variableColor + arena + mainColor + "'!");
            pl.sendMessage(prefix + "Чтобы покинуть игру, используйте " + variableColor + "Магма крем " + mainColor + "или команду " + variableColor + "/lobby" + variableColor + ".");

            int currCount = tmpArena.getAllPlayersType(PlayingPlayer.class).size();
            if (currCount < tmpArena.getMinPlayers()) {
                pl.sendMessage(prefix + "Ожидание " + variableColor + (tmpArena.getMinPlayers() - currCount) + mainColor + " игроков для начала игры...");
            }

            tmpArena.sendMessages(prefix + pl.getDisplayName() + mainColor + " присоединился к игре! " + getRemainPlayersArena(tmpArena, PlayingPlayer.class), pl.getName());
        }

        return newTp;
    }

    @Override
    public void leave(Player pl, IDefaultAction action) throws ConsoleGameException, PlayerGameException {
        if (pl == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        SpleefPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer == null) {
            throw new ConsoleGameException("Игрок не в игре!");
        }

        SpleefTeam team = tmpPlayer.getTeam();

        //Удаляем у тимы игрока
        team.setPlayer(null);

        SpleefArena arena = tmpPlayer.getArena();

        arena.removePlayer(pl.getName());

        this.players.remove(pl.getName());

        tmpPlayer.sendBoard(null);

        pl.sendMessage(prefix + "Вы покинули игру!");

        IPlayerType type = tmpPlayer.getPlayerType();
        if (type instanceof PlayingPlayer) {
            arena.sendMessages(prefix + pl.getDisplayName() + mainColor + " покинул игру! " + getRemainPlayersArena(arena, PlayingPlayer.class), pl.getName());
        }
        else {
            arena.sendMessages(prefix + pl.getDisplayName() + mainColor + " покинул игру!", pl.getName());
        }

        action.performDefaultLeaveAction(pl);
    }

    @Override
    public SpleefPlayer getGamePlayer(String name) {
        return this.players.get(name);
    }

    @Override
    public SpleefArena getGameArena(String name) {
        return this.arenas.get(name);
    }

    @Override
    public Collection<SpleefArena> getAllArenas() {
        return this.arenas.values();
    }

    @Override
    public Collection<SpleefPlayer> getAllPlayers() {
        return this.players.values();
    }

    @Override
    public SpleefStatistics getStatisticManager() {
        return this.stats;
    }

    public SpleefArena getArenaByLocation(Location loc) {
        for (SpleefArena ar : Spleef.getInstance().getSpleefManager().getAllArenas()) {
            if (ar.getArenaRegion().isInRegionPoint(loc)) {
                return ar;
            }
        }
        return null;
    }

    public void loadArenas() {

        FileConfiguration fc = Spleef.getInstance().getConfig();
        List<SpleefArena> arenas = (List<SpleefArena>) fc.getList("arenas");
        if (arenas != null) {
            for (SpleefArena ar : arenas) {
                try {
                    addArena(ar);
                }
                catch (GameControlException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveArenas() {
        FileConfiguration fc = Spleef.getInstance().getConfig();

        List<SpleefArena> tmp = new ArrayList<>(this.arenas.values());
        fc.set("arenas", tmp);

        Spleef.getInstance().saveConfig();
    }

    public void addArena(SpleefArena arena) throws ConsoleGameException {
        if (arena == null) {
            throw new ConsoleGameException("Арена не может быть нулем!");
        }

        SpleefArena tmpArena = this.arenas.get(arena.getName());
        if (tmpArena != null) {
            throw new ConsoleGameException("Арена " + arena.getName() + " уже создана!");
        }

        this.arenas.put(arena.getName(), arena);
    }

    public void removeArena(String name) throws ConsoleGameException {
        if (name == null) {
            throw new ConsoleGameException("Название не может быть нулем!");
        }

        SpleefArena tmpArena = this.arenas.get(name);
        if (tmpArena == null) {
            throw new ConsoleGameException("Арена " + name + " не найдена!");
        }

        this.arenas.remove(name);
    }


    public static String getRemainPlayersArena(SpleefArena arena, Class<? extends IPlayerType> clazz) {
        return "§8[" + variableColor + (clazz != null ? arena.getAllPlayersType(clazz).size() : arena.getAllPlayers().size()) + mainColor + "/" + otherColor + arena.getMaxPlayers() + "§8]";
    }

}
