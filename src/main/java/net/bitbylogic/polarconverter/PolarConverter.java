package net.bitbylogic.polarconverter;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.ChunkSelector;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PolarConverter {

    static void main(String[] args) {
        if (args == null || args.length == 0) {
            sendHelp();
            return;
        }

        String worldFolderName = null;
        String outputName = null;
        int chunkRadius = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "--world" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("Missing value for --world");
                        sendHelp();
                        return;
                    }

                    worldFolderName = args[++i];
                }
                case "--output" -> {
                    if (i + 1 >= args.length) {
                        System.err.println("Missing value for --output");
                        sendHelp();
                        return;
                    }

                    outputName = args[++i];
                }
                case "--radius" -> {
                    if (i + 1 >= args.length) {
                        continue;
                    }

                    chunkRadius = Integer.parseInt(args[++i]);
                }
                default -> {
                    System.err.println("Unknown argument: " + arg);
                    sendHelp();
                    return;
                }
            }
        }

        if (worldFolderName == null || outputName == null) {
            System.err.println("Both --world and --output are required.");
            sendHelp();
            return;
        }

        File worldFolder = new File(worldFolderName);

        startMinestom();
        convertWorld(worldFolder, outputName, chunkRadius);
    }

    private static void convertWorld(File worldFolder, String outputName, int chunkRadius) {
        if (!validateWorldFile(worldFolder)) {
            return;
        }

        try {
            if (chunkRadius != -1) {
                System.out.println("Converting with chunk radius of " + chunkRadius + "...");
                PolarWorld polarWorld = AnvilPolar.anvilToPolar(worldFolder.toPath(), ChunkSelector.radius(chunkRadius));
                System.out.println("Done! Saving to file: " + outputName + ".polar");
                saveWorldToFile(polarWorld, new File(outputName + ".polar"));
                System.out.println("Successfully converted world!");
                return;
            }

            System.out.println("Converting world...");
            PolarWorld polarWorld = AnvilPolar.anvilToPolar(worldFolder.toPath());
            System.out.println("Done! Saving to file: " + outputName + ".polar");
            saveWorldToFile(polarWorld, new File(outputName + ".polar"));
            System.out.println("Successfully converted world!");

            MinecraftServer.stopCleanly();
        }catch (IOException e) {
            MinecraftServer.stopCleanly();
            throw new RuntimeException(e);
        }
    }

    private static boolean validateWorldFile(File worldFolder) {
        if (!worldFolder.exists()) {
            System.err.println("World folder does not exist: " + worldFolder.getName());
            return false;
        }

        if (!worldFolder.isDirectory()) {
            System.err.println("World folder is not a directory: " + worldFolder.getName());
            return false;
        }

        Path path = worldFolder.toPath();
        Path levelPath = worldFolder.toPath().resolve("level.dat");

        if (!levelPath.toFile().exists()) {
            System.err.println("World folder does not contain a level.dat file: " + levelPath.getFileName());
            System.err.println("Will be assumed to be an unsupported version and cannot be converted.");
            return false;
        }

        try (InputStream is = Files.newInputStream(levelPath)) {
            final CompoundBinaryTag tag = BinaryTagIO.reader().readNamed(is, BinaryTagIO.Compression.GZIP).getValue();
            Files.copy(levelPath, path.resolve("level.dat_old"), StandardCopyOption.REPLACE_EXISTING);
            final CompoundBinaryTag data = tag.getCompound("Data");

            if(data.getInt("DataVersion") != MinecraftServer.DATA_VERSION) {
                final CompoundBinaryTag versionTag = data.getCompound("Version");
                String version = versionTag.getString("Name", "Unknown");

                System.err.printf("World version %s is not supported. Please update the world to %s before converting.%n", version, MinecraftServer.VERSION_NAME);
                System.exit(1);
                return false;
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        File regionsFolder = new File(worldFolder, "region");

        if (!regionsFolder.exists()) {
            System.err.println("World folder does not contain a region folder: " + regionsFolder.getName());
            return false;
        }

        return true;
    }

    private static void saveWorldToFile(PolarWorld world, File outputFile) {
        byte[] worldBytes = PolarWriter.write(world);

        try {
            Files.write(outputFile.toPath(), worldBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendHelp() {
        System.out.println(
                """
                        Valid Arguments:
                        --world "world folder" - The world folder to convert
                        --output "world" - The output file name for the polar world
                        --radius 5 - The chunk radius to convert, from 0,0
                        """
        );
    }

    private static void startMinestom() {
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Offline());

        minecraftServer.start("0.0.0.0", 0);
    }

}
