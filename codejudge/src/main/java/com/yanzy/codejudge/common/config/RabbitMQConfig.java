package com.yanzy.codejudge.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ==================== 常量定义区 (统一管理名称，防止拼写错误) ====================

    // 1. 交换机名称
    public static final String JUDGE_EXCHANGE = "oj.judge.exchange";

    // 2. 队列名称
    public static final String JUDGE_REQUEST_QUEUE = "oj.judge.request.queue";
    public static final String JUDGE_RESPONSE_QUEUE = "oj.judge.response.queue";

    // 3. 路由键 (Routing Key)
    public static final String JUDGE_REQUEST_ROUTING_KEY = "judge.request";
    public static final String JUDGE_RESPONSE_ROUTING_KEY = "judge.response";

    // 代码运行（与判题相同的 request → worker → response 模式）
    public static final String RUN_EXCHANGE = "oj.run.exchange";
    public static final String RUN_REQUEST_QUEUE = "oj.run.request.queue";
    public static final String RUN_RESPONSE_QUEUE = "oj.run.response.queue";
    public static final String RUN_REQUEST_ROUTING_KEY = "run.request";
    public static final String RUN_RESPONSE_ROUTING_KEY = "run.response";


    // ==================== Bean 声明区 ====================

    // --- 1. 声明交换机 ---
    // DirectExchange: 直连模式，路由键必须完全匹配才能投递
    @Bean
    public DirectExchange judgeExchange() {
        // 参数：交换机名称, 是否持久化(重启不丢), 是否自动删除(不常用，通常填false)
        return new DirectExchange(JUDGE_EXCHANGE, true, false);
    }

    // --- 2. 声明队列 ---

    // 判题请求队列 (生产者 -> MQ)
    @Bean
    public Queue judgeRequestQueue() {
        // 参数：队列名称, 是否持久化
        return new Queue(JUDGE_REQUEST_QUEUE, true);
    }

    // 判题结果队列 (MQ -> 消费者)
    @Bean
    public Queue judgeResponseQueue() {
        return new Queue(JUDGE_RESPONSE_QUEUE, true);
    }

    // --- 3. 声明绑定关系 (Binding) ---
    // 这里的逻辑是：把 "队列" 绑定到 "交换机" 上，并指定 "路由键"

    // 绑定：请求队列 <--> 交换机 (通过 judge.request 路由)
    @Bean
    public Binding bindingRequestQueue() {
        return BindingBuilder
                .bind(judgeRequestQueue())       // 绑定哪个队列
                .to(judgeExchange())             // 到哪个交换机
                .with(JUDGE_REQUEST_ROUTING_KEY); // 使用哪个路由键
    }

    // 绑定：结果队列 <--> 交换机 (通过 judge.response 路由)
    @Bean
    public Binding bindingResponseQueue() {
        return BindingBuilder
                .bind(judgeResponseQueue())
                .to(judgeExchange())
                .with(JUDGE_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange runExchange() {
        return new DirectExchange(RUN_EXCHANGE, true, false);
    }

    @Bean
    public Queue runRequestQueue() {
        return new Queue(RUN_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue runResponseQueue() {
        return new Queue(RUN_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding bindingRunRequestQueue() {
        return BindingBuilder
                .bind(runRequestQueue())
                .to(runExchange())
                .with(RUN_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding bindingRunResponseQueue() {
        return BindingBuilder
                .bind(runResponseQueue())
                .to(runExchange())
                .with(RUN_RESPONSE_ROUTING_KEY);
    }
}
