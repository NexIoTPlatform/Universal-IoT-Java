<template>
  <div class="svg-light-placeholder" @click="handleClick">
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200">
      <!-- 清新渐变背景 -->
      <defs>
        <linearGradient id="lightGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#f8fafc"/>
          <stop offset="100%" stop-color="#e2e8f0"/>
        </linearGradient>

        <linearGradient id="blueGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#3b82f6"/>
          <stop offset="100%" stop-color="#1d4ed8"/>
        </linearGradient>

        <linearGradient id="accentGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#06b6d4"/>
          <stop offset="100%" stop-color="#0891b2"/>
        </linearGradient>

        <!-- 柔和脉冲效果 -->
        <radialGradient id="softPulse" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#3b82f6" stop-opacity="0"/>
        </radialGradient>

        <!-- 微光效果 -->
        <filter id="glow">
          <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
          <feMerge>
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>

      <!-- 主容器背景 -->
      <rect width="100%" height="100%" fill="url(#lightGradient)" rx="16"/>
      <rect width="96%" height="96%" x="2%" y="2%" fill="none" stroke="#e2e8f0" stroke-width="1"
            rx="14"/>

      <!-- IoT设备图标 -->
      <g transform="translate(100, 100)">
        <!-- 中心设备 -->
        <circle cx="0" cy="0" r="25" fill="url(#blueGradient)" filter="url(#glow)"/>
        <circle cx="0" cy="0" r="20" fill="none" stroke="#ffffff" stroke-width="2"/>

        <!-- 设备内部细节 -->
        <rect x="-8" y="-8" width="16" height="16" fill="#ffffff" rx="2" opacity="0.9"/>
        <circle cx="-4" cy="-4" r="1.5" fill="url(#blueGradient)"/>
        <circle cx="4" cy="-4" r="1.5" fill="url(#blueGradient)"/>
        <circle cx="0" cy="4" r="1.5" fill="url(#accentGradient)"/>

        <!-- 连接线 -->
        <g stroke="url(#accentGradient)" stroke-width="2" opacity="0.7">
          <line x1="0" y1="-25" x2="0" y2="-40"/>
          <line x1="25" y1="0" x2="40" y2="0"/>
          <line x1="0" y1="25" x2="0" y2="40"/>
          <line x1="-25" y1="0" x2="-40" y2="0"/>
        </g>

        <!-- 外围节点 -->
        <g fill="url(#accentGradient)">
          <circle cx="0" cy="-45" r="6"/>
          <circle cx="45" cy="0" r="6"/>
          <circle cx="0" cy="45" r="6"/>
          <circle cx="-45" cy="0" r="6"/>
        </g>

        <!-- 节点内部 -->
        <g fill="#ffffff">
          <circle cx="0" cy="-45" r="3"/>
          <circle cx="45" cy="0" r="3"/>
          <circle cx="0" cy="45" r="3"/>
          <circle cx="-45" cy="0" r="3"/>
        </g>

        <!-- 动态脉冲圈 -->
        <circle class="pulse-ring" cx="0" cy="0" r="30" fill="none" stroke="url(#softPulse)"
                stroke-width="2"/>
        <circle class="pulse-ring delay" cx="0" cy="0" r="30" fill="none" stroke="url(#softPulse)"
                stroke-width="2"/>
      </g>

      <!-- 底部标识 -->
      <text x="50%" y="175" text-anchor="middle" fill="#64748b" font-size="11"
            font-family="system-ui, -apple-system, sans-serif" font-weight="500">
        智能设备
      </text>
    </svg>
  </div>
</template>

<script>
export default {
  name: 'SvgDefaultProduct',
  methods: {
    handleClick() {
      this.$emit('click')
    }
  }
}
</script>

<style scoped>
.svg-light-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.svg-light-placeholder:hover {
  transform: scale(1.02);
}

/* 柔和脉冲动画 */
@keyframes softPulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.3;
  }
  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

.pulse-ring {
  animation: softPulse 3s infinite ease-in-out;
}

.pulse-ring.delay {
  animation-delay: 1.5s;
}
</style>