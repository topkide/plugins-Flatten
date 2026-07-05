package com.dotorimaru.flatten.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** '&' 컬러코드 → Adventure 컴포넌트 헬퍼 + 공용 프리픽스. (볼드 &l 미사용) */
public final class Msg {

    public static final String PREFIX = "&8【&a평탄화&8】&r ";

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Msg() {}

    public static Component c(String legacy) {
        return LEGACY.deserialize(legacy);
    }

    public static void send(CommandSender to, String legacy) {
        to.sendMessage(c(legacy));
    }

    /** 프리픽스를 붙여 전송. */
    public static void tell(CommandSender to, String legacy) {
        to.sendMessage(c(PREFIX + legacy));
    }

    public static void actionbar(Player p, String legacy) {
        p.sendActionBar(c(legacy));
    }
}
