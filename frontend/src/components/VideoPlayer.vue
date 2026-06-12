<template>
  <div class="video-player">
    <div class="video-stage">
      <video
        v-if="playMode === 'native'"
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
      <iframe
        v-else-if="playMode === 'embed'"
        class="video-element"
        :src="embedUrl"
        title="视频播放"
        allow="autoplay; fullscreen; picture-in-picture"
        allowfullscreen
        referrerpolicy="no-referrer-when-downgrade"
      />
      <div v-else class="external-video">
        <div class="external-video__content">
          <p class="external-video__title">该链接不支持站内播放</p>
          <p class="external-video__desc">请在新窗口打开视频页面观看。</p>
          <el-button type="primary" @click="openExternal">新窗口打开</el-button>
        </div>
      </div>
      <div v-if="playMode === 'native' && danmakuEnabled" class="danmaku-layer">
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

    <div v-if="playMode === 'native'" class="video-controls">
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
    <div v-else-if="playMode === 'embed'" class="embed-tip">
      如果页面无法加载，说明该平台限制站内嵌入。
      <button type="button" class="embed-tip__link" @click="openExternal">新窗口打开</button>
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
  emits: ['ready', 'timeupdate', 'pause', 'ended', 'progress-save', 'send-danmaku', 'error', 'modechange'],
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
    playInfo() {
      return this.resolvePlayInfo(this.src)
    },
    playMode() {
      return this.playInfo.mode
    },
    embedUrl() {
      return this.playInfo.embedUrl
    },
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
      this.resetPlaybackState()
    },
    initialTime(value) {
      this.applyInitialTime(value)
    },
    playMode: {
      immediate: true,
      handler(mode) {
        this.$emit('modechange', { mode })
      }
    }
  },
  mounted() {
    this.resetPlaybackState()
  },
  beforeUnmount() {
    if (this.playMode === 'native') this.emitProgressSave()
  },
  methods: {
    resetPlaybackState() {
      this.playing = false
      this.currentTime = 0
      this.duration = 0
      this.lastProgressEmitAt = 0
      this.$nextTick(() => {
        if (this.playMode === 'native' && this.$refs.videoRef) {
          this.$refs.videoRef.load()
        }
      })
    },
    resolvePlayInfo(value) {
      const rawUrl = (value || '').trim()
      if (!rawUrl) return { mode: 'external', embedUrl: '' }

      let parsed
      try {
        parsed = new URL(rawUrl)
      } catch (error) {
        return { mode: 'external', embedUrl: '' }
      }

      if (!['http:', 'https:'].includes(parsed.protocol)) {
        return { mode: 'external', embedUrl: '' }
      }

      if (this.isDirectVideoUrl(parsed)) {
        return { mode: 'native', embedUrl: '' }
      }

      const platformEmbedUrl = this.getPlatformEmbedUrl(parsed)
      if (platformEmbedUrl) {
        return { mode: 'embed', embedUrl: platformEmbedUrl }
      }

      return { mode: 'embed', embedUrl: rawUrl }
    },
    isDirectVideoUrl(url) {
      return /\.(mp4|webm|ogg|ogv|mov)(?:$|[?#])/i.test(url.pathname)
    },
    getPlatformEmbedUrl(url) {
      const host = url.hostname.toLowerCase().replace(/^www\./, '')
      const path = url.pathname

      if (host === 'bilibili.com' || host.endsWith('.bilibili.com')) {
        const bvid = path.match(/\/video\/(BV[\w]+)/i)?.[1] || url.searchParams.get('bvid')
        const page = url.searchParams.get('p') || '1'
        if (bvid) {
          return `https://player.bilibili.com/player.html?bvid=${encodeURIComponent(bvid)}&page=${encodeURIComponent(page)}&autoplay=0`
        }
      }

      if (host === 'youtube.com' || host.endsWith('.youtube.com') || host === 'youtu.be') {
        const videoId = host === 'youtu.be'
          ? path.split('/').filter(Boolean)[0]
          : url.searchParams.get('v') || path.match(/\/embed\/([^/?#]+)/)?.[1]
        if (videoId) {
          return `https://www.youtube.com/embed/${encodeURIComponent(videoId)}`
        }
      }

      if (host === 'v.qq.com' || host.endsWith('.v.qq.com')) {
        const vid = path.match(/\/x\/page\/([^/.]+)\.html/i)?.[1] || url.searchParams.get('vid')
        if (vid) {
          return `https://v.qq.com/txp/iframe/player.html?vid=${encodeURIComponent(vid)}`
        }
      }

      return ''
    },
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
      if (this.playMode !== 'native') return
      this.playing = false
      this.emitProgressSave()
      this.$emit('pause', { currentTime: this.currentTime, duration: this.duration })
    },
    emitProgressSave() {
      if (this.playMode !== 'native') return
      this.lastProgressEmitAt = Date.now()
      this.$emit('progress-save', { currentTime: this.currentTime, duration: this.duration })
    },
    handleEnded() {
      if (this.playMode !== 'native') return
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
      this.$emit('error', '当前视频直链无法播放，请检查文件格式、跨域权限或视频地址。')
    },
    openExternal() {
      const url = (this.src || '').trim()
      if (url) window.open(url, '_blank', 'noopener,noreferrer')
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
  border: 0;
  object-fit: contain;
}

.external-video {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  padding: 24px;
}

.external-video__content {
  max-width: 360px;
  text-align: center;
}

.external-video__title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.external-video__desc {
  margin: 0 0 18px;
  color: #d1d5db;
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

.embed-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  gap: 8px;
  padding: 8px 12px;
  color: #d1d5db;
  background: #1f2937;
  font-size: 13px;
}

.embed-tip__link {
  padding: 0;
  border: 0;
  color: #93c5fd;
  background: transparent;
  cursor: pointer;
}

.embed-tip__link:hover {
  color: #bfdbfe;
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

  .embed-tip {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
