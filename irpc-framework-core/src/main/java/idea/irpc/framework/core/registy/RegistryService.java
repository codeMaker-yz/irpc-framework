package idea.irpc.framework.core.registy;



/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/9 18:50
 */
public interface RegistryService {
    /**
     * ע��url
     *
     * ��irpc����д��ע�����Ľڵ�
     * ���������綶��ʱ��Ҫ�ʵ�������
     * ע�����url��ʱ����Ҫд��־û��ļ���
     *
     * @param url
     */

    void register(URL url);

    /**
     *
     * ��������
     *
     * �־û��ڵ����޷����з������߲�����
     * ���߷�����뱣֤url������ƥ���
     * �Ƴ��־û��ļ��е�������Ϣ
     *
     * @param url
     */
    void unRegister(URL url);

    /**
     * ���ѷ����ķ���
     *
     * @param url
     */
    void subscribe(URL url);

    /**
     * ִ��ȡ���ڲ����߼�
     * @param url
     */
    void doUnSubscribe(URL url);
}
