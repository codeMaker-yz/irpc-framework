package idea.irpc.framework.core.common.event.listener;

import idea.irpc.framework.core.client.ConnectionHandler;
import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.event.IRpcListener;
import idea.irpc.framework.core.common.event.IRpcUpdateEvent;
import idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import idea.irpc.framework.core.common.utils.CommonUtils;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/12 15:06
 */
public class ServiceUpdateListener implements IRpcListener<IRpcUpdateEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);
    @Override
    public void callBack(Object t) {
        //��ȡ���ӽڵ��������Ϣ
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper)t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        if(CommonUtils.isEmptyList(channelFutureWrappers)){
            LOGGER.error("[ServiceUpdateListener] channelFutureWrapper is empty");
            return;
        } else {
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
            for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers){
                String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                //����ϵ�urlû�У�˵���Ѿ����Ƴ���
                if (!matchProviderUrl.contains(oldServerAddress)) {
                    continue;
                } else {
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }
            //��ʱ�ϵ�url�Ѿ����Ƴ��ˣ���ʼ����Ƿ����µ�url
            //ChannelFutureWrapper��ʵ��һ���Զ���İ�װ�࣬��netty�����õ�ChannelFuture����һЩ��װ
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
            for (String newProviderUrl : matchProviderUrl) {
                if (!finalUrl.contains(newProviderUrl)) {
                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setPort(port);
                    channelFutureWrapper.setHost(host);
                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = ConnectionHandler.createChannelFuture(host,port);
                        channelFutureWrapper.setChannelFuture(channelFuture);
                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
            //���ո��·���������
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(),finalChannelFutureWrappers);
        }


    }
}
