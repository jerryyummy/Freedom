package alchemystar.freedom.transaction.undo;

import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.transaction.OpType;
import alchemystar.freedom.transaction.log.Log;
import alchemystar.freedom.transaction.log.LogType;

/**
 * The type Undo manager.
 *
 * @Author lizhuyang
 */
public class UndoManager {

    /**
     * Undo.
     *
     * @param log the log
     */
    public static void undo(Log log) {
        Table table = TableManager.getTable(log.getTableName());
        if (log.getLogType() == LogType.ROW) {
            switch (log.getOpType()) {
                case OpType.insert:
                    undoInsert(table, log);
                    break;
                case OpType.update:
                    undoUpdate(table, log);
                    break;
                case OpType.delete:
                    undoDelete(table, log);
                    break;
            }
        } else {
            System.out.println("the log type is not row");
        }
    }

    /**
     * Undo insert.
     *
     * @param table the table
     * @param log   the log
     */
    public static void undoInsert(Table table, Log log) {
        // insert undo = > delete
        table.delete(log.getAfter());
    }

    /**
     * Undo update.
     *
     * @param table the table
     * @param log   the log
     */
    public static void undoUpdate(Table table, Log log) {
        //update undo = > delete + insert
        table.update(log.getAfter(), log.getBefore());
    }

    /**
     * Undo delete.
     *
     * @param table the table
     * @param log   the log
     */
    public static void undoDelete(Table table, Log log) {
        table.insert(log.getBefore());
    }
}
