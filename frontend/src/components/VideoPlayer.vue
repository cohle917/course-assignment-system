<template>
  <div class="video-player">
    <div class="video-stage">
      <video
        ref="videoRef"
        class="video-element"
        :src="src"
        :poster="poster"
        @loadedmetadata="handleLoadedMetadata"
        @play="playing = true"
        @timeupdate="handleTimeUpdate"
        @pause="emitPause"
        @ended="handleEnded"
        @error="handleError"
      />
      <div v-if="danmakuEnabled" class="danmaku-layer">
        <span
          v-for="item in activeDanmaku"
          :key="getDanmakuKey(item)"
          class="danmaku-item"
          :style="{ color: item.color || '#ffffff', top: item.top + 'px' }"
        >
          {{ item.content }}
        </span>
      </div>
    </div>

    <div class="video-controls">
      <el-button class="control-button" size="small" @click="togglePlay">
        {{ playing ? 'Pause' : 'Play' }}
      </el-button>

      <span class="time-text">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>

      <input
        v-model.number="currentTime"
        class="timeline"
        type="range"
        min="0"
        :max="duration || 0"
        step="1"
        aria-label="Video timeline"
        @input="seekToCurrent"
      />

      <div class="volume-control">
        <span class="volume-label">Vol</span>
        <input
          v-model.number="volume"
          class="volume-slider"
          type="range"
          min="0"
          max="1"
          step="0.05"
          aria-label="Video volume"
          @input="applyVolume"
        />
      </div>

      <el-select
        v-model="playbackRate"
        class="speed-select"
        size="small"
        @change="applyPlaybackRate"
      >
        <el-option
          v-for="rate in speedOptions"
          :key="rate"
          :label="rate + 'x'"
          :value="rate"
        />
      </el-select>

      <el-button class="control-button" size="small" @click="toggleFullscreen">
        Full
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VideoPlayer',
  props: {
    src: { type: String, required: true },
    poster: { type: String, default: '' },
    initialTime: { type: Number, default: 0 },
    danmakuList: { type: Array, default: () => [] },
    danmakuEnabled: { type: Boolean, default: true }
  },
  emits: ['ready', 'timeupdate', 'pause', 'ended', 'progress-save', 'send-danmaku', 'error'],
  data() {
    return {
      playing: false,
      currentTime: 0,
      duration: 0,
      volume: 1,
      playbackRate: 1,
      speedOptions: [0.75, 1, 1.25, 1.5, 2],
      lastProgressEmitAt: 0
    }
  },
  computed: {
    activeDanmaku() {
      const now = Math.floor(this.currentTime)
      return this.danmakuList
        .filter(item => Math.abs((Number(item.timeSeconds) || 0) - now) <= 1)
        .slice(0, 6)
        .map((item, index) => ({ ...item, top: 16 + index * 28 }))
    }
  },
  watch: {
    src() {
      this.playing = false
      this.currentTime = 0
      this.duration = 0
      this.lastProgressEmitAt = 0
      this.$nextTick(() => {
        if (this.$refs.videoRef) this.$refs.videoRef.load()
      })
    },
    initialTime(value) {
      this.applyInitialTime(value)
    }
  },
  beforeUnmount() {
    this.emitProgressSave()
  },
  methods: {
    handleLoadedMetadata() {
      const video = this.$refs.videoRef
      this.duration = Math.floor(video.duration || 0)
      this.applyInitialTime(this.initialTime)
      this.applyPlaybackRate()
      this.applyVolume()
      this.$emit('ready', { duration: this.duration })
    },
    handleTimeUpdate() {
      const video = this.$refs.videoRef
      this.currentTime = Math.floor(video.currentTime || 0)
      this.$emit('timeupdate', { currentTime: this.currentTime, duration: this.duration })
      if (Date.now() - this.lastProgressEmitAt > 15000) {
        this.emitProgressSave()
      }
    },
    emitPause() {
      this.playing = false
      this.emitProgressSave()
      this.$emit('pause', { currentTime: this.currentTime, duration: this.duration })
    },
    emitProgressSave() {
      this.lastProgressEmitAt = Date.now()
      this.$emit('progress-save', { currentTime: this.currentTime, duration: this.duration })
    },
    handleEnded() {
      this.playing = false
      this.emitProgressSave()
      this.$emit('ended', { currentTime: this.currentTime, duration: this.duration })
    },
    async togglePlay() {
      const video = this.$refs.videoRef
      if (!video) return

      if (video.paused) {
        try {
          await video.play()
          this.playing = true
        } catch (error) {
          this.playing = false
          this.$emit('error', 'Video playback could not start')
        }
      } else {
        video.pause()
        this.playing = false
      }
    },
    seekToCurrent() {
      const video = this.$refs.videoRef
      if (video) video.currentTime = this.currentTime
    },
    applyInitialTime(value) {
      const video = this.$refs.videoRef
      const target = Number(value) || 0
      if (!video || !this.duration || target <= 0 || target >= this.duration) return
      if (Math.abs(video.currentTime - target) > 1) {
        video.currentTime = target
        this.currentTime = Math.floor(target)
      }
    },
    applyPlaybackRate() {
      const video = this.$refs.videoRef
      if (video) video.playbackRate = this.playbackRate
    },
    applyVolume() {
      const video = this.$refs.videoRef
      if (video) video.volume = this.volume
    },
    toggleFullscreen() {
      const root = this.$el
      if (document.fullscreenElement) {
        document.exitFullscreen()
      } else if (root.requestFullscreen) {
        root.requestFullscreen()
      }
    },
    handleError() {
      this.playing = false
      this.$emit('error', 'Video cannot be played. Please check the video URL.')
    },
    getDanmakuKey(item) {
      return `${item.id || item.content}-${item.createdAt || item.timeSeconds}`
    },
    formatTime(seconds) {
      const value = Math.max(0, Math.floor(seconds || 0))
      const min = String(Math.floor(value / 60)).padStart(2, '0')
      const sec = String(value % 60).padStart(2, '0')
      return `${min}:${sec}`
    }
  }
}
</script>

<style scoped>
.video-player {
  width: 100%;
  overflow: hidden;
  color: #fff;
  background: #111827;
  border-radius: 6px;
}

.video-stage {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #000;
}

.video-element {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.danmaku-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.danmaku-item {
  position: absolute;
  right: -20%;
  white-space: nowrap;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  animation: danmaku-move 7s linear forwards;
}

.video-controls {
  display: grid;
  grid-template-columns: auto auto minmax(120px, 1fr) 120px 96px auto;
  gap: 10px;
  align-items: center;
  min-height: 48px;
  padding: 8px 10px;
  background: #1f2937;
}

.control-button {
  min-width: 56px;
}

.time-text {
  min-width: 96px;
  font-size: 13px;
  color: #d1d5db;
}

.timeline,
.volume-slider {
  width: 100%;
  accent-color: #409eff;
}

.volume-control {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px;
  align-items: center;
  min-width: 0;
}

.volume-label {
  font-size: 12px;
  color: #d1d5db;
}

.speed-select {
  width: 96px;
}

@keyframes danmaku-move {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-140%);
  }
}

@media (max-width: 700px) {
  .video-controls {
    grid-template-columns: auto minmax(120px, 1fr) 86px auto;
  }

  .time-text,
  .volume-control {
    display: none;
  }

  .speed-select {
    width: 86px;
  }
}
</style>
