package com.restonic4.versatilesanity.util;

import com.restonic4.versatilesanity.VersatileSanity;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Predicate;

public class Utils {
    public static final Random random = new Random();

    public static int getLightLevel(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // Obtener la luz de bloques (antorchas, lámparas, etc.)
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);

        // Obtener la luz del cielo sin modificar
        int rawSkyLight = level.getBrightness(LightLayer.SKY, pos);

        // Si no hay acceso al cielo, no hay luz del cielo
        if (rawSkyLight <= 0 || level.isNight()) {
            return blockLight;
        }

        return Math.max(blockLight, rawSkyLight);
    }

    public static boolean isPlayerNearEntity(ServerPlayer player, double radius, double ignoreVisionRadius, Predicate<Entity> filter) {
        Level level = player.level();

        // Radio externo - todos los hostiles en este rango (con comprobación de visión)
        AABB boxOuter = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        // Obtener todas las entidades en el radio máximo
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boxOuter, e -> true);

        // Posición de los ojos del jugador para el cálculo de visión
        Vec3 eyePos = player.getEyePosition();

        for (Entity entity : entities) {
            if (!entity.equals(player) && filter.test(entity)) {
                // Calcular la distancia al cuadrado (más eficiente que raíz cuadrada)
                double distanceSq = entity.distanceToSqr(player);

                // Si está dentro del radio interno, detectarlo siempre
                if (distanceSq <= ignoreVisionRadius * ignoreVisionRadius) {
                    return true;
                }

                // Para entidades en el radio externo, comprobar visión
                // Calculamos el punto central de la entidad para raytracing
                Vec3 entityCenter = entity.getBoundingBox().getCenter();

                // Crear un rayo desde los ojos del jugador hasta el centro de la entidad
                ClipContext clipContext = new ClipContext(
                        eyePos,
                        entityCenter,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                );

                // Realizar el raytracing
                BlockHitResult hitResult = level.clip(clipContext);

                // Si el rayo llega hasta la entidad sin chocar con bloques, está en visión
                if (hitResult.getType() == HitResult.Type.MISS ||
                        hitResult.getLocation().distanceToSqr(entityCenter) < 0.25) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isPlayerNearHostileEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isHostile);
    }

    private static boolean isHostile(Entity entity) {
        if (entity instanceof Enemy) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearPlayerOrVillagerEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isPlayerOrVillager);
    }

    private static boolean isPlayerOrVillager(Entity entity) {
        if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearBossEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isBoss);
    }

    private static boolean isBoss(Entity entity) {
        if (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof Warden) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearPetEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isPet);
    }

    private static boolean isPet(Entity entity) {
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame();
        }

        else if (entity instanceof AbstractHorse horse) {
            return horse.isTamed();
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerInOrNearVillage(ServerPlayer player, double radius) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Check if the player is on a village
        boolean isInVillage = serverLevel.structureManager().getStructureWithPieceAt(
                playerPos,
                StructureTags.VILLAGE
        ).isValid();

        if (isInVillage) {
            return true;
        }

        // Check if the player is near a village
        if (radius > 0) {
            int searchRadius = (int) Math.ceil(radius);

            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    if (x*x + z*z > radius*radius) {
                        continue;
                    }

                    BlockPos checkPos = playerPos.offset(x, 0, z);

                    if (serverLevel.structureManager().getStructureWithPieceAt(
                            checkPos,
                            StructureTags.VILLAGE
                    ).isValid()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isPlayerNearPortal(ServerPlayer player, double radius) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Radio de búsqueda (redondeado hacia arriba)
        int searchRadius = (int) Math.ceil(radius);

        // Comparamos el cuadrado de las distancias para eficiencia
        double radiusSq = radius * radius;

        // Buscar en un área cúbica alrededor del jugador
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-searchRadius, -searchRadius, -searchRadius),
                playerPos.offset(searchRadius, searchRadius, searchRadius))) {

            // Verificar distancia euclidiana
            double distSq = pos.distSqr(playerPos);
            if (distSq > radiusSq) {
                continue;
            }

            Block block = level.getBlockState(pos).getBlock();
            boolean isPortal = isPortal(block);

            if (isPortal) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPortal(Block block) {
        // Verificar portales vanilla
        if (block instanceof NetherPortalBlock ||
                block instanceof EndPortalBlock ||
                block instanceof EndGatewayBlock) {
            return true;
        }

        // Compatibilidad con portales de mods
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (blockId.contains("portal") ||
                block.getClass().getName().toLowerCase().contains("portal")) {
            return true;
        }

        return false;
    }

    public static float calculateHungerValue(Player player) {
        float threshold = VersatileSanity.getConfig().getSatietyThreshold();

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        float currentHungerPercentage = foodLevel / 20.0f;

        if (currentHungerPercentage < threshold) {
            return 0.0f;
        }

        return (currentHungerPercentage - threshold) / (1.0f - threshold);
    }

    public static boolean isOnLootTable(ItemStack stack, ResourceLocation lootTableId, MinecraftServer server) {
        LootTable lootTable = server.getLootData().getLootTable(lootTableId);

        for (LootPool pool : lootTable.pools) {
            for (LootPoolEntryContainer entry : pool.entries) {
                if (entry instanceof LootItem lootItem) {
                    if (lootItem.item == stack.getItem()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasParrotsOnShoulders(Player player) {
        CompoundTag leftShoulder = player.getShoulderEntityLeft();
        CompoundTag rightShoulder = player.getShoulderEntityRight();
        return isParrotTag(leftShoulder) || isParrotTag(rightShoulder);
    }

    private static boolean isParrotTag(CompoundTag tag) {
        return tag != null && !tag.isEmpty() && "minecraft:parrot".equals(tag.getString("id"));
    }

    public static boolean[] isNearPlayingJukebox(Player player, int radius) {
        BlockPos playerPos = player.blockPosition();

        boolean[] result = new boolean[] { false, false };

        BlockPos.betweenClosedStream(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius)
        ).forEach(pos -> {
            BlockState state = player.level().getBlockState(pos);

            if (state.getBlock() == Blocks.JUKEBOX) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);

                if (blockEntity instanceof JukeboxBlockEntity jukebox && jukebox.isRecordPlaying()) {
                    ItemStack discStack = jukebox.getFirstItem();

                    if (!discStack.isEmpty() && discStack.getItem() instanceof RecordItem recordItem) {
                        ResourceLocation discId = BuiltInRegistries.ITEM.getKey(recordItem);

                        if (isMusicDiscBadForSanity(discId.toString())) {
                            result[0] = true;
                            result[1] = true;
                        } else {
                            result[0] = true;
                            result[1] = false;
                        }
                    }
                }
            }
        });

        return result;
    }

    public static boolean isMusicDiscBadForSanity(String discId) {
        if ("minecraft:music_disc_13".equals(discId.toString())) {
            return true;
        } else if("minecraft:music_disc_11".equals(discId.toString())) {
            return true;
        } else if("minecraft:music_disc_5".equals(discId.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isInCobweb(Player player) {
        Level world = player.level();
        AABB boundingBox = player.getBoundingBox();

        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                BlockPos.containing(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ))
        ) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof WebBlock) {
                return true;
            }
        }
        return false;
    }

    // Entity.deltaMovement sucks so bad, it's so broken, it does not update in weird case escenarios.
    public static boolean isPlayerMoving(Player player) {
        return ((MovingTracker) player).versatileSanity$isMoving();
    }

    public static void shuffleInventory(Player player, int slotsToShuffle) {
        Inventory inventory = player.getInventory();

        // Generar lista de slots disponibles (9-35)
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            availableSlots.add(i);
        }

        // Mezclar y seleccionar slots aleatorios únicos
        Collections.shuffle(availableSlots);
        int targetSlots = org.joml.Math.clamp(slotsToShuffle, 1, 27);
        List<Integer> selectedSlots = availableSlots.subList(0, targetSlots);

        // Obtener items de los slots seleccionados
        List<ItemStack> selectedItems = new ArrayList<>();
        for (int slot : selectedSlots) {
            selectedItems.add(inventory.getItem(slot));
        }

        // Mezclar los items entre sí
        Collections.shuffle(selectedItems);

        // Reasignar los items a los mismos slots (en nuevo orden)
        for (int i = 0; i < selectedSlots.size(); i++) {
            inventory.setItem(selectedSlots.get(i), selectedItems.get(i));
        }

        inventory.setChanged();
    }

    public static ItemStack createSignedBook(String title, String author, String content) {
        // Crear un nuevo libro
        ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag nbt = new CompoundTag();

        // Establecer el autor y título
        nbt.putString("author", author);
        nbt.putString("title", title);

        // Dividir el contenido en páginas
        ListTag pages = new ListTag();
        String[] words = content.split(" ");
        StringBuilder currentPage = new StringBuilder();
        int charsInPage = 0;

        // Minecraft tiene un límite aproximado de 256 caracteres por página
        // (aunque también depende de la anchura de los caracteres)
        final int MAX_CHARS_PER_PAGE = 240;

        for (String word : words) {
            // Verificar si añadir la palabra excedería el límite
            if (charsInPage + word.length() + 1 > MAX_CHARS_PER_PAGE) {
                // Añadir la página actual y empezar una nueva
                pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(currentPage.toString()))));
                currentPage = new StringBuilder();
                charsInPage = 0;
            }

            // Añadir la palabra a la página actual
            if (charsInPage > 0) {
                currentPage.append(" ");
                charsInPage++;
            }
            currentPage.append(word);
            charsInPage += word.length();
        }

        // Añadir la última página si tiene contenido
        if (!currentPage.isEmpty()) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(currentPage.toString()))));
        }

        // Añadir las páginas al libro
        nbt.put("pages", pages);

        // Marcar el libro como resuelto (necesario para libros firmados)
        nbt.putBoolean("resolved", true);

        // Establecer el NBT al libro
        bookStack.setTag(nbt);

        return bookStack;
    }

    public static ItemEntity spawnSignedBookAt(ServerLevel level, BlockPos pos, String title, String author, String content) {
        // Crear el libro usando la función anterior
        ItemStack bookStack = createSignedBook(title, author, content);

        // Convertir BlockPos a coordenadas Vec3d (añadiendo 0.5 para centrar en el bloque)
        Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // Crear una entidad de item con el libro
        ItemEntity itemEntity = new ItemEntity(
                level,
                position.x(),
                position.y(),
                position.z(),
                bookStack,
                0.0, // velocidad en X
                0.1, // velocidad en Y (un pequeño rebote)
                0.0  // velocidad en Z
        );

        // Establecer un tiempo de despawn normal (6000 ticks = 5 minutos)
        itemEntity.setPickUpDelay(10); // pequeño retraso para recogerlo (10 ticks = 0.5 segundos)
        itemEntity.setUnlimitedLifetime();

        // Spawnear la entidad en el mundo
        level.addFreshEntity(itemEntity);

        return itemEntity;
    }


    private static final Set<Block> BLACKLIST = Set.of(
            Blocks.BEDROCK,
            Blocks.OBSIDIAN,
            Blocks.NETHERRACK
    );

    // Determines if a block is unsuitable to support fire (replaceable blocks like plants)
    private static boolean shouldBlackList(BlockState state) {
        return state.canBeReplaced();
    }

    public static void placeRandomFire(Level level, BlockPos center, int radius) {
        FlammableBlockRegistry registry = FlammableBlockRegistry.getDefaultInstance();

        for (int i = 0; i < 100; i++) {
            // Generate random position within radius
            int offsetX = random.nextInt(radius * 2 + 1) - radius;
            int offsetY = random.nextInt(radius * 2 + 1) - radius;
            int offsetZ = random.nextInt(radius * 2 + 1) - radius;
            BlockPos candidatePos = center.offset(offsetX, offsetY, offsetZ);

            // Only place fire in air blocks
            if (!level.getBlockState(candidatePos).isAir()) continue;

            boolean canPlace = false;

            // Check for valid ground placement (block below)
            BlockPos belowPos = candidatePos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!BLACKLIST.contains(belowState.getBlock()) &&
                    registry.get(belowState.getBlock()).getBurnChance() > 0 &&
                    !shouldBlackList(belowState)) {
                canPlace = true;
            }
            // Check for valid wall placement (horizontal neighbors)
            else {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighborPos = candidatePos.relative(dir);
                    BlockState neighborState = level.getBlockState(neighborPos);

                    if (!BLACKLIST.contains(neighborState.getBlock()) &&
                            registry.get(neighborState.getBlock()).getBurnChance() > 0 &&
                            !shouldBlackList(neighborState)) {
                        canPlace = true;
                        break;
                    }
                }
            }

            if (canPlace) {
                // Let FireBlock handle proper rotation and attachment
                BlockState fireState = Blocks.FIRE.defaultBlockState();
                FireBlock fireBlock = (FireBlock) Blocks.FIRE;

                // Update fire state based on neighboring blocks
                fireState = fireBlock.getStateForPlacement(level, candidatePos);
                level.setBlock(candidatePos, fireState, Block.UPDATE_ALL);
                return;
            }
        }
    }
}
