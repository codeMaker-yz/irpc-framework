package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.registy.URL;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/19 16:02
 */
public interface IRouter {


    /**
     * ˢ��·������
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * ��ȡ����������ͨ��
     *
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * ����Ȩ����Ϣ
     *
     * @param url
     */
    void updateWeight(URL url);
}
