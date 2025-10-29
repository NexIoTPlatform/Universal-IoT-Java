<a-col :md="8" :sm="24">
<a-form-item :label="$t('device.accessMethod')" prop="type">
  <a-select placeholder="请选择接入方式" style="width: 100%" v-model="queryParam.type" allow-clear>
    <a-select-option v-for="(d, index) in protocolTypeOptions" :key="index" :value="d.dictValue">
      {{ d.dictLabel }}
    </a-select-option>
  </a-select>
</a-form-item>
</a-col> 