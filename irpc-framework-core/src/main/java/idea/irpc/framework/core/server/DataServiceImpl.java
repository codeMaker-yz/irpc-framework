package idea.irpc.framework.core.server;

import idea.irpc.framework.interfaces.DataService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 13:11
 */
@Slf4j
public class DataServiceImpl implements DataService {
    @Override
    public String sendData(String body) {
        log.info("sendData body is " + body + " " + System.currentTimeMillis());
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
