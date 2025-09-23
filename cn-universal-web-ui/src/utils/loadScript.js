export function loadExternalScript(src) {
  return new Promise((resolve, reject) => {
    if (!src) {
      reject(new Error('src is required'))
      return
    }
    const scripts = document.getElementsByTagName('script')
    const existing = Array.from(scripts).find(s => s.src === src)
    if (existing) {
      if (existing.getAttribute('data-loaded') === 'true') {
        resolve()
        return
      }
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', () => reject(new Error('Failed to load script: ' + src)))
      return
    }
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = src
    script.async = true
    script.onload = () => {
      script.setAttribute('data-loaded', 'true')
      resolve()
    }
    script.onerror = () => reject(new Error('Failed to load script: ' + src))
    document.body.appendChild(script)
  })
}
