package com.qzsang.cachelib.bean;


import java.io.Serializable;

/**
 * Created by qzsang on 2017/8/7.
 * 存储的数据类型
 */

public class CacheDataBean implements Serializable {

    private static final long serialVersionUID = -3148192671407191275L;

    public Serializable data;
    public Long endTime;  //结束时间


    /**
     *
     * @param data 设置数据
     * @param hour 设置有效时间  以小时为单位
     */
    public CacheDataBean(Serializable data, Long hour) {
        this.data = data;
        if (hour != null && hour >= 0) {
            this.endTime = System.currentTimeMillis() + hour * 60 * 60 * 1000;
        }
    }
    public CacheDataBean() {
    }
}
