/**
 * Modal拖动指令
 * 使用方式: v-modal-drag
 * 功能：允许通过拖动Modal标题栏来移动整个Modal
 */

// 初始化拖拽功能的函数
function initDrag(el) {
    // 如果已经初始化过，先清理
    if (el.__modalDragInitialized) {
      if (el.__modalDragCleanup__) {
        el.__modalDragCleanup__()
      }
    }

    // 获取modal的DOM元素
    const modalWrap = el.querySelector('.ant-modal-wrap')
    if (!modalWrap) {
      // 如果找不到，可能是还没渲染，延迟重试
      setTimeout(() => this.initDrag(el), 100)
      return
    }

    const modal = modalWrap.querySelector('.ant-modal')
    if (!modal) {
      setTimeout(() => this.initDrag(el), 100)
      return
    }

    const header = modal.querySelector('.ant-modal-header')
    if (!header) {
      setTimeout(() => this.initDrag(el), 100)
      return
    }

    // 如果已经绑定过事件，不再重复绑定
    if (header.__modalDragBound) {
      return
    }

    // 设置标题栏样式，表明可以拖动
    header.style.cursor = 'move'
    header.style.userSelect = 'none'

    // 拖动逻辑
    let isDragging = false
    let startX = 0
    let startY = 0
    let initialLeft = 0
    let initialTop = 0

    // 鼠标按下
    const handleMouseDown = (e) => {
      // 只在点击标题栏时触发
      if (e.target.closest('.ant-modal-close')) {
        return // 不拖动关闭按钮
      }

      // 如果点击的是标题栏内的其他可交互元素（如按钮），也不拖动
      if (e.target.closest('button') || e.target.closest('a') || e.target.closest('input')) {
        return
      }

      isDragging = true
      startX = e.clientX
      startY = e.clientY

      // 获取Modal当前位置
      const rect = modal.getBoundingClientRect()
      initialLeft = rect.left
      initialTop = rect.top

      // 设置Modal为绝对定位
      modal.style.position = 'absolute'
      modal.style.left = initialLeft + 'px'
      modal.style.top = initialTop + 'px'
      modal.style.margin = '0'

      // 阻止事件冒泡，避免触发其他元素的拖拽
      e.stopPropagation()

      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }

    // 鼠标移动
    const handleMouseMove = (e) => {
      if (!isDragging) return

      const deltaX = e.clientX - startX
      const deltaY = e.clientY - startY

      let newLeft = initialLeft + deltaX
      let newTop = initialTop + deltaY

      // 边界限制（防止拖出视口）
      const modalWidth = modal.offsetWidth
      const modalHeight = modal.offsetHeight
      const viewportWidth = window.innerWidth
      const viewportHeight = window.innerHeight

      // 左边界
      if (newLeft < 0) newLeft = 0
      // 右边界
      if (newLeft + modalWidth > viewportWidth) {
        newLeft = viewportWidth - modalWidth
      }
      // 上边界
      if (newTop < 0) newTop = 0
      // 下边界
      if (newTop + modalHeight > viewportHeight) {
        newTop = viewportHeight - modalHeight
      }

      modal.style.left = newLeft + 'px'
      modal.style.top = newTop + 'px'
    }

    // 鼠标释放
    const handleMouseUp = () => {
      isDragging = false
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }

    header.addEventListener('mousedown', handleMouseDown)
    header.__modalDragBound = true

    // 保存清理函数
    el.__modalDragCleanup__ = () => {
      header.removeEventListener('mousedown', handleMouseDown)
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
      header.__modalDragBound = false
    }

    el.__modalDragInitialized = true
}

export default {
  bind(el, binding, vnode) {
    // 使用 nextTick 确保DOM已经渲染完成
    if (vnode.context) {
      vnode.context.$nextTick(() => {
        initDrag(el)
      })
    } else {
      // 如果没有 context，延迟执行
      setTimeout(() => {
        initDrag(el)
      }, 100)
    }
  },

  update(el, binding, vnode) {
    // 当组件更新时，如果还没有初始化，重新初始化（处理 destroyOnClose 的情况）
    if (!el.__modalDragInitialized) {
      if (vnode.context) {
        vnode.context.$nextTick(() => {
          initDrag(el)
        })
      } else {
        setTimeout(() => {
          initDrag(el)
        }, 100)
      }
    }
  },

  unbind(el) {
    if (el.__modalDragCleanup__) {
      el.__modalDragCleanup__()
      delete el.__modalDragCleanup__
      delete el.__modalDragInitialized
    }
  }
}
