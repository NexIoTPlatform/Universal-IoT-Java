<template>
  <a-modal
    :maskClosable="false"
    title="物模型详情"
    v-model="open"
    width="1200px"
    :footer="null"
    @cancel="closePanel"
    class="metadata-modal"
  >
    <div class="metadata-viewer-container">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <a-button
            icon="search"
            @click="toggleSearch"
            size="small"
            :type="showSearch ? 'primary' : 'default'"
          >
            搜索
          </a-button>

          <a-button
            icon="copy"
            @click="copyToClipboard"
            size="small"
          >
            复制
          </a-button>

          <a-button
            icon="download"
            @click="downloadJson"
            size="small"
          >
            下载
          </a-button>
        </div>

        <div class="toolbar-right">
          <a-switch
            v-model="darkTheme"
            checked-children="暗色"
            un-checked-children="亮色"
            size="small"
          />
        </div>
      </div>

      <!-- 搜索栏 -->
      <div v-if="showSearch" class="search-bar">
        <a-input
          v-model="searchText"
          placeholder="搜索键名或值..."
          prefix-icon="search"
          @input="handleSearch"
          class="search-input"
        />
        <div class="search-results" v-if="searchResults.length > 0">
          找到 {{ searchResults.length }} 个结果
        </div>
      </div>

      <!-- 内容区域 -->
      <a-spin :spinning="loading">
        <div
          class="content-area"
          :class="{ 'dark-theme': darkTheme, 'light-theme': !darkTheme }"
        >
          <!-- 代码视图 -->
          <div class="code-view">
            <pre class="json-code"><code v-html="highlightedJson"></code></pre>
          </div>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import {getProduct} from '@/api/system/dev/product'

