package util;

import java.util.Date;

public class LogsRange {
    public final Date timeStart;
    public final Date timeEnd;
    public final float availabilityRate;

    public LogsRange(Date timeStart, Date timeEnd, float availabilityRate) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.availabilityRate = availabilityRate;
    }
}
