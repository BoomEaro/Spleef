package ru.boomearo.spleef.objects;

import java.io.File;
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
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;

import ru.boomearo.gamecontrol.objects.IGameArena;
import ru.boomearo.gamecontrol.objects.IRegion;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.playertype.IPlayerType;
import ru.boomearo.spleef.objects.state.WaitingState;
import ru.boomearo.spleef.utils.RandomUtil;

public class SpleefArena implements IGameArena, ConfigurationSerializable {

    private final String name;
    
    private final int minPlayers;
    private final int maxPlayers;
    private final int timelimit;
    
    private final World world;
    private final IRegion arenaRegion;
    private final List<Location> spawnPoints;
    
    private final Location arenaCenter;
    
    private final Clipboard clipboard;
 
    private volatile IGameState state = new WaitingState(this);
    
    private final ConcurrentMap<String, SpleefPlayer> players = new ConcurrentHashMap<String, SpleefPlayer>();
    
    public SpleefArena(String name, int minPlayers, int maxPlayers, int timeLimit, World world, IRegion arenaRegion, List<Location> spawnPoints, Location arenaCenter, Clipboard clipboard) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timelimit = timeLimit;
        this.world = world;
        this.arenaRegion = arenaRegion;
        this.spawnPoints = spawnPoints;
        this.arenaCenter = arenaCenter;
        this.clipboard = clipboard;
    }
    
    @Override
    public String getName() {
        return this.name;
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
        return Spleef.getInstance().getTntRunManager();
    }
    
    @Override
    public IGameState getState() {
        return this.state;
    }
    
    @Override
    public void regen() {
        if (this.clipboard == null) {
            return;
        }
        
        try {
            Location loc = this.arenaCenter;
            EditSession editSession = this.clipboard.paste(FaweAPI.getWorld(this.world.getName()), BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), true, true, (Transform) null);
            editSession.flushQueue();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            setState(new WaitingState(this));
        }
    }
    
    public int getMinPlayers() {
        return this.minPlayers;
    }
    
    public int getMaxPlayers() {
        return this.maxPlayers;
    }
    
    public int getTimeLimit() {
        return this.timelimit;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public IRegion getArenaRegion() {
        return this.arenaRegion;
    }
    
    public List<Location> getSpawnPoints() {
        return this.spawnPoints;
    }
    
    public Location getArenaCenter() {
        return this.arenaCenter;
    }
    
    public Clipboard getClipboard() {
        return this.clipboard;
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
        Set<SpleefPlayer> tmp = new HashSet<SpleefPlayer>();
        for (SpleefPlayer tp : this.players.values()) {
            if (tp.getPlayerType().getClass() == clazz) {
                tmp.add(tp);
            }
        }
        return tmp;
    }
    
    public Location getRandomSpawnLocation() {
        List<Location> spawns = getSpawnPoints();
        
        return spawns.get(RandomUtil.getRandomNumberRange(0, spawns.size() - 1));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("name", this.name);
        result.put("minPlayers", this.minPlayers);
        result.put("maxPlayers", this.maxPlayers);
        result.put("timeLimit", this.timelimit);
        
        result.put("world", this.world.getName());
        result.put("region", this.arenaRegion);
        result.put("spawnPoints", this.spawnPoints);
        result.put("arenaCenter", this.arenaCenter);
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static SpleefArena deserialize(Map<String, Object> args) {
        String name = null;
        int minPlayers = 2;
        int maxPlayers = 15;
        int timeLimit = 300;
        World world = null;
        IRegion region = null;
        List<Location> spawnPoints = null;
        Location arenaCenter = null;

        Object na = args.get("name");
        if (na != null) {
            name = (String) na;
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

        Object sp = args.get("spawnPoints");
        if (sp != null) {
            spawnPoints = (List<Location>) sp;
        }

        Object ac = args.get("arenaCenter");
        if (ac != null) {
            arenaCenter = (Location) ac;
        }


        Clipboard cb = null;
        try {
            File schem = new File(Spleef.getInstance().getSchematicDir(), name + ".schem");
            if (schem.exists() && schem.isFile()) {
                cb = FaweAPI.load(schem);
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return new SpleefArena(name, minPlayers, maxPlayers, timeLimit, world, region, spawnPoints, arenaCenter, cb);
    }


}
