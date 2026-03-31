package com.starrysky.lifemini;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.starrysky.lifemini.mapper")
@EnableCaching//SpringCache
@EnableTransactionManagement//事务管理
@EnableScheduling//定时任务
@EnableAsync
//@EnableAspectJAutoProxy(exposeProxy = true)//暴露代理
public class LifeMiniApplication {//标记
    public static void main(String[] args) {
        SpringApplication.run(LifeMiniApplication.class, args);
    }

}
