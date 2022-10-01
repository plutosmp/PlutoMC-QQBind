package top.plutomc.qqbind;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import top.plutomc.qqbind.utils.BindUtil;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerListener implements Listener {
    @EventHandler
    public void playerJoinEvent(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        if (BindUtil.isWaitingToVerify(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, MiniMessage.miniMessage().deserialize("<green>验证成功！请重新进入服务器。"));
            try {
                BindUtil.completeVerify(uuid);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (!BindUtil.isBound(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, MiniMessage.miniMessage().deserialize("<red>请加入服务器 QQ 群并绑定账号再进行游戏！"));
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        QQBindPlugin.EXECUTOR_SERVICE.submit(() -> {
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<gray>这个账号已绑定至 QQ: <aqua>" + BindUtil.getBind(event.getPlayer().getUniqueId())));
        });
    }
}