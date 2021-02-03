package com.maxbill.base.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@Builder
public class ZTreeBean {

    private String id;

    @JSONField(name = "pId")
    private String pId;

    private String name;

    private String pattern;

    private Integer index;

    private Integer page;

    private Long count;

    private String icon;

    private boolean checked = false;

    @JSONField(name = "isParent")
    private boolean isParent;

    private LinkedList<ZTreeBean> children;

    public ZTreeBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ZTreeBean(String id, String pId, String name) {
        this.id = id;
        this.pId = pId;
        this.name = name;
    }

    public ZTreeBean() {
    }

    public ZTreeBean(String id, String pId, String name, String pattern, Integer index, Integer page, Long count, String icon, boolean checked, boolean isParent, LinkedList<ZTreeBean> children) {
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.pattern = pattern;
        this.index = index;
        this.page = page;
        this.count = count;
        this.icon = icon;
        this.checked = checked;
        this.isParent = isParent;
        this.children = children;
    }
}
