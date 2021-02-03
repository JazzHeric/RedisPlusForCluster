package com.maxbill.tool;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.maxbill.base.bean.*;
import com.maxbill.base.consts.Constant;
import com.maxbill.base.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Slowlog;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.maxbill.tool.StringUtil.FLAG_COLON;
import static com.maxbill.tool.StringUtil.FLAG_EQUAL;

public class RedisUtil {

    static Logger log = LoggerFactory.getLogger("RedisUtil");

    //可用连接实例的最大数目，默认值为8；
    private static final int MAX_TOTAL = 50;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static final int MAX_IDLE = 10;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static final int MAX_WAIT = 3000;

    //超时时间
    private static final int TIME_OUT = 3000;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static final boolean TEST_ON_BORROW = true;

    private static final boolean TEST_ON_RETURN = true;

    //redis连接池
    private static JedisPool jedisPool;

    //资源锁
    private static ReentrantLock lock = new ReentrantLock();

    private RedisUtil() {
    }

    /**
     * 初始化JedisPool
     */
    private static void initJedisPool(Connect connect) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(MAX_IDLE);
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setTestOnReturn(TEST_ON_RETURN);
        String host = connect.getRhost();
        int port = Integer.valueOf(connect.getRport());
        if (connect.getType().equals("1")) {
            port = 55555;
            if (host.equals(connect.getShost())) {
                host = "127.0.0.1";
            }
        }
        if (StringUtils.isEmpty(connect.getRpass())) {
            jedisPool = new JedisPool(config, host, port, TIME_OUT);
        } else {
            jedisPool = new JedisPool(config, host, port, TIME_OUT, connect.getRpass());
        }
    }

    /**
     * 释放当前Redis连接池
     */
    private static void freeJedisPool() {
        if (null != jedisPool && !jedisPool.isClosed()) {
            jedisPool.destroy();
        }
    }

    /**
     * 从JedisPool中获取Jedis
     */
    public static Jedis openJedis(Connect connect) throws Exception {
        log.info("正在建立新连接...");
        //销毁旧的连接池
        freeJedisPool();
        //防止吃初始化时多线程竞争问题
        lock.lock();
        initJedisPool(connect);
        lock.unlock();
        return jedisPool.getResource();
    }

    /**
     * 从JedisPool中获取Jedis
     */
    public static Jedis getJedis() {
        if (null != jedisPool) {
            return jedisPool.getResource();
        } else {
            return null;
        }
    }

    /**
     * 释放Jedis连接
     */
    public static void closeJedis(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    /**
     * 判断key是否存在
     */
    public static boolean existsKey(Jedis jedis, int index, String key) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        return jedis.exists(key);
    }

    /**
     * 重命名key
     */
    public static void renameKey(Jedis jedis, int index, String oldKey, String newKey) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            Constant.allClusterKeySet.remove(oldKey);
            oldKey = oldKey.replaceAll(Constant.notGroupKeyPrefix, "");
        }
        jedis.rename(oldKey, newKey);
    }

    /**
     * 设置key时间
     */
    public static void retimeKey(Jedis jedis, int index, String key, int time) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.expire(key, time);
    }

    /**
     * 删除key
     */
    public static void deleteKey(Jedis jedis, int index, String key) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            Constant.allClusterKeySet.remove(key);
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.del(key);
    }

    /**
     * 修改String的Value
     */
    public static void updateStr(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.set(key, val);
    }

    /**
     * 添加String
     */
    public static void insertStr(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.set(key, val);
    }

    /**
     * 添加Set的item
     */
    public static void insertSet(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.sadd(key, val);
    }

    /**
     * 添加Zset的item
     */
    public static void insertZset(Jedis jedis, int index, String key, String val, double score) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.zadd(key, score, val);
    }

    /**
     * 添加List的item
     */
    public static void insertList(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.rpush(key, val);
    }

    /**
     * 修改List的item
     */
    public static void updateList(Jedis jedis, int index, String key, int itemIndex, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.lset(key, itemIndex, val);
    }

    /**
     * 添加Hase的key和val
     */
    public static void insertHash(Jedis jedis, int index, String key, String mapKey, String mapVal) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.hset(key, mapKey, mapVal);
    }

    /**
     * 删除Set的item
     */
    public static void deleteSet(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.srem(key, val);
    }

    /**
     * 删除Zset的item
     */
    public static void deleteZset(Jedis jedis, int index, String key, String val) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.zrem(key, val);
    }

    /**
     * 删除List的item
     */
    public static void deleteList(Jedis jedis, int index, String key, long keyIndex) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        String tempItem = KeyUtil.getUUIDKey();
        jedis.lset(key, keyIndex, tempItem);
        jedis.lrem(key, 0, tempItem);
    }

    /**
     * 删除List的item
     */
    public static void deleteHash(Jedis jedis, int index, String key, String mapKey) {
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
        }
        jedis.hdel(key, mapKey);
    }

    /**
     * 还原数据
     */
    public static void recoveKey(Jedis jedis, int index, String jsonStr) {
        //Pipeline pipeline = jedis.pipelined();
        //pipeline.select(index);
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        }
        String[] jsons = jsonStr.split("\r\n", -1);
        for (String json : jsons) {
            KeyBean keyBean = JsonUtil.parseKeyBeanObject(json);
            if (null == keyBean) {
                continue;
            }
            String key = keyBean.getKey();
            String type = keyBean.getType();
            Object data = keyBean.getData();
            String temp = JSON.toJSONString(data);
            switch (type) {
                //set (集合)
                case "set":
                    List sets = JSON.parseObject(temp, List.class);
                    for (Object setTemp : sets) {
                        jedis.sadd(key, setTemp.toString());
                    }
                    break;
                //list (列表)
                case "list":
                    List lists = JSON.parseObject(temp, List.class);
                    for (Object listTemp : lists) {
                        jedis.lpush(key, listTemp.toString());
                    }
                    break;
                //zset (有序集)
                case "zset":
                    List zsets = JSON.parseObject(temp, List.class);
                    for (Object zsetTemp : zsets) {
                        jedis.zadd(key, zsets.indexOf(zsetTemp) + 1, zsetTemp.toString());
                    }
                    break;
                //hash (哈希表)
                case "hash":
                    Map map = JSON.parseObject(temp, Map.class);
                    jedis.hmset(key, map);
                    break;
                //string (字符串)
                case "string":
                    jedis.set(key, data.toString());
                    break;
            }
        }
        //pipeline.sync();
    }

    /**
     * 备份数据
     */
    public static String backupKey(Jedis jedis, int index, String pattern) {
        long startTime = System.currentTimeMillis();
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        }
        if (StringUtils.isEmpty(pattern)) {
            pattern = "*";
        }
        Set<String> keySet = jedis.keys(pattern);
        long endTime = System.currentTimeMillis();
        log.info("getKeyTree查询耗时：" + (endTime - startTime));
        if (null == keySet) {
            return "";
        }
        StringBuilder dataBuffer = new StringBuilder();
        for (String key : keySet) {
            KeyBean keyBean = new KeyBean();
            keyBean.setKey(key);
            keyBean.setType(jedis.type(key));
            keyBean.setTtl(jedis.ttl(key));
            switch (keyBean.getType()) {
                //set (集合)
                case "set":
                    keyBean.setData(jedis.smembers(key));
                    break;
                //list (列表)
                case "list":
                    keyBean.setData(jedis.lrange(key, 0, -1));
                    break;
                //zset (有序集)
                case "zset":
                    keyBean.setData(jedis.zrange(key, 0, -1));
                    break;
                //hash (哈希表)
                case "hash":
                    keyBean.setData(jedis.hgetAll(key));
                    break;
                //string (字符串)
                case "string":
                    keyBean.setData(jedis.get(key));
                    break;
            }
            dataBuffer.append(JSON.toJSONString(keyBean));
            dataBuffer.append("\r\n");
        }
        return dataBuffer.toString();
    }

    /**
     * 获取库的key值
     */
    public static long dbSize(Jedis jedis, Integer index) {
        if (null != index && !RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        }
        return jedis.dbSize();
    }

    /**
     * 按条件获取库的key数
     */
    public static long getKeysCount(Jedis jedis, int index, String pattern) {
        if(RedisUtil.isCluster(jedis)) {
            ClusterService clusterService = (ClusterService)SpringContextUtil.getBean("clusterService");
            return clusterService.searchKeySet(pattern).size();
        }
        jedis.select(index);
        if (StringUtils.isEmpty(pattern)) {
            pattern = "*";
        }
        return jedis.keys(pattern).size();
    }

    /**
     * 按条件获取分页数据
     */
    public static List<ZTreeBean> getKeyTree(Jedis jedis, int index, int page, String pid, String pattern) {
        long startTime = System.currentTimeMillis();
        /** CRC16 cluster模式 keys命令和select命令是禁止的*/
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        }
        if (StringUtils.isEmpty(pattern)) {
            pattern = "*";
        }
        Set<String> keySet = RedisUtil.getRedisKeys(pattern, jedis);
        long endTime = System.currentTimeMillis();
        log.info("getKeyTree查询耗时：" + (endTime - startTime));

        // 排序
        List<String> keyList = Lists.newLinkedList(keySet);
        Collections.sort(keyList);

        // 分页返回
        int startIndex = (page - 1) * Constant.treePageSize;
        int endIndex = page * Constant.treePageSize;
        if (endIndex > keyList.size()) {
            endIndex = keyList.size();
        }
        LinkedList<ZTreeBean> treeList = new LinkedList<>();
        for (String key : keyList.subList(startIndex, endIndex)) {
            groupRecursiveLoad(treeList, key, null, pid);
        }
        return treeList;
    }

    private static Set<String> getRedisKeys(String pattern, Jedis jedis) {
        if(RedisUtil.isCluster(jedis)){
            ClusterService clusterService = (ClusterService)SpringContextUtil.getBean("clusterService");
            Set<String> result = "*".equals(pattern) ? clusterService.getAllClusterKeys() : clusterService.searchKeySet(pattern);
            return result;
        }
        return jedis.keys(pattern);
    }



    /**
     * 分组递归加载节点
     *
     * @param nodes
     * @param key
     * @param pref
     * @param pid
     */
    public static void groupRecursiveLoad(LinkedList<ZTreeBean> nodes, String key, String pref, String pid) {
        String thisKey = new String(key);
        if (pref != null && thisKey.startsWith(pref)) {
            thisKey = thisKey.substring(pref.length());
        }

        /** 普通节点类型：直接添加 */
        String[] metas = thisKey.split(":");
        if (metas.length == 1) {
            ZTreeBean node = ZTreeBean.builder()
                    .id(KeyUtil.getUUIDKey())
                    .pId(pid)
                    .name(key)
                    .isParent(false)
                    .icon("../image/data-01.png")
                    .build();
            nodes.add(node);
            return;
        }

        /** 对象节点类型：递归添加(Object:Field:Field) */
        // 1、判断最后一个是否目标分组
        ZTreeBean groupNode = null;
        if (!CollectionUtils.isEmpty(nodes)) {
            ZTreeBean last = nodes.getLast();
            if (last.isParent() && last.getName().equals(metas[0])) {
                groupNode = last;
            }
        }
        // 2、创建分组节点
        if (groupNode == null) {
            groupNode = ZTreeBean.builder()
                    .id(KeyUtil.getUUIDKey())
                    .pId(pid)
                    .name(metas[0])
                    .isParent(true)
                    .children(new LinkedList<>())
                    .build();
            nodes.add(groupNode);
        }
        // 3、递归——添加分组children
        groupRecursiveLoad(groupNode.getChildren(), key, (pref == null ? "" : pref) + metas[0] + ":", groupNode.getId());
    }

    /**
     * 按条件获取分页数据（scan）
     */
    public static List<ZTreeBean> getKeyPage(Jedis jedis, int index, int page, String pid, String pattern) {
        long startTime = System.currentTimeMillis();
        List<ZTreeBean> treeList = new ArrayList<>();
        jedis.select(index);
        if (StringUtils.isEmpty(pattern)) {
            pattern = "*";
        }
        //计算获取的数据量
        long pageSize;
        long currSize = page * 1000;
        long currScan = (page - 1) * 1000;
        long keysSize = getKeysCount(jedis, index, pattern);
        if (keysSize - currSize < 0) {
            pageSize = currSize - keysSize;
        } else {
            pageSize = 1000;
        }
        //scan数据查询
        ScanParams scanParams = new ScanParams();
        scanParams.match(pattern);
        scanParams.count((int) pageSize);
        ScanResult<String> scanResult = jedis.scan(currScan + "", scanParams);
        long endTime = System.currentTimeMillis();
        log.info("getKeyPage查询耗时：" + (endTime - startTime));
        //封装返回符合条件数据
        ZTreeBean zTreeBean = null;
        if (null != scanResult) {
            for (String key : scanResult.getResult()) {
                zTreeBean = new ZTreeBean();
                zTreeBean.setId(KeyUtil.getUUIDKey());
                zTreeBean.setPId(pid);
                zTreeBean.setName(key);
                zTreeBean.setParent(false);
                zTreeBean.setIndex(index);
                zTreeBean.setIcon("../image/data-01.png");
                treeList.add(zTreeBean);
            }
        }
        return treeList;
    }

    /**
     * 解析服务器信息
     */
    public static RedisInfo getRedisInfo(Jedis jedis) {
        RedisInfo redisInfo = null;
        Client client = jedis.getClient();
        client.info();
        String info = client.getBulkReply();
        String[] infos = info.split("# ");
        if (infos.length > 0) {
            redisInfo = new RedisInfo();
            for (String infoStr : infos) {
                if (infoStr.startsWith("Server")) {
                    redisInfo.setServer(infoStr);
                }
                if (infoStr.startsWith("Clients")) {
                    redisInfo.setClient(infoStr);
                }
                if (infoStr.startsWith("Memory")) {
                    redisInfo.setMemory(infoStr);
                }
                if (infoStr.startsWith("Persistence")) {
                    redisInfo.setPersistence(infoStr);
                }
                if (infoStr.startsWith("Stats")) {
                    redisInfo.setStats(infoStr);
                }
                if (infoStr.startsWith("Replication")) {
                    redisInfo.setReplication(infoStr);
                }
                if (infoStr.startsWith("CPU")) {
                    redisInfo.setCpu(infoStr);
                }
                if (infoStr.startsWith("Cluster")) {
                    redisInfo.setCluster(infoStr);
                }
                if (infoStr.startsWith("Keyspace")) {
                    redisInfo.setKeyspace(infoStr);
                }
            }
        }
        return redisInfo;
    }

    /**
     * 获取redis信息
     */
    public static RedisInfo getRedisInfoList(Jedis jedis) {
        RedisInfo redisInfoBean = getRedisInfo(jedis);
        RedisInfo redisInfo = null;
        if (null != redisInfoBean) {
            redisInfo = new RedisInfo();
            redisInfo.setServer(returnServerInfo(redisInfoBean).toString());
            redisInfo.setClient(returnClientInfo(redisInfoBean).toString());
            redisInfo.setMemory(returnMemoryInfo(redisInfoBean).toString());
            redisInfo.setPersistence(returnPersistenceInfo(redisInfoBean).toString());
            redisInfo.setStats(returnStatsInfo(redisInfoBean).toString());
            redisInfo.setCpu(returnCpuInfo(redisInfoBean).toString());
            redisInfo.setUsers(returnUsersInfo(jedis));
        }
        return redisInfo;
    }

    /**
     * 解析服务端信息
     */
    private static StringBuffer returnServerInfo(RedisInfo redisInfo) {
        //服务端信息
        StringBuffer serverBuf = new StringBuffer("");
        String serverInfo = redisInfo.getServer();
        if (!StringUtils.isEmpty(serverInfo)) {
            String[] server = serverInfo.split("\n");
            for (String info : server) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "redis_version":
                        serverBuf.append("服务版本: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "redis_mode":
                        serverBuf.append("服务模式: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "os":
                        serverBuf.append("系统版本: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "arch_bits":
                        //serverBuf.append("系统架构: ").append(value);
                        //serverBuf.append("<br/>");
                        break;
                    case "multiplexing_api":
                        //serverBuf.append("事件机制: ").append(value);
                        //serverBuf.append("<br/>");
                        break;
                    case "process_id":
                        serverBuf.append("进程编号: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "tcp_port":
                        serverBuf.append("服务端口: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "uptime_in_seconds":
                        serverBuf.append("运行时间: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                    case "config_file":
                        serverBuf.append("配置文件: ").append(value);
                        serverBuf.append("<br/>");
                        break;
                }
            }
        }
        return serverBuf;
    }

    /**
     * 解析客户端信息
     */
    private static StringBuffer returnClientInfo(RedisInfo redisInfo) {
        StringBuffer clientBuf = new StringBuffer("");
        String clientInfo = redisInfo.getClient();
        if (!StringUtils.isEmpty(clientInfo)) {
            String[] client = clientInfo.split("\n");
            for (String info : client) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "connected_clients":
                        clientBuf.append("当前已连接客户端数量: ").append(value);
                        clientBuf.append("<br/>");
                        break;
                    case "blocked_clients":
                        clientBuf.append("当前已阻塞客户端数量: ").append(value);
                        clientBuf.append("<br/>");
                        break;
                    case "client_longest_output_list":
                        clientBuf.append("当前连接的客户端当中，最长输出列表: ").append(value);
                        clientBuf.append("<br/>");
                        break;
                    case "client_biggest_input_buf":
                        clientBuf.append("当前连接的客户端当中，最大输入缓存: ").append(value);
                        clientBuf.append("<br/>");
                        break;
                }
            }
        }
        return clientBuf;
    }

    /**
     * 解析客户端信息
     */
    private static StringBuffer returnMemoryInfo(RedisInfo redisInfo) {
        StringBuffer memoryBuf = new StringBuffer("");
        String memoryInfo = redisInfo.getMemory();
        if (!StringUtils.isEmpty(memoryInfo)) {
            String[] memory = memoryInfo.split("\n");
            for (String info : memory) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "used_memory":
                        memoryBuf.append("已占用内存量: ").append(value);
                        memoryBuf.append("<br/>");
                        break;
                    case "used_memory_rss":
                        memoryBuf.append("分配内存总量: ").append(value);
                        memoryBuf.append("<br/>");
                        break;
                    case "used_memory_peak_human":
                        memoryBuf.append("内存高峰值: ").append(value);
                        memoryBuf.append("<br/>");
                        break;
                    case "mem_fragmentation_ratio":
                        memoryBuf.append("内存碎片率: ").append(value);
                        memoryBuf.append("<br/>");
                        break;
                    case "mem_allocator":
                        memoryBuf.append("内存分配器: ").append(value);
                        memoryBuf.append("<br/>");
                        break;
                }
            }
        }
        return memoryBuf;
    }

    /**
     * 解析持久化信息
     */
    private static StringBuffer returnPersistenceInfo(RedisInfo redisInfo) {
        StringBuffer persistenceBuf = new StringBuffer("");
        String persistenceInfo = redisInfo.getPersistence();
        if (!StringUtils.isEmpty(persistenceInfo)) {
            String[] persistence = persistenceInfo.split("\n");
            for (String info : persistence) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "rdb_bgsave_in_progress":
                        persistenceBuf.append("是否正在创建RDB的文件: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "rdb_last_save_time":
                        persistenceBuf.append("最近成功创建RDB时间戳: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "rdb_last_bgsave_status":
                        persistenceBuf.append("最近创建RDB文件的结果: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "rdb_last_bgsave_time_sec":
                        persistenceBuf.append("最近创建RDB文件的耗时: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_enabled":
                        persistenceBuf.append("服务是否已经开启了AOF: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_rewrite_in_progress":
                        persistenceBuf.append("是否正在创建AOF的文件: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_last_rewrite_time_sec":
                        persistenceBuf.append("最近创建AOF文件的耗时: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_last_bgrewrite_status":
                        persistenceBuf.append("最近创建AOF文件的结果: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_current_size":
                        persistenceBuf.append("当前AOF文件记录的大小: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                    case "aof_buffer_length":
                        persistenceBuf.append("当前AOF文件缓冲区大小: ").append(value);
                        persistenceBuf.append("<br/>");
                        break;
                }
            }
        }
        return persistenceBuf;
    }

    /**
     * 解析连接的信息
     */
    private static StringBuffer returnStatsInfo(RedisInfo redisInfo) {
        StringBuffer statsBuf = new StringBuffer();
        String statsInfo = redisInfo.getStats();
        if (!StringUtils.isEmpty(statsInfo)) {
            String[] stats = statsInfo.split("\n");
            for (String info : stats) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "total_connections_received":
                        statsBuf.append("已连接客户端总数: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                    case "total_commands_processed":
                        statsBuf.append("执行过的命令总数: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                    case "instantaneous_ops_per_sec":
                        statsBuf.append("服务每秒执行数量: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                    case "total_net_input_bytes":
                        statsBuf.append("服务输入网络流量: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                    case "total_net_output_bytes":
                        statsBuf.append("服务输出网络流量: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                    case "rejected_connections":
                        statsBuf.append("拒绝连接客户端数: ").append(value);
                        statsBuf.append("<br/>");
                        break;
                }
            }
        }
        return statsBuf;
    }

    /**
     * 解析连接的信息
     */
    private static StringBuffer returnCpuInfo(RedisInfo redisInfo) {
        //处理器信息
        StringBuffer cpuBuf = new StringBuffer();
        String cpuInfo = redisInfo.getCpu();
        if (!StringUtils.isEmpty(cpuInfo)) {
            String[] cpu = cpuInfo.split("\n");
            for (String info : cpu) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                switch (key) {
                    case "used_cpu_sys":
                        cpuBuf.append("服务主进程在核心态累计CPU耗时: ").append(value);
                        cpuBuf.append("<br/>");
                        break;
                    case "used_cpu_user":
                        cpuBuf.append("服务主进程在用户态累计CPU耗时: ").append(value);
                        cpuBuf.append("<br/>");
                        break;
                    case "used_cpu_sys_children":
                        cpuBuf.append("服务后台进程在核心态累计CPU耗时: ").append(value);
                        cpuBuf.append("<br/>");
                        break;
                    case "used_cpu_user_children":
                        cpuBuf.append("服务后台进程在用户态累计CPU耗时: ").append(value);
                        cpuBuf.append("<br/>");
                        break;
                }
            }
        }
        return cpuBuf;
    }

    /**
     * 解析连接的信息
     */
    private static List<ClientInfo> returnUsersInfo(Jedis jedis) {
        //处理器信息
        List<ClientInfo> usersList = new ArrayList<>();
        String usersInfo = jedis.clientList();
        if (!StringUtils.isEmpty(usersInfo)) {
            String[] users = usersInfo.split("\n");
            if (users.length > 0) {
                for (String user : users) {
                    ClientInfo clientInfo = new ClientInfo();
                    String[] items = user.split(" ");
                    for (String item : items) {
                        if (item.startsWith("id=")) {
                            clientInfo.setId(StringUtil.getValueString(FLAG_EQUAL, item));
                        }
                        if (item.startsWith("addr=")) {
                            clientInfo.setAddr(StringUtil.getValueString(FLAG_EQUAL, item));
                        }
                        if (item.startsWith("age=")) {
                            clientInfo.setAge(StringUtil.getValueString(FLAG_EQUAL, item));
                        }
                        if (item.startsWith("db=")) {
                            clientInfo.setDb(StringUtil.getValueString(FLAG_EQUAL, item));
                        }
                    }
                    usersList.add(clientInfo);
                }
            }
        }
        return usersList;
    }

    /**
     * 获取Redis Key信息
     */
    public static KeyBean getKeyInfo(Jedis jedis, int index, String key, String order) {
        KeyBean keyBean = new KeyBean();
        if(!RedisUtil.isCluster(jedis)) {
            jedis.select(index);
        } else {
            key = key.replace(Constant.notGroupKeyPrefix, "");
            keyBean.setSlot(jedis.clusterKeySlot(key));
        }
        keyBean.setKey(key);
        keyBean.setType(jedis.type(key));
        keyBean.setTtl(jedis.ttl(key));
        switch (keyBean.getType()) {
            //set (集合)
            case "set":
                Set<String> set = jedis.smembers(key);
                StringBuilder setBuf = new StringBuilder();
                for (String info : set) {
                    setBuf.append(info).append(",");
                }
                String textSet = setBuf.toString();
                keyBean.setText(textSet.substring(0, textSet.length() - 1));
                keyBean.setJson(JSON.toJSONString(set));
                keyBean.setRaws(keyBean.getText().replace(",", "\r\n"));
                break;
            //list (列表)
            case "list":
                List<String> list = jedis.lrange(key, 0, -1);
                if (!StringUtils.isEmpty(order) && order.equals("desc")) {
                    Collections.reverse(list);
                }
                StringBuilder listBuf = new StringBuilder();
                for (String info : list) {
                    listBuf.append(info).append(",");
                }
                String textList = listBuf.toString();
                keyBean.setText(textList.substring(0, textList.length() - 1));
                keyBean.setJson(JSON.toJSONString(list));
                keyBean.setRaws(keyBean.getText().replace(",", "\r\n"));
                break;
            //zset (有序集)
            case "zset":
                Set<Tuple> zset = jedis.zrevrangeWithScores(key, 0, -1);
                List<Tuple> zsetList = new ArrayList<>(zset);
                if (StringUtils.isEmpty(order) || order.equals("asc")) {
                    Collections.reverse(zsetList);
                }
                StringBuilder zsetBuf = new StringBuilder();
                for (Tuple info : zsetList) {
                    zsetBuf.append(info.getElement()).append(",");
                }
                String textZset = zsetBuf.toString();
                keyBean.setText(textZset.substring(0, textZset.length() - 1));
                keyBean.setJson(JSON.toJSONString(zsetList));
                keyBean.setRaws(keyBean.getText().replace(",", "\r\n"));
                break;
            //hash (哈希表)
            case "hash":
                Map<String, String> map = jedis.hgetAll(key);
                StringBuilder mapBuf = new StringBuilder();
                for (Map.Entry entry : map.entrySet()) {
                    mapBuf.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
                String textMap = mapBuf.toString();
                keyBean.setText(textMap.substring(0, textMap.length() - 1));
                keyBean.setJson(JSON.toJSONString(map));
                keyBean.setRaws(keyBean.getText().replace(",", "\r\n"));
                break;
            //string (字符串)
            case "string":
                keyBean.setText(jedis.get(key));
                try {
                    JSON.parseObject(keyBean.getText());
                    keyBean.setJson(keyBean.getText());

                } catch (Exception e) {
                    keyBean.setJson(JSON.toJSONString(keyBean.getText()));
                }
                keyBean.setRaws(keyBean.getText());
                break;
        }
        keyBean.setSize(keyBean.getText().getBytes().length);
        return keyBean;
    }

    /**
     * 获取Redis配置信息
     */
    public static List<ConfigBean> getRedisConfig(Jedis jedis) {
        List<ConfigBean> confList = new ArrayList<>();
        List<String> configList = jedis.configGet("*");
        for (int i = 0; i < configList.size(); i++) {
            if (i % 2 != 0) {
                ConfigBean configBean = new ConfigBean();
                configBean.setKey(configList.get(i - 1));
                String value = configList.get(i);
                if (StringUtils.isEmpty(value)) {
                    configBean.setValue("");
                } else {
                    configBean.setValue(value);
                }
                confList.add(configBean);
            }
        }
        return confList;
    }

    /**
     * 修改redis配置信息
     */
    public static void setRedisConfig(Jedis jedis, Map<String, String> confMap) {
        for (String key : confMap.keySet()) {
            jedis.configSet(key, confMap.get(key));
        }
    }

    /**
     * 获取redis日志信息
     */
    public static List<Slowlog> getRedisLog(Jedis jedis) {
        return jedis.slowlogGet(100);
    }

    /**
     * 获取redis配置
     */
    public static int getDbCount(Jedis jedis) {
        return Integer.parseInt(jedis.configGet("databases").get(1));
    }

    /**
     * 执行redis信息
     */
    public static String getInfo(Jedis jedis, String info) {
        return jedis.info(info);
    }

    /**
     * 获取redis配置
     */
    public static List<String> getConf(Jedis jedis, String config) {
        return jedis.configGet(config);
    }


    public static void testCase(Jedis jedis) {
        /*-------------------String Test------------------------*/
        jedis.set("testString1", "testString1");
        jedis.set("testString2", "testString2");
        /*-------------------List Test--------------------------*/
        jedis.del("testList");
        jedis.lpush("testList", "list01");
        jedis.lpush("testList", "list02");
        jedis.lpush("testList", "list03");
        jedis.lpush("testList", "list04");
        jedis.lpush("testList", "list05");
        /*-------------------Set Test---------------------------*/
        jedis.sadd("testSet", "set-value01");
        jedis.sadd("testSet", "set-value02");
        jedis.sadd("testSet", "set-value03");
        jedis.sadd("testSet", "set-value04");
        jedis.sadd("testSet", "set-value05");
        /*-------------------Zset Test--------------------------*/
        jedis.zadd("testZset", 1, "set-value01");
        jedis.zadd("testZset", 2, "set-value02");
        jedis.zadd("testZset", 3, "set-value03");
        jedis.zadd("testZset", 4, "set-value04");
        jedis.zadd("testZset", 5, "set-value05");
    }


    public static boolean isCluster(Jedis jedis) {
        String config = RedisUtil.getInfo(jedis, "server");
        return config.contains("redis_mode:cluster");
    }

}
