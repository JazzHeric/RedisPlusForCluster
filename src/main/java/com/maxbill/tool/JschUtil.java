package com.maxbill.tool;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.maxbill.base.bean.Connect;
import org.springframework.util.StringUtils;

public class JschUtil {

    static JSch jsch;
    static Session session;

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
        session.connect(3000);
        String rhost = connect.getRhost();
        if (rhost.equals(connect.getShost())) {
            rhost = "127.0.0.1";
        }
        session.setPortForwardingL(55555, rhost, Integer.valueOf(connect.getRport()));
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
    }

}
