# ScroamDB

一个独立的 Minecraft 数据库和经济系统插件，为其他插件提供数据存储和经济功能 API。

## 功能特性

### 数据库系统
- **世界表** - 存储世界相关数据
- **用户表** - 存储玩家数据
- **家系统** - 存储玩家家的位置
- **领地系统** - 存储领地信息和成员权限
- **地标系统** - 存储公共传送点信息

### 经济系统
- **余额管理** - 查询、存入、取出玩家余额
- **转账功能** - 支持玩家间转账
- **货币设置** - 可自定义货币名称

## API 使用

### 获取 API 实例

```java
ScroamDBAPI dbAPI = ScroamDBAPI.getInstance();
```

### 经济功能

```java
// 查询余额
double balance = dbAPI.getBalance(player.getUniqueId());

// 存入金额
dbAPI.deposit(player.getUniqueId(), 100.0);

// 取出金额
dbAPI.withdraw(player.getUniqueId(), 50.0);

// 转账
dbAPI.transfer(fromUuid, toUuid, 100.0);

// 获取货币名称
String currencyName = dbAPI.getCurrencyName();
```

### 家功能

```java
// 保存家
dbAPI.saveHome(player.getUniqueId(), "home1", location);

// 获取家
Location home = dbAPI.getHome(player.getUniqueId(), "home1");

// 删除家
dbAPI.deleteHome(player.getUniqueId(), "home1");

// 获取所有家
Map<String, Location> homes = dbAPI.getHomes(player.getUniqueId());
```

### 地标功能

```java
// 保存地标
dbAPI.saveWaypoint(waypoint);

// 获取地标
Waypoint waypoint = dbAPI.getWaypoint(waypointId);

// 获取所有地标
List<Waypoint> waypoints = dbAPI.getAllWaypoints();
```

## 配置文件

### config.yml

```yaml
# 数据库设置
database:
  type: sqlite
  file: data.db

# 经济设置
economy:
  currency-name: "金币"
  starting-balance: 0.0
```

## 安装

1. 下载 `ScroamDB-1.0.0.jar`
2. 放入服务器 `plugins` 目录
3. 启动服务器，插件会自动生成配置文件

## 依赖插件

无前置依赖，独立运行。

## API 版本

- Paper API 1.21.11

## 开发信息

- 作者: Scroam
- 版本: 1.0.0
- 数据库: SQLite

## 作为依赖使用

如果你的插件需要使用 ScroamDB 的 API，在 `plugin.yml` 中添加：

```yaml
depend: [ScroamDB]
```

然后在代码中获取 API 实例即可使用。

## 相关插件

- **ScroamPayment** - 支付系统插件，提供转账、税率抽成功能
- **ScroamTPA** - 传送系统插件，提供 TPA、RTP、Home、Waypoint 功能

## 许可证

MIT License