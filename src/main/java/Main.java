import util.LogLinesCollector;
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
                    if(!logLineResult.isOk()){
                        System.err.printf("Problem during parsing: %s\n", logLineResult.getErr().get());
                    }

                    return logLineResult.getVal();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(new LogLinesCollector());

        int i = 4;

    }

}
