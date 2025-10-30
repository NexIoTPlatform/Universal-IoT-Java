<template>
  <page-header-wrapper>
    <div class="menu-list-container">
      <!-- 条件搜索 -->
      <a-card :bordered="false" class="search-card">
        <div class="table-page-search-wrapper">
          <a-form layout="inline">
            <a-row :gutter="48">
              <a-col :md="8" :sm="24">
                <a-form-item label="名称">
                  <a-input v-model="queryParam.menuName" placeholder="请输入"
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
                  <a-space>
                    <a-button type="primary" @click="handleQuery"><a-icon
                      type="search"/>{{ $t('button.search') }}</a-button>
                    <a-button @click="resetQuery"><a-icon type="sync"/>{{
                        $t('button.reset')
                      }}</a-button>
                  </a-space>
                </span>
              </a-col>
            </a-row>
          </a-form>
        </div>
      </a-card>
      <!-- 内容区 -->
      <a-card :bordered="false" class="content-card">
        <!-- 操作 -->
        <div class="table-operations">
          <a-space>
            <a-button type="primary" @click="$refs.createForm.handleAdd()"
                      v-hasPermi="['system:menu:add']">
              <a-icon type="plus"/>
              {{ $t('button.add') }}
            </a-button>
          </a-space>
          <a-tooltip :title="$t('button.refresh')">
            <a-button shape="circle" :loading="loading" :style="{float: 'right'}" icon="reload"
                      @click="getList"/>
          </a-tooltip>
        </div>
        <!-- 增加修改 -->
        <create-form
          ref="createForm"
          :menuOptions="menuOptions"
          :statusOptions="statusOptions"
          :visibleOptions="visibleOptions"
          @ok="getList"
          @select-tree="getTreeselect"
        />
        <!-- 数据展示 -->
        <a-table
          :loading="loading"
          :size="tableSize"
          rowKey="menuId"
          :columns="columns"
          :data-source="list"
          :pagination="false"
          class="menu-table"
        >
          <span slot="icon" slot-scope="text">
            <a-icon :component="allIcon[text]" v-if="allIcon[text]"/>
            <a-icon :component="allIcon[text + 'Icon']"
                    v-if="!allIcon[text] && allIcon[text + 'Icon']"/>
            <a-icon :type="text" v-if="!allIcon[text] && !allIcon[text + 'Icon']"/>
          </span>
          <span slot="status" slot-scope="text, record">
            {{ statusFormat(record) }}
          </span>
          <span slot="createTime" slot-scope="text, record">
            {{ parseTime(record.createTime) }}
          </span>
          <span slot="operation" slot-scope="text, record">
            <a-space>
              <a-button type="link" size="small" @click="$refs.createForm.handleUpdate(record)"
                        v-hasPermi="['system:menu:edit']">
                修改
              </a-button>
              <a-button type="link" size="small" @click="$refs.createForm.handleAdd(record)"
                        v-hasPermi="['system:menu:add']">
               {{ $t('button.add') }}
              </a-button>
              <a-button
                type="link"
                danger
                size="small"
                @click="handleDelete(record)"
                v-if="record.menuId != 0"
                v-hasPermi="['system:menu:remove']">
                {{ $t('button.delete') }} </a-button>
            </a-space>
          </span>
        </a-table>
      </a-card>
    </div>
  </page-header-wrapper>
</template>

<script>
import {delMenu, listMenu} from '@/api/system/menu'
import CreateForm from './modules/CreateForm'
import allIcon from '@/core/icons'

export default {
  name: 'Menu',
  components: {
    CreateForm
  },
  data() {
    return {
      allIcon,
      iconVisible: false,
      list: [],
      menuOptions: [],
      loading: false,
      statusOptions: [],
      visibleOptions: [],
      queryParam: {
        menuName: undefined,
        visible: undefined
      },
      columns: [
        {
          title: '菜单名称',
          dataIndex: 'menuName',
          ellipsis: true,
          width: '15%'
        },
        {
          title: '图标',
          dataIndex: 'icon',
          scopedSlots: {customRender: 'icon'},
          width: '5%',
          align: 'center'
        },
        {
          title: '排序',
          dataIndex: 'orderNum',
          width: '5%',
          align: 'center'
        },
        {
          title: '权限标识',
          dataIndex: 'perms',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '组件路径',
          dataIndex: 'component',
          scopedSlots: {customRender: 'component'},
          ellipsis: true,
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
          width: '20%',
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
    this.getDicts('sys_show_hide').then(response => {
      this.visibleOptions = response.data
    })
  },
  computed: {},
  watch: {},
  methods: {
    /** 查询菜单列表 */
    getList() {
      this.loading = true
      listMenu(this.queryParam).then(response => {
          this.list = this.handleTree(response.data, 'menuId')
          this.loading = false
        }
      )
    },
    // 字典状态字典翻译
    visibleFormat(row) {
      if (row.menuType === 'F') {
        return ''
      }
      return this.selectDictLabel(this.visibleOptions, row.visible)
    },
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
        memuName: undefined,
        status: undefined
      }
      this.handleQuery()
    },
    /** 查询菜单下拉树结构 */
    getTreeselect() {
      listMenu().then(response => {
        this.menuOptions = []
        const menu = {menuId: 0, menuName: '主目录', children: []}
        menu.children = this.handleTree(response.data, 'menuId')
        this.menuOptions.push(menu)
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      var that = this
      const menuId = row.menuId
      this.$confirm({
        title: '确认删除所选中数据?',
        content: '当前选中编号为' + menuId + '的数据',
        onOk() {
          return delMenu(menuId)
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
.menu-list-container {
  .search-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .content-card {
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .table-operations {
    padding-bottom: 20px;
    border-bottom: 1px solid #e8e8e8;
    margin-bottom: 20px;
  }

  .menu-table {
    .ant-btn-link {
      padding: 0;
      height: auto;
    }
  }

  .ant-table-pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
