package net.thenextlvl.commander.paper.listener;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.commander.CommandRegistry;
import net.thenextlvl.commander.PermissionOverride;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CommandListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(final AsyncPlayerSendCommandsEvent<?> event) {
        if (!event.isAsynchronous() && event.hasFiredAsync()) return;
        if (event.getPlayer().permissionValue("commander.bypass").equals(TriState.TRUE)) return;
        final var hiddenCommands = CommandRegistry.instance().hiddenCommands();
        final var permissionOverride = PermissionOverride.instance();
        event.getCommandNode().getChildren().removeIf(node -> {
            if (node.getName().contains(":")) return true;
            if (hiddenCommands.contains(node.getName())) return true;
            final var permission = permissionOverride.permission(node.getName());
            return permission != null && !event.getPlayer().hasPermission(permission);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final var noSlash = event.getMessage().substring(1);
        final var command = noSlash.split(" ", 2)[0];
        if (command.contains(":") && !event.getPlayer().permissionValue("commander.bypass").equals(TriState.TRUE)) {
            sendUnknownCommand(event.getPlayer(), event.getMessage(), noSlash);
            event.setCancelled(true);
            return;
        }
        final var permission = PermissionOverride.instance().permission(command);
        if (permission == null || event.getPlayer().hasPermission(permission)) return;
        sendUnknownCommand(event.getPlayer(), event.getMessage(), noSlash);
        event.setCancelled(true);
    }

    private void sendUnknownCommand(final Player player, final String message, final String command) {
        player.sendMessage(Component.translatable("command.unknown.command").appendNewline()
                .append(Component.text().append(Component.text(command).decorate(TextDecoration.UNDERLINED))
                        .append(Component.translatable("command.context.here").decorate(TextDecoration.ITALIC))
                        .clickEvent(ClickEvent.suggestCommand(message)))
                .color(NamedTextColor.RED));
    }
}
