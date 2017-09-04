一个简单的键值对缓存工具


写这个工具类是为了节约开发效率，其中缓存的具体实现，这个项目中已实现了两种 - FileCacheManagerImpl 、 SnappyCacheManagerImpl，即基于类的序列化  和 基于SnappyDB框架实现的存储，欢迎拓展，定制自己的内部缓存<br>
使用如下<br>

int
```java
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
```

put
```java
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
        
    }

````


get
```java
public void get () {
        CacheUtil.Builder builder = new CacheUtil.Builder();

        Log.e("boolean", builder.getBoolean( "boolean" ) + "");
        Log.e("double", builder.getDouble( "double") + "");
        Log.e("integer", builder.getInt( "integer" ) + "");
        Log.e("long", builder.getLong( "long" ) + "");
        Log.e("Serializable", builder.get( "Serializable" ,StudentBean.class) + "");
        Log.e("JSONObject", builder.getJSONObject( "JSONObject" ) + "");
        Log.e("JSONArray", builder.getJSONArray( "JSONArray" ) + "");
   
    }
````