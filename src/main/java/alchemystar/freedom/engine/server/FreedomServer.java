package alchemystar.freedom.engine.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.freedom.config.SocketConfig;
import alchemystar.freedom.engine.Database;
import alchemystar.freedom.engine.net.handler.factory.FrontHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 无毁的湖光 启动器
 *
 * @Author lizhuyang
 */
public class FreedomServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(FreedomServer.class);
    public static final int BOSS_THREAD_COUNT = 1;

    public static void main(String[] args) {
        FreedomServer server = new FreedomServer();
        try {
            server.start();
            while (true) {
                try {
                    Thread.sleep(1000 * 300);
                }catch (Exception e){
                    // just ignore it
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("Start Freedom");
        startServer();
    }

    public void startServer() {
        // acceptor , one port => one thread
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_THREAD_COUNT);//创建一个用于处理新连接的线程组。这个组只负责接收新的连接
        // worker
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 创建一个用于处理已经建立连接的数据传输的线程组。

        try {
            // Freedom Server
            Database database = Database.getInstance();
            ServerBootstrap b = new ServerBootstrap();
            // 这边的childHandler是用来管理accept的
            // 由于线程间传递的是byte[],所以内存池okay
            // 只需要保证分配ByteBuf和write在同一个线程(函数)就行了
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)//设置用于实例化新通道的类以接受传入的连接
                    .option(ChannelOption.SO_BACKLOG, 1024)//设置TCP的参数，此处的 SO_BACKLOG 表示系统用于临时存放已完成三次握手的请求的队列的最大长度
                    .childHandler(new FrontHandlerFactory()).option(ChannelOption.ALLOCATOR,
                    PooledByteBufAllocator.DEFAULT)//设置内存分配器，此处使用池化的 ByteBuf 分配器以优化内存使用
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SocketConfig.CONNECT_TIMEOUT_MILLIS)
                    .option(ChannelOption.SO_TIMEOUT, SocketConfig.SO_TIMEOUT);
            ChannelFuture f = b.bind(database.getServerPort()).sync();//启动服务器并绑定到从数据库配置中获取的端口上，使用 .sync() 确保在绑定完成前方法不会返回
            f.channel().closeFuture().sync();//等待服务器通道关闭，这是一个阻塞操作

        } catch (InterruptedException e) {
            logger.error("监听失败" + e);
        }
    }

}
