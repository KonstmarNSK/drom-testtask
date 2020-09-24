package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LogParser {
    private static final String REQ_TIME_MASK = "dd/MM/yyyy:HH:mm:ss";
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) - - \\[(\\d{2}/\\d{2}/\\d{4}:\\d{2}:\\d{2}:\\d{2}) [\\+\\-]\\d{4}] \"(.*)\" (\\d{3}) \\d+ (\\d+\\.\\d+) .*");


    /**
     * Parses log lines from input stream into {@link LogLine} objects wrapped into {@link Result} objects that contain
     * LogLine object if it was parsed successfully or description of parsing error.
     *
     * @param is stream from which logs are read
     * @param charset input stream charset
     * @return Stream of lines parsing results
     */
    public static Stream<Result<LogLine, ParsingProblemDescription>> tryParseFromIStream(InputStream is, Charset charset) {

        SimpleDateFormat sdf = new SimpleDateFormat(REQ_TIME_MASK);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));


        return reader.lines().map(line -> {
            Matcher matcher = LOG_LINE_PATTERN.matcher(line);

            if (matcher.matches()) {

                Date reqTime;
                try {
                    reqTime = sdf.parse(matcher.group(2));
                } catch (ParseException e) {
                    return Result.err(
                            new ParsingProblemDescription(
                                    line,
                                    String.format("Couldn't parse request time (expected format: %s)", REQ_TIME_MASK)
                            ));
                }

                String address = matcher.group(1);
                String reqUrl = matcher.group(3);
                int responseCode = Integer.parseInt(matcher.group(4));
                float reqDelay = Float.parseFloat(matcher.group(5));

                return Result.ok(new LogLine(address, reqTime, reqUrl, responseCode, reqDelay));
            } else {
                return Result.err(new ParsingProblemDescription(line, "Regex doesn't match"));
            }
        });
    }

    public static class ParsingProblemDescription {
        public final String logLine;
        public final String problemDescription;

        public ParsingProblemDescription(String logLine, String problemDescription) {
            this.logLine = logLine;
            this.problemDescription = problemDescription;
        }

        @Override
        public String toString() {
            return "ParsingProblemDescription{" +
                    "logLine='" + logLine + '\'' +
                    ", problemDescription='" + problemDescription + '\'' +
                    '}';
        }
    }
}
