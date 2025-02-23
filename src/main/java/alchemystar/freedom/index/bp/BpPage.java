package alchemystar.freedom.index.bp;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.constant.ItemConst;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageHeaderData;
import alchemystar.freedom.store.page.PageLoader;

/**
 * Page
 * byte struct
 * PageHeaderData
 * isLeaf int
 * isRoot int
 * this pageNo int
 * parent pageNo int
 * entryCount int
 * entries Tuples
 * childCount int
 * childCounts Tuples
 * previous page
 * next page
 * <p>
 * 这个文件似乎处理与节点存储相关的底层页面管理，具体包括：
 * 页面管理：管理页面的使用和空闲空间，确保数据有效地存储在磁盘页面上。
 * 空间计算：提供方法来计算页面的初始空闲空间和当前空闲空间，这对于节点操作中的空间管理非常关键
 *
 * @Author lizhuyang
 */
public class BpPage extends Page {

    private BPNode bpNode;

    private int leafInitFreeSpace;

    private int nodeInitFreeSpace;

    /**
     * Instantiates a new Bp page.
     *
     * @param defaultSize the default size
     */
    public BpPage(int defaultSize) {
        super(defaultSize);
        init();
    }

    /**
     * Instantiates a new Bp page.
     *
     * @param bpNode the bp node
     */
    public BpPage(BPNode bpNode) {
        super(SystemConfig.DEFAULT_PAGE_SIZE);
        this.bpNode = bpNode;
        init();
    }

    private void init() {
        leafInitFreeSpace =
                length - SystemConfig.DEFAULT_SPECIAL_POINT_LENGTH - ItemConst.INT_LEANGTH * 7 - PageHeaderData
                        .PAGE_HEADER_SIZE;
        nodeInitFreeSpace =
                length - SystemConfig.DEFAULT_SPECIAL_POINT_LENGTH - ItemConst.INT_LEANGTH * 6 - PageHeaderData
                        .PAGE_HEADER_SIZE;
    }

    /**
     * Read from page bp node.
     *
     * @param bpTree the bp tree
     * @return the bp node
     */
    public BPNode readFromPage(BPTree bpTree) {
        PageLoader loader = new PageLoader(this);
        loader.load();

        boolean isLeaf = getTupleBoolean(loader.getIndexEntries()[0]);
        boolean isRoot = getTupleBoolean(loader.getIndexEntries()[1]);

        bpNode = new BPNode(isLeaf, isRoot, bpTree);
        if (loader.getIndexEntries() == null) {
            // 处理没有记录的情况
            return bpNode;
        }
        // 由于是从磁盘中读取,以磁盘记录的为准
        int pageNo = getTupleInt(loader.getIndexEntries()[2]);
        bpNode.setPageNo(pageNo);
        // 首先在这边放入nodeMap,否则由于一直递归,一直没机会放入,导致循环递归
        bpTree.nodeMap.put(pageNo, bpNode);
        int parentPageNo = getTupleInt(loader.getIndexEntries()[3]);
        bpNode.setParent(bpTree.getNodeFromPageNo(parentPageNo));
        int entryCount = getTupleInt(loader.getIndexEntries()[4]);
        for (int i = 0; i < entryCount; i++) {
            bpNode.getEntries().add(loader.getIndexEntries()[5 + i]);
        }
        if (!isLeaf) {
            int childCount = getTupleInt(loader.getIndexEntries()[5 + entryCount]);
            int initSize = 6 + entryCount;
            for (int i = 0; i < childCount; i++) {
                int childPageNo = getTupleInt(loader.getIndexEntries()[initSize + i]);
                bpNode.getChildren().add(bpTree.getNodeFromPageNo(childPageNo));
            }
        } else {
            int initSize = 5 + entryCount;
            int previousNo = getTupleInt(loader.getIndexEntries()[initSize]);
            int nextNo = getTupleInt(loader.getIndexEntries()[initSize + 1]);
            bpNode.setPrevious(bpTree.getNodeFromPageNo(previousNo));
            bpNode.setNext(bpTree.getNodeFromPageNo(nextNo));
        }

        return bpNode;
    }

