package com.hlin.counter.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 分片的redis客户端
 * 
 * @author hailin0@yeah.net
 * @createDate 2016年3月3日
 * 
 */
public class JedisManager {

    private static final Logger log = LoggerFactory.getLogger(JedisManager.class);

    private ShardedJedisPool shardedJedisPool;

    /**
     * @param shardedJedisPool
     */
    public JedisManager(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }


    /**
     * del
     * 
     * @param key
     */
    public boolean del(String key) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            return shardedJedis.del(key) == 1;
        } catch (Exception e) {
            shardedJedis.close();
            return false;
        }
    }


    /**
     * 单存redis
     *
     * @param key
     * @param obj
     * @return
     */
    public String setStringValue(String key, String obj, int expireTime) {
        String result = "";
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return result;
        }
        try {
            shardedJedis.setex(key, expireTime, obj);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }


    /**
     * 单存redis
     *
     * @param key
     * @param obj
     * @return
     */
    public String setStringValue(String key, String obj) {
        String result = "";
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return result;
        }
        try {
            shardedJedis.set(key, obj);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }



    /**
     * 计数器
     *
     * @param key
     * @param increment
     * @param expireTime
     * @return
     */
    public long incr(String key, long increment, int expireTime) {
        long result = 0l;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return result;
        }
        try {
            result = shardedJedis.incrBy(key, increment);
            shardedJedis.expire(key, expireTime);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 计数器
     *
     * @param key
     * @param increment
     * @return
     */
    public long incr(String key, long increment) {
        long result = 0l;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return result;
        }
        try {
            result = shardedJedis.incrBy(key, increment);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 原子操作，设置新值返回旧值
     *
     * @param key
     * @param newValue
     * @return
     */
    public long getset(String key, Integer newValue) {
        long result = 0l;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return result;
        }
        try {
            String get = shardedJedis.getSet(key, String.valueOf(newValue));
            result = Long.valueOf(get);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 单个数据
     *
     * @param cacheKey
     * @return
     */
    public String getStringValue(String cacheKey) {
        String b = null;
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        if (shardedJedis == null) {
            return null;
        }
        try {
            b = shardedJedis.get(cacheKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shardedJedisPool.close();
        }
        return b;
    }
}