package com.maxbill.tool;

import java.util.UUID;

public class KeyUtil {

    /**
     * 生成32位的UUid
     */
    public static String getUUIDKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println("ss[" + i + "]=====" + getUUIDKey());
        }
    }

}
