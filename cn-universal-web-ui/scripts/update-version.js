#!/usr/bin/env node

/**
 * 版本更新脚本
 * 用于更新 version.json 配置文件
 * 
 * 使用方法：
 * node scripts/update-version.js --version=1.2.1 --type=patch --title="问题修复"
 */

const fs = require('fs')
const path = require('path')
const { program } = require('commander')

// 解析命令行参数
program
  .option('-v, --version <version>', '版本号')
  .option('-t, --type <type>', '版本类型 (major|minor|patch|hotfix)')
  .option('--title <title>', '版本标题')
  .option('--description <description>', '版本描述')
  .option('--features <features>', '新功能列表，用逗号分隔')
  .option('--changelog <changelog>', '更新日志链接')
  .option('--new', '标记为新版本')
  .parse()

const options = program.opts()

// 版本配置文件路径
const versionConfigPath = path.join(__dirname, '../src/config/version.json')

// 读取当前配置
let config
try {
  const configContent = fs.readFileSync(versionConfigPath, 'utf8')
  config = JSON.parse(configContent)
} catch (error) {
  console.error('读取版本配置文件失败:', error.message)
  process.exit(1)
}

// 验证必需参数
if (!options.version) {
  console.error('错误: 必须指定版本号')
  process.exit(1)
}

if (!options.type || !['major', 'minor', 'patch', 'hotfix'].includes(options.type)) {
  console.error('错误: 必须指定有效的版本类型 (major|minor|patch|hotfix)')
  process.exit(1)
}

// 创建新版本对象
const newVersion = {
  version: options.version,
  type: options.type,
  date: new Date().toISOString().split('T')[0], // YYYY-MM-DD 格式
  title: options.title || getDefaultTitle(options.type),
  description: options.description || getDefaultDescription(options.type),
  features: options.features ? options.features.split(',').map(f => f.trim()) : [],
  changelog: options.changelog || `https://gitee.com/NexIoT/Universal-IoT-Java/releases/tag/v${options.version}`,
  isNew: options.new || false
}

// 获取默认标题
function getDefaultTitle(type) {
  const titles = {
    major: '重大功能更新',
    minor: '功能更新',
    patch: '问题修复',
    hotfix: '紧急修复'
  }
  return titles[type]
}

// 获取默认描述
function getDefaultDescription(type) {
  const descriptions = {
    major: '重大版本更新，包含重要新功能和改进',
    minor: '新增功能，优化用户体验',
    patch: '修复已知问题，提升系统稳定性',
    hotfix: '紧急修复关键问题'
  }
  return descriptions[type]
}

// 将新版本添加到配置中（添加到开头）
config.versions.unshift(newVersion)

// 更新当前版本
config.currentVersion = options.version

// 保存配置
try {
  fs.writeFileSync(versionConfigPath, JSON.stringify(config, null, 2))
  console.log('✅ 版本配置更新成功!')
  console.log(`📦 版本: ${newVersion.version}`)
  console.log(`🏷️  类型: ${newVersion.type}`)
  console.log(`📅 日期: ${newVersion.date}`)
  console.log(`📝 标题: ${newVersion.title}`)
  console.log(`🔗 更新日志: ${newVersion.changelog}`)
} catch (error) {
  console.error('保存版本配置文件失败:', error.message)
  process.exit(1)
}

// 提示更新 CHANGELOG.md
console.log('\n💡 提示: 请记得更新 CHANGELOG.md 文件以保持文档同步')
