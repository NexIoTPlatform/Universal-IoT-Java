/**
 * 全局水印工具
 * 支持配置化水印内容
 */

class Watermark {
  constructor(options = {}) {
    this.options = {
      // 水印文本数组
      text: options.text || [],
      // 字体大小
      fontSize: options.fontSize || 14,
      // 字体颜色
      fontColor: options.fontColor || 'rgba(0, 0, 0, 0.15)',
      // 字体样式
      fontFamily: options.fontFamily || 'Arial, sans-serif',
      // 水印间距
      gap: options.gap || [100, 100],
      // 水印旋转角度
      rotate: options.rotate || -22,
      // z-index
      zIndex: options.zIndex || 9999,
      // 是否启用
      enabled: options.enabled !== false,
      ...options
    }
    this.watermarkDiv = null
    this.observer = null
    this.init()
  }

  /**
   * 初始化水印
   */
  init() {
    if (!this.options.enabled || !this.options.text || this.options.text.length === 0) {
      return
    }

    this.createWatermark()
    this.observeWatermark()
  }

  /**
   * 创建水印
   */
  createWatermark() {
    // 移除旧的水印
    this.remove()

    // 创建水印容器
    this.watermarkDiv = document.createElement('div')
    this.watermarkDiv.id = 'watermark-container'
    this.watermarkDiv.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: ${this.options.zIndex};
      overflow: hidden;
    `

    // 创建 canvas 绘制水印
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    
    // 设置字体样式（先设置字体才能测量文本宽度）
    ctx.font = `${this.options.fontSize}px ${this.options.fontFamily}`
    
    // 计算所有文本的最大宽度，确保长文本能完整显示
    let maxTextWidth = 0
    this.options.text.forEach((text) => {
      if (text) {
        const textWidth = ctx.measureText(text).width
        maxTextWidth = Math.max(maxTextWidth, textWidth)
      }
    })
    
    // 设置 canvas 尺寸（根据间距和文本宽度动态调整）
    const [gapX, gapY] = this.options.gap
    const lineHeight = this.options.fontSize + 8
    const textLines = this.options.text.filter(t => t).length
    
    // canvas 宽度：取文本宽度（加边距）和间距的较小值，但至少能容纳文本
    const minCanvasWidth = maxTextWidth + 40 // 左右各留20px边距
    canvas.width = Math.max(minCanvasWidth, Math.min(gapX * 0.9, 500))
    
    // canvas 高度：根据行数和间距计算
    const minCanvasHeight = textLines * lineHeight + 20 // 上下各留10px边距
    canvas.height = Math.max(minCanvasHeight, Math.min(gapY * 0.9, 400))

    // 重新设置字体样式（canvas尺寸改变后需要重新设置）
    ctx.font = `${this.options.fontSize}px ${this.options.fontFamily}`
    ctx.fillStyle = this.options.fontColor
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    
    // 移动到画布中心
    ctx.save()
    ctx.translate(canvas.width / 2, canvas.height / 2)
    ctx.rotate((this.options.rotate * Math.PI) / 180)

    // 绘制文本（多行）
    const startY = -((textLines - 1) * lineHeight) / 2
    
    this.options.text.forEach((text, index) => {
      if (text) {
        ctx.fillText(text, 0, startY + index * lineHeight)
      }
    })
    
    ctx.restore()

    // 将 canvas 转为 base64 图片
    const base64Url = canvas.toDataURL()
    
    // 创建水印背景
    this.watermarkDiv.style.backgroundImage = `url(${base64Url})`
    this.watermarkDiv.style.backgroundRepeat = 'repeat'
    this.watermarkDiv.style.backgroundPosition = '0 0'
    this.watermarkDiv.style.backgroundSize = `${gapX}px ${gapY}px`

    // 添加到页面
    document.body.appendChild(this.watermarkDiv)
  }

  /**
   * 监听水印 DOM 变化，防止被删除
   */
  observeWatermark() {
    if (!this.watermarkDiv || !window.MutationObserver) {
      return
    }

    // 如果已有观察者，先断开
    if (this.observer) {
      this.observer.disconnect()
    }

    this.observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.removedNodes.forEach((node) => {
          if (node === this.watermarkDiv || (node.nodeType === 1 && node.contains && node.contains(this.watermarkDiv))) {
            // 水印被删除，重新创建
            this.createWatermark()
          }
        })
      })
    })

    // 监听 body 的变化
    this.observer.observe(document.body, {
      childList: true,
      subtree: true
    })
  }

  /**
   * 更新水印内容
   */
  update(options) {
    this.options = {
      ...this.options,
      ...options
    }
    this.init()
  }

  /**
   * 移除水印
   */
  remove() {
    if (this.watermarkDiv && this.watermarkDiv.parentNode) {
      this.watermarkDiv.parentNode.removeChild(this.watermarkDiv)
      this.watermarkDiv = null
    }
    if (this.observer) {
      this.observer.disconnect()
      this.observer = null
    }
  }

  /**
   * 销毁水印
   */
  destroy() {
    this.remove()
  }
}

// 单例模式
let watermarkInstance = null

/**
 * 创建或获取水印实例
 */
export function createWatermark(options) {
  if (watermarkInstance) {
    watermarkInstance.update(options)
  } else {
    watermarkInstance = new Watermark(options)
  }
  return watermarkInstance
}

/**
 * 移除水印
 */
export function removeWatermark() {
  if (watermarkInstance) {
    watermarkInstance.remove()
  }
}

/**
 * 更新水印
 */
export function updateWatermark(options) {
  if (watermarkInstance) {
    watermarkInstance.update(options)
  } else {
    createWatermark(options)
  }
}

export default Watermark

