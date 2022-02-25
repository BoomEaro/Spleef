package ru.boomearo.spleef;

import java.io.File;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.spleef.commands.spleef.CmdExecutorSpleef;
import ru.boomearo.spleef.listeners.PlayerButtonListener;
import ru.boomearo.spleef.listeners.PlayerListener;
import ru.boomearo.spleef.listeners.SpectatorListener;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefTeam;

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

    public static Spleef getInstance() {
        return instance;
    }

}
