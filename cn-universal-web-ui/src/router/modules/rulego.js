import Layout from '@/layout'

const rulegoRouter = {
  path: '/rulego',
  component: Layout,
  redirect: '/rulego/chain',
  name: 'Rulego',
  meta: {
    title: 'rulego规则引擎',
    icon: 'node-index'
  },
  children: [
    {
      path: 'chain',
      name: 'RulegoChain',
      component: () => import('@/views/rulego/chain/index'),
      meta: {
        title: '规则链管理',
        icon: 'tree-table'
      }
    }
  ]
}

export default rulegoRouter
