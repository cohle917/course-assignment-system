<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>教师端 - 微服务教学系统</h2>
          <div>
            <span>欢迎，{{ userInfo.username }}</span>
            <el-button @click="handleLogout" type="danger" size="small">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="my-courses">我的课程</el-menu-item>
            <el-menu-item index="homeworks">作业管理</el-menu-item>
            <el-menu-item index="publish-homework">发布作业</el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main>
          <div v-if="activeMenu === 'my-courses'">
            <h3>我的课程</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="course in myCourses" :key="course.id">
                <el-card class="course-card" @click="viewCourseDetail(course.id)" style="cursor: pointer;">
                  <h4>{{ course.name }}</h4>
                  <p>{{ course.description }}</p>
                  <p>选课人数：{{ course.studentCount || 0 }}</p>
                  <div class="course-actions">
                    <el-button type="primary" size="small" @click.stop="viewCourseDetail(course.id)">
                      查看详情
                    </el-button>
                    <el-button type="success" size="small" @click.stop="viewCourseStudents(course.id)">
                      查看学生
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-if="myCourses.length === 0" description="暂无课程" />
          </div>
          
          <div v-if="activeMenu === 'homeworks'">
            <h3>作业管理</h3>
            <el-table :data="homeworks" border>
              <el-table-column prop="title" label="作业标题" />
              <el-table-column prop="courseName" label="课程" />
              <el-table-column prop="deadline" label="截止日期" />
              <el-table-column prop="submissionCount" label="已提交" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewSubmissions(scope.row.id)">
                    查看提交
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-if="activeMenu === 'publish-homework'">
            <h3>发布作业</h3>
            <el-form :model="homeworkForm" label-width="120px" style="max-width: 600px">
              <el-form-item label="作业标题">
                <el-input v-model="homeworkForm.title" placeholder="请输入作业标题" />
              </el-form-item>
              <el-form-item label="选择课程">
                <el-select v-model="homeworkForm.courseId" placeholder="请选择课程">
                  <el-option 
                    v-for="course in myCourses" 
                    :key="course.id" 
                    :label="course.name" 
                    :value="course.id" 
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="截止日期">
                <el-date-picker 
                  v-model="homeworkForm.deadline" 
                  type="datetime" 
                  placeholder="选择截止日期"
                />
              </el-form-item>
              <el-form-item label="作业描述">
                <el-input 
                  v-model="homeworkForm.description" 
                  type="textarea" 
                  :rows="4"
                  placeholder="请输入作业描述"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handlePublishHomework">发布作业</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <el-dialog v-model="studentsDialogVisible" title="课程学生列表" width="600px">
      <el-table :data="courseStudents" border>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
      </el-table>
    </el-dialog>
    
    <el-dialog v-model="submissionsDialogVisible" title="作业提交列表" width="800px">
      <el-table :data="homeworkSubmissions" border>
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="content" label="提交内容" />
        <el-table-column prop="submitTime" label="提交时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" size="small">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import api from '../api'

export default {
  name: 'TeacherDashboard',
  data() {
    return {
      userInfo: {},
      activeMenu: 'my-courses',
      myCourses: [],
      homeworks: [],
      homeworkForm: {
        title: '',
        courseId: null,
        deadline: '',
        description: ''
      },
      studentsDialogVisible: false,
      courseStudents: [],
      submissionsDialogVisible: false,
      homeworkSubmissions: []
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
      if (index === 'my-courses') {
        this.loadMyCourses()
      } else if (index === 'homeworks') {
        this.loadHomeworks()
      }
    },
    
    async loadMyCourses() {
      try {
        const result = await api.getTeacherCourses()
        this.myCourses = result.data || []
      } catch (error) {
        console.error('加载课程失败:', error)
        ElMessage.error('加载课程失败: ' + (error.message || '未知错误'))
        this.myCourses = []
      }
    },
    
    async loadHomeworks() {
      try {
        const result = await api.getHomeworks()
        this.homeworks = result.data || []
      } catch (error) {
        ElMessage.error('加载作业失败: ' + (error.message || '未知错误'))
      }
    },
    
    async handlePublishHomework() {
      if (!this.homeworkForm.title || !this.homeworkForm.courseId || !this.homeworkForm.deadline) {
        ElMessage.warning('请填写完整信息')
        return
      }
      
      try {
        await api.publishHomework(this.homeworkForm)
        ElMessage.success('发布成功')
        this.homeworkForm = {
          title: '',
          courseId: null,
          deadline: '',
          description: ''
        }
        this.activeMenu = 'homeworks'
        this.loadHomeworks()
      } catch (error) {
        ElMessage.error('发布失败: ' + (error.message || '未知错误'))
      }
    },
    
    async viewCourseStudents(courseId) {
      try {
        const result = await api.getCourseStudents(courseId)
        this.courseStudents = result.data || []
        this.studentsDialogVisible = true
      } catch (error) {
        ElMessage.error('加载学生列表失败: ' + (error.message || '未知错误'))
      }
    },
    
    async viewSubmissions(homeworkId) {
      try {
        const result = await api.getHomeworkSubmissions(homeworkId)
        this.homeworkSubmissions = result.data || []
        this.submissionsDialogVisible = true
      } catch (error) {
        ElMessage.error('加载提交列表失败: ' + (error.message || '未知错误'))
      }
    },
    
    viewCourseDetail(courseId) {
      this.$router.push(`/teacher/course/${courseId}`)
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

.course-card {
  margin-bottom: 20px;
  transition: box-shadow 0.3s;
}

.course-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

h3 {
  margin-bottom: 20px;
}
</style>
