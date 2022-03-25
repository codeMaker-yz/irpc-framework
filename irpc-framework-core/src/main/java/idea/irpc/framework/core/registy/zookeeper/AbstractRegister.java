package idea.irpc.framework.core.registy.zookeeper;

import idea.irpc.framework.core.registy.RegistryService;
import idea.irpc.framework.core.registy.URL;

import java.util.List;
import java.util.Map;

import static idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/9 19:08
 */
public abstract class AbstractRegister implements RegistryService {
    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

    /**
     * 获取服务的权重信息
     * @param serviceName
     * @return
     */
    public abstract Map<String,String> getServiceWeightMap(String serviceName);

    /**
     *
     * @param url
     */

    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

}
