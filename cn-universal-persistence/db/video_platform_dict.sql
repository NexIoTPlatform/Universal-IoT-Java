-- 视频平台类型字典
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (214, '视频平台类型', 'video_platform_type', '0', 'admin', NOW(), '', NULL, '视频平台类型定义');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (795, 1, 'WVP视频平台', 'wvp', 'video_platform_type', '', 'primary', 'Y', '0', 'admin', NOW(), '', NULL, NULL);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (796, 2, '海康ISC', 'ics', 'video_platform_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, NULL);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (797, 3, '大华ICC', 'icc', 'video_platform_type', '', 'warning', 'N', '0', 'admin', NOW(), '', NULL, NULL);

-- WVP平台配置字段
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (215, 'WVP平台配置字段', 'video_wvp_fields', '0', 'admin', NOW(), '', NULL, 'WVP平台配置字段定义');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (798, 1, '平台地址', 'endpoint', 'video_wvp_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"http://127.0.0.1:18080"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (799, 2, 'Token', 'token', 'video_wvp_fields', '', '', 'N', '0', 'admin', NOW(), '', NULL, '{"type":"password","required":false,"placeholder":"可选，填写后自动携带鉴权头"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (800, 3, '平台版本', 'version', 'video_wvp_fields', '', '', 'N', '0', 'admin', NOW(), '', NULL, '{"type":"select","required":false,"options":["v1","v2"],"default":"v2"}');

-- 海康ISC平台配置字段
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (216, '海康ISC平台配置字段', 'video_hik_ics_fields', '0', 'admin', NOW(), '', NULL, '海康ISC平台配置字段定义');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (801, 1, '平台地址', 'endpoint', 'video_hik_ics_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"https://icc-dev.hibetatest.com:4077"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (802, 2, '用户名', 'username', 'video_hik_ics_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"请输入平台用户名"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (726, 3, '密码', 'password', 'video_hik_ics_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"password","required":true,"placeholder":"请输入平台密码"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (727, 4, 'ClientId', 'clientId', 'video_hik_ics_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"请输入ClientId"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (728, 5, 'ClientSecret', 'clientSecret', 'video_hik_ics_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"password","required":true,"placeholder":"请输入ClientSecret"}');

-- 大华ICC平台配置字段
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (217, '大华ICC平台配置字段', 'video_dahua_icc_fields', '0', 'admin', NOW(), '', NULL, '大华ICC平台配置字段定义');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (729, 1, '平台地址', 'endpoint', 'video_dahua_icc_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"https://open-icc.dahuatech.com"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (730, 2, 'AppKey', 'appKey', 'video_dahua_icc_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":true,"placeholder":"请输入AppKey"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (731, 3, 'AppSecret', 'appSecret', 'video_dahua_icc_fields', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '{"type":"password","required":true,"placeholder":"请输入AppSecret"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (732, 4, '用户名', 'username', 'video_dahua_icc_fields', '', '', 'N', '0', 'admin', NOW(), '', NULL, '{"type":"input","required":false,"placeholder":"可选，用于基础认证"}');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES (733, 5, '密码', 'password', 'video_dahua_icc_fields', '', '', 'N', '0', 'admin', NOW(), '', NULL, '{"type":"password","required":false,"placeholder":"可选，用于基础认证"}');
