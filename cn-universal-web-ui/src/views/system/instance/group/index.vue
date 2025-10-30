<template>
  <page-header-wrapper>
    <a-card :bordered="false" class="device-group-card">
      <a-row :gutter="24">
        <a-col :span="5">
          <div class="group-tree-container">
            <div class="tree-header">
              <h3 class="tree-title">{{ $t('device.deviceGroup') }}</h3>
              <div class="tree-actions">
                <a-tooltip title="添加根分组">
                  <a-button type="text" icon="plus" @click="handleAddRoot(1)" class="action-btn"/>
                </a-tooltip>
                <a-tooltip title="添加子分组" v-if="leven !== 0">
                  <a-button type="text" icon="plus-square" @click="handleAddRoot(2)"
                            class="action-btn"/>
                </a-tooltip>
                <a-tooltip title="删除分组" v-if="leven !== 0">
                  <a-button type="text" icon="delete" @click="deleteGroup"
                            class="action-btn danger"/>
                </a-tooltip>
                <a-tooltip title="$t('button.refresh')">
                  <a-button type="text" icon="reload" @click="retryGroup" class="action-btn"/>
                </a-tooltip>
              </div>
            </div>
            <div class="tree-content">
              <a-tree ref="tree" :tree-data="groupNodes" :defaultExpandParent="false"
                      :default-expanded-keys="['0']"
                      :show-line="true" :show-icon="false" @select="onSelect" class="group-tree">
              </a-tree>
            </div>
          </div>
        </a-col>
        <a-col :span="19">
          <div class="group-detail-container">
            <!-- 设备列表区域 -->
            <div class="device-list-section">
              <!-- 分组信息显示 -->
              <div class="group-info-display" v-if="groupDetail.groupName">
                <div class="group-basic-info">
                  <h2 class="group-name">{{ groupDetail.groupName }}</h2>
                  <div class="group-meta">
                    <span class="group-id">ID: {{ groupDetail.id }}</span>
                    <span class="group-desc"
                          v-if="groupDetail.groupDescribe">{{ groupDetail.groupDescribe }}</span>
                  </div>
                </div>
                <a-button type="link" icon="edit" @click="editGroup(3)" size="small">编辑分组
                </a-button>
              </div>

              <div class="list-header">
                <div class="header-info">
                  <h3 class="section-title">{{ $t('device.list') }}</h3>
                  <span class="device-count" v-if="total > 0">{{ total }} 台设备</span>
                </div>
                <div class="header-actions">
                  <a-button type="primary" icon="plus" @click="bindInstance"
                            :disabled="groupId === 0 || groupId === '0'" size="small">
                    {{ $t('button.bind.device') }}
                  </a-button>
                  <a-button type="danger" icon="disconnect" :disabled="selectedRowKeys.length == 0"
                            @click="unBinds"
                            size="small" class="unbind-btn">
                    {{ $t('button.bulk.unbind') }}
                  </a-button>
                  <a-button icon="reload" @click="resetDevice"
                            :disabled="groupId === 0 || groupId === '0'"
                            size="small">
                    {{ $t('button.refresh') }}
                  </a-button>
                </div>
              </div>

              <div class="table-wrapper">
                <a-table :loading="loading" :size="tableSize" rowKey="id" :columns="columns"
                         :data-source="list"
                         :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
                         :pagination="false"
                         class="device-table">
                  <span slot="deviceId" slot-scope="text, record">
                    <a-button type="link"
                              @click="$router.push(`/system/instance/instance-details/${record.id}`)">{{
                        record.deviceId
                      }}</a-button>
                  </span>
                  <span slot="productName" slot-scope="text, record">
                    <div class="product-name">
                      {{ record.productName || '未知产品' }}
                      <span v-if="record.deviceNode === 'DEVICE'" class="sub-device-badge">直</span>
                      <span v-if="record.deviceNode === 'GATEWAY'"
                            class="sub-device-badge">网</span>
                      <span v-if="record.deviceNode === 'GATEWAY_SUB_DEVICE'"
                            class="sub-device-badge">子</span>
                    </div>
                  </span>
                  <div slot="state" slot-scope="text, record" class="state-cell">
                    <div
                      :class="{ 'status-badge online': record.state, 'status-badge offline': !record.state }">
                      <span class="status-dot"></span>
                      <span class="status-text">{{ stateFormat(record) }}</span>
                    </div>
                  </div>
                  <span slot="onlineTime" slot-scope="text, record">
                    {{ parseTime(record.onlineTime) }}
                  </span>
                  <span slot="operation" slot-scope="text, record">
                    <a-button type="link" danger @click="unBind(text, record)"
                              v-hasPermi="['system:instance:remove']">
                      <a-icon type="disconnect"/>解绑
                    </a-button>
                  </span>
                </a-table>

                <!-- 分页 -->
                <a-pagination class="ant-table-pagination" show-size-changer show-quick-jumper
                              :current="queryParam.pageNum" :total="total"
                              :page-size="queryParam.pageSize"
                              :showTotal="total => `共 ${total || 0} 条`"
                              @showSizeChange="onShowSizeChange" @change="changeSize"/>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>
    <!-- 增加修改分组 -->
    <group-form :formType.sync="formType" :showDialog.sync="showDialog" ref="groupForm"
                :groupTree="groupNodes"
                @ok="getGroupList" @reset="getGroupDetail"/>
    <!-- 增加修改 -->
    <bind-group ref="bindGroup" @reset="getDeviceList"/>
  </page-header-wrapper>
