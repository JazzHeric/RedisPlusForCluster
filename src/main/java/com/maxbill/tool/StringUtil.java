package com.maxbill.tool;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static final String FLAG_COLON = ":";

    public static final String FLAG_EQUAL = "=";

    /**
     * 拆分key flag value形式字符返回key
     */
    public static String getKeyString(String falg, String tempStr) {
        if (!StringUtils.isEmpty(tempStr) && appearStringNumber(tempStr, falg) == 1) {
            String[] tempStrArray = tempStr.split(falg);
            return tempStrArray[0];
        } else {
            return "";
        }
    }

    /**
     * 拆分key flag value形式字符返回value
     */
    public static String getValueString(String falg, String tempStr) {
        if (!StringUtils.isEmpty(tempStr) && appearStringNumber(tempStr, falg) == 1) {
            String[] tempStrArray = tempStr.split(falg);
            return tempStrArray[1];
        } else {
            return "";
        }
    }

    /**
     * 获取指定字符串出现的次数
     */
    public static int appearStringNumber(String srcText, String findText) {
        Integer count = 0;
        Pattern pattern = Pattern.compile(findText);
        Matcher matcher = pattern.matcher(srcText);
        while (matcher.find()) {
            count++;
        }
        return count;
    }


    public static void main(String[] args) {

    }

}
