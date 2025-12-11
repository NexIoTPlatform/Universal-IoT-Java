# 🤖 NexIoT AI IoT Platform

<div align="center">

![License](https://img.shields.io/badge/license-AGPL3.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![SpringBoot](https://img.shields.io/badge/SpringBoot-3.5-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)
![IoTDB](https://img.shields.io/badge/IoTDB-Supported-green.svg)
![ClickHouse](https://img.shields.io/badge/ClickHouse-Supported-yellow.svg)
![AI](https://img.shields.io/badge/AI-Enabled-purple.svg)

## 🎯 Innovative "True Zero-Code Intrusion" IoT Platform

> **💡 Breaking Traditional IoT Platform Design · Fully Externalized Device Drivers · One-Click Export · Zero Code Intrusion**

[📖 Documentation](https://nexiotplatform.github.io/universal-iot-docs/) | [🌐 Live Demo](http://iot.192886.xyz:81/) | [🔧 AI Debug IDE](http://iot.192886.xyz:81/magic/debug/index.html)

**[中文](README.md) | English**

</div>

## ✨ Platform Introduction

**NexIoT AI IoT Platform** is an enterprise-level IoT platform with innovative architecture design featuring **True Zero-Code Intrusion**.

### 🎯 Core Highlights

> **🚀 What Can This Project Do For You?**

- 🏢 **Perfect for Medium & Large Enterprises**: **IoT Infrastructure Center, Unified Device Data Access**, ideal for building your own products and B|G projects
- 🔓 **No More Vendor Lock-in**: Free yourself from device vendors and software providers who charge exorbitant prices
- 🎓 **Easy to Get Started**: Simple debugger, accessible even to college students, saving massive R&D, testing, and O&M costs
- 🤝 **Ecosystem Co-construction**: One-click export of products, thing models, and drivers for ecosystem sharing
- ⚡ **Real-time Hot Deployment**: **Real-time hot deployment**, from 0 to 100 device integrations without service restart for years
- 🚀 **High Availability Cluster**: Open-source version supports clustering for millions of devices

## 🌟 Platform Features

- ✅ **Zero Code Intrusion**: Externalized device drivers, no platform code modification needed, no recompilation or redeployment, zero coupling with core platform code
- ✅ **Full Protocol Support**: TCP, Modbus RTU/TCP, MQTT, HTTP and other industrial and IoT protocols
- ✅ **Cloud Platform Integration**: Integration with platforms like CTIoT, China Mobile OneNet, WVP GB28281 video standards
- ✅ **Multi-Database Support**: Supports **MySQL 8.0+**, **IoTDB**, **ClickHouse** and other relational and time-series databases

## 🏗️ Technical Architecture

### 🛠️ Tech Stack

#### 🚀 Core Framework (Extremely Lightweight)

- **Backend Framework**: `Java 21` `SpringBoot 3.5` `Tk.Mybatis 5.0.1` 
- **Frontend Technology**: Built on `RuoYi-Antdv`, thanks to the open-source community!
- **Log Storage**: **IoTDB** / **ClickHouse** / MySQL / None (Production-grade seamless dynamic switching)

## 🧭 Deployment & Startup (One-Click Start)

###  The image is the Enterprise edition image (preview) as of 5 December 2025, including a closed-source access agreement!

- **One-Click Start**: `docker-compose up -d`
- **Access URLs**:
  - Admin Panel: `http://localhost:80` (Default: `nexiot/nexiot@123321`)
  - IDE Debugger: `http://localhost:9092/magic/debug/index.html` (Same password as admin)
  - EMQX Management: `http://localhost:18083` (Default: `admin/public`)

> **🔧 One-click startup includes database and initialization data (SQL), help yourself!**

## 📈 Ongoing Development

### 🚀 Roadmap

- **🚀 WVP Video Platform Integration**: Integration with WVP video platform series (in beta testing)
- **🚀 Dahua ICC Product Series**: Integration with Dahua ICC product series
- **🚀 Hikvision Integrated Security Platform**: Integration with Hikvision platform product series
- **📱 Mobile Application**: Paid mini-program with command control, property viewing, alarm monitoring, lightweight, multi-admin support

## 🚀 Quick Start

### 🎯 Demo Site (Latest Version)

> **✨ All Real Devices, Driver Source Code Open, Fully Transparent!**

> **💎 Exclusively sponsored by [Aeolian Cloud](https://www.aeoliancloud.com/cart/goodsList.htm) for NexIoT online demo server**

- **🌐 Demo Site**: <http://demo.nexiot.cc/>
- **🔧 Debug IDE**: <http://demo.nexiot.cc/magic/debug/index.html>
- **👤 Demo Account**: `test`
- **🔑 Demo Password**: `nexiot@123321`
- **📖 Documentation**: <https://docs.nexiot.cc/>
- **🔗 Backup URL**: <http://iot.192886.xyz:81>

## 📸 Real-World Cases & Video Tutorials

### 📊 Device Integration Cases (Partial Display)

|                                                |                                                |                                        |
|------------------------------------------------|------------------------------------------------|----------------------------------------|
| ![Electric Meter](/__MACOSX/shot/1018/dianbiap.png) | ![Camera](/__MACOSX/shot/1018/lechen.png) | ![Water Leakage Device](/__MACOSX/shot/1018/111.jpg) |
| ![Gateway DTU](/__MACOSX/shot/1018/dtu.jpg) | ![Water Leakage](/__MACOSX/shot/1026/device-sj.jpg) | ![SOS](/__MACOSX/shot/1109/sos.jpg) |
| ![4G Tracker](/__MACOSX/shot/1018/4gcz.png) | ![Sound-Light Alarm](/__MACOSX/shot/1026/device-sgbjq.png) | ![Water Sensor](/__MACOSX/shot/1018/4gcz2.png) |

#### ⚡ Integration Showcase

![Integration Demo](__MACOSX/shot/dianbiao.gif)

---

## 📸 Platform Interface Display

### 🏗️ Driver System Architecture

![System Architecture](/__MACOSX/shot/dataflow.png)

### 🚀 Cloud-Native Deployment Architecture

![Cloud-Native Deployment](/__MACOSX/shot/deploy.jpg)

### 🖥️ Intelligent Features Showcase

#### 📺 Product Overview

![Product Overview](__MACOSX/shot/1026/product.png)

#### 🔄 Gateway Access to Various Sub-devices (DTU, Modbus)

![Sub-devices](__MACOSX/shot/1026/gw.png)

#### 🛠️ All Protocols Visualized Management & Online Debugging

![Protocol Management & IDE](__MACOSX/shot/1026/ide.png)

#### 🔄 Workflow Orchestration Engine

![Workflow Orchestration](__MACOSX/shot/53.png)

### 🎯 In-depth Feature Display

|                                          |                                                 |
|------------------------------------------|-------------------------------------------------|
| ![Device Management](/__MACOSX/shot/1026/devices.png) | ![System Config](/__MACOSX/shot/1026/zt.png) |
| ![Video Monitoring](/__MACOSX/shot/1109/lechen.png) | ![Device Management](/__MACOSX/shot/1026/zhuapai.png) |
| ![O&M Management](/__MACOSX/shot/1026/xieyi.png) | ![Network Management](/__MACOSX/shot/1026/net.png) |
| ![Alarm Management](/__MACOSX/shot/1026/notice.png) | ![Data Collection](/__MACOSX/shot/1026/notice-log.png) |
| ![Linkage](/__MACOSX/shot/1026/liandong.png) | ![Data Collection](/__MACOSX/shot/1109/product-detail.png) |
| ![Data Management 1](/__MACOSX/shot/1109/dt.png) | ![Data Management 2](/__MACOSX/shot/1026/d2.png) |

### 🎯 WVP-GB28281 + Hikvision ISC + Dahua ICC

|                                       |                                         |
|---------------------------------------|-----------------------------------------|
| ![Platform Instance](/__MACOSX/shot/1109/hlht.png) | ![GB Real-time](/__MACOSX/shot/1109/gbss.png) |
| ![Recording List](/__MACOSX/shot/1109/lxlb.png) | ![Recording](/__MACOSX/shot/1109/lsbf.png) |
| ![Device Recording](/__MACOSX/shot/1109/sblx.png) | ![Recording Playback](/__MACOSX/shot/1109/lxhf.png) |

#### 🌐 CTIoT Product Integration

![CTIoT Product Integration](__MACOSX/shot/99.gif)

### 🎯 Mobile / Mini-Program

![App](__MACOSX/shot/1026/app.jpg)

## 📺 Video Tutorials

More videos on Bilibili and Douyin

### 📚 Basic Tutorials

| No. | Tutorial Name | Video Link |
|:---:|:---|:---|
| 1 | [NexIoT Course] (1) IDEA & Docker One-Click Start | [📺 Watch on Bilibili](https://www.bilibili.com/video/BV1WNUnBnEx5/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff) |
| 2 | [NexIoT Course] (2) EMQX Configuration | [📺 Watch on Bilibili](https://www.bilibili.com/video/BV1MdUJB4E7k/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff) |

### 📨 MQTT Integration Tutorial

| No. | Tutorial Name | Video Link |
|:---:|:---|:---|
| 1 | Any Topic & Full Process Integration Tutorial | [📺 Watch on Bilibili](https://www.bilibili.com/video/BV1q1UZBmEHS/?share_source=copy_web&vd_source=c9e1500efcc8aa0763f711fadaa68dff) |

## 📊 Feature Comparison

| Main Features | Description | Open Source | Enterprise |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|--------|
| JDK21 Virtual Threads | Based on Java21, SpringBoot3.x framework, supporting virtual threads, netty high performance | ✅ | ✅ |
| RuoYi Scaffold | Admin, role management, permission management, log management, including extensions for phone verification, OSS file management<br>Supports login logs, operation log query management | ✅ | ✅ |
| Login/Homepage | Account password verification code login<br>Supports i18n multi-language<br>Dashboard data statistics display | ✅ | ✅ |
| Product Management | One-click export/import (product, TSL, parser) for immediate sharing<br>Online thing model editing, supports properties, events, functions<br>Device tag settings, common configs (auto registration, offline detection rules, device SN parsing)<br>TCP cloud config (port, packet splitting, encoding/decoding)<br>HTTP third-party cloud config<br>Independent device thing model storage | ✅ | ✅ |
| Product Categories | Category creation management, supports adding sub-categories | ✅ | ✅ |
| Protocol Management | Unified protocol management and hot updates<br>Protocol drivers fully externalized | ✅ | ✅ |
| Device List | Complete device lifecycle management<br>Device list display, device status<br>Online function calls, device shadows<br>Communication log management, message subscription config<br>Gateway & gateway sub-device support<br>Topology diagram display<br>Batch device operations | ✅ | ✅ |
| Device Shadow | Latest device status cache<br>Supports property, event, function status storage<br>Shadow data query and update | ✅ | ✅ |
| Device Groups | Device grouping for unified management and scene linkage<br>Supports batch group operations | ✅ | ✅ |
| Gateway Polling | Cloud-based scheduled gateway polling<br>Multi-command config, polling intervals (30s/60s/120s/300s/600s)<br>Retry on failure, timeout control<br>Distributed cluster deployment | ✅ | ✅ |
| **Integrated WEB IDE** | Online protocol debugging, unified management<br>Browser-based visual IDE<br>Protocol script writing, debugging, hot deployment<br>(Premium version provides IDE debug frontend source code) | ✅ | ✅ |
| Scene Linkage | Device thing model event, property configuration<br>Execute linkage to other devices<br>Notification sending<br>Linkage log query | ✅ | ✅ |
| Rule Engine | Visual rule orchestration engine (similar to Node-RED)<br>Supports industrial protocols like Modbus, OPCUA<br>Workflow orchestration, node configuration<br>Greatly enhances platform access capabilities (under development) | ✅ | ✅ |
| **Data Bridge** | Data source configuration, bridge rule definition<br>Bridge types: input, output, bidirectional<br>Direct output to databases (MySQL, PostgreSQL, SQLServer, Oracle)<br>Time-series database output (IoTDB, Clickhouse, InfluxDB)<br>MQ output (Kafka, MQTT)<br>HTTP data bridging<br>Other middleware extensions | ✅ | ✅ |
| Data Forwarding | Device group filtering<br>Custom data forwarding by rules<br>Forward to HTTP, MQTT and other targets | ✅ | ✅ |
| **Multi-Application Management** | Application data isolation; multi-application management; complete north-south data push and command invocation<br>Create multiple applications per account<br>Independent data between applications (similar to multi-tenancy) | ✅ | ✅ |
| **SDK Support (Java)** | Device management: device registration, query, update, delete<br>Device communication: online/offline status management<br>Data reporting: property and event reporting<br>Security authentication: OAuth2 client authentication and password authentication<br>Easy integration: out-of-the-box SDK | ✅ | ✅ |
| Northbound Push | HTTP push, MQTT push (open source support)<br>Other push methods (enterprise exclusive) | ✅ | ✅ |
| Notification Channels | Supports DingTalk, Alibaba Cloud SMS/Voice, Tencent Cloud SMS/Voice, Feishu, Email | ✅ | ✅ |
| Notification Templates | Custom template creation<br>Variable replacement support | ✅ | ✅ |
| Certificate Management | TLS/SSL certificate management<br>Certificate upload, query, delete | ✅ | ✅ |
| MQTT Direct Access | Built-in MQTT access<br>Support for custom MQTT nodes<br>MQTT cluster deployment | ✅ | ✅ |
| HTTP Access | Cloud-to-cloud integration<br>OAUTH2 standard access<br>Seamless Java SDK integration | ✅ | ✅ |
| **Modbus Protocol** | Modbus RTU/TCP protocol support<br>Modbus device access and data processing<br>**Note: Open source version supports MQTT method** | ✅ | ✅ |
| **TCP/UDP Direct Access** | Custom protocols, Modbus and other TCP communication<br>SNI TLS wildcard domain access<br>TCP clustering, unlimited node expansion<br>TCP packet splitting/merging<br>Dynamic start/stop and listener creation | ❌ | ✅ |
| **CTIoT** | Seamless integration with China Telecom CTIoT AIOT<br>Product and device management unified in NexIoT | ❌ | ✅ |
| **Video Access** | Lechange cloud video access (enterprise complimentary)<br>GB28181 video protocol support (WVP integration in beta)<br>Dahua ICC and Hikvision ISC in planning | ❌ | ✅ |
| **Cluster Deployment** | Multi-node cluster deployment<br>Distributed locks, load balancing<br>High availability, horizontal scaling | ✅ | ✅ |
| **Time-Series Database** | Seamless dynamic switching of log storage<br>Supports None (no storage) / MySQL / **ClickHouse** / **IoTDB** | ✅ | ✅ |
| **MQTT Any Topic** | External MQTT Broker integration<br>MQTT any topic data integration and command control<br>Data passthrough and thing model structure | ❌ | ✅ |
| **Community Free Drivers** | Thanks to excellent architecture, community drivers can be exported and used for free<br>Supports DLT645-2007, JT808, CTIoT 50+ devices | ✅ | ✅ |

---

### 📢 Important Notice

- **Media Sharing**: Welcome to share project information, please comply with AGPL3.0 open-source license
- **Enterprise Authorization**: Commercial use requires authorization, retain enterprise authorization information
- **Legal Liability**: Reserve the right to pursue legal action for misuse that causes legal consequences

### 🌐 Community Contact

| QQ Group | WeChat | Bilibili | Douyin |
|-------------------------------|-------------------------------|---------------------------------|----------------------------------|
| ![QQ Group](/__MACOSX/shot/qq.png) | ![WeChat Group](/__MACOSX/shot/wx.png) | ![Bilibili](/__MACOSX/shot/bzhan.jpg) | ![Douyin](/__MACOSX/shot/douyin.jpg) |

## 🛠️ Technical Support & Services

We provide comprehensive technical support services, from basic Q&A to enterprise-level solutions, meeting different levels of needs. Fees enable better service and greater encouragement for authors!

### 📋 Service Type Comparison

| Service Type | Service Content | Price | Applicable Scenarios | Contact |
|---------------|--------------------------------------|--------------|-----------|--------------------------------------------------------------|
| **🆓 Community Q&A** | • Basic Q&A<br>• Usage guidance<br>• Community exchange | **Free** | Learning, Usage | QQ Group, WeChat Group |
| **📚 Technical Documentation** | • Technical docs<br>• Practice guides<br>• Video tutorials | **Free** | Self-learning, Reference | [Documentation](https://docs.nexiot.cc/) |
| **☁️ Online Deployment** | • Customer provides hardware<br>• Complete system deployment<br>• Cloud service deployment | **¥199** | Quick launch, Cloud deployment | Contact Technical Support |
| **🔧 Dedicated Support** | • Various integration support<br>• Usage guidance<br>• Deployment solutions<br>• Solutions | **¥600/4hrs** | Remote support | Contact Technical Support |
| **⚡ Device Integration** | • IDE parser source code<br>• Product config export<br>• Custom integration | **¥1000/day** | Complex device integration, Custom development | Contact Customer Service |
| **🏢 Enterprise Worry-Free** | • Commercial on-premise deployment<br>• Managed operations<br>• 5×8 technical support | **¥9,999/year** | - | Contact Customer Service |

---

## 📄 Open Source License & Authorization

Please comply with AGPL3.0 open-source license, commercial use requires authorization

### 📜 Detailed Authorization Terms

#### ✅ Authorized User Rights

1. **Internal Use**: Authorized users can use indefinitely within their organization
    - Enterprise authorization: excludes branches, subsidiaries, and third parties
    - Personal authorization: personal use only, not for employer or third parties

2. **Development Rights**: Authorized users can perform secondary development via project form
    - Customized software must be encrypted and packaged before delivery to customers
    - Source code delivery requires purchasing corresponding commercial authorization for customers

#### ❌ Authorization Restrictions

1. **No Transfer**: Cannot distribute or transfer authorized source code to third parties
    - Whether paid or unpaid transfer
    - Cannot apply for software copyright containing this project

### 🔒 Closed Source Content

**TCP**, **UDP**, **CTIoT** modules. Can be extended through secondary development, commercial use requires project sponsorship and authorization.

### 🙏 Acknowledgments

Thanks to the following open-source projects and technology platforms:

- **Open Source Frameworks**: RuoYi, Antdv, jetlink, ssssssss-team
- **Cloud Platforms**: Alibaba Cloud, Huawei Cloud, Tencent Cloud, AEP, OneNet and other IoT platforms
- **Community Support**: All contributors and users for their support and feedback
