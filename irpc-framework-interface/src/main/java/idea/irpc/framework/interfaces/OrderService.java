package idea.irpc.framework.interfaces;

import java.util.List;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/4/13 14:44
 */
public interface OrderService {
    List<String> getOrderNoList();

    String testMaxData(int i);
}
