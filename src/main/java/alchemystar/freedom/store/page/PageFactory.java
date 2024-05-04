package alchemystar.freedom.store.page;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BpPage;

/**
 * BPFactory
 *
 * @Author lizhuyang
 */
public class PageFactory {

    private static PageFactory factory = new PageFactory();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static PageFactory getInstance() {
        return factory;
    }

    private PageFactory() {
    }

    /**
     * New page page.
     *
     * @return the page
     */
    public Page newPage() {
        return new Page(SystemConfig.DEFAULT_PAGE_SIZE);
    }

    /**
     * New bp page bp page.
     *
     * @param bpNode the bp node
     * @return the bp page
     */
    public BpPage newBpPage(BPNode bpNode) {
        return new BpPage(bpNode);
    }

}
