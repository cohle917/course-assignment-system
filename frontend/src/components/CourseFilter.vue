<template>
  <div class="course-filter">
    <el-input
      v-model="localFilters.keyword"
      placeholder="搜索课程名称或描述..."
      :prefix-icon="Search"
      clearable
      class="search-input"
      @input="onKeywordInput"
      @clear="emitFilter"
    />

    <div class="filter-row">
      <el-select
        v-model="localFilters.category"
        placeholder="全部分类"
        clearable
        @change="emitFilter"
      >
        <el-option
          v-for="cat in categories"
          :key="cat"
          :label="cat"
          :value="cat"
        />
      </el-select>

      <el-select
        v-model="localFilters.department"
        placeholder="全部院系"
        clearable
        @change="emitFilter"
      >
        <el-option
          v-for="dept in departments"
          :key="dept"
          :label="dept"
          :value="dept"
        />
      </el-select>

      <el-select
        v-model="localFilters.semester"
        placeholder="全部学期"
        clearable
        @change="emitFilter"
      >
        <el-option
          v-for="sem in semesters"
          :key="sem"
          :label="sem"
          :value="sem"
        />
      </el-select>

      <el-select
        v-model="localFilters.sortBy"
        @change="emitFilter"
      >
        <el-option label="默认排序" value="default" />
        <el-option label="🔥 热度优先" value="popularity" />
        <el-option label="🕐 最新发布" value="newest" />
      </el-select>
    </div>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'CourseFilter',
  props: {
    filters: { type: Object, default: () => ({ keyword: '', category: '', department: '', semester: '', sortBy: 'default' }) },
    categories: { type: Array, default: () => [] },
    departments: { type: Array, default: () => [] },
    semesters: { type: Array, default: () => [] }
  },
  emits: ['filter-change'],
  data() {
    return {
      Search,
      localFilters: { ...this.filters },
      debounceTimer: null
    }
  },
  watch: {
    filters: {
      handler(val) {
        this.localFilters = { ...val }
      },
      deep: true
    }
  },
  methods: {
    onKeywordInput() {
      if (this.debounceTimer) clearTimeout(this.debounceTimer)
      this.debounceTimer = setTimeout(() => {
        this.emitFilter()
      }, 300)
    },
    emitFilter() {
      this.$emit('filter-change', { ...this.localFilters })
    }
  }
}
</script>

<style scoped>
.course-filter {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
.search-input {
  margin-bottom: 12px;
  max-width: 500px;
}
.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-row .el-select {
  width: 160px;
}
</style>
