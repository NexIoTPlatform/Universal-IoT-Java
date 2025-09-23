<template>
  <div :class="[isShow ? 'outer' : 'outer-hidden']">
    <div class="vedio-box" v-show="isShow" @click="showModal">
      <img src="../../assets/images/userIntroduce.jpg"
           style="width: 100%;height: 100%;object-fit: cover;"/>
      <a-icon type="play-circle" theme="filled"
              style="fontSize:28px;opacity:0.4;position:absolute;left:40%;top:40%;z-index:1;cursor:pointer"/>
    </div>
    <div :class="[isShow ? '' : 'head-hidden']">
      <div class="head">
        <span style="font-size: 20px; font-weight: bold; color: black; margin-bottom:10px"
              v-show="isShow">设备接入流程概览</span>
        <div style="float:right">
          <span style="margin-right:10px" v-show="isShow">查看流程图 </span> <span @click="click"
                                                                                   style="cursor:pointer">{{
            message
          }}<a-icon :type="isShow ? 'up' : 'down'"/></span>
        </div>
      </div>
      <div class="step-box" v-show="isShow">
        <div v-for="item in Steps" :key="item.step" @click="click1(item.url)" class="content-card">
          <div class="step-item">{{ item.step }}</div>
          <div>
            <div style="font-weight: bold; color: black; margin: 5px 5px 5px 0px">{{
                item.title
              }}
            </div>
            <div style="margin-bottom: 5px;font-size:12px">{{ item.content }}</div>
          </div>
        </div>
      </div>
    </div>
    <a-modal v-model="visible" :footer="null" width="900px" @cancel="handleCancel">
      <video src="/abc.mp4" id="introduce" style="width: 100%;height: 90%;margin-top:15px"
             controls="true" autoplay="autoplay">
        Your browser does not support the video tag
      </video>
    </a-modal>
  </div>
</template>

<script>

export default {
  name: 'UseFlow',
  data() {
    return {
      Steps: [
        {
          step: '01',
          title: '创建产品',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/product/addproduct',
          content: '产品也称设备模型，是一组具有相同功能定义的设备集合'
        },
        {
          step: '02',
          title: '编辑物模型',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/metadata/metadata',
          content: '物模型是为产品定义的数据模型，用于描述产品的功能'
        },
        {
          step: '03',
          title: '编解码插件',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/protocol/protocol',
          content: '当该产品设备上报数据为二进制码流时，则需要进行编解码插件开发'
        },
        {
          step: '04',
          title: '创建设备',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/instance/addinstance',
          content: '创建设备获取连接平台所需要的各类信息'
        },
        {
          step: '05',
          title: '在线调试',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/simulation/simulation',
          content: '在线调试设备上报与功能，减少设备端开发工作量，实现快速配置'
        },
        {
          step: 'next',
          title: '接下来，建议您可以',
          url: 'https://iot.universal.cn/wxopen/wxmp/iot/index.html#/docked/dataFlow/dataFlow',
          content: '配置数据流转'
        }
      ],
      isShow: true,
      message: '隐藏指引',
      icon: 'up',
      visible: false
    }
  },
  methods: {
    click() {
      console.log(99)
      this.isShow = !this.isShow
      this.message = this.isShow ? '隐藏指引' : '显示指引'
    },
    click1(url) {
      window.open(url)
    },
    showModal() {
      this.visible = true
      document.getElementById('introduce').play()
    },
    handleCancel(e) {
      console.log(e)
      document.getElementById('introduce').pause()
    }
  }
}
</script>

<style scoped lang="less">
.outer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 10px;
  background-color: #fff;
}

.outer-hidden {
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
}

.vedio-box {
  width: 24%;
  height: 120px;
  text-align: center;
  margin-right: 15px;
  border: 1px solid #f2f3f5;
  position: relative
}

.head {
  margin-bottom: 10px;
}

.head-hidden {
  position: absolute;
  right: 25px;
}

.content-card {
  display: flex;
  justify-content: space-around;
  border: 1px solid #f2f3f5;
  margin-right: 10px;
  margin-bottom: 8px;
  padding-right: 3px;
  //flex-shrink: 0;
  width: 30%;
  transition: 0.3s;
  cursor: pointer;
}

.content-card:hover {

  box-shadow: 0 3px 15px 3px rgba(179, 165, 165, 0.5);
}

.step-box {
  //width: 100%;
  display: flex;
  flex-wrap: nowrap;
  // overflow-x: auto;
}

.step-item {
  margin-left: 10px;
  margin-right: 10px;
  height: 60px;
  font-weight: 700;
  color: #1966ff;
  font-size: 20px;
}
</style>
