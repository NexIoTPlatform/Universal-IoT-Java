/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.network.web;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.annotation.Log;
import cn.universal.admin.common.enums.BusinessType;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.network.service.INetworkService;
import cn.universal.admin.system.web.BaseController;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.Network;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.NetworkBO;
import cn.universal.persistence.entity.vo.NetworkVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.persistence.query.NetworkQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@RestController
@Tag(name = "网络管理", description = "网络管理")
@RequestMapping("/admin/network")
public class NetworkController extends BaseController {

  @Autowired private INetworkService networkService;

  /** 查询列表 */
  @GetMapping("/list")
  public TableDataInfo list(NetworkQuery query) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
      if (iotUser != null && !iotUser.isAdmin()) {
        query.setCreateUser(iotUser.getUnionId());
      }
      List<NetworkBO> list = networkService.selectNetworkList(query);
      return getDataTable(list);
    } catch (Exception e) {
      log.error("查询列表失败", e);
      throw new IoTException("查询列表失败: " + e.getMessage());
    }
  }

  /** 根据ID查询 */
  @GetMapping("/{id}")
  public AjaxResult getInfo(@PathVariable Integer id) {
    try {
      String createUser = SecurityUtils.getUnionId();
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }
      // 校验用户权限
      if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限访问该");
      }
      return AjaxResult.success(network);
    } catch (Exception e) {
      log.error("查询失败", e);
      return AjaxResult.error("查询失败: " + e.getMessage());
    }
  }

  /** 新增 */
  @PostMapping
  public AjaxResult add(@RequestBody Network network) {
    try {
      // 设置创建用户
      network.setCreateUser(SecurityUtils.getUnionId());
      int result = networkService.insertNetwork(network);
      if (result > 0) {
        return AjaxResult.success("新增成功");
      } else {
        return AjaxResult.error("新增失败");
      }
    } catch (Exception e) {
      log.error("新增失败", e);
      return AjaxResult.error("新增失败: " + e.getMessage());
    }
  }

  /** 修改 */
  @PutMapping
  public AjaxResult edit(@RequestBody Network network) {
    try {
      // 校验用户权限
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      NetworkVO existingNetwork = networkService.selectNetworkById(network.getId());
      if (existingNetwork == null) {
        return AjaxResult.error("不存在");
      }
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(existingNetwork.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限修改该");
      }

      int result = networkService.updateNetwork(network);
      if (result > 0) {
        return AjaxResult.success("修改成功");
      } else {
        return AjaxResult.error("修改失败");
      }
    } catch (Exception e) {
      log.error("修改失败", e);
      return AjaxResult.error("操作失败" + e.getMessage());
    }
  }

  /** 删除 */
  @DeleteMapping("/{id}")
  public AjaxResult remove(@PathVariable Integer id) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 校验用户权限
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限删除该");
      }

      int result = networkService.deleteNetworkById(id);
      if (result > 0) {
        return AjaxResult.success("删除成功");
      } else {
        return AjaxResult.error("删除失败");
      }
    } catch (Exception e) {
      log.error("删除失败", e);
      return AjaxResult.error("删除失败: " + e.getMessage());
    }
  }

  /** 批量删除 */
  @DeleteMapping("/batch/{ids}")
  public AjaxResult removeBatch(@PathVariable Integer[] ids) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 校验用户权限
      String createUser = SecurityUtils.getUnionId();
      for (Integer id : ids) {
        NetworkVO network = networkService.selectNetworkById(id);
        if (network == null) {
          return AjaxResult.error("不存在: " + id);
        }
        if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
          return AjaxResult.error("无权限删除: " + id);
        }
      }

      int result = networkService.deleteNetworkByIds(ids);
      if (result > 0) {
        return AjaxResult.success("批量删除成功");
      } else {
        return AjaxResult.error("批量删除失败");
      }
    } catch (Exception e) {
      log.error("批量删除失败", e);
      return AjaxResult.error("批量删除失败: " + e.getMessage());
    }
  }

  /** 启动 */
  @PostMapping("/start/{id}")
  public AjaxResult start(@PathVariable Integer id) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 校验用户权限
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限操作该");
      }

      int result = networkService.startNetwork(id);
      if (result > 0) {
        return AjaxResult.success("启动成功");
      } else {
        return AjaxResult.error("启动失败");
      }
    } catch (Exception e) {
      log.error("启动失败", e);
      return AjaxResult.error("启动失败: " + e.getMessage());
    }
  }

  /** 停止 */
  @PostMapping("/stop/{id}")
  public AjaxResult stop(@PathVariable Integer id) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 校验用户权限
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限操作该");
      }

      int result = networkService.stopNetwork(id);
      if (result > 0) {
        return AjaxResult.success("操作成功");
      } else {
        return AjaxResult.error("操作失败");
      }
    } catch (Exception e) {
      log.error("操作失败", e);
      return AjaxResult.error("操作失败: " + e.getMessage());
    }
  }

  /** 重启 */
  @PostMapping("/restart/{id}")
  public AjaxResult restart(@PathVariable Integer id) {
    try {
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 校验用户权限
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(network.getCreateUser()) && !iotUser.isAdmin()) {
        return AjaxResult.error("无权限操作该");
      }

      int result = networkService.restartNetwork(id);
      if (result > 0) {
        return AjaxResult.success("重启成功");
      } else {
        return AjaxResult.error("重启失败");
      }
    } catch (Exception e) {
      log.error("重启失败", e);
      return AjaxResult.error("重启失败: " + e.getMessage());
    }
  }

  /** 获取网络类型列表 */
  @GetMapping("/types")
  public AjaxResult getNetworkTypes() {
    try {
      List<String> types = networkService.getNetworkTypes();
      return AjaxResult.success(types);
    } catch (Exception e) {
      log.error("获取网络类型列表失败", e);
      return AjaxResult.error("获取网络类型列表失败: " + e.getMessage());
    }
  }

  /** 验证配置 */
  @PostMapping("/validate")
  public AjaxResult validateConfig(@RequestBody Network network) {
    try {
      // 设置创建用户
      network.setCreateUser(SecurityUtils.getUnionId());
      boolean isValid = networkService.validateNetworkConfig(network);
      if (isValid) {
        return AjaxResult.success("配置验证通过");
      } else {
        return AjaxResult.error("配置验证失败");
      }
    } catch (Exception e) {
      log.error("验证配置失败", e);
      return AjaxResult.error("验证配置失败: " + e.getMessage());
    }
  }

  @Resource private INetworkService iNetworkService;

  /** 查询网络列表 */
  @GetMapping("/v1/list")
  public TableDataInfo list(NetworkBO bo) {
    startPage();
    // 设置用户权限过滤
    bo.setCreateUser(SecurityUtils.getUnionId());
    List<NetworkVO> list = iNetworkService.selectNetworkList(bo);
    return getDataTable(list);
  }

  /** 查询绑定指定星纵网关的设备 */
  @GetMapping("/milesight/list")
  public TableDataInfo queryMileSightList(NetworkBO bo) {
    startPage();
    // 设置用户权限过滤
    bo.setCreateUser(SecurityUtils.getUnionId());
    List<IoTDevice> list = iNetworkService.queryMileSightList(bo);
    return getDataTable(list);
  }

  /** 新增设备 */
  @PostMapping("/milesight/add")
  @Log(title = "新增设备", businessType = BusinessType.INSERT)
  public R add(@RequestBody IoTDeviceBO devInstancebo) {
    // 设置创建用户
    devInstancebo.setCreatorId(SecurityUtils.getUnionId());
    R result = iNetworkService.insertDevInstance(devInstancebo);
    JSONObject jsonObject = JSONUtil.parseObj(result);
    String code = jsonObject.getStr("code");
    String msg = jsonObject.getStr("msg");
    if ("0".equals(code)) {
      return R.ok();
    } else {
      return R.error(msg);
    }
  }

  /** 删除设备 */
  @DeleteMapping("/milesight/{ids}")
  @Log(title = "删除设备", businessType = BusinessType.DELETE)
  public R remove(@PathVariable String[] ids) {
    // 校验用户权限
    String currentUser = SecurityUtils.getUnionId();
    for (String id : ids) {
      IoTDevice device = iNetworkService.getDeviceById(id);
      if (device == null) {
        return R.error("设备不存在: " + id);
      }
      if (!currentUser.equals(device.getCreatorId())) {
        return R.error("无权限删除设备: " + id);
      }
    }
    R result = iNetworkService.deleteDevInstanceByIds(ids);
    JSONObject jsonObject = JSONUtil.parseObj(result);
    String code = jsonObject.getStr("code");
    String msg = jsonObject.getStr("msg");
    if ("0".equals(code)) {
      return R.ok();
    } else {
      return R.error(msg);
    }
  }

  @GetMapping("/reload/tcpclient/{applicationId}")
  public R reloadTcpClient(@PathVariable("applicationId") String applicationId) {
    // 校验用户权限 - 需要根据applicationId获取相关的信息进行权限校验
    // 这里可能需要额外的业务逻辑来实现权限校验
    iNetworkService.reloadTcpClient(applicationId);
    return R.ok();
  }

  /** 获取状态 */
  @GetMapping("/status/{id}")
  public AjaxResult getStatus(@PathVariable Integer id) {
    try {
      NetworkVO network = networkService.selectNetworkById(id);
      if (network == null) {
        return AjaxResult.error("不存在");
      }

      // 校验用户权限
      String createUser = SecurityUtils.getUnionId();
      if (!createUser.equals(network.getCreateUser())) {
        return AjaxResult.error("无权限查看该状态");
      }

      // 构建状态信息
      Map<String, Object> status = new HashMap<>();
      status.put("id", network.getId());
      status.put("name", network.getName());
      status.put("type", network.getType());
      status.put("typeName", network.getTypeName());
      status.put("state", network.getState());
      status.put("stateName", network.getStateName());
      status.put("productKey", network.getProductKey());
      status.put("unionId", network.getUnionId());
      status.put("createDate", network.getCreateDate());
      status.put("createUser", network.getCreateUser());

      return AjaxResult.success(status);
    } catch (Exception e) {
      log.error("获取状态失败", e);
      return AjaxResult.error("获取状态失败: " + e.getMessage());
    }
  }

  /** 获取所有状态 */
  @GetMapping("/status")
  public AjaxResult getAllStatus() {
    try {
      NetworkQuery query = new NetworkQuery();
      query.setPageNum(1);
      query.setPageSize(1000); // 获取所有
      // 设置用户权限过滤
      query.setCreateUser(SecurityUtils.getUnionId());

      List<NetworkBO> networks = networkService.selectNetworkList(query);

      List<Map<String, Object>> statusList = new ArrayList<>();
      for (NetworkBO network : networks) {
        Map<String, Object> status = new HashMap<>();
        status.put("id", network.getId());
        status.put("name", network.getName());
        status.put("type", network.getType());
        status.put("state", network.getState());
        status.put("productKey", network.getProductKey());
        status.put("unionId", network.getUnionId());
        statusList.add(status);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("total", statusList.size());
      result.put("networks", statusList);

      return AjaxResult.success(result);
    } catch (Exception e) {
      log.error("获取所有状态失败", e);
      return AjaxResult.error("获取所有状态失败: " + e.getMessage());
    }
  }
}
