package com.tibia.weeklytasks.model.enums;

public enum TranscendenceTier {
    TIER_0(0.0),
    TIER_1(0.13),
    TIER_2(0.27),
    TIER_3(0.44),
    TIER_4(0.64),
    TIER_5(0.86),
    TIER_6(1.11),
    TIER_7(1.38),
    TIER_8(1.68),
    TIER_9(2.00),
    TIER_10(2.35);

    private final double procRate;

    TranscendenceTier(double procRate) {
        this.procRate = procRate;
    }

    public double getProcRate() {
        return procRate;
    }

    public static TranscendenceTier fromTierNumber(int tier) {
        if (tier < 0 || tier > 10) {
            throw new IllegalArgumentException("Tier must be between 0 and 10");
        }
        return values()[tier];
    }
}
