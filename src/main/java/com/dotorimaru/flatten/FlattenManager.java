package com.dotorimaru.flatten;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 평탄화 실행 로직 및 플레이어 상태 보관.
 *
 * <p>평탄화 규칙:
 * <ul>
 *   <li>기준 Y(포함) 이상의 모든 블록을 공기로 제거한다.</li>
 *   <li>기준 Y 바로 아래(Y-1)를 설정된 채움 블록(기본 잔디)으로 채운다.</li>
 * </ul>
 * 결과적으로 잔디 표면이 기준 Y 높이에서 평평하게 형성된다.</p>
 */
public final class FlattenManager {

    private final FlattenPlugin plugin;
    private final FlattenConfig cfg;
    private final Map<UUID, FlattenState> states = new HashMap<>();

    public FlattenManager(FlattenPlugin plugin, FlattenConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    /** 상태를 가져오되 없으면 config 기본값으로 생성. */
    public FlattenState get(Player p) {
        return states.computeIfAbsent(p.getUniqueId(),
                k -> new FlattenState(cfg.defaultY, cfg.defaultRadius));
    }

    /** 상태를 가져오되 없으면 null. */
    public FlattenState getIfPresent(Player p) {
        return states.get(p.getUniqueId());
    }

    public void remove(Player p) {
        states.remove(p.getUniqueId());
    }

    public boolean worldAllowed(World w) {
        return !cfg.disabledWorlds.contains(w.getName().toLowerCase(Locale.ROOT));
    }

    /**
     * 플레이어 주변 지정 반경을 평탄화한다.
     * (main 스레드에서만 호출할 것)
     */
    public void flattenAround(Player p) {
        FlattenState st = getIfPresent(p);
        if (st == null || !st.isEnabled()) return;

        World world = p.getWorld();
        if (!worldAllowed(world)) return;

        final int r = Math.min(st.getRadius(), cfg.maxRadius);
        final int targetY = st.getY();
        final int cx = p.getLocation().getBlockX();
        final int cz = p.getLocation().getBlockZ();

        final int minY = world.getMinHeight();
        final int maxY = world.getMaxHeight();          // 상한(제외값) → 실제 최상단 블록 = maxY-1
        final int floorY = targetY - 1;                 // 잔디로 채울 높이
        final int startY = Math.max(targetY, minY);     // 제거 시작 높이
        final int rr = r * r;
        final boolean physics = cfg.physics;
        final Material surface = cfg.surface;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (cfg.circle && (dx * dx + dz * dz) > rr) continue;

                final int x = cx + dx;
                final int z = cz + dz;

                // 로드되지 않은 청크는 동기 로딩(렉) 방지를 위해 건너뜀
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                // 1) 기준 Y 이상 제거 — 지형 표면 높이까지만 스캔하여 하늘(공기) 낭비 스캔 방지
                int top = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
                int clearTo = Math.min(top, maxY - 1);
                for (int y = startY; y <= clearTo; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (!b.getType().isAir()) {
                        b.setType(Material.AIR, physics);
                    }
                }

                // 2) 기준 Y 바로 아래(Y-1)를 채움 블록으로 매움 → 빈 공간 제거
                if (floorY >= minY && floorY < maxY) {
                    Block floor = world.getBlockAt(x, floorY, z);
                    if (floor.getType() != surface) {
                        floor.setType(surface, physics);
                    }
                }
            }
        }
    }
}
