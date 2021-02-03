package com.maxbill.base.service;

import com.maxbill.base.bean.Connect;
import com.maxbill.base.bean.Setting;
import com.maxbill.base.mapper.DataMapper;
import com.maxbill.tool.DateUtil;
import com.maxbill.tool.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private DataMapper dataMapper;

    public void createConnectTable() {
        this.dataMapper.createConnectTable();
    }

    public void createSettingTable() {
        this.dataMapper.createSettingTable();
    }

    public int isExistsTable(String tableName) {
        return this.dataMapper.isExistsTable(tableName);
    }

    public Connect selectConnectById(String id) {
        return this.dataMapper.selectConnectById(id);
    }

    public List<Connect> selectConnect() {
        return this.dataMapper.selectConnect();
    }

    public int insertConnect(Connect connect) {
        connect.setId(KeyUtil.getUUIDKey());
        connect.setOnssl("0");
        connect.setTime(DateUtil.formatDateTime(new Date()));
        if ("0".equals(connect.getType())) {
            connect.setSname("");
        }
        return this.dataMapper.insertConnect(connect);
    }

    public int updateConnect(Connect connect) {
        connect.setOnssl("0");
        connect.setTime(DateUtil.formatDateTime(new Date()));
        if ("0".equals(connect.getType())) {
            connect.setSname("");
        }
        return this.dataMapper.updateConnect(connect);
    }

    public int deleteConnectById(String id) {
        return this.dataMapper.deleteConnectById(id);
    }


    public void insertSetting(Setting setting) {
        Setting settingTemp = selectSetting(setting.getKeys());
        if (null == settingTemp) {
            this.dataMapper.insertSetting(setting);
        }
    }

    public Setting selectSetting(String keys) {
        return this.dataMapper.selectSetting(keys);
    }

    public int updateSetting(Setting setting) {
        return this.dataMapper.updateSetting(setting);
    }


}
