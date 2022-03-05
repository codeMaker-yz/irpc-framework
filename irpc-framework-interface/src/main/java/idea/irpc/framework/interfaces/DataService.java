package idea.irpc.framework.interfaces;

import java.util.List;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 13:07
 */
public interface DataService {

    /**
     * 发送数据
     *
     * @param body
     */
    String sendData(String body);

    /**
     * 获取数据
     *
     * @return
     */
    List<String> getList();
}
