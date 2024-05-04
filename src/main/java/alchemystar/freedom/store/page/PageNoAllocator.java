package alchemystar.freedom.store.page;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PageNoAllocator
 *
 * @Author lizhuyang
 */
public class PageNoAllocator {

    private AtomicInteger count;

    private Queue<Integer> freePageNoList;//存储已被回收的页面号，可以被重新分配

    /**
     * Instantiates a new Page no allocator.
     */
    public PageNoAllocator() {
        // 0 for meta page
        count = new AtomicInteger(1);
        freePageNoList = new ConcurrentLinkedQueue<Integer>();
    }

    /**
     * Gets next page no.
     *
     * @return the next page no
     */
    public int getNextPageNo() {
        Integer pageNo = freePageNoList.remove();
        return (pageNo != null) ? pageNo : count.getAndIncrement();
    }

    /**
     * Recycle count.
     *
     * @param pageNo the page no
     */
    public void recycleCount(int pageNo) {
        freePageNoList.add(pageNo);
    }

    /**
     * Sets count.
     *
     * @param lastPageNo the last page no
     */
// 从磁盘中,重新构造page的时候,需要重新设置其pageNo
    public void setCount(int lastPageNo) {
        count.set(lastPageNo + 1);
    }
}
