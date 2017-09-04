package com.qzsang.mycacheutil;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.qzsang.cachelib.CacheException;
import com.qzsang.cachelib.CacheUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(this, null);
        

    }

    public void clickPut (View view) {
        put();
    }


    public void clickGet (View view) {
        get();
    }


    public void put () {
        CacheUtil.Builder builder = new CacheUtil.Builder();
        Log.e("boolean", builder.put( "boolean" ,true) + "");
        Log.e("double", builder.put( "double" ,0.1) + "");
        Log.e("integer", builder.put( "integer" ,1) + "");
        Log.e("long", builder.put( "long" ,11L) + "");
        Log.e("Serializable", builder.put( "Serializable" ,new StudentBean("SerializableBean", 18)) + "");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("qzsang","q");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("JSONObject", builder.put( "JSONObject" ,jsonObject) + "");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put("qzsang");
        jsonArray.put("xiaowang");
        Log.e("JSONArray", builder.put( "JSONArray" ,jsonArray) + "");

        //设置定时
        builder.setTime(1L)
                .put("time","我将在一个小时后过期");
    }


    public void get () {
        CacheUtil.Builder builder = new CacheUtil.Builder();

        Log.e("boolean", builder.getBoolean( "boolean" ) + "");
        Log.e("double", builder.getDouble( "double") + "");
        Log.e("integer", builder.getInt( "integer" ) + "");
        Log.e("long", builder.getLong( "long" ) + "");
        Log.e("Serializable", builder.get( "Serializable" ,StudentBean.class) + "");
        Log.e("JSONObject", builder.getJSONObject( "JSONObject" ) + "");
        Log.e("JSONArray", builder.getJSONArray( "JSONArray" ) + "");

        //得到定时的值
        Log.e("time", builder.getString( "time" ) + "");//正常方式得到值： 如果过期 我将得不到值
        builder.setKey("time")
                .getString(new CacheUtil.GetValueCallBack<String>() {//通过回调回去值 ： 如果过期 我也能得到值
                    @Override
                    public void onGetValue(String result, boolean isValid) {
                        Log.e("time", "是否有效:" + isValid + ", result：" + result);
                    }
                });
    }


    //初始化库
    public void init (Context context, String userId) {
        try {
            CacheUtil.getInstance().init(
                    new File(context.getFilesDir().getParentFile(),"best_cache").getAbsolutePath(),//缓存存储目录
                    "cache",//库名字
                    userId);  //初始化CacheUtil
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }


}
