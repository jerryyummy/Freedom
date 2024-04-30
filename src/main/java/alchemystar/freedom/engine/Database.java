package alchemystar.freedom.engine;

import alchemystar.freedom.meta.TableLoader;
import alchemystar.freedom.recovery.RecoverManager;
import alchemystar.freedom.store.log.LogStore;

/**
 * The type Database.
 *
 * @Author lizhuyang
 */
public class Database {

    private static Database database = null;
    // 默认端口号是8090
    private int serverPort = 8090;
    // 默认用户名密码是pay|miracle
    private String userName = "pay";
    private String passWd = "MiraCle";
    private TableLoader tableLoader;
    private LogStore logStore;

    // 单例模式
    static {
        database = new Database();
        // 加载数据
        TableLoader tableLoader = new TableLoader();
        tableLoader.readAllTable();
        database.setTableLoader(tableLoader);
        LogStore logStore = new LogStore();
        database.setLogStore(logStore);
        RecoverManager recoverManager = new RecoverManager();
        recoverManager.setLogStore(logStore);
        recoverManager.recover();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Database getInstance() {
        return database;
    }

    /**
     * Gets server port.
     *
     * @return the server port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Sets server port.
     *
     * @param serverPort the server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name.
     *
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets pass wd.
     *
     * @return the pass wd
     */
    public String getPassWd() {
        return passWd;
    }

    /**
     * Sets pass wd.
     *
     * @param passWd the pass wd
     */
    public void setPassWd(String passWd) {
        this.passWd = passWd;
    }

    /**
     * Gets table loader.
     *
     * @return the table loader
     */
    public TableLoader getTableLoader() {
        return tableLoader;
    }

    /**
     * Sets table loader.
     *
     * @param tableLoader the table loader
     */
    public void setTableLoader(TableLoader tableLoader) {
        this.tableLoader = tableLoader;
    }

    /**
     * Gets log store.
     *
     * @return the log store
     */
    public LogStore getLogStore() {
        return logStore;
    }

    /**
     * Sets log store.
     *
     * @param logStore the log store
     */
    public void setLogStore(LogStore logStore) {
        this.logStore = logStore;
    }
}
