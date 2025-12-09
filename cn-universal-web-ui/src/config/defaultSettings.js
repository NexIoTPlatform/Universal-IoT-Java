/**
 * 项目默认配置项
 * primaryColor - 默认主题色, 如果修改颜色不生效，请清理 localStorage
 * navTheme - sidebar theme ['dark', 'light'] 两种主题
 * colorWeak - 色盲模式
 * layout - 整体布局方式 ['sidemenu', 'topmenu'] 两种布局
 * fixedHeader - 固定 Header : boolean
 * fixSiderbar - 固定左侧菜单栏 ： boolean
 * contentWidth - 内容区布局： 流式 |  固定
 *
 * storageOptions: {} - Vue-ls 插件配置项 (localStorage/sessionStorage)
 *
 */

export default {
  navTheme: 'light', // theme for nav menu
  // primaryColor: '#1890ff', // primary color of ant design
  // layout: 'topmenu', // nav menu position: `sidemenu` or `topmenu`
  // contentWidth: 'Fluid', // layout of content: `Fluid` or `Fixed`, only works when layout is topmenu
  // fixedHeader: true, // sticky header
  // fixSiderbar: true, // sticky siderbar
  // colorWeak: false,
  // multiTab: true,
  menu: {
    locale: true
  },
  title: 'NexIoT',
  // 侧边栏标题配置
  siderMenu: {
    // 收缩状态下的标题（仅英文）
    collapsedTitle: 'NexIoT',
    // 展开状态下的标题（默认仅英文）
    expandedTitle: 'NexIoT',
    // 宽屏时显示的标题（中文+英文）
    wideScreenTitle: '奈科斯 NexIoT',
    // 宽屏断点宽度（单位：px），大于等于此宽度时显示 wideScreenTitle
    wideScreenBreakpoint: 1280
  },
  pwa: false,
  iconfontUrl: '',
  production: process.env.NODE_ENV === 'production'
    && process.env.VUE_APP_PREVIEW !== 'true',
  tableSize: 'middle',
  layout: 'sidemenu',
  contentWidth: 'Fluid',
  theme: 'light',
  primaryColor: '#2f54eb',
  fixedHeader: false,
  fixSiderbar: false,
  multiTab: true,
  hideHintAlert: true,
  hideCopyButton: false,
  // 全局水印配置
  watermark: {
    // 是否启用水印功能
    enabled: true,
    // 自定义水印文本（会自动追加到登录用户名后面，作为第二行显示）
    // 示例：如果用户名为 "admin"，customText 为 "内部系统"，则水印显示为：
    // 第一行：admin
    // 第二行：内部系统
    // customText: '学校公益免费，商用请联系outlookfil',
    customText: '全部为真实设备，演示环境需爱护，请勿进行破坏性 / 违规操作！',
    // 字体大小（单位：px）
    fontSize: 16,
    // 字体颜色（RGBA格式，建议透明度在0.05-0.15之间，值越小越淡）
    fontColor: 'rgba(0, 0, 0, 0.08)',
    // 字体族（支持系统字体或Web字体）
    fontFamily: 'Arial, sans-serif',
    // 水印间距 [水平间距, 垂直间距]（单位：px）
    // 值越大，水印越稀疏，对页面内容干扰越小
    gap: [300, 500],
    // 水印旋转角度（单位：度，负数表示逆时针旋转）
    rotate: -10,
    // 水印层级（z-index值，确保水印在最上层但不遮挡交互元素）
    zIndex: 9999
  }
}