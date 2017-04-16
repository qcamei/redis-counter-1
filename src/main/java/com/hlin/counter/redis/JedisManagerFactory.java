package com.hlin.counter.redis;

import com.hlin.counter.conf.Conf;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * jedis客户端产生工厂
 *
 * @author hailin0@yeah.net
 * @createDate 2016年5月6日
 */
public class JedisManagerFactory {

    private static volatile JedisManager jedisManager;

    public static JedisManager getJedisManager() {
        if (jedisManager == null) {
            synchronized (JedisManagerFactory.class) {
                if (jedisManager == null) {
                    jedisManager = createJedisManager();
                }
            }
        }
        JedisManager jm = jedisManager;
        return jm;
    }

    /**
     * 设置
     *
     * @return
     */
    public static void setJedisManager(JedisManager jm) {
        if (jedisManager == null) {
            synchronized (JedisManagerFactory.class) {
                if (jedisManager == null) {
                    jedisManager = jm;
                }
            }
        }
    }

    /**
     * 根据配置创建
     *
     * @return
     */
    private static JedisManager createJedisManager() {

        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(Conf.getInt("redis.maxIdle"));
        config.setMinIdle(Conf.getInt("redis.minIdle"));
        config.setMaxWaitMillis(Conf.getLong("redis.maxWaitMillis"));
        config.setTestOnBorrow(Conf.getBoolean("redis.testOnBorrow"));
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        // 链接
        shards.add(new JedisShardInfo(Conf.getString("redis.host"), Conf.getInt("redis.port"), Conf
                .getInt("redis.timeout")));

        // 构造池
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config, shards);

        return new JedisManager(shardedJedisPool);
    }
}
