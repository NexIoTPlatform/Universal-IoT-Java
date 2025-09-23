package cn.universal.manager.notice.model;

import java.util.Map;

public class NoticeSendRequest {

  private Long templateId;
  private Map<String, Object> params;
  private String receivers;

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public String getReceivers() {
    return receivers;
  }

  public void setReceivers(String receivers) {
    this.receivers = receivers;
  }
}
