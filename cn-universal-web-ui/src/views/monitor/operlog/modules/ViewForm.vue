<template>
  <a-drawer width="35%" placement="right" :closable="false" :visible="openView"
            @close="onCloseView">
    <a-form ref="form" :model="form" laba-width="100px" size="default">
      <a-row>
        <a-col :span="12">
          <a-form-item label="操作模块：">{{ form.title }}</a-form-item>
          <a-form-item
            label="登录信息："
          >{{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="请求地址：">{{ form.operUrl }}</a-form-item>
          <a-form-item label="请求方式：">{{ form.requestMethod }}</a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="操作方法：">{{ form.method }}</a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="请求参数：">{{ form.operParam }}</a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="返回参数：">{{ form.jsonResult }}</a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="操作状态：">
            <div v-if="form.status === 0">正常</div>
            <div v-else-if="form.status === 1">失败</div>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="操作时间：">{{ parseTime(form.operTime) }}</a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="异常信息：" v-if="form.status === 1">{{ form.errorMsg }}</a-form-item>
        </a-col>
      </a-row>
    </a-form>
    <div slot="footer" class="dialog-footer">
      <a-button @click="openView = false">关 闭</a-button>
    </div>
  </a-drawer>
</template>

<script>

export default {
  name: 'ViewForm',
  props: {},
  data() {
    return {
      // 表单参数
      form: {},
      openView: false
    }
  },
  filters: {},
  created() {
  },
  computed: {},
  watch: {},
  methods: {
    handleView(row) {
      this.openView = true
      this.form = row
    },
    onCloseView() {
      this.openView = false
    }
  }
}
</script>
