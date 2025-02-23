package alchemystar.freedom.engine.net.handler.frontend;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 前端连接收集器
 *
 * @Author lizhuyang
 */
public class FrontendGroupHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FrontendGroupHandler.class);

    public static ConcurrentHashMap<Long, FrontendConnection> frontendGroup = new ConcurrentHashMap<Long,
            FrontendConnection>();

    protected FrontendConnection source;

    public FrontendGroupHandler(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        frontendGroup.put(source.getId(), source);
        ctx.fireChannelActive();//传递事件给管道中的下一个处理器
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        frontendGroup.remove(source.getId());
        source.close();
        ctx.fireChannelInactive();//通知管道中的其他处理器通道已经关闭
    }

}
