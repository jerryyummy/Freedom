package alchemystar.freedom.index.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.access.ClusterIndexCursor;
import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.access.SecondIndexCursor;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.CompareType;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;
import alchemystar.freedom.store.page.PagePool;

/**
 * BPTree
 * B plus tree
 * <p>
 * 这个文件定义了整个B+树的结构和操作，主要包括：
 * 树操作：插入、删除、搜索和遍历功能。
 * 根节点和头节点的管理：保持对根节点和头节点（叶子节点链表的起始节点）的引用，这对于许多树操作是必要的。
 * 持久化到磁盘：提供方法将树的状态写入磁盘，这是数据库和文件系统中常见的要求。
 * 批量删除和插入：允许对多个条目进行批量操作，优化大量数据处理的性能
 *
 * @Author lizhuyang
 */
public class BPTree extends BaseIndex {

    /**
     * 根节点
     */
    protected BPNode root;

    /**
     * 叶子节点的链表头
     */
    protected BPNode head;

    /**
     * The Node map.
     */
    protected Map<Integer, BPNode> nodeMap;

    /**
     * Instantiates a new Bp tree.
     *
     * @param table      the table
     * @param indexName  the index name
     * @param attributes the attributes
     */
    public BPTree(Table table, String indexName, Attribute[] attributes) {
        super(table, indexName, attributes);
        root = new BPNode(true, true, this);
        head = root;
        nodeMap = new HashMap<Integer, BPNode>();
    }

    /**
     * Load from disk.
     */
    public void loadFromDisk() {
        int rootPageNo = getRootPageNoFromMeta();
        getNodeFromPageNo(rootPageNo);
    }

    /**
     * Gets root page no from meta.
     *
     * @return the root page no from meta
     */
    public int getRootPageNoFromMeta() {
        PageLoader loader = new PageLoader(fStore.readPageFromFile(0));//读取文件中第一页的数据，并使用 PageLoader 加载页数据
        loader.load();
        return ((ValueInt) loader.getIndexEntries()[0].getValues()[0]).getInt();
    }

    /**
     * Gets node from page no.
     *
     * @param pageNo the page no
     * @return the node from page no
     */
    public BPNode getNodeFromPageNo(int pageNo) {//根据给定的页号获取对应的节点
        if (pageNo == -1) {
            return null;
        }
        BPNode bpNode = nodeMap.get(pageNo);//首先检查缓存中是否存在对应页号的节点，如果存在则直接返回
        if (bpNode != null) {
            return bpNode;
        }
        BpPage bpPage = (BpPage) fStore.readPageFromFile(pageNo, true);//从文件存储中读取指定页号的页数据，并将其转换为 BpPage 对象，然后调用 readFromPage 方法从页数据中读取节点，并进行初始化
        bpNode = bpPage.readFromPage(this);
        if (bpNode.isRoot()) {
            root = bpNode;
        }
        if (bpNode.isLeaf() && bpNode.getPrevious() == null) {
            head = bpNode;
        }
        return bpNode;
    }

    @Override
    public Cursor searchEqual(IndexEntry key) {
        Position startPos = getFirst(key, CompareType.EQUAL);
        if (startPos == null) {
            return null;
        }
        startPos.setSearchEntry(key);
        if (isPrimaryKey) {
            return new ClusterIndexCursor(startPos, null, true);
        } else {
            SecondIndexCursor cursor = new SecondIndexCursor(startPos, null, true);
            cursor.setClusterIndex(table.getClusterIndex());
            return cursor;
        }
    }

    @Override
    public Cursor searchRange(IndexEntry lowKey, IndexEntry upKey) {
        Position startPos = getFirst(lowKey, CompareType.LOW);
        if (startPos == null) {
            return null;
        }
        Position endPos = null;
        if (upKey != null) {
            startPos.setSearchEntry(lowKey);
            if (upKey != null) {
                endPos = getLast(upKey, CompareType.UP);
            }
            if (endPos != null) {
                endPos.setSearchEntry(upKey);
            }
        }
        if (isPrimaryKey) {
            return new ClusterIndexCursor(startPos, endPos, false);
        } else {
            SecondIndexCursor cursor = new SecondIndexCursor(startPos, endPos, false);
            cursor.setClusterIndex(table.getClusterIndex());
            return cursor;
        }
    }

