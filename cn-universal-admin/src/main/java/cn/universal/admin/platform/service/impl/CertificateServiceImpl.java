package cn.universal.admin.platform.service.impl;

import cn.hutool.json.JSONObject;
import cn.universal.admin.platform.service.ICertificateService;
import cn.universal.common.utils.CertInfoUtil;
import cn.universal.persistence.entity.IoTCertificate;
import cn.universal.persistence.mapper.IoTCertificateMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class CertificateServiceImpl implements ICertificateService {

  private static final Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

  @Autowired private IoTCertificateMapper certificateMapper;

  @Override
  public IoTCertificate getBySslKey(String sslKey) {
    Example example = new Example(IoTCertificate.class);
    example.createCriteria().andEqualTo("sslKey", sslKey);
    return certificateMapper.selectOneByExample(example);
  }

  @Override
  public List<IoTCertificate> listAll() {
    return certificateMapper.selectAll();
  }

  @Override
  public void addCertificate(IoTCertificate cert) {
    fillCertInfo(cert);
    certificateMapper.insertSelective(cert);
  }

  @Override
  public void updateCertificate(IoTCertificate cert) {
    fillCertInfo(cert);
    certificateMapper.updateByPrimaryKeySelective(cert);
  }

  @Override
  public void deleteCertificate(Long id) {
    certificateMapper.deleteByPrimaryKey(id);
  }

  @Override
  public List<IoTCertificate> listByUser(String userId) {
    Example example = new Example(IoTCertificate.class);
    example.createCriteria().andEqualTo("createUser", userId);
    return certificateMapper.selectByExample(example);
  }

  @Override
  public List<IoTCertificate> searchCertificates(
      String name,
      String userId,
      String sslKey,
      String expireStart,
      String expireEnd,
      boolean isAdmin) {
    Example example = new Example(IoTCertificate.class);
    Example.Criteria c = example.createCriteria();
    if (!isAdmin && userId != null) {
      c.andEqualTo("createUser", userId);
    }
    if (name != null && !name.isEmpty()) {
      c.andLike("name", "%" + name + "%");
    }
    if (sslKey != null && !sslKey.isEmpty()) {
      c.andEqualTo("sslKey", sslKey);
    }
    if (expireStart != null && !expireStart.isEmpty()) {
      c.andGreaterThanOrEqualTo("expireTime", expireStart);
    }
    if (expireEnd != null && !expireEnd.isEmpty()) {
      c.andLessThanOrEqualTo("expireTime", expireEnd);
    }
    example.orderBy("createTime").desc();
    return certificateMapper.selectByExample(example);
  }

  /** 自动提取证书详细信息并填充到certInfo字段 */
  private void fillCertInfo(IoTCertificate cert) {
    if (cert == null || cert.getCertContent() == null) {
      return;
    }
    try {
      JSONObject jsonObject = CertInfoUtil.extractCertInfo(cert.getCertContent());
      if (jsonObject != null) {
        cert.setCertInfo(jsonObject.toString());
        if (jsonObject.getDate("notAfter") != null) {
          cert.setExpireTime(jsonObject.getDate("notAfter"));
        }
      } else {
        log.warn("证书详细信息提取失败: sslKey={}", cert.getSslKey());
      }
    } catch (Exception e) {
      log.warn("证书详细信息提取异常: sslKey={}, err={}", cert.getSslKey(), e.getMessage());
    }
  }
}
