package util.linescollector;

import util.LogLine;
import util.LogsRange;

import java.util.function.Predicate;

public class LogRangesCollectorBuilder {
    private int linesInRangeCount = 1000;                           // max lines count in one range
    private Predicate<LogsRange> rangesFilter = range -> true;      // function that returns true if given range must be returned
    private Predicate<LogLine> logLinePredicate = line -> true;     // function that returns false if given line indicates a problem

    public LogRangesCollectorBuilder setLinesInRangeCount(int linesInRangeCount) {
        this.linesInRangeCount = linesInRangeCount;
        return this;
    }

    public LogRangesCollectorBuilder setRangesFilter(Predicate<LogsRange> rangesFilter) {
        this.rangesFilter = rangesFilter;
        return this;
    }

    public LogRangesCollectorBuilder setLogLinePredicate(Predicate<LogLine> logLinePredicate) {
        this.logLinePredicate = logLinePredicate;
        return this;
    }

    public LogRangesCollector build(){
       if(linesInRangeCount < 1 || linesInRangeCount > 100_000 ) throw new IllegalArgumentException("Max lines in range count must be between 1 and 100 000");
       if(rangesFilter == null) throw new IllegalArgumentException("Ranges filter must not be null");
       if(logLinePredicate == null) throw new IllegalArgumentException("Log line predicate must not be null");

        return new LogRangesCollector(linesInRangeCount, rangesFilter, logLinePredicate);
    }
}
