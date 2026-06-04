<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>管理员端 - 微服务教学系统</h2>
          <div>
            <span>欢迎，{{ userInfo.username }}</span>
            <el-button @click="handleLogout" type="danger" size="small">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="users">用户管理</el-menu-item>
            <el-menu-item index="courses">课程管理</el-menu-item>
            <el-menu-item index="statistics">数据统计</el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main>
          <div v-if="activeMenu === 'users'">
            <h3>用户管理</h3>
            <el-button type="primary" @click="showAddUserDialog" style="margin-bottom: 20px">
              添加用户
            </el-button>
            <el-table :data="users" border>
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="name" label="姓名" />
              <el-table-column prop="role" label="角色">
                <template #default="scope">
                  <el-tag :type="getRoleType(scope.row.role)">
                    {{ getRoleText(scope.row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="email" label="邮箱" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="deleteUser(scope.row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-if="activeMenu === 'courses'">
            <h3>课程管理</h3>
            <el-button type="primary" @click="showAddCourseDialog" style="margin-bottom: 20px">
              添加课程
            </el-button>
            <el-table :data="courses" border>
              <el-table-column prop="name" label="课程名称" />
              <el-table-column prop="teacherName" label="教师" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="studentCount" label="选课人数" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="deleteCourse(scope.row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-if="activeMenu === 'statistics'">
            <h3>数据统计</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <h4>用户总数</h4>
                  <p class="stat-number">{{ statistics.totalUsers }}</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <h4>课程总数</h4>
                  <p class="stat-number">{{ statistics.totalCourses }}</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card>
                  <h4>作业总数</h4>
                  <p class="stat-number">{{ statistics.totalHomeworks }}</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <el-dialog v-model="addUserDialogVisible" title="添加用户" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.name" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addUserDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser">添加</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="addCourseDialogVisible" title="添加课程" width="500px">
      <el-form :model="courseForm" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="courseForm.teacherId" placeholder="选择教师">
            <el-option 
              v-for="teacher in teachers" 
              :key="teacher.id" 
              :label="teacher.name" 
              :value="teacher.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addCourseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddCourse">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import api from '../api'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      userInfo: {},
      activeMenu: 'users',
      users: [],
      courses: [],
      teachers: [],
      statistics: {
        totalUsers: 0,
        totalCourses: 0,
        totalHomeworks: 0
      },
      addUserDialogVisible: false,
      addCourseDialogVisible: false,
      userForm: {
        username: '',
        name: '',
        password: '',
        role: 'student',
        email: ''
      },
      courseForm: {
        name: '',
        teacherId: null,
        description: ''
      }
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadUsers()
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
      if (index === 'users') {
        this.loadUsers()
      } else if (index === 'courses') {
        this.loadCourses()
      } else if (index === 'statistics') {
        this.loadStatistics()
      }
    },
    
    async loadUsers() {
      try {
        const response = await api.getUserInfo()
        this.users = response.data
      } catch (error) {
        ElMessage.error('加载用户失败')
      }
    },
    
    async loadCourses() {
      try {
        const response = await api.getCourses()
        this.courses = response.data
      } catch (error) {
        ElMessage.error('加载课程失败')
      }
    },
    
    async loadStatistics() {
      try {
        this.statistics = {
          totalUsers: this.users.length,
          totalCourses: this.courses.length,
          totalHomeworks: 0
        }
      } catch (error) {
        ElMessage.error('加载统计数据失败')
      }
    },
    
    showAddUserDialog() {
      this.userForm = {
        username: '',
        name: '',
        password: '',
        role: 'student',
        email: ''
      }
      this.addUserDialogVisible = true
    },
    
    async handleAddUser() {
      try {
        ElMessage.success('用户添加成功')
        this.addUserDialogVisible = false
        this.loadUsers()
      } catch (error) {
        ElMessage.error('添加失败')
      }
    },
    
    async deleteUser(userId) {
      try {
        await this.$confirm('确定要删除这个用户吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        ElMessage.success('删除成功')
        this.loadUsers()
      } catch (error) {
        ElMessage.info('已取消删除')
      }
    },
    
    showAddCourseDialog() {
      this.courseForm = {
        name: '',
        teacherId: null,
        description: ''
      }
      this.addCourseDialogVisible = true
    },
    
    async handleAddCourse() {
      try {
        ElMessage.success('课程添加成功')
        this.addCourseDialogVisible = false
        this.loadCourses()
      } catch (error) {
        ElMessage.error('添加失败')
      }
    },
    
    async deleteCourse(courseId) {
      try {
        await this.$confirm('确定要删除这个课程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        ElMessage.success('删除成功')
        this.loadCourses()
      } catch (error) {
        ElMessage.info('已取消删除')
      }
    },
    
    getRoleType(role) {
      const types = {
        student: '',
        teacher: 'success',
        admin: 'warning'
      }
      return types[role] || ''
    },
    
    getRoleText(role) {
      const texts = {
        student: '学生',
        teacher: '教师',
        admin: '管理员'
      }
      return texts[role] || role
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

.stat-number {
  font-size: 36px;
  text-align: center;
  color: #409EFF;
  font-weight: bold;
}
</style>
