package com.dotorimaru.flatten;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** config.yml 값을 읽어 보관한다. {@link #reload()} 로 즉시 재적용 가능. */
public final class FlattenConfig {

    private final FlattenPlugin plugin;

    public int defaultY;
    public int defaultRadius;
    public int maxRadius;
    public Material surface;
    public boolean circle;
    public boolean physics;
    public final Set<String> disabledWorlds = new HashSet<>();

    public FlattenConfig(FlattenPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        defaultY = c.getInt("default-y", 64);
        defaultRadius = Math.max(0, c.getInt("default-radius", 3));
        maxRadius = Math.max(1, c.getInt("max-radius", 20));
        circle = c.getBoolean("circle", false);
        physics = c.getBoolean("apply-physics", false);

        String mat = c.getString("surface-block", "GRASS_BLOCK");
        Material m = Material.matchMaterial(mat == null ? "GRASS_BLOCK" : mat);
        if (m == null || !m.isBlock()) {
            plugin.getLogger().warning("surface-block 값이 올바르지 않습니다: " + mat + " → GRASS_BLOCK 사용");
            m = Material.GRASS_BLOCK;
        }
        surface = m;

        disabledWorlds.clear();
        for (String w : c.getStringList("disabled-worlds")) {
            if (w != null && !w.isBlank()) disabledWorlds.add(w.toLowerCase(Locale.ROOT));
        }
    }
}
