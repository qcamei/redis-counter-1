/**
 * 
 */
package com.hlin.counter.redis;

import java.util.Enumeration;
import java.util.List;

/**
 * 
 * 通用计数器接口
 * 
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年3月26日
 * 
 */
public interface ICounter {

    /**
     * 计数器中field计数+1并返回
     * 
     * @param field
     * @return
     */
    long incr(String field);

    /**
     * 批量更新pv
     */
    void updates();

    /**
     * 单个更新pv
     * 
     * @param field
     */
    void update(String field);

    /**
     * 获取keys
     */
    Enumeration<String> counterKeys();

}