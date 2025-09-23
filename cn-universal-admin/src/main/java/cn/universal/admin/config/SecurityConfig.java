package cn.universal.admin.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 安全配置
 * 管理敏感字段的加密密钥
 */
@Data
@Component
public class SecurityConfig {
    
    /**
     * 产品密钥加密密钥
     * 建议在生产环境中使用环境变量或配置中心
     */
    @Value("${security.product-secret.key:cn-universal-product-secret-key-2024}")
    private String productSecretKey;
    
    /**
     * 是否启用敏感字段加密
     */
    @Value("${security.sensitive-field.enabled:true}")
    private Boolean sensitiveFieldEnabled;
}
