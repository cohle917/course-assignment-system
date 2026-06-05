<template>
  <span class="star-rating" :class="{ readonly: readonly }">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="{ active: star <= displayValue, 'half-active': star === Math.ceil(displayValue) && displayValue % 1 !== 0 }"
      :style="{ fontSize: size + 'px', cursor: readonly ? 'default' : 'pointer' }"
      @click="handleClick(star)"
      @mousemove="handleHover($event, star)"
      @mouseleave="handleLeave"
    >
      <template v-if="star <= displayValue">
        ★
      </template>
      <template v-else-if="star === Math.ceil(displayValue) && displayValue % 1 !== 0">
        <span class="half">★</span>
      </template>
      <template v-else>
        ☆
      </template>
    </span>
    <span v-if="showText" class="rating-text">{{ displayValue > 0 ? displayValue.toFixed(1) : '暂无评分' }}</span>
  </span>
</template>

<script>
export default {
  name: 'StarRating',
  props: {
    modelValue: { type: Number, default: 0 },
    readonly: { type: Boolean, default: false },
    size: { type: [Number, String], default: 18 },
    showText: { type: Boolean, default: false }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      hoverValue: 0
    }
  },
  computed: {
    displayValue() {
      if (this.readonly) return this.modelValue
      return this.hoverValue || this.modelValue
    }
  },
  methods: {
    handleClick(star) {
      if (this.readonly) return
      this.$emit('update:modelValue', star)
    },
    handleHover(event, star) {
      if (this.readonly) return
      const rect = event.target.getBoundingClientRect()
      const x = event.clientX - rect.left
      const half = x < rect.width / 2
      this.hoverValue = half ? star - 0.5 : star
    },
    handleLeave() {
      this.hoverValue = 0
    }
  }
}
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.star {
  color: #ddd;
  transition: color 0.15s;
  user-select: none;
}
.star.active {
  color: #f7ba2a;
}
.star.half-active {
  position: relative;
}
.star.half-active .half {
  color: #ddd;
}
.rating-text {
  margin-left: 6px;
  font-size: 13px;
  color: #909399;
}
</style>
