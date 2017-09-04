package com.qzsang.cachelib.impl;


import com.qzsang.cachelib.CacheManager;
import com.snappydb.DB;
import com.snappydb.DBFactory;

import java.io.Serializable;

/**
 * Created by qzsang on 2017/7/14.
 * 基于SnappyDB的实现
 */

public class SnappyCacheManagerImpl implements CacheManager<DB> {


    @Override
    public DB open(String cachePath, String cacheName) {
        DB db = null;
        try {
            db = DBFactory.open(cachePath,cacheName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    @Override
    public void close(DB db) {
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean put(DB db, String key, Object value) {
        try {
            if (db.isOpen()) {
                db.put(key, value);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T extends Serializable> T get(DB db, String key, Class<T> className) {
        T value = null;
        try {
            if (db.isOpen()) {
                value = db.get(key, className);
            }
        } catch (Exception e) {
           // e.printStackTrace();
        }
        return value;
    }

    @Override
    public void delete(DB db, String key) {
        try {
            if (db.isOpen()) {
                db.del(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
