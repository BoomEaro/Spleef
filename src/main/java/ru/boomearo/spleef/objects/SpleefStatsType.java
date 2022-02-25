package ru.boomearo.spleef.objects;

import org.bukkit.Material;
import ru.boomearo.gamecontrol.objects.statistics.IStatsType;

public enum SpleefStatsType implements IStatsType {

    Wins("Побед", "wins", Material.IRON_SWORD),
    Defeat("Поражений", "defeats", Material.SKELETON_SKULL);

    private final String name;
    private final String dbName;
    private final Material icon;

    SpleefStatsType(String name, String dbName, Material icon) {
        this.name = name;
        this.dbName = dbName;
        this.icon = icon;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTableName() {
        return this.dbName;
    }

    @Override
    public Material getIcon() {
        return this.icon;
    }

}
