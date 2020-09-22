package util;

import java.util.Date;

public class LogLine {
    public final String address;
    public final Date reqTime;
    public final String url;
    public final int responseCode;
    public final float delay;

    public LogLine(String address, Date reqTime, String url, int responseCode, float delay) {
        this.address = address;
        this.reqTime = reqTime;
        this.url = url;
        this.responseCode = responseCode;
        this.delay = delay;
    }
}
