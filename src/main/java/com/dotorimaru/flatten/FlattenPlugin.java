package com.dotorimaru.flatten;

import com.dotorimaru.flatten.command.FlattenCommand;
import com.dotorimaru.flatten.listener.MoveListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 평탄화 플러그인 메인 클래스.
 *
 * <p>MySQL/Redis/Core 의존 없이 단독 동작하는 독립 플러그인.
 * 플레이어별 ON/OFF 상태와 기준 Y·반경은 메모리에만 보관하며,
 * 접속 종료 시 초기화된다.</p>
 */
public final class FlattenPlugin extends JavaPlugin {

    private FlattenConfig cfg;
    private FlattenManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.cfg = new FlattenConfig(this);
        this.manager = new FlattenManager(this, cfg);

        PluginCommand c = getCommand("평탄화");
        if (c == null) {
            getLogger().severe("'평탄화' 명령어 등록 실패 (plugin.yml 확인). 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        FlattenCommand exec = new FlattenCommand(this);
        c.setExecutor(exec);
        c.setTabCompleter(exec);

        getServer().getPluginManager().registerEvents(new MoveListener(this), this);

        getLogger().info("Flatten(평탄화) 활성화 완료.");
    }

    public FlattenConfig cfg() { return cfg; }

    public FlattenManager manager() { return manager; }
}
