package cn.qihangerp.service.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@EnableDubbo
//@ComponentScan(basePackages={"com.qihang"})
@SpringBootApplication
public class OrderService
{
    public static void main( String[] args )
    {
        System.out.println( "Hello Order-Service!" );
        SpringApplication.run(OrderService.class, args);
    }
}
