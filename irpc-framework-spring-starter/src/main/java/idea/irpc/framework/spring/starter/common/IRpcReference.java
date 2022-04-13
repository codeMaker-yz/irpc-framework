package idea.irpc.framework.spring.starter.common;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IRpcReference {
    String url() default "";

    //�������
    String group() default "default";

    //���������У��
    String serviceToken() default "";

    //����ĵ��ó�ʱʱ��
    int timeOut() default 3000;

    //�Ƿ��첽����
    boolean async() default false;

}
