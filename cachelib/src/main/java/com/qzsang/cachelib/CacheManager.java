package com.qzsang.cachelib;

import java.io.Serializable;

/**
 *
 * Created by qzsang on 2017/7/14.
 * Cache 为缓存的类型
 *  以key - value 的形式
 *
 *  注：写这个接口的目的在于，下次在换缓存框架的时候，可以快速替换，而不用改动原本写好的代码
 */

public interface CacheManager <Cache> {

    /**
     * 获得一个Chache   如果是需要关闭的  请操作结束后调用 close()
     * @param cachePath
     * @param cacheName
     * @return
     */
    Cache open (String cachePath, String cacheName);



    /**
     * 关闭Cache
     * 如果 Cache 类型需要关闭  请针对性重写
     * @return
     */
    void close(Cache cache);

    /**
     *   以key - value  的形式存储
     * @param key  传入的key  将会通过getFinalKey() 生成最终使用的 key
     * @param value
     * @return
     */
    boolean put(Cache cache, String key, Object value);

    /**
     * 获取value
     * @param key 传入的key  将会通过getFinalKey() 生成最终使用的 key
     * @param className 返回类型
     * @param <T>
     * @return
     */
    <T extends Serializable> T get(Cache cache, String key, Class<T> className);

    void delete(Cache cache, String key);


}
