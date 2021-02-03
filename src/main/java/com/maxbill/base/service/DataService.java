package com.maxbill.base.service;

import com.maxbill.base.bean.Connect;
import com.maxbill.base.bean.Setting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataService {

    void createConnectTable();

    void createSettingTable();

    int isExistsTable(String tableName);

    Connect selectConnectById(String id);

    List<Connect> selectConnect();

    int insertConnect(Connect obj);

    int updateConnect(Connect obj);

    int deleteConnectById(String id);

    Setting selectSetting(String keys);

    void insertSetting(Setting setting);

    int updateSetting(Setting setting);

}
