package idea.irpc.framework.core.router;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/19 16:07
 */
public class Selector {
    /**
     * ��������
     * eg:com.test.DataService
     */
    private String providerServiceName;

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }
}
