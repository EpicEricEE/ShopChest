package de.epiceric.shopchest.nms.v1_8_R1;

import de.epiceric.shopchest.nms.IJsonBuilder;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonBuilder extends IJsonBuilder {

    public JsonBuilder(String text, String hoverText, String downloadLink) {
        parse(text, hoverText, downloadLink);
    }

    @Override
    public void sendJson(Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutChat(ChatSerializer.a(toString())));
    }
}