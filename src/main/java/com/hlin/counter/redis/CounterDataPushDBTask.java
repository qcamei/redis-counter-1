package com.hlin.counter.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 定时任务，清理计数器数据保存到数据库
 *
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年3月26日
 */
@Component
public class CounterDataPushDBTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterDataPushDBTask.class);

    /**
     * jedis客户端
     */
    @Resource(name = "jedisManager")
    private JedisManager jedisManager;

    /**
     * 计数器列表
     */
    private static CopyOnWriteArrayList<ICounter> counters = new CopyOnWriteArrayList();


    /**
     * 加入计数器
     *
     * @param counter
     */
    public static void addCounter(ICounter counter) {
        counters.add(counter);
    }

    /**
     * 手动保存计数器
     */
    public static void saveCounter() {
        for (ICounter counter : counters) {
            counter.updates();
        }
    }

    /**
     * 同步数据，每5分钟执行一次
     *
     */
    @Scheduled(cron = "0 0/5 *  * * ? ")
    //@Scheduled(cron = "0/5 * *  * * ? ")
    public void syncData() {
        for (ICounter counter : counters) {
            counter.updates();
        }
    }

    /**
     * 删除空key
     *
     */
    //@Scheduled(cron = "0 0/5 *  * * ? ")
    //@Scheduled(cron = "0/5 * *  * * ? ")
    public void delNullKey() {
        for (ICounter counter : counters) {
            Enumeration<String> keys = counter.counterKeys();
            while (keys.hasMoreElements()){
                String val = jedisManager.getStringValue(keys.nextElement());
                if(val != null && val.equals("0")){
                    jedisManager.del(keys.nextElement());
                }
            }
        }
    }

}