export default {
  name: 'MetadataShowVue',
  props: {
    productId: {
      type: String,
      require: true,
      default: ''
    },
    gid: {
      type: String,
      require: true,
      default: ''
    },
    show: {
      type: Boolean,
      default: false
    },
    // type === 1 IoT物模型；type === 2 的物模型
    type: {
      type: Number,
      default: 1
    }
  },
  watch: {
    show(val) {
      this.open = val
      if (val === true) {
        this.metadata = {}
        this.resetState()
        if (this.type === 1) {
          this.getProductDetail()
        }
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
      metadata: {},
      // 产品详情
      productDetail: undefined,
      // 是否显示搜索
      showSearch: false,
      // 搜索文本
      searchText: '',
      // 搜索结果
      searchResults: [],
      // 暗色主题
      darkTheme: false
    }
  },
  computed: {
    highlightedJson() {
      if (!this.metadata) {
        return ''
      }

      let jsonStr = JSON.stringify(this.metadata, null, 2)

      // 高亮搜索文本
      if (this.searchText) {
        const regex = new RegExp(`(${this.escapeRegex(this.searchText)})`, 'gi')
        jsonStr = jsonStr.replace(regex, '<mark>$1</mark>')
      }

      // 语法高亮
      jsonStr = jsonStr
      .replace(/"([^"]+)":/g, '<span class="json-key">"$1":</span>')
      .replace(/: "([^"]*)"/g, ': <span class="json-string">"$1"</span>')
      .replace(/: (\d+)/g, ': <span class="json-number">$1</span>')
      .replace(/: (true|false)/g, ': <span class="json-boolean">$1</span>')
      .replace(/: null/g, ': <span class="json-null">null</span>')

      return jsonStr
    }
  },
  methods: {
    /** 获取设备产品信息 */
    getProductDetail() {
      this.loading = true
      console.log('this.productId = ', this.productId)
      getProduct(this.productId).then(response => {
        this.productDetail = response.data
        console.log('this.productDetail = ', this.productDetail)
        if (this.productDetail.metadata !== undefined && this.productDetail.metadata !== null) {
          this.metadata = JSON.parse(this.productDetail.metadata)
        }
        console.log('this.productDetail = ', this.productDetail)
      }).finally(() => {
        this.loading = false
      })
    },

    // 重置状态
    resetState() {
      this.searchText = ''
      this.searchResults = []
      this.showSearch = false
    },

    // 切换搜索
    toggleSearch() {
      this.showSearch = !this.showSearch
      if (!this.showSearch) {
        this.searchText = ''
        this.searchResults = []
      }
    },

    // 处理搜索
    handleSearch() {
      if (!this.searchText) {
        this.searchResults = []
        return
      }

      this.searchResults = this.searchInObject(this.metadata, [], this.searchText.toLowerCase())
    },

    // 在对象中搜索
    searchInObject(obj, path, searchText) {
      let results = []

      if (typeof obj === 'object' && obj !== null) {
        Object.entries(obj).forEach(([key, value]) => {
          const currentPath = [...path, key]

          // 搜索键名
          if (key.toLowerCase().includes(searchText)) {
            results.push({path: currentPath, type: 'key', value: key})
          }

          // 搜索值
          if (typeof value !== 'object') {
            if (String(value).toLowerCase().includes(searchText)) {
              results.push({path: currentPath, type: 'value', value})
            }
          } else {
            results = results.concat(this.searchInObject(value, currentPath, searchText))
          }
        })
      }

      return results
    },

    // 复制到剪贴板
    copyToClipboard() {
      const jsonStr = JSON.stringify(this.metadata, null, 2)
      if (navigator.clipboard) {
        navigator.clipboard.writeText(jsonStr).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopyTextToClipboard(jsonStr)
        })
      } else {
        this.fallbackCopyTextToClipboard(jsonStr)
      }
    },

    // 兜底复制方法
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textArea)
    },

    // 下载JSON文件
    downloadJson() {
      const jsonStr = JSON.stringify(this.metadata, null, 2)
      const blob = new Blob([jsonStr], {type: 'application/json'})
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `metadata_${this.productId}_${Date.now()}.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      this.$message.success('下载成功')
    },

    // 转义正则表达式特殊字符
    escapeRegex(string) {
      return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    },

    // 关闭弹出层
    closePanel() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped lang="less">
.metadata-modal {
  :deep(.ant-modal-body) {
    padding: 0;
  }
}

.metadata-viewer-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  background: #ffffff;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
  }
}

/* 搜索栏 */
.search-bar {
  padding: 12px 16px;
  background: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;

  .search-input {
    margin-bottom: 8px;
  }

  .search-results {
    font-size: 12px;
    color: #64748b;
  }
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow: auto;
  padding: 16px;

  &.light-theme {
    background: #ffffff;
    color: #1a202c;
  }

  &.dark-theme {
    background: #1a202c;
    color: #e2e8f0;
  }
}

/* 代码视图 */
.code-view {
  .json-code {
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 13px;
    line-height: 1.5;
    margin: 0;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;
    overflow: auto;

    .json-key {
      color: #7c3aed;
      font-weight: 500;
    }

    .json-string {
      color: #059669;
    }

    .json-number {
      color: #dc2626;
    }

    .json-boolean {
      color: #2563eb;
    }

    .json-null {
      color: #6b7280;
      font-style: italic;
    }

    mark {
      background: rgba(251, 191, 36, 0.5);
      padding: 0 2px;
      border-radius: 2px;
    }
  }
}

/* 暗色主题 */
.dark-theme {
  .json-code {
    background: #111827;
    color: #e5e7eb;

    .json-key {
      color: #a78bfa;
    }

    .json-string {
      color: #34d399;
    }

    .json-number {
      color: #f87171;
    }

    .json-boolean {
      color: #60a5fa;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .metadata-viewer-container {
    height: 60vh;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .toolbar-left {
      justify-content: center;
      flex-wrap: wrap;
    }
  }

  .content-area {
    padding: 12px;
  }
}
</style>
