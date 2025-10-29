<script>
export default {
  name: 'Analysis',
  props: {
    type: {
      type: String,
      default: ''
    },
    record: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {}
  },
  methods: {
    baseColumns(record, other) {
      return <div class="analysis-container">
        {other ? other() : null}
      </div>
    }
  },
  render() {
    const {record, type, baseColumns} = this

    if (type === 'properties') {
      if (record.type === 'string') {
        return baseColumns(record)
      } else if (record.type === 'int') {
        return baseColumns(record)
      } else if (record.type === 'enum') {
        return baseColumns(record, () => <div class="enum-values">
          <span class="enum-label">枚举值：</span>
          <div class="enum-tags">
            {JSON.parse(record.elements).map((item) =>
              <a-tag class="enum-tag">
                {item.value} - {item.text}
              </a-tag>
            )}
          </div>
        </div>)
      } else {
        return null
      }
    } else if (type === 'functions') {
      if (record.type === 'string') {
        return record.valueType.expands ? baseColumns(record, () => <div class="value-info">
            <span class="info-label">最大长度：</span>
            <span class="info-value">{record.valueType.expands.maxLength}</span>
          </div>)
          : baseColumns(record)
      } else if (record.type === 'int') {
        return (record.valueType.min || record.valueType.max) ? baseColumns(record,
            () => <div class="value-info">
              <span class="info-label">取值范围：</span>
              <span class="info-value">{record.valueType.min || 0} ~ {record.valueType.max
                || '+∞'}</span>
            </div>)
          : baseColumns(record)
      } else if (record.type === 'enum') {
        return record.valueType.elements ? baseColumns(record, () => <div class="enum-values">
            <span class="enum-label">枚举值：</span>
            <div class="enum-tags">
              {record.valueType.elements.map(item => <a-tag class="enum-tag">
                {item.value} - {item.text}
              </a-tag>)}
            </div>
          </div>)
          : baseColumns(record)
      } else {
        return null
      }
    } else {
      return null
    }
  }
}
</script>

<style scoped lang="less">
.analysis-container {
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin: 8px 0;
  max-width: 100%;
  overflow: hidden;
}

.enum-values {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .enum-label {
    font-size: 13px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 4px;
  }

  .enum-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    max-width: 100%;

    .enum-tag {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      background: #f0f9ff;
      color: #1966ff;
      border: 1px solid #bfdbfe;
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 200px;
    }
  }
}

.value-info {
  display: flex;
  align-items: center;
  gap: 8px;

  .info-label {
    font-size: 13px;
    font-weight: 600;
    color: #374151;
    white-space: nowrap;
  }

  .info-value {
    font-size: 13px;
    color: #1966ff;
    font-weight: 500;
    font-family: 'Monaco', 'Menlo', monospace;
  }
}


@media (max-width: 768px) {
  .enum-tags {
    .enum-tag {
      max-width: 150px;
    }
  }

  .value-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
