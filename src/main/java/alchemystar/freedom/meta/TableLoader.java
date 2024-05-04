package alchemystar.freedom.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;
import alchemystar.freedom.store.page.PagePool;
import alchemystar.freedom.util.ValueConvertUtil;

/**
 * @Author lizhuyang
 */
public class TableLoader {

    public static final int META_PAGE_INDEX = 0;

    public void readAllTable() {
        String path = SystemConfig.FREEDOM_REL_META_PATH;
        File file = new File(path);
        for (File fileItem : file.listFiles()) {
            readTableMeta(fileItem.getPath());
        }
    }

    // 此方法负责读取单个元数据文件，并根据文件中的内容构建 Table 对象。方法首先通过文件路径创建一个 FStore 对象，
    // 然后读取文件中的第一页数据（元信息只有一页）。根据这一页中的信息，它构建了表的属性列表和索引。
    // 这些数据通过 IndexEntry 数组读取，并使用 ValueConvertUtil 将这些值转换为 Attribute 对象。
    // 之后，它还会读取关于表索引的信息，包括是否为唯一索引和是否为主键索引，并根据这些信息构建索引的数据结构（如 BPTree）。
    // 最后，构建的 Table 对象被添加到 TableManager 中
    public void readTableMeta(String path) {
        FStore metaStore = new FStore(path);
        Table table = new Table();
        // 元信息只有一页
        PageLoader loader = metaStore.readPageLoaderFromFile(0);
        List<Attribute> list = new ArrayList<Attribute>();
        // 第一个项是除index之外的meta大小
        int size = loader.getIndexEntries()[0].getValues()[0].getInt();
        String name = loader.getIndexEntries()[1].getValues()[0].getString();
        table.setName(name);
        for (int i = 2; i <= size; i++) {
            IndexEntry entry = loader.getIndexEntries()[i];
            Attribute attr = ValueConvertUtil.convertValue(entry.getValues());
            list.add(attr);
        }
        Attribute[] attributes = list.toArray(new Attribute[list.size()]);
        table.setAttributes(attributes);

        int indexCount = loader.getIndexEntries()[size + 1].getValues()[0].getInt();
        // 第一个是clusterIndex
        int position = size + 2;
        for (int i = 0; i < indexCount; i++) {
            position = readOneIndex(table, loader.getIndexEntries(), position);
        }

        TableManager.addTable(table, false);
    }

    // 读并设置startPosition和index
    //这个方法读取一个索引的信息，并创建一个索引对象。它首先读取索引的基本属性（如名称、是否唯一、是否为主键等），然后根据这些属性构建一个 BPTree 对象。
    // 如果表尚未设置聚簇索引（clusterIndex），则新的 BPTree 成为聚簇索引；
    // 否则，它被添加为一个次级索引
    public int readOneIndex(Table table, IndexEntry[] indexEntries, int startPosition) {
        int primaryItemSize = indexEntries[startPosition].getValues()[0].getInt();
        List<Attribute> list = new ArrayList<Attribute>();
        String name = indexEntries[startPosition + 1].getValues()[0].getString();
        boolean isUnique = indexEntries[startPosition + 2].getValues()[0].getInt() > 0;
        boolean isPrimaryKey = indexEntries[startPosition + 3].getValues()[0].getInt() > 0;
        for (int i = (startPosition + 4); i < startPosition + primaryItemSize + 1; i++) {
            IndexEntry entry = indexEntries[i];
            Attribute attr = ValueConvertUtil.convertValue(entry.getValues());
            list.add(attr);
        }

        BPTree bpTree = new BPTree(table, name, list.toArray(new Attribute[] {}));
        bpTree.setUnique(isUnique);
        bpTree.setPrimaryKey(isPrimaryKey);
        if (table.getClusterIndex() == null) {
            table.setClusterIndex(bpTree);
        } else {
            table.getSecondIndexes().add(bpTree);
        }
        return startPosition + primaryItemSize + 1;
    }

    //此方法将 Table 对象的元数据写回到文件系统。它首先将表的基本信息（如表名、属性列表大小等）写入一个页面，然后写入表的索引信息。
    // 所有这些数据都被写入到 Page 对象中，然后通过 FStore 的 writePageToFile 方法保存回文件
    public void writeTableMeta(Table table) {
        String name = table.getName();
        // tableName
        Item itemName = new Item(new IndexEntry(new Value[] {new ValueString(name)}));
        // for attribute
        List<Item> metaItems = table.getItems();
        Item itemSize = new Item(new IndexEntry(new Value[] {new ValueInt(1 + metaItems.size())}));
        Page page = PagePool.getIntance().getFreePage();
        page.writeItem(itemSize);
        page.writeItem(itemName);
        page.writeItems(metaItems);
        int indexCount = 1 + table.getSecondIndexes().size();
        Item indexCountItem = new Item(new IndexEntry(new Value[] {new ValueInt(indexCount)}));
        page.writeItem(indexCountItem);
        // for indexes
        page.writeItems(table.getClusterIndex().getItems());
        for (BaseIndex baseIndex : table.getSecondIndexes()) {
            page.writeItems(baseIndex.getItems());
        }
        table.getMetaStore().writePageToFile(page, META_PAGE_INDEX);
    }
}
