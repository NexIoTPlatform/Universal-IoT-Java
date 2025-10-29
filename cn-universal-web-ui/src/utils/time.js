// 统一的在线时间格式化与“距今”描述方法
// 依赖现有的 parseTime 工具，保持与全站一致的格式化规则
import {parseTime} from './ruoyi'

/**
 * 将时间戳格式化为标准时间字符串
 * - 支持秒(10位)与毫秒(13位)时间戳，字符串/数字均可
 * - 支持字符串格式时间（如 "2025-10-23 14:27:20"）
 * - 空值或'0'时返回"从未通信"
 */
export function formatOnlineTime(timestamp) {
  if (!timestamp || timestamp === '0') {
    return '从未通信'
  }
  
  // 如果是字符串格式的时间（包含'-'或':'），直接返回
  if (typeof timestamp === 'string' && (timestamp.includes('-') || timestamp.includes(':'))) {
    return timestamp
  }
  
  // 处理数字时间戳
  const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
  const timeMs = time.toString().length === 10 ? time * 1000 : time
  return parseTime(timeMs, '{y}-{m}-{d} {h}:{i}:{s}')
}

/**
 * 输出与当前时间的相对描述
 * - 支持数字时间戳（秒/毫秒）
 * - 支持字符串格式时间（如 "2025-10-23 14:27:20"）
 * - 刚刚在线 / x分钟前 / x小时前 / x天前 / 长时间未通信
 */
export function getTimeAgo(timestamp) {
  if (!timestamp || timestamp === '0') {
    return '从未通信'
  }
  
  let timeMs
  
  // 如果是字符串格式的时间（包含'-'或':'），转换为时间戳
  if (typeof timestamp === 'string' && (timestamp.includes('-') || timestamp.includes(':'))) {
    timeMs = new Date(timestamp).getTime()
  } else {
    // 处理数字时间戳
    const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
    timeMs = time.toString().length === 10 ? time * 1000 : time
  }
  
  const now = Date.now()
  const diff = now - timeMs
  
  if (diff < 60000) {
    return '刚刚在线'
  }
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  if (diff < 2592000000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  return '长时间未通信'
}

export default {
  formatOnlineTime,
  getTimeAgo
}


