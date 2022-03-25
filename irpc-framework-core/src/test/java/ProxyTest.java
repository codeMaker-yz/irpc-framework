import idea.irpc.framework.core.proxy.ProxyFactory;
import idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import idea.irpc.framework.core.server.DataServiceImpl;
import idea.irpc.framework.interfaces.DataService;
import org.junit.Test;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/18 16:19
 */
public class ProxyTest {
    @Test
    public void test() throws Throwable {
        DataService dataService = new JDKProxyFactory().getProxy(DataService.class);
        dataService.sendData("test");
    }

}