    /**
     * Write to page.
     */
    public void writeToPage() {
        // header already write
        // write isLeaf
        writeTuple(genIsLeafTuple());
        // write isRoot
        writeTuple(genIsRootTuple());
        // this pageNo
        writeTuple(genTupleInt(bpNode.getPageNo()));
        // parent node pageNo
        if (!bpNode.isRoot) {
            writeTuple(genTupleInt(bpNode.getParent().getPageNo()));
        } else {
            // parent node -1 表示当前页面是root页面
            writeTuple(genTupleInt(-1));
        }
        // write entries,(count,entry1,entry2 ...)
        // entry count
        writeTuple(genTupleInt(bpNode.getEntries().size()));
        // entries
        for (int i = 0; i < bpNode.getEntries().size(); i++) {
            writeTuple(bpNode.getEntries().get(i));
        }
        if (!bpNode.isLeaf()) {
            // 非叶子节点
            // write childrensPageNo,(count,child1PageNo1,child2PageNo2 ...)
            writeTuple(genTupleInt(bpNode.getChildren().size()));
            for (int i = 0; i < bpNode.getChildren().size(); i++) {
                writeTuple(genTupleInt(bpNode.getChildren().get(i).getPageNo()));
            }
        } else {
            if (bpNode.getPrevious() == null) {
                writeTuple(genTupleInt(-1));
            } else {
                writeTuple(genTupleInt(bpNode.getPrevious().getPageNo()));
            }
            if (bpNode.getNext() == null) {
                writeTuple(genTupleInt(-1));
            } else {
                writeTuple(genTupleInt(bpNode.getNext().getPageNo()));
            }

        }
    }

    /**
     * Gets content size.
     *
     * @return the content size
     */
    public int getContentSize() {
        int size = 0;
        for (IndexEntry key : bpNode.getEntries()) {
            size += Item.getItemLength(key);
        }
        if (!bpNode.isLeaf()) {
            for (int i = 0; i < bpNode.getChildren().size(); i++) {
                size += ItemConst.INT_LEANGTH;
            }
        }
        return size;
    }

    /**
     * Calculate remain free space int.
     *
     * @return the int
     */
    public int calculateRemainFreeSpace() {
        return getInitFreeSpace() - getContentSize();
    }

    /**
     * Gets init free space.
     *
     * @return the init free space
     */
    public int getInitFreeSpace() {
        if (bpNode.isLeaf()) {
            return leafInitFreeSpace;
        } else {
            return nodeInitFreeSpace;
        }
    }

    private IndexEntry genIsLeafTuple() {
        return genBoolTuple(bpNode.isLeaf());
    }

    private IndexEntry genIsRootTuple() {
        return genBoolTuple(bpNode.isRoot());
    }

    // 当前bool tuple都用int代替
    private IndexEntry genBoolTuple(boolean b) {
        if (b) {
            return genTupleInt(1);
        } else {
            return genTupleInt(0);
        }
    }

    /**
     * Gen tuple int index entry.
     *
     * @param i the
     * @return the index entry
     */
    public static IndexEntry genTupleInt(int i) {
        Value[] vs = new Value[1];
        ValueInt valueInt = new ValueInt(i);
        vs[0] = valueInt;
        return new IndexEntry(vs);
    }

    /**
     * Gets tuple int.
     *
     * @param indexEntry the index entry
     * @return the tuple int
     */
    public int getTupleInt(IndexEntry indexEntry) {
        return ((ValueInt) indexEntry.getValues()[0]).getInt();
    }

    /**
     * Gets tuple boolean.
     *
     * @param indexEntry the index entry
     * @return the tuple boolean
     */
    public boolean getTupleBoolean(IndexEntry indexEntry) {
        int i = ((ValueInt) indexEntry.getValues()[0]).getInt();
        if (i == 1) {
            return true;
        } else {
            return false;
        }
    }
}
