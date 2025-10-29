/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 七牛云存储实现
 *
 */

package cn.universal.ossm.oss.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.exception.IoTException;
import cn.universal.ossm.oss.entity.UploadResult;
import cn.universal.ossm.oss.properties.CloudStorageProperties;
import cn.universal.ossm.oss.service.ICloudStorageService;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class QiniuCloudStorageServiceImpl implements ICloudStorageService {

  @Resource private CloudStorageProperties properties;

  private UploadManager uploadManager;

  @PostConstruct
  public void init() {
    CloudStorageProperties.QiniuProperties cfg = properties.getQiniu();
    Region region = Region.autoRegion();
    Configuration configuration = new Configuration(region);
    this.uploadManager = new UploadManager(configuration);
  }

  @Override
  public void createBucket() {}

  @Override
  public String getServiceType() {
    return "qiniu";
  }

  @Override
  public String getPath(String prefix, String suffix) {
    StringBuilder path = new StringBuilder();
    if (StrUtil.isNotBlank(prefix)) {
      path.append(prefix);
    }
    path.append(System.currentTimeMillis());
    if (StrUtil.isNotBlank(suffix)) {
      path.append(suffix);
    }
    return path.toString();
  }

  private String getUpToken() {
    CloudStorageProperties.QiniuProperties cfg = properties.getQiniu();
    Auth auth = Auth.create(cfg.getAccessKey(), cfg.getSecretKey());
    StringMap putPolicy = new StringMap();
    return auth.uploadToken(cfg.getBucketName(), null, 3600, putPolicy);
  }

  private String buildUrl(String key) {
    CloudStorageProperties.QiniuProperties cfg = properties.getQiniu();
    String domain = cfg.getDomain();
    if (Boolean.TRUE.equals(cfg.getIsHttps())) {
      domain = domain.replaceFirst("^http://", "https://");
    }
    if (!domain.startsWith("http")) {
      domain = (Boolean.TRUE.equals(cfg.getIsHttps()) ? "https://" : "http://") + domain;
    }
    return domain + "/" + key;
  }

  @Override
  public UploadResult upload(byte[] data, String path, String contentType) {
    try {
      String token = getUpToken();
      String key = path;
      com.qiniu.http.Response response =
          uploadManager.put(new ByteArrayInputStream(data), key, token, null, contentType);
      if (!response.isOK()) {
        throw new IoTException("七牛上传失败:" + response.toString());
      }
      DefaultPutRet putRet = JSONUtil.toBean(response.bodyString(), DefaultPutRet.class);
      String url = buildUrl(putRet.key);
      return new UploadResult().setUrl(url).setFilename(putRet.key);
    } catch (Exception e) {
      throw new IoTException("七牛上传异常", e);
    }
  }

  @Override
  public void delete(String path) {
    // 可按需实现：调用 BucketManager 删除
  }

  @Override
  public UploadResult uploadSuffix(byte[] data, String suffix, String contentType) {
    String path = getPath(properties.getQiniu().getPrefix(), suffix);
    return upload(data, path, contentType);
  }

  @Override
  public UploadResult upload(InputStream inputStream, String path, String contentType) {
    try {
      byte[] bytes = inputStream.readAllBytes();
      return upload(bytes, path, contentType);
    } catch (Exception e) {
      throw new IoTException("七牛上传异常", e);
    }
  }

  @Override
  public UploadResult uploadSuffix(InputStream inputStream, String suffix, String contentType) {
    String path = getPath(properties.getQiniu().getPrefix(), suffix);
    return upload(inputStream, path, contentType);
  }

  @Override
  public String getEndpointLink() {
    return properties.getQiniu().getDomain();
  }
}
