package idea.irpc.framework.consumer.springboot.controller;

import idea.irpc.framework.interfaces.OrderService;
import idea.irpc.framework.interfaces.UserService;
import idea.irpc.framework.spring.starter.common.IRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/13 16:11
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
    @IRpcReference
    private UserService userService;

    /**
     * 验证各类参数配置是否异常
     */
    @IRpcReference(group = "order-group", serviceToken = "order-token")
    private OrderService orderService;


    @GetMapping(value = "/test")
    public void test(){
        userService.test();
    }

    @GetMapping(value = "/testMaxData")
    public String testMaxData(int i){
        String result = orderService.testMaxData(i);
        System.out.println(result.length());
        return result;
    }

    @GetMapping(value = "/get-order-no")
    public List<String> getOrderNo(){
        List<String> result =  orderService.getOrderNoList();
        System.out.println(result);
        return result;
    }
}
