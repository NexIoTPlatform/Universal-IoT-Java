import {Message} from 'ant-design-vue'

export default {
  install: (Vue) => {
    Vue.directive('copy', {
      bind: (el, binding) => {
        let text = ''
        if (binding.value) {
          text = binding.value
        } else {
          text = el.innerText
        }
        el.addEventListener('click', () => {
          if (navigator.clipboard) {
            navigator.clipboard.writeText(text).then(() => {
              Message.success('已复制到剪切板')
            })
          } else {
            const textArea = document.createElement('textarea')
            textArea.value = text
            textArea.style.position = 'fixed'
            textArea.style.opacity = 0
            document.body.appendChild(textArea)
            textArea.focus()
            textArea.select()
            if (document.execCommand('copy')) {
              document.execCommand('copy')
              Message.success('已复制到剪切板')
            }
            textArea.remove()
          }
        })
      }
    })
  }
}
