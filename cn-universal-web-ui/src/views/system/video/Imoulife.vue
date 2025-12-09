<!-- eslint-disable -->
<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="DeviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备SN" @keyup.enter="handleQuery"
                         allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.name')">
                <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称" @keyup.enter="handleQuery"
                         allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.onlineStatus')">
                <a-select placeholder="请选择" v-model="queryParam.state" style="width: 100%" allow-clear>
                  <a-select-option v-for="(d, index) in stateOptions" :key="index" :value="d.dictValue">{{
                      d.dictLabel
                    }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('common.tag')">
                <a-input v-model="queryParam.tags" placeholder="输入标签，逗号分隔" allow-clear
                         @keyup.enter="handleQuery"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons" :style="{ float: 'right', overflow: 'hidden' }">
                <a-button type="primary" @click="handleQuery"><iot-icon type="icon-search"/>{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns" :data-source="list"
               :pagination="false">
        <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
          <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
                     v-if="getConfigInfo(record.configuration)">
            <template slot="content">
              <div class="config-popover">
                <div v-for="(value, key) in getConfigInfo(record.configuration)" :key="key" class="config-popover-item">
                  <span class="config-popover-key">{{ key }}:</span>
                  <span class="config-popover-value">{{ value }}</span>
                </div>
              </div>
            </template>
            <div class="device-info-content">
              <div class="device-name">
                <a-button type="link" class="device-name-link" @click="openPreview(record)">{{ record.deviceName || '未命名设备' }}</a-button>
              </div>
              <div class="device-id">{{ record.deviceId }}</div>
              <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
            </div>
          </a-popover>
          <div v-else class="device-info-content">
            <div class="device-name">
              <a-button type="link" class="device-name-link" @click="openPreview(record)">{{ record.deviceName || '未命名设备' }}</a-button>
            </div>
            <div class="device-id">{{ record.deviceId }}</div>
            <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
          </div>
        </div>

        <div slot="productInfo" slot-scope="text, record" class="product-info-cell">
          <div class="product-name">
            {{ record.productName || '未知产品' }}
          </div>
          <div class="product-key">{{ record.productKey }}</div>
        </div>

        <div slot="state" slot-scope="text, record" class="state-cell">
          <div :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
            <span class="status-dot"></span>
            <span class="status-text">{{ stateFormat(record) }}</span>
          </div>
        </div>

        <div slot="onlineTime" slot-scope="text, record" class="online-time-cell">
          <div class="time-main">{{ formatOnlineTime(record.onlineTime) }}</div>
          <div class="time-ago">{{ getTimeAgo(record.onlineTime) }}</div>
        </div>

        <span slot="operation" slot-scope="text, record" class="operation-buttons">
          <a @click="openPreview(record)"
             v-hasPermi="['video:imoulife:live']"
             class="operation-btn"><iot-icon type="icon-play-circle"/>{{ $t('button.play') }} </a>
          <!-- 隐藏回放按钮 -->
          <!-- <a-divider type="vertical" v-hasPermi="['video:imoulife:live', 'video:imoulife:playback']"/> -->
          <!-- <a @click="openPlayback(record)"
             v-hasPermi="['video:imoulife:playback']"
             class="operation-btn"><iot-icon type="icon-file-video"/>{{ $t('button.playback') }} </a> -->
          <a-divider type="vertical" v-hasPermi="['system:instance:edit']"/>
          <a @click="$refs.createForm.handleUpdate(record, undefined)"
             v-hasPermi="['system:instance:edit']"
             class="operation-btn">
            <iot-icon type="icon-u-edit"/>
            {{ $t('button.edit') }} </a>
          <a-divider type="vertical" v-hasPermi="['system:instance:remove']"/>
          <a style="color:#F53F3F" @click="handleDelete(record)"
             v-hasPermi="['system:instance:remove']"
             class="operation-btn">
            <iot-icon type="icon-u-del"/>
            {{ $t('button.delete') }} </a>
        </span>
      </a-table>
      <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper :current="queryParam.pageNum"
                    :total="total" :page-size="queryParam.pageSize" :showTotal="t => `共 ${t} 条`"
                    @showSizeChange="onShowSizeChange" @change="changeSize"/>
    </a-card>

    <a-modal :visible="playerVisible" :footer="null" width="960px" :destroyOnClose="true" @cancel="handleClosePlayer">
      <div ref="playerContainer" style="width: 100%; height: 540px; background: #000;"></div>
    </a-modal>

    <!-- 增加修改 -->
    <create-form ref="createForm" :stateOptions="stateOptions" @ok="getList"/>

    <!-- 视频预览组件 -->
    <ImouPlayer
      v-model="previewModalVisible"
      :deviceId="currentRecord ? currentRecord.deviceId : ''"
      :channelId="0"
      :token="currentRecord ? currentRecord.token : ''"
      :deviceModel="currentRecord ? currentRecord.productName : ''"
      :playType="1"
      :streamId="0"
    />
  </page-header-wrapper>
</template>

<script>
import { delInstance, listInstanceByPlatform } from '@/api/system/dev/instance'
import { functionDown } from '@/api/system/dev/instance'
import { formatOnlineTime, getTimeAgo } from '@/utils/time'
import ImouPlayer from './ImouPlayer'
import CreateForm from '../instance/modules/CreateForm'

export default {
  name: 'Imoulife',
  components: {
    ImouPlayer,
    CreateForm
  },
  data() {
    return {
      list: [],
      tableSize: 'default',
      loading: false,
      total: 0,
      queryParam: {
        thirdPlatform: 'imoulife',
        deviceId: undefined, // 设备SN
        deviceName: undefined,
        state: undefined,
        tags: undefined,
        pageNum: 1,
        pageSize: 10
      },
      stateOptions: [],
      columns: [
        {
          title: this.$t('compound.deviceNameDeviceId'),
          dataIndex: 'deviceInfo',
          scopedSlots: {customRender: 'deviceInfo'},
          width: '28%',
          align: 'left'
        },
        {
          title: this.$t('compound.productProductKey'),
          dataIndex: 'productInfo',
          scopedSlots: {customRender: 'productInfo'},
          width: '22%',
          align: 'left'
        },
        {title: this.$t('device.status'), dataIndex: 'state', scopedSlots: {customRender: 'state'}, width: '12%', align: 'center'},
        {
          title: this.$t('device.communicationStatus'),
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          width: '18%',
          align: 'center'
        },
        {title: this.$t('user.operation'), dataIndex: 'operation', scopedSlots: {customRender: 'operation'}, width: '20%', align: 'center'}
      ],
      playerVisible: false,
      currentDevice: null,
      player: null,
      previewModalVisible: false,
      currentRecord: null
    }
  },
  created() {
    this.getList()
    this.getDicts('dev_instance_state').then(res => {
      this.stateOptions = res.data || []
    })
  },
  beforeDestroy() {
    this.destroyPlayer()
  },
  methods: {
    getList() {
      this.loading = true
      const params = {...this.queryParam}
      if (params.deviceName) params.deviceName = params.deviceName.trim()
      if (params.deviceId) params.deviceId = params.deviceId.trim()
      if (params.tags) params.tags = params.tags.trim()
      listInstanceByPlatform(params).then(res => {
        const rows = res.rows || []
        this.list = rows
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParam = {
        thirdPlatform: 'imoulife',
        deviceId: undefined,
        deviceName: undefined,
        state: undefined,
        tags: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.getList()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getList()
    },
    // 0-离线，1-在线 字典翻译
    stateFormat(row) {
      return this.selectDictLabel(this.stateOptions, row.state)
    },
    // 统一的时间工具方法
    formatOnlineTime(timestamp) {
      return formatOnlineTime(timestamp)
    },
    getTimeAgo(timestamp) {
      return getTimeAgo(timestamp)
    },
    // 配置信息解析
    getConfigInfo(configStr) {
      if (!configStr) {
        return null
      }
      try {
        const config = JSON.parse(configStr)
        const keyConfigs = {}
        if (config.iccid) keyConfigs['ICCID'] = config.iccid
        if (config.meterNo) keyConfigs['表号'] = config.meterNo
        if (config.imei) keyConfigs['IMEI'] = config.imei
        if (config.version) keyConfigs['版本'] = config.version
        return Object.keys(keyConfigs).length > 0 ? keyConfigs : null
      } catch (e) {
        return null
      }
    },
    async ensureImouSdk() {
      const sdkUrl = process.env.VUE_APP_IMOU_SDK_URL || ''
      if (sdkUrl && !window.ImouPlayer) {
        await loadExternalScript(sdkUrl)
      }
    },
    async openPreview(record) {
      try {
        this.$message.loading('正在获取播放地址...', 0)
        
        // 构建请求参数
        const requestData = {
          appUnionId: 'iot',
          productKey: record.productKey,
          deviceId: record.deviceId,
          cmd: 'DEV_FUNCTION',
          function: {
            messageType: 'FUNCTIONS',
            function: 'cameraLiveStream',
            data: {
              streamType: 'main', // 主码流
              protocol: 'hls',
              channelId: 0
            }
          }
        }
        
        // 通过functionDown接口获取播放地址
        const response = await functionDown(record.productKey, requestData)
        this.$message.destroy()
        
        if (response.code === 0 && response.data) {
          const { url, token, type } = response.data
          
          if (!url || !token) {
            this.$message.warning('设备暂无预览地址')
            return
          }
          
          // 保存token到record中
          this.currentRecord = {
            ...record,
            token: token
          }
          this.previewModalVisible = true
        } else {
          this.$message.error(response.msg || '获取播放地址失败')
        }
      } catch (e) {
        this.$message.destroy()
        this.$message.error(e.message || '获取播放地址失败')
      }
    },
    closePreviewModal() {
      this.previewModalVisible = false
    },
    async openPlayback(record) {
      try {
        this.$message.loading('正在获取回放地址...', 0)

        // 构建请求参数 - 回放功能
        const requestData = {
          appUnionId: 'iot',
          productKey: record.productKey,
          deviceId: record.deviceId,
          cmd: 'DEV_FUNCTION',
          function: {
            messageType: 'FUNCTIONS',
            function: 'cameraLiveStream',
            data: {
              streamType: 'sub',
              protocol: 'hls',
              playType: 'cloud', // 云存储回放
              beginTime: this.getBeginTime(),
              endTime: this.getEndTime()
            }
          }
        }

        // 通过functionDown接口获取回放地址
        const response = await functionDown(record.productKey, requestData)
        this.$message.destroy()

        if (response.code === 0 && response.data) {
          const {url, token, type} = response.data

          if (!url) {
            this.$message.warning('设备暂无回放地址')
            return
          }

          // 构建回放播放器参数
          const query = {
            deviceId: record.deviceId,
            channelId: 0,
            token: token,
            type: 2, // 2=回放
            streamId: 0,
            recordType: 'cloud' // 云存储回放
          }

          const href = this.$router.resolve({
            path: '/video/imou/player',
            query: query
          }).href
          window.open(href, '_blank')
        } else {
          this.$message.error(response.msg || '获取回放地址失败')
        }
      } catch (e) {
        this.$message.destroy()
        this.$message.error(e.message || '获取回放地址失败')
      }
    },
    getBeginTime() {
      // 获取当前时间前1小时的时间戳（毫秒）
      return Date.now() - 60 * 60 * 1000
    },
    getEndTime() {
      // 获取当前时间戳（毫秒）
      return Date.now()
    },
    handleClosePlayer() {
      this.playerVisible = false
      this.destroyPlayer()
    },
    destroyPlayer() {
      try {
        if (this.player && this.player.destroy) this.player.destroy()
        this.player = null
      } catch (e) {
      }
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const ids = row.id
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + ids + '的数据',
        onOk() {
          return delInstance(ids)
            .then(() => {
              that.getList()
              that.$message.success(
                '删除成功',
                3
              )
            })
            .catch(() => {
            })
        },
        onCancel() {
        }
      })
    }
  }
}
</script>


<style scoped>
.device-info-cell {
  padding: 12px 0;
}

.device-info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-name-link {
  font-size: 15px;
  font-weight: 600;
  padding: 0 !important;
  margin: 0;
  height: auto;
  color: #1890ff;
  text-align: left;
  justify-content: flex-start;
  line-height: 1.4;
  border: none;
  box-shadow: none;
  background: none;
}

.device-name-link:hover {
  color: #40a9ff;
}

.device-id {
  font-size: 13px;
  color: #595959;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  line-height: 1.4;
}

.iot-id {
  font-size: 11px;
  color: #8c8c8c;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}

.product-info-cell {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.product-key {
  font-size: 12px;
  color: #595959;
  font-family: 'Courier New', monospace;
  line-height: 1.4;
}

.state-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.status-badge.offline {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-badge.online .status-dot {
  background-color: #52c41a;
}

.status-badge.offline .status-dot {
  background-color: #f5222d;
}

.online-time-cell {
  text-align: center;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.time-main {
  font-size: 13px;
  color: #262626;
  font-weight: 500;
  line-height: 1.4;
}

.time-ago {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
  font-weight: 400;
}

.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  min-width: 120px;
}

.operation-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.config-popover {
  max-width: 300px;
}

.config-popover-item {
  display: flex;
  margin-bottom: 6px;
  font-size: 12px;
  line-height: 1.4;
}

.config-popover-key {
  color: #595959;
  font-weight: 500;
  min-width: 60px;
  margin-right: 8px;
}

.config-popover-value {
  color: #262626;
  font-family: 'Courier New', monospace;
  word-break: break-all;
  flex: 1;
}
</style>
