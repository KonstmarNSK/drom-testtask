import util.LogLine;
import util.LogParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("./access.log");

        List<LogLine> logLineList = LogParser.parseFromIStream(is);
        int i = 4;
    }
}
