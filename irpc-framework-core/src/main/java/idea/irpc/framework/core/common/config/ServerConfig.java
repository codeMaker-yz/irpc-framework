package idea.irpc.framework.core.common.config;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 18:48
 */
public class ServerConfig {
    private Integer serverPort;

    private String registerAddr;

    private String applicationName;

    /**
     * 服务端序列化方式 example： hession2,kryo,jdk,fastJson
     */
    private String serverSerialize;

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServerSerialize() {
        return serverSerialize;
    }

    public void setServerSerialize(String serverSerialize) {
        this.serverSerialize = serverSerialize;
    }
}
