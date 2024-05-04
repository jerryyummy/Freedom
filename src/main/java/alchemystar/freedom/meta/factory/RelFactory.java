package alchemystar.freedom.meta.factory;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Table;

/**
 * RelFactory
 *
 * @Author lizhuyang
 */
public class RelFactory {

    private static RelFactory relFactory;

    static {
        relFactory = new RelFactory();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static RelFactory getInstance() {
        return relFactory;
    }

    /**
     * New relation table.
     *
     * @param tableName the table name
     * @return the table
     */
    public Table newRelation(String tableName) {
        Table table = new Table();
        return table;
    }
}
