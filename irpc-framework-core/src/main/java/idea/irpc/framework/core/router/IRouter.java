package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.registy.URL;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/19 16:02
 */
public interface IRouter {


    /**
     * 刷新路由数组
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求到连接通道
     *
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     *
     * @param url
     */
    void updateWeight(URL url);
}
