#### 2022-01-06 更新
1.修复set zset list hash元素value因<input>标签中双引号显示问题，导致元素value不展示bug。

#### 2021-11-05 更新
1.由于redis cluster批量删除功能无法使用，在搜索框下增加一个批量删除框功能，适用于redis cluster环境。

#### 2021-10-25 更新
1.修复ssh登录问题，原逻辑中Jsch绑定本地端口后，Redis连接应使用本地IP及端口。

--------------------------------
#### 项目介绍
1.该项目基于RedisPlus dev3.2版本二次开发<br/>
原作者: MaxBill<br/>
原项目地址：https://gitee.com/MaxBill/RedisPlus<br/>
2.RedisPlus项目原作者已不再更新，但由于该项目并不支持CRC16算法Redis Cluster，故而进行简单改造。<br/>
3.SSH登录并不支持，但是由于jedis对于ssh登录没有支持，自行开发量较大，暂未进行改造。<br/>
4.项目基于JavaFx Html JS开发，也是一个非常不错的JavaFx Demo项目。


#### 打包说明

1.linux平台：需要安装fakeroot、rpm、alien库

2.windows：需要安装WiX Toolset、Inno Setup、microsoft .net framework库

#### 原应用截图
<p><img alt="" src="https://raw.githubusercontent.com/JazzHeric/RedisPlusForCluster/master/src/main/deploy/package/windows/original_desktop.jpeg" /></p>

#### 初次改造后截图

<p><img alt="" src="https://raw.githubusercontent.com/JazzHeric/RedisPlusForCluster/master/src/main/deploy/package/windows/new_desktop.png" /></p>


