package openblocks.lib.utils;

import net.minecraft.world.entity.player.Player;

public class EnchantmentUtils {

    /**
     * Be warned, minecraft doesn't update experienceTotal properly, so we have
     * to do this.
     *
     * @param player
     * @return
     */
    public static int getPlayerXP(Player player) {
        return (int)(EnchantmentUtils.getExperienceForLevel(player.experienceLevel) + (player.experienceProgress * player.getXpNeededForNextLevel()));
    }

    public static void addPlayerXP(Player player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.totalExperience = experience;
        player.experienceLevel = EnchantmentUtils.getLevelForExperience(experience);
        int expForLevel = EnchantmentUtils.getExperienceForLevel(player.experienceLevel);
        player.experienceProgress = (float)(experience - expForLevel) / (float)player.getXpNeededForNextLevel();
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getXpToNextLevel(int level) {
        int levelXP = EnchantmentUtils.getLevelForExperience(level);
        int nextXP = EnchantmentUtils.getExperienceForLevel(level + 1);
        return nextXP - levelXP;
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }

    public static float getLevelAsFloat(Player player) {
        return player.experienceLevel + player.experienceProgress;
    }

    public record ExperienceResult(int level, float progress) {
        public float getLevelAsFloat() { return level + progress; };
    }

    public static ExperienceResult getLevelIfApplied(Player player, int amount) {
        int experience = getPlayerXP(player) + amount;
        int totalExperience = experience;
        int experienceLevel = EnchantmentUtils.getLevelForExperience(experience);
        int expForLevel = EnchantmentUtils.getExperienceForLevel(experienceLevel);
        float experienceProgress = (float) (experience - expForLevel) / (float) player.getXpNeededForNextLevel();
        return new ExperienceResult(experienceLevel, experienceProgress);
    }
}