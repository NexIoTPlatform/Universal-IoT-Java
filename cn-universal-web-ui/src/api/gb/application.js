import request from '@/utils/request'

/**
 * 国标应用列表
 * @param {*} params
 * @returns
 */
export function getGBAppList(params) {
  return request({
    url: '/admin/wvpThirdApplication/page',
    method: 'get',
    params
  })
}
