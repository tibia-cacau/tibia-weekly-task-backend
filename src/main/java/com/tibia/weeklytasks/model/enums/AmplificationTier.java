package com.tibia.weeklytasks.model.enums;

public enum AmplificationTier {
    TIER_0(0.0),
    TIER_1(2.50),
    TIER_2(5.40),
    TIER_3(9.10),
    TIER_4(13.60),
    TIER_5(18.90),
    TIER_6(25.00),
    TIER_7(31.90),
    TIER_8(39.60),
    TIER_9(48.10),
    TIER_10(57.40);

    private final double procRate;

    AmplificationTier(double procRate) {
        this.procRate = procRate;
    }

    public double getProcRate() {
        return procRate;
    }

    public static AmplificationTier fromTierNumber(int tier) {
        if (tier < 0 || tier > 10) {
            throw new IllegalArgumentException("Tier must be between 0 and 10");
        }
        return values()[tier];
    }
}
