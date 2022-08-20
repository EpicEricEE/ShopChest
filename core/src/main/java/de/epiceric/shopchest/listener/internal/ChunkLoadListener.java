package de.epiceric.shopchest.listener.internal;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.ShopManagerImpl;
import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.util.Logger;

public class ChunkLoadListener implements Listener {
    private ShopChest plugin;
    
    private final Set<Chunk> newLoadedChunks = new HashSet<>();

    public ChunkLoadListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!((ShopChestImpl) plugin).getDatabase().isInitialized()) {
            return;
        }

        // Wait 10 ticks after first event is triggered, so that multiple
        // chunk loads can be handled at the same time without having to
        // send a database request for each chunk.

        if (newLoadedChunks.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                int chunkCount = newLoadedChunks.size();
                
                ((ShopManagerImpl) plugin.getShopManager())
                        .loadShops(newLoadedChunks.toArray(new Chunk[chunkCount]))
                        .exceptionally(ex -> {
                            Logger.severe("Failed to load shops in newly loaded chunks");
                            Logger.severe(ex);
                            return null;
                        });

                newLoadedChunks.clear();
            }, 10L);
        }
        newLoadedChunks.add(e.getChunk());
    }
}