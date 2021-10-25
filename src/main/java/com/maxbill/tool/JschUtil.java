package com.maxbill.tool;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.maxbill.base.bean.Connect;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JschUtil {

    public static JSch jsch;
    public static Session session;

    public static void openSSH(Connect connect) throws Exception {
        LogUtil.setLogMessage("正在打开SSH通道...");
        if (null != session) {
            session.disconnect();
        }
        jsch = new JSch();
        if (!StringUtils.isEmpty(connect.getSpkey())) {
            jsch.addIdentity(connect.getSpkey(), "");
        }
        session = jsch.getSession(connect.getSname(), connect.getShost(), Integer.valueOf(connect.getSport()));
        session.setTimeout(3000);
        if (StringUtils.isEmpty(connect.getSpkey())) {
            session.setPassword(connect.getSpass());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("userauth.gssapi-with-mic", "no");
        String rhost = connect.getRhost();
        //将远程服务和端口 绑定为本地IP:127.0.0.1 端口绑定为：55555（任意本地不可选端口即可）
        session.setPortForwardingL(55555, rhost, Integer.valueOf(connect.getRport()));
        session.connect(3000);

        LogUtil.setLogMessage("已成功打开SSH通道，SSH服务器版本：" + session.getServerVersion());
    }

    public static void closeSSH() {
        try {
            if (null != session) {
                session.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Connect connect = TestJsch.connect;

        char[] passwdChar = "***********".toCharArray();

        try {
            openSSH(connect);

            ChannelShell shell = (ChannelShell) session.openChannel("shell");
            shell.setPtyType("dumb");
            shell.setPty(true);
            shell.connect(60000);
            boolean connected = shell.isConnected();

            OutputStream outputStream = shell.getOutputStream();
            InputStream inputStream = shell.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            //通过流写入命令

/*            Scanner scanner = new Scanner(System.in);

            while(true) {
                System.err.println("输入linux命令:");
                String input = scanner.nextLine();
                System.out.println("输入命令为：" + input);
                outputStream.write((input + "\n").getBytes());
                outputStream.flush();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            }*/

            outputStream.write("pwd\n cd /home\nls\npwd\n".getBytes());
            outputStream.flush();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }


           /* outputStream.write("redis-cli -h *********** -p 6379\n".getBytes());
            //outputStream.write("auth *************\n".getBytes());
            outputStream.flush();

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }


/*        try {
            JschUtil.openSSH(connect);

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(10);
            config.setMaxTotal(50);
            config.setMaxWaitMillis(3000);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);

            URI uri = new URI("redis://:********@****************:6379/0");
            JedisPool jedisPool = new JedisPool(config, uri);

            Jedis jedis = jedisPool.getResource();
            System.out.println(jedis.randomKey());*/

/*            JedisPool jedisPool2 = new JedisPool(config,
                    "************************************",
                    6379, 20000, "**************************");
            Jedis jedis2 = jedisPool2.getResource();
            System.out.println(jedis2.randomKey());*/

/*

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }




}
