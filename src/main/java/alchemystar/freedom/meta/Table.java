package alchemystar.freedom.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.Index;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.optimizer.Optimizer;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.util.ValueConvertUtil;

/**
 * Table
 *
 * @Author lizhuyang
 */
public class Table {
    // table名称
    private String name;
    // relation包含的元组描述
    private Attribute[] attributes;
    // 属性map
    private Map<String, Integer> attributesMap;
    // 主键属性
    private Attribute primaryAttribute;
    // Relation对应的FilePath
    private String tablePath;
    // Relation对应的metaPath
    private String metaPath;
    // 装载具体数据信息
    private FStore tableStore;
    // 元信息store
    private FStore metaStore;
    // 主键索引,聚簇索引
    private BaseIndex clusterIndex;
    // second索引 二级索引
    private List<BaseIndex> secondIndexes = new ArrayList<BaseIndex>();

    private Optimizer optimizer;

    /**
     * Instantiates a new Table.
     */
    public Table() {
        optimizer = new Optimizer(this);
    }

    /**
     * Search equal cursor.
     *
     * @param entry the entry
     * @return the cursor
     */
    public Cursor searchEqual(IndexEntry entry) {
        // choose index by entry
        Index chooseIndex = optimizer.chooseIndex(entry);
        return chooseIndex.searchEqual(entry);
    }

    /**
     * Search range cursor.
     *
     * @param lowKey the low key
     * @param upKey  the up key
     * @return the cursor
     */
    public Cursor searchRange(IndexEntry lowKey, IndexEntry upKey) {
        // choose index by entry
        Index chooseIndex = optimizer.chooseIndex(lowKey);
        return chooseIndex.searchRange(lowKey, upKey);
    }

    /**
     * Insert.
     *
     * @param entry the entry
     */
// CRUD
    public void insert(IndexEntry entry) {
        // 插入聚集索引
        clusterIndex.insert(entry, true);
        // 二级索引的插入
        for (BaseIndex secondIndex : secondIndexes) {
            secondIndex.insert(entry, false);
        }
    }

    /**
     * Delete.
     *
     * @param entry the entry
     */
    public void delete(IndexEntry entry) {
        // 删除聚集索引
        clusterIndex.delete(entry);
        for (BaseIndex secondIndex : secondIndexes) {
            secondIndex.delete(entry);
        }
    }

    /**
     * Update an entry in the table.
     *
     * @param oldEntry the old entry to be updated
     * @param newEntry the new entry to replace the old one
     */
    public void update(IndexEntry oldEntry, IndexEntry newEntry) {
        if (clusterIndex.getTable().find(oldEntry)!=null) {
            clusterIndex.delete(oldEntry);
            clusterIndex.insert(newEntry, true);
            // 更新二级索引
            for (BaseIndex secondIndex : secondIndexes) {
                secondIndex.delete(oldEntry);
                secondIndex.insert(newEntry, false);
            }
        } else {
            System.out.println("Error: Entry not found for update.");
        }
    }

    /**
     * Find an index entry in the table.
     *
     * @param searchEntry the entry to find
     * @return the matching index entry, or null if not found
     */
    public IndexEntry find(IndexEntry searchEntry) {
        // 使用优化器选择合适的索引
        Index index = optimizer.chooseIndex(searchEntry);

        // 在选定的索引中进行查找
        Cursor cursor = index.searchEqual(searchEntry);

        // 如果找到了匹配的条目，将其返回
        return cursor.next();
    }

    /**
     * Gets attribute index.
     *
     * @param name the name
     * @return the attribute index
     */
    public int getAttributeIndex(String name) {
        return attributesMap.get(name);
    }

    /**
     * Get attributes attribute [ ].
     *
     * @return the attribute [ ]
     */
    public Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * Sets attributes.
     *
     * @param attributes the attributes
     */
    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
        attributesMap = new HashMap<String, Integer>();
        for (int i = 0; i < attributes.length; i++) {
            attributesMap.put(attributes[i].getName(), i);
            if (attributes[i].isPrimaryKey()) {
                primaryAttribute = attributes[i];
            }
        }
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
        if (metaPath == null) {
            metaPath = SystemConfig.FREEDOM_REL_META_PATH + "/" + name;
        }
        if (tablePath == null) {
            tablePath = SystemConfig.FREEDOM_REL_DATA_PATH + "/" + name;
        }
    }

    /**
     * Gets cluster index.
     *
     * @return the cluster index
     */
    public BaseIndex getClusterIndex() {
        return clusterIndex;
    }

    /**
     * Sets cluster index.
     *
     * @param clusterIndex the cluster index
     */
    public void setClusterIndex(BaseIndex clusterIndex) {
        this.clusterIndex = clusterIndex;
    }

    /**
     * Gets second indexes.
     *
     * @return the second indexes
     */
    public List<BaseIndex> getSecondIndexes() {
        return secondIndexes;
    }

    /**
     * Sets second indexes.
     *
     * @param secondIndexes the second indexes
     */
    public void setSecondIndexes(List<BaseIndex> secondIndexes) {
        this.secondIndexes = secondIndexes;
    }

    /**
     * Gets primary attribute.
     *
     * @return the primary attribute
     */
    public Attribute getPrimaryAttribute() {
        return primaryAttribute;
    }

    /**
     * Sets primary attribute.
     *
     * @param primaryAttribute the primary attribute
     */
    public void setPrimaryAttribute(Attribute primaryAttribute) {
        this.primaryAttribute = primaryAttribute;
    }

    /**
     * Load from disk.
     */
// todo persistent
    public void loadFromDisk() {
        // 先不考虑持久化
    }

    /**
     * Flush data to disk.
     */
    public void flushDataToDisk() {
        clusterIndex.flushToDisk();
        for (BaseIndex baseIndex : secondIndexes) {
            baseIndex.flushToDisk();
        }
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        List<Item> list = new LinkedList<Item>();
        for (Attribute attribute : attributes) {
            Value[] values = ValueConvertUtil.convertAttr(attribute);
            IndexEntry tuple = new IndexEntry(values);
            Item item = new Item(tuple);
            list.add(item);
        }
        return list;
    }

    /**
     * Gets meta store.
     *
     * @return the meta store
     */
    public FStore getMetaStore() {
        if (metaStore == null) {
            metaStore = new FStore(metaPath);
        }
        return metaStore;
    }

    /**
     * Sets meta store.
     *
     * @param metaStore the meta store
     */
    public void setMetaStore(FStore metaStore) {
        this.metaStore = metaStore;
    }
}
