import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.linescollector.LogRangesCollector;

// проверки наличия проверки входных данных в билдере LogRangeCollector'а
public class RangesCollectorTests {

    @Test
    public void testRangesFilterNullCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> LogRangesCollector.builder()
                .setRangesFilter(null)
                .build());
    }

    @Test
    public void testLinePredicateNullCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> LogRangesCollector.builder()
                .setLogLinePredicate(null)
                .build());
    }

    @Test
    public void testLinesInRangeCountCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> LogRangesCollector.builder()
                .setLinesInRangeCount(-1)
                .build());

        Assertions.assertThrows(IllegalArgumentException.class, () -> LogRangesCollector.builder()
                .setLinesInRangeCount(0)
                .build());

        Assertions.assertThrows(IllegalArgumentException.class, () -> LogRangesCollector.builder()
                .setLinesInRangeCount(999_999_999)
                .build());
    }
}
