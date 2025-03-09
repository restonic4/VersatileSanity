package com.restonic4.versatilesanity.components;

import com.restonic4.versatilesanity.util.Utils;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;

public class HomeDetectionComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent {
    public static final int DETECTION_RANGE = 64; // Radio de detección en bloques
    private static final int HOME_SCORE_THRESHOLD = 100; // Puntuación mínima para considerar una ubicación como casa
    private static final int CONTAINER_SCORE = 10; // Puntuación por cada contenedor cercano
    private static final int FURNACE_SCORE = 15; // Puntuación por cada horno
    private static final int CRAFTING_TABLE_SCORE = 20; // Puntuación por mesa de crafteo
    private static final int DECORATION_SCORE = 5; // Puntuación por bloque decorativo

    private static final double VILLAGE_DETECTION_RADIUS = 64.0; // Radio para detectar si se está en una villa
    private static final int VILLAGE_PENALTY = 300;

    private final Player player;
    private final Map<BlockPos, HomeLocation> potentialHomes = new HashMap<>();
    private BlockPos primaryHome = null;
    private int tickCounter = 0;

    public HomeDetectionComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        tickCounter++;

        // Actualizamos cada 5 minutos (6000 ticks) o cuando el jugador duerme
        if (tickCounter >= 6000) {
            evaluateCurrentLocation(serverPlayer);
            tickCounter = 0;
        }

