package com.qzsang.cachelib;

import android.text.TextUtils;
import android.util.Log;


import com.qzsang.cachelib.bean.CacheDataBean;
import com.qzsang.cachelib.impl.FileCacheManagerImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;

/**
 * Created by qzsang on 2017/7/14.
 * 缓冲工具类  缓存的逻辑都在这里
 * 没有版本号
 */

public class CacheUtil {
    private CacheManager mCacheManager = null; //缓存管理工具

    private String mCachePath;              //缓存路径
    private String mDBName = "myCache";     //缓存文件名称
    private String mUserId;                 //用户id

    private boolean mIsInitSuccess = false;  //是否已经初始化成功

    public CacheUtil () {
        mCacheManager = new FileCacheManagerImpl();//new SnappyCacheManagerImpl();
        Log.e("CacheUtil", "new CacheManager");
    }
    /**
     * 通过静态内部类的方式获得单例
     * @return
     */
    public static CacheUtil getInstance () {
        return CacheUtilHolder.mCacheUtil;
    }

    private static class CacheUtilHolder {
        private final static CacheUtil mCacheUtil = new CacheUtil();
    }

    /**
     * 初始化
     * @param cachePath 路径
     * @param dbName 库的名称
     * @param userId
     * @throws CacheException
     */
    public synchronized void init (String cachePath, String dbName, String userId) throws CacheException{
        mIsInitSuccess = false;
        if (TextUtils.isEmpty(cachePath)) {
            throw new CacheException("cachePath is null");
        }

        File cacheFile = new File(cachePath);
        if (!cacheFile.exists()) {
            if (!cacheFile.mkdirs()) {
                throw new CacheException("cacheFile mkdirs fail");
            }
        }

        if (!TextUtils.isEmpty(dbName)) {
            this.mDBName = dbName;
        }

        this.mCachePath = cachePath;
        this.mUserId = userId;

        mIsInitSuccess = true;
    }

    /**
     * 是否初始化成功
     * @return
     */
    public boolean isInitSuccess () {
        return mIsInitSuccess && mCacheManager!= null && !TextUtils.isEmpty(mCachePath) && !TextUtils.isEmpty(mDBName) ;
    }


    /**
     * 获取一个db做执行操作
     * @param executeListener
     */
    private  <T> T execute (ExecuteListener<T> executeListener,boolean isNeedUserId) {
        if (executeListener == null || !isInitSuccess ())
            return null;

        String dbName ;
        if (isNeedUserId && !TextUtils.isEmpty(mUserId)) {
            dbName = mDBName + "_" + mUserId;
        } else if (isNeedUserId && TextUtils.isEmpty(mUserId)) {//需要id  userId为空就返回空
            return null;
        } else {
            dbName = mDBName;
        }

        Object db = mCacheManager.open(mCachePath,dbName);
        if (db == null) {
            return null;
        }

       T result = executeListener.onExecute(mCacheManager, db);
        mCacheManager.close(db);
        return result;
    }

