<template>
  <a-modal
    :maskClosable="false"
    title="导入物模型"
    v-model="open"
    width="800px"
    :footer="null"
    @cancel="closePanel">
    <div style="height: 70vh">
      <CodeEditor :key="100" v-model="metadata"
                  :options="{mode: 'application/json', theme: 'material'}"/>
    </div>
    <a-row style="display: flex;align-items: flex-end;flex-direction: column;">
      <a-row style="margin-top: 10px">
        <a-button type="primary" @click="updateMetadata">
          保存
        </a-button>
        <a-button type="dashed" style="margin-left: 10px" @click="closePanel">{{
            $t('button.cancel')
          }}
        </a-button>
      </a-row>
    </a-row>
  </a-modal>
</template>

<script>
import CodeEditor from '@/components/CodeEditor.vue'
import {updateProduct} from '@/api/system/dev/product'

export default {
  name: 'IpmortMetadataVue',
  props: {
    productId: {
      type: String,
      require: true,
      default: ''
    },
    show: {
      type: Boolean,
      default: false
    }
  },
  components: {
    CodeEditor
  },
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
      }
    }
  },
  data() {
    return {
      // 是否显示弹出层
      open: false,
      // 遮罩层
      loading: false,
      // 物模型
      metadata: '',
      // 表单参数
      form: {
        id: null,
        metadata: null
      }
    }
  },
  methods: {
    /** 更新设备物模型 */
    updateMetadata() {
      console.log('this.metadata = ', this.metadata)
      console.log('this.metadata input ', this.metadata.match(/^[0-9]+$/))
      try {
        console.log('this.metadata = ', this.metadata)
        if (this.metadata.match(/^[0-9]+$/) !== undefined && this.metadata.match(/^[0-9]+$/)
          !== null) {
          this.$message.error('json 格式错误')
          return
        }
        JSON.parse(this.metadata)
        console.log('true Access')
        this.form.id = this.productId
        this.form.metadata = this.metadata // this.compressJSON(this.metadata, true)
        console.log('this.form = ', this.form)
        updateProduct(this.form).then(response => {
          this.$message.success(
            '导入成功,请刷新页面',
            3
          )
          this.open = false
          this.$emit('ok')
        })
      } catch (e) {
        this.$message.error('json 格式错误')
      }
    },
    // 关闭弹出层
    closePanel() {
      this.$emit('close')
    }
  }

}
</script>
<style scoped>

</style>
