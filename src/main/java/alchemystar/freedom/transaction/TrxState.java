package alchemystar.freedom.transaction;

/**
 * TrxState
 *
 * @Author lizhuyang
 */
public interface TrxState {
    /**
     * The constant TRX_STATE_NOT_STARTED.
     */
// 事务未开始
    int TRX_STATE_NOT_STARTED = 0;
    /**
     * The constant TRX_STATE_ACTIVE.
     */
// 事务进行中
    int TRX_STATE_ACTIVE = 1;
    /**
     * The constant TRX_STATE_PREPARED.
     */
// 暂时不用 for 2PC/XA
    int TRX_STATE_PREPARED = 2;
    /**
     * The constant TRX_COMMITTED.
     */
// 事务已提交
    int TRX_COMMITTED = 3;

}
