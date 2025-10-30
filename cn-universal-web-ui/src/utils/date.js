// 通用时间解析工具：支持秒/毫秒时间戳与 ISO 字符串
export function toDate(value) {
  if (value == null) {
    return new Date(NaN)
  }
  if (typeof value === 'string') {
    const trimmed = value.trim()
    // 纯数字字符串按时间戳处理（兼容秒/毫秒）
    if (/^\d+(\.\d+)?$/.test(trimmed)) {
      const asNumber = Number(trimmed)
      if (!isFinite(asNumber) || asNumber <= 0) {
        return new Date(NaN)
      }
      const msFromNum = asNumber < 1e12 ? asNumber * 1000 : asNumber
      return new Date(msFromNum)
    }
    const parsed = Date.parse(value)
    if (!Number.isNaN(parsed)) {
      return new Date(parsed)
    }
    return new Date(NaN)
  }
  const num = Number(value)
  if (!isFinite(num) || num <= 0) {
    return new Date(NaN)
  }
  const ms = num < 1e12 ? num * 1000 : num
  return new Date(ms)
}

// 将任意时间值格式化为字符串（非法/空返回“无”）
export function formatTimeValue(value, formatter) {
  if (!value) {
    return '无'
  }
  const d = toDate(value)
  if (isNaN(d.getTime())) {
    return '无'
  }
  return (formatter || defaultFormatDate)(d)
}

// 默认格式化：YYYY-MM-DD HH:mm:ss
export function defaultFormatDate(time) {
  const year = time.getFullYear()
  const month = time.getMonth() + 1
  const date = time.getDate()
  const hours = time.getHours()
  const minutes = time.getMinutes()
  const seconds = time.getSeconds()
  const pad = (n) => (n > 9 ? n : '0' + n)
  return `${year}-${pad(month)}-${pad(date)} ${pad(hours)}:${pad(
    minutes)}:${pad(seconds)}`
}


