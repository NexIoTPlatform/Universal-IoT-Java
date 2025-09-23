<template>
  <a-modal v-model="visible" title="网关列表" @ok="handleOk" width="400px">
    <a-input-search
      allowClear
      v-model.trim="keywords"
      placeholder="请输入关键词"
      enter-button
      @input="onSearch"
      @keyup.enter.native="onSearch"
      @search="onSearch"
    />

    <div class="wrap">

      <a-radio-group v-model="selectedValue">
        <a-row>
          <a-col :span="24" style="margin-top: 20px" v-for="item in list" :key="item.productKey">
            <a-radio :value="item.productKey">{{ item.name }}</a-radio>
          </a-col>
        </a-row>
      </a-radio-group>

    </div>

  </a-modal>
</template>
<script>
import {gatewayList} from '@/api/system/dev/product'

export default {
  props: {
    selectedGateway: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      visible: false,
      originList: [],
      list: [],
      keywords: '',
      selectedValue: ''
    }
  },
  watch: {
    selectedValue(value) {
      // 单选不需要复杂的逻辑
    }
  },
  methods: {
    showModal() {
      this.keywords = ''
      this.selectedValue = this.selectedGateway ? this.selectedGateway.productKey : ''
      this.visible = true
      this.getList()
    },

    handleOk() {
      if (this.selectedValue) {
        const selectedItem = this.originList.find(i => i.productKey === this.selectedValue)
        this.$emit('add', selectedItem)
      }
      this.visible = false
    },
    onSearch() {
      this.list = this.originList.filter(item => item.name.includes(this.keywords))
    },
    getList() {
      gatewayList().then(res => {
        this.originList = res.data
        this.list = res.data
      })
    }
  }
}
</script>

<style lang="less" scoped>
.wrap {
  height: 200px;
  overflow-y: auto;
}
</style>
