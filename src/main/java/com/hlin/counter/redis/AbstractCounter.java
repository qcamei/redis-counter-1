/**
 *
 */
package com.hlin.counter.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 抽象模板-计数器
 *
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年3月26日
 */
public abstract class AbstractCounter implements ICounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCounter.class);

    /**
     * 计数map
     */
    private ConcurrentHashMap<String, Object> counterKeyMap = new ConcurrentHashMap<String, Object>();

    /**
     * jedis客户端
     */
    @Resource(name = "jedisManager")
    private JedisManager jedisManager;

    /**
     * 获取CounterKey前缀
     *
     * @return
     */
    public abstract String getCounterPrefix();

    /**
     * 获取过期时间,-1为永不过期
     *
     * @return
     */
    protected abstract int getCounterExpireSecond();

    /**
     * 获取DBData过期时间,-1为永不过期
     *
     * @return
     */
    protected abstract int getDBDataExpireSecond();

    /**
     * 子类可选择覆盖，调用更新数据库方法。单纯的计数则不需要覆盖
     *
     * @param field
     * @param val
     * @return
     */
    protected abstract boolean updateDBData(String field, long val);

    /**
     * 子类可选择覆盖，调用查询数据库方法。单纯的计数则不需要覆盖
     *
     * @param field
     * @return
     */
    protected long getDBData(String field) {
        return 0l;
    }

    /**
     * 子类可选择覆盖,获取每次累加的基础增量
     *
     * @return
     */
    protected int getBaseIncrement() {
        return 1;
    }

    /**
     * 子类可选择覆盖，计数器中每个计数最多允许保存多少计数，超过则触发刷新db操作
     *
     * @return
     */
    protected int getCounterCacheMaxDataVal() {
        return 50;
    }

    /**
     * 子类可选择覆盖，计数器是否支持触发异步刷新db操作
     *
     * @return
     */
    protected boolean isAsyncFlushData() {
        return false;
    }

    /**
     * 子类可选择覆盖，当前field是否允许继续计数，比如限定一个用户一次之类的
     *
     * @param field
     * @return
     */
    protected boolean checkField(String field) {
        return true;
    }


    /**
     * 对db取出来的data进行cache
     */
    private long getCacheDBData(String field){
        String key = getCounterKey(field)+"_dbdata";
        String val = jedisManager.getStringValue(getCounterKey(field)+"_dbdata");
        if(StringUtils.isNotBlank(val)){
            val = getDBData(field) + "";
            if(getDBDataExpireSecond() > 0){
                jedisManager.setStringValue(key,val,getDBDataExpireSecond());
            }else{
                jedisManager.setStringValue(key,val);
            }
        }
        return Long.valueOf(val);
    }

    /**
     * 清理DBDataCache
     * @param field
     */
    private void clearDBDataCache(String field){
        String key = getCounterKey(field)+"_dbdata";
        jedisManager.del(key);
    }

    @Override
    public long incr(String field) {
        if (!checkField(field)) {
            return getCacheDBData(field) + getCounterVal(field);
        }

        long incr = incr(field, getBaseIncrement());
        // 异步刷新到db
        final String f_field = field;
        if (isAsyncFlushData() && incr > getCounterCacheMaxDataVal()) {
            update(f_field);
            //可通过线程池更新
            /*new Thread(new Runnable() {
                public void run() {
                    update(f_field);
                }
            }).start();*/
        }
        // 当前计数器值+初始化值=真正的val
        return incr + getCacheDBData(field);
    }

    @Override
    public void updates() {
        Collection<Object> fields = counterKeyMap.values();
        for (Object field : fields) {
            update((String) field);
        }
    }

    /**
     * 单个更新
     *
     * @param field
     */
    @Override
    public void update(String field) {
        long v1 = getCounterVal(field);
        if (v1 <= 0) {
            return;
        }
        long v2 = getSet(field);
        // v1 大于 v2：则表明其他机器已更新过这个key并通过getset设置过新值，当前机器放弃本次更新,并恢复从计数器取出的值v2。
        if (v1 > v2) {
            if (v2 > 0) {
                incr(field, v2);
            }
            return;
        }
        // 将计数器的值更新到数据库，可以考虑trycache这一步调用，在异常时表示更新数据库失败，然后从新将计数量incr到计数器中，以免计数值丢失
        String key = (String) counterKeyMap.remove(getCounterKey(field));
        try {
            updateDBData(key, v2);
        } catch (Exception e) {
            // 从新设置计数值
            if (v2 > 0) {
                incr(field, v2);
                counterKeyMap.put(getCounterKey(field), key);
            }
            LOGGER.error(String.format("清理counter信息出错！field：%s count:%s", field, v2), e);
        }
        clearDBDataCache(field);
    }

    /**
     * 计数器中field计数+increment并返回
     *
     * @param field
     * @param increment
     * @return
     */
    private long incr(String field, long increment) {
        counterKeyMap.putIfAbsent(getCounterKey(field), field);
        if(getCounterExpireSecond() > 0){
            return jedisManager.incr(getCounterKey(field), increment, getCounterExpireSecond());
        }
        return jedisManager.incr(getCounterKey(field), increment);
    }

    /**
     * 原子操作，更新新值返回旧值
     *
     * @param field
     * @return
     */
    private long getSet(String field) {
        return jedisManager.getset(getCounterKey(field), 0);
    }

    /**
     * 获取field计数
     *
     * @param field
     * @return
     */
    private long getCounterVal(String field) {
        String count = jedisManager.getStringValue(getCounterKey(field));
        if (StringUtils.isBlank(count)) {
            return 0;
        }
        return Long.valueOf(count);
    }


    /**
     * 获取完整CounterKey：CounterPrefix() + field
     *
     * @return
     */
    private String getCounterKey(String field) {
        if (field.length() > getCounterPrefix().length()) {
            return field;
        }
        if (field.indexOf(getCounterPrefix()) != -1) {
            return field;
        }
        return getCounterPrefix() + field;
    }

    @Override
    public Enumeration<String> counterKeys(){
        return counterKeyMap.keys();
    }

}