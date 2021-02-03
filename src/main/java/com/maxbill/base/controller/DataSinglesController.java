package com.maxbill.base.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxbill.base.bean.ZTreeBean;
import com.maxbill.core.desktop.Desktop;
import com.maxbill.tool.DateUtil;
import com.maxbill.tool.FileUtil;
import com.maxbill.tool.KeyUtil;
import com.maxbill.tool.RedisUtil;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.maxbill.base.bean.ResultInfo.*;
import static com.maxbill.tool.DataUtil.getCurrentJedisObject;

/**
 * 单机模式下数据处理器
 */
@Component
public class DataSinglesController {

    /**
     * 初始化DB树
     */
    @SuppressWarnings("unused")
    public String treeInit() {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                List<ZTreeBean> treeList = new ArrayList<>();
                String role = RedisUtil.getInfo(jedis, "server");
                boolean isCluster = role.contains("redis_mode:cluster");
                for (int i = 0; i < RedisUtil.getDbCount(jedis); i++) {
                    long dbSize;
                    if (i > 0 && isCluster) {
                        break;
                    }
                    if (isCluster) {
                        /** CRC16 database只有1个 databases配置无效*/
                        dbSize = 1;
                    } else {
                        dbSize = RedisUtil.dbSize(jedis, i);
                    }
                    if (dbSize == 0) {
                        break;
                    }
                    ZTreeBean zTreeBean = new ZTreeBean();
                    zTreeBean.setId(KeyUtil.getUUIDKey());
                    zTreeBean.setName("DB" + i + " (" + dbSize + ")");
                    zTreeBean.setPattern("");
                    zTreeBean.setParent(true);
                    zTreeBean.setCount(dbSize);
                    zTreeBean.setPage(1);
                    zTreeBean.setIndex(i);
                    treeList.add(zTreeBean);
                }
                return getOkByJson(treeList);
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 模糊匹配初始化DB树 - 【搜索】按钮触发
     */
    @SuppressWarnings("unused")
    public String likeInit(int index, String pattern) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                long keysCount = RedisUtil.getKeysCount(jedis, index, pattern);
                ZTreeBean ztreeBean = new ZTreeBean();
                ztreeBean.setId(KeyUtil.getUUIDKey());
                ztreeBean.setName("DB" + index + " (" + keysCount + ")");
                ztreeBean.setParent(true);
                ztreeBean.setCount(keysCount);
                ztreeBean.setPage(1);
                ztreeBean.setPattern(pattern);
                ztreeBean.setIndex(index);
                return getOkByJson(ztreeBean);
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 数据分页查询
     */
    @SuppressWarnings("unused")
    public String treeData(String id, int index, int page, String pattern) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                return getOkByJson(RedisUtil.getKeyTree(jedis, index, page, id, pattern));
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 数据分页查询（scan）
     */
    @SuppressWarnings("unused")
    public String pageData(String id, int index, int page, String pattern) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                return getOkByJson(RedisUtil.getKeyPage(jedis, index, page, id, pattern));
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 查询KEY详细信息
     */
    @SuppressWarnings("unused")
    public String keysData(int index, String keys, String order) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, keys)) {
                    return getNoByJson("该KEY不存在");
                }
                return getOkByJson(RedisUtil.getKeyInfo(jedis, index, keys, order));
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 重命名KEY
     */
    @SuppressWarnings("unused")
    public String renameKey(int index, String oldKey, String newKey) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, oldKey)) {
                    return getNoByJson("该KEY不存在");
                }
                if (RedisUtil.existsKey(jedis, index, newKey)) {
                    return getNoByJson("该KEY已存在");
                }
                RedisUtil.renameKey(jedis, index, oldKey, newKey);
                return getOkByJson("重命名KEY成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 设置KEY失效时间
     */
    @SuppressWarnings("unused")
    public String retimeKey(int index, String key, int time) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                RedisUtil.retimeKey(jedis, index, key, time);
                return getOkByJson("设置TTL成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 删除KEY
     */
    @SuppressWarnings("unused")
    public String deleteKey(int index, String key) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                RedisUtil.deleteKey(jedis, index, key);
                return getOkByJson("删除KEY成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 更新STRING数据
     */
    @SuppressWarnings("unused")
    public String updateStr(int index, String key, String val) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                RedisUtil.updateStr(jedis, index, key, val);
                return getOkByJson("修改数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 添加ITEM数据
     */
    @SuppressWarnings("unused")
    public String insertVal(int type, int index, String key, String val, double score) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                //1:set,2:zset,3:list,4:hash
                switch (type) {
                    case 1:
                        RedisUtil.insertSet(jedis, index, key, val);
                        break;
                    case 2:
                        RedisUtil.insertZset(jedis, index, key, val, score);
                        break;
                    case 3:
                        RedisUtil.insertList(jedis, index, key, val);
                        break;
                    case 4:
                        String[] valArray = val.split(":");
                        String mapKey = valArray[0];
                        String mapVal = valArray[1];
                        RedisUtil.insertHash(jedis, index, key, mapKey, mapVal);
                        break;
                }
                return getOkByJson("添加数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 删除ITEM数据
     */
    @SuppressWarnings("unused")
    public String deleteVal(int type, int index, String key, String val) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                //1:set,2:zset,3:list,4:hash
                switch (type) {
                    case 1:
                        RedisUtil.deleteSet(jedis, index, key, val);
                        break;
                    case 2:
                        RedisUtil.deleteZset(jedis, index, key, val);
                        break;
                    case 3:
                        long keyIndex = Long.parseLong(val);
                        RedisUtil.deleteList(jedis, index, key, keyIndex);
                        break;
                    case 4:
                        String mapKey = val;
                        RedisUtil.deleteHash(jedis, index, key, mapKey);
                        break;
                }
                return getOkByJson("删除数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 更新ITEM数据
     */
    @SuppressWarnings("unused")
    public String updateVal(String json) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                JSONObject data = JSON.parseObject(json);
                Integer type = data.getInteger("type");
                Integer index = data.getInteger("index");
                String key = data.getString("key");
                if (!RedisUtil.existsKey(jedis, index, key)) {
                    return getNoByJson("该KEY不存在");
                }
                //1:set,2:zset,3:list,4:hash
                switch (type) {
                    case 1:
                        String oldVal = data.getString("oldVal");
                        String newVal = data.getString("newVal");
                        RedisUtil.deleteSet(jedis, index, key, oldVal);
                        RedisUtil.insertSet(jedis, index, key, newVal);
                        break;
                    case 2:
                        String oldZval = data.getString("oldVal");
                        String newZval = data.getString("newVal");
                        Integer score = data.getInteger("score");
                        RedisUtil.deleteZset(jedis, index, key, oldZval);
                        RedisUtil.insertZset(jedis, index, key, newZval, score);
                        break;
                    case 3:
                        Integer itemIndex = data.getInteger("itemIndex");
                        String val = data.getString("val");
                        RedisUtil.updateList(jedis, index, key, itemIndex, val);
                        break;
                    case 4:
                        String oldMapKey = data.getString("oldKey");
                        String newMapKey = data.getString("newKey");
                        String newMapVal = data.getString("newVal");
                        RedisUtil.deleteHash(jedis, index, key, oldMapKey);
                        RedisUtil.insertHash(jedis, index, key, newMapKey, newMapVal);
                        break;
                }
                return getOkByJson("修改数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 新增KEY数据
     */
    @SuppressWarnings("unused")
    public String insertKey(int type, int index, String key, String val, int time, double score) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                //1:set,2:zset,3:list,4:hash
                switch (type) {
                    case 1:
                        RedisUtil.insertSet(jedis, index, key, val);
                        break;
                    case 2:
                        RedisUtil.insertZset(jedis, index, key, val, score);
                        break;
                    case 3:
                        RedisUtil.insertList(jedis, index, key, val);
                        break;
                    case 4:
                        String[] valArray = val.split(":");
                        String mapKey = valArray[0];
                        String mapVal = valArray[1];
                        RedisUtil.insertHash(jedis, index, key, mapKey, mapVal);
                        break;
                    case 5:
                        RedisUtil.insertStr(jedis, index, key, val);
                        break;
                }
                if (time != -1) {
                    RedisUtil.retimeKey(jedis, index, key, time);
                }
                return getOkByJson("新增数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 清空KEY数据
     */
    @SuppressWarnings("unused")
    public String removeKey(int index) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                jedis.select(index);
                jedis.flushDB();
                return getOkByJson("清空数据成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 备份KEY数据
     */
    @SuppressWarnings("unused")
    public String backupKey(int index, String pattern) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                String baseUrl = System.getProperty("user.home");
                String fileName = "redisplus-dbdata-" + DateUtil.formatDate(new Date(), DateUtil.DATE_STR_FILE) + ".bak";
                String filePath = baseUrl + "/" + fileName;
                boolean flag = FileUtil.writeStringToFile(filePath, RedisUtil.backupKey(jedis, index, pattern));
                if (flag) {
                    return getOkByJson("数据成功导出至当前用户目录中");
                } else {
                    return getNoByJson("导出数据失败");
                }
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    /**
     * 还原KEY数据
     */
    @SuppressWarnings("unused")
    public String recoveKey(int index) {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(Desktop.getRootStage());
                if (null != file) {
                    RedisUtil.recoveKey(jedis, index, FileUtil.readFileToString(file.toString()));
                    return getOkByJson("还原数据成功");
                } else {
                    return getNoByJson("取消还原操作");
                }
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

}
