import util.Configs;
import util.LogsRange;
import util.linescollector.LogRangesCollector;
import util.LogParser;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final Set<String> argsParamsNames = new HashSet<>();

    static {
        argsParamsNames.add("-t");
        argsParamsNames.add("-u");
        argsParamsNames.add("-h");
    }

    public static void main(String[] args) throws Exception {
        final Configs cfg = readCfgFromArgs(args);

        List<LogsRange> badRanges = LogParser.tryParseFromIStream(System.in, Charset.forName("UTF-8"))
                .map(logLineResult -> {
                    if (!logLineResult.isOk()) {
                        System.err.printf("Problem during parsing: %s\n", logLineResult.getErr().get());
                    }

                    return logLineResult.getVal();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(LogRangesCollector.builder()
                        .setLinesInRangeCount(1000)
                        .setLogLinePredicate(line -> line.delay <= cfg.maxRequestDelayMs && (line.responseCode < 500 || line.responseCode >= 600))
                        .setRangesFilter(range -> range.availabilityRate < cfg.minAvailabilityRate)
                        .build()
                );

        badRanges.sort(Comparator.comparing(a -> a.timeStart));

        SimpleDateFormat outDateFormat = new SimpleDateFormat("HH:mm:ss");


        badRanges.forEach(logsRange ->
                System.out.printf("%8s - %8s  %.1f\n",
                        outDateFormat.format(logsRange.timeStart),
                        outDateFormat.format(logsRange.timeEnd),
                        logsRange.availabilityRate
                ));
    }


    private static Configs readCfgFromArgs(String[] args) {
        String paramName = null;
        float maxRequestDelayMs = 0.0f;
        float minAvailabilityRate = 0.0f;

        for (int i = 0; i < args.length; i++) {
            String currParam = args[i];

            // parameter name
            if (paramName == null) {
                if (!argsParamsNames.contains(currParam)) {
                    System.err.printf("Parameter name '%s' is not allowed. Launch with -h key for help\n", currParam);
                    System.exit(0);
                }

                if (currParam.equals("-h")) {
                    System.out.println("usage: cat log.log | java -jar log-analysis -u <MIN_AVAILABILITY_RATE> -t <MAX_REQUEST_DELAY_MS>");
                    System.exit(0);
                }

                paramName = currParam;
                continue;
            }

            // parameter value
            switch (paramName) {
                case "-t":
                    maxRequestDelayMs = Float.parseFloat(currParam);
                    break;

                case "-u":
                    minAvailabilityRate = Float.parseFloat(currParam);
                    break;
            }

            paramName = null;
        }

        return new Configs(maxRequestDelayMs, minAvailabilityRate);
    }
}
