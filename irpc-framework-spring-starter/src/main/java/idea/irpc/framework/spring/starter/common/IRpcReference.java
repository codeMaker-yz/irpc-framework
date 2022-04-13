package idea.irpc.framework.spring.starter.common;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IRpcReference {
    String url() default "";

    //服务分组
    String group() default "default";

    //服务的令牌校验
    String serviceToken() default "";

    //服务的调用超时时间
    int timeOut() default 3000;

    //是否异步调用
    boolean async() default false;

}
