<template>
  <codemirror
    v-model="code"
    :options="cmOptions"
    style="width:100%;height:100%;"
    @change="onChange"
    @input="onInput"
    @blur="onBlur"
    @focus="onFocus"
  />
</template>

<script>
import {codemirror} from 'vue-codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/theme/material.css'

export default {
  name: 'CodeEditor',
  components: {codemirror},
  props: {
    value: {
      type: String,
      default: ''
    },
    options: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      code: this.value || '',
      cmOptions: {
        mode: 'javascript',
        theme: 'material',
        lineNumbers: true,
        lineWrapping: true,
        tabSize: 2,
        ...this.options
      }
    }
  },
  watch: {
    value: {
      handler(newVal) {
        console.log('【CodeEditor】value prop changed:', newVal)
        if (newVal !== this.code) {
          this.code = newVal || ''
          console.log('【CodeEditor】code updated to:', this.code)
        }
      },
      immediate: true
    }
  },
  methods: {
    onChange(val) {
      console.log('【CodeEditor】onChange event triggered:', val)
      this.code = val
      this.$emit('input', val)
      this.$emit('change', val)
    },
    onInput(val) {
      console.log('【CodeEditor】onInput event triggered:', val)
      this.code = val
      this.$emit('input', val)
    },
    onBlur() {
      console.log('【CodeEditor】onBlur event triggered, current code:', this.code)
      this.$emit('blur', this.code)
    },
    onFocus() {
      console.log('【CodeEditor】onFocus event triggered, current code:', this.code)
      this.$emit('focus', this.code)
    }
  }
}
</script>

<style scoped>
:deep(.CodeMirror) {
  height: 100% !important;
  min-height: 200px;
  font-size: 14px;
  font-family: 'Fira Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
}
</style>
