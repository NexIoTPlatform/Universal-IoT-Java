package cn.universal.http.protocol.controller;

import cn.universal.http.protocol.service.HttpDownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP下行操作控制器
 *
 * <p>提供设备下行操作的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/http/down")
public class HttpDownController {

  @Autowired private HttpDownService httpDownService;
}
