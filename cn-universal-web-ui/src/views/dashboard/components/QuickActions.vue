<template>
  <div class="quick-actions">
    <TitleBox title="快速操作" class="actions-card">
      <div class="actions-grid">
        <!-- 设备管理 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="deployment-unit"/>
            设备管理
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('device', 'create')">
              <div class="action-icon primary">
                <a-icon type="plus"/>
              </div>
              <div class="action-content">
                <div class="action-title">添加设备</div>
                <div class="action-desc">快速添加新设备</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('device', 'list')">
              <div class="action-icon success">
                <a-icon type="table"/>
              </div>
              <div class="action-content">
                <div class="action-title">设备列表</div>
                <div class="action-desc">查看所有设备</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('device', 'monitor')">
              <div class="action-icon warning">
                <a-icon type="monitor"/>
              </div>
              <div class="action-content">
                <div class="action-title">设备监控</div>
                <div class="action-desc">实时监控状态</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 产品管理 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="gold"/>
            产品管理
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('product', 'create')">
              <div class="action-icon primary">
                <a-icon type="plus-square"/>
              </div>
              <div class="action-content">
                <div class="action-title">创建产品</div>
                <div class="action-desc">定义设备类型</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('product', 'list')">
              <div class="action-icon success">
                <a-icon type="appstore"/>
              </div>
              <div class="action-content">
                <div class="action-title">产品列表</div>
                <div class="action-desc">管理产品类型</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('product', 'template')">
              <div class="action-icon info">
                <a-icon type="file-text"/>
              </div>
              <div class="action-content">
                <div class="action-title">产品模板</div>
                <div class="action-desc">使用模板创建</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 应用管理 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="appstore"/>
            应用管理
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('app', 'create')">
              <div class="action-icon primary">
                <a-icon type="plus-circle"/>
              </div>
              <div class="action-content">
                <div class="action-title">创建应用</div>
                <div class="action-desc">新建IoT应用</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('app', 'list')">
              <div class="action-icon success">
                <a-icon type="bars"/>
              </div>
              <div class="action-content">
                <div class="action-title">应用列表</div>
                <div class="action-desc">管理所有应用</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('app', 'config')">
              <div class="action-icon warning">
                <a-icon type="setting"/>
              </div>
              <div class="action-content">
                <div class="action-title">应用配置</div>
                <div class="action-desc">配置应用参数</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 规则引擎 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="branches"/>
            规则引擎
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('rule', 'create')">
              <div class="action-icon primary">
                <a-icon type="plus"/>
              </div>
              <div class="action-content">
                <div class="action-title">创建规则</div>
                <div class="action-desc">新建数据处理规则</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('rule', 'list')">
              <div class="action-icon success">
                <a-icon type="ordered-list"/>
              </div>
              <div class="action-content">
                <div class="action-title">规则列表</div>
                <div class="action-desc">管理所有规则</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('rule', 'flow')">
              <div class="action-icon info">
                <a-icon type="share-alt"/>
              </div>
              <div class="action-content">
                <div class="action-title">规则流程</div>
                <div class="action-desc">可视化规则设计</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统管理 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="setting"/>
            系统管理
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('system', 'user')">
              <div class="action-icon primary">
                <a-icon type="user"/>
              </div>
              <div class="action-content">
                <div class="action-title">用户管理</div>
                <div class="action-desc">管理系统用户</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('system', 'role')">
              <div class="action-icon success">
                <a-icon type="team"/>
              </div>
              <div class="action-content">
                <div class="action-title">角色管理</div>
                <div class="action-desc">配置用户角色</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('system', 'config')">
              <div class="action-icon warning">
                <a-icon type="tool"/>
              </div>
              <div class="action-content">
                <div class="action-title">系统配置</div>
                <div class="action-desc">系统参数设置</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据分析 -->
        <div class="action-group">
          <h4 class="group-title">
            <a-icon type="bar-chart"/>
            数据分析
          </h4>
          <div class="action-items">
            <div class="action-item" @click="handleAction('analysis', 'realtime')">
              <div class="action-icon primary">
                <a-icon type="line-chart"/>
              </div>
              <div class="action-content">
                <div class="action-title">实时数据</div>
                <div class="action-desc">查看实时数据流</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('analysis', 'report')">
              <div class="action-icon success">
                <a-icon type="file-text"/>
              </div>
              <div class="action-content">
                <div class="action-title">数据报表</div>
                <div class="action-desc">生成分析报表</div>
              </div>
            </div>
            <div class="action-item" @click="handleAction('analysis', 'export')">
              <div class="action-icon info">
                <a-icon type="download"/>
              </div>
              <div class="action-content">
                <div class="action-title">数据导出</div>
                <div class="action-desc">导出历史数据</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </TitleBox>
  </div>
</template>

<script>
import TitleBox from './TitleBox.vue'

export default {
  name: 'QuickActions',
  components: {
    TitleBox
  },
  methods: {
    handleAction(category, action) {
      const routes = {
        device: {
          create: '/system/instance/create',
          list: '/system/instance',
          monitor: '/system/instance/monitor'
        },
        product: {
          create: '/product/list/create',
          list: '/product/list',
          template: '/product/template'
        },
        app: {
          create: '/app/application/create',
          list: '/app/application',
          config: '/app/application/config'
        },
        rule: {
          create: '/rule/flow/create',
          list: '/rule/flow',
          flow: '/rule/flow/design'
        },
        system: {
          user: '/system/user',
          role: '/system/role',
          config: '/system/config'
        },
        analysis: {
          realtime: '/analysis/realtime',
          report: '/analysis/report',
          export: '/analysis/export'
        }
      }

      const route = routes[category]?.[action]
      if (route) {
        this.$router.push(route)
      } else {
        this.$message.warning('功能开发中，敬请期待')
      }
    }
  }
}
</script>

<style lang="less" scoped>
.quick-actions {
  .actions-card {
    /deep/ .content {
      padding: 20px;
    }
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;

    .action-group {
      .group-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #f0f0f0;

        .anticon {
          margin-right: 8px;
          color: #1890ff;
        }
      }

      .action-items {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
        gap: 12px;

        .action-item {
          display: flex;
          align-items: center;
          padding: 12px;
          background: #fafafa;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid transparent;

          &:hover {
            background: white;
            border-color: #d9d9d9;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }

          .action-icon {
            width: 32px;
            height: 32px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            flex-shrink: 0;

            .anticon {
              color: white;
              font-size: 14px;
            }

            &.primary {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }

            &.success {
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            }

            &.warning {
              background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
            }

            &.info {
              background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
            }
          }

          .action-content {
            flex: 1;
            min-width: 0;

            .action-title {
              font-size: 13px;
              font-weight: 600;
              color: #333;
              margin-bottom: 2px;
              line-height: 1.2;
            }

            .action-desc {
              font-size: 11px;
              color: #999;
              line-height: 1.2;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .quick-actions {
    .actions-grid {
      grid-template-columns: 1fr;
      gap: 16px;

      .action-group {
        .action-items {
          grid-template-columns: 1fr;
          gap: 8px;
        }
      }
    }
  }
}
</style> 