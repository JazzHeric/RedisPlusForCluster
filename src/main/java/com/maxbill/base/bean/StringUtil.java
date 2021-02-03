package com.maxbill.base.bean;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static final String FLAG_COLON=":";

    public static final String FLAG_EQUAL="=";

    /**
     * 拆分key flag value形式字符返回key
     */
    public static String getKeyString(String falg, String tempStr) {
        if (!StringUtils.isEmpty(tempStr) && appearStringNumber(tempStr, falg) == 1) {
            String[] tempStrArray = tempStr.split(falg);
            return tempStrArray[0];
        } else {
            return "";
        }
    }

    /**
     * 拆分key flag value形式字符返回value
     */
    public static String getValueString(String falg, String tempStr) {
        if (!StringUtils.isEmpty(tempStr) && appearStringNumber(tempStr, falg) == 1) {
            String[] tempStrArray = tempStr.split(falg);
            return tempStrArray[1];
        } else {
            return "";
        }
    }

    /**
     * 获取指定字符串出现的次数
     */
    public static int appearStringNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }


    public static void main(String[] args) {
        String str = "# Server\n" +
                "redis_version:3.0.503\n" +
                "redis_git_sha1:00000000\n" +
                "redis_git_dirty:0\n" +
                "redis_build_id:d14575c6134f877\n" +
                "redis_mode:standalone\n" +
                "os:Windows  \n" +
                "arch_bits:64\n" +
                "multiplexing_api:WinSock_IOCP\n" +
                "process_id:2972\n" +
                "run_id:40573886d243b3286d9af903f749016da5230aee\n" +
                "tcp_port:6379\n" +
                "uptime_in_seconds:5636\n" +
                "uptime_in_days:0\n" +
                "hz:10\n" +
                "lru_clock:8712978\n" +
                "config_file:D:\\DEV\\redis\\server\\redis\\redis.windows.conf\n" +
                "\n" +
                "# Clients\n" +
                "connected_clients:1\n" +
                "client_longest_output_list:0\n" +
                "client_biggest_input_buf:0\n" +
                "blocked_clients:0\n" +
                "\n" +
                "# Memory\n" +
                "used_memory:692248\n" +
                "used_memory_human:676.02K\n" +
                "used_memory_rss:654472\n" +
                "used_memory_peak:735016\n" +
                "used_memory_peak_human:717.79K\n" +
                "used_memory_lua:36864\n" +
                "mem_fragmentation_ratio:0.95\n" +
                "mem_allocator:jemalloc-3.6.0\n" +
                "\n" +
                "# Persistence\n" +
                "loading:0\n" +
                "rdb_changes_since_last_save:0\n" +
                "rdb_bgsave_in_progress:0\n" +
                "rdb_last_save_time:1535433998\n" +
                "rdb_last_bgsave_status:ok\n" +
                "rdb_last_bgsave_time_sec:-1\n" +
                "rdb_current_bgsave_time_sec:-1\n" +
                "aof_enabled:0\n" +
                "aof_rewrite_in_progress:0\n" +
                "aof_rewrite_scheduled:0\n" +
                "aof_last_rewrite_time_sec:-1\n" +
                "aof_current_rewrite_time_sec:-1\n" +
                "aof_last_bgrewrite_status:ok\n" +
                "aof_last_write_status:ok\n" +
                "\n" +
                "# Stats\n" +
                "total_connections_received:13\n" +
                "total_commands_processed:52\n" +
                "instantaneous_ops_per_sec:1\n" +
                "total_net_input_bytes:982\n" +
                "total_net_output_bytes:15831\n" +
                "instantaneous_input_kbps:0.02\n" +
                "instantaneous_output_kbps:0.01\n" +
                "rejected_connections:0\n" +
                "sync_full:0\n" +
                "sync_partial_ok:0\n" +
                "sync_partial_err:0\n" +
                "expired_keys:0\n" +
                "evicted_keys:0\n" +
                "keyspace_hits:0\n" +
                "keyspace_misses:0\n" +
                "pubsub_channels:0\n" +
                "pubsub_patterns:0\n" +
                "latest_fork_usec:0\n" +
                "migrate_cached_sockets:0\n" +
                "\n" +
                "# Replication\n" +
                "role:master\n" +
                "connected_slaves:0\n" +
                "master_repl_offset:0\n" +
                "repl_backlog_active:0\n" +
                "repl_backlog_size:1048576\n" +
                "repl_backlog_first_byte_offset:0\n" +
                "repl_backlog_histlen:0\n" +
                "\n" +
                "# CPU\n" +
                "used_cpu_sys:0.03\n" +
                "used_cpu_user:0.09\n" +
                "used_cpu_sys_children:0.00\n" +
                "used_cpu_user_children:0.00\n" +
                "\n" +
                "# Cluster\n" +
                "cluster_enabled:0\n" +
                "\n" +
                "# Keyspace\n" +
                "db0:keys=3,expires=0,avg_ttl=0\n" +
                "\n";
    }

}
