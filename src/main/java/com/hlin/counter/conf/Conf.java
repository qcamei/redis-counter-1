package com.hlin.counter.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * 配置工具类-单例<br>
 * 数据源为classpath：conf.properties
 * 
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年3月11日
 * 
 */
public final class Conf {

    /**
     * 配置文件名
     */
    private final String CONF_FILE_NAME = "conf.properties";

    // 缓存配置数据
    private Map<String, String> cacheConfig = new HashMap<String, String>();

    /**
     * 当前部署环境根元素
     */
    private String root;

    /**
     * 实例-volatile
     */
    private static volatile Conf conf;

    private Conf() {
        InputStream in = null;
        try {
            in = ClassPathResourcesReader.getResource(CONF_FILE_NAME);
            Properties prop = new Properties();
            prop.load(in);

            // 一次性装载
            Set<Object> keySet = prop.keySet();
            Object value;
            for (Object key : keySet) {
                value = prop.get(key);
                cacheConfig.put(String.valueOf(key), String.valueOf(value));
                // System.out.println(String.valueOf(key) + "=" + String.valueOf(value));
            }
            // root根元素配置
            if (null != cacheConfig.get("root") && !"".equals(cacheConfig.get("root"))) {
                root = cacheConfig.get("root");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取唯一实例
     * 
     * @return
     */
    public static Conf newInstance() {
        if (null == conf) {
            synchronized (Conf.class) {
                if (null == conf) {
                    conf = new Conf();
                }
            }
        }
        return conf;
    }

    /**
     * 基于root根元素的配置获取.<br>
     * key格式：<br>
     * 如配置为:develop.img_upload_server_path 则key为：img_upload_server_path<br>
     * 如配置为:img_upload_server_path 则key为：img_upload_server_path<br>
     * 默认以root.key获取.获取不到则直接根据key获取<br>
     * 如果未配置root元素，则直接以key获取<br>
     * 
     * @param key
     * @return
     */
    public static String getRootPropertie(String key) {
        String value = null;
        // root为前缀获取
        if (null != newInstance().root) {
            value = newInstance().cacheConfig.get(newInstance().root + "." + key);
        }
        // 无前缀.直接key获取
        if (null == value || "".equals(value)) {
            value = newInstance().cacheConfig.get(key);
        }
        return value;
    }

    /**
     * 基于root根元素的配置获取.<br>
     * key格式：<br>
     * 如配置为:develop.img_upload_server_path 则key为：img_upload_server_path<br>
     * 如配置为:img_upload_server_path 则key为：img_upload_server_path<br>
     * 默认以root.key获取.获取不到则直接根据key获取<br>
     * 如果未配置root元素，则直接以key获取<br>
     * 
     * @param key
     * @return
     */
    public static String getString(String key) {
        String value = null;
        // root为前缀获取
        if (null != newInstance().root) {
            value = newInstance().cacheConfig.get(newInstance().root + "." + key);
        }
        // 无前缀.直接key获取
        if (null == value || "".equals(value)) {
            value = newInstance().cacheConfig.get(key);
        }
        if (null == value) {
            throw new RuntimeException("config key is not found ! key:" + key);
        }
        return value;
    }

    /**
     * 获取int
     * 
     * @param key
     * @return
     */
    public static int getInt(String key) {
        String propertie = getString(key);
        if (null == propertie) {
            return 0;
        }
        return Integer.valueOf(propertie);
    }

    /**
     * 获取Long
     * 
     * @param key
     * @return
     */
    public static long getLong(String key) {
        String propertie = getString(key);
        if (null == propertie) {
            return 0;
        }
        return Long.valueOf(propertie);
    }

    /**
     * 获取Boolean
     * 
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
        String propertie = getString(key);
        if (null == propertie) {
            return false;
        }
        return Boolean.valueOf(propertie);
    }

    /**
     * 当前是运行在生产环境
     * 
     * @return
     */
    public static boolean isRuningProduc() {
        boolean b = null == newInstance().root || "".equals(newInstance().root) ? false
                : newInstance().root.endsWith("produc");
        return b;
    }
}
