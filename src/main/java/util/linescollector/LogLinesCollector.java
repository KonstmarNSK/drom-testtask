package util.linescollector;

import util.LogLine;
import util.LogsRange;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class LogLinesCollector implements Collector<LogLine, LogLinesCollector.Accumulator, List<LogsRange>> {
    private final int linesInRangeCount;
    private final Predicate<LogsRange> rangesFilter;
    private final Predicate<LogLine> logLinePredicate;

    LogLinesCollector(int linesInRangeCount, Predicate<LogsRange> rangesFilter, Predicate<LogLine> logLinePredicate) {
        this.linesInRangeCount = linesInRangeCount;
        this.rangesFilter = rangesFilter;
        this.logLinePredicate = logLinePredicate;
    }

    public static LogLinesCollectorBuilder builder(){
        return new LogLinesCollectorBuilder();
    }


    @Override
    public Supplier<Accumulator> supplier() {
        return () -> new Accumulator(linesInRangeCount, rangesFilter, logLinePredicate);
    }

    @Override
    public BiConsumer<Accumulator, LogLine> accumulator() {
        return Accumulator::addLogLine;
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return null;
    }

    @Override
    public Function<Accumulator, List<LogsRange>> finisher() {
        return Accumulator::getRanges;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }




    public static class Accumulator {
        private int currentRangeBadLinesCount = 0;
        private int currentRangeLinesTotal = 0;
        private Date currentRangeStartTime = null;
        private Date lastLineInRangeReqTime = null;

        private List<LogsRange> ranges = new ArrayList<>();

        private final int maxLinesInRangeCount;
        private final Predicate<LogsRange> rangesFilter;
        private final Predicate<LogLine> logLinePredicate;



        Accumulator(int maxLinesInRangeCount, Predicate<LogsRange> rangesFilter, Predicate<LogLine> logLinePredicate) {
            this.maxLinesInRangeCount = maxLinesInRangeCount;
            this.rangesFilter = rangesFilter;
            this.logLinePredicate = logLinePredicate;
        }

        public void addLogLine(LogLine line){

            // if this is the first log line in range
            if(currentRangeLinesTotal == 0){
                currentRangeStartTime = line.reqTime;
            }

            lastLineInRangeReqTime = line.reqTime;
            currentRangeLinesTotal++;

            if(!logLinePredicate.test(line)){
                currentRangeBadLinesCount++;
            }

            if(currentRangeLinesTotal == maxLinesInRangeCount){
                saveCurrentRange();
            }
        }

        private void saveCurrentRange(){

            // if there was no log lines in current range
            if(currentRangeLinesTotal == 0){
                return;
            }

            float availabilityRate = 100.0f - (((float) currentRangeBadLinesCount) / currentRangeLinesTotal) * 100.0f;
            LogsRange newLogsRange = new LogsRange(currentRangeStartTime, lastLineInRangeReqTime, availabilityRate);

            if(rangesFilter.test(newLogsRange)) {
                ranges.add(newLogsRange);
            }

            currentRangeLinesTotal = 0;
            currentRangeBadLinesCount = 0;
        }

        public List<LogsRange> getRanges() {
            saveCurrentRange();
            return ranges;
        }
    }
}
