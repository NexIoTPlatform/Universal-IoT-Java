<template>
  <a-drawer width="65%" :label-col="4" :wrapper-col="14" :visible="open" @close="onClose">
    <a-divider orientation="left">
      <b>添加参数</b>
    </a-divider>
    <a-alert message="只能使用物模型中'读写'的属性为参数。" banner style="margin-bottom: 10px"/>
    <a-table
      :loading="loading"
      :size="tableSize"
      :row-key="record => record.id"
      :columns="properties"
      :data-source="propertieslist"
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :pagination="false">
      <span slot="mode" slot-scope="text, record">
        <span v-if="record.mode === 'r'">{{ '只读' }}</span>
        <span v-else-if="record.mode === 'rw'">{{ '读写' }}</span>
      </span>
    </a-table>

    <div class="bottom-control">
      <a-space>
        <!--@click="submitForm"-->
        <a-button type="primary" @click="submit">
          保存
        </a-button>
        <!--@click="cancel"-->
        <a-button type="dashed" @click="onClose">
          取消
        </a-button>
      </a-space>
    </div>
  </a-drawer>
</template>

<script>
import {getFunctionPropertiesList} from '@/api/system/dev/product'

export default {
  props: {
    show: {
      type: Boolean,
      require: true
    },
    productId: {
      type: String,
      require: true,
      default: ''
    }
  },
  data() {
    return {
      open: false,
      loading: false,
      propertieslist: [],
      properties: [
        {
          title: '属性标识',
          dataIndex: 'id',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '属性名称',
          dataIndex: 'name',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '数据类型',
          dataIndex: 'type',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '单位',
          dataIndex: 'unit',
          ellipsis: true,
          align: 'center'
        },
        {
          title: '只读',
          dataIndex: 'mode',
          ellipsis: true,
          align: 'center',
          scopedSlots: {customRender: 'mode'}
        },
        {
          title: '属性来源',
          dataIndex: 'source',
          ellipsis: true,
          align: 'center',
          customRender: (text) => {
            return text === 'device' ? '设备' : text === 'extended' ? '扩展' : text
          }
        },
        {
          title: '描述',
          dataIndex: 'description',
          ellipsis: true,
          align: 'center'
        }
      ],
      selectedRowKeys: [],
      selectedRows: []
    }
  },
  watch: {
    show(val) {
      this.open = val
      if (this.open) {
        this.getList()
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 关闭抽屉
    onClose(e) {
      this.$emit('ok')
      this.open = false
    },
    getList() {
      this.loading = true
      getFunctionPropertiesList(this.productId).then(response => {
        this.propertieslist = response.data
        this.loading = false
      })
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
      console.log(selectedRowKeys)
      console.log(selectedRows)
    },
    submit() {
      this.$emit('addProperties', this.selectedRows)
      this.onClose()
    }
  }
}
</script>

<style scoped lang="less">

</style>
