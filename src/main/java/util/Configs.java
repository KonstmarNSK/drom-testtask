package util;

public class Configs {
    public final float maxRequestDelayMs;
    public final float minAvailabilityRate;

    public Configs(float maxRequestDelayMs, float minAvailabilityRate) {
        this.maxRequestDelayMs = maxRequestDelayMs;
        this.minAvailabilityRate = minAvailabilityRate;
    }
}
