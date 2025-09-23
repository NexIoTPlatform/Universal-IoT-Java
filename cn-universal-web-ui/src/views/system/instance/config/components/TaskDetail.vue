<template>
  <a-modal
    title="详情"
    width="1200px"
    :visible="visible"
    :confirm-loading="confirmLoading"
    @cancel="close"
    footer=""
  >
    <div>

      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="设备序列号" prop="deviceId">
                <a-input v-model="queryParam.deviceId" placeholder="请输入设备序列号"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="设备名称" prop="deviceName">
                <a-input v-model="queryParam.deviceName" placeholder="请输入设备名称"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="配置状态" prop="downState">
                <a-select v-model="queryParam.downState" placeholder="请选择配置状态"
                          @keyup.enter="handleQuery" allow-clear>
                  <a-select-option
                    v-for="item in downStateOptions"
                    :key="item.value"
                  >{{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="配置结果" prop="downResult">
                <a-select v-model="queryParam.downResult" placeholder="请选择配置结果"
                          @keyup.enter="handleQuery" allow-clear>
                  <a-select-option
                    v-for="item in downResultOptions"
                    :key="item.value"
                  >{{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery"><iot-icon
                  type="icon-search"/>查询</a-button>
                <a-button style="margin-left: 8px" type="primary" @click="reConfig(undefined)">
                  <a-icon type="setting"/>
                  重新配置
                </a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon
                  type="icon-refresh"/>重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>

        <a-table
          :loading="loading"
          rowKey="id"
          :columns="columns"
          :data-source="list"
          :pagination="false"
          :row-selection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectChange,
          }"
        >

          <div slot="downState" slot-scope="text, record">
            <span>{{ getStateValue(record.downState) }}</span>
          </div>

          <div slot="downResult" slot-scope="text, record">
            <div
              v-if="record.downResult !== null && record.downResult !== undefined"
              class="down-result"
              :class="record.downResult === 0 ? 'error' : 'success'"
            >
              <span>{{ getResultValue(record.downResult) }}</span>
              <span v-if="record.downResult === 0"> - {{ record.downError }}</span>

              <a
                v-if="record.downResult === 0"
                style="margin-left: 10px;"
                @click="reConfig(record)"
              >
                <a-icon type="setting"/>
                重新配置
              </a>
            </div>

          </div>

        </a-table>
        <div class="pagination-wrap">
          <a-pagination
            class="pagination"
            show-size-changer
            show-quick-jumper
            :current="queryParam.pageNum"
            :total="total"
            :page-size="queryParam.pageSize"
            :showTotal="total => `共 ${total} 条`"
            @showSizeChange="onShowSizeChange"
            @change="changeSize"
          />
        </div>

      </div>

    </div>
  </a-modal>
</template>

<script>
import {queryTaskById, reConfigTask} from '@/api/system/dev/batch'

export default {
  data() {
    return {
      id: '',
      visible: false,
      confirmLoading: false,
      queryParam: {
        deviceId: undefined,
        deviceName: undefined,
        downState: undefined,
        downResult: undefined,
        pageNum: 1,
        pageSize: 10
      },
      downStateOptions: [
        {
          label: '待下发',
          value: 0
        },
        {
          label: '已下发',
          value: 2
        }
      ],
      downResultOptions: [
        {
          label: '失败',
          value: 0
        },
        {
          label: '成功',
          value: 1
        }
      ],
      list: [],
      total: 0,
      columns: [
        {
          title: '设备序列号',
          dataIndex: 'deviceId',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '设备名称',
          dataIndex: 'deviceName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '配置状态',
          dataIndex: 'downState',
          scopedSlots: {customRender: 'downState'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '配置结果',
          dataIndex: 'downResult',
          scopedSlots: {customRender: 'downResult'},
          ellipsis: true,
          align: 'center'
        }
      ],
      loading: false,
      selectedRowKeys: []
    }
  },
  methods: {
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    getStateValue(val) {
      const result = this.downStateOptions.find(t => t.value === val)
      if (result) {
        return result.label
      }
      return ''
    },
    getResultValue(val) {
      const result = this.downResultOptions.find(t => t.value === val)
      if (result) {
        return result.label
      }
      return ''
    },
    open(record) {
      this.visible = true
      this.id = record.id
      this.getList()
    },
    close() {
      this.visible = false
    },
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParam = {
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
    },
    getList() {
      const params = {
        taskId: this.id,
        ...this.queryParam
      }
      this.loading = true
      queryTaskById(params)
      .then(res => {
        this.list = res.rows
        this.total = res.total
      })
      .finally(() => {
        this.loading = false
      })
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
    reConfig(record) {
      let ids
      if (record) {
        ids = [record.id]
      } else {
        ids = this.selectedRowKeys
        if (!ids.length) {
          this.$message.warning('请勾选数据项!')
          return
        }
      }
      this.$confirm({
        title: '确认重新配置?',
        onOk: () => {
          const data = {
            taskId: this.id,
            ids
          }
          reConfigTask(data).then(res => {
            this.$message.success('下发成功!')
            this.getList()
            this.selectedRowKeys = []
          })
        }
      })
    }
  }
}
</script>
<style lang="less" scoped>
.pagination-wrap {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.down-result {
  display: flex;
  justify-content: center;
  align-items: center;
}

.error {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #f5222d;
  }
}

.success {
  &::before {
    content: '';
    display: inline-block;
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #52c41a;
  }
}
</style>
