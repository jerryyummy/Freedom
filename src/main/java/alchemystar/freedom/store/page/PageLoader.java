package alchemystar.freedom.store.page;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.store.item.ItemPointer;

/**
 * PageLoader
 * 存储了一页page中所有的tuple
 *
 * @Author lizhuyang
 */
public class PageLoader {

    /**
     * The Page.
     */
    Page page;
    private IndexEntry[] indexEntries;
    private int tupleCount;

    /**
     * Instantiates a new Page loader.
     *
     * @param page the page
     */
    public PageLoader(Page page) {
        this.page = page;
    }

    /**
     * Load.
     */
    /*
    首先读取页面的头数据（通过 PageHeaderData.read(page)），这通常包含有关页面如何组织的元数据，例如元组的数量和页面头的长度。
    通过页面头数据获取元组的数量（tupleCount）和指针开始的偏移量（ptrStartOff），这个偏移量表示元组数据在页面中开始的位置。
    初始化一个 IndexEntry 对象的列表（temp），用于暂存页面中读取的所有有效元组。
    循环读取每个元组：
        从页面中读取元组的位置和长度信息（通过 ItemPointer 对象，它包含元组的偏移量和长度）。
        如果指针表示的元组长度不是 -1（-1 可能表示该元组已被删除或无效），则从指定偏移量读取指定长度的字节数据。
        使用这些字节数据创建 IndexEntry 对象，并调用 indexEntry.read(bb) 方法从字节数据中解析 IndexEntry 的内容。
        将解析后的 IndexEntry 添加到列表 temp 中。
        更新 ptrStartOff 以指向下一个元组的起始位置。
    最后，将列表 temp 转换成数组 indexEntries，并更新 tupleCount 为实际有效的元组数量。
     */
    public void load() {
        PageHeaderData pageHeaderData = PageHeaderData.read(page);
        tupleCount = pageHeaderData.getTupleCount();
        int ptrStartOff = pageHeaderData.getLength();
        // 首先建立存储tuple的数组
        List<IndexEntry> temp = new ArrayList<IndexEntry>();
        // 循环读取
        for (int i = 0; i < tupleCount; i++) {
            // 重新从page读取tuple
            ItemPointer ptr = new ItemPointer(page.readInt(), page.readInt());
            if (ptr.getTupleLength() == -1) {
                continue;
            }
            byte[] bb = page.readBytes(ptr.getOffset(), ptr.getTupleLength());
            IndexEntry indexEntry = new IndexEntry();
            indexEntry.read(bb);
            temp.add(indexEntry);
            // 进入到下一个元组位置
            ptrStartOff = ptrStartOff + ptr.getTupleLength();
        }
        // 由于可能由于被删除,置为-1,所以以temp为准
        indexEntries = temp.toArray(new IndexEntry[temp.size()]);
        tupleCount = temp.size();
    }

    /**
     * Get index entries index entry [ ].
     *
     * @return the index entry [ ]
     */
    public IndexEntry[] getIndexEntries() {
        return indexEntries;
    }

    /**
     * Gets tupl count.
     *
     * @return the tupl count
     */
    public int getTuplCount() {
        return tupleCount;
    }

}
