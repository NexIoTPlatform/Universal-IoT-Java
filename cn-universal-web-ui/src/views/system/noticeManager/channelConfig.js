// 通知渠道定义
export const channelTypes = [
  {
    value: 'dingTalk',
    label: '钉钉',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>'
  },
  {
    value: 'email',
    label: '邮箱',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>'
  },
  {
    value: 'feishu',
    label: '飞书',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>'
  },
  {
    value: 'sms_ali',
    label: '阿里云短信',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/></svg>'
  },
  {
    value: 'sms_tencent',
    label: '腾讯云短信',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/></svg>'
  },
  {
    value: 'audio_ali',
    label: '阿里云语音',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 14.2 14.47 16 12 16s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V20c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/></svg>'
  },
  {
    value: 'audio_tencent',
    label: '腾讯云语音',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 14.2 14.47 16 12 16s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V20c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/></svg>'
  },
  {
    value: 'webhook',
    label: 'webhook',
    icon: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M17.65 6.35a7.95 7.95 0 0 0-11.3 0 7.95 7.95 0 0 0 0 11.3 7.95 7.95 0 0 0 11.3 0 7.95 7.95 0 0 0 0-11.3zm-1.41 9.89a6 6 0 1 1 0-8.48 6 6 0 0 1 0 8.48zm-2.12-2.12a4 4 0 1 0 0-5.66 4 4 0 0 0 0 5.66zm-1.41-1.41a2 2 0 1 1 0-2.83 2 2 0 0 1 0 2.83z"/></svg>'
  }
]

// 渠道配置定义
export function getChannelTypeConfig(type) {
  const configs = {
    webhook: {
      tip: 'Webhook配置：需要配置Webhook地址',
      fields: [
        {
          key: 'webhook',
          label: 'webhook地址',
          type: 'input',
          required: true,
          placeholder: '请输入webhook地址',
          hint: 'Post方法请求的Content-Type：application/json'
        },
        {
          key: 'AuthTokenKey',
          label: 'AuthTokenKey',
          type: 'input',
          required: false,
          placeholder: '默认Authenication(可选）',
          hint: '自定义HTTP请求头'
        },
        {
          key: 'TokenValue',
          label: 'TokenValue',
          type: 'input',
          required: false,
          placeholder: '请输入请求头的TokenValue（可选）',
          hint: '自定义HTTP请求值TokenValue'
        }
      ]
    },
    dingTalk: {
      tip: '钉钉机器人配置：需要配置机器人的Webhook地址和签名密钥',
      fields: [
        {
          key: 'webhook',
          label: 'Webhook地址',
          type: 'input',
          required: true,
          placeholder: '请输入Webhook地址',
          hint: '机器人的Webhook地址，以https://开头'
        },
        {
          key: 'secret',
          label: '签名密钥',
          type: 'input',
          required: false,
          placeholder: '请输入签名密钥（可选）',
          hint: '用于安全验证的签名密钥'
        }
      ]
    },
    email: {
      tip: '邮件配置：需要配置SMTP服务器信息',
      fields: [
        {
          key: 'smtpHost',
          label: 'SMTP服务器',
          type: 'input',
          required: true,
          placeholder: '如：smtp.qq.com'
        },
        {
          key: 'smtpPort',
          label: 'SMTP端口',
          type: 'input',
          required: true,
          placeholder: '如：587'
        },
        {
          key: 'username',
          label: '邮箱用户名',
          type: 'input',
          required: true,
          placeholder: '邮箱用户名或邮箱地址'
        },
        {
          key: 'password',
          label: '邮箱密码',
          type: 'input',
          inputType: 'password',
          required: true,
          placeholder: '邮箱密码或授权码'
        },
        {
          key: 'fromEmail',
          label: '发件人邮箱',
          type: 'input',
          required: true,
          placeholder: '发件人邮箱地址'
        }
      ]
    },
    feishu: {
      tip: '飞书机器人配置：需要配置机器人的Webhook地址',
      fields: [
        {
          key: 'webhook',
          label: 'Webhook地址',
          type: 'input',
          required: true,
          placeholder: '请输入Webhook地址',
          hint: '飞书机器人的Webhook地址'
        }
      ]
    },
    sms_ali: {
      tip: '阿里云短信配置：需要配置AccessKey、SecretKey、签名和模板',
      fields: [
        {
          key: 'accessKey',
          label: 'AccessKey ID',
          type: 'input',
          required: true,
          placeholder: '阿里云AccessKey ID'
        },
        {
          key: 'secretKey',
          label: 'AccessKey Secret',
          type: 'input',
          inputType: 'password',
          required: true,
          placeholder: '阿里云AccessKey Secret'
        },
        {
          key: 'signName',
          label: '短信签名',
          type: 'input',
          required: true,
          placeholder: '已审核通过的短信签名'
        },
        {
          key: 'templateCode',
          label: '模板代码',
          type: 'input',
          required: true,
          placeholder: '已审核通过的短信模板代码'
        }
      ]
    },
    sms_tencent: {
      tip: '腾讯云短信配置：需要配置SecretId、SecretKey、签名和模板',
      fields: [
        {
          key: 'secretId',
          label: 'SecretId',
          type: 'input',
          required: true,
          placeholder: '腾讯云SecretId'
        },
        {
          key: 'secretKey',
          label: 'SecretKey',
          type: 'input',
          inputType: 'password',
          required: true,
          placeholder: '腾讯云SecretKey'
        },
        {
          key: 'signName',
          label: '短信签名',
          type: 'input',
          required: true,
          placeholder: '已审核通过的短信签名'
        },
        {
          key: 'templateCode',
          label: '模板代码',
          type: 'input',
          required: true,
          placeholder: '已审核通过的短信模板代码'
        }
      ]
    },
    audio_ali: {
      tip: '阿里云语音配置：需要配置AccessKey、SecretKey、签名和模板',
      fields: [
        {
          key: 'accessKey',
          label: 'AccessKey ID',
          type: 'input',
          required: true,
          placeholder: '阿里云AccessKey ID'
        },
        {
          key: 'secretKey',
          label: 'AccessKey Secret',
          type: 'input',
          inputType: 'password',
          required: true,
          placeholder: '阿里云AccessKey Secret'
        },
        {
          key: 'signName',
          label: '语音签名',
          type: 'input',
          required: true,
          placeholder: '已审核通过的语音签名'
        },
        {
          key: 'templateCode',
          label: '模板代码',
          type: 'input',
          required: true,
          placeholder: '已审核通过的语音模板代码'
        }
      ]
    },
    audio_tencent: {
      tip: '腾讯云语音配置：需要配置SecretId、SecretKey、签名和模板',
      fields: [
        {
          key: 'secretId',
          label: 'SecretId',
          type: 'input',
          required: true,
          placeholder: '腾讯云SecretId'
        },
        {
          key: 'secretKey',
          label: 'SecretKey',
          type: 'input',
          inputType: 'password',
          required: true,
          placeholder: '腾讯云SecretKey'
        },
        {
          key: 'signName',
          label: '语音签名',
          type: 'input',
          required: true,
          placeholder: '已审核通过的语音签名'
        },
        {
          key: 'templateCode',
          label: '模板代码',
          type: 'input',
          required: true,
          placeholder: '已审核通过的语音模板代码'
        }
      ]
    }
  }

  return configs[type] || {tip: '', fields: []}
} 