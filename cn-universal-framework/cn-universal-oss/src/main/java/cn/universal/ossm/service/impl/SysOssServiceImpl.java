/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.ossm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.exception.IoTException;
import cn.universal.ossm.entity.SysOss;
import cn.universal.ossm.entity.bo.SysOssBo;
import cn.universal.ossm.mapper.SysOssMapper;
import cn.universal.ossm.oss.entity.UploadResult;
import cn.universal.ossm.oss.factory.OssFactory;
import cn.universal.ossm.oss.service.ICloudStorageService;
import cn.universal.ossm.service.ISysOssService;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 文件上传 服务层实现 @Author Lion Li */
@Service
public class SysOssServiceImpl implements ISysOssService {

  @Resource private SysOssMapper sysOssMapper;

  @Value("${codec.path:nexiot/}")
  private String prePath;

  @Override
  public SysOss getById(Long ossId) {
    return sysOssMapper.selectByPrimaryKey(ossId);
  }

  @Override
  public List<SysOss> queryPageList(SysOssBo bo) {
    SysOss sysOss = BeanUtil.toBean(bo, SysOss.class);
    return sysOssMapper.select(sysOss);
  }

  @Override
  public SysOss upload(MultipartFile file, String unionId) {
    if (file.getOriginalFilename() == null) {
      throw new IoTException("文件名为空！");
    }
    // 获取文件扩展名
    String originalFilename = file.getOriginalFilename();
    String suffix = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      suffix = StrUtil.sub(originalFilename, originalFilename.lastIndexOf("."), originalFilename.length());
    }
    // 生成基于 Unix 时间戳的文件名
    String originalfileName = prePath + System.currentTimeMillis() + suffix;
    ICloudStorageService storage = OssFactory.instance();
    UploadResult uploadResult;
    try {
      uploadResult = storage.upload(file.getBytes(), originalfileName, file.getContentType());
    } catch (IOException e) {
      throw new IoTException("文件读取异常!!!", e);
    }
    // 保存文件信息
    SysOss oss =
        new SysOss()
            .setUrl(uploadResult.getUrl())
            .setFileSuffix(suffix)
            .setFileName(uploadResult.getFilename())
            .setOriginalName(originalfileName)
            .setService(storage.getServiceType())
            .setCreateBy(unionId)
            .setCreateTime(new Date());
    sysOssMapper.insert(oss);
    return oss;
  }

  @Override
  public SysOss uploadStream(InputStream inputStream, String fileName) {
    ICloudStorageService storage = OssFactory.instance();
    UploadResult uploadResult;
    try {
      uploadResult =
          storage.upload(
              inputStream, "snapshot/" + fileName, fileName.substring(fileName.lastIndexOf(".")));
    } catch (Exception e) {
      throw new IoTException("文件读取异常!!!", e);
    }
    // 保存文件信息
    SysOss oss =
        new SysOss()
            .setUrl(uploadResult.getUrl())
            .setOriginalName(fileName)
            .setFileSuffix(fileName.substring(fileName.lastIndexOf(".")))
            .setFileName(fileName)
            .setService(storage.getServiceType())
            .setCreateTime(new Date());
    sysOssMapper.insert(oss);
    return oss;
  }

  @Override
  public SysOss uploadVideoStream(InputStream inputStream, String fileName) {
    ICloudStorageService storage = OssFactory.instance();
    UploadResult uploadResult;
    try {
      uploadResult =
          storage.upload(
              inputStream, "video/" + fileName, fileName.substring(fileName.lastIndexOf(".")));
    } catch (Exception e) {
      throw new IoTException("文件读取异常!!!", e);
    }
    // 保存文件信息
    SysOss oss =
        new SysOss()
            .setUrl(uploadResult.getUrl())
            .setOriginalName(fileName)
            .setFileSuffix(fileName.substring(fileName.lastIndexOf(".")))
            .setFileName(fileName)
            .setService(storage.getServiceType())
            .setCreateTime(new Date());
    sysOssMapper.insert(oss);
    return oss;
  }

  @Override
  public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
    if (isValid) {
      // 做一些业务上的校验,判断是否需要校验
    }
    List<SysOss> list = sysOssMapper.listByIds(ids);
    for (SysOss sysOss : list) {
      ICloudStorageService storage = OssFactory.instance(sysOss.getService());
      storage.delete(sysOss.getUrl());
    }
    return sysOssMapper.removeByIds(ids) > 0;
  }

  @Override
  public Boolean deleteWithValidByUrls(String[] urls, Boolean isValid) {
    if (urls == null || urls.length <= 0) {
      return false;
    }

    if (isValid) {
      // 做一些业务上的校验,判断是否需要校验
    }
    List<SysOss> list = sysOssMapper.listByUrls(urls);
    for (SysOss sysOss : list) {
      ICloudStorageService storage = OssFactory.instance(sysOss.getService());
      storage.delete(sysOss.getUrl());
    }
    return sysOssMapper.removeByUrls(urls) > 0;
  }
}
