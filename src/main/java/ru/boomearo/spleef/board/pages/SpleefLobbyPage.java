package ru.boomearo.spleef.board.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.statistics.SpleefStatsData;
import ru.boomearo.spleef.objects.statistics.SpleefStatsType;

public class SpleefLobbyPage extends AbstractPage {

    private final SpleefPlayer spPlayer;
    
    public SpleefLobbyPage(AbstractPageList pageList, SpleefPlayer spPlayer) {
        super(pageList);
        this.spPlayer = spPlayer;
    }

    @Override
    public int getTimeToChange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public String getTitle() {
        return SpleefManager.gameNameDys;
    }

    @Override
    protected List<AbstractHolder> createHolders() {
        List<AbstractHolder> holders = new ArrayList<AbstractHolder>();
        
        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return SpleefManager.mainColor + new SimpleDateFormat("dd/MM/yyyy").format(new Date(System.currentTimeMillis()));
            }

        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return " ";
            }

        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Карта: '" + SpleefManager.variableColor + spPlayer.getArena().getName() + SpleefManager.mainColor + "'";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Игроков: " + SpleefManager.variableColor + spPlayer.getArena().getAllPlayers().size() + "§8/" + SpleefManager.otherColor + spPlayer.getArena().getMaxPlayers();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Статус: " + spPlayer.getArena().getState().getName();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Статистика: ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(SpleefStatsType.Wins, spPlayer.getName());
            }
            
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(SpleefStatsType.Defeat, spPlayer.getName());
            }
            
            
        });
        
        return holders;
    }

    private static String getStatisticData(SpleefStatsType type, String name) {
        SpleefStatsData data = Spleef.getInstance().getSpleefManager().getStatisticManager().getStatsData(type);
        StatsPlayer sp = data.getStatsPlayer(name);
        if (sp == null) {
            return SpleefManager.mainColor + type.getName() + ": " + SpleefManager.variableColor + "0";
        }
        
        return SpleefManager.mainColor + type.getName() + ": " + SpleefManager.variableColor + (long) sp.getValue();
    }
    
}