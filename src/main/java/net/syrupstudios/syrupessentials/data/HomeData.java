package net.syrupstudios.syrupessentials.data;

import net.minecraft.nbt.NbtCompound;

public record HomeData(double x, double y, double z, float yaw, float pitch, String dimension) {

    // Custom method to convert TO Nbt
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        nbt.putDouble("z", z);
        nbt.putFloat("yaw", yaw);
        nbt.putFloat("pitch", pitch);
        nbt.putString("dimension", dimension);
        return nbt;
    }

    // Custom method to convert FROM Nbt
    public static HomeData fromNbt(NbtCompound nbt) {
        return new HomeData(
                nbt.getDouble("x"),
                nbt.getDouble("y"),
                nbt.getDouble("z"),
                nbt.getFloat("yaw"),
                nbt.getFloat("pitch"),
                nbt.getString("dimension")
        );
    }
}