package idea.irpc.framework.core.server;

import idea.irpc.framework.interfaces.UserService;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/4/1 13:53
 */
public class UserServiceImpl implements UserService {
    @Override
    public void test() {
        System.out.println("user_test");
    }
}
