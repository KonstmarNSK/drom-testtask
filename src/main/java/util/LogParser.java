package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    public static List<LogLine> parseFromIStream(InputStream is) throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss ZZZZ");
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+) - - \\[(.*)] \"(.*)\" (\\d+) \\d+ (\\d+\\.\\d+) .*");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        List<LogLine> result = new ArrayList<>();

        String nextLine;
        while (null != (nextLine = reader.readLine())) {
            Matcher matcher = pattern.matcher(nextLine);

            if (matcher.matches()) {
                String address = matcher.group(1);
                Date reqTime = sdf.parse(matcher.group(2));
                String reqUrl = matcher.group(3);
                int responseCode = Integer.parseInt(matcher.group(4));
                float reqDelay = Float.parseFloat(matcher.group(5));

                result.add(new LogLine(address, reqTime, reqUrl, responseCode, reqDelay));
            } else {
                // todo: обрботать ошибку парсинга
            }
        }

        result.sort((a, b) -> {
            if(a.reqTime.before(b.reqTime)){
                return -1;
            }
            if(a.reqTime.after(b.reqTime)){
                return 1;
            }

            return 0;
        });

        return result;
    }
}
