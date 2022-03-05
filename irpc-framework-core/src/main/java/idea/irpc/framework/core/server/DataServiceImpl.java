package idea.irpc.framework.core.server;

import idea.irpc.framework.interfaces.DataService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 13:11
 */
public class DataServiceImpl implements DataService {
    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success";
    }

    @Override
    public List<String> getList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }
}
