package com.maxbill.base.controller;

import com.alibaba.fastjson.JSON;
import com.maxbill.base.bean.Connect;
import com.maxbill.base.bean.Setting;
import com.maxbill.base.service.DataService;
import com.maxbill.core.desktop.Desktop;
import com.maxbill.tool.FileUtil;
import com.maxbill.tool.KeyUtil;
import com.maxbill.tool.MailUtil;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;

import static com.maxbill.base.bean.ResultInfo.*;
import static com.maxbill.tool.DataUtil.getCurrentOpenConnect;
import static com.maxbill.tool.ItemUtil.*;

@Component
public class OtherController {

    @Autowired
    private DataService dataService;

    public void changeWebview(int pageNo) {
        Connect connect = getCurrentOpenConnect();
        String pageUrl = "";
        switch (pageNo) {
            case 1:
                String connectTheme = getSetting(SETTING_CONNECT_THEME);
                if (!StringUtils.isEmpty(connectTheme) && connectTheme.equals("1")) {
                    pageUrl = PAGE_CONNECT_QUICKER;
                } else {
                    pageUrl = PAGE_CONNECT_DEFAULT;
                }
                break;
            case 2:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_DATA_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_DATA_CLUSTER;
                }
                break;
            case 3:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_INFO_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_INFO_CLUSTER;
                }
                break;
            case 4:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_CONF_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_CONF_CLUSTER;
                }
                break;
            case 5:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_MONITOR_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_MONITOR_CLUSTER;
                }
                break;
        }
        Desktop.setWebViewPage(pageUrl);
    }


    /**
     * 配置连接信息
     */
    public void initSystems() {
        try {
            int tableCount1 = this.dataService.isExistsTable("T_CONNECT");
            if (tableCount1 == 0) {
                this.dataService.createConnectTable();
            }
            int tableCount2 = this.dataService.isExistsTable("T_SETTING");
            if (tableCount2 == 0) {
                this.dataService.createSettingTable();
            }
            //1.初始化默认主题颜色
            Setting setting01 = new Setting();
            setting01.setId(KeyUtil.getUUIDKey());
            setting01.setKeys(SETTING_THEME_COLOR);
            setting01.setVals("#D6D6D7");
            this.dataService.insertSetting(setting01);
            //2.初始化连接默认主题
            Setting setting02 = new Setting();
            setting02.setId(KeyUtil.getUUIDKey());
            setting02.setKeys(SETTING_CONNECT_THEME);
            setting02.setVals("0");
            this.dataService.insertSetting(setting02);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getSetting(String keys) {
        Setting setting = this.dataService.selectSetting(keys);
        if (null != setting) {
            return setting.getVals();
        }
        return null;
    }


    public String setSetting(String keys, String vals) {
        try {
            Setting setting = new Setting();
            setting.setKeys(keys);
            setting.setVals(vals);
            int flag = this.dataService.updateSetting(setting);
            if (flag == 1) {
                switch (keys) {
                    case SETTING_THEME_COLOR:
                        Color backgroundColor = Color.web(vals, 1.0);
                        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, null, null);
                        Desktop.getTopsView().setBackground(new Background(backgroundFill));
                        break;
                    case SETTING_CONNECT_THEME:
                        if (!StringUtils.isEmpty(vals) && vals.equals("1")) {
                            Desktop.setWebViewPage(PAGE_CONNECT_QUICKER);
                        } else {
                            Desktop.setWebViewPage(PAGE_CONNECT_DEFAULT);
                        }
                        break;
                }
                return getOkByJson("修改设置项成功");
            } else {
                return getNoByJson("修改设置项失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return exception(e);
        }
    }

    public String sendMail(String mailAddr, String mailText) {
        try {
            boolean sendFlag = MailUtil.sendMail(mailAddr, mailText);
            if (sendFlag) {
                return getOkByJson("发送邮件成功");
            } else {
                return getNoByJson("发送邮件失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    public String pickPkey() {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Desktop.getRootStage());
            if (null != file) {
                return file.getPath();
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

}
