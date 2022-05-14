package idea.irpc.framework.consumer.springboot.controller;

import idea.irpc.framework.interfaces.OrderService;
import idea.irpc.framework.interfaces.good.GoodRpcService;
import idea.irpc.framework.interfaces.pay.PayRpcService;
import idea.irpc.framework.interfaces.user.UserRpcService;
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
@RequestMapping(value = "/api-test")
public class ApiTestController {

    /**
     * 验证各类参数配置是否异常
     */
    @IRpcReference
    private UserRpcService userRpcService;

    @IRpcReference
    private GoodRpcService goodRpcService;

    @IRpcReference
    private PayRpcService payRpcService;


    @GetMapping(value = "/do-test1")
    public boolean doTest1(){
        System.out.println("111");
        return true;
    }

    @GetMapping(value = "/do-test")
    public boolean doTest(){
        long begin1 = System.currentTimeMillis();
        userRpcService.getUserId();
        long end1 = System.currentTimeMillis();
        System.out.println("userRpc----->" + (end1 - begin1) + "ms");
        long begin2 = System.currentTimeMillis();
        goodRpcService.decreaseStock();
        long end2 = System.currentTimeMillis();
        System.out.println("goodRpc----->" + (end2 - begin2) + "ms");
        long begin3 = System.currentTimeMillis();
        payRpcService.doPay();
        long end3 = System.currentTimeMillis();
        System.out.println("payRpc----->" + (end3 - begin3) + "ms");
        return true;
    }

    @GetMapping("/do-test-2")
    public void doTest2(){
        String userId = userRpcService.getUserId();
        System.out.println("userRpcService result: " + userId);
        boolean goodResult = goodRpcService.decreaseStock();
        System.out.println("goodRpcService result: " + goodResult);
    }
}
