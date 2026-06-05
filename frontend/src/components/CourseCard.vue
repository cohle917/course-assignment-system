<template>
  <el-card class="course-card" shadow="hover" @click="goDetail">
    <div class="card-cover">
      <img v-if="course.coverImage" :src="course.coverImage" alt="封面" />
      <div v-else class="cover-placeholder">
        <span>{{ course.name?.charAt(0) || '课' }}</span>
      </div>
    </div>

    <div class="card-body">
      <div class="card-tags">
        <el-tag v-if="course.category" size="small" type="warning">{{ course.category }}</el-tag>
        <el-tag v-if="course.department" size="small" type="info">{{ course.department }}</el-tag>
      </div>

      <h4 class="course-name">{{ course.name }}</h4>
      <p class="teacher-name">👨‍🏫 {{ course.teacherName }}</p>

      <div class="course-meta">
        <span>{{ course.credit }} 学分</span>
        <span>{{ course.currentStudents || 0 }}/{{ course.maxStudents }} 人</span>
      </div>

      <div class="card-footer">
        <StarRating :modelValue="course.avgRating || 0" :readonly="true" :size="14" showText />
        <el-button
          v-if="showSelect && !isSelected"
          type="primary"
          size="small"
          @click.stop="$emit('select', course.id)"
        >
          选课
        </el-button>
        <el-tag v-else-if="showSelect && isSelected" type="success" size="small">已选</el-tag>
      </div>
    </div>
  </el-card>
</template>

<script>
import StarRating from './StarRating.vue'

export default {
  name: 'CourseCard',
  components: { StarRating },
  props: {
    course: { type: Object, required: true },
    showSelect: { type: Boolean, default: false },
    isSelected: { type: Boolean, default: false }
  },
  emits: ['select'],
  methods: {
    goDetail() {
      const role = JSON.parse(localStorage.getItem('userInfo') || '{}').role
      const prefix = role === 'teacher' ? '/teacher' : '/student'
      this.$router.push(`${prefix}/course/${this.course.id}`)
    }
  }
}
</script>

<style scoped>
.course-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
}
.course-card:hover {
  transform: translateY(-4px);
}
.card-cover {
  height: 120px;
  overflow: hidden;
  margin: -20px -20px 12px -20px;
  border-radius: 4px 4px 0 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-placeholder span {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: bold;
}
.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}
.course-name {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #303133;
}
.teacher-name {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #909399;
}
.course-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
