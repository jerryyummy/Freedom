package alchemystar.freedom.transaction.redo;

import alchemystar.freedom.meta.ClusterIndexEntry;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.transaction.OpType;
import alchemystar.freedom.transaction.log.Log;

/**
 * The type Redo manager.
 *
 * @Author lizhuyang
 */
public class RedoManager {

    /**
     * Redo.
     *
     * @param log the log
     */
    public static void redo(Log log) {
        Table table = TableManager.getTable(log.getTableName());
        switch (log.getOpType()) {
            case OpType.insert:
                IndexEntry indexEntry = new ClusterIndexEntry(log.getAfter().getValues());
                indexEntry.setIndexDesc(new IndexDesc(table.getAttributes()));
                table.insert(indexEntry);
                break;
            case OpType.delete:
                table.delete(log.getBefore());
                break;
            case OpType.update:
                IndexEntry oldEntry = table.find(log.getBefore());
                if (oldEntry != null) {
                    IndexEntry newEntry = new ClusterIndexEntry(log.getAfter().getValues());
                    newEntry.setIndexDesc(new IndexDesc(table.getAttributes()));
                    table.update(oldEntry, newEntry); // 假设`update`方法更新旧条目为新条目
                } else {
                    System.out.println("Error: No matching entry found for update.");
                }
                break;
        }
    }
}
