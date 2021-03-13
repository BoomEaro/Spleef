package ru.boomearo.tntrun.objects.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IRunningState;
import ru.boomearo.gamecontrol.utils.DateUtil;
import ru.boomearo.gamecontrol.utils.Vault;
import ru.boomearo.tntrun.Spleef;
import ru.boomearo.tntrun.managers.SpleefManager;
import ru.boomearo.tntrun.managers.SpleefStatistics;
import ru.boomearo.tntrun.objects.SpleefArena;
import ru.boomearo.tntrun.objects.SpleefPlayer;
import ru.boomearo.tntrun.objects.playertype.LosePlayer;
import ru.boomearo.tntrun.objects.playertype.PlayingPlayer;
import ru.boomearo.tntrun.objects.statistics.SpleefStatsType;

public class RunningState implements IRunningState, ICountable, SpectatorFirst {

    private final SpleefArena arena;
    private int count;
    private int deathPlayers = 0;

    private int cd = 20;
    
    private final Map<String, BlockOwner> removedBlocks = new HashMap<String, BlockOwner>();
    
    public RunningState(SpleefArena arena, int count) {
        this.arena = arena;
        this.count = count;
    }
    
    @Override
    public String getName() {
        return "§aИдет игра";
    }

    @Override
    public SpleefArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        //Подготавливаем всех игроков (например тп на точку возрождения)
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayerType().preparePlayer(tp);
        }
        
        this.arena.sendMessages(SpleefManager.prefix + "Игра началась. Удачи!");
        this.arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
    }
    
    @Override
    public void autoUpdateHandler() {
        //Играть одним низя
        if (this.arena.getAllPlayersType(PlayingPlayer.class).size() <= 1) {
            this.arena.sendMessages(SpleefManager.prefix + "Не достаточно игроков для игры! Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }
        
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                if (tp.getPlayerType() instanceof PlayingPlayer) {
                    PlayingPlayer pp = (PlayingPlayer) tp.getPlayerType();
                    tp.setPlayerType(new LosePlayer());
                    
                    this.deathPlayers++;
                    
                    //Добавляем единицу в статистику поражений
                    SpleefStatistics trs = Spleef.getInstance().getTntRunManager().getStatisticManager();
                    trs.addStats(SpleefStatsType.Defeat, tp.getName());
                    
                    this.arena.sendSounds(Sound.ENTITY_WITHER_HURT, 999, 2);
                    
                    if (pp.getKiller() != null) {
                        if (tp.getName().equals(pp.getKiller())) {
                            this.arena.sendMessages(SpleefManager.prefix + "Игрок §c" + tp.getName() + " §7проиграл, свалившись в свою же яму! " + SpleefManager.getRemainPlayersArena(this.arena));
                        }
                        else {
                            this.arena.sendMessages(SpleefManager.prefix + "Игрок §c" + tp.getName() + " §7проиграл, свалившись в яму игрока §c" + pp.getKiller() + " " + SpleefManager.getRemainPlayersArena(this.arena));
                        }
                    }
                    else {
                        this.arena.sendMessages(SpleefManager.prefix + "Игрок §c" + tp.getName() + " §7проиграл, зайдя за границы игры. " + SpleefManager.getRemainPlayersArena(this.arena));
                    }
                    
                    Collection<SpleefPlayer> win = this.arena.getAllPlayersType(PlayingPlayer.class);
                    if (win.size() == 1) {
                        SpleefPlayer winner = null;
                        for (SpleefPlayer w : win) {
                            winner = w;
                            break;
                        }
                        if (winner != null) {
                            winner.setPlayerType(new LosePlayer());
                            this.arena.sendMessages(SpleefManager.prefix + "Игрок §c" + winner.getName() + " §7победил!");
                            
                            this.arena.sendSounds(Sound.ENTITY_PLAYER_LEVELUP, 999, 2);
                            
                            //Добавляем единицу в статистику побед
                            trs.addStats(SpleefStatsType.Wins, winner.getName());
                            
                            //В зависимости от того сколько игроков ПРОИГРАЛО мы получим награду.
                            double reward = SpleefManager.winReward + (this.deathPlayers * SpleefManager.winReward);
                            
                            Vault.addMoney(winner.getName(), reward);
                            
                            winner.getPlayer().sendMessage(SpleefManager.prefix + "Ваша награда за победу: " + GameControl.getFormatedEco(reward));
                            
                            this.arena.setState(new EndingState(this.arena));
                            return;
                        }
                    }
                }
                
                tp.getPlayerType().preparePlayer(tp);
            }
            

            if (tp.getPlayerType() instanceof PlayingPlayer) {
                PlayingPlayer pp = (PlayingPlayer) tp.getPlayerType();
                
                BlockOwner bo = this.removedBlocks.get(convertLocToString(tp.getPlayer().getLocation()));
                if (bo != null) {
                    pp.setKiller(bo.getName());
                }
            }
        }
        
        
        handleCount(this.arena);
    }
    
    @Override
    public int getCount() {
        return this.count;
    }
    
    @Override
    public void setCount(int count) {
        this.count = count;
    }
    
    private void handleCount(SpleefArena arena) {
        if (this.cd <= 0) {
            this.cd = 20;
            
            if (this.count <= 0) {
                arena.sendMessages(SpleefManager.prefix + "Время вышло! §cНичья!");
                arena.setState(new EndingState(this.arena));
                return;
            }
            
            arena.sendLevels(this.count);
            
            if (this.count <= 10) {
                arena.sendMessages(SpleefManager.prefix + "Игра закончится через §c" + DateUtil.formatedTime(this.count, false));
            }
            else {
                if ((this.count % 30) == 0){
                    arena.sendMessages(SpleefManager.prefix + "Игра закончится через §c" + DateUtil.formatedTime(this.count, false));
                }
            }
            
            this.count--;
            
            return;
            
        }
        this.cd--;
    }

    public BlockOwner getBlockByLocation(Location loc) {
        return this.removedBlocks.get(convertLocToString(loc));
    }
    
    public void addBlock(Block block, String owner) {
        this.removedBlocks.put(convertLocToString(block.getLocation()), new BlockOwner(block.getType(), owner));
    }
    
    public static String convertLocToString(Location loc) {
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" +  loc.getBlockZ();
    }

    public static class BlockOwner {
        private final Material mat;
        private final String owner;
        
        public BlockOwner(Material mat, String owner) {
            this.mat = mat;
            this.owner = owner;
        }
        
        public Material getMaterial() {
            return this.mat;
        }
        
        public String getName() {
            return this.owner;
        }
    }
}
