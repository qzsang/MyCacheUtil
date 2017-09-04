package com.qzsang.cachelib.impl;


import com.qzsang.cachelib.CacheManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by qzsang on 2017/8/7.
 * 基于  类序列号的实现
 */

public class FileCacheManagerImpl implements CacheManager<File> {


    @Override
    public File open(String cachePath, String cacheName) {

        File file = new File(cachePath + File.separator + cacheName);

        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return file;
    }

    @Override
    public void close(File file) {

    }

    @Override
    public boolean put(File file, String key, Object value) {
        File dataFile = new File(file, key);
        ObjectOutputStream objectOutputStream = getOutput(dataFile);
        if (objectOutputStream == null)
            return false;
        try {
            objectOutputStream.writeObject(value);
            objectOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public <T extends Serializable> T get(File file, String key, Class<T> className) {
        File dataFile = new File(file, key);
        if (!file.exists())
            return null;
        ObjectInputStream objectInputStream = getInput(dataFile);
        if (objectInputStream == null)
            return null;

        try {
            T readObject = (T) objectInputStream.readObject();
            objectInputStream.close();
            return readObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void delete(File file, String key) {
        File dataFile = new File(file, key);
        if (dataFile.isFile() && dataFile.exists()) {
            dataFile.delete();
        }
    }



    private ObjectOutputStream getOutput (File file) {
        try {
            return new ObjectOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private ObjectInputStream getInput (File file) {
        try {
            if (file.exists())
                return new ObjectInputStream(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
