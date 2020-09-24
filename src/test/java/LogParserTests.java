import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.Configs;
import util.LogParser;
import util.LogsRange;
import util.linescollector.LogRangesCollector;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class LogParserTests {

    private static ExecutorService threadPool;
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    @BeforeAll
    public static void setUpPool(){
        threadPool = Executors.newFixedThreadPool(10);
    }

    @AfterAll
    public static void shutdownPool(){
        threadPool.shutdownNow();
    }

    @Test
    public void testRateCorrectness() throws IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        LogRangesCollector collector = LogRangesCollector.builder()
                .setLinesInRangeCount(10)
                .setLogLinePredicate(line -> line.delay <= 45 && (line.responseCode < 500 || line.responseCode >= 600))
                .build();

        Future<List<LogsRange>> rangesFuture = threadPool.submit(new LogParserCallable(collector, in));


        String template = "192.168.32.181 - - [14/06/2017:16:%s:00 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=6076537c HTTP/1.1\" %s 2 %s.510983 \"-\" \"@list-item-updater\" prio:0\n";

        // первый диапазон. 10 запсей, 1 с неправильным кодом, 1 с большим временем выполнения. Ожидается 80 % доступности
        for(int i = 0; i< 8; i++){
            out.write(String.format(template, "1" + i, "200", 40).getBytes(UTF8_CHARSET));
        }
        out.write(String.format(template, "1" + 8, "501", 40).getBytes(UTF8_CHARSET));
        out.write(String.format(template, "1" + 9, "200", 50).getBytes(UTF8_CHARSET));

        // второй диапазон. 10 записей, 1 с неправильным кодом. Ожидается 90 % доступности
        for(int i = 0; i< 9; i++){
            out.write(String.format(template, "2" + i, "200", 40).getBytes(UTF8_CHARSET));
        }

        out.write(String.format(template, "2" + 9, "500", 50).getBytes(UTF8_CHARSET));
        out.flush();
        out.close();

        List<LogsRange> ranges = null;
        try {
            ranges = rangesFuture.get();
        }catch (ExecutionException | InterruptedException e){
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(ranges.size(), 2);
        Assertions.assertTrue(ranges.stream().anyMatch(range -> range.availabilityRate == 80.0f));
        Assertions.assertTrue(ranges.stream().anyMatch(range -> range.availabilityRate == 90.0f));
    }



    public static class LogParserCallable implements Callable<List<LogsRange>> {
        private final LogRangesCollector collector;
        private final InputStream in;

        public LogParserCallable(LogRangesCollector collector, InputStream in) {
            this.collector = collector;
            this.in = in;
        }

        @Override
        public List<LogsRange> call() throws Exception {

            return LogParser.tryParseFromIStream(in, Charset.forName("UTF-8"))
                    .map(logLineResult -> {

                        if (!logLineResult.isOk()) {
                            System.err.printf("Problem during parsing: %s\n", logLineResult.getErr().get());
                        }

                        return logLineResult.getVal();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(collector);
        }
    }
}
