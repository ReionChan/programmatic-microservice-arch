[![](https://img.shields.io/badge/release-v1.0-lightgray.svg)](https://github.com/ReionChan/programmatic-microservice-arch/)&nbsp;[![](https://img.shields.io/badge/license-Apache--2.0-orange.svg)](https://github.com/ReionChan/programmatic-microservice-arch/blob/main/LICENSE)&nbsp;[![](https://img.shields.io/badge/Author-ReionChan-cyan.svg)](https://reionchan.github.io/)

# 基于『Spring Cloud』编程式、非云原生的骨架

> **子曰：欣聞關注，樂見散佈！**&emsp;&emsp; <a href="https://github.com/ReionChan/programmatic-microservice-arch/stargazers"><img src="https://img.shields.io/github/stars/ReionChan/programmatic-microservice-arch?style=social&label=Star" title="关注" alt="关注" height="18" /></a>&emsp;<a href="https://github.com/ReionChan/programmatic-microservice-arch/network/members"><img src="https://img.shields.io/github/stars/ReionChan/programmatic-microservice-arch?style=social&label=Fork" title="关注" alt="关注" height="18" /></a>

## 为什么强调非原生？

&emsp;&emsp;这是个容器化、云端化的后微服务时代。

&emsp;&emsp;若从分布式微服务技术与解决方案演进角度来看，**后微服务时代**主张 “***向应用代码隐藏分布式架构复杂度、让分布式架构得以成为一种可普遍推广的普适架构风格***”[^1] 的技术流派，可称为 “仙术” 派。

&emsp;&emsp;而采用**足够灵活的编程式**微服务解决方案，是软硬件能够提供**透明化的不可变基础设施**出现之前的过渡方案，这个时期也被称作**微服务时代**，而采用编程式提供微服务的技术流派，不妨称作 “体术” 派。之所以在 “体术” 派的项目中着重强调其非 “仙术” 派，是为了增强对比性来观察体会架构迭代中的原由。

## 项目架构

![](https://raw.githubusercontent.com/ReionChan/PhotoRepo/master/arch/programmatic-microservice-arch%20Private.png)

## 项目运行

### dev 模式

```sh
# 容器启动必要依赖中间件: Nacos、Jaeger、OpenTelemetry Collector
docker compose up -d --build

# 然后在 IDE 运行模块，推荐顺序：
# 认证授权中心
arch-iam
# 用户模块
arch-users
# 测试应用
arch-app
# API 网关
arch-gateway
```

### test 模式

```sh
# 采用 test 配置 Docker Compose 方式启动所有模块
docker compose --profile test up -d --build
```

## Web API 端点

* 应用内部零信任网络端点认证端点 *OAuth2 Client - credentials 模式*（包含：后台服务、前台 Web 端服务、前端 App 端）

  ```sh
  # 示例演示 WEB 前端认证获得访问令牌
  POST http://localhost:9000/arch-iam/oauth2/token
  Content-Type: application/x-www-form-urlencoded
  Authorization: Basic YXJjaC13ZWI6c2VjcmV0d2Vi
  
  grant_type=client_credentials&scope=WEB
  ```

* 应用自身用户登录端点 *OAuth2 Client - password 模式* （即：己方或一方用户登录）

  > 🔔 系统初始化的用户账号及密码参考 `arch-user` 模块资源文件夹下面的 `data.sql`

  ```sh
  # 示例演示用户 wukong 使用 WEB 端登录获取访问、刷新令牌 
  POST http://localhost:9000/arch-iam/oauth2/token
  Content-Type: application/x-www-form-urlencoded
  Authorization: Basic YXJjaC13ZWI6c2VjcmV0d2Vi
  
  grant_type=password&scope=WEB&username=wukong&password=wukong
  ```

* 应用自身用户访问令牌刷新端点

  ```sh
  # 示例演示用户 wukong 使用 WEB 端刷新令牌 
  POST http://localhost:9000/arch-iam/oauth2/token
  Content-Type: application/x-www-form-urlencoded
  Authorization: Basic YXJjaC13ZWI6c2VjcmV0d2Vi
  
  grant_type=refresh_token&scope=WEB&refresh_token=kGrXegF9RW2zqwvMl_NvAc47YtIsVMy_eSV-P7MgmKPwPmS8Ov1mF0qLe7Z2L-FBmfMmGooQlkLHqdl0vn7QM_BRT88D5mL73W-7bEn6bByprP1uIyxS3gmo7sC2OJWk
  ```

* 登录用户访问受限资源测试端点

  ```sh
  # 示例演示用户 wukong 使用登录令牌认证方式访问 arch-app 下的受限资源 /ping
  GET http://localhost:9000/arch-app/ping
  Authorization: Bearer eyJraWQiOiI2ZTQxNTE4NS05YWU3LTRkZjgtYjU5MS0zZTU5NWZhYzgwNTIiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ3dWtvbmciLCJhdWQiOiJhcmNoLXdlYiIsIm5iZiI6MTcxODA5OTkzOCwic2NvcGUiOlsiV0VCIl0sInJvbGVzIjpbIlVTRVIiXSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkwIiwiZXhwIjoxNzE4MTAwMjM4LCJpYXQiOjE3MTgwOTk5MzgsImp0aSI6ImQ5NGVkNzMwLTA2MjItNGM1OS05YzYyLTljMmJjMzlhNmNjZSJ9.SUrLC7Jy3azs6apyaZ3s6rZdQCX2WvZPtgPcEPTXpq2gBQYgXaj-fhn_iU59fvAuHWitfwTOl7dnlnTArSubAsXtDQjYrCLMViItXYbJFan683sZPkaxnUYVZlMNjQTcsvkH9YR13p2ZHf_YNN4dgnvS2Meup41L9uJLvfcfMAuRanZFzsoCUlGSkeGJyaHME5VeaVt-U8fDLsv9xAnWwDoXN4wCYf5CEBPm8zw5QPcc0Wg4CM7o8RaxdFFXuXjC7O8XgXMm48zj3j2GzVnrf6rZrl_zXri7aFm99RS_-FZcoIrS2NbCH27QUKtgwANV-mmeTwG04eDhcOS1mhHGew
  
  ```

## 使用技术栈

* 服务注册与发现
  * Alibaba Nacos
* 负载均衡
  * Spring Cloud Loadbalancer
* 服务容错
  * Spring Cloud Circuitbreak
  * Resilience4J
* RPC
  * Spring Cloud OpenFeign
* 认证授权
  * Spring Security OAuth2 Server
  * Spring Security OAuth2 Client
  * Spring Security OAuth2 Resource
* 可观测性
  * Micrometer （统一埋点 API）
  * OpenTelemetry Java Agent （统一采集方式）
  * OpenTelemetry Collector （统一 OTLP 协议收集，隔离不同监控提供商）
    * 指标数据观测，包括不限于：Prometheus、Grafana、
    * 追踪数据观测，包括不限于：Jaeger、Zipkin、Tempo
    * 日志数据观测，包括不限于：ELK、Loki

## 云原生基础设施可替代

|          | 基于 Spring Cloud 编程式                                     | 基于 K8S 云原生基础设施 |
| -------- | ------------------------------------------------------------ | ----------------------- |
| 弹性伸缩 | ——                                                           | Autoscaling             |
| 服务发现 | Spring Cloud Alibaba Nacos / Netflix Eureka                  | KubeDNS / CoreDNS       |
| 配置中心 | Spring Cloud Config Alibaba Nacos / Azure App Configuratioin | ConfigMap / Secret      |
| 服务网关 | Spring Cloud Gateway                                         | Ingress Controller      |
| 负载均衡 | Spring Cloud Loadbalancer                                    | Load Balancer           |
| 服务安全 | Spring Security OAuth2                                       | RBAC API                |
| 监控追踪 | Micrometer Tracing                                           | Metrics API / Dashboard |
| 熔断降级 | Spring Cloud Circuit Breaker with Resilience4J / Spring Retry | Istio Envoy             |

[^1]: [凤凰架构 - 从微服务到云原生](https://icyfenix.cn/immutable-infrastructure/msa-to-cn.html)