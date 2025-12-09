import versionConfig from '@/config/version.json'
import request from '@/utils/request'
import storage from 'store'

class VersionService {
  constructor() {
    this.config = versionConfig
    this.cache = null
    this.lastCheckTime = null
    this.storageKey = 'version_info'
    this.readVersionKey = 'last_read_version'  // 改为单一版本
  }

  /**
   * 获取版本信息
   * @param {boolean} forceRefresh 是否强制刷新
   * @returns {Promise<Object>}
   */
  async getVersionInfo(forceRefresh = false) {
    // 检查本地配置的版本号
    const localVersion = this.config.currentVersion
    
    // 检查缓存中的版本号
    const storedData = storage.get(this.storageKey)
    if (storedData && storedData.currentVersion !== localVersion) {
      // 版本号不匹配，清除缓存
      console.log(`检测到版本更新: ${storedData.currentVersion} -> ${localVersion}`)
      this.clearCache()
      forceRefresh = true
    }
    
    // 如果缓存存在且未过期，直接返回缓存
    if (!forceRefresh && this.cache && this.isCacheValid()) {
      // 再次验证版本号
      if (this.cache.currentVersion === localVersion) {
        return this.cache
      } else {
        console.log('版本号不匹配，强制刷新')
        forceRefresh = true
      }
    }

    // 尝试从本地存储加载
    if (!forceRefresh) {
      if (storedData && this.isCacheValid(storedData.lastCheckTime)) {
        // 验证版本号
        if (storedData.currentVersion === localVersion) {
          this.cache = storedData
          this.lastCheckTime = storedData.lastCheckTime
          return storedData
        }
      }
    }

    try {
      // 尝试从API获取最新版本信息
      const apiData = await this.fetchFromAPI()
      if (apiData) {
        this.cache = apiData
        this.lastCheckTime = Date.now()
        // 保存到本地存储
        this.saveToStorage(apiData)
        return apiData
      }
    } catch (error) {
      console.warn('Failed to fetch version from API, using local config:', error)
    }

    // 如果API失败，使用本地配置
    this.cache = this.config
    this.lastCheckTime = Date.now()
    this.saveToStorage(this.config)
    return this.config
  }

  /**
   * 从API获取版本信息
   * @returns {Promise<Object|null>}
   */
  async fetchFromAPI() {
    if (!this.config.apiEndpoint) {
      return null
    }

    const response = await request({
      url: this.config.apiEndpoint,
      method: 'get',
      timeout: 5000 // 5秒超时
    })

    return response.data
  }

  /**
   * 保存数据到本地存储
   * @param {Object} data 版本数据
   */
  saveToStorage(data) {
    const dataToStore = {
      ...data,
      lastCheckTime: this.lastCheckTime
    }
    storage.set(this.storageKey, dataToStore)
  }

  /**
   * 检查缓存是否有效
   * @param {number} checkTime 检查时间
   * @returns {boolean}
   */
  isCacheValid(checkTime = this.lastCheckTime) {
    if (!checkTime) return false
    const now = Date.now()
    const interval = this.config.checkInterval || 3600000 // 默认1小时
    return (now - checkTime) < interval
  }

  /**
   * 获取新版本数量
   * @returns {number}
   */
  getNewVersionCount() {
    if (!this.cache) return 0
    const lastReadVersion = storage.get(this.readVersionKey)
    // 只要当前版本与最后读过的版本不同，就认为有新版本
    return this.cache.versions.filter(v => v.isNew && v.version !== lastReadVersion).length
  }

  /**
   * 获取当前版本
   * @returns {string}
   */
  getCurrentVersion() {
    return this.cache ? this.cache.currentVersion : this.config.currentVersion
  }

  /**
   * 获取版本列表
   * @returns {Array}
   */
  getVersionList() {
    return this.cache ? this.cache.versions : this.config.versions
  }

  /**
   * 清除所有缓存
   */
  clearCache() {
    this.cache = null
    this.lastCheckTime = null
    storage.remove(this.storageKey)
    console.log('版本缓存已清除')
  }

  /**
   * 标记版本为已读
   * @param {string} version 版本号
   */
  markAsRead(version) {
    if (!this.cache) return
    
    // 直接保存当前版本号
    storage.set(this.readVersionKey, version)
    console.log(`版本 ${version} 已标记为已读`)
    
    // 更新缓存中的版本状态
    const versionItem = this.cache.versions.find(v => v.version === version)
    if (versionItem) {
      versionItem.isNew = false
    }
  }

  /**
   * 检查版本是否已读
   * @param {string} version 版本号
   * @returns {boolean}
   */
  isVersionRead(version) {
    const lastReadVersion = storage.get(this.readVersionKey)
    return lastReadVersion === version
  }

  /**
   * 获取版本类型颜色
   * @param {string} type 版本类型
   * @returns {string}
   */
  getVersionTypeColor(type) {
    const colorMap = {
      'major': 'red',
      'minor': 'blue',
      'patch': 'green',
      'hotfix': 'orange'
    }
    return colorMap[type] || 'default'
  }

  /**
   * 获取版本类型标签
   * @param {string} type 版本类型
   * @param {string} lang 语言
   * @returns {string}
   */
  getVersionTypeLabel(type, lang = 'zh-CN') {
    const labels = {
      'zh-CN': {
        'major': '重大更新',
        'minor': '功能更新',
        'patch': '问题修复',
        'hotfix': '紧急修复'
      },
      'en-US': {
        'major': 'Major Update',
        'minor': 'Feature Update',
        'patch': 'Bug Fix',
        'hotfix': 'Hotfix'
      }
    }
    return labels[lang]?.[type] || type
  }
}

// 创建单例实例
const versionService = new VersionService()

export default versionService
