// 可拖拽元素指令
export default {
  bind(el, binding) {
    const dragHandle = el.querySelector('.drag-handle') || el
    
    dragHandle.style.cursor = 'move'
    dragHandle.style.userSelect = 'none'

    let isDragging = false
    let startX = 0
    let startY = 0
    let initialLeft = 0
    let initialTop = 0

    const handleMouseDown = (e) => {
      // 如果点击的是关闭按钮，不触发拖拽
      if (e.target.closest('.anticon-close')) {
        return
      }

      isDragging = true
      startX = e.clientX
      startY = e.clientY

      const rect = el.getBoundingClientRect()
      initialLeft = rect.left
      initialTop = rect.top

      // 确保元素是绝对定位
      if (window.getComputedStyle(el).position !== 'fixed') {
        el.style.position = 'fixed'
      }
      el.style.left = initialLeft + 'px'
      el.style.top = initialTop + 'px'
      el.style.right = 'auto'
      el.style.bottom = 'auto'

      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
      
      // 防止文本选择
      e.preventDefault()
    }

    const handleMouseMove = (e) => {
      if (!isDragging) return

      const deltaX = e.clientX - startX
      const deltaY = e.clientY - startY

      let newLeft = initialLeft + deltaX
      let newTop = initialTop + deltaY

      // 可选：限制在视口内（可以注释掉以允许拖出屏幕）
      // const elWidth = el.offsetWidth
      // const elHeight = el.offsetHeight
      // const viewportWidth = window.innerWidth
      // const viewportHeight = window.innerHeight

      // if (newLeft < 0) newLeft = 0
      // if (newLeft + elWidth > viewportWidth) newLeft = viewportWidth - elWidth
      // if (newTop < 0) newTop = 0
      // if (newTop + elHeight > viewportHeight) newTop = viewportHeight - elHeight

      el.style.left = newLeft + 'px'
      el.style.top = newTop + 'px'
    }

    const handleMouseUp = () => {
      isDragging = false
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }

    dragHandle.addEventListener('mousedown', handleMouseDown)

    // 保存清理函数
    el.__draggableCleanup__ = () => {
      dragHandle.removeEventListener('mousedown', handleMouseDown)
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  },

  unbind(el) {
    if (el.__draggableCleanup__) {
      el.__draggableCleanup__()
      delete el.__draggableCleanup__
    }
  }
}
