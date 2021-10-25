package com.maxbill.tool;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.maxbill.base.bean.Connect;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author: chi.zhang
 * @date: created in 2021/10/25/0025 10:36
 * @description:
 */
public class TestJsch {

    public static Connect connect;

    static {
        connect = new Connect();
        //是不是集群
        connect.setIsha("0");
        //redis域名
        connect.setRhost("redis域名或ip");
        //redis端口号
        connect.setRport("redis端口号");
        //redis密码
        connect.setRpass("redis密码");
        //ssh相关设置
        connect.setType("1");
        connect.setShost("跳板机域名/ip");
        connect.setSport("跳板机端口号");
        connect.setSname("跳板机姓名");
        connect.setSpass("跳板机密码");
    }


    public static Channel connect(Connect connect) throws JSchException
    {

        Session sshSession = null;
        JSch jsch = new JSch();
        sshSession = jsch.getSession(connect.getSname(), connect.getShost(), Integer.valueOf(connect.getSport()));

        sshSession.setPassword(connect.getSpass());
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        sshConfig.put("PreferredAuthentications",
                "password,keyboard-interactive");
        sshSession.setConfig(sshConfig);

        //可设置超时时间
        sshSession.connect(2000);
        System.out.println(sshSession.getServerVersion());
        //此处开始为端口映射到本地的部分
        sshSession.setPortForwardingL(55555, connect.getRhost(), Integer.valueOf(connect.getRport()));
        //完成上诉映射之后，即可通过本地端口连接了
        Session session = jsch.getSession("", "127.0.0.1",55555);
        Properties remoteCfg = new Properties();
        remoteCfg.put("StrictHostKeyChecking", "no");
        remoteCfg.put("PreferredAuthentications",
                "password,keyboard-interactive");
        session.setConfig(remoteCfg);
        session.setPassword(connect.getRpass());
        session.connect();
        System.out.println(session.getServerVersion());
        //创建sftp通信通道
        Channel channel = session.openChannel("session");
        channel.connect();
        return channel;
    }


    public static Jedis getJedis(Connect connect) throws Exception {
        JschUtil.openSSH(connect);
        JedisPoolConfig jedisConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisConfig, "127.0.0.1", 55555, 10000, connect.getRpass());
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

    public static StatefulRedisConnection getLettuce(Connect connect) throws Exception {
        JschUtil.openSSH(connect);
        char[] passwdChar = connect.getRpass().toCharArray();
        RedisURI redisUri = RedisURI.builder().withHost("127.0.0.1")
                .withPort(55555)
                .withPassword(passwdChar)
                .withDatabase(0).build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> redisConnection = redisClient.connect();
        return redisConnection;
    }

    public static void main(String[] args) {


        try {
            //TestJsch.connect(connect);

            Jedis jedis = TestJsch.getJedis(connect);
            System.out.println("----------------");
            System.out.println(jedis.get("test:dict:biz_order_status"));
/*            System.out.println("--------------");
            StatefulRedisConnection lettuce = TestJsch.getLettuce(connect);*/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
