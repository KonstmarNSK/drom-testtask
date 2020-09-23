import util.Configs;
import util.LogsRange;
import util.linescollector.LogLinesCollector;
import util.LogParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                .collect(LogLinesCollector.builder()
                        .setLinesInRangeCount(1000)
                        .setLogLinePredicate(line -> line.delay <= cfg.maxRequestDelayMs && (line.responseCode < 500 || line.responseCode >= 600))
                        .setRangesFilter(range -> range.availabilityRate < cfg.minAvailabilityRate)
                        .build()
                );

        badRanges.sort((a, b) -> {
            if(a.timeStart.before(b.timeStart)){
                return -1;
            }else if(a.timeStart.after(b.timeStart)){
                return 1;
            }

            return 0;
        });

        badRanges.forEach(System.out::println);
    }


    private static Configs readCfgFromArgs(String[] args) {
        String paramName = null;
        float maxRequestDelayMs = 0.0f;
        float minAvailabilityRate = 0.0f;

        for (int i = 0; i < args.length; i++) {
            String currParam = args[i];

            // parameter name
            if (paramName == null) {
                if (!argsParamsNames.contains(currParam)){
                    System.err.printf("Parameter name '%s' is not allowed. Launch with -h key for help\n", currParam);
                    System.exit(0);
                }

                if(currParam.equals("-h")) {
                    System.out.println("usage: cat log.log | java -jar log-analysis -u <MIN_AVAILABILITY_RATE> -t <MAX_REQUEST_DELAY_MS>");
                    System.exit(0);
                }

                paramName = currParam;
                continue;
            }

            // parameter value
            switch (paramName){
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
