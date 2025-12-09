package cn.universal.security;

import cn.hutool.core.date.DateUtil;
import cn.universal.common.utils.StringUtils;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.page.PageDomain;
import cn.universal.persistence.page.PageUtils;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.page.TableSupport;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.service.IoTUserService;
import cn.universal.security.utils.SecurityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/** 数据处理 */
public class BaseController {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private IoTUserService ioTUserService;

  /** 日期格式字符串自动转换Date */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    // Date 类型转换
    binder.registerCustomEditor(
        Date.class,
        new PropertyEditorSupport() {
          @Override
          public void setAsText(String text) {
            // 如果字符串为空或空白，设置为 null，避免解析错误
            if (StringUtils.isBlank(text)) {
              setValue(null);
            } else {
              setValue(DateUtil.parseDate(text));
            }
          }
        });
  }

  @Cacheable(cacheNames = "user_parent_entity", key = "''+#unionId", unless = "#result == null")
  public IoTUser loginIoTUnionUser(String unionId) {
    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    if (iotUser.getParentUnionId() != null) {
      iotUser = ioTUserService.selectUserByUnionId(iotUser.getParentUnionId());
      return iotUser;
    }
    return iotUser;
  }

  public boolean isAdmin() {
    IoTUser iotUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());
    return iotUser.isAdmin();
  }

  /** 设置请求分页数据 */
  protected void startPage() {
    PageUtils.startPage();
  }

  /** 设置请求排序数据 */
  protected void startOrderBy() {
    PageDomain pageDomain = TableSupport.buildPageRequest();
    if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
      String orderBy = pageDomain.getOrderBy();
      PageHelper.orderBy(orderBy);
    }
  }

  /** 响应请求分页数据 */
  protected <T> TableDataInfo<T> getDataTable(List<T> list, Integer total) {
    TableDataInfo<T> rspData = new TableDataInfo<>();
    // 请求成功返回0
    rspData.setCode(0);
    rspData.setMsg("查询成功");
    rspData.setRows(list);
    rspData.setTotal(total);
    return rspData;
  }

  /** 响应请求分页数据 */
  protected <T> TableDataInfo<T> getDataTable(List<T> list) {
    TableDataInfo<T> rspData = new TableDataInfo<>();
    // 请求成功返回0
    rspData.setCode(0);
    rspData.setMsg("查询成功");
    rspData.setRows(list);
    rspData.setTotal((int) new PageInfo(list).getTotal());
    return rspData;
  }

  /** 返回成功 */
  public AjaxResult<Void> success() {
    return AjaxResult.success();
  }

  /** 返回失败消息 */
  public AjaxResult<Void> error() {
    return AjaxResult.error();
  }

  /** 返回成功消息 */
  public AjaxResult<Void> success(String message) {
    return AjaxResult.success(message);
  }

  /** 返回失败消息 */
  public AjaxResult<Void> error(String message) {
    return AjaxResult.error(message);
  }

  /**
   * 响应返回结果
   *
   * @param rows 影响行数
   * @return 操作结果
   */
  protected AjaxResult<Void> toAjax(int rows) {
    return rows > 0 ? AjaxResult.success() : AjaxResult.error();
  }

  /**
   * 响应返回结果
   *
   * @param result 结果
   * @return 操作结果
   */
  protected AjaxResult<Void> toAjax(boolean result) {
    return result ? success() : error();
  }

  /** 页面跳转 */
  public String redirect(String url) {
    return StringUtils.format("redirect:{}", url);
  }
}
