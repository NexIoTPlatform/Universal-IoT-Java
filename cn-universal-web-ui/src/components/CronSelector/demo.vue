<!-- 
  CronSelector 组件使用示例
  这个文件展示了 CronSelector 组件的各种使用方式
-->
<template>
  <div class="cron-selector-demo">
    <a-card title="CronSelector 组件示例" :bordered="false">
      
      <!-- 示例1: 基础使用 -->
      <a-divider orientation="left">示例1: 基础使用</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <cron-selector v-model="example1.cron" @change="handleExample1Change" />
        </a-col>
        <a-col :span="12">
          <a-card size="small" title="当前配置">
            <p>表达式: <a-tag color="green">{{ example1.cron || '未配置' }}</a-tag></p>
          </a-card>
        </a-col>
      </a-row>

      <!-- 示例2: 指定默认模式 -->
      <a-divider orientation="left">示例2: 默认使用可视化模式</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <cron-selector 
            v-model="example2.cron" 
            :default-mode="'visual'"
            @change="handleExample2Change" 
          />
        </a-col>
        <a-col :span="12">
          <a-card size="small" title="当前配置">
            <p>表达式: <a-tag color="green">{{ example2.cron || '未配置' }}</a-tag></p>
          </a-card>
        </a-col>
      </a-row>

      <!-- 示例3: 在表单中使用 -->
      <a-divider orientation="left">示例3: 在表单中使用</a-divider>
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="任务名称">
          <a-input v-model="example3.taskName" placeholder="请输入任务名称" style="width: 300px;" />
        </a-form-item>
        
        <a-form-item label="执行时间">
          <cron-selector 
            v-model="example3.cron"
            style="width: 400px;"
          />
        </a-form-item>
        
        <a-form-item label="任务描述">
          <a-textarea 
            v-model="example3.description" 
            placeholder="请输入任务描述"
            :rows="3"
            style="width: 400px;"
          />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ span: 20, offset: 4 }">
          <a-space>
            <a-button type="primary" @click="handleExample3Submit">
              保存任务
            </a-button>
            <a-button @click="handleExample3Reset">
              {{ $t('button.reset') }} </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 示例4: 使用组件方法 -->
      <a-divider orientation="left">示例4: 使用组件方法</a-divider>
      <a-row :gutter="16">
        <a-col :span="12">
          <cron-selector 
            ref="cronSelectorRef"
            v-model="example4.cron"
          />
          <div style="margin-top: 16px;">
            <a-space>
              <a-button @click="clearExample4">清空配置</a-button>
              <a-button @click="setExample4Daily">设置为每天执行</a-button>
              <a-button @click="setExample4Weekly">设置为每周执行</a-button>
            </a-space>
          </div>
        </a-col>
        <a-col :span="12">
          <a-card size="small" title="当前配置">
            <p>表达式: <a-tag color="green">{{ example4.cron || '未配置' }}</a-tag></p>
          </a-card>
        </a-col>
      </a-row>

      <!-- 示例5: 多个定时任务 -->
      <a-divider orientation="left">示例5: 管理多个定时任务</a-divider>
      <a-list :data-source="example5.tasks" bordered>
        <a-list-item slot="renderItem" slot-scope="item, index">
          <a-list-item-meta>
            <span slot="title">{{ item.name }}</span>
            <div slot="description">
              <cron-selector 
                v-model="item.cron"
                @change="val => handleExample5Change(index, val)"
              />
            </div>
          </a-list-item-meta>
          <template slot="actions">
            <a @click="removeExample5Task(index)">{{ $t('button.delete') }}</a>
          </template>
        </a-list-item>
        <div slot="header">
          <a-button type="dashed" icon="plus" @click="addExample5Task" block>
            添加定时任务
          </a-button>
        </div>
      </a-list>

    </a-card>
  </div>
</template>

<script>
import CronSelector from '@/components/CronSelector'

export default {
  name: 'CronSelectorDemo',
  components: {
    CronSelector
  },
  data() {
    return {
      // 示例1
      example1: {
        cron: '0 9 * * *'
      },
      
      // 示例2
      example2: {
        cron: ''
      },
      
      // 示例3
      example3: {
        taskName: '',
        cron: '',
        description: ''
      },
      
      // 示例4
      example4: {
        cron: ''
      },
      
      // 示例5
      example5: {
        tasks: [
          { name: '数据备份任务', cron: '0 0 * * *' },
          { name: '日志清理任务', cron: '0 2 * * *' },
          { name: '报表生成任务', cron: '0 9 * * 1' }
        ]
      }
    }
  },
  methods: {
    // 示例1
    handleExample1Change(expression) {
      console.log('Example 1 - Cron changed:', expression)
      this.$message.info(`表达式已更新: ${expression}`)
    },
    
    // 示例2
    handleExample2Change(expression) {
      console.log('Example 2 - Cron changed:', expression)
    },
    
    // 示例3
    handleExample3Submit() {
      if (!this.example3.taskName) {
        this.$message.warning('请输入任务名称')
        return
      }
      if (!this.example3.cron) {
        this.$message.warning('请配置执行时间')
        return
      }
      
      this.$message.success('任务保存成功')
      console.log('Task submitted:', this.example3)
    },
    
    handleExample3Reset() {
      this.example3 = {
        taskName: '',
        cron: '',
        description: ''
      }
      this.$message.info('表单已重置')
    },
    
    // 示例4
    clearExample4() {
      this.$refs.cronSelectorRef.clear()
      this.$message.info('配置已清空')
    },
    
    setExample4Daily() {
      this.$refs.cronSelectorRef.setExpression('0 9 * * *')
      this.$message.success('已设置为每天上午9点执行')
    },
    
    setExample4Weekly() {
      this.$refs.cronSelectorRef.setExpression('0 9 * * 1')
      this.$message.success('已设置为每周一上午9点执行')
    },
    
    // 示例5
    handleExample5Change(index, expression) {
      console.log(`Task ${index} cron changed:`, expression)
    },
    
    addExample5Task() {
      this.example5.tasks.push({
        name: `新任务 ${this.example5.tasks.length + 1}`,
        cron: ''
      })
    },
    
    removeExample5Task(index) {
      this.example5.tasks.splice(index, 1)
      this.$message.success('任务已删除')
    }
  }
}
</script>

<style scoped>
.cron-selector-demo {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}
</style>
