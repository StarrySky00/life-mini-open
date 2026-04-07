# 🏙️ Life-Mini 本地生活服务平台

> 💡 **在线演示/核心功能速览**
> <br/>

<details>
<summary>👉 <b>点击此处展开查看：AI 导购、一键代写与核心交互动图演示</b> 🚀</summary>
<br/>

<p align="center">
  <img src="https://github.com/user-attachments/assets/46dfe3e3-5c5f-4024-9a72-06b8cb8146b9" width="220" />
  <img src="https://github.com/user-attachments/assets/73738107-5a5a-4b3c-a858-b578d02c5b8a" width="220" />
  <img src="https://github.com/user-attachments/assets/c9bfb8b8-341d-4496-98ab-ceb5311335a3" width="220" />
  <img src="https://github.com/user-attachments/assets/9ff05ecd-9c19-4568-b86f-b205be616a30" width="220" />
</p>
</details>

## 📖 项目简介

**Life-Mini** 是一款轻量级的本地生活聚合服务平台（类似精简版大众点评）。项目致力于聚合周边的美食与便民服务（如快递站、药店、超市、同城活动），为用户提供便捷的位置搜索、商铺收藏与互动评论功能。

🚀 **核心亮点**：
* 🤖 **AI 赋能**：深度集成 Spring AI Alibaba，提供智能周边高性价比美食推荐、智能店铺风评总结、一键代写评价。
* 📍 **精准 LBS 服务**：基于高性能缓存实现的地理位置检索，快速发现“附近的好店”。
* 🛡️ **企业级高可用与风控**：内置一键全栈熔断、接口防刷限流、敏感词/违规图片双重审核，保障平台健康运行。

---

## 📸 更多界面展示

### 📱 微信小程序端 (用户视角)
<details>
<summary>👉 <b>点击此处展开查看：小程序前端页面截图</b> 📸</summary>
<br/>

<p align="center">
  <img src="https://github.com/user-attachments/assets/abf464af-40a3-4007-b93c-202fc5a3de45" width="260" alt="首页评分展示" />
  <img src="https://github.com/user-attachments/assets/8332b510-a25f-4fe9-9896-e3f7353d9ae3" width="260" alt="距离展示" />
  <img src="https://github.com/user-attachments/assets/8e81dfc1-cc6f-42e8-b44a-524f0f46935f" width="260" alt="历史评价展示" />
</p>
</details>

### 💻 Web 管理端 (管理员视角)
<details>
<summary>👉 <b>点击此处展开查看：Web 管理端页面截图</b> 🖥️</summary>
<br/>

**1. 数据控制台仪表盘**
<img src="https://github.com/user-attachments/assets/c1cca942-4379-4cc8-9dc7-be9769ed77de" width="800" />

**2. 用户与权限管理**
<img src="https://github.com/user-attachments/assets/2521edf4-c0a3-4898-840e-edaf7c6b958f" width="800" />

**3. 商铺动态管理**
<img src="https://github.com/user-attachments/assets/5f5a2f38-465f-41b6-89d0-dab0f062f6fb" width="800" />

**4. 评价审核**
<img src="https://github.com/user-attachments/assets/3f2f612a-fe3a-4bec-ac65-cdbf595b06cc" width="800" />

**5. 风控封禁**
<img src="https://github.com/user-attachments/assets/ba25cfdf-1afb-494a-8c73-638c9725acff" width="800" />
</details>

---

## 🛠️ 技术栈选型

| 模块 | 技术方案 | 说明 |
| :--- | :--- | :--- |
| **核心框架** | Spring Boot 3+ | 最新一代微服务基础设施 |
| **开发语言** | Java 17 | 拥抱新特性，如 Record、文本块等 |
| **AI 引擎** | Spring AI Alibaba | 无缝接入大模型能力 |
| **数据持久化** | MySQL 8.0+ & MyBatis-Plus | 关系型数据存储与高效 ORM |
| **多级缓存** | Redis & Caffeine | LBS 空间检索、热点数据与双Token校验 |
| **对象存储** | MinIO | 管理头像、商品封面等静态资源 |
| **安全与认证** | JWT (java-jwt) | 无状态的 Token 身份校验与角色授权 |
| **工具支持** | Knife4j, EasyExcel, Hutool, JavaMail | 接口文档生成、报表导出、高频工具类、邮件发送 |

---

## ✨ 核心功能模块

### 👤 1. 用户与认证中心
* 支持微信小程序一键登录及手机号绑定。
* 提供完整的用户生命周期管理：注册、登录、信息修改、头像上传与安全注销。
* **双重校验机制**：基于 Redis 缓存 Token，实现精准的在线用户状态管理与强制下线功能。

### 🏪 2. 商铺与内容管理
* **动态商铺**：支持商铺信息的全面管理（入驻、封面更换、详情编辑、下架）。
* **分类聚合**：灵活的商铺类型管理机制。

### 💬 3. 社交与互动系统
* **丰富评价**：支持图文并茂的评论发布，支持点赞、踩等互动态度表达。
* **私人收藏**：便捷的商铺收藏与取消机制。

### 🤖 4. 智能 AI 助手 (亮点,模型为：qwen-flash)
* 智能上下文对话与历史记录查询。
* 基于用户偏好的热门及附近商铺智能推荐。
* **智能文案**：根据用户标签一键代写高质量探店评价。
* **风评分析**：AI 自动汇总提取店铺的海量评论特征，生成风评摘要。

### 🛡️ 5. 安全防护与管理控制台 (Admin)
* **系统级熔断**：支持一键开启全栈熔断，从容应对突发流量。
* **智能风控**：
  * 限制单日注册与邮件发送频次，防止恶意刷量。
  * 接入敏感词过滤与违规图片审核，多次违规自动触发账号封禁。
  * 支持管理员手动封禁与踢出违规在线用户。
* **可视化数据面板**：实时追踪 PV/UV、用户增量、商铺增量，并支持通过 EasyExcel 一键导出运营报表。

### 📁 6. 智能文件服务
* 基于 MinIO 的统一文件路由。
* **垃圾回收机制**：实现待确认文件的临时记录与定时清理任务，避免无效资源占用存储空间。

---

## 🚀 快速开始

### 1. 克隆项目
``` bash
git clone git@github.com:StarrySky00/life-mini-open.git
cd life-mini-open
```
### 2. 数据库初始化
导入位于 src/main/resources/sql 目录下的 SQL 脚本文件。
### 3. 修改配置文件
将 application-example.yml 复制或重命名为 application-local.yml，并根据你的本地环境填入配置信息。

### 4. 启动服务与访问接口文档
运行主启动类 LifeMiniApplication.java。
项目启动成功后，浏览器访问：http://localhost:8080/doc.html 即可查看 Knife4j 生成的完整接口文档。

