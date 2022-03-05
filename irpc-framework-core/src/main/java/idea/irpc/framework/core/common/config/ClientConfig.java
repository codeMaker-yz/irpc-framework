package idea.irpc.framework.core.common.config;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:39
 */
public class ClientConfig {
    private String ServerAddr;
    private Integer port;

    public String getServerAddr() {
        return ServerAddr;
    }

    public void setServerAddr(String serverAddr) {
        ServerAddr = serverAddr;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
