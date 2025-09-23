<template>

  <a-card :bordered="false">

    <div class="title">
      <a-button type="text" icon="left" @click="$router.back()" class="icon"></a-button>
      <span class="name">批量历史</span>
    </div>

    <!-- 条件搜索 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="任务名称" prop="taskName">
              <a-input v-model="queryParam.taskName" placeholder="请输入任务名称"
                       @keyup.enter="handleQuery" allow-clear/>
            </a-form-item>
          </a-col>
          <a-col :md="16" :sm="24">
            <a-form-item label="开始时间" prop="rangeTime">
              <a-range-picker
                v-model="queryParam.rangeTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                valueFormat="YYYY-MM-DD HH:mm:ss"
                allow-clear
                :model="timeMode"
                @openChange="openTimeChange"
              ></a-range-picker>
            </a-form-item>
          </a-col>

          <a-col md="8" :sm="24">
            <span class="table-page-search-submitButtons">
              <a-button type="primary" @click="handleQuery"><iot-icon
                type="icon-search"/>查询</a-button>
              <a-button style="margin-left: 8px" @click="resetQuery"><iot-icon type="icon-refresh"/>重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <a-table
      :loading="loading"
      rowKey="id"
      :columns="columns"
      :data-source="list"
      :pagination="false">

      <div slot="command" slot-scope="text, record">
        <span>{{ record.command }}</span>
        <a @click="showCmdData(record)" style="margin-left: 10px;">
          <a-icon type="file-text"/>
          详情
        </a>
      </div>

      <div slot="totalNum" slot-scope="text, record">
        <span>{{ record.successNum || 0 }} / {{ record.totalNum || 0 }}</span>
        <a @click="showTaskDetail(record)" style="margin-left: 10px;">
          <a-icon type="file-text"/>
          详情
        </a>
      </div>

    </a-table>
    <a-pagination
      class="ant-table-pagination"
      show-size-changer
      show-quick-jumper
      :current="queryParam.pageNum"
      :total="total"
      :page-size="queryParam.pageSize"
      :showTotal="total => `共 ${total} 条`"
      @showSizeChange="onShowSizeChange"
      @change="changeSize"
    />

    <a-modal
      title="指令详情"
      :visible="showCmd"
      @cancel="showCmd = false"
      footer=""
    >
      <p>{{ cmdData }}</p>
    </a-modal>

    <TaskDetail ref="taskDetail"/>

  </a-card>

</template>

<script>
import {batchTaskList} from '@/api/system/dev/batch'
import TaskDetail from './components/TaskDetail.vue'

export default {
  name: 'BatchHistory',
  components: {TaskDetail},
  data() {
    return {
      timeMode: 'time',

      list: [],

      loading: false,
      total: 0,
      // 0-离线，1-在线字典
      stateOptions: [],
      // 查询参数
      queryParam: {
        taskName: undefined,
        rangeTime: [],
        pageNum: 1,
        pageSize: 10
      },
      showCmd: false,
      cmdData: '',
      columns: [
        {
          title: '任务名称',
          dataIndex: 'taskName',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '开始时间',
          dataIndex: 'beginTime',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '完成时间',
          dataIndex: 'endTime',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '操作人',
          dataIndex: 'creator',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '功能指令',
          dataIndex: 'command',
          scopedSlots: {customRender: 'command'},
          ellipsis: true,
          align: 'center'
        },
        {
          title: '成功数量/总数',
          dataIndex: 'totalNum',
          scopedSlots: {customRender: 'totalNum'},
          ellipsis: true,
          align: 'center'
        }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    openTimeChange(open) {
      if (open) {
        this.timeMode = 'time'
      }
    },
    // 0-离线，1-在线 字典翻译
    stateFormat(row, column) {
      return this.selectDictLabel(this.stateOptions, row.state)
    },
    /** 查询设备列表 */
    getList() {
      const queryParams = {
        taskName: this.queryParam.taskName,
        params: {
          queryBeginTime: this.queryParam.rangeTime.length ? this.queryParam.rangeTime[0]
            : undefined,
          queryEndTime: this.queryParam.rangeTime.length ? this.queryParam.rangeTime[1] : undefined
        },
        pageNum: this.queryParam.pageNum,
        pageSize: this.queryParam.pageSize
      }
      this.loading = true
      batchTaskList(queryParams).then(response => {
        this.list = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParam.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        taskName: undefined,
        rangeTime: [],
        pageNum: 1,
        pageSize: 10
      }
      this.handleQuery()
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
    showCmdData(record) {
      this.cmdData = record.commandData
      this.showCmd = true
    },
    showTaskDetail(record) {
      this.$refs.taskDetail.open(record)
    }
  }
}
</script>

<style scoped lang="less">
:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fff;
}

:deep(.ant-table) {
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  font-size: 14px;
}

:deep(.ant-table-thead > tr > th) {
  background: #f7f8fa;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 8px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  color: #262626;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f7fa;
}

:deep(.ant-pagination) {
  margin-top: 24px;
  text-align: right;
}

:deep(.ant-btn) {
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}

:deep(.ant-btn-primary) {
  background: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-btn-primary:hover) {
  background: #40a9ff;
  border-color: #40a9ff;
}

:deep(.ant-btn-link) {
  color: #1890ff;
  font-weight: 500;
  padding: 0;
}

:deep(.ant-btn-link:hover) {
  color: #40a9ff;
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
    width: 5px;
    height: 5px;
    border-radius: 50%;
    margin-right: 4px;
    background-color: #52c41a;
  }
}

.offline {
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

.title {
  margin: 15px 0;
  font-size: 20px;
  display: flex;
  align-items: center;

  .icon {
    background-color: #f7f8fa;
    border: none;
  }

  .name {
    font-size: 24px;
    font-weight: 600;
    margin: 0 10px;
  }
}
</style>
