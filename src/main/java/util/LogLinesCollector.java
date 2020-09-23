package util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class LogLinesCollector implements Collector<LogLine, LogLinesCollector.Accumulator, List<LogsRange>> {


    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
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
        private List<LogLine> currentRangeLogs = new ArrayList<>();
        private List<LogsRange> ranges = new ArrayList<>();

        public void addLogLine(LogLine line){
            if(currentRangeLogs.size() == 999){
                ranges.add(new LogsRange(currentRangeLogs.get(0).reqTime,line.reqTime, 99.9f));
                currentRangeLogs.clear();
            } else {
                currentRangeLogs.add(line);
            }
        }

        public List<LogsRange> getRanges() {
            return ranges;
        }
    }
}
