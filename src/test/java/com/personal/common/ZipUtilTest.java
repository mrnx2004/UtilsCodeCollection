package com.personal.common;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ZipUtilTest {

    @Test
    public void unzip() throws Exception {
        File file = new File("C:\\Users\\mrnx2\\Desktop\\带复习pdf.zip");
        ZipUtil.unzip(file,"C:\\Users\\mrnx2\\Desktop\\吉他谱");
    }

    @Test
    public void zip() throws Exception{
        File file = new File("C:\\Users\\mrnx2\\Desktop\\带复习pdf");
        ZipUtil.zip("C:\\Users\\mrnx2\\Desktop\\带复习pdf.zip",file);
    }
}