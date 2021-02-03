package com.maxbill.tool;

import com.maxbill.base.bean.Connect;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class TelnetUtil {

    public static void main(String[] args) {
        Connect connect = new Connect();
        connect.setRhost("127.0.0.1");
        connect.setRport("6379");
        connect.setRpass("123456");
        connect.setType("0");
        sendCommand(connect, "echo 111");
    }

    /**
     * 发送命令
     */
    public static String sendCommand(Connect connect, String command) {
        try {
            command = command.replaceAll("\\u00A0", " ");
            //指明Telnet终端类型，否则会返回来的数据中文会乱码
            TelnetClient telnetClient = new TelnetClient("vt200");
            //socket延迟时间：5000ms
            telnetClient.setConnectTimeout(5000);
            telnetClient.setDefaultTimeout(5000);
            //建立一个连接
            String host = connect.getRhost();
            int port = Integer.parseInt(connect.getRport());
            if ("1".equals(connect.getType())) {
                JschUtil.openSSH(connect);
                port = 55555;
                if (host.equals(connect.getShost())) {
                    host = "127.0.0.1";
                }
            }
            telnetClient.connect(host, port);
            if (telnetClient.isConnected() && telnetClient.isAvailable()) {
                //读命令的流
                InputStream istream = telnetClient.getInputStream();
                //写命令的流
                PrintStream pstream = new PrintStream(telnetClient.getOutputStream());
                if (!StringUtils.isEmpty(connect.getRpass())) {
                    pstream.println("auth " + connect.getRpass());
                    pstream.flush();
                }
                //发送命令
                pstream.println(command);
                pstream.flush();
                //退出连接
                pstream.println("quit");
                pstream.flush();
                String result = readCommand(istream);
                telnetClient.disconnect();
                //修正返回数据
                result = result.replaceAll("\r\n", "<br/>");
                result = result.replaceAll("\\+OK", "");
                if (result.startsWith("<br/>")) {
                    int length = result.length();
                    result = result.substring(5, length);
                }
                result = result.substring(0, result.length() - 5);
                JschUtil.closeSSH();
                return result;
            } else {
                return "连接已断开...";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 解析数据
     */
    private static String readCommand(InputStream inputStream) {
        try {
            //1创建字节数组输出流，用来输出读取到的内容
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            //2创建缓存大小
            byte[] buffer = new byte[10240];
            //3开始读取输入流中的内容
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, len);
            }
            //4把字节数组转换为字符串
            String content = result.toString();
            //5关闭输入流和输出流
            result.close();
            //6返回字符串结果
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