</template>

<script>
import {
  delDevByGroupId,
  delGroup,
  deviceListByGroupId,
  getGroup,
  listGroup
} from '@/api/system/dev/devGroup'
import GroupForm from '../modules/GroupForm'
import allIcon from '@/core/icons'
import BindGroup from '../modules/BindGroup'

export default {
  name: 'InstancePage',
  components: {
    GroupForm,
    BindGroup
  },
  data() {
    return {
      selectedRowKeys: [],
      selectedRows: [],
      // 0-离线，1-在线字典
      stateOptions: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      ids: [],
      // 设备列表
      list: [],
      // 设备总数
      total: null,
      // 加载显示控制
      loading: false,
      // columns 表格渲染规则
      columns: [
        {
          title: this.$t('device.deviceSerial'),
          dataIndex: 'deviceId',
          ellipsis: true,
          scopedSlots: {customRender: 'deviceId'},
          align: 'center'
        },
        {
          title: this.$t('device.deviceName'),
          dataIndex: 'deviceName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('device.productName'),
          dataIndex: 'productName',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'productName'}
        },
        {
          title: this.$t('device.status'),
          dataIndex: 'state',
          scopedSlots: {customRender: 'state'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('device.lastCommunicationTime'),
          dataIndex: 'onlineTime',
          scopedSlots: {customRender: 'onlineTime'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '18%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ],
      // 分组详细信息
      groupDetail: {
        id: undefined,
        groupName: undefined,
        parentId: undefined,
        groupDescribe: undefined,
        hasChild: undefined,
        groupLevel: undefined
      },
      // 分组id
      groupId: 0,
      // 分组名称
      groupName: '',
      // 控制 增加根节点,增加子节点 的显示
      leven: 0,
      // 1增加根节点,2增加子节点,3修改分组,
      formType: 0,
      // 分组增修显示控制
      showDialog: false,
      // 自定义图标
      allIcon,
      // 间距大小
      spanSize: 10,
      // 设备群组树
      groupNodes: [],
      // 设备查询集合
      queryParam: {
        groupId: undefined,
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  filters: {},
  created() {
    this.getGroupList()
    this.getDicts('dev_instance_state').then(response => {
      this.stateOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    // 打开分组弹窗
    handleAddRoot(data) {
      if (data === 1) {
        this.formType = 1
      } else if (data === 2) {
        this.formType = 2
      }
      this.$refs.groupForm.handleAdd(data, this.groupId)
      this.showDialog = true
    },
    /** 查询设备群组树 */
    getGroupList() {
      listGroup().then(response => {
        this.groupNodes = response
      })
    },
    // 分组树的点击事件
    onSelect(selectedKeys, e) {
      this.selectedRowKeys = []
      this.selectedRows = []
      // 非单个禁用
      this.single = true
      // 非多个禁用
      this.multiple = true
      this.ids = []
      if (e.selectedNodes[0] === null || e.selectedNodes[0] === undefined) {
        return
      }
      this.leven = e.selectedNodes[0].data.props.groupLevel
      this.groupId = e.selectedNodes[0].data.props.id
      this.groupName = e.selectedNodes[0].data.props.groupName
      getGroup(this.groupId).then(response => {
        this.groupDetail = response.data
        if (response.data === null || response.data === undefined) {
          this.resetDetail()
        }
      })

      // 重置搜索参数
      this.resetQuery()
      // 获取设备列表
      this.getDeviceList()
    },
    // 刷新分组树
    retryGroup() {
      this.getGroupList()
    },
    // 0-离线，1-在线字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
    },
    // 删除分组
    deleteGroup() {
      var that = this
      if (this.groupId === 0) {
        this.$notification.open({
          key: 999,
          message: '未选中分组',
          description: '未选中分组，请点击要删除的分组',
          duration: 5
        })
        return
      }
      this.$confirm({
        title: '确认删除所选中分组?',
        content: '当前选中组名为' + that.groupName + '的数据',
        onOk() {
          return delGroup(that.groupId)
          .then(() => {
            that.groupId = 0
            that.groupName = ''
            that.leven = 0
            that.resetDetail()
            that.getGroupList()
            that.$message.success(
              '删除成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    // 修改分组
    editGroup(data) {
      this.formType = 3
      getGroup(this.groupId).then(response => {
        this.$refs.groupForm.handleUpdate(response.data)
      })
      this.showDialog = true
    },
    // 重置详情
    resetDetail() {
      this.groupDetail = {
        id: undefined,
        groupName: undefined,
        parentId: undefined,
        groupDescribe: undefined,
        hasChild: undefined,
        groupLevel: undefined
      }
    },
    // 获取去详情
    getGroupDetail() {
      getGroup(this.groupId).then(response => {
        this.groupDetail = response.data
      })
      this.getGroupList()
    },
    // 绑定设备
    bindInstance() {
      this.$refs.bindGroup.show(this.groupId)
    },
    // 根据分组id获取设备列表
    getDeviceList() {
      this.loading = true
      this.queryParam.groupId = this.groupId
      console.log(this.queryParam.groupId)
      deviceListByGroupId(this.queryParam).then(response => {
        this.list = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    changeSize(current, pageSize) {
      this.queryParam.pageNum = current
      this.queryParam.pageSize = pageSize
      this.getDeviceList()
    },
    onShowSizeChange(current, pageSize) {
      this.queryParam.pageSize = pageSize
      this.getDeviceList()
    },
    // 解绑
    unBind(a, b) {
      var that = this
      this.$confirm({
        title: '解除设备绑定',
        content: '确定要解绑"' + b.deviceId + '"设备吗?',
        onOk() {
          return delDevByGroupId(that.groupId, b.iotId)
          .then(() => {
            that.getDeviceList()
            that.$message.success(
              '删除成功',
              3
            )
          })
        },
        onCancel() {
        }
      })
    },
    // 批量解绑
    unBinds() {
      var that = this
      // console.log('this.ids = ', this.ids)
      this.$confirm({
        title: '解除设备绑定',
        content: '确定要批量解绑设备吗?',
        onOk() {
          return delDevByGroupId(that.groupId, that.ids)
          .then(() => {
            that.getDeviceList()
            that.$message.success(
              '删除成功',
              3
            )
          }).finally(() => {
            that.selectedRowKeys = []
            that.selectedRows = []
            // 非单个禁用
            that.single = true
            // 非多个禁用
            that.multiple = true
            that.ids = []
          })
        },
        onCancel() {
        }
      })
    },
    resetQuery() {
      this.queryParam = {
        groupId: undefined,
        pageNum: 1,
        pageSize: 10
      }
    },
    // 刷新设备列表
    resetDevice() {
      this.selectedRowKeys = []
      this.selectedRows = []
      // 非单个禁用
      this.single = true
      // 非多个禁用
      this.multiple = true
      this.ids = []
      this.resetQuery()
      this.getDeviceList()
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      this.ids = this.selectedRows.map(item => item.iotId)
      this.single = selectedRowKeys.length !== 1
      this.multiple = !selectedRowKeys.length
    }
  }
}
</script>

<style scoped lang="less">
.device-group-card {
  /deep/ .ant-card-body {
    padding: 16px;
  }
}

.group-tree-container {
  background: white;
  border-radius: 4px;
  padding: 12px;
  height: calc(100vh - 200px);
  overflow-y: auto;
  border: 1px solid #f0f0f0;

  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;

    .tree-title {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #262626;
      writing-mode: horizontal-tb !important;
      text-orientation: mixed !important;
      text-orientation: upright !important;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .tree-actions {
      display: flex;
      gap: 2px;

      .action-btn {
        width: 20px;
        height: 20px;
        border-radius: 2px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;

        &:hover {
          background: #e6f7ff;
          color: #6dabe5;
        }

        &.danger:hover {
          background: #fff2f0;
          color: #ff4d4f;
        }
      }
    }
  }
}

.group-detail-container {
  .device-list-section {
    background: white;
    border-radius: 4px;
    padding: 16px;
    margin-bottom: 16px;
    border: 1px solid #f0f0f0;

    .group-info-display {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;

      .group-basic-info {
        .group-name {
          margin: 0 0 8px 0;
          font-size: 20px;
          font-weight: 600;
          color: #262626;
        }

        .group-meta {
          font-size: 13px;
          color: #8c8c8c;
          display: flex;
          align-items: center;
          gap: 12px;

          .group-id {
            color: #1890ff;
            font-weight: 500;
          }

          .group-desc {
            color: #595959;
          }
        }
      }
    }

    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;

      .header-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .section-title {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #262626;
        }

        .device-count {
          font-size: 13px;
          color: #8c8c8c;
          background: #f5f5f5;
          padding: 2px 8px;
          border-radius: 12px;
        }
      }

      .header-actions {
        display: flex;
        gap: 8px;

        .unbind-btn {
          background: #ff4d4f;
          border-color: #ff4d4f;
          color: white;

          &:hover {
            background: #ff7875;
            border-color: #ff7875;
          }

          &:disabled {
            background: #ffccc7;
            border-color: #ffccc7;
            color: rgba(255, 255, 255, 0.65);
          }
        }
      }
    }

    .table-wrapper {
      .device-table {
        /deep/ .ant-table-thead > tr > th {
          padding: 12px 8px;
          font-size: 13px;
          font-weight: 500;
          background: #fafafa;
        }

        /deep/ .ant-table-tbody > tr > td {
          padding: 12px 8px;
          font-size: 13px;
        }

        /deep/ .ant-table-tbody > tr:hover > td {
          background: #f6ffed;
        }
      }

      .ant-table-pagination {
        margin-top: 16px;
        text-align: right;
      }
    }
  }
}

.status {
  display: flex;
  align-items: center;
  justify-content: center;
}

.online {
  &::before {
    content: '';
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
    background-color: #52c41a;
  }
}

.offline {
  &::before {
    content: '';
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
    background-color: #f5222d;
  }
}

.device-group-container {
  height: 100%;
  display: flex;
  background: #f5f5f5;

  .left-tree {
    width: 280px;
    background: white;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;

    .tree-header {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;

      .tree-title {
        margin: 0 0 8px 0;
        font-size: 14px;
        font-weight: 600;
        color: #262626;
      }

      .tree-actions {
        display: flex;
        gap: 6px;

        .ant-btn {
          height: 24px;
          padding: 0 6px;
          font-size: 11px;
          border-radius: 3px;
          border: 1px solid #d9d9d9;
          background: white;
          color: #595959;
          min-width: auto;

          &:hover {
            border-color: #40a9ff;
            color: #40a9ff;
          }
        }
      }
    }

    .tree-content {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      .ant-tree {
        flex: 1;
        padding: 8px 16px;
        overflow-y: auto;
        overflow-x: hidden;

        /* 自定义滚动条样式 */

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: #c1c1c1;
          border-radius: 3px;

          &:hover {
            background: #a8a8a8;
          }
        }

        /deep/ .ant-tree-node-content-wrapper {
          padding: 4px 8px;
          border-radius: 6px;
          margin: 2px 0;
          transition: all 0.3s ease;
          position: relative;

          &.ant-tree-node-selected {
            background: linear-gradient(135deg, #e6f7ff 0%, #f0f8ff 100%) !important;
            color: #1890ff !important;
            font-weight: 600;
            border: 2px solid #91d5ff;
            box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
            transform: translateX(4px);

            &::before {
              content: '';
              position: absolute;
              left: -2px;
              top: 50%;
              transform: translateY(-50%);
              width: 4px;
              height: 16px;
              background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
              border-radius: 2px;
            }
          }

          &:hover {
            background: #f0f8ff;
            transform: translateX(2px);
          }

          &:active {
            transform: translateX(1px);
          }
        }

        /deep/ .ant-tree-checkbox {
          margin-right: 8px;
        }

        /deep/ .ant-tree-switcher {
          margin-right: 4px;
        }

        /deep/ .ant-tree-checkbox {
          margin-right: 6px;
        }

        /deep/ .ant-tree-title {
          font-size: 13px;
          color: #262626;
          writing-mode: horizontal-tb !important;
          text-orientation: mixed !important;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          transition: all 0.3s ease;
        }

        /deep/ .ant-tree-node-selected .ant-tree-title {
          color: #1890ff !important;
          font-weight: 600;
        }

        /deep/ .ant-tree-switcher {
          transition: all 0.3s ease;
        }

        /deep/ .ant-tree-switcher_open,
        /deep/ .ant-tree-switcher_close {
          color: #8c8c8c;
        }

        /deep/ .ant-tree-node-selected .ant-tree-switcher {
          color: #1890ff;
        }

        /* 添加选中状态的额外视觉效果 */

        /deep/ .ant-tree-node-selected {
          .ant-tree-node-content-wrapper {
            &::after {
              content: '';
              position: absolute;
              right: 8px;
              top: 50%;
              transform: translateY(-50%);
              width: 8px;
              height: 8px;
              background: #1890ff;
              border-radius: 50%;
              box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
              animation: pulse 2s infinite;
            }
          }
        }

        @keyframes pulse {
          0% {
            box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.4);
          }

          70% {
            box-shadow: 0 0 0 6px rgba(24, 144, 255, 0);
          }

          100% {
            box-shadow: 0 0 0 0 rgba(24, 144, 255, 0);
          }
        }
      }
    }
  }

  .right-content {
    flex: 1;
    padding: 16px;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .group-detail-container {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      .device-list-section {
        flex: 1;
        background: white;
        border-radius: 4px;
        padding: 16px;
        border: 1px solid #f0f0f0;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .group-info-display {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          padding-bottom: 16px;
          border-bottom: 1px solid #f0f0f0;
          flex-shrink: 0;

          .group-basic-info {
            .group-name {
              margin: 0 0 8px 0;
              font-size: 20px;
              font-weight: 600;
              color: #262626;
            }

            .group-meta {
              font-size: 13px;
              color: #8c8c8c;
              display: flex;
              align-items: center;
              gap: 12px;

              .group-id {
                color: #1890ff;
                font-weight: 500;
              }

              .group-desc {
                color: #595959;
              }
            }
          }
        }

        .list-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
          padding-bottom: 12px;
          border-bottom: 1px solid #f0f0f0;
          flex-shrink: 0;

          .header-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .section-title {
              margin: 0;
              font-size: 16px;
              font-weight: 600;
              color: #262626;
            }

            .device-count {
              font-size: 13px;
              color: #8c8c8c;
              background: #f5f5f5;
              padding: 2px 8px;
              border-radius: 12px;
            }
          }

          .header-actions {
            display: flex;
            gap: 8px;

            .unbind-btn {
              background: #ff4d4f;
              border-color: #ff4d4f;
              color: white;

              &:hover {
                background: #ff7875;
                border-color: #ff7875;
              }

              &:disabled {
                background: #ffccc7;
                border-color: #ffccc7;
                color: rgba(255, 255, 255, 0.65);
              }
            }
          }
        }

        .table-wrapper {
          flex: 1;
          display: flex;
          flex-direction: column;
          overflow: hidden;

          .device-table {
            flex: 1;
            overflow: hidden;

            /deep/ .ant-table-wrapper {
              height: 100%;
              display: flex;
              flex-direction: column;
            }

            /deep/ .ant-table {
              flex: 1;
              overflow: hidden;
            }

            /deep/ .ant-table-thead > tr > th {
              padding: 12px 8px;
              font-size: 13px;
              font-weight: 500;
              background: #fafafa;
            }

            /deep/ .ant-table-tbody > tr > td {
              padding: 12px 8px;
              font-size: 13px;
            }

            /deep/ .ant-table-tbody > tr:hover > td {
              background: #f6ffed;
            }
          }

          .ant-table-pagination {
            margin-top: 24px;
            margin-bottom: 0;
            text-align: right;
            flex-shrink: 0;
            padding-top: 16px;
            border-top: 1px solid #f0f0f0;
          }
        }
      }
    }
  }
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
</style>
