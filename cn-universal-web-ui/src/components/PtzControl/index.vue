<template>
  <div class="ptz-control-panel">
    <div class="ptz-direction">
      <div class="ptz-row">
        <a-button class="ptz-btn" @mousedown="sendCommand('leftUp')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-up" :rotate="-45" />
        </a-button>
        <a-button class="ptz-btn" @mousedown="sendCommand('up')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-up" />
        </a-button>
        <a-button class="ptz-btn" @mousedown="sendCommand('rightUp')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-up" :rotate="45" />
        </a-button>
      </div>
      <div class="ptz-row">
        <a-button class="ptz-btn" @mousedown="sendCommand('left')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-left" />
        </a-button>
        <a-button class="ptz-btn ptz-home" @click="sendCommand('home')">
          <a-icon type="home" />
        </a-button>
        <a-button class="ptz-btn" @mousedown="sendCommand('right')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-right" />
        </a-button>
      </div>
      <div class="ptz-row">
        <a-button class="ptz-btn" @mousedown="sendCommand('leftDown')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-down" :rotate="45" />
        </a-button>
        <a-button class="ptz-btn" @mousedown="sendCommand('down')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-down" />
        </a-button>
        <a-button class="ptz-btn" @mousedown="sendCommand('rightDown')" @mouseup="sendCommand('stop')">
          <a-icon type="arrow-down" :rotate="-45" />
        </a-button>
      </div>
    </div>
    
    <!-- <div class="ptz-zoom">
      <div class="zoom-item">
        <span>变倍:</span>
        <a-button size="small" @mousedown="sendCommand('zoomIn')" @mouseup="sendCommand('stop')">
          <a-icon type="plus" />
        </a-button>
        <a-button size="small" @mousedown="sendCommand('zoomOut')" @mouseup="sendCommand('stop')">
          <a-icon type="minus" />
        </a-button>
      </div>
      <div class="zoom-item">
        <span>聚焦:</span>
        <a-button size="small" @mousedown="sendCommand('focusIn')" @mouseup="sendCommand('stop')">
          <a-icon type="plus" />
        </a-button>
        <a-button size="small" @mousedown="sendCommand('focusOut')" @mouseup="sendCommand('stop')">
          <a-icon type="minus" />
        </a-button>
      </div>
      <div class="zoom-item">
        <span>光圈:</span>
        <a-button size="small" @mousedown="sendCommand('irisIn')" @mouseup="sendCommand('stop')">
          <a-icon type="plus" />
        </a-button>
        <a-button size="small" @mousedown="sendCommand('irisOut')" @mouseup="sendCommand('stop')">
          <a-icon type="minus" />
        </a-button>
      </div>
    </div> -->
    
    <!-- <div class="ptz-speed">
      <span>速度:</span>
      <a-slider v-model="speed" :min="1" :max="100" style="flex: 1; margin: 0 12px;" />
      <span>{{ speed }}</span>
    </div>
    
    <div class="ptz-presets" v-if="showPresets">
      <a-button size="small" @click="$emit('queryPresets')">查询预置位</a-button>
      <a-select 
        v-model="selectedPreset" 
        placeholder="选择预置位" 
        size="small" 
        style="width: 120px; margin: 0 8px;"
        :disabled="!presets.length"
      >
        <a-select-option v-for="p in presets" :key="p.value" :value="p.value">
          {{ p.label }}
        </a-select-option>
      </a-select>
      <a-button size="small" @click="gotoPreset" :disabled="!selectedPreset">转到</a-button>
      <a-button size="small" @click="setPreset">设置</a-button>
    </div> -->
  </div>
</template>

<script>
export default {
  name: 'PtzControl',
  props: {
    showPresets: {
      type: Boolean,
      default: true
    },
    presets: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      speed: 50,
      selectedPreset: undefined
    }
  },
  methods: {
    sendCommand(command) {
      this.$emit('command', { command, speed: this.speed })
    },
    gotoPreset() {
      if (this.selectedPreset) {
        this.$emit('gotoPreset', this.selectedPreset)
      }
    },
    setPreset() {
      this.$emit('setPreset', this.selectedPreset || 1)
    }
  }
}
</script>

<style lang="less" scoped>
.ptz-control-panel {
  padding: 16px;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  
  .ptz-direction {
    margin-bottom: 16px;
    
    .ptz-row {
      display: flex;
      justify-content: center;
      gap: 8px;
      margin-bottom: 8px;
      
      .ptz-btn {
        width: 48px;
        height: 48px;
        
        &.ptz-home {
          background: #f0f0f0;
        }
      }
    }
  }
  
  .ptz-zoom {
    margin-bottom: 16px;
    
    .zoom-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      
      span {
        width: 50px;
        font-size: 12px;
      }
      
      button {
        margin-left: 8px;
      }
    }
  }
  
  .ptz-speed {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    font-size: 12px;
  }
  
  .ptz-presets {
    display: flex;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #e8e8e8;
    
    button {
      margin-right: 8px;
    }
  }
}
</style>
