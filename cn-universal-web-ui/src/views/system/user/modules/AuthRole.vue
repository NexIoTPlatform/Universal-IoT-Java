<template>
  <a-modal
    ref="authRole"
    :title="'为 ' + user.alias + ' 分配角色'"
    :width="700"
    :visible="visible"
    @cancel="close"
    @ok="confirm"
    :confirmLoading="confirmLoading"
    class="auth-role-modal"
  >
    <a-table
      :loading="loading"
      :size="tableSize"
      rowKey="roleId"
      :columns="columns"
      :data-source="list"
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ y: tableHeight }"
      :pagination="false"
      class="role-table"
    >
      <span slot="createTime" slot-scope="text, record">
        {{ parseTime(record.createTime) }}
      </span>
    </a-table>
  </a-modal>
</template>

<script>
import {getAuthRole, updateAuthRole} from '@/api/system/user'

export default {
  name: 'AuthRole',
  props: {},
  components: {},
  data() {
    return {
      list: [],
      user: {},
      selectedRowKeys: [],
      selectedRows: [],
      tableHeight: 'calc(100vh - 400px)', // 更现代的动态计算方式
      roleIds: [],
      loading: false,
      confirmLoading: false,
      visible: false,
      columns: [
        {
          title: '角色编号',
          dataIndex: 'roleId',
          width: '20%',
          align: 'center'
        },
        {
          title: '角色名称',
          dataIndex: 'roleName',
          width: '30%',
          align: 'center'
        },
        {
          title: '权限字符',
          dataIndex: 'roleKey',
          width: '30%',
          align: 'center'
        },
        {
          title: this.$t('time.create'),
          dataIndex: 'createTime',
          scopedSlots: {customRender: 'createTime'},
          width: '20%',
          align: 'center'
        }
      ]
    }
  },
  created() {
  },
  methods: {
    getList(id) {
      this.loading = true
      getAuthRole(id).then(res => {
        if (res.code === 0) {
          this.list = res.data.roles
          this.user = res.data.user
          this.$nextTick(() => {
            const roleIds = res.data.roles.filter(item => item.flag).map(item => item.roleId)
            this.roleIds = roleIds
            this.selectedRowKeys = roleIds
          })
          this.loading = false
        }
      })
    },
    close() {
      this.visible = false
      this.selectedRowKeys = []
      this.selectedRows = []
    },
    handleAuthRole(row) {
      this.visible = true
      this.getList(row.id)
    },
    confirm() {
      this.confirmLoading = true
      updateAuthRole({unionId: this.user.unionId, roleIds: this.roleIds}).then(res => {
        this.$message.success(res.msg)
        this.visible = false
        this.$emit('ok')
      }).finally(() => {
        this.confirmLoading = false
      })
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows.map(item => item.roleId)
    }
  }
}
</script>

<style lang="less">
.auth-role-modal {
  .ant-modal-body {
    padding: 16px 24px;
  }

  .role-table {
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    overflow: hidden;

    .ant-table-thead > tr > th {
      background-color: #fafafa;
      font-weight: 600;
    }

    .ant-table-tbody > tr:hover:not(.ant-table-expanded-row) > td {
      background: #e6f7ff;
    }

    /* 斑马纹 */

    .ant-table-tbody > tr.ant-table-row-level-0:nth-child(even) {
      background: #fafafa;
    }

    /* 滚动条美化 */

    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      border-radius: 4px;
      background: rgba(0, 0, 0, 0.2);
    }
  }
}
</style>
