# 🤖 NexIoT AI物联网平台

<div align="center">

![License](https://img.shields.io/badge/license-AGPL3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.5-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-5.7+-blue.svg)
![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)
![AI](https://img.shields.io/badge/AI-Enabled-purple.svg)

## 🎯 基于AI驱动的"零代码侵入"企业级物联网平台

**让AI赋能物联网，让设备接入像搭积木一样简单**

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
- [📄 开源协议](#-开源协议)

## ✨ 平台简介

**NexIoT AI物联网平台** 是一款基于Java技术栈构建的"零代码侵入"企业级物联网平台，融合AI智能能力，实现设备接入的智能化、自动化。平台具备高内聚低耦合架构，真正实现零代码侵入式设备接入。

### 🎯 核心价值
- **🤖 AI加持**：物模型定义、设备协议接入，AI一把梭
- **🚀 零代码接入**：可视化配置，无需编写一行代码即可完成设备接入
- **🔧 全协议支持**：TCP、MQTT、Modbus、S7、天翼物联、HTTP等主流协议
- **🏗️ 企业级架构**：支持前万级设备接入，高可用、高性能
- **📦 开箱即用**：完整开源、导入即可运行，快速部署上线

## 🌟 平台亮点

### 🚀 零代码侵入体验
- ✅ **可视化配置**：拖拽式界面设计，无需编程基础，`大学生`即可完成设备接入
- ✅ **外置调试器**：基于浏览器的AI调试IDE，实时协议开发和调试
- ✅ **一键部署**：配置完成后一键部署，系统自动处理所有技术细节
- ✅ **热插拔支持**：设备接入无需重启系统，支持在线热更新

### 🔧 全协议生态支持
- ✅ **工业协议**：Modbus RTU/TCP、S7、OPC-UA等工业标准协议
- ✅ **物联网协议**：MQTT、CoAP、HTTP等主流IoT协议
- ✅ **网关设备**：支持网关设备接入，统一管理子设备
- ✅ **云平台对接**：天翼物联、阿里云IoT、华为云IoT等云平台集成


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
- **协议支持**：TCP、MQTT、Modbus、S7、OPC-UA、HTTP、WebSocket
- **前端技术**：基于`RuoYi-Antdv`构建，感谢开源社区！

#### 💾 数据存储
- **关系数据库**：MySQL 5.8+ 
- **缓存系统**：Redis 6.0+ 
- **时序数据库**：ClickHouse / IoTDB（配置开启）

## 📈 正在推进

### 🚀 近期规划
- **🤖 AI能力增强**：GPT集成、自然语言协议配置、智能故障诊断
- **📱 移动端应用**：付费图鸟定制UI，具备指令控制、属性、告警查看，轻量化、多管理员

### 🌟 长期愿景
- **🧠 深度学习**：设备行为学习、预测性维护、智能优化
- **🌐 生态建设**：插件市场、第三方集成、开发者社区
- **🏭 行业方案**：智能制造、智慧城市、智慧农业等垂直解决方案


## 💡 最佳实践

### 🎯 零代码接入实战案例

我们基于真实设备进行实战演示，让您体验真正的"零代码侵入"接入过程。通过AI智能识别和可视化配置，即使是初学者也能快速完成设备接入。

#### 📊 设备接入案例
- **NB水浸传感器**：通过天翼物联平台接入，AI自动识别协议格式
- **DL/T645电表**：TCP直连接入，智能解析电表数据
- **Modbus设备**：工业设备接入，自动识别RTU/TCP模式

#### 🚀 接入优势
- **零代码+AI**：得力于“零代码侵入”+AI,减少60%研发、运维、测试所需的时间和人力成本
- **实时调试**：基于浏览器的AI调试IDE，所见即所得
- **快速部署**：配置完成后一键部署，无需重启系统

> 💡 **实践建议**：单看文档不如对照实物从0到1动手实践更高效，在实际操作中会有更多收获！

|                                                 |                                      | 
|-------------------------------------------------|--------------------------------------|
| ![电表设备](/__MACOSX/shot/1018/dian.png)           | ![摄像头](/__MACOSX/shot/1018/v22.png)  | 
| ![网关DTU](/__MACOSX/shot/1018/1760431601441.jpg) | ![水浸设备](/__MACOSX/shot/1018/111.jpg) | 

## 🎥 AI驱动的视频教程

基于真实设备的详细视频教程，让您体验真正的智能化接入过程。

### 📡 天翼物联AI接入教程

#### 🌊 NB水浸传感器系列

1. **【手把手实战：水浸传感器开箱：初见与硬件解析】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1kLY5z5EMH/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：设备开箱、硬件介绍、AI智能识别功能测试

2. **【手把手实战：NexIoT接入Ctwing参数配置】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1jVY7zVEjM/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：AI辅助天翼物联平台参数配置、智能设备注册流程

3. **【手把手实战：NB水浸传感器CTWing接入全流程】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1sJaZzBEfr/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：AI驱动的完整设备接入流程，从智能配置到数据上报

### 🔌 TCP直连AI接入教程

#### ⚡ DL/T645电表系列

1. **【NexIoT开源】手把手实战：DL/T645电表TCP直连接入】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1x4pBzZEVN/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：AI智能TCP协议解析、零代码设备直连配置、智能数据解析

2. **【NexIoT开源】手把手实战：产品创建与SNI解读】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1zSWNzoETF/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：AI辅助产品创建流程、智能SNI设备标识解析、AI协议调试

3. **【NexIoT开源】手把手实战：如何写TCP接入的DL/T645电表解析脚本】**
    - 📺 [B站观看](https://www.bilibili.com/video/BV1SAWVzwEZE/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff)
    - 📝 内容：AI数据流介绍、智能调试IDE的编写和调试、645电表的AI解析

#### ⚡ 接入成果展示（电表外接灯泡智能控制）

![AI接入成果动态](__MACOSX/shot/dianbiao.gif)

### 🎯 更多教程

我们正在持续制作更多设备的AI接入教程，包括Modbus、S7、网关设备等，尽情期待。非常希望得到您的建议！

## 📸 平台界面展示

### 🏗️ 驱动的系统架构

![系统架构](/__MACOSX/shot/dataflow.png)

### 🚀 云原生部署架构

![云原生部署](/__MACOSX/shot/deploy.jpg)

### 🖥️ 智能化功能展示

#### 🌐 国际双语支持

![img.png](__MACOSX/shot/1018/en.png)

#### 🌐 驱动的天翼产品接入

![天翼产品接入](__MACOSX/shot/99.gif)

#### 🔄 流程编排引擎

![流程编排](__MACOSX/shot/53.png)

#### 🛠️ 协议统一管理与智能IDE

![协议管理与IDE](__MACOSX/shot/54.png)

### 🎯 功能深度展示

|                                          |                                       | 
|------------------------------------------|---------------------------------------|
| ![产品管理](/__MACOSX/shot/1018/pd.jpg)     | ![设备管理](/__MACOSX/shot/1018/sb.jpg)  |
| ![视频监控](/__MACOSX/shot/1018/video.jpg)  | ![网络管理](/__MACOSX/shot/1018/net.jpg) |
| ![数据管理1](/__MACOSX/shot/1018/d1.jpg)     | ![数据管理2](/__MACOSX/shot/1018/d2.jpg)  |
| ![运维管理](/__MACOSX/shot/1018/yz.jpg)     | ![设备状态](/__MACOSX/shot/1018/zsb.jpg) |
| ![设备面板](/__MACOSX/shot/1018/zsb-pb.jpg) | ![系统配置](/__MACOSX/shot/1018/zt.jpg)  |
| ![报警管理](/__MACOSX/shot/1018/bjm.jpg) | ![数据采集](/__MACOSX/shot/1018/cj.jpg)  |


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

| QQ群                           | 微信                            | B站                               | 抖音                               |
|-------------------------------|-------------------------------|----------------------------------|----------------------------------|
| ![QQ群](/__MACOSX/shot/qq.png) | ![微信群](/__MACOSX/shot/wx.png) | ![B站](/__MACOSX/shot/bzhan.jpg) | ![抖音](/__MACOSX/shot/douyin.jpg) |

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