        // Si el jugador está durmiendo, registramos la ubicación de la cama
        if (serverPlayer.isSleeping()) {
            recordBedUsage(serverPlayer);
        }
    }

    /**
     * Registra el uso de una cama y actualiza las estadísticas
     */
    private void recordBedUsage(ServerPlayer player) {
        BlockPos bedPos = player.getSleepingPos().orElse(null);
        if (bedPos == null) return;

        // Necesitamos encontrar la posición de la cabeza de la cama
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(bedPos);

        if (!(state.getBlock() instanceof BedBlock)) return;

        // Asegúrate de que estamos trabajando con la posición de la cabeza de la cama
        if (state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT) {
            bedPos = bedPos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        // Recuperar o crear la ubicación de casa
        HomeLocation homeLocation = potentialHomes.computeIfAbsent(bedPos, pos -> new HomeLocation(pos, level.dimension().location().toString()));

        // Incrementar el tiempo dormido
        homeLocation.incrementSleepTime();

        // Reevaluar la puntuación de la casa
        evaluateHomeScore(homeLocation, level);

        // Actualizar la casa principal si es necesario
        updatePrimaryHome();
    }

    /**
     * Evalúa la ubicación actual del jugador para determinar si podría ser una casa
     */
    private void evaluateCurrentLocation(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();

        // Buscar camas cercanas
        BlockPos nearestBedPos = findNearestBed(playerPos, level);
        if (nearestBedPos == null) return;

        // Si hay una cama cerca, evaluar la ubicación como posible casa
        HomeLocation homeLocation = potentialHomes.computeIfAbsent(nearestBedPos, pos -> new HomeLocation(pos, level.dimension().location().toString()));

        // Registrar actividad en esta ubicación
        homeLocation.recordActivity();

        // Evaluar puntuación de la casa
        evaluateHomeScore(homeLocation, level);

        // Actualizar la casa principal si es necesario
        updatePrimaryHome();
    }

    /**
     * Busca la cama más cercana en un radio determinado
     */
    private BlockPos findNearestBed(BlockPos center, Level level) {
        BlockPos nearestBed = null;
        double nearestDistance = Double.MAX_VALUE;

        int range = DETECTION_RANGE;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range/2; y <= range/2; y++) {
                for (int z = -range; z <= range; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(mutable);

                    if (state.getBlock() instanceof BedBlock) {
                        // Asegúrate de que estamos trabajando con la posición de la cabeza de la cama
                        if (state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT) {
                            mutable.move(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                            state = level.getBlockState(mutable);
                        }

                        double distance = center.distSqr(mutable);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestBed = mutable.immutable();
                        }
                    }
                }
            }
        }

        return nearestBed;
    }

    /**
     * Evalúa la puntuación de una ubicación como casa
     */
    private void evaluateHomeScore(HomeLocation homeLocation, ServerLevel level) {
        BlockPos pos = homeLocation.getPosition();
        int score = homeLocation.getBaseScore();

        // Contar estructuras cercanas que indican una casa
        int containerCount = countBlocksInRadius(pos, level, 16, block ->
                block.contains("chest") ||
                        block.contains("barrel") ||
                        block.contains("shulker")
        );

        int furnaceCount = countBlocksInRadius(pos, level, 16, block ->
                block.contains("furnace") ||
                        block.contains("smoker") ||
                        block.contains("blast")
        );

        int craftingCount = countBlocksInRadius(pos, level, 16, block ->
                block.contains("crafting_table")
        );

        int decorationCount = countBlocksInRadius(pos, level, 16, block ->
                block.contains("flower") ||
                        block.contains("pot") ||
                        block.contains("lantern") ||
                        block.contains("painting") ||
                        block.contains("carpet")
        );

        // Calcular puntuación total
        score += containerCount * CONTAINER_SCORE;
        score += furnaceCount * FURNACE_SCORE;
        score += craftingCount * CRAFTING_TABLE_SCORE;
        score += decorationCount * DECORATION_SCORE;

        // Si el jugador está en o cerca de una villa, restar una penalización a la puntuación
        if (player instanceof ServerPlayer serverPlayer && Utils.isPlayerInOrNearVillage(serverPlayer, VILLAGE_DETECTION_RADIUS)) {
            score -= VILLAGE_PENALTY;
        }

        // Actualizar puntuación
        homeLocation.setScore(score);
    }

    /**
     * Cuenta bloques de cierto tipo en un radio
     */
    private int countBlocksInRadius(BlockPos center, Level level, int radius, java.util.function.Predicate<String> blockFilter) {
        int count = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius/2; y <= radius/2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    String blockId = state.getBlock().toString().toLowerCase();

                    if (blockFilter.test(blockId)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Actualiza la casa principal basada en las puntuaciones
     */
    private void updatePrimaryHome() {
        if (potentialHomes.isEmpty()) {
            primaryHome = null;
            return;
        }

        BlockPos bestHome = null;
        int bestScore = 0;

        for (Map.Entry<BlockPos, HomeLocation> entry : potentialHomes.entrySet()) {
            HomeLocation location = entry.getValue();
            if (location.getScore() > bestScore && location.getScore() >= HOME_SCORE_THRESHOLD) {
                bestScore = location.getScore();
                bestHome = entry.getKey();
            }
        }

        primaryHome = bestHome;
    }

    /**
     * Obtiene la ubicación de la casa principal del jugador
     */
    public BlockPos getPrimaryHome() {
        return primaryHome;
    }

    /**
     * Verifica si una ubicación es la casa del jugador
     */
    public boolean isHome(BlockPos pos, int radius) {
        if (primaryHome == null) return false;

        double distSq = pos.distSqr(primaryHome);
        return distSq <= radius * radius;
    }

    /**
     * Obtiene todas las casas potenciales con sus puntuaciones
     */
    public Map<BlockPos, Integer> getAllPotentialHomes() {
        Map<BlockPos, Integer> homes = new HashMap<>();
        for (Map.Entry<BlockPos, HomeLocation> entry : potentialHomes.entrySet()) {
            homes.put(entry.getKey(), entry.getValue().getScore());
        }
        return homes;
    }

    /**
     * Obtiene la puntuación de la posición actual en un radio determinado.
     * Si no se encuentra ninguna ubicación registrada en ese radio, se retorna 0.
     *
     * @param currentPos la posición actual
     * @param radius el radio en bloques para considerar una ubicación registrada
     * @return la puntuación de la casa registrada o 0 si no existe
     */
    public int getScoreAtCurrentPosition(BlockPos currentPos, int radius) {
        for (Map.Entry<BlockPos, HomeLocation> entry : potentialHomes.entrySet()) {
            if (entry.getKey().closerThan(currentPos, radius)) {
                return entry.getValue().getScore();
            }
        }
        return 0;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        potentialHomes.clear();

        ListTag homesList = tag.getList("PotentialHomes", Tag.TAG_COMPOUND);
        for (int i = 0; i < homesList.size(); i++) {
            CompoundTag homeTag = homesList.getCompound(i);
            int x = homeTag.getInt("X");
            int y = homeTag.getInt("Y");
            int z = homeTag.getInt("Z");
            BlockPos pos = new BlockPos(x, y, z);

            HomeLocation homeLocation = new HomeLocation(pos, homeTag.getString("Dimension"));
            homeLocation.setScore(homeTag.getInt("Score"));
            homeLocation.setSleepTime(homeTag.getInt("SleepTime"));
            homeLocation.setLastActivity(homeTag.getLong("LastActivity"));

            potentialHomes.put(pos, homeLocation);
        }

        if (tag.contains("PrimaryHome")) {
            CompoundTag primaryTag = tag.getCompound("PrimaryHome");
            int x = primaryTag.getInt("X");
            int y = primaryTag.getInt("Y");
            int z = primaryTag.getInt("Z");
            primaryHome = new BlockPos(x, y, z);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        ListTag homesList = new ListTag();

        for (Map.Entry<BlockPos, HomeLocation> entry : potentialHomes.entrySet()) {
            BlockPos pos = entry.getKey();
            HomeLocation homeLocation = entry.getValue();

            CompoundTag homeTag = new CompoundTag();
            homeTag.putInt("X", pos.getX());
            homeTag.putInt("Y", pos.getY());
            homeTag.putInt("Z", pos.getZ());
            homeTag.putString("Dimension", homeLocation.getDimension());
            homeTag.putInt("Score", homeLocation.getScore());
            homeTag.putInt("SleepTime", homeLocation.getSleepTime());
            homeTag.putLong("LastActivity", homeLocation.getLastActivity());

            homesList.add(homeTag);
        }

        tag.put("PotentialHomes", homesList);

        if (primaryHome != null) {
            CompoundTag primaryTag = new CompoundTag();
            primaryTag.putInt("X", primaryHome.getX());
            primaryTag.putInt("Y", primaryHome.getY());
            primaryTag.putInt("Z", primaryHome.getZ());
            tag.put("PrimaryHome", primaryTag);
        }
    }

    /**
     * Clase que representa una ubicación potencial de casa
     */
    private static class HomeLocation {
        private final BlockPos position;
        private final String dimension;
        private int score;
        private int sleepTime;
        private long lastActivity;

        public HomeLocation(BlockPos position, String dimension) {
            this.position = position;
            this.dimension = dimension;
            this.score = 0;
            this.sleepTime = 0;
            this.lastActivity = System.currentTimeMillis();
        }

        public BlockPos getPosition() {
            return position;
        }

        public String getDimension() {
            return dimension;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getSleepTime() {
            return sleepTime;
        }

        public void setSleepTime(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        public void incrementSleepTime() {
            this.sleepTime++;
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(long lastActivity) {
            this.lastActivity = lastActivity;
        }

        public void recordActivity() {
            this.lastActivity = System.currentTimeMillis();
        }

        /**
         * Obtiene la puntuación base según el tiempo dormido
         */
        public int getBaseScore() {
            return sleepTime * 5; // 5 puntos por cada tick dormido
        }
    }
}