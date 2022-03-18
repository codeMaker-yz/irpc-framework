package idea.irpc.framework.core.registy;



/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/9 18:50
 */
public interface RegistryService {
    /**
     * 注册url
     *
     * 将irpc服务写入注册中心节点
     * 当出现网络抖动时需要适当的重试
     * 注册服务url的时候需要写入持久化文件中
     *
     * @param url
     */

    void register(URL url);

    /**
     *
     * 服务下线
     *
     * 持久化节点是无法进行服务下线操作的
     * 下线服务必须保证url是完整匹配的
     * 移除持久化文件中的内容信息
     *
     * @param url
     */
    void unRegister(URL url);

    /**
     * 消费方订阅服务
     *
     * @param url
     */
    void subscribe(URL url);

    /**
     * 执行取消内部的逻辑
     * @param url
     */
    void doUnSubscribe(URL url);
}
