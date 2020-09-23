package util.linescollector;

import util.LogLine;
import util.LogsRange;

import java.util.function.Predicate;

public class LogLinesCollectorBuilder {
    private int linesInRangeCount = 1000;
    private Predicate<LogsRange> rangesFilter = range -> true;
    private Predicate<LogLine> logLinePredicate = line -> true;

    public LogLinesCollectorBuilder setLinesInRangeCount(int linesInRangeCount) {
        this.linesInRangeCount = linesInRangeCount;
        return this;
    }

    public LogLinesCollectorBuilder setRangesFilter(Predicate<LogsRange> rangesFilter) {
        this.rangesFilter = rangesFilter;
        return this;
    }

    public LogLinesCollectorBuilder setLogLinePredicate(Predicate<LogLine> logLinePredicate) {
        this.logLinePredicate = logLinePredicate;
        return this;
    }

    public LogLinesCollector build(){
       if(linesInRangeCount < 1 || linesInRangeCount > 100_000 ) throw new IllegalArgumentException("Max lines in range count must be between 1 and 100 000");
       if(rangesFilter == null) throw new IllegalArgumentException("Ranges filter must not be null");
       if(logLinePredicate == null) throw new IllegalArgumentException("Log line predicate must not be null");

        return new LogLinesCollector(linesInRangeCount, rangesFilter, logLinePredicate);
    }
}
