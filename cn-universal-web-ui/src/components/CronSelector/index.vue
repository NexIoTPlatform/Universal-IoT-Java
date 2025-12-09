<template>
  <div class="cron-selector">
    <!-- 简化的主界面 - 只显示可视化配置按钮 -->
    <div class="main-config">
      <a-button 
        type="primary" 
        icon="setting" 
        @click="openVisualEditor" 
        block
        style="
          height: 48px;
          font-size: 16px;
          border-radius: 8px;
          background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
          border: none;
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
        "
      >
        <a-icon type="setting" style="margin-right: 8px;" />
        <span>打开可视化配置</span>
      </a-button>
    </div>

    <!-- 表达式预览区域 -->
    <div v-if="currentExpression" class="preview-section">
      <a-divider orientation="left" style="margin: 12px 0 8px 0;">
        <span style="font-size: 12px; color: #666;">当前配置</span>
      </a-divider>
      
      <div class="preview-content">
        <div class="preview-item">
          <a-tag color="blue" class="preview-tag">
            <a-icon type="info-circle" />
            {{ expressionDescription }}
          </a-tag>
        </div>
        <div class="preview-item">
          <a-tag color="green" class="preview-tag">
            <a-icon type="code" />
            {{ currentExpression }}
          </a-tag>
        </div>
      </div>

      <!-- 最近运行时间 -->
      <div v-if="recentRunTimes.length > 0" class="run-times">
        <a-divider orientation="left" style="margin: 12px 0 8px 0;">
          <span style="font-size: 12px; color: #666;">最近5次运行时间</span>
        </a-divider>
        <div class="run-times-list">
          <a-tag v-for="(time, index) in recentRunTimes" :key="index" color="orange" class="run-time-tag">
            {{ time }}
          </a-tag>
        </div>
      </div>
    </div>

    <!-- 可视化编辑器弹窗 -->
    <a-modal
      v-model="visualModalVisible"
      title="定时任务可视化配置"
      width="900px"
      :footer="null"
      :maskClosable="false"
      @cancel="closeVisualEditor"
      class="visual-modal"
      :bodyStyle="{ padding: '16px' }"
      :headerStyle="{ 
        background: 'linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)',
        borderBottom: 'none',
        color: 'white'
      }"
    >
      <div class="visual-editor-wrapper">
        <div class="visual-config">
          <!-- 配置方式Tab栏 -->
          <a-tabs v-model="activeTab" type="card" style="margin-bottom: 16px;">
            <a-tab-pane key="visual" tab="可视化配置">
              <div class="tab-content" style="
                padding: 12px;
                background: linear-gradient(135deg, #fff8f0 0%, #fff2e6 100%);
                border-radius: 8px;
                border: 1px solid #ffe6cc;
              ">
                <div class="section-header" style="
                  display: flex;
                  align-items: center;
                  margin-bottom: 12px;
                ">
                  <a-icon type="setting" style="
                    font-size: 14px;
                    color: #fa8c16;
                    margin-right: 6px;
                  " />
                  <span style="
                    font-size: 14px;
                    font-weight: 600;
                    color: #333;
                  ">可视化配置执行时间</span>
                </div>
                
                <!-- 时间配置 -->
                <div class="time-config" style="
                  margin-bottom: 16px;
                  padding: 12px;
                  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
                  border-radius: 8px;
                  border: 1px solid #e6f0ff;
                ">
            <div class="config-header" style="
              display: flex;
              align-items: center;
              margin-bottom: 12px;
            ">
              <a-icon type="clock-circle" class="header-icon" style="
                font-size: 14px;
                color: #1890ff;
                margin-right: 6px;
              " />
              <span class="header-title" style="
                font-size: 13px;
                font-weight: 600;
                color: #333;
              ">执行时间</span>
            </div>
            <div class="time-selectors" style="
              display: flex;
              gap: 12px;
            ">
              <div class="time-item" style="flex: 1;">
                <label class="time-label" style="
                  display: block;
                  margin-bottom: 6px;
                  font-size: 12px;
                  font-weight: 500;
                  color: #666;
                ">小时</label>
                <a-select v-model="visualConfig.hour" @change="updateVisualExpression" class="time-select" style="width: 100%;">
                  <a-select-option v-for="h in 24" :key="h-1" :value="h-1">
                    {{ h-1 }}点
                  </a-select-option>
                </a-select>
              </div>
              <div class="time-item" style="flex: 1;">
                <label class="time-label" style="
                  display: block;
                  margin-bottom: 6px;
                  font-size: 12px;
                  font-weight: 500;
                  color: #666;
                ">分钟</label>
                <a-select v-model="visualConfig.minute" @change="updateVisualExpression" class="time-select" style="width: 100%;">
                  <a-select-option v-for="m in 60" :key="m" :value="m">
                    {{ m }}分
                  </a-select-option>
                </a-select>
              </div>
              <div class="time-item" style="flex: 1;">
                <label class="time-label" style="
                  display: block;
                  margin-bottom: 6px;
                  font-size: 12px;
                  font-weight: 500;
                  color: #666;
                ">秒</label>
                <a-select v-model="visualConfig.second" @change="updateVisualExpression" class="time-select" style="width: 100%;">
                  <a-select-option v-for="s in 60" :key="s" :value="s">
                    {{ s }}秒
                  </a-select-option>
                </a-select>
              </div>
            </div>
          </div>

          <!-- 频率配置 -->
          <div class="frequency-config" style="
            margin-bottom: 16px;
            padding: 12px;
            background: linear-gradient(135deg, #fff8f0 0%, #fff2e6 100%);
            border-radius: 8px;
            border: 1px solid #ffe6cc;
          ">
            <div class="config-header" style="
              display: flex;
              align-items: center;
              margin-bottom: 12px;
            ">
              <a-icon type="calendar" class="header-icon" style="
                font-size: 14px;
                color: #fa8c16;
                margin-right: 6px;
              " />
              <span class="header-title" style="
                font-size: 13px;
                font-weight: 600;
                color: #333;
              ">执行频率</span>
            </div>
            <div class="frequency-options" style="margin-bottom: 12px;">
              <a-radio-group v-model="visualConfig.frequency" @change="onFrequencyChange" class="frequency-radio-group" style="
                display: flex;
                gap: 8px;
              ">
                <a-radio-button value="daily" class="frequency-radio" style="
                  flex: 1;
                  text-align: center;
                  height: 32px;
                  line-height: 30px;
                  border-radius: 6px;
                  border: 2px solid #e8e8e8;
                  transition: all 0.3s ease;
                  font-size: 12px;
                ">
                  <a-icon type="clock-circle" />
                  每天
                </a-radio-button>
                <a-radio-button value="weekly" class="frequency-radio" style="
                  flex: 1;
                  text-align: center;
                  height: 32px;
                  line-height: 30px;
                  border-radius: 6px;
                  border: 2px solid #e8e8e8;
                  transition: all 0.3s ease;
                  font-size: 12px;
                ">
                  <a-icon type="calendar" />
                  每周
                </a-radio-button>
                <a-radio-button value="monthly" class="frequency-radio" style="
                  flex: 1;
                  text-align: center;
                  height: 32px;
                  line-height: 30px;
                  border-radius: 6px;
                  border: 2px solid #e8e8e8;
                  transition: all 0.3s ease;
                  font-size: 12px;
                ">
                  <a-icon type="calendar" />
                  每月
                </a-radio-button>
                <a-radio-button value="interval" class="frequency-radio" style="
                  flex: 1;
                  text-align: center;
                  height: 32px;
                  line-height: 30px;
                  border-radius: 6px;
                  border: 2px solid #e8e8e8;
                  transition: all 0.3s ease;
                  font-size: 12px;
                ">
                  <a-icon type="field-time" />
                  间隔
                </a-radio-button>
              </a-radio-group>
            </div>

            <!-- 每周配置 -->
            <div v-if="visualConfig.frequency === 'weekly'" class="frequency-detail">
              <div class="detail-header">
                <a-icon type="calendar" />
                <span>选择星期</span>
              </div>
              <div class="weekday-selector">
                <a-checkbox-group v-model="visualConfig.weekdays" @change="updateVisualExpression" class="weekday-group">
                  <div class="weekday-item" v-for="(day, index) in weekdayOptions" :key="day.value">
                    <a-checkbox :value="day.value" class="weekday-checkbox">
                      <span class="weekday-text">{{ day.label }}</span>
                    </a-checkbox>
                  </div>
                </a-checkbox-group>
              </div>
            </div>

            <!-- 每月配置 -->
            <div v-if="visualConfig.frequency === 'monthly'" class="frequency-detail" style="
              margin-top: 12px;
              padding: 12px;
              background: white;
              border-radius: 6px;
              border: 1px solid #e8e8e8;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
            ">
              <div class="detail-header" style="
                display: flex;
                align-items: center;
                margin-bottom: 10px;
                font-size: 12px;
                font-weight: 500;
                color: #333;
              ">
                <a-icon type="calendar" style="
                  margin-right: 6px;
                  color: #1890ff;
                " />
                <span>选择日期</span>
              </div>
              <div class="monthday-selector">
                <!-- 快速选择按钮 -->
                <div style="margin-bottom: 16px;">
                  <a-button-group>
                    <a-button size="small" @click="selectAllMonthDays">全选</a-button>
                    <a-button size="small" @click="clearAllMonthDays">清空</a-button>
                    <a-button size="small" @click="selectWeekdays">工作日</a-button>
                    <a-button size="small" @click="selectWeekends">周末</a-button>
                  </a-button-group>
                </div>
                
                <!-- 日期网格选择器 -->
                <div style="
                  display: grid;
                  grid-template-columns: repeat(7, 1fr);
                  gap: 8px;
                  margin-bottom: 16px;
                ">
                  <div 
                    v-for="d in 31" 
                    :key="d"
                    @click="toggleMonthDay(d)"
                    style="
                      height: 36px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      border: 2px solid #e8e8e8;
                      border-radius: 6px;
                      cursor: pointer;
                      transition: all 0.3s ease;
                      font-size: 14px;
                      font-weight: 500;
                      background: white;
                      color: #333;
                    "
                    :style="{
                      background: visualConfig.monthDays.includes(d) ? '#1890ff' : 'white',
                      borderColor: visualConfig.monthDays.includes(d) ? '#1890ff' : '#e8e8e8',
                      color: visualConfig.monthDays.includes(d) ? 'white' : '#333'
                    }"
                  >
                    {{ d }}号
                  </div>
                </div>
                
                <!-- 已选日期标签 -->
                <div class="monthday-tags" v-if="visualConfig.monthDays.length > 0" style="
                  display: flex;
                  flex-wrap: wrap;
                  gap: 8px;
                  margin-top: 12px;
                ">
                  <a-tag 
                    v-for="day in visualConfig.monthDays" 
                    :key="day" 
                    closable 
                    @close="removeMonthDay(day)"
                    class="monthday-tag"
                    style="
                      background: #e6f7ff;
                      border-color: #91d5ff;
                      color: #1890ff;
                      border-radius: 6px;
                      padding: 4px 8px;
                      font-size: 12px;
                      height: 28px;
                      line-height: 20px;
                    "
                  >
                    {{ day }}号
                  </a-tag>
                </div>
              </div>
            </div>

            <!-- 间隔配置 -->
            <div v-if="visualConfig.frequency === 'interval'" class="frequency-detail" style="
              margin-top: 12px;
              padding: 12px;
              background: white;
              border-radius: 6px;
              border: 1px solid #e8e8e8;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
            ">
              <div class="detail-header" style="
                display: flex;
                align-items: center;
                margin-bottom: 10px;
                font-size: 12px;
                font-weight: 500;
                color: #333;
              ">
                <a-icon type="field-time" style="
                  margin-right: 6px;
                  color: #1890ff;
                " />
                <span>间隔设置</span>
              </div>
              <div class="interval-selector" style="
                display: flex;
                gap: 12px;
              ">
                <div class="interval-item" style="flex: 1;">
                  <label class="interval-label" style="
                    display: block;
                    margin-bottom: 6px;
                    font-size: 12px;
                    font-weight: 500;
                    color: #666;
                  ">间隔值</label>
                  <a-input-number 
                    v-model="visualConfig.intervalValue" 
                    :min="1" 
                    @change="updateVisualExpression"
                    class="interval-input"
                    style="
                      width: 100%;
                      height: 32px;
                      font-size: 12px;
                    "
                  />
                </div>
                <div class="interval-item" style="flex: 1;">
                  <label class="interval-label" style="
                    display: block;
                    margin-bottom: 6px;
                    font-size: 12px;
                    font-weight: 500;
                    color: #666;
                  ">间隔单位</label>
                  <a-select 
                    v-model="visualConfig.intervalUnit" 
                    @change="updateVisualExpression" 
                    class="interval-select"
                    style="
                      width: 100%;
                      height: 32px;
                    "
                    :dropdownStyle="{ 
                      maxHeight: '200px',
                      fontSize: '14px'
                    }"
                  >
                    <a-select-option 
                      value="hour"
                      style="
                        height: 36px;
                        line-height: 36px;
                        font-size: 14px;
                        padding: 0 12px;
                      "
                    >小时</a-select-option>
                    <a-select-option 
                      value="day"
                      style="
                        height: 36px;
                        line-height: 36px;
                        font-size: 14px;
                        padding: 0 12px;
                      "
                    >天</a-select-option>
                  </a-select>
                </div>
              </div>
            </div>
          </div>
              </div>
            </a-tab-pane>
            
            <a-tab-pane key="quick" tab="快速选择">
              <div class="tab-content" style="
                padding: 12px;
                background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
                border-radius: 8px;
                border: 1px solid #e6f0ff;
              ">
                <div class="section-header" style="
                  display: flex;
                  align-items: center;
                  margin-bottom: 12px;
                ">
                  <a-icon type="thunderbolt" style="
                    font-size: 14px;
                    color: #1890ff;
                    margin-right: 6px;
                  " />
                  <span style="
                    font-size: 14px;
                    font-weight: 600;
                    color: #333;
                  ">选择常用执行频率</span>
                </div>
                
                <a-select 
                  v-model="selectedPreset" 
                  placeholder="选择常用执行频率" 
                  @change="onPresetChange"
                  style="width: 100%;"
                >
                  <a-select-opt-group label="常用频率">
                    <a-select-option value="0 0 0 * * ?">
                      <a-icon type="clock-circle" /> 每天凌晨执行
                    </a-select-option>
                    <a-select-option value="0 0 9 * * ?">
                      <a-icon type="clock-circle" /> 每天上午9点
                    </a-select-option>
                    <a-select-option value="0 0 18 * * ?">
                      <a-icon type="clock-circle" /> 每天下午6点
                    </a-select-option>
                    <a-select-option value="0 0 9,18 * * ?">
                      <a-icon type="clock-circle" /> 每天9点和18点
                    </a-select-option>
                  </a-select-opt-group>
                  
                  <a-select-opt-group label="每周执行">
                    <a-select-option value="0 0 9 ? * 1">
                      <a-icon type="calendar" /> 每周一上午9点
                    </a-select-option>
                    <a-select-option value="0 0 9 ? * 1,3,5">
                      <a-icon type="calendar" /> 每周一、三、五上午9点
                    </a-select-option>
                    <a-select-option value="0 0 9 ? * 0">
                      <a-icon type="calendar" /> 每周日上午9点
                    </a-select-option>
                  </a-select-opt-group>
                  
                  <a-select-opt-group label="每月执行">
                    <a-select-option value="0 0 9 1 * ?">
                      <a-icon type="calendar" /> 每月1号上午9点
                    </a-select-option>
                    <a-select-option value="0 0 9 1,15 * ?">
                      <a-icon type="calendar" /> 每月1号和15号上午9点
                    </a-select-option>
                  </a-select-opt-group>
                  
                  <a-select-opt-group label="间隔执行">
                    <a-select-option value="0 0 */2 * * ?">
                      <a-icon type="field-time" /> 每2小时执行
                    </a-select-option>
                    <a-select-option value="0 0 */6 * * ?">
                      <a-icon type="field-time" /> 每6小时执行
                    </a-select-option>
                    <a-select-option value="0 0 9 */3 * ?">
                      <a-icon type="field-time" /> 每3天上午9点
                    </a-select-option>
                  </a-select-opt-group>
                </a-select>
              </div>
            </a-tab-pane>
            
            <a-tab-pane key="manual" tab="表达式输入">
              <div class="tab-content" style="
                padding: 12px;
                background: linear-gradient(135deg, #f6ffed 0%, #f0f9e8 100%);
                border-radius: 8px;
                border: 1px solid #d9f7be;
              ">
                <div class="section-header" style="
                  display: flex;
                  align-items: center;
                  margin-bottom: 12px;
                ">
                  <a-icon type="code" style="
                    font-size: 14px;
                    color: #52c41a;
                    margin-right: 6px;
                  " />
                  <span style="
                    font-size: 14px;
                    font-weight: 600;
                    color: #333;
                  ">手动输入Cron表达式</span>
                </div>
                
                <a-input 
                  v-model="manualExpression" 
                  placeholder="例如: 0 5 8 * * ? (每天8点5分执行)"
                  @change="onManualChange"
                  style="width: 100%;"
                >
                  <a-icon slot="prefix" type="edit" />
                  <a-tooltip slot="suffix" title="Cron表达式格式：秒 分 时 日 月 周">
                    <a-icon type="question-circle" style="color: rgba(0,0,0,.45)" />
                  </a-tooltip>
                </a-input>
                
                <div class="helper-links" style="
                  margin-top: 12px;
                  text-align: right;
                ">
                  <a href="https://crontab.guru/" target="_blank" rel="noopener noreferrer" style="
                    color: #52c41a;
                    text-decoration: none;
                    font-size: 12px;
                  ">
                    <a-icon type="link" /> Cron表达式参考
                  </a>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
          
          <!-- 表达式预览 -->
          <div v-if="tempExpression" class="preview-section" style="
            margin-top: 16px;
            padding: 12px;
            background: linear-gradient(135deg, #f6ffed 0%, #f0f9e8 100%);
            border-radius: 8px;
            border: 1px solid #d9f7be;
          ">
            <div class="preview-header" style="
              display: flex;
              align-items: center;
              margin-bottom: 12px;
            ">
              <a-icon type="eye" class="preview-icon" style="
                font-size: 14px;
                color: #52c41a;
                margin-right: 6px;
              " />
              <span class="preview-title" style="
                font-size: 13px;
                font-weight: 600;
                color: #333;
              ">配置预览</span>
            </div>
            
            <div class="preview-content" style="
              display: flex;
              flex-direction: column;
              gap: 10px;
            ">
              <div class="preview-item" style="
                background: white;
                padding: 10px;
                border-radius: 6px;
                border: 1px solid #e8e8e8;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
              ">
                <div class="preview-label" style="
                  display: flex;
                  align-items: center;
                  margin-bottom: 6px;
                  font-size: 12px;
                  font-weight: 500;
                  color: #666;
                ">
                  <a-icon type="info-circle" style="
                    margin-right: 4px;
                    color: #1890ff;
                  " />
                  <span>执行描述</span>
                </div>
                <div class="preview-value description-value" style="
                  font-size: 12px;
                  color: #1890ff;
                  font-weight: 500;
                  word-break: break-all;
                ">
                  {{ parseCronExpression(tempExpression) }}
                </div>
              </div>
              <div class="preview-item" style="
                background: white;
                padding: 10px;
                border-radius: 6px;
                border: 1px solid #e8e8e8;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
              ">
                <div class="preview-label" style="
                  display: flex;
                  align-items: center;
                  margin-bottom: 6px;
                  font-size: 12px;
                  font-weight: 500;
                  color: #666;
                ">
                  <a-icon type="code" style="
                    margin-right: 4px;
                    color: #1890ff;
                  " />
                  <span>Cron表达式</span>
                </div>
                <div class="preview-value expression-value" style="
                  font-size: 12px;
                  color: #333;
                  word-break: break-all;
                  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                  background: #f5f5f5;
                  padding: 6px 10px;
                  border-radius: 4px;
                  border: 1px solid #d9d9d9;
                ">
                  {{ tempExpression }}
                </div>
              </div>
            </div>

            <!-- 最近运行时间 -->
            <div v-if="recentRunTimes.length > 0" class="run-times" style="
              margin-top: 12px;
              background: white;
              padding: 10px;
              border-radius: 6px;
              border: 1px solid #e8e8e8;
            ">
              <div class="run-times-header" style="
                display: flex;
                align-items: center;
                margin-bottom: 8px;
              ">
                <a-icon type="clock-circle" class="run-times-icon" style="
                  font-size: 12px;
                  color: #fa8c16;
                  margin-right: 4px;
                " />
                <span class="run-times-title" style="
                  font-size: 12px;
                  font-weight: 500;
                  color: #333;
                ">最近5次运行时间</span>
              </div>
              <div class="run-times-list" style="
                display: flex;
                flex-direction: column;
                gap: 6px;
              ">
                <div v-for="(time, index) in recentRunTimes" :key="index" class="run-time-item" style="
                  display: flex;
                  align-items: center;
                  padding: 6px 10px;
                  background: #fff7e6;
                  border: 1px solid #ffd591;
                  border-radius: 4px;
                ">
                  <a-icon type="clock-circle" class="run-time-icon" style="
                    font-size: 11px;
                    color: #fa8c16;
                    margin-right: 6px;
                  " />
                  <span class="run-time-text" style="
                    font-size: 11px;
                    color: #d46b08;
                    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                  ">{{ time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer" style="
          text-align: right;
          padding: 12px 0 0 0;
          border-top: 1px solid #e8e8e8;
          margin-top: 12px;
        ">
          <a-space>
            <a-button @click="closeVisualEditor" class="cancel-btn" style="margin-right: 12px;">
              <a-icon type="close" />
              取消
            </a-button>
            <a-button type="primary" @click="confirmVisualConfig" class="confirm-btn" style="
              background: #1890ff;
              border-color: #1890ff;
              box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
            ">
              <a-icon type="check" />
              确定
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import cronParser from 'cron-parser'

export default {
  name: 'CronSelector',
  props: {
    value: {
      type: String,
      default: ''
    },
    // 是否显示详细描述
    showDescription: {
      type: Boolean,
      default: true
    },
    // 默认配置模式
  },
  data() {
    return {
      activeTab: 'visual',
      selectedPreset: '',
      manualExpression: '',
      currentExpression: this.value || '',
      recentRunTimes: [],
      visualModalVisible: false,
      tempExpression: '',
      visualConfig: {
        second: 0,
        minute: 0,
        hour: 9,
        frequency: 'daily',
        weekdays: [],
        monthDays: [],
        intervalValue: 1,
        intervalUnit: 'hour'
      },
      weekdayOptions: [
        { value: '0', label: '周日' },
        { value: '1', label: '周一' },
        { value: '2', label: '周二' },
        { value: '3', label: '周三' },
        { value: '4', label: '周四' },
        { value: '5', label: '周五' },
        { value: '6', label: '周六' }
      ]
    }
  },
  computed: {
    expressionDescription() {
      return this.parseCronExpression(this.currentExpression)
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(newVal) {
        if (newVal && newVal !== this.currentExpression) {
          this.currentExpression = newVal
          this.manualExpression = newVal
          this.parseExpressionToVisual(newVal)
          this.calculateRecentRunTimes()
        }
      }
    },
    currentExpression: {
      handler(newVal) {
        if (newVal) {
          this.calculateRecentRunTimes()
        }
      }
    }
  },
  methods: {
    
    onPresetChange(value) {
      this.selectedPreset = value
      // 同时更新tempExpression，确保确定时使用正确的值
      this.tempExpression = value
      this.updateExpression(value)
      // 计算最近运行时间
      if (value) {
        this.recentRunTimes = this.calculateCronRunTimes(value)
      } else {
        this.recentRunTimes = []
      }
    },
    
    onManualChange() {
      // 同时更新tempExpression，确保确定时使用正确的值
      this.tempExpression = this.manualExpression
      this.updateExpression(this.manualExpression)
      // 计算最近运行时间
      if (this.manualExpression) {
        this.recentRunTimes = this.calculateCronRunTimes(this.manualExpression)
      } else {
        this.recentRunTimes = []
      }
    },
    
    onFrequencyChange() {
      this.updateVisualExpression()
    },
    
    openVisualEditor() {
      this.tempExpression = this.currentExpression || '0 0 9 * * ?'
      this.parseExpressionToVisual(this.tempExpression)
      this.visualModalVisible = true
    },
    
    closeVisualEditor() {
      this.visualModalVisible = false
    },
    
    confirmVisualConfig() {
      this.updateExpression(this.tempExpression)
          this.visualModalVisible = false
          this.$message.success('定时任务配置成功')
    },
    
    updateVisualExpression() {
      const { second, minute, hour, frequency, weekdays, monthDays, intervalValue, intervalUnit } = this.visualConfig
      let expression = ''
      
      switch (frequency) {
        case 'daily':
          expression = `${second} ${minute} ${hour} * * ?`
          break
        case 'weekly':
          if (weekdays.length > 0) {
            // 转换星期格式：前端0=周日, 1=周一, ..., 6=周六 -> Quartz 1=周日, 2=周一, ..., 7=周六
            const convertedWeekdays = weekdays.map(day => {
              const dayNum = parseInt(day)
              // 前端: 0=周日, 1=周一, 2=周二, 3=周三, 4=周四, 5=周五, 6=周六
              // Quartz: 1=周日, 2=周一, 3=周二, 4=周三, 5=周四, 6=周五, 7=周六
              return dayNum + 1  // 直接+1：0->1, 1->2, 2->3, 3->4, 4->5, 5->6, 6->7
            }).sort((a, b) => a - b) // 排序确保顺序正确
            expression = `${second} ${minute} ${hour} ? * ${convertedWeekdays.join(',')}`
        } else {
            expression = `${second} ${minute} ${hour} * * ?`
          }
          break
        case 'monthly':
          if (monthDays.length > 0) {
            // 处理多天情况，确保天数排序正确
            const sortedDays = monthDays.map(day => parseInt(day)).sort((a, b) => a - b)
            expression = `${second} ${minute} ${hour} ${sortedDays.join(',')} * ?`
          } else {
            expression = `${second} ${minute} ${hour} 1 * ?`
          }
          break
        case 'interval':
          if (intervalUnit === 'hour') {
            expression = `${second} ${minute} */${intervalValue} * * ?`
          } else if (intervalUnit === 'day') {
            expression = `${second} ${minute} ${hour} */${intervalValue} * ?`
          }
          break
      }
      
      this.tempExpression = expression
      
      // 计算最近运行时间
      if (expression) {
        this.recentRunTimes = this.calculateCronRunTimes(expression)
        console.log('弹窗中计算运行时间:', expression, this.recentRunTimes)
      } else {
        this.recentRunTimes = []
      }
    },
    
    parseExpressionToVisual(expression) {
      if (!expression) return
      
      try {
        const parts = expression.trim().split(/\s+/)
        if (parts.length === 6) {
          const [second, minute, hour, day, month, weekday] = parts
          
          this.visualConfig.second = parseInt(second) || 0
          this.visualConfig.minute = parseInt(minute) || 0
          this.visualConfig.hour = parseInt(hour) || 0
          
          // 判断频率类型
          if (day === '*' && weekday === '?') {
            this.visualConfig.frequency = 'daily'
          } else if (day === '*' && weekday !== '*' && weekday !== '?') {
            this.visualConfig.frequency = 'weekly'
            // 转换星期格式：Quartz 1=周日, 2=周一, ..., 7=周六 -> 前端 0=周日, 1=周一, ..., 6=周六
            const convertedWeekdays = weekday.split(',').map(d => {
              const dayNum = parseInt(d)
              // Quartz: 1=周日, 2=周一, 3=周二, 4=周三, 5=周四, 6=周五, 7=周六
              // 前端: 0=周日, 1=周一, 2=周二, 3=周三, 4=周四, 5=周五, 6=周六
              return dayNum - 1  // 直接-1：1->0, 2->1, 3->2, 4->3, 5->4, 6->5, 7->6
            }).filter(d => d !== undefined && d >= 0 && d <= 6)
            this.visualConfig.weekdays = convertedWeekdays
          } else if (day !== '*' && weekday === '?') {
            this.visualConfig.frequency = 'monthly'
            // 处理多天情况，确保天数有效
            this.visualConfig.monthDays = day.split(',').map(d => parseInt(d)).filter(d => !isNaN(d) && d >= 1 && d <= 31)
          } else if (day.includes('/') || hour.includes('/')) {
            this.visualConfig.frequency = 'interval'
            if (hour.includes('/')) {
              this.visualConfig.intervalValue = parseInt(hour.split('/')[1]) || 1
              this.visualConfig.intervalUnit = 'hour'
            } else if (day.includes('/')) {
              this.visualConfig.intervalValue = parseInt(day.split('/')[1]) || 1
              this.visualConfig.intervalUnit = 'day'
            }
          }
        }
      } catch (error) {
        console.error('解析表达式失败:', error)
      }
    },
    
    updateExpression(expression) {
      this.currentExpression = expression
      this.$emit('input', expression)
      this.$emit('change', expression)
    },
    
    parseCronExpression(cron) {
      if (!cron) {
        return '未配置'
      }
      
      try {
        const parts = cron.trim().split(/\s+/)
        if (parts.length !== 6) {
          return '自定义表达式'
        }
        
        const [second, minute, hour, day, month, weekday] = parts
        let desc = []
        
        // 解析秒
        if (second !== '0') {
          desc.push(`第${second}秒`)
        }
        
        // 解析分钟
        if (minute === '0') {
          desc.push('整点')
        } else if (minute === '*') {
          desc.push('每分钟')
        } else if (minute.includes('/')) {
          const interval = minute.split('/')[1]
          desc.push(`每${interval}分钟`)
        } else if (minute.includes(',')) {
          desc.push(`第${minute.replace(/,/g, '、')}分钟`)
        } else {
          desc.push(`第${minute}分钟`)
        }
        
        // 解析小时
        if (hour === '*' && minute !== '*') {
          desc.push('每小时')
        } else if (hour === '*') {
          // 已经包含在分钟里了
        } else if (hour.includes('/')) {
          const interval = hour.split('/')[1]
          desc.push(`每${interval}小时`)
        } else if (hour.includes(',')) {
          const hours = hour.split(',').map(h => `${h}点`).join('、')
          desc.push(hours)
        } else {
          desc.push(`${hour}点`)
        }
        
        // 解析日期
        if (day === '*') {
          desc.push('每天')
        } else if (day === 'L') {
          desc.push('最后一天')
        } else if (day.includes('/')) {
          const interval = day.split('/')[1]
          desc.push(`每${interval}天`)
        } else if (day.includes(',')) {
          const days = day.split(',').join('、')
          desc.push(`每月${days}号`)
        } else {
          desc.push(`每月${day}号`)
        }
        
        // 解析星期
        if (weekday !== '*' && weekday !== '?') {
          const weekMap = {
            '0': '周日', '1': '周一', '2': '周二', '3': '周三',
            '4': '周四', '5': '周五', '6': '周六', '7': '周日'
          }
          
          if (weekday.includes(',')) {
            const days = weekday.split(',').map(d => weekMap[d] || d).join('、')
            desc.push(`(${days})`)
          } else if (weekday.includes('/')) {
            const interval = weekday.split('/')[1]
            desc.push(`每${interval}周`)
          } else {
            desc.push(`(${weekMap[weekday] || weekday})`)
          }
        }
        
        return desc.join(' ')
      } catch (error) {
        console.error('解析Cron表达式失败:', error)
        return '表达式格式错误'
      }
    },
    
    calculateRecentRunTimes() {
      if (!this.currentExpression) {
        this.recentRunTimes = []
        return
      }
      
      try {
        // 使用Cron表达式计算最近运行时间
        this.recentRunTimes = this.calculateCronRunTimes(this.currentExpression)
        console.log('计算运行时间:', this.currentExpression, this.recentRunTimes)
      } catch (error) {
        console.error('计算运行时间失败:', error)
        this.recentRunTimes = []
      }
    },
    
    calculateCronRunTimes(cronExpression) {
      try {
        const runTimes = []
        const now = new Date()
        
        // 使用cron-parser库解析Cron表达式
        const interval = cronParser.parseExpression(cronExpression, {
          currentDate: now,
          tz: 'Asia/Shanghai'
        })
        
        // 获取未来5次运行时间
        for (let i = 0; i < 5; i++) {
          try {
            const nextRun = interval.next()
            runTimes.push(this.formatDateTime(nextRun.toDate()))
          } catch (error) {
            // 如果没有更多运行时间，跳出循环
            break
          }
        }
        
        if (runTimes.length === 0) {
          return ['没有找到匹配的运行时间']
        }
        
        return runTimes
      } catch (error) {
        console.error('Cron表达式解析失败:', error)
        return ['表达式格式错误']
      }
    },
    
    
    formatDateTime(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      const second = String(date.getSeconds()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    
    // 公开方法：清空配置
    clear() {
      this.currentExpression = ''
      this.selectedPreset = ''
      this.manualExpression = ''
      this.recentRunTimes = []
      this.updateExpression('')
    },
    
    // 公开方法：设置表达式
    setExpression(expression) {
      this.updateExpression(expression)
    },
    
    // 移除月份日期
    removeMonthDay(day) {
      const index = this.visualConfig.monthDays.indexOf(day)
      if (index > -1) {
        this.visualConfig.monthDays.splice(index, 1)
        this.updateVisualExpression()
      }
    },
    
    // 切换月份日期选择
    toggleMonthDay(day) {
      const index = this.visualConfig.monthDays.indexOf(day)
      if (index > -1) {
        this.visualConfig.monthDays.splice(index, 1)
      } else {
        this.visualConfig.monthDays.push(day)
      }
      this.updateVisualExpression()
    },
    
    // 全选月份日期
    selectAllMonthDays() {
      this.visualConfig.monthDays = Array.from({length: 31}, (_, i) => i + 1)
      this.updateVisualExpression()
    },
    
    // 清空月份日期
    clearAllMonthDays() {
      this.visualConfig.monthDays = []
      this.updateVisualExpression()
    },
    
    // 选择工作日（周一到周五）
    selectWeekdays() {
      this.visualConfig.monthDays = Array.from({length: 31}, (_, i) => i + 1).filter(day => {
        // 这里简化处理，实际应该根据具体月份计算
        return day % 7 >= 1 && day % 7 <= 5
      })
      this.updateVisualExpression()
    },
    
    // 选择周末（周六和周日）
    selectWeekends() {
      this.visualConfig.monthDays = Array.from({length: 31}, (_, i) => i + 1).filter(day => {
        // 这里简化处理，实际应该根据具体月份计算
        return day % 7 === 0 || day % 7 === 6
      })
      this.updateVisualExpression()
    }
  }
}
</script>

<style lang="less" scoped>
.cron-selector {
  width: 100%;
  
  .mode-selector {
    width: 100%;
    margin-bottom: 12px;
  }
  
  .config-section {
    width: 100%;
    
    .preset-selector {
      width: 100%;
    }
    
    .manual-input {
      width: 100%;
    }
    
    .helper-links {
      margin-top: 8px;
      text-align: right;
      
      a {
        font-size: 12px;
        color: #1890ff;
        
        &:hover {
          color: #40a9ff;
        }
      }
    }
    
    .visual-button {
      height: 40px;
      font-size: 14px;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
      }
    }
    
    .visual-config {
      .time-config {
        margin-bottom: 16px;
        
        h4 {
          margin-bottom: 12px;
          color: #333;
          font-size: 14px;
          font-weight: 500;
        }
      }
      
      .frequency-config {
        h4 {
          margin-bottom: 12px;
          color: #333;
          font-size: 14px;
          font-weight: 500;
        }
        
        .frequency-detail {
          margin-top: 12px;
          padding: 12px;
          background: #f8f9fa;
          border-radius: 6px;
        }
      }
    }
    
    .visual-editor-wrapper {
      .modal-footer {
      text-align: right;
        padding: 20px 0 0 0;
        border-top: 1px solid #e8e8e8;
        margin-top: 20px;
        
        .cancel-btn {
          margin-right: 12px;
        }
        
        .confirm-btn {
          background: #1890ff;
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
        }
      }
      
      .visual-config {
        .time-config {
          margin-bottom: 32px;
          padding: 20px;
          background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
          border-radius: 12px;
          border: 1px solid #e6f0ff;
          
          .config-header {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            
            .header-icon {
              font-size: 18px;
        color: #1890ff;
              margin-right: 8px;
            }
            
            .header-title {
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
          }
          
          .time-selectors {
            display: flex;
            gap: 20px;
            
            .time-item {
              flex: 1;
              
              .time-label {
                display: block;
                margin-bottom: 8px;
                font-size: 14px;
                font-weight: 500;
                color: #666;
              }
              
              .time-select {
                width: 100%;
              }
            }
          }
        }
        
        .frequency-config {
          margin-bottom: 32px;
          padding: 20px;
          background: linear-gradient(135deg, #fff8f0 0%, #fff2e6 100%);
          border-radius: 12px;
          border: 1px solid #ffe6cc;
          
          .config-header {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            
            .header-icon {
              font-size: 18px;
              color: #fa8c16;
              margin-right: 8px;
            }
            
            .header-title {
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
          }
          
          .frequency-options {
            margin-bottom: 20px;
            
            .frequency-radio-group {
              display: flex;
              gap: 12px;
              
              .frequency-radio {
                flex: 1;
                text-align: center;
                height: 40px;
                line-height: 38px;
                border-radius: 8px;
                border: 2px solid #e8e8e8;
                transition: all 0.3s ease;
        
        &:hover {
                  border-color: #1890ff;
                  color: #1890ff;
                }
                
                &.ant-radio-button-wrapper-checked {
                  background: #1890ff;
                  border-color: #1890ff;
                  color: white;
                  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
                }
              }
            }
          }
          
          .frequency-detail {
            margin-top: 20px;
            padding: 20px;
            background: white;
            border-radius: 8px;
            border: 1px solid #e8e8e8;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
            
            .detail-header {
              display: flex;
              align-items: center;
              margin-bottom: 16px;
              font-size: 14px;
              font-weight: 500;
              color: #333;
              
              .anticon {
                margin-right: 6px;
                color: #1890ff;
              }
            }
            
            .weekday-selector {
              .weekday-group {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 12px;
                
                .weekday-item {
                  .weekday-checkbox {
                    width: 100%;
                    height: 40px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border: 2px solid #e8e8e8;
                    border-radius: 8px;
                    transition: all 0.3s ease;
                    
                    &:hover {
                      border-color: #1890ff;
                      background: #f0f8ff;
                    }
                    
                    &.ant-checkbox-wrapper-checked {
                      background: #1890ff;
                      border-color: #1890ff;
                      color: white;
                      
                      .weekday-text {
                        color: white;
                      }
                    }
                    
                    .weekday-text {
                      font-weight: 500;
                    }
                  }
                }
              }
            }
            
            .monthday-selector {
              .monthday-select {
                width: 100%;
                margin-bottom: 12px;
              }
              
              .monthday-tags {
                display: flex;
                flex-wrap: wrap;
                gap: 8px;
                
                .monthday-tag {
                  background: #e6f7ff;
                  border-color: #91d5ff;
                  color: #1890ff;
                  border-radius: 6px;
                  padding: 4px 8px;
                  font-size: 12px;
                }
              }
            }
            
            .interval-selector {
              display: flex;
              gap: 20px;
              
              .interval-item {
                flex: 1;
                
                .interval-label {
                  display: block;
                  margin-bottom: 8px;
                  font-size: 14px;
                  font-weight: 500;
                  color: #666;
                }
                
                .interval-input,
                .interval-select {
                  width: 100%;
                }
              }
            }
          }
        }
        
        .preview-section {
          margin-top: 32px;
          padding: 20px;
          background: linear-gradient(135deg, #f6ffed 0%, #f0f9e8 100%);
          border-radius: 12px;
          border: 1px solid #d9f7be;
          
          .preview-header {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            
            .preview-icon {
              font-size: 18px;
              color: #52c41a;
              margin-right: 8px;
            }
            
            .preview-title {
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
          }
          
          .preview-content {
            display: flex;
            flex-direction: column;
            gap: 16px;
            
            .preview-item {
              background: white;
              padding: 16px;
              border-radius: 8px;
              border: 1px solid #e8e8e8;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
              
              .preview-label {
                display: flex;
                align-items: center;
                margin-bottom: 8px;
                font-size: 14px;
                font-weight: 500;
                color: #666;
                
                .anticon {
                  margin-right: 6px;
                  color: #1890ff;
                }
              }
              
              .preview-value {
                font-size: 14px;
                color: #333;
                word-break: break-all;
                
                &.description-value {
                  color: #1890ff;
                  font-weight: 500;
                }
                
                &.expression-value {
                  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                  background: #f5f5f5;
                  padding: 8px 12px;
                  border-radius: 4px;
                  border: 1px solid #d9d9d9;
                }
              }
            }
          }
          
          .run-times {
            margin-top: 20px;
            background: white;
            padding: 16px;
            border-radius: 8px;
            border: 1px solid #e8e8e8;
            
            .run-times-header {
              display: flex;
              align-items: center;
              margin-bottom: 12px;
              
              .run-times-icon {
                font-size: 16px;
                color: #fa8c16;
                margin-right: 6px;
              }
              
              .run-times-title {
                font-size: 14px;
                font-weight: 500;
                color: #333;
              }
            }
            
            .run-times-list {
              display: flex;
              flex-direction: column;
              gap: 8px;
              
              .run-time-item {
                display: flex;
                align-items: center;
                padding: 8px 12px;
                background: #fff7e6;
                border: 1px solid #ffd591;
                border-radius: 6px;
                
                .run-time-icon {
                  font-size: 12px;
                  color: #fa8c16;
                  margin-right: 8px;
                }
                
                .run-time-text {
                  font-size: 12px;
                  color: #d46b08;
                  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                }
              }
            }
          }
        }
      }
    }
  }
  
  .preview-section {
    margin-top: 8px;
    
    .preview-content {
      display: flex;
      flex-direction: column;
      gap: 6px;
      
      .preview-item {
        .preview-tag {
          margin: 0;
          padding: 4px 12px;
          font-size: 12px;
          border-radius: 4px;
          display: inline-flex;
          align-items: center;
          gap: 6px;
          max-width: 100%;
          
          i {
            font-size: 12px;
          }
        }
      }
    }
    
    .run-times {
      margin-top: 12px;
      
      .run-times-list {
      display: flex;
        flex-wrap: wrap;
        gap: 6px;
        
        .run-time-tag {
          margin: 0;
          padding: 2px 8px;
          font-size: 11px;
          border-radius: 3px;
        }
      }
    }
  }
}

// 弹窗样式优化
:deep(.visual-modal) {
  .ant-modal-header {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%) !important;
    border-bottom: none !important;
    
    .ant-modal-title {
      color: white !important;
      font-weight: 600 !important;
    }
  }
  
  .ant-modal-close {
    color: white !important;
    
    &:hover {
      color: rgba(255, 255, 255, 0.8) !important;
    }
  }
  
  .ant-modal-body {
    padding: 16px !important;
  }
}
</style>

<style lang="less">
// 全局样式，确保弹窗样式生效
.visual-modal {
  .ant-modal-header {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%) !important;
    border-bottom: none !important; 
    
    .ant-modal-title {
      color: white !important;
      font-weight: 600 !important;
    }
  }
  
  .ant-modal-close {
    color: white !important;
    
    &:hover {
      color: rgba(255, 255, 255, 0.8) !important;
    }
  }
  
  .ant-modal-body {
    padding: 16px !important;
  }
}
</style>
