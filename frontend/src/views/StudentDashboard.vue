<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>学生端 - 微服务教学系统</h2>
          <div>
            <span>欢迎，{{ userInfo.username }}</span>
            <el-button @click="handleLogout" type="danger" size="small">退出</el-button>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="courses">我的课程</el-menu-item>
            <el-menu-item index="homeworks">我的作业</el-menu-item>
            <el-menu-item index="all-courses">选课</el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <!-- 我的课程 -->
          <div v-if="activeMenu === 'courses'">
            <h3>我的课程</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="course in myCourses" :key="course.id">
                <CourseCard :course="course" />
              </el-col>
            </el-row>
            <el-empty v-if="myCourses.length === 0" description="暂无已选课程" />
          </div>

          <!-- 我的作业 -->
          <div v-if="activeMenu === 'homeworks'">
            <h3>我的作业</h3>
            <el-table :data="myHomeworks" border>
              <el-table-column prop="title" label="作业标题" />
              <el-table-column prop="courseName" label="课程" />
              <el-table-column prop="deadline" label="截止日期" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '已完成' ? 'success' : 'warning'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.status !== '已完成'"
                    type="primary"
                    size="small"
                    @click="showSubmitDialog(scope.row)"
                  >
                    提交作业
                  </el-button>
                  <el-button
                    v-else
                    type="info"
                    size="small"
                    @click="viewSubmission(scope.row)"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 选课 -->
          <div v-if="activeMenu === 'all-courses'">
            <h3>可选课程</h3>
            <CourseFilter
              :filters="filters"
              :categories="filterOptions.categories"
              :departments="filterOptions.departments"
              :semesters="filterOptions.semesters"
              @filter-change="handleFilterChange"
            />
            <el-row :gutter="20">
              <el-col :span="8" v-for="course in allCourses" :key="course.id">
                <CourseCard
                  :course="course"
                  :showSelect="true"
                  :isSelected="selectedCourseIds.has(course.id)"
                  @select="handleSelectCourse"
                />
              </el-col>
            </el-row>
            <el-empty v-if="allCourses.length === 0" description="没有找到匹配的课程" />
          </div>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="submitDialogVisible" title="提交作业" width="500px">
      <el-form :model="submitForm">
        <el-form-item label="作业内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入作业内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitHomework">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import api from '../api'
import CourseCard from '../components/CourseCard.vue'
import CourseFilter from '../components/CourseFilter.vue'

export default {
  name: 'StudentDashboard',
  components: { CourseCard, CourseFilter },
  data() {
    return {
      userInfo: {},
      activeMenu: 'courses',
      myCourses: [],
      allCourses: [],
      myHomeworks: [],
      selectedCourseIds: new Set(),
      filters: {
        keyword: '',
        category: '',
        department: '',
        semester: '',
        sortBy: 'default'
      },
      filterOptions: {
        categories: [],
        departments: [],
        semesters: []
      },
      submitDialogVisible: false,
      submitForm: {
        homeworkId: null,
        content: ''
      }
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadMyCourses()
  },
  methods: {
    loadUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        this.userInfo = JSON.parse(userInfoStr)
      }
    },

    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'courses') {
        this.loadMyCourses()
      } else if (index === 'homeworks') {
        this.loadMyHomeworks()
      } else if (index === 'all-courses') {
        this.loadAllCourses()
        this.loadFilterOptions()
      }
    },

    async loadMyCourses() {
      try {
        const result = await api.getMyCourses()
        this.myCourses = result.data || []
        this.selectedCourseIds = new Set(this.myCourses.map(c => c.id))
      } catch (error) {
        console.error('加载课程失败:', error)
        ElMessage.error('加载课程失败: ' + (error.message || '未知错误'))
        this.myCourses = []
      }
    },

    async loadAllCourses() {
      try {
        const params = {}
        if (this.filters.keyword) params.keyword = this.filters.keyword
        if (this.filters.category) params.category = this.filters.category
        if (this.filters.department) params.department = this.filters.department
        if (this.filters.semester) params.semester = this.filters.semester
        if (this.filters.sortBy) params.sortBy = this.filters.sortBy

        const result = await api.getCourses(params)
        this.allCourses = result.data || []
      } catch (error) {
        console.error('加载课程失败:', error)
        ElMessage.error('加载课程失败: ' + (error.message || '未知错误'))
        this.allCourses = []
      }
    },

    async loadFilterOptions() {
      try {
        const [catRes, deptRes, semRes] = await Promise.all([
          api.getCourseCategories(),
          api.getCourseDepartments(),
          api.getCourseSemesters()
        ])
        this.filterOptions.categories = catRes.data || []
        this.filterOptions.departments = deptRes.data || []
        this.filterOptions.semesters = semRes.data || []
      } catch (error) {
        console.error('加载筛选选项失败:', error)
      }
    },

    handleFilterChange(newFilters) {
      this.filters = newFilters
      this.loadAllCourses()
    },

    async handleSelectCourse(courseId) {
      try {
        await api.selectCourse(courseId)
        ElMessage.success('选课成功')
        this.selectedCourseIds.add(courseId)
        this.loadMyCourses()
        this.loadAllCourses()
      } catch (error) {
        ElMessage.error('选课失败: ' + (error.message || '未知错误'))
      }
    },

    async loadMyHomeworks() {
      try {
        const result = await api.getMyHomeworks()
        this.myHomeworks = result.data || []
      } catch (error) {
        ElMessage.error('加载作业失败: ' + (error.message || '未知错误'))
        this.myHomeworks = []
      }
    },

    showSubmitDialog(homework) {
      this.submitForm.homeworkId = homework.id
      this.submitForm.content = ''
      this.submitDialogVisible = true
    },

    async handleSubmitHomework() {
      try {
        await api.submitHomework(this.submitForm.homeworkId, this.submitForm.content)
        ElMessage.success('提交成功')
        this.submitDialogVisible = false
        this.loadMyHomeworks()
      } catch (error) {
        ElMessage.error('提交失败: ' + (error.message || '未知错误'))
      }
    },

    viewSubmission() {
      ElMessage.info('查看作业功能')
    },

    handleLogout() {
      localStorage.removeItem('userInfo')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.dashboard {
  height: 100vh;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.el-header {
  background-color: #409EFF;
  color: white;
}

.el-aside {
  background-color: #f5f5f5;
  padding-top: 20px;
}

h3 {
  margin-bottom: 20px;
}
</style>
