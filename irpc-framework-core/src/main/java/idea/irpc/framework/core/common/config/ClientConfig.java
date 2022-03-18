package idea.irpc.framework.core.common.config;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:39
 */
public class ClientConfig {
    private String applicationName;

    private String registerAddr;

    private String proxyType;

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
