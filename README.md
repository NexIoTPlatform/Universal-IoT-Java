# 🤖 NexIoT AI物联网平台

<div align="center">

![License](https://img.shields.io/badge/license-AGPL3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.5-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-5.7+-blue.svg)
![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)
![AI](https://img.shields.io/badge/AI-Enabled-purple.svg)

## 🎯 创新的"真·零代码侵入"物联网平台

**突破传统物联网平台设计思路 · 设备驱动完全外置 · 一键导出即用 · 零代码侵入**

[📖 文档地址](https://nexiotplatform.github.io/universal-iot-docs/) | [🌐 在线演示](http://iot.192886.xyz:81/) | [🔧 AI调试IDE](http://iot.192886.xyz:81/magic/debug/index.html)

</div>

## 📋 目录

- [✨ 平台简介](#-平台简介)
- [🌟 平台亮点](#-平台亮点)
- [🚀 快速开始](#-快速开始)
- [📦 运行部署](#-运行部署)
- [🎯 核心功能](#-核心功能)
- [🏗️ 技术架构](#️-技术架构)
- [📈 正在推进](#-正在推进)
- [💡 最佳实践](#-最佳实践)
- [🎥 视频教程](#-视频教程)
- [📸 界面展示](#-界面展示)
- [📝 更新日志](#-更新日志)
- [🤝 贡献指南](#-贡献指南)
- [🛠️ 技术支持](#️-技术支持与服务)
- [📄 开源协议](#-开源协议)

## ✨ 平台简介

**NexIoT AI物联网平台** 是一款采用创新架构设计的**真·零代码侵入**企业级物联网平台。

### 🎯 核心亮点

**🚀 这个项目能为你做什么？**  
告别繁重的设备驱动开发，告别重复造轮子。通过解耦设计，你可以专注业务逻辑，复用他人成果，快速构建物联网应用。

**🔧 解耦设计，共建共享**  
基于实际项目经验，将设备驱动与平台核心完全解耦。用户可导出完整产品包（物模型+驱动源码），NexIoT用户之间可直接共享使用。你对接的设备，我可直接导入；我开发的驱动，你拿来即用。

**⚡ 零代码侵入，热插拔架构**  
一键导入，无需修改平台代码，无需重新编译部署。设备驱动在线热更新，新增设备类型零停机。稳定运行几年无需重启维护。

### 🎯 核心价值

- **🚀 零代码侵入**：设备驱动完全外置，一键导出源码直接使用，无任何代码侵入
- **🔧 解耦架构**：驱动与平台完全分离，热插拔式设备接入，生态共享
- **🏗️ 全协议支持**：TCP、MQTT、Modbus、天翼物联、HTTP等主流协议
- **⚡ 企业级性能**：支持`集群`部署，支持千万级设备接入，高可用、高性能、国际化

## 🌟 平台亮点

- ✅ **设备驱动外置**：编解码逻辑在浏览器IDE中实现，与平台核心代码零耦合
- ✅ **一键导出**：产品物模型 + 设备驱动源码，打包导出直接注册使用
- ✅ **零代码侵入**：无需修改平台代码，无需重新编译部署，真正的零侵入
- ✅ **热插拔架构**：设备驱动在线热更新，新增设备类型零停机
- ✅ **可视化配置**：Web界面完成所有配置，基于浏览器的调试IDE
- ✅ **全协议支持**：Modbus RTU/TCP、MQTT、HTTP等工业协议和物联网协议
- ✅ **云平台对接**：天翼物联、移动OneNet等云平台集成

## 🚀 快速开始

### 🎯 演示地址

- **演示地址**: <http://iot.192886.xyz:81/>
- **调试IDE**: <http://iot.192886.xyz:81/magic/debug/index.html>
- **演示账号**: test
- **演示密码**: Haha@2025
- **文档地址**: <https://nexiotplatform.github.io/universal-iot-docs/>
- **文档地址(国内)**: <http://docs.192886.xyz:81/>

## 🏗️ 技术架构

### 🛠️ 技术栈

#### 🚀 核心框架（极简轻量）

- **后端框架**：`Java 21` `SpringBoot 3.5` `Tk.Mybatis 5.0.1`
- **前端技术**：基于`RuoYi-Antdv`构建，感谢开源社区！

#### 💾 数据存储

- **关系数据库**：MySQL 5.8+
- **缓存系统**：Redis 6.0+
- **时序数据库**：ClickHouse / IoTDB（配置开启）

## 📈 正在推进

### 🚀 近期规划

- **🤖 AI能力增强**：智能故障诊断
- **📱 移动端应用**：付费图鸟定小程序，具备指令控制、属性、告警查看，轻量化、多管理员

### 🌟 长期愿景

- **🧠 深度学习**：设备行为学习、预测性维护、智能优化
- **🌐 生态建设**：插件市场、第三方集成、开发者社区
- **🏭 行业方案**：智能制造、智慧城市、智慧农业等垂直解决方案

## 📸 实战案例与视频教程

### 📊 支持设备类型

|                                                |                                            | 
|------------------------------------------------|--------------------------------------------|
| ![电表设备](/__MACOSX/shot/1018/dianbiap.png)      | ![摄像头](/__MACOSX/shot/1018/lechen.png)     | 
| ![网关DTU](/__MACOSX/shot/1018/dtu.jpg)          | ![水浸设备](/__MACOSX/shot/1018/wenshidu.jpg)  | 
| ![声光报警器](/__MACOSX/shot/1026/device-sgbjq.png) | ![水浸设备](/__MACOSX/shot/1026/device-sj.jpg) |

#### ⚡ 接入成果展示

![接入成果动态](__MACOSX/shot/dianbiao.gif)

---

## 📸 平台界面展示

### 🏗️ 驱动的系统架构

![系统架构](/__MACOSX/shot/dataflow.png)

### 🚀 云原生部署架构

![云原生部署](/__MACOSX/shot/deploy.jpg)

### 🖥️ 智能化功能展示

#### 🌐 天翼产品接入

![天翼产品接入](__MACOSX/shot/99.gif)


#### 📺 产品一览图

![产品一览图](__MACOSX/shot/1026/product.png)

#### 🔄 网关接入各种不同子设备(DTU、Modbus)

![子设备](__MACOSX/shot/1026/gw.png)

#### 🛠️ 所有协议可视化管理和在线调试

![协议管理与IDE](__MACOSX/shot/1026/ide.png)

#### 🔄 流程编排引擎

![流程编排](__MACOSX/shot/53.png)



### 🎯 功能深度展示

|                                          |                                                 | 
|------------------------------------------|-------------------------------------------------|
| ![设备管理](/__MACOSX/shot/1026/devices.png) | ![系统配置](/__MACOSX/shot/1026/zt.png)             |
| ![视频监控](/__MACOSX/shot/1026/video.png)   | ![设备管理](/__MACOSX/shot/1026/zhuapai.png)        |
| ![数据管理1](/__MACOSX/shot/1026/d1.png)     | ![数据管理2](/__MACOSX/shot/1026/d2.png)            |
| ![运维管理](/__MACOSX/shot/1026/xieyi.png)   | ![网络管理](/__MACOSX/shot/1026/net.png)            |
| ![报警管理](/__MACOSX/shot/1026/notice.png)  | ![数据采集](/__MACOSX/shot/1026/notice-log.png)     |
| ![联动](/__MACOSX/shot/1026/liandong.png)  | ![数据采集](/__MACOSX/shot/1026/product-detail.png) |

### 🎯 移动端/小程序

![app](__MACOSX/shot/1026/app.jpg)


### 📡 天翼物联AI接入教程

#### 🌊 NB水浸传感器

1. **【手把手实战：水浸传感器开箱：初见与硬件解析】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1kLY5z5EMH/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：设备开箱、硬件介绍、AI智能识别功能测试

2. **【手把手实战：NexIoT接入Ctwing参数配置】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1jVY7zVEjM/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：天翼物联平台参数配置、智能设备注册流程

3. **【手把手实战：NB水浸传感器CTWing接入全流程】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1sJaZzBEfr/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：外置驱动的完整设备接入流程，从智能配置到数据上报

### 🔌 TCP直连AI接入教程

#### ⚡ DL/T645电表

1. **【NexIoT开源】手把手实战：DL/T645电表TCP直连接入】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1x4pBzZEVN/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：TCP协议解析、设备直连配置、智能数据解析

2. **【NexIoT开源】手把手实战：产品创建与SNI解读】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1zSWNzoETF/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：创建流程、智能SNI设备标识解析、AI协议调试

3. **【NexIoT开源】手把手实战：如何写TCP接入的DL/T645电表解析脚本】**
   -
   📺 [B站观看](https://www.bilibili.com/video/BV1SAWVzwEZE/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：数据流介绍、智能调试IDE的编写和调试、645电表的AI解析


## 📝 更新日志

我们维护详细的更新日志，记录每个版本的重要变更和新功能。

### 📖 完整更新日志

查看完整的更新历史，请访问：[📝 CHANGELOG.md](./CHANGELOG.md)

---

## 🤝 贡献指南

我们热烈欢迎所有形式的贡献！无论是代码、文档、问题反馈还是功能建议，您的参与将推动物联网平台的发展。

### 🚀 如何贡献

#### 📋 贡献流程

1. **Fork 项目**
   ```bash
   # 点击右上角 Fork 按钮，创建您的项目副本
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/ai-enhanced-protocol-support
   git checkout -b fix/zero-code-debugging-issue
   git checkout -b docs/improve-ai-tutorials
   ```

3. **提交代码**
   ```bash
   git add .
   git commit -m "feat: add AI protocol recognition for Modbus devices"
   git push origin feature/ai-enhanced-protocol-support
   ```

4. **创建 Pull Request**
    - 在 Gitee 上创建 Pull Request
    - 详细描述您的修改内容和增强功能
    - 等待代码审查和功能验证

#### 🌟 社区价值观

- **🤝 相互尊重**：欢迎所有开发者加入，有问题我们第一时间回复
- **⭐ 真诚支持**：喜欢请点star，不喜欢也请尊重，不要恶意行为
- **🚀 共同成长**：分享技术经验，推动物联网智能化发展
- **💡 开放创新**：鼓励功能创新和零代码技术改进

#### 📢 重要声明

- **自媒体转发**：欢迎转发项目信息，请遵循AGPL3.0开源协议
- **企业授权**：商业使用需获得授权，保留企业授权信息
- **法律追责**：如单位误用引起法律后果，保留追责责任

#### 🌐 社区联系方式

| QQ群                           | 微信                            | B站                              | 抖音                               |
|-------------------------------|-------------------------------|---------------------------------|----------------------------------|
| ![QQ群](/__MACOSX/shot/qq.png) | ![微信群](/__MACOSX/shot/wx.png) | ![B站](/__MACOSX/shot/bzhan.jpg) | ![抖音](/__MACOSX/shot/douyin.jpg) |

## 🛠️ 技术支持与服务

我们提供全方位的技术支持服务，从基础答疑到企业级解决方案，满足不同层次的需求。

### 📋 服务类型对比

| 服务类型          | 服务内容                                 | 价格           | 适用场景            | 联系方式                                                         |
|---------------|--------------------------------------|--------------|-----------------|--------------------------------------------------------------|
| **🆓 社区技术答疑** | • 基础问题答疑<br>• 使用指导<br>• 社区交流         | **免费**       | 学习、使用           | QQ群、微信群                                                      |
| **📚 技术文档**   | • 技术文档<br>• 实践指南<br>• 视频教程           | **免费**       | 自学、参考           | [文档地址](https://nexiotplatform.github.io/universal-iot-docs/) |
| **☁️ 数据初始化**  | • 初始化字段数据<br>• 演示环境全部数据<br>     | **赞助¥99**    | 数据初始化           | 联系技术                                                         |
| **☁️ 线上部署服务** | • 客户提供硬件<br>• 完成系统部署<br>• 云服务部署      | **赞助¥169**     | 快速上线、云部署        | 联系技术                                                         |
| **🔧 专属技术支持** | • 各类对接支持<br>• 使用指导<br>• 部署方案<br>• 解决方案 | **赞助¥100/1小时** | 远程支持            | 联系技术                                                         |
| **⚡ 设备接入服务**  | • IDE解析源码<br>• 产品配置导出<br>• 定制化接入     | **¥1000/日**  | 复杂设备接入、定制开发     | 联系客服                                                         |
| **🏢 企业省心服务** | • 商业版本地部署<br>• 代运维服务<br>• 5×8技术支持    | **¥5,999/年** | 仅剩2名，企业级应用、长期运维 | 联系客服                                                         |

---

## 📄 开源协议与授权

### 📜 详细授权条款

#### ✅ 授权用户权益

1. **内部使用**：授权用户可无限期在其内部使用
    - 企业授权：不含分公司、子公司及第三方公司
    - 个人授权：仅限个人使用，不可用于在职公司或第三方

2. **开发权限**：授权用户可通过项目形式进行源码二次开发
    - 定制化软件必须加密打包后交付客户
    - 如需交付源码，必须为客户购买对应商业授权

#### ❌ 授权限制

1. **禁止转让**：不可向第三方发行或转让授权源码
    - 无论有偿或无偿转让
    - 不可申请包含本项目的软件著作权

### 🔒 开源和商业

开源版本暂不包含 **TCP**、**天翼物联ctaiot** 模块。可自行二次开发扩展，商业用途需赞助项目，获得授权。

### 🙏 致谢

感谢以下开源项目和技术平台：

- **开源框架**：若依、Antdv、jetlink、ssssssss-team
- **云平台**：阿里云、华为云、腾讯云、AEP、OneNet 等物联网平台
- **社区支持**：所有贡献者和用户的支持与反馈
