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

    //主机 -
    private String rhost;

    //redis端口
    private String rport;

    //redis密码
    private String rpass;

    //是否启用ssl
    private String onssl;

    /** ---------------SSH 相关参数-----------------*/

    //类型：0默认，1：ssh  --是否开启SSH通道
    private String type;

    //主机  - SSH域名地址
    private String shost;

    //连接名 - SSH通道
    private String sname;

    //ssh端口 - SSH端口
    private String sport;

    //ssh密码
    private String spass;

    //ssh登录私钥
    private String spkey;


}
