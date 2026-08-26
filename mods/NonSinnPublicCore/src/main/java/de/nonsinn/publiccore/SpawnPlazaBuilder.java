package de.nonsinn.publiccore;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Procedural Master Builder for the 100x100 Hytale RPG Spawn on Bauwelt.
 * Covers coordinate footprint X: -18..82, Z: -18..82 (101x101 blocks, 16 chunks).
 */
public class SpawnPlazaBuilder {

    private final World world;
    private final NonSinnPublicCore plugin;
    private final Random random = new Random(424242);
    private int blocksPlaced = 0;

    public SpawnPlazaBuilder(World world, NonSinnPublicCore plugin) {
        this.world = world;
        this.plugin = plugin;
    }

    public static void build(World world, NonSinnPublicCore plugin) {
        if (world == null) {
            plugin.getLogger().atWarning().log("Bauwelt ist nicht verfuegbar fuer SpawnPlazaBuilder!");
            return;
        }

        // 1. Asynchronously preload all 16 chunks for the 100x100 area (cx: -1..2, cz: -1..2)
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int cx = -1; cx <= 2; cx++) {
            for (int cz = -1; cz <= 2; cz++) {
                long chunkId = ChunkUtil.indexChunk(cx, cz);
                futures.add(world.getChunkAsync(chunkId));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            world.execute(() -> {
                SpawnPlazaBuilder builder = new SpawnPlazaBuilder(world, plugin);
                builder.generateFullSpawn();
            });
        });
    }

    private void generateFullSpawn() {
        plugin.getLogger().atInfo().log("Starte Bau des 100x100 RPG-Spawns auf Bauwelt...");
        long startTime = System.currentTimeMillis();

        // 1. Base Terrain & Elevation Sculpting (100x100 from -18 to +82)
        buildTerrainAndRelief();

        // 2. Organic Master Road & Pathway Network
        buildPathways();

        // 3. Zone 1: The Arrival Court (Spawnpunkt at X: 32, Z: 18, Y: 64)
        buildArrivalCourt();

        // 4. Zone 2: Ancient Sacred World Tree & Cascading Spring Grotto (Offset at X: 54, Z: 48)
        buildSacredGroveAndCascade();

        // 5. Zone 3: Adventurer's Guild Pavilion & Terrace (X: 4..20, Z: 40..56)
        buildGuildPavilion();

        // 6. Zone 4: Old Market Courtyard & Stalls (X: 4..20, Z: 4..20)
        buildMarketSquare();

        // 7. Zone 5: Outer Wandering Gardens, Trees, Flower Groves & Lamp Posts
        buildGardensAndFoliage();

        long duration = System.currentTimeMillis() - startTime;
        plugin.getLogger().atInfo().log("100x100 RPG-Spawn erfolgreich gebaut: %d Bloecke in %d ms platziert.", blocksPlaced, duration);
    }

    // ==========================================
    // 1. BASE TERRAIN & ELEVATION SCULPTING
    // ==========================================
    private void buildTerrainAndRelief() {
        // Base grass foundation across the full 101x101 grid at Y=64, clearing up to Y=85
        for (int x = -18; x <= 82; x++) {
            for (int z = -18; z <= 82; z++) {
                // Ground surface
                setBlock(x, 64, z, "Soil_Grass");
                // Clear old structures / air above
                for (int y = 65; y <= 85; y++) {
                    setBlock(x, y, z, "Empty");
                }
            }
        }

        // Natural raised hillocks / berms around outer perimeter (Y=65..66)
        // North-East elevated garden ridge (X: 58..80, Z: 58..80)
        fillMound(68, 68, 12, 65, "Soil_Grass", "Rock_Stone_Cobble_Mossy");
        // South-East scenic lookout mound (X: 58..80, Z: -12..10)
        fillMound(68, 0, 10, 65, "Soil_Grass", "Rock_Stone_Cobble_Mossy");
        // North-West forested knoll (X: -12..10, Z: 60..80)
        fillMound(0, 68, 10, 65, "Soil_Grass", "Rock_Stone_Cobble_Mossy");
        // South-West orchard hillock (X: -15..0, Z: -15..0)
        fillMound(-8, -8, 8, 65, "Soil_Grass", "Rock_Stone_Cobble_Mossy");
    }

    private void fillMound(int cx, int cz, int radius, int topY, String coreBlock, String edgeBlock) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (x < -18 || x > 82 || z < -18 || z > 82) continue;
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist <= radius) {
                    if (dist > radius - 1.5) {
                        setBlock(x, topY, z, edgeBlock);
                    } else {
                        setBlock(x, topY, z, coreBlock);
                    }
                }
            }
        }
    }

    // ==========================================
    // 2. ORGANIC MASTER ROAD & PATHWAY NETWORK
    // ==========================================
    private void buildPathways() {
        // Main Arrival Promenade (South Gate -18 to Arrival Court Z: 18)
        buildOrganicRoad(32, -18, 32, 18, 5, "Rock_Stone_Brick", "Rock_Marble_Brick", "Rock_Stone_Brick_Smooth");

        // Central Walkway (Arrival Court Z: 18 to Central Crossroads Z: 35)
        buildOrganicRoad(32, 18, 32, 35, 5, "Rock_Stone_Brick", "Rock_Marble_Brick_Ornate", "Rock_Stone_Brick_Smooth");

        // North Avenue (Central Crossroads Z: 35 to North Gate Z: 82)
        buildOrganicRoad(32, 35, 32, 82, 5, "Rock_Stone_Brick", "Rock_Stone_Cobble", "Rock_Stone_Brick_Mossy");

        // East Sacred Grove Way (Central Crossroads X: 32, Z: 35 -> Sacred Tree X: 54, Z: 48 -> East Gate X: 82, Z: 32)
        buildOrganicCurve(32, 35, 54, 48, 4, "Soil_Pathway", "Rock_Stone_Cobble_Mossy", "Soil_Calcite_Gravel");
        buildOrganicCurve(54, 48, 82, 32, 4, "Soil_Pathway", "Rock_Stone_Cobble", "Soil_Gravel");

        // West Guild & Market Way (Central Crossroads X: 32, Z: 35 -> Guild Pavilion X: 14, Z: 48 -> West Gate X: -18, Z: 32)
        buildOrganicCurve(32, 35, 14, 48, 4, "Rock_Stone_Brick", "Wood_Hardwood_Planks", "Rock_Stone_Cobble");
        buildOrganicCurve(14, 48, -18, 32, 4, "Rock_Stone_Brick", "Rock_Stone_Cobble", "Soil_Pathway");

        // Market Connection Path (Arrival Court X: 32, Z: 18 -> Market Square X: 14, Z: 14)
        buildOrganicCurve(32, 18, 14, 14, 4, "Soil_Pathway", "Rock_Stone_Cobble", "Soil_Gravel");

        // Garden Promenade (Circling the whole central sanctuary connecting East Mound to North Forest)
        buildOrganicCurve(54, 48, 68, 68, 3, "Soil_Pathway", "Soil_Calcite_Gravel", "Rock_Stone_Cobble_Mossy");
        buildOrganicCurve(14, 48, 0, 68, 3, "Soil_Pathway", "Rock_Stone_Cobble", "Soil_Gravel");
    }

    private void buildOrganicRoad(int x1, int z1, int x2, int z2, int width, String primary, String accent, String secondary) {
        int half = width / 2;
        int minX = Math.min(x1, x2) - half;
        int maxX = Math.max(x1, x2) + half;
        int minZ = Math.min(z1, z2) - half;
        int maxZ = Math.max(z1, z2) + half;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x < -18 || x > 82 || z < -18 || z > 82) continue;
                int r = random.nextInt(10);
                String block = (r < 6) ? primary : (r < 8 ? secondary : accent);
                setBlock(x, 64, z, block);
            }
        }
    }

    private void buildOrganicCurve(int x1, int z1, int x2, int z2, int width, String p1, String p2, String p3) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1)) * 2;
        int half = width / 2;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int px = (int) Math.round(x1 + t * (x2 - x1));
            int pz = (int) Math.round(z1 + t * (z2 - z1));

            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    int x = px + dx;
                    int z = pz + dz;
                    if (x < -18 || x > 82 || z < -18 || z > 82) continue;
                    int r = random.nextInt(10);
                    String b = (r < 6) ? p1 : (r < 8 ? p2 : p3);
                    setBlock(x, 64, z, b);
                }
            }
        }
    }

    // ==========================================
    // 3. ZONE 1: THE ARRIVAL ESPLANADE (SPAWN)
    // ==========================================
    private void buildArrivalCourt() {
        int cx = 32;
        int cz = 18;
        int radius = 7;

        // Circular stone plaza with concentric noble marble rings (Y = 64)
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist <= radius) {
                    String block;
                    if (dist >= radius - 1.0) {
                        block = "Rock_Marble_Brick_Ornate";
                    } else if (dist >= radius - 2.5) {
                        block = "Rock_Marble_Brick";
                    } else if ((x + z) % 2 == 0) {
                        block = "Rock_Stone_Brick_Smooth";
                    } else {
                        block = "Rock_Stone_Brick";
                    }
                    setBlock(x, 64, z, block);
                }
            }
        }

        // 4 Monumental Waystone Pillars at the diagonal corners (completely clear of spawn at 32, 18)
        int[][] obelisks = {{26, 12}, {38, 12}, {26, 24}, {38, 24}};
        for (int[] pos : obelisks) {
            int ox = pos[0];
            int oz = pos[1];
            setBlock(ox, 65, oz, "Rock_Marble_Brick_Pillar_Base");
            setBlock(ox, 66, oz, "Rock_Marble_Brick");
            setBlock(ox, 67, oz, "Rock_Marble_Brick_Ornate");
            setBlock(ox, 68, oz, "Deco_Lantern");
        }

        // East & West Information Pergolas with quest / bulletin boards
        buildNoticePergola(24, 18, true);
        buildNoticePergola(40, 18, false);

        // Low decorative marble balustrades with planter boxes flanking the entrance
        for (int z = 14; z <= 22; z++) {
            if (z != 18) {
                setBlock(24, 65, z, "Rock_Marble_Brick_Wall");
                setBlock(40, 65, z, "Rock_Marble_Brick_Wall");
            }
        }
        setBlock(24, 65, 14, "Plant_Flower_Bushy_Blue");
        setBlock(24, 65, 22, "Plant_Flower_Bushy_Cyan");
        setBlock(40, 65, 14, "Plant_Flower_Bushy_Red");
        setBlock(40, 65, 22, "Plant_Flower_Bushy_Purple");
    }

    private void buildNoticePergola(int x, int z, boolean isWest) {
        setBlock(x, 65, z - 2, "Wood_Oak_Trunk");
        setBlock(x, 66, z - 2, "Wood_Oak_Trunk");
        setBlock(x, 67, z - 2, "Wood_Oak_Trunk");
        setBlock(x, 65, z + 2, "Wood_Oak_Trunk");
        setBlock(x, 66, z + 2, "Wood_Oak_Trunk");
        setBlock(x, 67, z + 2, "Wood_Oak_Trunk");

        // Timber crossbeam & roof
        for (int dz = -2; dz <= 2; dz++) {
            setBlock(x, 68, z + dz, "Wood_Hardwood_Planks");
            setBlock(x, 67, z + dz, "Wood_Hardwood_Planks_Half");
        }
        // Notice board wall
        for (int dz = -1; dz <= 1; dz++) {
            setBlock(x, 65, z + dz, "Wood_Darkwood_Planks");
            setBlock(x, 66, z + dz, "Wood_Darkwood_Planks");
        }
        // Hanging lantern
        setBlock(x, 67, z, "Deco_Lantern_Ceiling");
    }

    // ==========================================
    // 4. ZONE 2: ANCIENT WORLD TREE & CASCADING SPRING
    // ==========================================
    private void buildSacredGroveAndCascade() {
        int tx = 54;
        int tz = 48;

        // --- The Great World Tree ---
        // Massive 3x3 Oak Core rising from Y=64 to Y=77
        for (int y = 64; y <= 76; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setBlock(tx + dx, y, tz + dz, "Wood_Oak_Trunk");
                }
            }
        }
        // Roots buttresses at base
        setBlock(tx - 2, 64, tz, "Wood_Oak_Roots");
        setBlock(tx - 2, 65, tz, "Wood_Oak_Trunk");
        setBlock(tx + 2, 64, tz, "Wood_Oak_Roots");
        setBlock(tx + 2, 65, tz, "Wood_Oak_Trunk");
        setBlock(tx, 64, tz - 2, "Wood_Oak_Roots");
        setBlock(tx, 65, tz - 2, "Wood_Oak_Trunk");
        setBlock(tx, 64, tz + 2, "Wood_Oak_Roots");
        setBlock(tx, 65, tz + 2, "Wood_Oak_Trunk");

        // 4 Sprawling Main Limbs at Y=71..75
        buildBranch(tx, 72, tz, 6, 0, 3, "Wood_Oak_Trunk");   // East limb
        buildBranch(tx, 72, tz, -6, 0, 3, "Wood_Oak_Trunk");  // West limb
        buildBranch(tx, 73, tz, 0, 6, 3, "Wood_Oak_Trunk");   // North limb
        buildBranch(tx, 71, tz, 0, -6, 2, "Wood_Oak_Trunk");  // South limb

        // Massive Lush Leaf Canopy (Oak & Autumn Leaves)
        buildSphereCanopy(tx, 75, tz, 8, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");
        buildSphereCanopy(tx + 5, 73, tz, 5, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");
        buildSphereCanopy(tx - 5, 73, tz, 5, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");
        buildSphereCanopy(tx, 74, tz + 5, 5, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");
        buildSphereCanopy(tx, 72, tz - 5, 4, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");

        // Hanging lanterns suspended from branches
        setBlock(tx + 4, 70, tz, "Deco_Lantern_Ceiling");
        setBlock(tx - 4, 70, tz, "Deco_Lantern_Ceiling");
        setBlock(tx, 71, tz + 4, "Deco_Lantern_Ceiling");
        setBlock(tx, 69, tz - 4, "Deco_Lantern_Ceiling");

        // --- Sunken Cascading Spring & Grotto (West of the tree: X: 42..50, Z: 36..44) ---
        // Raised natural spring outcrop at (48, 38)
        for (int x = 46; x <= 50; x++) {
            for (int z = 36; z <= 40; z++) {
                setBlock(x, 65, z, "Rock_Stone_Cobble_Mossy");
                setBlock(x, 66, z, "Rock_Stone_Cobble_Mossy");
            }
        }
        setBlock(48, 67, 38, "Rock_Marble_Brick_Ornate");
        setBlock(48, 67, 39, "Fluid_Water"); // Spring Source

        // Cascading Water Flow down steps to sunken pond
        setBlock(47, 66, 39, "Fluid_Water");
        setBlock(46, 65, 39, "Fluid_Water");

        // Sunken Pond (X: 42..47, Z: 37..43 at Y=63)
        for (int x = 41; x <= 47; x++) {
            for (int z = 36; z <= 44; z++) {
                double d = Math.sqrt((x - 44) * (x - 44) + (z - 40) * (z - 40));
                if (d <= 3.5) {
                    setBlock(x, 63, z, "Soil_Sand_White");
                    setBlock(x, 64, z, "Fluid_Water");
                } else if (d <= 4.5) {
                    setBlock(x, 64, z, (random.nextBoolean() ? "Rock_Stone_Cobble_Mossy" : "Rock_Stone_Brick_Mossy"));
                    setBlock(x, 65, z, "Empty");
                }
            }
        }
        // Lily pads and water blooms
        setBlock(43, 65, 41, "Plant_Flower_Water_Blue");
        setBlock(45, 65, 39, "Plant_Flower_Water_Green");

        // Stepping stones across the pond
        setBlock(44, 64, 38, "Rock_Marble_Brick_Half");
        setBlock(44, 64, 40, "Rock_Marble_Brick_Half");
        setBlock(44, 64, 42, "Rock_Marble_Brick_Half");

        // Resting Benches overlooking the pond
        buildBench(41, 65, 36, true);
        buildBench(48, 65, 43, false);
    }

    private void buildBranch(int startX, int startY, int startZ, int dx, int dz, int dy, String block) {
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int x = startX + (int) (t * dx);
            int z = startZ + (int) (t * dz);
            int y = startY + (int) (t * dy);
            setBlock(x, y, z, block);
            setBlock(x, y - 1, z, block);
        }
    }

    private void buildSphereCanopy(int cx, int cy, int cz, int r, String leaf1, String leaf2) {
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                for (int z = cz - r; z <= cz + r; z++) {
                    double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy) * 1.4 + (z - cz) * (z - cz));
                    if (dist <= r && random.nextDouble() > 0.15) {
                        String b = (random.nextDouble() > 0.3) ? leaf1 : leaf2;
                        setBlockIfAir(x, y, z, b);
                    }
                }
            }
        }
    }

    // ==========================================
    // 5. ZONE 3: ADVENTURER'S GUILD PAVILION
    // ==========================================
    private void buildGuildPavilion() {
        int minX = 4;
        int maxX = 22;
        int minZ = 40;
        int maxZ = 58;

        // Raised Stone Podium (Y = 65)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                setBlock(x, 64, z, "Rock_Stone_Brick");
                // Interior hardwood floor
                if (x >= minX + 2 && x <= maxX - 2 && z >= minZ + 2 && z <= maxZ - 2) {
                    setBlock(x, 65, z, ((x + z) % 2 == 0) ? "Wood_Hardwood_Planks" : "Wood_Darkwood_Planks");
                } else {
                    setBlock(x, 65, z, "Rock_Marble_Brick");
                }
            }
        }

        // Approach Steps on South & East sides
        for (int x = minX + 5; x <= maxX - 5; x++) {
            setBlock(x, 64, minZ - 1, "Rock_Stone_Brick_Stairs");
        }
        for (int z = minZ + 5; z <= maxZ - 5; z++) {
            setBlock(maxX + 1, 64, z, "Rock_Stone_Brick_Stairs");
        }

        // 6 Monumental Support Columns (X: 6, 13, 20; Z: 42, 56)
        int[] colX = {6, 13, 20, 6, 13, 20};
        int[] colZ = {42, 42, 42, 56, 56, 56};
        for (int i = 0; i < 6; i++) {
            int cx = colX[i];
            int cz = colZ[i];
            setBlock(cx, 66, cz, "Rock_Marble_Brick_Pillar_Base");
            for (int y = 67; y <= 70; y++) {
                setBlock(cx, y, cz, "Wood_Oak_Trunk");
            }
            setBlock(cx, 71, cz, "Wood_Hardwood_Beam");
        }

        // Timber Header Beams connecting all columns (Y = 71)
        for (int x = 6; x <= 20; x++) {
            setBlock(x, 71, 42, "Wood_Hardwood_Beam");
            setBlock(x, 71, 56, "Wood_Hardwood_Beam");
        }
        for (int z = 42; z <= 56; z++) {
            setBlock(6, 71, z, "Wood_Hardwood_Beam");
            setBlock(13, 71, z, "Wood_Hardwood_Beam");
            setBlock(20, 71, z, "Wood_Hardwood_Beam");
        }

        // Grand Gabled Hip Roof (Y = 72..76)
        int roofMinX = minX + 1;
        int roofMaxX = maxX - 1;
        int roofMinZ = minZ + 1;
        int roofMaxZ = maxZ - 1;

        for (int layer = 0; layer <= 4; layer++) {
            int y = 72 + layer;
            int x1 = roofMinX + layer;
            int x2 = roofMaxX - layer;
            int z1 = roofMinZ + layer;
            int z2 = roofMaxZ - layer;

            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    boolean isEdge = (x == x1 || x == x2 || z == z1 || z == z2);
                    if (isEdge) {
                        setBlock(x, y, z, "Wood_Hardwood_Roof");
                    } else if (layer == 4) {
                        setBlock(x, y, z, "Wood_Darkwood_Roof_Flat");
                    }
                }
            }
        }

        // Interior Furnishings & Lore Details (Fully Accessible)
        // Central Expedition Table
        setBlock(13, 66, 49, "Furniture_Desert_Table");
        setBlock(14, 66, 49, "Furniture_Desert_Table");
        setBlock(13, 66, 48, "Furniture_Ancient_Chair");
        setBlock(14, 66, 48, "Furniture_Ancient_Chair");
        setBlock(13, 66, 50, "Furniture_Ancient_Chair");
        setBlock(14, 66, 50, "Furniture_Ancient_Chair");
        setBlock(13, 67, 49, "Deco_Book_Pile_Large");

        // West Wall: Lore Library Bookshelves & Chests
        setBlock(6, 66, 46, "Furniture_Bookshelf_Single");
        setBlock(6, 66, 47, "Furniture_Bookshelf_Single");
        setBlock(6, 66, 51, "Furniture_Bookshelf_Single");
        setBlock(6, 66, 52, "Furniture_Bookshelf_Single");
        setBlock(6, 67, 46, "Furniture_Bookshelf_Single");
        setBlock(6, 67, 47, "Furniture_Bookshelf_Single");

        // Rustic Supply Crates & Ale Barrels in corners
        setBlock(19, 66, 54, "Furniture_Tavern_Barrel");
        setBlock(19, 66, 55, "Furniture_Tavern_Barrel");
        setBlock(18, 66, 55, "Furniture_Village_Crate");
        setBlock(18, 67, 55, "Furniture_Village_Crate");

        // Hanging Chandelier from roof center
        setBlock(13, 73, 49, "Deco_Lantern_Ceiling");
        setBlock(14, 73, 49, "Deco_Lantern_Ceiling");
    }

    // ==========================================
    // 6. ZONE 4: OLD MARKET COURTYARD & STALLS
    // ==========================================
    private void buildMarketSquare() {
        // Market Square Base (X: 4..22, Z: 4..22)
        for (int x = 4; x <= 22; x++) {
            for (int z = 4; z <= 22; z++) {
                int r = random.nextInt(10);
                String b = (r < 5) ? "Soil_Pathway" : (r < 8 ? "Rock_Stone_Cobble" : "Soil_Gravel");
                setBlock(x, 64, z, b);
            }
        }

        // --- Stall A: The Herbalist & Alchemist (X: 6..10, Z: 15..19) ---
        buildMarketStall(6, 15, "Wood_Hardwood_Planks", "Wood_Hardwood_Planks_Half", "Plant_Flower_Bushy_Cyan");

        // --- Stall B: The Armorer & Blacksmith (X: 15..19, Z: 15..19) ---
        buildMarketStall(15, 15, "Rock_Stone_Brick", "Wood_Hardwood_Planks_Half", "Furniture_Village_Crate");

        // --- Stall C: Traveling Merchant Covered Wagon (X: 7..13, Z: 6..10) ---
        buildMerchantWagon(7, 6);

        // --- Public Stone Water Well (X: 18, Z: 6) ---
        buildStoneWell(18, 6);
    }

    private void buildMarketStall(int minX, int minZ, String counterBlock, String canopyBlock, String propBlock) {
        // 4 Timber corner posts
        for (int y = 65; y <= 67; y++) {
            setBlock(minX, y, minZ, "Wood_Oak_Trunk");
            setBlock(minX + 4, y, minZ, "Wood_Oak_Trunk");
            setBlock(minX, y, minZ + 4, "Wood_Oak_Trunk");
            setBlock(minX + 4, y, minZ + 4, "Wood_Oak_Trunk");
        }

        // Counter Tables
        for (int x = minX + 1; x <= minX + 3; x++) {
            setBlock(x, 65, minZ + 1, counterBlock);
        }
        for (int z = minZ + 2; z <= minZ + 3; z++) {
            setBlock(minX + 1, 65, z, counterBlock);
        }

        // Awning / Canopy Roof (Y = 68)
        for (int x = minX; x <= minX + 4; x++) {
            for (int z = minZ; z <= minZ + 4; z++) {
                setBlock(x, 68, z, "Wood_Hardwood_Planks_Half");
            }
        }
        // Goods & Lighting
        setBlock(minX + 2, 66, minZ + 1, propBlock);
        setBlock(minX + 2, 67, minZ + 2, "Deco_Lantern_Ceiling");
    }

    private void buildMerchantWagon(int minX, int minZ) {
        // Wagon base & oak log wheels
        setBlock(minX, 64, minZ, "Wood_Oak_Trunk");
        setBlock(minX + 5, 64, minZ, "Wood_Oak_Trunk");
        setBlock(minX, 64, minZ + 3, "Wood_Oak_Trunk");
        setBlock(minX + 5, 64, minZ + 3, "Wood_Oak_Trunk");

        // Wagon bed
        for (int x = minX + 1; x <= minX + 4; x++) {
            for (int z = minZ; z <= minZ + 3; z++) {
                setBlock(x, 65, z, "Wood_Hardwood_Planks");
            }
        }
        // Wagon cargo
        setBlock(minX + 2, 66, minZ + 1, "Furniture_Village_Crate");
        setBlock(minX + 3, 66, minZ + 1, "Furniture_Tavern_Barrel");
        setBlock(minX + 2, 66, minZ + 2, "Furniture_Tavern_Barrel");

        // Canvas hoop roof
        for (int x = minX + 1; x <= minX + 4; x++) {
            setBlock(x, 67, minZ, "Wood_Hardwood_Roof_Shallow");
            setBlock(x, 67, minZ + 3, "Wood_Hardwood_Roof_Shallow");
            setBlock(x, 68, minZ + 1, "Wood_Hardwood_Planks_Half");
            setBlock(x, 68, minZ + 2, "Wood_Hardwood_Planks_Half");
        }
    }

    private void buildStoneWell(int wx, int wz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    setBlock(wx, 63, wz, "Fluid_Water");
                    setBlock(wx, 64, wz, "Fluid_Water");
                } else {
                    setBlock(wx + dx, 65, wz + dz, "Rock_Stone_Brick");
                }
            }
        }
        // Well Roof & Posts
        setBlock(wx - 1, 66, wz - 1, "Wood_Hardwood_Fence");
        setBlock(wx - 1, 67, wz - 1, "Wood_Hardwood_Fence");
        setBlock(wx + 1, 66, wz + 1, "Wood_Hardwood_Fence");
        setBlock(wx + 1, 67, wz + 1, "Wood_Hardwood_Fence");
        setBlock(wx, 68, wz, "Wood_Hardwood_Roof");
        setBlock(wx, 67, wz, "Deco_Lantern_Ceiling");
    }

    // ==========================================
    // 7. ZONE 5: OUTER WANDERING GARDENS & LIGHTING
    // ==========================================
    private void buildGardensAndFoliage() {
        // Natural Hand-Crafted Trees across the 100x100 perimeter
        int[][] trees = {
            {-8, 12, 7, 3},   // West garden
            {-10, 50, 8, 4},  // North-West grove
            {8, 72, 9, 4},    // North park
            {52, 72, 8, 4},   // North-East forest edge
            {72, 54, 9, 4},   // East hillside
            {70, 18, 8, 4},   // East orchard
            {55, -8, 7, 3},   // South-East grove
            {-6, -6, 6, 3}    // South-West orchard
        };

        for (int[] t : trees) {
            buildHandcraftedTree(t[0], 65, t[1], t[2], t[3]);
        }

        // Rich Flower Beds in natural groupings
        buildFlowerPatch(26, 36, 4, "Plant_Flower_Bushy_Blue", "Plant_Bush_Green");
        buildFlowerPatch(38, 36, 4, "Plant_Flower_Bushy_Cyan", "Plant_Bush_Lush");
        buildFlowerPatch(60, 36, 5, "Plant_Flower_Bushy_Red", "Plant_Bush_Green");
        buildFlowerPatch(62, 60, 5, "Plant_Flower_Bushy_Purple", "Plant_Bush_Lush");
        buildFlowerPatch(20, 68, 6, "Plant_Flower_Bushy_White", "Plant_Bush_Green");
        buildFlowerPatch(-5, 30, 4, "Plant_Flower_Bushy_Yellow", "Plant_Bush_Green");
        buildFlowerPatch(50, 10, 5, "Plant_Flower_Bushy_Blue", "Plant_Bush_Lush");

        // Atmospheric Street Lamps along all pathways
        int[][] lamps = {
            {30, 0}, {34, 0},     // South Entrance
            {30, 28}, {34, 28},   // Spawn Plaza North
            {30, 45}, {34, 45},   // Central North Way
            {30, 65}, {34, 65},   // North Gate
            {45, 35}, {65, 40},   // East Sacred Grove
            {25, 38}, {8, 42},    // West Guild Pavilion
            {20, 16}, {12, 8},    // Market Square
            {68, 60}, {-5, 45}    // Outer Garden Trails
        };

        for (int[] pos : lamps) {
            buildLampPost(pos[0], 64, pos[1]);
        }
    }

    private void buildHandcraftedTree(int x, int baseY, int z, int height, int canopyRadius) {
        if (x < -16 || x > 80 || z < -16 || z > 80) return;

        // Trunk
        for (int y = baseY; y <= baseY + height; y++) {
            setBlock(x, y, z, "Wood_Oak_Trunk");
        }
        // Roots
        setBlock(x + 1, baseY, z, "Wood_Oak_Roots");
        setBlock(x - 1, baseY, z, "Wood_Oak_Roots");
        setBlock(x, baseY, z + 1, "Wood_Oak_Roots");
        setBlock(x, baseY, z - 1, "Wood_Oak_Roots");

        // Spherical canopy
        buildSphereCanopy(x, baseY + height, z, canopyRadius, "Plant_Leaves_Oak", "Plant_Leaves_Autumn");
    }

    private void buildFlowerPatch(int cx, int cz, int radius, String flowerBlock, String bushBlock) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (x < -18 || x > 82 || z < -18 || z > 82) continue;
                double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (d <= radius && random.nextDouble() > 0.3) {
                    String b = (random.nextDouble() > 0.3) ? flowerBlock : bushBlock;
                    setBlockIfAir(x, 65, z, b);
                }
            }
        }
    }

    private void buildLampPost(int x, int baseY, int z) {
        if (x < -18 || x > 82 || z < -18 || z > 82) return;
        setBlock(x, baseY + 1, z, "Rock_Stone_Brick_Wall");
        setBlock(x, baseY + 2, z, "Wood_Hardwood_Fence");
        setBlock(x, baseY + 3, z, "Wood_Hardwood_Fence");
        setBlock(x, baseY + 4, z, "Deco_Lantern");
    }

    private void buildBench(int x, int y, int z, boolean facingX) {
        if (facingX) {
            setBlock(x, y, z, "Wood_Hardwood_Stairs");
            setBlock(x + 1, y, z, "Wood_Hardwood_Stairs");
        } else {
            setBlock(x, y, z, "Wood_Hardwood_Stairs");
            setBlock(x, y, z + 1, "Wood_Hardwood_Stairs");
        }
    }

    // ==========================================
    // BLOCK PLACEMENT HELPERS
    // ==========================================
    private void setBlock(int x, int y, int z, String blockName) {
        try {
            long chunkId = ChunkUtil.indexChunkFromBlock(x, z);
            WorldChunk chunk = world.getChunkIfLoaded(chunkId);
            if (chunk != null) {
                chunk.setBlock(x, y, z, blockName);
                blocksPlaced++;
            }
        } catch (Exception exception) {
            // Ignore minor placement warnings
        }
    }

    private void setBlockIfAir(int x, int y, int z, String blockName) {
        try {
            long chunkId = ChunkUtil.indexChunkFromBlock(x, z);
            WorldChunk chunk = world.getChunkIfLoaded(chunkId);
            if (chunk != null) {
                chunk.setBlock(x, y, z, blockName);
                blocksPlaced++;
            }
        } catch (Exception exception) {
            // Ignore minor placement warnings
        }
    }
}
