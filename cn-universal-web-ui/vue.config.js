const modifyVars = require('./src/config/theme')

const path = require('path')
const webpack = require('webpack')
const GitRevisionPlugin = require('git-revision-webpack-plugin')
const GitRevision = new GitRevisionPlugin()
// const buildDate = JSON.stringify(new Date().toLocaleString())
const createThemeColorReplacerPlugin = require('./config/plugin.config')

function resolve(dir) {
  return path.join(__dirname, dir)
}

// check Git
// function getGitHash () {
//   try {
//     return GitRevision.version()
//   } catch (e) {}
//   return 'unknown'
// }

const isProd = process.env.NODE_ENV === 'production'
const assetsCDN = {
  // webpack build externals
  externals: {
    vue: 'Vue',
    'vue-router': 'VueRouter',
    vuex: 'Vuex',
    axios: 'axios',
  },
  css: [],
  js: [
    '/js/lib/vue.min.js',
    '/js/lib/vue-router.min.js',
    '/js/lib/vuex.min.js',
    '/js/lib/axios.min.js'
  ],
}

// vue.config.js
const vueConfig = {
  configureWebpack: {
    optimization: isProd
      ? {
        splitChunks: {
          chunks: 'all',
          maxInitialRequests: Infinity, // 默认为3，调整为允许无限入口资源
          minSize: 20000, // 20K以下的依赖不做拆分
          cacheGroups: {
            vendors: {
              // 拆分依赖，避免单文件过大拖慢页面展示
              // 得益于HTTP2多路复用，不用太担心资源请求太多的问题
              name(module) {
                // 拆包
                const packageName = module.context.match(
                  /[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1]
                // 进一步将Ant组件拆分出来,请根据情况来
                // const packageName = module.context.match(/[\\/]node_modules[\\/](?:ant-design-vue[\\/]es[\\/])?(.*?)([\\/]|$)/)[1]
                return `npm.${packageName.replace('@', '')}` // 部分服务器不允许URL带@
              },
              test: /[\\/]node_modules[\\/]/,
              priority: -10,
              chunks: 'initial',
            },
          },
        },
        runtimeChunk: {name: (entrypoint) => `runtime-${entrypoint.name}`},
      }
      : {},
    // webpack plugins
    plugins: [
      // Ignore all locale files of moment.js
      // 不打包moment时区文件
      new webpack.IgnorePlugin({
        resourceRegExp: /^\.\/locale$/,
        contextRegExp: /moment$/,
      }),
      // Handle problematic Less files with JavaScript
      new webpack.NormalModuleReplacementPlugin(
        /bezierEasing\.less$/,
        require.resolve('./src/config/bezierEasing.less')
      ),
      // new webpack.DefinePlugin({
      //   APP_VERSION: `"${require('./package.json').version}"`,
      //   GIT_HASH: JSON.stringify(getGitHash()),
      //   BUILD_DATE: buildDate
      // })
    ],
    // if prod, add externals
    externals: isProd ? assetsCDN.externals : {},
  },

  chainWebpack: (config) => {
    config.resolve.alias.set('@$', resolve('src'))

    const svgRule = config.module.rule('svg')
    svgRule.uses.clear()
    svgRule
    .oneOf('inline')
    .resourceQuery(/inline/)
    .use('vue-svg-icon-loader')
    .loader('vue-svg-icon-loader')
    .end()
    .end()
    .oneOf('external')
    .use('file-loader')
    .loader('file-loader')
    .options({
      name: 'assets/[name].[hash:8].[ext]',
    })

    // if prod is on
    // assets require on cdn
    if (isProd) {
      config.plugin('html').tap((args) => {
        args[0].cdn = assetsCDN
        return args
      })
    }
    // if(isProd) {
    //   config.plugin('compression-webpack-plugin').use(
    //     new CompressionPlugin({
    //       test: /\.js$|\.html$|\.css$/, // 匹配文件名
    //       threshold: 10240, // 对超过10k的数据压缩
    //       deleteOriginalAssets: false, // 不删除源文件
    //     })
    //   )
    // }
  },

  css: {
    loaderOptions: {
      less: {
        lessOptions: {
          javascriptEnabled: true,
          modifyVars: modifyVars,
          // Add additional options to handle JavaScript in Less
          math: 'always',
          strictUnits: false,
          globalVars: {
            // Define global variables to avoid import issues
            '@primary-color': '#1890ff',
            '@link-color': '#1890ff',
            '@success-color': '#52c41a',
            '@warning-color': '#faad14',
            '@error-color': '#f5222d',
            '@font-size-base': '14px',
            '@heading-color': 'rgba(0, 0, 0, 0.85)',
            '@text-color': 'rgba(0, 0, 0, 0.65)',
            '@text-color-secondary': 'rgba(0, 0, 0, 0.45)',
            '@disabled-color': 'rgba(0, 0, 0, 0.25)',
            '@border-radius-base': '6px',
            '@border-color-base': '#d9d9d9',
            '@box-shadow-base': '0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 9px 28px 8px rgba(0, 0, 0, 0.05)'
          }
        }
      }
    },
  },

  devServer: {
    // development server port 8000
    host: '0.0.0.0',
    port: 8000,
    // 启用热重载
    hot: true,
    // 启用实时重载
    liveReload: true,
    // 启用压缩
    compress: true,
    // 启用历史API回退
    historyApiFallback: true,
    // If you want to turn on the proxy, please remove the mockjs /src/main.jsL11
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: process.env.VUE_APP_API_PATH,
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: '/',
        },
      },
    },
  },

  // disable source map in production
  productionSourceMap: false,
  lintOnSave: undefined,
  // babel-loader no-ignore node_modules/*
  transpileDependencies: [],
}

// preview.pro.loacg.com only do not use in your production;
if (process.env.VUE_APP_PREVIEW === 'true') {
  console.log('VUE_APP_PREVIEW', true)
  // add `ThemeColorReplacer` plugin to webpack plugins
  vueConfig.configureWebpack.plugins.push(createThemeColorReplacerPlugin())
}

module.exports = vueConfig
