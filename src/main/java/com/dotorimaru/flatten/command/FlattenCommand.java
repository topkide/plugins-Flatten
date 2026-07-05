package com.dotorimaru.flatten.command;

import com.dotorimaru.flatten.FlattenManager;
import com.dotorimaru.flatten.FlattenPlugin;
import com.dotorimaru.flatten.FlattenState;
import com.dotorimaru.flatten.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /평탄화 명령어.
 *
 * <pre>
 *   /평탄화 on [Y] [범위]   - 켜기 (지나가는 곳 평탄화). Y·범위 인라인 지정 가능
 *   /평탄화 off            - 끄기
 *   /평탄화 y &lt;값&gt;         - 기준 Y 좌표 지정
 *   /평탄화 범위 &lt;값&gt;       - 반경 지정
 *   /평탄화 정보            - 현재 설정 확인
 *   /평탄화 리로드          - config.yml 재적용 (flatten.admin)
 * </pre>
 */
public final class FlattenCommand implements CommandExecutor, TabCompleter {

    private final FlattenPlugin plugin;
    private final FlattenManager manager;

    public FlattenCommand(FlattenPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.manager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            Msg.send(sender, "&c플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (!p.hasPermission("flatten.use")) {
            Msg.tell(p, "&c권한이 없습니다.");
            return true;
        }

        if (args.length == 0) { help(p); return true; }

        String sub = args[0].toLowerCase(Locale.ROOT);
        FlattenState st = manager.get(p);

        switch (sub) {
            case "on", "켜기" -> {
                // /평탄화 on [Y] [범위]
                if (args.length >= 2) {
                    Integer y = parseInt(args[1]);
                    if (y == null) { Msg.tell(p, "&cY 좌표는 숫자여야 합니다."); return true; }
                    st.setY(y);
                }
                if (args.length >= 3) {
                    Integer rad = parseInt(args[2]);
                    if (rad == null || rad < 0) { Msg.tell(p, "&c범위는 0 이상의 숫자여야 합니다."); return true; }
                    st.setRadius(Math.min(rad, plugin.cfg().maxRadius));
                }

                st.setEnabled(true);
                st.resetMark();

                if (!manager.worldAllowed(p.getWorld())) {
                    Msg.tell(p, "&e주의: 이 월드에서는 평탄화가 비활성화되어 있습니다.");
                }
                Msg.tell(p, "&a평탄화 &2ON &7| 기준 Y=&f" + st.getY() + " &7범위=&f" + st.getRadius() + "&7칸");

                // 켜는 즉시 현재 위치 1회 평탄화
                st.mark(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
                manager.flattenAround(p);
            }
            case "off", "끄기" -> {
                st.setEnabled(false);
                Msg.tell(p, "&c평탄화 OFF");
            }
            case "y", "높이" -> {
                if (args.length < 2) { Msg.tell(p, "&c사용법: /평탄화 y <값>"); return true; }
                Integer y = parseInt(args[1]);
                if (y == null) { Msg.tell(p, "&cY 좌표는 숫자여야 합니다."); return true; }
                st.setY(y);
                Msg.tell(p, "&a기준 Y 좌표를 &f" + y + " &a(으)로 설정했습니다.");
            }
            case "범위", "radius" -> {
                if (args.length < 2) { Msg.tell(p, "&c사용법: /평탄화 범위 <값>"); return true; }
                Integer rad = parseInt(args[1]);
                if (rad == null || rad < 0) { Msg.tell(p, "&c범위는 0 이상의 숫자여야 합니다."); return true; }
                int applied = Math.min(rad, plugin.cfg().maxRadius);
                st.setRadius(applied);
                if (applied != rad) Msg.tell(p, "&e최대 범위(" + plugin.cfg().maxRadius + ")로 제한되었습니다.");
                Msg.tell(p, "&a범위를 &f" + applied + "&a칸으로 설정했습니다.");
            }
            case "정보", "info", "status" -> info(p, st);
            case "리로드", "reload" -> {
                if (!p.hasPermission("flatten.admin")) { Msg.tell(p, "&c권한이 없습니다."); return true; }
                plugin.cfg().reload();
                Msg.tell(p, "&a설정(config.yml)을 다시 불러왔습니다.");
            }
            default -> help(p);
        }
        return true;
    }

    private void info(Player p, FlattenState st) {
        Msg.tell(p, "&7━━━ &a평탄화 정보 &7━━━");
        Msg.send(p, " &7상태: " + (st.isEnabled() ? "&2ON" : "&cOFF"));
        Msg.send(p, " &7기준 Y: &f" + st.getY() + " &8(Y 이상 제거 / Y-1 채움)");
        Msg.send(p, " &7범위: &f" + st.getRadius() + "&7칸 &8(최대 " + plugin.cfg().maxRadius + ")");
        Msg.send(p, " &7채움 블록: &f" + plugin.cfg().surface.name());
    }

    private void help(Player p) {
        Msg.tell(p, "&7━━━ &a평탄화 사용법 &7━━━");
        Msg.send(p, " &f/평탄화 on [Y] [범위] &7- 켜기 (지나가는 곳 평탄화)");
        Msg.send(p, " &f/평탄화 off &7- 끄기");
        Msg.send(p, " &f/평탄화 y <값> &7- 기준 Y 좌표 지정");
        Msg.send(p, " &f/평탄화 범위 <값> &7- 반경 지정");
        Msg.send(p, " &f/평탄화 정보 &7- 현재 설정 확인");
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String cur = args[0].toLowerCase(Locale.ROOT);
            for (String s : new String[]{"on", "off", "y", "범위", "정보", "리로드"}) {
                if (s.startsWith(cur)) out.add(s);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("y"))) {
            if (sender instanceof Player p) out.add(String.valueOf(p.getLocation().getBlockY()));
        }
        return out;
    }
}
