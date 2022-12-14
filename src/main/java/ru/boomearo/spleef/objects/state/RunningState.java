package ru.boomearo.spleef.objects.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import ru.boomearo.adveco.AdvEco;
import ru.boomearo.adveco.exceptions.EcoException;
import ru.boomearo.adveco.managers.EcoManager;
import ru.boomearo.adveco.objects.EcoType;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.states.game.ICountable;
import ru.boomearo.gamecontrol.objects.states.game.IRunningState;
import ru.boomearo.gamecontrol.objects.states.perms.SpectatorFirst;
import ru.boomearo.gamecontrol.objects.statistics.DefaultStatsManager;
import ru.boomearo.serverutils.utils.other.DateUtil;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.board.SpleefPLGame;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;
import ru.boomearo.spleef.objects.SpleefStatsType;

public class RunningState implements IRunningState, ICountable, SpectatorFirst {

    private final SpleefArena arena;
    private int count;
    private int deathPlayers = 0;

    private int cd = 20;

    private final Map<String, BlockOwner> removedBlocks = new HashMap<>();

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
        try {
            GameControl.getInstance().getGameManager().setRegenGame(this.arena, true);
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }

        //Подготавливаем всех игроков (например тп на точку возрождения)
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayerType().preparePlayer(tp);

            tp.sendBoard((playerBoard -> new SpleefPLGame(playerBoard, tp)));
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

            if (!this.arena.getArenaRegion().isInRegionPoint(tp.getPlayer().getLocation())) {
                if (tp.getPlayerType() instanceof PlayingPlayer pp) {
                    tp.setPlayerType(new LosePlayer());

                    this.deathPlayers++;

                    //Добавляем единицу в статистику поражений
                    DefaultStatsManager trs = Spleef.getInstance().getSpleefManager().getStatisticManager();
                    trs.addStatsToPlayer(SpleefStatsType.Defeat, tp.getName());

                    this.arena.sendSounds(Sound.ENTITY_WITHER_HURT, 999, 2);

                    SpleefPlayer killer = pp.getKiller();

                    if (killer != null) {
                        if (tp.getName().equals(killer.getName())) {
                            this.arena.sendMessages(SpleefManager.prefix + tp.getPlayer().getDisplayName() + SpleefManager.mainColor + " проиграл, свалившись в свою же яму! " + SpleefManager.getRemainPlayersArena(this.arena, PlayingPlayer.class));
                        }
                        else {
                            this.arena.sendMessages(SpleefManager.prefix + tp.getPlayer().getDisplayName() + SpleefManager.mainColor + " проиграл, свалившись в яму игрока " + killer.getPlayer().getDisplayName() + " " + SpleefManager.getRemainPlayersArena(this.arena, PlayingPlayer.class));
                        }
                    }
                    else {
                        this.arena.sendMessages(SpleefManager.prefix + tp.getPlayer().getDisplayName() + SpleefManager.mainColor + " проиграл, зайдя за границы игры. " + SpleefManager.getRemainPlayersArena(this.arena, PlayingPlayer.class));
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

                            this.arena.sendTitle("", winner.getPlayer().getDisplayName() + SpleefManager.mainColor + " победил!", 20, 20 * 15, 20);

                            this.arena.sendMessages(SpleefManager.prefix + winner.getPlayer().getDisplayName() + SpleefManager.mainColor + " победил!");

                            this.arena.sendSounds(Sound.ENTITY_PLAYER_LEVELUP, 999, 2);

                            //Добавляем единицу в статистику побед
                            trs.addStatsToPlayer(SpleefStatsType.Wins, winner.getName());

                            //В зависимости от того сколько игроков ПРОИГРАЛО мы получим награду.
                            double reward = SpleefManager.winReward + (this.deathPlayers * SpleefManager.winReward);

                            try {
                                AdvEco.getInstance().getEcoManager().addPlayerEco(winner.getName(), winner.getPlayer(), EcoType.Credit, reward);
                            }
                            catch (EcoException e) {
                                e.printStackTrace();
                            }

                            winner.getPlayer().sendMessage(SpleefManager.prefix + "Ваша награда за победу: " + EcoManager.getFormatedEco(reward, EcoType.Credit));

                            this.arena.setState(new EndingState(this.arena));
                            return;
                        }
                    }
                }

                tp.getPlayerType().preparePlayer(tp);
            }


            if (tp.getPlayerType() instanceof PlayingPlayer pp) {

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
                arena.sendMessages(SpleefManager.prefix + "Время вышло! " + SpleefManager.variableColor + "Ничья!");
                arena.setState(new EndingState(this.arena));
                return;
            }

            arena.sendLevels(this.count);

            if (this.count <= 10) {
                arena.sendMessages(SpleefManager.prefix + "Игра закончится через " + SpleefManager.variableColor + DateUtil.formatedTime(this.count, false));
            }
            else {
                if ((this.count % 30) == 0) {
                    arena.sendMessages(SpleefManager.prefix + "Игра закончится через " + SpleefManager.variableColor + DateUtil.formatedTime(this.count, false));
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

    public void addBlock(Block block, SpleefPlayer owner) {
        this.removedBlocks.put(convertLocToString(block.getLocation()), new BlockOwner(block.getType(), owner));
    }

    public static String convertLocToString(Location loc) {
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
    }

    public static class BlockOwner {
        private final Material mat;
        private final SpleefPlayer owner;

        public BlockOwner(Material mat, SpleefPlayer owner) {
            this.mat = mat;
            this.owner = owner;
        }

        public Material getMaterial() {
            return this.mat;
        }

        public SpleefPlayer getName() {
            return this.owner;
        }
    }
}
