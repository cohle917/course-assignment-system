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
          <div v-if="activeMenu === 'courses'">
            <h3>我的课程</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="course in myCourses" :key="course.id">
                <el-card class="course-card">
                  <h4>{{ course.name }}</h4>
                  <p>教师：{{ course.teacherName }}</p>
                  <p>{{ course.description }}</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
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
          
          <div v-if="activeMenu === 'all-courses'">
            <h3>可选课程</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="course in allCourses" :key="course.id">
                <el-card class="course-card">
                  <h4>{{ course.name }}</h4>
                  <p>教师：{{ course.teacherName }}</p>
                  <p>{{ course.description }}</p>
                  <el-button 
                    v-if="!course.selected" 
                    type="primary" 
                    @click="handleSelectCourse(course.id)"
                  >
                    选课
                  </el-button>
                  <el-tag v-else type="success">已选</el-tag>
                </el-card>
              </el-col>
            </el-row>
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

export default {
  name: 'StudentDashboard',
  data() {
    return {
      userInfo: {},
      activeMenu: 'courses',
      myCourses: [],
      allCourses: [],
      myHomeworks: [],
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
      }
    },
    
    async loadMyCourses() {
      try {
        const response = await api.getMyCourses()
        this.myCourses = response.data
      } catch (error) {
        ElMessage.error('加载课程失败')
      }
    },
    
    async loadAllCourses() {
      try {
        const response = await api.getCourses()
        this.allCourses = response.data.map(course => ({
          ...course,
          selected: course.selected || false
        }))
      } catch (error) {
        ElMessage.error('加载课程失败')
      }
    },
    
    async handleSelectCourse(courseId) {
      try {
        await api.selectCourse(courseId)
        ElMessage.success('选课成功')
        this.loadAllCourses()
        this.loadMyCourses()
      } catch (error) {
        ElMessage.error('选课失败')
      }
    },
    
    async loadMyHomeworks() {
      try {
        const response = await api.getMyHomeworks()
        this.myHomeworks = response.data
      } catch (error) {
        ElMessage.error('加载作业失败')
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
        ElMessage.error('提交失败')
      }
    },
    
    viewSubmission(homework) {
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

.course-card {
  margin-bottom: 20px;
}

h3 {
  margin-bottom: 20px;
}
</style>
