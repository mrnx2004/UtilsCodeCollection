package com.personal.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.zip.ZipFile;

public class ZipUtil {
    private ZipUtil() {
    }


    //这里使用ant.jar下的提供的zipFile类
    public static String unzip(File zipFile, String unzipPackPath) throws IOException {
        ZipFile convertedZipFile = new ZipFile(zipFile, "GBK");
        return unZipPacks(unzipPackPath, convertedZipFile);
    }

    public static void zip(String zipFilePath, File inputFile) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));
        BufferedOutputStream bo = new BufferedOutputStream(out);
        zip(out, inputFile, inputFile.getName(), bo);
        out.close();
    }

    private static void zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws Exception {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files.length == 0) {
                out.putNextEntry(new ZipEntry(base + '/'));
            }
            for (int i = 0; i < files.length; i++) {
                zip(out, files[i], base + '/' + files[i].getName(), bo);
            }
        } else {
            dealWithUnzippedFile(out, f, base, bo);
        }
    }

    private static void dealWithUnzippedFile(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws IOException {
        out.putNextEntry(new ZipEntry(base));
        try (BufferedInputStream bi = new BufferedInputStream(new FileInputStream(f))) {
            int b;
            while ((b = bi.read()) != -1) {
                bo.write(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bo.flush();
        }
    }

    private static String unZipPacks(String path, ZipFile zipFile) {
        File file = null;
        HashSet<String> rootPathSet = new HashSet<>();
        try {
            int bufSize = 1024;
            byte[] buf = new byte[bufSize];
            Enumeration<ZipEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String srcPath = (path).replaceAll("\\\\", "/");
                rootPathSet.add(getRootPath(entryName));
                file = new File(srcPath + "/" + entryName);
                if (entry.isDirectory()) {
                    file.mkdirs();//mkdirs会自动创建父目录，出错会返回false.  Question
                } else {
                    dealWithZippedFile(zipFile, file, buf, entry);
                }
            }
            zipFile.close();

            if (rootPathSet.size() > 1) {
                return null;
            }

            Iterator<String> iterator = rootPathSet.iterator();
            return iterator.next();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void dealWithZippedFile(ZipFile zipFile, File file, byte[] buf, ZipEntry entry) throws IOException {
        int readBytes;
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream inputStream = zipFile.getInputStream(entry);
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            while ((readBytes = inputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, readBytes);
            }
        }
    }

    private static String getRootPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "/";
        }
        String[] hierarchyPathArr = path.split("/");
        return hierarchyPathArr[0];
    }
}
