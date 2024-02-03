package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.utils.ColorUtils;
import fr.krishenk.castel.utils.PacketUtils;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class GuildMaSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public GuildMaSCPacket(CastelPlayer cPlayer, Guild guild) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(1);
        Group group = guild.getGroup();

        packetByteBuffer.writeString(group.getId().toString());
        packetByteBuffer.writeString(group.getLeaderId().toString());
        packetByteBuffer.writeString(PacketUtils.GUILD.getLeaderName(guild));
        packetByteBuffer.writeString(group.getName());
        packetByteBuffer.writeString(group.getTag());
        packetByteBuffer.writeLong(group.getSince());
        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.GUILD.onlinePlayer(guild)));
        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.GUILD.offlinePlayer(guild)));
        packetByteBuffer.writeString(new Gson().toJson(guild.getRanks().getRanks().values()));
        packetByteBuffer.writeDouble(group.getPublicHomeCost());
        packetByteBuffer.writeBoolean(group.isHomePublic());
        packetByteBuffer.writeString(group.getColor() != null ? ColorUtils.toHexString(group.getColor()) : "#FFFFFF");
        packetByteBuffer.writeDouble(guild.getBank());
        packetByteBuffer.writeString(group.getTax());
        packetByteBuffer.writeString(group.getFlag());
//        Map<UUID, GuildRelationshipRequest> relationshipRequests
//        Map<UUID, GuildRelation> relations;
//        Map<GuildRelation, Set<RelationAttribute>> attributes;
        packetByteBuffer.writeLong(group.getResourcePoints());
        packetByteBuffer.writeBoolean(guild.requiresInvite());
        packetByteBuffer.writeBoolean(group.isPermanent());
        packetByteBuffer.writeBoolean(group.isHidden());
        packetByteBuffer.writeString(new Gson().toJson(group.getMails()));
//        Set<SimpleChunkLocation> lands;
//        Map<Guild.Powerup, Integer> powerups;
//        Map<Guild.MiscUpgrade, Integer> miscUpgrades;
//        Map<String, Guild.InviteCode> inviteCodes;
        packetByteBuffer.writeString(new Gson().toJson(guild.getChallenges()));
        packetByteBuffer.writeString(new Gson().toJson(guild.getChest().getContents()));
        packetByteBuffer.writeString(guild.getLore());
        packetByteBuffer.writeBoolean(guild.isPacifist());
        packetByteBuffer.writeInt(guild.getMaxLandsModifier());



//        packetByteBuffer.writeString(guild.getName());
//        packetByteBuffer.writeDouble(guild.getBank());
//        packetByteBuffer.writeInt((int) guild.getPower());
//        packetByteBuffer.writeInt(Config.Powers.POWER_FACTION_MAX.getManager().getInt());
//        packetByteBuffer.writeString(PacketUtils.getLeaderName(guild));
//        packetByteBuffer.writeString(guild.getLeaderId().toString());
//        packetByteBuffer.writeString(new Gson().toJson(guild.getRanks().getRanks().values()));
//        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.onlinePlayer(guild)));
//        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.offlinePlayer(guild)));
        packetByteBuffer.writeString(cPlayer.getRank().getName());


        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
