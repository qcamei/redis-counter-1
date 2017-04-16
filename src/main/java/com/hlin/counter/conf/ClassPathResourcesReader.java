package com.hlin.counter.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 
 * 加载classpath目录下文件资源
 * 
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年8月5日
 * 
 */
public class ClassPathResourcesReader {

    private static Logger logger = LoggerFactory.getLogger(ClassPathResourcesReader.class);

    public static void main(String[] args) throws Throwable {

        // 获取流中内容
        InputStream in = getResource("conf.properties");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        List<String> resourcePath = getClassPathResourcePath("field-conf", "", "");
        System.out.println(resourcePath);
    }

    /**
     * 获取classpath下资源
     * <p>
     * 例如：
     * <p>
     * conf.xml
     * <p>
     * conf/conf.xml
     * 
     * @param resourcePath
     * @return
     */
    public static InputStream getResource(String resourcePath) {
        return isJar() ? getJarResourceAsStream(resourcePath) : getResourceAsStream(resourcePath);
    }

    /**
     * 批量获取classpath下资源，返回文件名
     * 
     * @param subPath
     * @param prefix
     * @param suffix
     * @return
     */
    public static List<String> getResourcePath(String subPath, String prefix, String suffix) {
        return isJar() ? getJarResourcePath(subPath, prefix, suffix) : getClassPathResourcePath(
                subPath, prefix, suffix);
    }

    /**
     * 判断当前环境是否为jar
     * 
     * @return
     */
    public static boolean isJar() {
        return getPath().endsWith(".jar") ? true : false;
    }

    /**
     * 获取当前路径
     * 
     * @return
     */
    private static String getPath() {
        CodeSource codeSource = ClassPathResourcesReader.class.getProtectionDomain()
                .getCodeSource();
        String path = codeSource.getLocation().getPath();
        logger.info("current the path is :{}", path);
        return path;
    }

    /**
     * 获取当前classpath中的资源流
     * 
     * @return
     */
    private static InputStream getResourceAsStream(String resourcePath) {
        //ClassPathResourcesReader.class.getClassLoader().getResourceAsStream(resourcePath);
        File file = new File(getPath() + resourcePath);
        InputStream resourceStream;
        try {
            resourceStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return resourceStream;
    }

    /**
     * 获取当前classpath中的资源流-jar
     * 
     * @return
     */
    private static InputStream getJarResourceAsStream(String resourcePath) {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        return ClassPathResourcesReader.class.getResourceAsStream(resourcePath);
    }

    /**
     * 批量返回目录下文件名
     * 
     * @param subPath
     * @param prefix
     * @param suffix
     * @return
     */
    private static List<String> getClassPathResourcePath(String subPath, String prefix,
            String suffix) {
        //String path = ClassPathResourcesReader.class.getClassLoader().getResource(subPath).getPath();
        String path = new File(getPath() + subPath).getPath();
        File file = new File(path);
        if (file.isDirectory()) {
            ArrayList<Object> files = Lists.newArrayList();
            String[] list = file.list();
            for (String filePath : list) {
                if (StringUtils.isBlank(prefix) || !filePath.startsWith(prefix)) {
                    continue;
                }
                if (StringUtils.isBlank(suffix) || !filePath.endsWith(suffix)) {
                    continue;
                }
                files.add(filePath);
            }

        }
        return Lists.newArrayList();
    }

    /**
     * 获取jar中子文件目录下全部文件路径（完整包路径）
     * <p>
     * 例如：
     * </p>
     * com/cn/1.xml<br>
     * com/cn/2.xml<br>
     * conf/1.txt<br>
     * conf/2.txt<br>
     * 
     * @param subPath jar中指定的子目录,"/"表示根目录
     * @param prefix 前缀
     * @param suffix 后缀
     */
    private static List<String> getJarResourcePath(String subPath, String prefix, String suffix) {
        List<String> retPath = new ArrayList<String>();
        String jarPath = getPath();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<JarEntry> jes = jarFile.entries();
        JarEntry entry;
        while (jes.hasMoreElements()) {
            entry = (JarEntry) jes.nextElement();
            // 过滤我们出满足我们需求的东西，这里的fileName是指向一个具体的文件的对象的完整包路径，比如com/mypackage/test.txt
            if (entry.getName().startsWith(subPath) || "/".equals(subPath)) {
                if (entry.isDirectory()) {
                    // String ename = entry.getName().substring(0, entry.getName().lastIndexOf("/"));
                    // if (!subPath.equals(ename)) {
                    // 对子目录的处理-可选择递归
                    // getFliesFromJarFile(entry.getName());
                    continue;
                    // }
                }
                String fileName = entry.getName();
                if (!"/".equals(subPath)) {
                    fileName = fileName.replaceFirst(subPath, "");
                    if ("/".equals(fileName.substring(0, 1))) {
                        fileName = fileName.substring(1);
                    }
                }
                if (fileName.contains("/")) {
                    continue;
                }
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                if (prefix != null && !"".equals(prefix)) {
                    if (!fileName.startsWith(prefix)) {
                        continue;
                    }
                }
                if (suffix != null && !"".equals(suffix)) {
                    if (!fileName.endsWith(suffix)) {
                        continue;
                    }
                }

                retPath.add("/" + entry.getName());
            }
        }
        return retPath;
    }
}
