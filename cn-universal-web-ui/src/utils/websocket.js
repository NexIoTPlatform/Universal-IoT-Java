/**
 * WebSocket 服务工具
 * 用于实时消息推送和灵动岛消息通知
 */

class WebSocketService {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.maxReconnectAttempts = 5
    this.reconnectAttempts = 0
    this.reconnectInterval = 3000
    this.heartbeatInterval = 30000
    this.isConnected = false
    this.messageHandlers = []
    this.config = {
      url: process.env.VUE_APP_WS_URL || 'ws://localhost:8080/ws',
      // 如果需要认证，可以添加token
      protocols: [],
    }
  }

  /**
   * 连接WebSocket
   * @param {string} url WebSocket地址
   * @param {Object} options 连接选项
   */
  connect(url = null, options = {}) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接')
      return
    }

    const wsUrl = url || this.config.url
    console.log('正在连接WebSocket:', wsUrl)

    try {
      this.ws = new WebSocket(wsUrl, this.config.protocols)
      this.setupEventHandlers()
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.handleReconnect()
    }
  }

  /**
   * 设置事件处理器
   */
  setupEventHandlers() {
    this.ws.onopen = (event) => {
      console.log('WebSocket连接成功')
      this.isConnected = true
      this.reconnectAttempts = 0
      this.startHeartbeat()

      // 发送认证信息（如果需要）
      this.authenticate()
    }

    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        this.handleMessage(message)
      } catch (error) {
        console.error('WebSocket消息解析失败:', error)
      }
    }

    this.ws.onclose = (event) => {
      console.log('WebSocket连接关闭:', event.code, event.reason)
      this.isConnected = false
      this.stopHeartbeat()

      // 非正常关闭时尝试重连
      if (event.code !== 1000) {
        this.handleReconnect()
      }
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
      this.isConnected = false
    }
  }

  /**
   * 处理收到的消息
   * @param {Object} message 消息对象
   */
  handleMessage(message) {
    console.log('收到WebSocket消息:', message)

    // 如果是心跳响应，直接返回
    if (message.type === 'heartbeat_response') {
      return
    }

    // 通知所有消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(message)
      } catch (error) {
        console.error('消息处理器执行失败:', error)
      }
    })
  }

  /**
   * 发送认证信息
   */
  authenticate() {
    const token = localStorage.getItem('Access-Token')
      || sessionStorage.getItem('Access-Token')
    if (token) {
      this.send({
        type: 'auth',
        token: token,
        timestamp: Date.now()
      })
    }
  }

  /**
   * 发送消息
   * @param {Object} message 消息对象
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message))
        return true
      } catch (error) {
        console.error('WebSocket发送消息失败:', error)
        return false
      }
    } else {
      console.warn('WebSocket未连接，无法发送消息')
      return false
    }
  }

  /**
   * 添加消息处理器
   * @param {Function} handler 消息处理函数
   */
  addMessageHandler(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler)
    }
  }

  /**
   * 移除消息处理器
   * @param {Function} handler 消息处理函数
   */
  removeMessageHandler(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  /**
   * 开始心跳检测
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send({
          type: 'heartbeat',
          timestamp: Date.now()
        })
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 处理重连
   */
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数已达上限，停止重连')
      return
    }

    this.reconnectAttempts++
    console.log(
      `WebSocket重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 断开连接
   */
  disconnect() {
    console.log('手动断开WebSocket连接')

    // 清理定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    this.stopHeartbeat()

    // 关闭连接
    if (this.ws) {
      this.ws.close(1000, '手动断开')
      this.ws = null
    }

    this.isConnected = false
    this.reconnectAttempts = 0
  }

  /**
   * 获取连接状态
   */
  getConnectionState() {
    if (!this.ws) {
      return 'CLOSED'
    }

    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return 'CONNECTING'
      case WebSocket.OPEN:
        return 'OPEN'
      case WebSocket.CLOSING:
        return 'CLOSING'
      case WebSocket.CLOSED:
        return 'CLOSED'
      default:
        return 'UNKNOWN'
    }
  }

  /**
   * 模拟消息推送（用于开发测试）
   */
  simulateMessages() {
    const mockMessages = [
      {
        type: 'device',
        title: '设备告警',
        content: '温度传感器TH-001离线',
        priority: 'high',
        deviceId: 'TH-001',
        location: '机房A-01'
      },
      {
        type: 'info',
        title: '系统通知',
        content: '数据备份已完成',
        priority: 'normal'
      },
      {
        type: 'success',
        title: '设备上线',
        content: '智能水表WM-003已连接',
        priority: 'normal',
        deviceId: 'WM-003'
      },
      {
        type: 'warning',
        title: '存储空间警告',
        content: '系统存储空间使用率达到85%',
        priority: 'high'
      },
      {
        type: 'error',
        title: '网络异常',
        content: '无法连接到数据中心',
        priority: 'high'
      }
    ]

    let index = 0
    const interval = setInterval(() => {
      if (index >= mockMessages.length) {
        clearInterval(interval)
        return
      }

      const message = {
        ...mockMessages[index],
        id: Date.now() + Math.random(),
        timestamp: new Date().toISOString()
      }

      // 直接调用消息处理器
      this.handleMessage(message)
      index++
    }, 5000)

    return interval
  }
}

// 创建全局WebSocket服务实例
const wsService = new WebSocketService()

export default wsService

// 导出类，允许创建多个实例
export {WebSocketService}