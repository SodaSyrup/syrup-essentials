package net.syrupstudios.syrupessentials.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataDirectory;

    public PlayerDataManager(MinecraftServer server) {
        this.playerDataMap = new HashMap<>();
        this.dataDirectory = server.getSavePath(WorldSavePath.ROOT).resolve("syrup_essential_data").toFile();

        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        net.syrupstudios.syrupessentials.SyrupEssentials.LOGGER.info("Player data directory: {}", dataDirectory.getAbsolutePath());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public PlayerData getPlayerData(ServerPlayerEntity player) {
        return getPlayerData(player.getUuid());
    }

    private PlayerData loadPlayerData(UUID uuid) {
        File playerFile = new File(dataDirectory, uuid.toString() + ".snbt");

        if (playerFile.exists()) {
            try {
                String snbtContent = Files.readString(playerFile.toPath());
                NbtCompound nbt = StringNbtReader.parse(snbtContent);
                return PlayerData.fromNbt(uuid, nbt);
            } catch (Exception e) {
                net.syrupstudios.syrupessentials.SyrupEssentials.LOGGER.error("Failed to load player data for " + uuid, e);
            }
        }

        return new PlayerData(uuid);
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File playerFile = new File(dataDirectory, uuid.toString() + ".snbt");

        try {
            NbtCompound nbt = data.toNbt();
            String snbtContent = formatNbt(nbt);

            try (FileWriter writer = new FileWriter(playerFile)) {
                writer.write(snbtContent);
            }
        } catch (IOException e) {
            net.syrupstudios.syrupessentials.SyrupEssentials.LOGGER.error("Failed to save player data for " + uuid, e);
        }
    }

    public void savePlayerData(ServerPlayerEntity player) {
        savePlayerData(player.getUuid());
    }

    public void saveAll() {
        net.syrupstudios.syrupessentials.SyrupEssentials.LOGGER.info("Saving all player data...");
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
    }

    private String formatNbt(NbtCompound nbt) {
        String snbt = nbt.asString();
        StringBuilder formatted = new StringBuilder();
        int indent = 0;
        int listDepth = 0;
        int compactDepth = 0;
        boolean inQuote = false;
        char lastChar = 0;

        for (int i = 0; i < snbt.length(); i++) {
            char c = snbt.charAt(i);

            if (c == '"' && lastChar != '\\') {
                inQuote = !inQuote;
            }

            if (!inQuote) {
                if (compactDepth > 0) {
                    if (c == '}' || c == ']') {
                        compactDepth--;
                        formatted.append(c);
                    } else if (c == '{' || c == '[') {
                        compactDepth++;
                        formatted.append(c);
                    } else if (c == ',') {
                        formatted.append(", ");
                    } else if (c == ':') {
                        formatted.append(": ");
                    } else {
                        formatted.append(c);
                    }
                }
                else {
                    if (c == '{') {
                        if (listDepth > 0) {
                            compactDepth = 1;
                            formatted.append(c);
                        } else {
                            formatted.append("{\n");
                            indent++;
                            appendIndent(formatted, indent);
                        }
                    } else if (c == '[') {
                        if (i + 2 < snbt.length() && snbt.charAt(i + 2) == ';') {
                            compactDepth = 1;
                            formatted.append(c);
                        } else {
                            formatted.append("[\n");
                            indent++;
                            listDepth++;
                            appendIndent(formatted, indent);
                        }
                    } else if (c == '}') {
                        formatted.append("\n");
                        indent--;
                        appendIndent(formatted, indent);
                        formatted.append(c);
                    } else if (c == ']') {
                        formatted.append("\n");
                        indent--;
                        listDepth--;
                        appendIndent(formatted, indent);
                        formatted.append(c);
                    } else if (c == ',') {
                        formatted.append(",\n");
                        appendIndent(formatted, indent);
                    } else if (c == ':') {
                        formatted.append(": ");
                    } else {
                        formatted.append(c);
                    }
                }
            } else {
                formatted.append(c);
            }
            lastChar = c;
        }
        return formatted.toString();
    }

    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }
}