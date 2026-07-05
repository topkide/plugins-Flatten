package com.dotorimaru.flatten.listener;

import com.dotorimaru.flatten.FlattenManager;
import com.dotorimaru.flatten.FlattenPlugin;
import com.dotorimaru.flatten.FlattenState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** 플레이어 이동 시 "지나가는 곳" 평탄화 + 접속 종료 시 상태 정리. */
public final class MoveListener implements Listener {

    private final FlattenManager manager;

    public MoveListener(FlattenPlugin plugin) {
        this.manager = plugin.manager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        FlattenState st = manager.getIfPresent(p);
        if (st == null || !st.isEnabled()) return;

        Location to = e.getTo();
        if (to == null) return;

        int bx = to.getBlockX();
        int bz = to.getBlockZ();

        // 같은 블록 내 이동(고개 돌리기 등)은 무시 → 블록이 바뀔 때만 처리
        if (!st.moved(bx, bz)) return;
        st.mark(bx, bz);

        manager.flattenAround(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // DB 미사용 → 접속 종료 시 상태 초기화
        manager.remove(e.getPlayer());
    }
}
