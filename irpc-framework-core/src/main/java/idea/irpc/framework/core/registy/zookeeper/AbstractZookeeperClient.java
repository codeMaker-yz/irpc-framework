package idea.irpc.framework.core.registy.zookeeper;

import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/9 19:14
 */
public abstract class AbstractZookeeperClient {
    private String zkAddress;
    private int baseSleepTimes;
    private int maxRetryTimes;

    public AbstractZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        //Ĭ��3000ms
        this.baseSleepTimes = 1000;
        this.maxRetryTimes = 3;
    }

    public AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        this.zkAddress = zkAddress;
        if (baseSleepTimes == null) {
            this.baseSleepTimes = 1000;
        } else {
            this.baseSleepTimes = baseSleepTimes;
        }
        if (maxRetryTimes == null) {
            this.maxRetryTimes = 3;
        } else {
            this.maxRetryTimes = maxRetryTimes;
        }
    }

    public int getBaseSleepTimes() {
        return baseSleepTimes;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setBaseSleepTimes(int baseSleepTimes) {
        this.baseSleepTimes = baseSleepTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public abstract void updateNodeData(String address, String data);
    public abstract Object getClient();

    /**
     * ��ȡ�ڵ������
     *
     * @param path
     * @return
     */
    public abstract String getNodeData(String path);

    /**
     * ��ȡָ��Ŀ¼�µ��ֽڵ�����
     *
     * @param path
     * @return
     */
    public abstract List<String> getChildrenData(String path);


    /**
     * �����־û����ͽڵ�������Ϣ
     *
     * @param address
     * @param data
     */
    public abstract void createPersistentData(String address, String data);

    /**
     * @param address
     * @param data
     */
    public abstract void createPersistentWithSeqData(String address, String data);

    /**
     * ������������ʱ���ͽڵ�������Ϣ
     *
     * @param address
     * @param data
     */
    public abstract void createTemporarySeqData(String address, String data);

    /**
     * ������ʱ�ڵ�����������Ϣ
     *
     * @param address
     * @param data
     */
    public abstract void createTemporaryData(String address, String data);

    /**
     * ����ĳ���ڵ����ֵ
     *
     * @param address
     * @param data
     */
    public abstract void setTemporaryData(String address, String data);

    /**
     * �Ͽ�zk�Ŀͻ�������
     */
    public abstract void destroy();

    /**
     * չʾ�ڵ��±ߵ�����
     *
     * @param address
     */
    public abstract List<String> listNode(String address);

    /**
     * ɾ���ڵ��±ߵ�����
     *
     * @param address
     * @return
     */
    public abstract boolean deleteNode(String address);

    /**
     * �ж��Ƿ���������ڵ�
     *
     * @param address
     * @return
     */
    public abstract boolean existNode(String address);

    /**
     * ����path·����ĳ���ڵ�����ݱ仯
     *
     * @param path
     */
    public abstract void watchNodeData(String path, Watcher watcher);


    /**
     * �����ӽڵ��µ����ݱ仯
     *
     * @param path
     * @param watcher
     */
    public abstract void watchChildNodeData(String path, Watcher watcher);


}
