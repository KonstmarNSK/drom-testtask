import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.LogParser;
import util.LogsRange;
import util.linescollector.LogLinesCollector;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public class LogParserTests {
    @Test
    public void testRateCorrectness() throws IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        String correct = "192.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" 200 2 44.510983 \"-\" \"@list-item-updater\" prio:0";
        out.write(correct.getBytes(Charset.forName("UTF-8")));
        out.close();

        List<LogsRange> badRanges = LogParser.tryParseFromIStream(in, Charset.forName("UTF-8"))
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
                        .setLogLinePredicate(line -> line.delay <= 45 && (line.responseCode < 500 || line.responseCode >= 600))
//                        .setRangesFilter(range -> range.availabilityRate < 95)
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

        Assertions.assertEquals(badRanges.size(), 1);
    }
}
