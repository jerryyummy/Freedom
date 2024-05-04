package alchemystar.freedom.optimizer;

import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.Index;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;

/**
 * @Author lizhuyang
 */
public class Optimizer {

    private Table table;

    public Optimizer(Table table) {
        this.table = table;
    }

    public Index chooseIndex(IndexEntry entry) {
        if (entry != null && !entry.isAllNull()) {
            IndexDesc indexDesc = entry.getIndexDesc();
            if (indexDesc.getPrimaryAttr() != null && entry.getValues()[indexDesc.getPrimaryAttr().getIndex()] != null) {
                return table.getClusterIndex();
            }

            // 优化选择最适合的二级索引
            for (BaseIndex idx : table.getSecondIndexes()) {
                IndexDesc idesc = idx.getIndexDesc();
                if (isIndexApplicable(idesc, entry)) {
                    return idx;
                }
            }
        }
        // 如果没有合适的二级索引，回退到聚簇索引
        return table.getClusterIndex();
    }

    private boolean isIndexApplicable(IndexDesc idesc, IndexEntry entry) {
        for (Attribute attr : idesc.getAttrs()) {
            if (entry.getValues()[attr.getIndex()] == null) {
                return false;
            }
        }
        return true;
    }


}
