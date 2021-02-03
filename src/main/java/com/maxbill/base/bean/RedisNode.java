package com.maxbill.base.bean;

import lombok.Data;

@Data
public class RedisNode {

    private String id;

    private String addr;

    private String flag;

    private String pid;

    private String ping;

    private String pong;

    private String epoch;

    private String state;

    private String hash;
}
