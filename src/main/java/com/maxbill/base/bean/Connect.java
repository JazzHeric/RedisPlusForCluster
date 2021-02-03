package com.maxbill.base.bean;

import lombok.Data;

@Data
public class Connect {

    //主键
    private String id;

    //说明
    private String text;

    //时间
    private String time;

    //是否集群：0单机，1集群
    private String isha;

    //类型：0默认，1：ssh
    private String type;

    //连接名
    private String sname;

    //主机
    private String rhost;

    //主机
    private String shost;

    //redis端口
    private String rport;

    //ssh端口
    private String sport;

    //redis密码
    private String rpass;

    //ssh密码
    private String spass;

    //ssh登录私钥
    private String spkey;

    //是否启用ssl
    private String onssl;

}
