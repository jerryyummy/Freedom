package alchemystar.freedom.transaction.log;

/**
 * The interface Log type.
 *
 * @Author lizhuyang
 */
public interface LogType {

    /**
     * The constant TRX_START.
     */
    int TRX_START = 0;

    /**
     * The constant ROLL_BACK.
     */
    int ROLL_BACK = 1 ;

    /**
     * The constant COMMIT.
     */
    int COMMIT = 2;

    /**
     * The constant ROW.
     */
    int ROW = 3;
}
