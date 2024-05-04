package alchemystar.freedom.transaction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Trx manager.
 *
 * @Author lizhuyang
 */
public class TrxManager {

    private static AtomicInteger trxIdCount = new AtomicInteger(1);

    /**
     * New trx trx.
     *
     * @return the trx
     */
    public static Trx newTrx() {
        Trx trx = new Trx();
        trx.setTrxId(trxIdCount.getAndIncrement());
        return trx;
    }

    /**
     * New empty trx trx.
     *
     * @return the trx
     */
    public static Trx newEmptyTrx() {
        Trx trx = new Trx();
        return trx;
    }
}
