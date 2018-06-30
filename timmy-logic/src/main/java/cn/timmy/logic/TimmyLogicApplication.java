package cn.timmy.logic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableTransactionManagement
@MapperScan("cn.timmy.logic.*.mapper")
public class TimmyLogicApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimmyLogicApplication.class, args);
    }

}
