package com.maxbill.base.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxbill.base.bean.Connect;
import com.maxbill.base.service.DataService;
import com.maxbill.core.desktop.Desktop;
import com.maxbill.core.desktop.LogView;
import com.maxbill.tool.*;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.maxbill.base.bean.ResultInfo.*;
import static com.maxbill.core.desktop.Desktop.setEndsViewImage;
import static com.maxbill.core.desktop.Desktop.setEndsViewTitle;
import static com.maxbill.tool.ClusterUtil.getMasterSelf;
import static com.maxbill.tool.DataUtil.*;
import static com.maxbill.tool.ItemUtil.PAGE_DATA_CLUSTER;
import static com.maxbill.tool.ItemUtil.PAGE_DATA_SINGLES;


@Component
public class ConnectController {

    @Autowired
    private DataService dataService;

    /**
     * 查询连接列表
     */
    public String selectConnect() {
        LogView.setLogView(true, "查询连接数据中...");
        return JSON.toJSONString(this.dataService.selectConnect());
    }

    /**
     * 新增连接数据
     */
    public String insertConnect(String json) {
        try {
            int insFlag = this.dataService.insertConnect(parsedConnect(json));
            if (insFlag == 1) {
                return getOkByJson("新增连接成功");
            } else {
                return getNoByJson("新增连接失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 更新连接数据
     */
    public String updateConnect(String json) {
        try {
            int updFlag = this.dataService.updateConnect(parsedConnect(json));
            if (updFlag == 1) {
                return getOkByJson("修改连接成功");
            } else {
                return getNoByJson("修改连接失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 删除连接数据
     */
    public String deleteConnect(String id) {
        try {

            int delFlag = this.dataService.deleteConnectById(id);
            if (delFlag == 1) {
                return getOkByJson("删除连接成功");
            } else {
                return getNoByJson("删除连接失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 查询连接数据
     */
    public String querysConnect(String id) {
        return JSON.toJSONString(this.dataService.selectConnectById(id));
    }


    /**
     * 打开连接数据
     */
    public String createConnect(Boolean toData, String id) {
        try {
            Connect connect = this.dataService.selectConnectById(id);
            //如果存在ssh session 则进行关闭
            if (Objects.nonNull(JschUtil.session) || Objects.nonNull(JschUtil.jsch)) {
                JschUtil.closeSSH();
            }
            if ("1".equals(connect.getType())) {
                JschUtil.openSSH(connect);
            }
            if (connect.getIsha().equals("0")) {
                Jedis jedis = RedisUtil.openJedis(connect);
                if (null != jedis) {
                    //关闭原来连接
                    Jedis oldJedis = getCurrentJedisObject();
                    if (null != oldJedis && oldJedis.isConnected()) {
                        oldJedis.close();
                    }
                    JedisCluster oldCluster = getJedisClusterObject();
                    if (null != oldCluster) {
                        oldCluster.close();
                    }
                    DataUtil.setConfig("currentOpenConnect", connect);
                    DataUtil.setConfig("currentJedisObject", jedis);
                    //修改状态栏
                    setEndsViewTitle(ItemUtil.DESKTOP_STATUS_OK + connect.getText(), "ok");
                    setEndsViewImage(ItemUtil.DESKTOP_STATUS_IMAGE_OK);
                    //跳转数据页面
                    if (toData) {
                        Desktop.setWebViewPage(PAGE_DATA_SINGLES);
                    }
                    return getOkByJson("打开连接成功");
                } else {
                    return getNoByJson("打开连接失败");
                }
            } else {
                JedisCluster cluster = ClusterUtil.openCulter(connect);
                if (null == cluster || cluster.getClusterNodes().size() == 0) {
                    return getNoByJson("打开连接失败");
                } else {
                    //关闭原来连接
                    Jedis oldJedis = getCurrentJedisObject();
                    if (null != oldJedis && oldJedis.isConnected()) {
                        oldJedis.close();
                    }
                    JedisCluster oldCluster = getJedisClusterObject();
                    if (null != oldCluster) {
                        oldCluster.close();
                    }
                    DataUtil.setConfig("currentOpenConnect", connect);
                    DataUtil.setConfig("jedisClusterObject", cluster);
                    DataUtil.setConfig("currentJedisObject", getMasterSelf());
                    //修改状态栏
                    setEndsViewTitle(ItemUtil.DESKTOP_STATUS_OK + connect.getText(), "ok");
                    setEndsViewImage(ItemUtil.DESKTOP_STATUS_IMAGE_OK);
                    //跳转数据页面
                    if (toData) {
                        Desktop.setWebViewPage(PAGE_DATA_CLUSTER);
                    }
                    return getOkByJson("打开连接成功");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.setLogException(e);
            String msg = e.getMessage();
            if (msg.contains("Auth fail")) {
                return getNoByJson("SSH认证失败");
            }
            if (msg.contains("UnknownHostKey")) {
                return getNoByJson("未知的主机");
            }
            if (msg.contains("connect failed")) {
                return getNoByJson("网络不可达");
            }
            if (msg.contains("invalid password")) {
                return getNoByJson("密码不正确");
            }
            if (msg.contains("connect timed out")) {
                return getNoByJson("请求服务超时");
            }
            if (msg.contains("Connection refused")) {
                return getNoByJson("服务拒绝连接");
            }
            if (msg.contains("Authentication required")) {
                return getNoByJson("密码不能为空");
            }
            if (msg.contains("Could not get a resource")) {
                return getNoByJson("请检查服务状态");
            }
            if (msg.contains("socket is not established")) {
                return getNoByJson("打开SSH连接超时");
            }
            return getNoByJson("打开连接失败");
        }
    }


    /**
     * 断开连接数据
     */
    public String disconConnect(String id) {
        try {
            Connect connect = getCurrentOpenConnect();
            if (connect.getIsha().equals("0")) {
                Jedis jedis = RedisUtil.openJedis(connect);
                if (null != jedis) {
                    RedisUtil.closeJedis(jedis);
                    DataUtil.clearConfig();
                }
            } else {
                ClusterUtil.closeCulter();
                DataUtil.clearConfig();
            }
            setEndsViewTitle(ItemUtil.DESKTOP_STATUS_NO, "no");
            setEndsViewImage(ItemUtil.DESKTOP_STATUS_IMAGE_NO);
            return getOkByJson("关闭连接成功");
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 检测连接状态
     */
    public Integer isopenConnect(String id) {
        Connect connect = getCurrentOpenConnect();
        if (null != connect) {
            if (!StringUtils.isEmpty(id) && !connect.getId().equals(id)) {
                return 0;
            }
            if (connect.getIsha().equals("0")) {
                Jedis jedis = getCurrentJedisObject();
                if (null != jedis) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                Object jedisCluster = getJedisClusterObject();
                if (null != jedisCluster) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }


    /**
     * 查询当前连接
     */
    public String pickedConnect() {
        return JSON.toJSONString(getCurrentOpenConnect());
    }


    /**
     * 测试连接状态
     */
    public String detectConnect(Integer type, String data) {
        try {
            Connect connect;
            if (type == 0) {
                connect = this.dataService.selectConnectById(data);
            } else {
                connect = parsedConnect(data);
            }
            String host = connect.getRhost();
            int port = Integer.parseInt(connect.getRport());
            if ("1".equals(connect.getType())) {
                JschUtil.openSSH(connect);
                port = 55555;
                host = "127.0.0.1";
/*                if (host.equals(connect.getShost())) {
                    host = "127.0.0.1";
                }*/
            }
            Jedis jedis = new Jedis(host, port, 3000);
            jedis.connect();
            if (!jedis.isConnected()) {
                return getNoByJson("服务不可用");
            }
            if (!StringUtils.isEmpty(connect.getRpass())) {
                jedis.auth(connect.getRpass());
            }
            jedis.ping();
            jedis.disconnect();
            jedis.close();
            JschUtil.closeSSH();
            return getOkByJson("连接成功");
        } catch (Exception e) {
            LogUtil.setLogException(e);
            String msg = e.getMessage();
            if (msg.contains("Auth fail")) {
                return getNoByJson("SSH认证失败");
            }
            if (msg.contains("UnknownHostKey")) {
                return getNoByJson("未知的主机");
            }
            if (msg.contains("connect failed")) {
                return getNoByJson("网络不可达");
            }
            if (msg.contains("invalid password")) {
                return getNoByJson("密码不正确");
            }
            if (msg.contains("connect timed out")) {
                return getNoByJson("请求服务超时");
            }
            if (msg.contains("Connection refused")) {
                return getNoByJson("服务拒绝连接");
            }
            if (msg.contains("Authentication required")) {
                return getNoByJson("密码不能为空");
            }
            if (msg.contains("socket is not established")) {
                return getNoByJson("打开SSH连接超时");
            }
            return getNoByJson("服务不可用");
        }
    }


    /**
     * 备份连接信息
     */
    public String backupConnect() {
        try {
            String baseUrl = System.getProperty("user.home");
            String fileName = "redisplus-connect-" + DateUtil.formatDate(new Date(), DateUtil.DATE_STR_FILE) + ".bak";
            String filePath = baseUrl + "/" + fileName;
            List<Connect> connectList = this.dataService.selectConnect();
            if (null == connectList || connectList.isEmpty()) {
                return getNoByJson("暂无需要备的份数据");
            }
            StringBuilder dataBuffer = new StringBuilder("");
            for (Connect connect : connectList) {
                dataBuffer.append(JSON.toJSONString(connect));
                dataBuffer.append("\r\n");
            }
            boolean flag = FileUtil.writeStringToFile(filePath, dataBuffer.toString());
            if (flag) {
                return getOkByJson("数据成功备份至当前用户目录中");
            } else {
                return getNoByJson("备份数据失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 还原连接信息
     */
    public String recoveConnect() {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Desktop.getRootStage());
            if (null != file) {
                String[] jsons = FileUtil.readFileToString(file.toString()).split("\r\n", -1);
                for (String json : jsons) {
                    Connect connect = JSON.parseObject(json, Connect.class);
                    if (null != connect) {
                        this.dataService.insertConnect(connect);
                    }
                }
                return getOkByJson("还原数据成功");
            } else {
                return getNoByJson("取消还原操作");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    public String cmdwinConnect(String cmd) {
        try {
            Connect connect = getCurrentOpenConnect();
            if (null != connect) {
                return TelnetUtil.sendCommand(connect, cmd);
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 还原连接信息
     */
    private Connect parsedConnect(String json) {
        JSONObject data = JSON.parseObject(json);
        Connect connect = new Connect();
        String id = data.getString("id");
        if (!StringUtils.isEmpty(id)) {
            connect.setId(id);
        }
        connect.setText(data.getString("text"));
        connect.setType(data.getString("type"));
        connect.setIsha(data.getString("isha"));
        connect.setRhost(data.getString("rhost"));
        connect.setRport(data.getString("rport"));
        connect.setRpass(data.getString("rpass"));
        connect.setShost(data.getString("shost"));
        connect.setSport(data.getString("sport"));
        connect.setSname(data.getString("sname"));
        connect.setSpass(data.getString("spass"));
        connect.setSpkey(data.getString("spkey"));
        return connect;
    }

}
