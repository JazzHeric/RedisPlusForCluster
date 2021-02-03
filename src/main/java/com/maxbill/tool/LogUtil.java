package com.maxbill.tool;

import com.maxbill.core.desktop.LogView;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {

    public static String getExceptionAllinformation(Exception e) {
        StringBuilder outBuffer = new StringBuilder();
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement s : trace) {
            outBuffer.append(s + "\r\n");
        }
        return outBuffer.toString();
    }

    public static String dealException(Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        e.printStackTrace(pout);
        String info = new String(out.toByteArray());
        pout.close();
        try {
            out.close();
        } catch (Exception ex) {
            info = "";
        }
        return info;
    }


    private static String throwableToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }


    public static void setLogMessage(String info) {
        LogView.setLogView(true, info);
    }

    public static void setLogException(Exception e) {
        LogView.setLogView(false, dealException(e));
    }

}
