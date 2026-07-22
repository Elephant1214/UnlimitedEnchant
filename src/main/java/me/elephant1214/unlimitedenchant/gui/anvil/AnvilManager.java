package me.elephant1214.unlimitedenchant.gui.anvil;

import com.google.common.collect.Maps;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class AnvilManager {
    private static final Map<UUID, AnvilGui> ANVILS = Maps.newHashMap();

    /**
     * Creates a new {@link AnvilGui} for the given player if one doesn't already exist.
     *
     * @return the new {@link AnvilGui} if one doesn't already exist
     * @throws IllegalStateException if there is already an anvil for the specified player
     */
    public static @NotNull AnvilGui makeAnvil(@NotNull Player player, @NotNull Block anvil) {
        final UUID playerId = player.getPlayerProfile().getId();

        if (ANVILS.containsKey(playerId)) {
            throw new IllegalStateException("Something went wrong! Attempted to create a new AnvilGui instance when one already existed!");
        }

        return ANVILS.compute(playerId, (_, _) -> new AnvilGui(player, anvil));
    }

    /**
     * Gets the active {@link AnvilGui} for the given player if one exists.
     *
     * @return An {@link AnvilGui} or null.
     */
    public static @Nullable AnvilGui getAnvil(@NotNull Player player) {
        return ANVILS.get(player.getPlayerProfile().getId());
    }

    /**
     * Removes the active {@link AnvilGui} for the player.
     */
    public static void removeAnvil(@NotNull Player player) {
        ANVILS.remove(player.getPlayerProfile().getId());
    }
}
