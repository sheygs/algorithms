package TokenManager.TokenManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private Instant currentTime;

    public MutableClock(Instant currentTime) {
        this.currentTime = currentTime;
    }

    public void setInstant(Instant instant) {
        this.currentTime = instant;
    }

    @Override
    public Instant instant() {
        return currentTime;
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}