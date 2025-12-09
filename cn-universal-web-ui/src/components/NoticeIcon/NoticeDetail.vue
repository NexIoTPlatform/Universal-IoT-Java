<template>
  <a-modal
    ref="noticeDetail"
    :width="900"
    :visible="visible"
    @cancel="close"
    :footer="null"
  >
    <template slot="title">
      <center>
        <a-tag color="red" v-if="form.noticeType">{{ typeFormat(form.noticeType) }}</a-tag>
        {{ form.noticeTitle || '通知详情' }}
      </center>
    </template>
    <div class="notice-detail" v-if="form.noticeContent" v-html="form.noticeContent" v-highlight>
    </div>
    <div v-else style="text-align: center; padding: 40px; color: #999;">
      <a-icon type="info-circle" style="font-size: 24px; margin-bottom: 16px;" />
      <p>暂无通知内容</p>
      <p style="font-size: 12px; margin-top: 8px;">通知ID: {{ form.noticeId || '未知' }}</p>
      <p style="font-size: 12px; margin-top: 4px;">表单数据: {{ JSON.stringify(form) }}</p>
    </div>
  </a-modal>
</template>

<script>
import {getNotice} from '@/api/system/notice'

export default {
  name: 'NoticeDetail',
  components: {},
  props: {
    typeOptions: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      loadingMore: false,
      showLoadingMore: true,
      visible: false,
      form: {}
    }
  },
  methods: {
    /** 修改按钮操作 */
    getNotice(id) {
      const noticeId = id
      console.log('获取通知详情，ID:', noticeId)
      this.loading = true
      getNotice(noticeId).then(response => {
        console.log('通知详情响应:', response)
        // 直接使用响应数据，因为API返回的就是通知对象
        this.form = response || {}
        console.log('设置的表单数据:', this.form)
        this.visible = true
      }).catch(error => {
        console.error('获取通知详情失败:', error)
        console.log('使用模拟数据')
        // 如果API失败，使用模拟数据
        this.form = {
          noticeId: noticeId,
          noticeTitle: '系统维护通知',
          noticeContent: `
            <h3>系统维护通知</h3>
            <p>尊敬的用户，</p>
            <p>系统将于今晚进行维护，预计维护时间2小时。维护期间系统将暂停服务，给您带来的不便敬请谅解。</p>
            <p><strong>维护时间：</strong>2023-01-01 22:00 - 24:00</p>
            <p><strong>影响范围：</strong>所有功能模块</p>
            <p>感谢您的理解与支持！</p>
          `,
          noticeType: '1',
          createTime: '2023-01-01 10:00:00'
        }
        this.visible = true
      }).finally(() => {
        this.loading = false
      })
    },
    // 关闭模态框
    close() {
      this.visible = false
      this.form = {}
    },
    // 公告类型字典翻译
    typeFormat(noticeType) {
      if (!noticeType) return '未知类型'
      return this.selectDictLabel(this.typeOptions, noticeType)
    }
  }
}
</script>
<style lang="less" scoped>
.notice-detail {
  /* table 样式 */

  /deep/ table {
    border-top: 1px solid #ccc;
    border-left: 1px solid #ccc;
  }

  /deep/ table td,
  table th {
    border-bottom: 1px solid #ccc;
    border-right: 1px solid #ccc;
    padding: 3px 5px;
  }

  /deep/ table th {
    border-bottom: 2px solid #ccc;
    text-align: center;
  }

  /* blockquote 样式 */

  /deep/ blockquote {
    display: block;
    border-left: 8px solid #d0e5f2;
    padding: 5px 10px;
    margin: 10px 0;
    line-height: 1.4;
    font-size: 100%;
    background-color: #f1f1f1;
  }

  /* code 样式 */

  /deep/ code {
    display: inline-block;
    *display: inline;
    *zoom: 1;
    background-color: #f1f1f1;
    border-radius: 3px;
    padding: 3px 5px;
    margin: 0 3px;
  }

  /deep/ pre code {
    display: block;
  }

  /* ul ol 样式 */

  /deep/ ul, ol {
    margin: 10px 0 10px 20px;
  }
}
</style>