    private synchronized <T extends Serializable> boolean put (final String key, final T value, boolean isNeedUserId, final Long hour) {
        if (TextUtils.isEmpty(key) || value == null) {
            return false;
        }

        Boolean result = execute(new ExecuteListener<Boolean>() {
            @Override
            public Boolean onExecute(CacheManager mCacheManager, Object db) {
                try {
                    String finalKey = getFinalKey(key);
                    return mCacheManager.put(db, finalKey, new CacheDataBean(value, hour));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;

            }
        }, isNeedUserId);

        return result == null ? false : result;
    }

    private synchronized boolean delete (final String key, boolean isNeedUserId) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        Boolean result = execute(new ExecuteListener<Boolean>() {
            @Override
            public Boolean onExecute(CacheManager mCacheManager, Object db) {
                try {
                    String finalKey = getFinalKey(key);
                    mCacheManager.delete(db,finalKey);
                    return true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;

            }
        }, isNeedUserId);

        return result == null ? false : result;
    }


    private synchronized <T extends Serializable> T get (final String key, final Class<T> className, final boolean isNeedUserId){
        return get(key, className, isNeedUserId, null);
    }
    private synchronized <T extends Serializable> T get (final String key, final Class<T> className, final boolean isNeedUserId, final GetValueCallBack<T> getValueCallBack) {

        if (TextUtils.isEmpty(key) || className == null) {
            return null;
        }
        //用数据的方式 让final 变成可赋值的
        final boolean [] isValid = {true};
        final CacheDataBean [] cacheDataBeen = {null};

        T  result = execute(new ExecuteListener<T>() {

            @Override
            public T onExecute(CacheManager mCacheManager, Object db) {
                String finalKey = getFinalKey(key);
                cacheDataBeen[0] = (CacheDataBean) mCacheManager.get(db,finalKey, CacheDataBean.class);
                if(cacheDataBeen[0] == null) return null;
                try {
                    //判断是否有效  有效返回  无效删除
                    isValid[0] = isValidTime(cacheDataBeen[0].endTime);
                    if (isValid[0]) {
                        return (T) cacheDataBeen[0].data;
                    }

                    return null;
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        } , isNeedUserId);

        //如果有回调就调用回调
        if (getValueCallBack != null) {
            T value = null;
            if (cacheDataBeen[0] != null) {
                value = (T) cacheDataBeen[0].data;
            }
            getValueCallBack.onGetValue(value, isValid[0]);
        }
        return result ;
    }

    /**
     * 是否是有效的
     * @param time
     * @return
     */
    private boolean isValidTime (Long time) {
        return time == null || time >= System.currentTimeMillis();
    }
    /**
     * 为key做个过度封装
     * @param key
     * @return
     *
     */
    private String getFinalKey(String key) {

        String result = key;
        try {
            if (key.length() > 8) {
                String md5 = getMD5(key);
                String flag = "" + key.charAt(0) + key.charAt(key.length()/2) + key.charAt(1) + key.charAt(key.length()-2) + key.charAt(key.length()-1);
                result = md5 + URLEncoder.encode(flag,"utf-8").replace("%","");
            } else {
                result = URLEncoder.encode(key,"utf-8").replace("%","");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  result;
    }

    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * build类 主要是为了更方便使用
     *
     *  考虑到以后缓存框架可能会改变的情况，
     *  所以 将存储的类型从  Serializable改变，限制成只能存储  String 类型。
     *
     *  注： 存储的类型越少，以后越好维护
     */
    public static class Builder {
        private boolean isNeedUserId = false;//默认不需要userId
        private Long hour;//有效期  以小时为单位
        private String key;//key

        public Builder () {
        }

        public Builder needUserId(boolean needUserId) {
            this.isNeedUserId = needUserId;
            return this;
        }

        public boolean delete(String key) {
            return CacheUtil.getInstance().delete(key, isNeedUserId);
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setTime(Long hour) {
            this.hour = hour;
            return this;
        }

        public  boolean put (String value) {
            return put(key, value);
        }

        public  boolean put (int value) {
            return put(key, value);
        }

        public  boolean put (long value) {
            return put(key, value);
        }

        public  boolean put (boolean value) {
            return put(key, value);
        }

        public boolean put ( JSONObject value) {
            return  put(key, value);
        }

        public boolean put (JSONArray value) {
            return  put(key, value);
        }

        public boolean put (String key, JSONObject value) {
            return put(key, value == null ? null : value.toString());
        }

        public boolean put (String key, JSONArray value) {
            return put(key, value == null ? null : value.toString());
        }


        public String getString (String key) {
            try {
                return get(key, String.class);
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public long getLong (String key) {//类型转换可能出现异常
            try {
                Long value = get(key, Long.class);
                return value == null ? 0 : value;
            }catch (Exception e) {
                e.printStackTrace();
                return  0;
            }
        }

        public double getDouble (String key) {
            try {
                Double value = get(key, Double.class);
                return value == null ? 0 : value;
            }catch (Exception e) {
                e.printStackTrace();
                return  0;
            }
        }

        public int getInt (String key) {
            try {
                Integer value = get(key, Integer.class);
                return value == null ? 0 : value;
            }catch (Exception e) {
                e.printStackTrace();
                return  0;
            }
        }

        public boolean getBoolean (String key) {
            return getBoolean(key, false);
        }

        public boolean getBoolean (String key, boolean def) {
            try {
                Boolean value = get(key, Boolean.class);
                return value == null ? def : value;
            }catch (Exception e) {
                e.printStackTrace();
                return  def;
            }

        }

        public JSONObject getJSONObject (String key) {
            try {
                String value = getString(key);
                return new JSONObject(value);
            }catch (Exception e) {
                return null;
            }
        }

        public JSONArray getJSONArray (String key) {
            try {
                String value = getString(key);
                return new JSONArray(value);
            }catch (Exception e) {
                return null;
            }
        }

        public boolean getBooleanDefaultFalse () {
            return getBoolean(key, false);
        }

        public boolean getBooleanDefaultTrue () {
            return getBoolean(key, true);
        }

        public JSONObject getJSONObject () {
            return getJSONObject(key);
        }

        public JSONArray getJSONArray () {
            return getJSONArray(key);
        }


        public void getString (GetValueCallBack <String> getValueCallBack) {

            CacheUtil.getInstance().get(key, String.class, isNeedUserId, getValueCallBack);
        }




        public void getJSONObject (final GetValueCallBack <JSONObject> getValueCallBack) {
            GetValueCallBack <String> stringGetValueCallBack = new GetValueCallBack<String>() {
                @Override
                public void onGetValue(String result, boolean isValid) {

                    JSONObject jsonObject = null;
                    try {
                        if (!TextUtils.isEmpty(result))
                          jsonObject = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getValueCallBack.onGetValue(jsonObject ,isValid);

                }
            };
            CacheUtil.getInstance().get(key, String.class, isNeedUserId, stringGetValueCallBack);
        }

        public void getJSONArray (final GetValueCallBack <JSONArray> getValueCallBack) {

            GetValueCallBack <String> stringGetValueCallBack = new GetValueCallBack<String>() {
                @Override
                public void onGetValue(String result, boolean isValid) {

                    JSONArray jsonObject = null;
                    try {
                        if (!TextUtils.isEmpty(result))
                            jsonObject = new JSONArray(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getValueCallBack.onGetValue(jsonObject ,isValid);


                }
            };
            CacheUtil.getInstance().get(key, String.class, isNeedUserId, stringGetValueCallBack);
        }

//         // TODO: 2017/8/30  待实现
//        public <T extends Serializable> void get (final GetValueCallBack<T> getValueCallBack) {
//
//            CacheUtil.getInstance().get(key, T.getClass, isNeedUserId, getValueCallBack);
//        }

       public <T extends Serializable>  boolean put (String key, T value) {
           try {
               if (value == null)
                   return delete(key);
               return  CacheUtil.getInstance().put(key, value, isNeedUserId, hour);
           }catch (Exception e) {
               e.printStackTrace();
               return false;
           }

        }


        public <T extends Serializable> T get (String key, Class<T> className) {
            try {
                return  CacheUtil.getInstance().get(key, className, isNeedUserId);
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

    }

    /**
     * 一个执行读取的回调接口
     * @param <T>
     */
    private interface ExecuteListener <T> {
        T onExecute(CacheManager mCacheManager, Object db);
    }

    /**
     * 获取值的赋值回调
     * 当返回的数据需要根据是否过期来处理时
     * @param <T>
     */
    public interface GetValueCallBack <T> {
        /**
         *
         * @param result 结果
         * @param isValid  是否有效  false： 过期  true ： 有效
         */
        void onGetValue(T result, boolean isValid);
    }

}
