package idea.irpc.framework.core.proxy;



/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:51
 */
public interface ProxyFactory {

    <T> T getProxy(final Class clazz) throws Throwable;

}
