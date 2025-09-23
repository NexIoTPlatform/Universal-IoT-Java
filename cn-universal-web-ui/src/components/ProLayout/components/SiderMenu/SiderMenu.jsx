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

  if (collapsed) {
    // 收缩状态：显示简写 "U-IoT" 或图标样式
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '12px 0',
        height: '64px',
        transition: 'all 0.3s ease'
      }}>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <span style={{
            fontWeight: '800',
            fontSize: '16px',
            color: '#1890ff',
            letterSpacing: '0px',
            fontFamily: 'Arial, sans-serif',
            lineHeight: '1',
            textShadow: '0 1px 2px rgba(0,0,0,0.1)'
          }}>
            U
          </span>
          <span style={{
            fontWeight: '600',
            fontSize: '10px',
            color: '#52c41a',
            letterSpacing: '0.5px',
            fontFamily: 'Arial, sans-serif',
            lineHeight: '1',
            marginTop: '2px'
          }}>
            IoT
          </span>
        </div>
      </div>
    )
  } else {
    // 展开状态：显示完整的 "Universal-IoT" 
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
          {/* 图标部分 */}
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, #1890ff 0%, #52c41a 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginRight: '12px',
            boxShadow: '0 2px 8px rgba(24, 144, 255, 0.3)'
          }}>
            <span style={{
              color: '#fff',
              fontSize: '16px',
              fontWeight: '800',
              fontFamily: 'Arial, sans-serif'
            }}>
              U
            </span>
          </div>

          {/* 文字部分 */}
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
              fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif',
              lineHeight: '1.2',
              marginBottom: '2px'
            }}>
              Universal
            </span>
            <span style={{
              fontWeight: '500',
              fontSize: '12px',
              color: '#52c41a',
              letterSpacing: '1px',
              fontFamily: 'Arial, sans-serif',
              lineHeight: '1'
            }}>
              IoT Platform
            </span>
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
