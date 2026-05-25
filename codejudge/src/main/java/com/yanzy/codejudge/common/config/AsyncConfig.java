package com.yanzy.codejudge.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean
    @Primary
    @Override
    public Executor getAsyncExecutor() {
        // 这里直接返回一个简单的线程池，或者返回 null 让 Spring 用默认的
        // 如果返回 null，Spring 会使用 SimpleAsyncTaskExecutor (不推荐生产环境)
        // 所以我们简单配置一个
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}
