package net.syrupstudios.syrupessentials.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUuid;
    private final Map<String, HomeData> homes;
    private Vec3d lastPosition;
    private String lastDimension;
    private int maxHomes = 5;

    public PlayerData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.homes = new HashMap<>();
    }

    public boolean addHome(String name, ServerPlayerEntity player) {
        if (homes.size() >= maxHomes) {
            return false;
        }

        HomeData home = new HomeData(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.getWorld().getRegistryKey().getValue().toString()
        );

        homes.put(name.toLowerCase(), home);
        return true;
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public HomeData getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public Map<String, HomeData> getHomes() {
        return homes;
    }

    public int getHomeCount() {
        return homes.size();
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int max) {
        this.maxHomes = max;
    }

    public void setLastPosition(Vec3d pos, String dimension) {
        this.lastPosition = pos;
        this.lastDimension = dimension;
    }

    public Vec3d getLastPosition() {
        return lastPosition;
    }

    public String getLastDimension() {
        return lastDimension;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        NbtList homesList = new NbtList();
        for (Map.Entry<String, HomeData> entry : homes.entrySet()) {
            NbtCompound homeNbt = new NbtCompound();
            homeNbt.putString("name", entry.getKey());
            homeNbt.put("data", entry.getValue().toNbt());
            homesList.add(homeNbt);
        }
        nbt.put("homes", homesList);

        if (lastPosition != null) {
            NbtCompound lastPos = new NbtCompound();
            lastPos.putDouble("x", lastPosition.x);
            lastPos.putDouble("y", lastPosition.y);
            lastPos.putDouble("z", lastPosition.z);
            lastPos.putString("dimension", lastDimension);
            nbt.put("last_position", lastPos);
        }

        return nbt;
    }

    public static PlayerData fromNbt(UUID uuid, NbtCompound nbt) {
        PlayerData data = new PlayerData(uuid);

        if (nbt.contains("homes")) {
            NbtList homesList = nbt.getList("homes", 10);
            for (int i = 0; i < homesList.size(); i++) {
                NbtCompound homeNbt = homesList.getCompound(i);
                String name = homeNbt.getString("name");
                HomeData home = HomeData.fromNbt(homeNbt.getCompound("data"));
                data.homes.put(name, home);
            }
        }

        if (nbt.contains("last_position")) {
            NbtCompound lastPos = nbt.getCompound("last_position");
            data.lastPosition = new Vec3d(
                    lastPos.getDouble("x"),
                    lastPos.getDouble("y"),
                    lastPos.getDouble("z")
            );
            data.lastDimension = lastPos.getString("dimension");
        }

        return data;
    }
}