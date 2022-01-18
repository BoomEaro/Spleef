package ru.boomearo.spleef.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import ru.boomearo.gamecontrol.objects.IForceStartable;
import ru.boomearo.gamecontrol.objects.arena.ClipboardRegenableGameArena;
import ru.boomearo.gamecontrol.objects.region.IRegion;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.playertype.IPlayerType;
import ru.boomearo.spleef.objects.state.WaitingState;

public class SpleefArena extends ClipboardRegenableGameArena implements IForceStartable, ConfigurationSerializable {

    private final int minPlayers;
    private final int maxPlayers;
    private final int timeLimit;

    private final IRegion arenaRegion;
    private final ConcurrentMap<Integer, SpleefTeam> teams;

    private volatile IGameState state = new WaitingState(this);

    private final ConcurrentMap<String, SpleefPlayer> players = new ConcurrentHashMap<>();

    private boolean forceStarted = false;

    public SpleefArena(String name, World world, Material icon, Location originCenter, int minPlayers, int maxPlayers, int timeLimit, IRegion arenaRegion, ConcurrentMap<Integer, SpleefTeam> teams) {
        super(name, world, icon, originCenter);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timeLimit = timeLimit;
        this.arenaRegion = arenaRegion;
        this.teams = teams;
    }

    @Override
    public boolean isForceStarted() {
        return this.forceStarted;
    }

    @Override
    public void setForceStarted(boolean force) {
        this.forceStarted = force;
    }

    @Override
    public SpleefPlayer getGamePlayer(String name) {
        return this.players.get(name);
    }

    @Override
    public Collection<SpleefPlayer> getAllPlayers() {
        return this.players.values();
    }

    @Override
    public SpleefManager getManager() {
        return Spleef.getInstance().getSpleefManager();
    }

    @Override
    public IGameState getState() {
        return this.state;
    }

    @Override
    public int getMinPlayers() {
        return this.minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getTimeLimit() {
        return this.timeLimit;
    }

    public IRegion getArenaRegion() {
        return this.arenaRegion;
    }

    public SpleefTeam getTeamById(int id) {
        return this.teams.get(id);
    }

    public Collection<SpleefTeam> getAllTeams() {
        return this.teams.values();
    }

    public SpleefTeam getFreeTeam() {
        for (SpleefTeam team : this.teams.values()) {
            if (team.getPlayer() == null) {
                return team;
            }
        }
        return null;
    }

    public void setState(IGameState state) {
        //Устанавливаем новое
        this.state = state;

        //Инициализируем новое
        this.state.initState();
    }

    public void addPlayer(SpleefPlayer player) {
        this.players.put(player.getName(), player);
    }

    public void removePlayer(String name) {
        this.players.remove(name);
    }

    public void sendMessages(String msg) {
        sendMessages(msg, null);
    }

    public void sendMessages(String msg, String ignore) {
        for (SpleefPlayer tp : this.players.values()) {
            if (ignore != null) {
                if (tp.getName().equals(ignore)) {
                    continue;
                }
            }

            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.sendMessage(msg);
            }
        }
    }

    public void sendLevels(int level) {
        if (Bukkit.isPrimaryThread()) {
            handleSendLevels(level);
        }
        else {
            Bukkit.getScheduler().runTask(Spleef.getInstance(), () -> {
                handleSendLevels(level);
            });
        }
    }

    public void sendSounds(Sound sound, float volume, float pitch, Location loc) {
        for (SpleefPlayer tp : this.players.values()) {
            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.playSound((loc != null ? loc : pl.getLocation()), sound, volume, pitch);
            }
        }
    }

    public void sendSounds(Sound sound, float volume, float pitch) {
        sendSounds(sound, volume, pitch, null);
    }

    private void handleSendLevels(int level) {
        for (SpleefPlayer tp : this.players.values()) {
            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.setLevel(level);
            }
        }
    }

    public Collection<SpleefPlayer> getAllPlayersType(Class<? extends IPlayerType> clazz) {
        Set<SpleefPlayer> tmp = new HashSet<>();
        for (SpleefPlayer tp : this.players.values()) {
            if (tp.getPlayerType().getClass() == clazz) {
                tmp.add(tp);
            }
        }
        return tmp;
    }

    public void sendTitle(String first, String second, int in, int stay, int out) {
        for (SpleefPlayer sp : this.players.values()) {
            Player pl = sp.getPlayer();
            if (pl.isOnline()) {
                pl.sendTitle(first, second, in, stay, out);
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("name", getName());
        result.put("icon", getIcon().name());
        result.put("minPlayers", this.minPlayers);
        result.put("maxPlayers", this.maxPlayers);
        result.put("timeLimit", this.timeLimit);

        result.put("world", getWorld().getName());
        result.put("region", this.arenaRegion);

        List<SpleefTeam> t = new ArrayList<>(this.teams.values());
        result.put("teams", t);
        result.put("arenaCenter", getOriginCenter());

        return result;
    }

    public static SpleefArena deserialize(Map<String, Object> args) {
        String name = null;
        Material icon = Material.STONE;
        int minPlayers = 2;
        int maxPlayers = 15;
        int timeLimit = 300;
        World world = null;
        IRegion region = null;
        List<SpleefTeam> teams = new ArrayList<>();
        Location arenaCenter = null;

        Object na = args.get("name");
        if (na != null) {
            name = (String) na;
        }

        Object ic = args.get("icon");
        if (ic != null) {
            try {
                icon = Material.valueOf((String) ic);
            }
            catch (Exception ignored) {
            }
        }

        Object minp = args.get("minPlayers");
        if (minp != null) {
            minPlayers = ((Number) minp).intValue();
        }

        Object maxp = args.get("maxPlayers");
        if (maxp != null) {
            maxPlayers = ((Number) maxp).intValue();
        }

        Object tl = args.get("timeLimit");
        if (tl != null) {
            timeLimit = ((Number) tl).intValue();
        }

        Object wo = args.get("world");
        if (wo != null) {
            world = Bukkit.getWorld((String) wo);
        }

        Object re = args.get("region");
        if (re != null) {
            region = (IRegion) re;
        }

        Object sp = args.get("teams");
        if (sp != null) {
            teams = (List<SpleefTeam>) sp;
        }

        Object ac = args.get("arenaCenter");
        if (ac != null) {
            arenaCenter = (Location) ac;
        }

        ConcurrentMap<Integer, SpleefTeam> nTeams = new ConcurrentHashMap<>();
        for (SpleefTeam team : teams) {
            nTeams.put(team.getId(), team);
        }

        return new SpleefArena(name, world, icon, arenaCenter, minPlayers, maxPlayers, timeLimit, region, nTeams);
    }


}
