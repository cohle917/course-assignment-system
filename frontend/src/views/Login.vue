<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>微服务教学系统</h2>
      <el-form :model="loginForm" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="loginForm.role" placeholder="选择角色">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import api from '../api'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        role: 'student'
      }
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        ElMessage.warning('请输入用户名和密码')
        return
      }
      
      try {
        const result = await api.login(this.loginForm.username, this.loginForm.password, this.loginForm.role)
        
        if (!result || !result.data) {
          ElMessage.error('登录失败：返回数据异常')
          return
        }
        
        const data = result.data
        
        if (!data.token) {
          ElMessage.error('登录失败：缺少 token')
          return
        }
        
        localStorage.setItem('userInfo', JSON.stringify(data))
        
        ElMessage.success('登录成功')
        
        if (data.role === 'student') {
          this.$router.push('/student')
        } else if (data.role === 'teacher') {
          this.$router.push('/teacher')
        } else if (data.role === 'admin') {
          this.$router.push('/admin')
        }
      } catch (error) {
        ElMessage.error('登录失败：' + (typeof error === 'string' ? error : error.message || '未知错误'))
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.el-select {
  width: 100%;
}
</style>
