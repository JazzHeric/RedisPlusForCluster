package com.maxbill.base.bean;

import lombok.Data;

@Data
public class KeyBean {

    private long ttl;

    private long size;

    private String key;

    private String type;

    private String text;

    private String json;

    private String raws;

    private String hexs;

    private long slot;

    private Object data;
}
