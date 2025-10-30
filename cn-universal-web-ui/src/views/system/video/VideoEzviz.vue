<!-- eslint-disable -->
<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="DeviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备SN"
                         @keyup.enter="handleQuery"
                         allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.name')">
                <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称"
                         @keyup.enter="handleQuery"
                         allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('device.onlineStatus')">
                <a-select placeholder="请选择" v-model="queryParam.state" style="width: 100%"
                          allow-clear>
                  <a-select-option v-for="(d, index) in stateOptions" :key="index"
                                   :value="d.dictValue">{{
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
              <span class="table-page-search-submitButtons"
                    :style="{ float: 'right', overflow: 'hidden' }">
                <a-button type="primary" @click="handleQuery"><iot-icon type="icon-search"/>{{
                    $t('button.search')
                  }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns"
               :data-source="list"
               :pagination="false">
        <div slot="deviceInfo" slot-scope="text, record" class="device-info-cell">
          <a-popover placement="rightTop" trigger="hover" :title="'设备配置信息'"
                     v-if="getConfigInfo(record.configuration)">
            <template slot="content">
              <div class="config-popover">
                <div v-for="(value, key) in getConfigInfo(record.configuration)" :key="key"
                     class="config-popover-item">
                  <span class="config-popover-key">{{ key }}:</span>
                  <span class="config-popover-value">{{ value }}</span>
                </div>
              </div>
            </template>
            <div class="device-info-content">
              <div class="device-name">
                <a-button type="link" class="device-name-link">{{
                    record.deviceName || '未命名设备'
                  }}
                </a-button>
              </div>
              <div class="device-id">{{ record.deviceId }}</div>
              <div v-if="record.iotId" class="iot-id">{{ record.iotId }}</div>
            </div>
          </a-popover>
          <div v-else class="device-info-content">
            <div class="device-name">
              <a-button type="link" class="device-name-link">{{
                  record.deviceName || '未命名设备'
                }}
              </a-button>
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
          <div
            :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
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
             v-hasPermi="['video:ezviz:live']"
             class="operation-btn"><iot-icon type="icon-play-circle"/>{{ $t('button.play') }} </a>
          <a-divider type="vertical" v-hasPermi="['video:ezviz:live', 'video:ezviz:playback']"/>
          <a @click="openPlayback(record)"
             v-hasPermi="['video:ezviz:playback']"
             class="operation-btn"><iot-icon type="icon-file-video"/>{{
              $t('button.playback')
            }} </a>
        </span>
      </a-table>
      <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                    :current="queryParam.pageNum"
                    :total="total" :page-size="queryParam.pageSize" :showTotal="t => `共 ${t} 条`"
                    @showSizeChange="onShowSizeChange" @change="changeSize"/>
    </a-card>

    <a-modal :visible="playerVisible" :footer="null" width="960px" :destroyOnClose="true"
             @cancel="handleClosePlayer">
      <div ref="playerContainer" style="width: 100%; height: 540px; background: #000;"></div>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import {listInstanceByPlatform} from '@/api/system/dev/instance'
import {loadExternalScript} from '@/utils/loadScript'
import {formatOnlineTime, getTimeAgo} from '@/utils/time'

export default {
  name: 'VideoEzviz',
  data() {
    return {
      list: [],
      tableSize: 'default',
      loading: false,
      total: 0,
      queryParam: {
        thirdPlatform: 'ezviz',
        deviceId: undefined,
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
        {
          title: this.$t('device.status'),
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          width: '12%',
          align: 'center'
        },
        {
          title: this.$t('device.communicationStatus'),
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          width: '18%',
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          scopedSlots: {customRender: 'operation'},
          width: '20%',
          align: 'center'
        }
      ],
      playerVisible: false,
      currentDevice: null,
      player: null
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
      if (params.deviceName) {
        params.deviceName = params.deviceName.trim()
      }
      if (params.deviceId) {
        params.deviceId = params.deviceId.trim()
      }
      if (params.tags) {
        params.tags = params.tags.trim()
      }
      listInstanceByPlatform(params).then(res => {
        const rows = (res.rows || []).map(r => this.normalizeRow(r))
        this.list = rows
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    normalizeRow(row) {
      // 后端数据已包含正确的 productName 和 productKey 字段
      return {...row}
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParam = {
        thirdPlatform: 'ezviz',
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
    // 配置信息解析
    getConfigInfo(configStr) {
      if (!configStr) {
        return null
      }
      try {
        const config = JSON.parse(configStr)
        const keyConfigs = {}
        if (config.iccid) {
          keyConfigs['ICCID'] = config.iccid
        }
        if (config.meterNo) {
          keyConfigs['表号'] = config.meterNo
        }
        if (config.imei) {
          keyConfigs['IMEI'] = config.imei
        }
        if (config.version) {
          keyConfigs['版本'] = config.version
        }
        return Object.keys(keyConfigs).length > 0 ? keyConfigs : null
      } catch (e) {
        return null
      }
    },
    // 统一的时间工具方法
    formatOnlineTime(timestamp) {
      return formatOnlineTime(timestamp)
    },
    getTimeAgo(timestamp) {
      return getTimeAgo(timestamp)
    },
    async ensureEzvizSdk() {
      const sdkUrl = process.env.VUE_APP_EZVIZ_SDK_URL
        || 'https://open.ys7.com/sdk/js/1.5/ezuikit.js'
      if (!window.EZUIKit) {
        await loadExternalScript(sdkUrl)
      }
      if (!window.EZUIKit) {
        throw new Error('EZVIZ SDK 未加载')
      }
    },
    async openPreview(record) {
      try {
        // 期望后端在设备记录里返回 ezopen 与 token，或通过接口实时获取
        const url = record.ezopen || ''
        const token = record.ezvizAccessToken || ''
        if (!url || !token) {
          this.$message.warning('缺少萤石播放参数（ezopen/token）')
          return
        }
        const href = this.$router.resolve({
          path: '/video/ezviz/player',
          query: {url, token}
        }).href
        window.open(href, '_blank')
      } catch (e) {
        this.$message.error(e.message || '打开播放窗口失败')
      }
    },
    async openPlayback(record) {
      try {
        // 回放地址与token应由后端提供（含时间片段参数），此处复用占位字段
        const url = record.ezopenPlayback || record.ezopen || ''
        const token = record.ezvizAccessToken || ''
        if (!url || !token) {
          this.$message.warning('缺少萤石回放参数（ezopen/token）')
          return
        }
        const href = this.$router.resolve({
          path: '/video/ezviz/player',
          query: {url, token}
        }).href
        window.open(href, '_blank')
      } catch (e) {
        this.$message.error(e.message || '打开回放窗口失败')
      }
    },
    handleClosePlayer() {
      this.playerVisible = false
      this.destroyPlayer()
    },
    destroyPlayer() {
      try {
        if (this.player && this.player.stop) {
          this.player.stop()
        }
        this.player = null
      } catch (e) {
      }
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