    @Override
    public Position getFirst(IndexEntry outKey, int CompareType) {
        IndexEntry key = buildEntry(outKey);
        Position position = root.get(key.getCompareEntry(), CompareType);
        if (position == null) {
            return null;
        }
        // 由于存在key大量一样的情况,所以必须往前遍历,因为前面也可能有相同的key;
        BPNode bpNode = position.getBpNode().getPrevious();
        while (bpNode != null) {
            // 从后往前倒查找
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                IndexEntry item = bpNode.getEntries().get(i);
                if (item.compareIndex(key) == 0) {
                    position.setBpNode(bpNode);
                    position.setPosition(i);
                }
                if (!item.equals(key)) {
                    break;
                }
            }
            bpNode = bpNode.getPrevious();
        }
        return position;
    }

    @Override
    public Position getLast(IndexEntry outKey, int compareType) {
        IndexEntry key = buildEntry(outKey);
        Position position = root.get(key.getCompareEntry(), compareType);
        if (position == null) {
            return null;
        }
        // 由于存在key一样的情况,所以必须往后遍历,因为前面也可能有相同的key;
        BPNode bpNode = position.getBpNode().getNext();
        while (bpNode != null) {
            boolean notEqualFound = false;
            // 从前往后查找
            for (int i = 0; i < bpNode.entries.size(); i++) {
                IndexEntry item = bpNode.getEntries().get(i);
                if (item.compareIndex(key) == 0) {
                    position.setBpNode(bpNode);
                    position.setPosition(i);
                }
                if (!item.equals(key)) {
                    notEqualFound = true;
                    break;
                }
            }
            if (notEqualFound) {
                break;
            } else {
                bpNode = bpNode.getNext();
            }
        }
        return position;
    }

    // 遍历当前bpNode以及之后的node
    @Override
    public List<IndexEntry> getAll(IndexEntry key) {
        Position res = getFirst(key, CompareType.LOW);
        List<IndexEntry> list = new ArrayList<IndexEntry>();
        BPNode bpNode = res.getBpNode();
        BPNode initNode = res.getBpNode();
        while (bpNode != null) {
            for (IndexEntry indexEntry : bpNode.getEntries()) {
                if (indexEntry.compareIndex(key) == 0) {
                    list.add(indexEntry);
                } else {
                    // 这边对initNode做特殊处理的原因是
                    // 需要将计算出来的firstNode中的等值ke加进来
                    if (initNode != bpNode) {
                        break;
                    }
                }
            }
            bpNode = bpNode.getNext();
        }
        return list;
    }

    /**
     * Gets node map.
     *
     * @return the node map
     */
    public Map<Integer, BPNode> getNodeMap() {
        return nodeMap;
    }

    /**
     * Sets node map.
     *
     * @param nodeMap the node map
     * @return the node map
     */
    public BPTree setNodeMap(Map<Integer, BPNode> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    /**
     * Inner remove boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean innerRemove(IndexEntry key) {
        return root.remove(key, this);
    }

    @Override
    public int remove(IndexEntry key) {
        int count = 0;
        while (true) {
            if (!innerRemove(key)) {
                break;
            }
            count++;
        }
        return count;
    }

    @Override
    public boolean removeOne(IndexEntry entry) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        return innerRemove(matchIndexEntry);
    }

    @Override
    public void insert(IndexEntry entry, boolean isUnique) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        root.insert(matchIndexEntry, this, isUnique);
    }

    @Override
    public void delete(IndexEntry entry) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        root.remove(matchIndexEntry, this);
    }

    @Override
    public void flushToDisk() {
        writeMetaPage();
        // 深度遍历
        root.flushToDisk(fStore);
    }

    /**
     * Write meta page.
     */
// MetaPage for root page no
    public void writeMetaPage() {
        Page page = PagePool.getIntance().getFreePage();
        page.writeItem(new Item(BpPage.genTupleInt(root.getPageNo())));
        fStore.writePageToFile(page, 0);
    }

    /**
     * Gets root.
     *
     * @return the root
     */
    public BPNode getRoot() {
        return root;
    }

    /**
     * Sets root.
     *
     * @param root the root
     * @return the root
     */
    public BPTree setRoot(BPNode root) {
        this.root = root;
        return this;
    }

    /**
     * Gets head.
     *
     * @return the head
     */
    public BPNode getHead() {
        return head;
    }

    /**
     * Sets head.
     *
     * @param head the head
     * @return the head
     */
    public BPTree setHead(BPNode head) {
        this.head = head;
        return this;
    }

}
