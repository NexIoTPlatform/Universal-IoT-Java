package cn.universal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_certificate")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTCertificate implements Serializable {

  @Id
  private Long id;

  @Column(name = "ssl_key")
  private String sslKey;

  @Column(name = "name")
  private String name;

  @Column(name = "cert_content")
  private String certContent; // PEM内容或加密内容

  @Column(name = "key_content")
  private String keyContent; // PEM内容或加密内容

  @Column(name = "cert_password")
  private String certPassword;

  @Column(name = "key_password")
  private String keyPassword;

  @Column(name = "expire_time")
  private Date expireTime;

  @Column(name = "remark")
  private String remark;

  @Column(name = "create_time")
  private Date createTime;

  @Column(name = "update_time")
  private Date updateTime;

  @Column(name = "create_user")
  private String createUser;

  @Column(name = "cert_info")
  private String certInfo; // 证书详细信息（JSON）
}
