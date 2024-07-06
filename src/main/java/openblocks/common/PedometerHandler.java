package openblocks.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import openblocks.OpenBlocks;

public class PedometerHandler {

    public static class PedometerState {
        public PedometerState() {
        }

        private double totalDistance;

        private long startTicks;

        private Vec3 startPos;

        private Vec3 prevTickPos;

        private long prevTickTime;

        private Vec3 lastCheckPos;

        private long lastCheckTime;

        private PedometerData lastResult;

        private boolean isRunning;

        public void reset() {
            isRunning = false;
            totalDistance = 0;
            lastResult = null;

            lastCheckPos = null;
            lastCheckTime = 0;

            prevTickPos = null;
            prevTickTime = 0;
        }

        public void init(Entity entity, Level world) {
            lastCheckPos = prevTickPos = startPos = entity.position();
            lastCheckTime = prevTickTime = startTicks = world.getGameTime();
            isRunning = true;
        }

        public void update(Entity entity) {
            Vec3 currentPosition = entity.position();
            Vec3 deltaSinceLastUpdate = currentPosition.subtract(prevTickPos);
            prevTickPos = currentPosition;

            long currentTime = entity.level().getGameTime();
            double ticksSinceLastUpdate = currentTime - prevTickTime;
            prevTickTime = currentTime;

            double distanceSinceLastTick = deltaSinceLastUpdate.length();
            double currentSpeed = ticksSinceLastUpdate != 0? distanceSinceLastTick / ticksSinceLastUpdate : 0;
            totalDistance += distanceSinceLastTick;

            Vec3 deltaFromStart = currentPosition.subtract(startPos);
            long ticksFromStart = currentTime - startTicks;

            double distanceFromStart = deltaFromStart.length();

            double distanceFromLastCheck = 0;
            if (lastCheckPos != null) distanceFromLastCheck = currentPosition.subtract(lastCheckPos).length();

            long timeFromLastCheck = currentTime - lastCheckTime;

            lastResult = new PedometerData(startPos, ticksFromStart, totalDistance, distanceFromStart, distanceFromLastCheck, timeFromLastCheck, currentSpeed);
        }

        public boolean isRunning() {
            return isRunning;
        }

        public PedometerData getData() {
            lastCheckPos = prevTickPos;
            lastCheckTime = prevTickTime;
            return lastResult;
        }
    }

    public static class PedometerData {
        public final Vec3 startingPoint;
        public final long totalTime;
        public final double totalDistance;
        public final double straightLineDistance;
        public final double lastCheckDistance;
        public final long lastCheckTime;

        public final double currentSpeed;

        private PedometerData(Vec3 startingPoint,
                              long totalTime,
                              double totalDistance,
                              double straightLineDistance,
                              double lastCheckDistance,
                              long lastCheckTime,
                              double currentSpeed) {
            this.startingPoint = startingPoint;
            this.totalTime = totalTime;
            this.totalDistance = totalDistance;
            this.straightLineDistance = straightLineDistance;
            this.lastCheckDistance = lastCheckDistance;
            this.lastCheckTime = lastCheckTime;
            this.currentSpeed = currentSpeed;
        }

        public double averageSpeed() {
            if (totalTime == 0) return 0;
            return totalDistance / totalTime;
        }

        public double straightLineSpeed() {
            if (totalTime == 0) return 0;
            return straightLineDistance / totalTime;
        }

        public double lastCheckSpeed() {
            if (lastCheckTime == 0) return 0;
            return lastCheckDistance / lastCheckTime;
        }
    }

    public static PedometerState getProperty(Entity entity) {
        return entity.getData(OpenBlocks.PEDOMETER_ATTACHMENT);
    }
}