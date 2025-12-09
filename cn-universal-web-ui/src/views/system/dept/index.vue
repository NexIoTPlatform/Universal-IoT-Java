<template>
  <page-header-wrapper>
    <a-card :bordered="false" class="search-card">
      <!-- 条件搜索 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="部门名称">
                <a-input v-model="queryParam.deptName" placeholder="请输入"
                         @keyup.enter="handleQuery" allow-clear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item :label="$t('common.status')">
                <a-select placeholder="请选择" v-model="queryParam.status" style="width: 100%"
                          allow-clear>
                  <a-select-option v-for="(d, index) in statusOptions" :key="index"
                                   :value="d.dictValue">{{ d.dictLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleQuery"><a-icon type="search"/>{{ $t('button.search') }}</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery"><a-icon
                  type="reload"/>{{ $t('button.reset') }}</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-card>
    <a-card :bordered="false" class="table-card">
      <!-- 操作 -->
      <div class="table-operations">
        <a-space>
          <a-button type="primary" @click="$refs.createForm.handleAdd()"
                    v-hasPermi="['system:dept:add']">
            <a-icon type="plus"/>
            {{ $t('button.add') }}</a-button>
        </a-space>
        <a-button
          type="dashed"
          shape="circle"
          :loading="loading"
          :style="{float: 'right'}"
          icon="reload"
          @click="getList"/>
      </div>
      <!-- 增加修改 -->
      <create-form
        ref="createForm"
        :deptOptions="deptOptions"
        :statusOptions="statusOptions"
        @ok="getList"
        @select-tree="getTreeselect"
      />
      <!-- 数据展示 -->
      <a-table
        :loading="loading"
        :size="tableSize"
        rowKey="deptId"
        :columns="columns"
        :data-source="list"
        :pagination="false">
        <span slot="status" slot-scope="text, record">
          {{ statusFormat(record) }}
        </span>
        <span slot="createTime" slot-scope="text, record">
          {{ parseTime(record.createTime) }}
        </span>
        <span slot="operation" slot-scope="text, record">
          <a-space>
            <a-button type="link" icon="edit" @click="$refs.createForm.handleUpdate(record)"
                      v-hasPermi="['system:dept:edit']">
              {{ $t('button.edit') }} </a-button>
            <a-button type="link" icon="plus" @click="$refs.createForm.handleAdd(record)"
                      v-hasPermi="['system:dept:add']">
               {{ $t('button.add') }}
            </a-button>
            <a-button
              type="link"
              icon="delete"
              danger
              @click="handleDelete(record)"
              v-if="record.parentId != 0"
              v-hasPermi="['system:dept:remove']">
              {{ $t('button.delete') }} </a-button>
          </a-space>
        </span>
      </a-table>
    </a-card>
  </page-header-wrapper>
</template>

<script>

import {delDept, listDept, listDeptExcludeChild} from '@/api/system/dept'
import CreateForm from './modules/CreateForm'

export default {
  name: 'Dept',
  components: {
    CreateForm
  },
  data() {
    return {
      list: [],
      // 部门树选项
      deptOptions: [],
      loading: false,
      // 状态数据字典
      statusOptions: [],
      queryParam: {
        deptName: undefined,
        status: undefined
      },
      columns: [
        {
          title: '部门名称',
          dataIndex: 'deptName'
        },
        {
          title: '排序',
          dataIndex: 'orderNum',
          align: 'center'
        },
        {
          title: this.$t('common.status'),
          dataIndex: 'status',
          scopedSlots: {customRender: 'status'},
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          ellipsis: true,
          scopedSlots: {customRender: 'createTime'},
          align: 'center'
        },
        {
          title: this.$t('user.operation'),
          dataIndex: 'operation',
          width: '30%',
          scopedSlots: {customRender: 'operation'},
          align: 'center'
        }
      ]
    }
  },
  filters: {},
  created() {
    this.getList()
    this.getDicts('sys_normal_disable').then(response => {
      this.statusOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询部门列表 */
    getList() {
      this.loading = true
      listDept(this.queryParam).then(response => {
          this.list = this.handleTree(response.data, 'deptId')
          this.loading = false
        }
      )
    },
    /** 查询菜单下拉树结构 */
    getTreeselect(row) {
      if (!row) {
        listDept().then(response => {
          this.deptOptions = this.handleTree(response.data, 'deptId')
        })
      } else {
        listDeptExcludeChild(row.deptId).then(response => {
          this.deptOptions = this.handleTree(response.data, 'deptId')
        })
      }
    },
    // 字典状态字典翻译
    statusFormat(row) {
      return this.selectDictLabel(this.statusOptions, row.status)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParam = {
        deptName: undefined,
        status: undefined
      }
      this.handleQuery()
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const deptId = row.deptId
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + deptId + '的数据',
        onOk() {
          return delDept(deptId)
            .then(() => {
              that.getList()
              that.$message.success(
                '删除成功',
                3
              )
            })
        },
        onCancel() {
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.search-card {
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.table-operations {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16px;
}
</style>
