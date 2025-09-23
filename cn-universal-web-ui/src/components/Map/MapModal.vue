<template>
  <a-modal
    :title="title"
    width="800px"
    :visible="visible"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="close"
  >
    <div class="map-modal-container">
      <Map ref="map"/>
    </div>
  </a-modal>

</template>

<script>
import Map from './index.vue'

export default {
  components: {Map},
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      confirmLoading: false
    }
  },
  created() {
  },
  methods: {
    open() {
      this.visible = true
    },
    close() {
      this.visible = false
    },
    handleOk() {
      this.$emit('confirm', {
        locationStr: this.$refs.map.locationStr,
        location: this.$refs.map.location
      })
      this.close()
    }

  }
}
</script>
<style lang="less" scoped>
.map-modal-container {
  margin-top: 20px;
  width: 100%;
  height: 600px;
}
</style>
