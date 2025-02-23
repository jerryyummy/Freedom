package alchemystar.freedom.transaction.log;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Lsn factory.
 *
 * @Author lizhuyang
 */
public class LSNFactory {

    private static AtomicLong lsnAllocator = new AtomicLong(0);

    /**
     * Next lsn long.
     *
     * @return the long
     */
    public static long nextLSN() {
        return lsnAllocator.getAndIncrement();
    }
}
