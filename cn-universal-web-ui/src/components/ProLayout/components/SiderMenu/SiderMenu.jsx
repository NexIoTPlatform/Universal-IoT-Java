import './index.less'

import PropTypes from 'ant-design-vue/es/_util/vue-types'
import 'ant-design-vue/es/layout/style'
import Layout from 'ant-design-vue/es/layout'
import {isFun} from '../../utils/util'
import BaseMenu from '../RouteMenu'

const {Sider} = Layout

export const SiderMenuProps = {
  i18nRender: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]).def(false),
  mode: PropTypes.string.def('inline'),
  theme: PropTypes.string.def('dark'),
  contentWidth: PropTypes.oneOf(['Fluid', 'Fixed']).def('Fluid'),
  collapsible: PropTypes.bool,
  collapsed: PropTypes.bool,
  handleCollapse: PropTypes.func,
  menus: PropTypes.array,
  siderWidth: PropTypes.number.def(256),
  isMobile: PropTypes.bool,
  layout: PropTypes.string.def('inline'),
  fixSiderbar: PropTypes.bool,
  logo: PropTypes.any,
  title: PropTypes.string.def(''),
  // render function or vnode
  menuHeaderRender: PropTypes.oneOfType(
    [PropTypes.func, PropTypes.array, PropTypes.object, PropTypes.bool]),
  menuRender: PropTypes.oneOfType(
    [PropTypes.func, PropTypes.array, PropTypes.object, PropTypes.bool])
}

export const defaultRenderLogo = (h, logo) => {
  if (typeof logo === 'string') {
    return <img src={logo} alt="logo"/>
  }
  if (typeof logo === 'function') {
    return logo()
  }
  return h(logo)
}

export const defaultRenderLogoAntTitle = (h, props) => {
  const {collapsed} = props
  // 标题：默认英文，宽屏时显示 中文+英文
  let mainTitle = 'NexIoT'
  try {
    if (typeof window !== 'undefined' && window.innerWidth >= 1280) {
      mainTitle = '奈科斯 NexIoT'
    }
  } catch (e) {
    // ignore
  }

  if (collapsed) {
    // 收缩状态：仅显示英文标题
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '12px 0',
        height: '64px',
        transition: 'all 0.3s ease'
      }}>
        <span style={{
          fontWeight: '700',
          fontSize: '14px',
          color: '#1890ff',
          letterSpacing: '0.5px',
          fontFamily: 'PingFang SC, Microsoft YaHei, Arial, sans-serif',
          lineHeight: '1.2'
        }}>NexIoT</span>
      </div>
    )
  } else {
    // 展开状态：仅显示标题文本
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-start',
        padding: '16px',
        height: '64px',
        transition: 'all 0.3s ease'
      }}>
        <div style={{
          display: 'flex',
          alignItems: 'center'
        }}>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'flex-start'
          }}>
            <span style={{
              fontWeight: '700',
              fontSize: '16px',
              color: '#1890ff',
              letterSpacing: '0.5px',
              fontFamily: 'PingFang SC, Microsoft YaHei, Arial, sans-serif',
              lineHeight: '1.2'
            }}>{mainTitle}</span>
          </div>
        </div>
      </div>
    )
  }
}

const SiderMenu = {
  name: 'SiderMenu',
  model: {
    prop: 'collapsed',
    event: 'collapse'
  },
  props: SiderMenuProps,
  render(h) {
    const {
      collapsible,
      collapsed,
      siderWidth,
      fixSiderbar,
      mode,
      theme,
      menus,
      logo,
      title,
      onMenuHeaderClick = () => null,
      i18nRender,
      menuHeaderRender,
      menuRender
    } = this
    const siderCls = ['ant-pro-sider-menu-sider']
    if (fixSiderbar) {
      siderCls.push('fix-sider-bar')
    }
    if (theme === 'light') {
      siderCls.push('light')
    }
    //
    // const handleCollapse = (collapsed, type) => {
    //   this.$emit('collapse', collapsed)
    // }

    const headerDom = defaultRenderLogoAntTitle(h, {
      logo, title, menuHeaderRender, collapsed
    })

    return (<Sider
      class={siderCls}
      breakpoint={'lg'}
      trigger={null}
      width={siderWidth}
      theme={theme}
      collapsible={collapsible}
      collapsed={collapsed}
    >
      {headerDom && (
        <div
          class="ant-pro-sider-menu-logo"
          onClick={onMenuHeaderClick}
          id="logo"
        >
          <router-link to={{path: '/'}}>
            {headerDom}
          </router-link>
        </div>
      )}
      {menuRender && (
        isFun(menuRender) &&
        menuRender(h, this.$props) ||
        menuRender
      ) || (
        <BaseMenu collapsed={collapsed} menus={menus} mode={mode} theme={theme}
                  i18nRender={i18nRender}/>
      )}
    </Sider>)
  }
}

export default SiderMenu
