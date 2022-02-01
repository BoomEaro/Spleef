package ru.boomearo.spleef;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.spleef.commands.spleef.CmdExecutorSpleef;
import ru.boomearo.spleef.database.Sql;
import ru.boomearo.spleef.database.sections.SectionStats;
import ru.boomearo.spleef.listeners.PlayerButtonListener;
import ru.boomearo.spleef.listeners.PlayerListener;
import ru.boomearo.spleef.listeners.SpectatorListener;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.statistics.SpleefStatsData;
import ru.boomearo.spleef.objects.statistics.SpleefStatsType;

public class Spleef extends JavaPlugin {

    private SpleefManager arenaManager = null;

    private static Spleef instance = null;

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(SpleefArena.class);
        ConfigurationSerialization.registerClass(SpleefTeam.class);

        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый...");
            saveDefaultConfig();
        }

        if (this.arenaManager == null) {
            this.arenaManager = new SpleefManager();
        }

        loadDataBase();
        loadDataFromDatabase();

        try {
            GameControl.getInstance().getGameManager().registerGame(this.getClass(), this.arenaManager);
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }

        getCommand("spleef").setExecutor(new CmdExecutorSpleef());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerButtonListener(), this);

        getServer().getPluginManager().registerEvents(new SpectatorListener(), this);

        getLogger().info("Плагин успешно запущен.");
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (Exception e) {
            e.printStackTrace();
            getLogger().info("Не удалось отключиться от базы данных...");
        }

        try {
            GameControl.getInstance().getGameManager().unregisterGame(this.getClass());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }

        ConfigurationSerialization.unregisterClass(SpleefArena.class);
        ConfigurationSerialization.unregisterClass(SpleefTeam.class);

        getLogger().info("Плагин успешно выключен.");
    }

    public SpleefManager getSpleefManager() {
        return this.arenaManager;
    }

    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();

        }
        try {
            Sql.initSql();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            for (SpleefStatsType type : SpleefStatsType.values()) {
                SpleefStatsData data = this.arenaManager.getStatisticManager().getStatsData(type);
                for (SectionStats stats : Sql.getInstance().getAllStatsData(type).get()) {
                    data.addStatsPlayer(new StatsPlayer(stats.name, stats.value));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Spleef getInstance() {
        return instance;
    }

}
