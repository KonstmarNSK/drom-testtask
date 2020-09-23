import util.linescollector.LogLinesCollector;
import util.LogParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("./access.log");

        Object r = LogParser.tryParseFromIStream(is, Charset.forName("UTF-8"))
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
                        .setLogLinePredicate(line -> line.delay <= 45 && (line.responseCode < 500 || line.responseCode >= 600) )
                        .setRangesFilter(range -> range.availabilityRate < 99.9f)
                        .build()
                );

        int i = 4;

    }

}
