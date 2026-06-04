<template>
  <div class="course-detail">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="header-left">
            <el-button @click="goBack" type="primary" size="small">返回</el-button>
            <h2>课程详情</h2>
          </div>
          <div>
            <span>欢迎，{{ userInfo.username }}</span>
            <el-button @click="handleLogout" type="danger" size="small">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main v-loading="loading">
        <el-card v-if="course">
          <template #header>
            <div class="card-header">
              <h3>{{ course.name }}</h3>
              <el-tag v-if="course.status === 'open'" type="success">进行中</el-tag>
              <el-tag v-else type="info">已结束</el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="课程代码">{{ course.code || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="授课教师">{{ course.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="学分">{{ course.credit }}</el-descriptions-item>
            <el-descriptions-item label="选课人数">{{ course.currentStudents }}/{{ course.maxStudents }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ course.semester || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="课程状态">
              <el-tag :type="course.status === 'open' ? 'success' : 'info'">
                {{ course.status === 'open' ? '开放选课' : '已关闭' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="course-description">
            <h4>课程简介</h4>
            <p>{{ course.description || '暂无课程简介' }}</p>
          </div>
        </el-card>
        
        <!-- 学生端：显示作业列表 -->
        <el-card v-if="isStudent" class="homework-card">
          <template #header>
            <h4>课程作业</h4>
          </template>
          
          <el-table :data="courseHomeworks" border>
            <el-table-column prop="title" label="作业标题" />
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
          
          <el-empty v-if="courseHomeworks.length === 0" description="暂无作业" />
        </el-card>
        
        <!-- 教师端：显示选课学生列表 -->
        <el-card v-if="isTeacher" class="students-card">
          <template #header>
            <div class="card-header">
              <h4>选课学生</h4>
              <span class="student-count">共 {{ courseStudents.length }} 人</span>
            </div>
          </template>
          
          <el-table :data="courseStudents" border>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewStudentHomeworks(scope.row)">
                  查看作业
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="courseStudents.length === 0" description="暂无选课学生" />
        </el-card>
        
        <!-- 评论区 -->
        <el-card class="comments-card">
          <template #header>
            <div class="comments-header">
              <h4>课程讨论区</h4>
              <span class="comment-count">共 {{ comments.length }} 条讨论</span>
            </div>
          </template>
          
          <!-- 发帖区域 -->
          <div class="comment-input-area">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="有什么问题想和大家讨论？"
            />
            <div class="comment-actions">
              <el-button type="primary" @click="handlePostComment" :disabled="!newComment.trim()">
                发布
              </el-button>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-main">
                <div class="comment-avatar">
                  <el-tag :type="getRoleTagType(comment.userRole)" size="small">
                    {{ comment.userRole === 'teacher' ? '教师' : comment.userRole === 'admin' ? '管理员' : '学生' }}
                  </el-tag>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.userName }}</span>
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-actions-row">
                    <el-button type="primary" link size="small" @click="showReplyInput(comment)">
                      回复 ({{ comment.replyCount || 0 }})
                    </el-button>
                    <el-button 
                      v-if="comment.userId == userInfo.id" 
                      type="danger" 
                      link 
                      size="small"
                      @click="handleDeleteComment(comment.id)"
                    >
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div v-if="replyingTo === comment.id" class="reply-input-area">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 @${comment.userName}：`"
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button type="primary" size="small" @click="handleReplyComment(comment.id)" :disabled="!replyContent.trim()">
                        发布回复
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 回复列表 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                    <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                      <div class="reply-avatar">
                        <el-tag :type="getRoleTagType(reply.userRole)" size="small">
                          {{ reply.userRole === 'teacher' ? '教师' : reply.userRole === 'admin' ? '管理员' : '学生' }}
                        </el-tag>
                      </div>
                      <div class="reply-content">
                        <div class="comment-header">
                          <span class="comment-author">{{ reply.userName }}</span>
                          <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                        </div>
                        <div class="comment-text">{{ reply.content }}</div>
                        <div class="comment-actions-row">
                          <el-button 
                            v-if="reply.userId == userInfo.id" 
                            type="danger" 
                            link 
                            size="small"
                            @click="handleDeleteComment(reply.id)"
                          >
                            删除
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <el-empty v-if="comments.length === 0" description="暂无讨论，快来发起第一个话题吧！" />
          </div>
        </el-card>
      </el-main>
    </el-container>
    
    <!-- 提交作业对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交作业" width="500px">
      <el-form :model="submitForm">
        <el-form-item label="作业标题">
          <span>{{ submitForm.title }}</span>
        </el-form-item>
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
  name: 'CourseDetail',
  data() {
    return {
      loading: false,
      userInfo: {},
      course: null,
      courseHomeworks: [],
      courseStudents: [],
      submitDialogVisible: false,
      submitForm: {
        homeworkId: null,
        title: '',
        content: ''
      },
      // 评论相关
      comments: [],
      newComment: '',
      replyContent: '',
      replyingTo: null
    }
  },
  computed: {
    isStudent() {
      return this.userInfo.role === 'student'
    },
    isTeacher() {
      return this.userInfo.role === 'teacher'
    },
    courseId() {
      return this.$route.params.courseId
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadCourseDetail()
    if (this.isTeacher) {
      this.loadCourseStudents()
    }
    if (this.isStudent) {
      this.loadCourseHomeworks()
    }
    this.loadComments()
  },
  methods: {
    loadUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        this.userInfo = JSON.parse(userInfoStr)
      }
    },
    
    async loadCourseDetail() {
      this.loading = true
      try {
        const result = await api.getCourses()
        const courses = result.data || []
        this.course = courses.find(c => c.id == this.courseId)
        if (!this.course) {
          ElMessage.error('课程不存在')
          this.$router.push(this.isTeacher ? '/teacher' : '/student')
        }
      } catch (error) {
        console.error('加载课程详情失败:', error)
        ElMessage.error('加载课程详情失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadCourseStudents() {
      try {
        const result = await api.getCourseStudents(this.courseId)
        this.courseStudents = result.data || []
      } catch (error) {
        console.error('加载学生列表失败:', error)
        ElMessage.error('加载学生列表失败')
      }
    },
    
    async loadCourseHomeworks() {
      try {
        const result = await api.getMyHomeworks()
        const allHomeworks = result.data || []
        // 过滤出属于当前课程的作业
        this.courseHomeworks = allHomeworks.filter(h => h.courseName === this.course?.name)
      } catch (error) {
        console.error('加载作业列表失败:', error)
        ElMessage.error('加载作业列表失败')
      }
    },
    
    showSubmitDialog(homework) {
      this.submitForm.homeworkId = homework.id
      this.submitForm.title = homework.title
      this.submitForm.content = ''
      this.submitDialogVisible = true
    },
    
    async handleSubmitHomework() {
      try {
        await api.submitHomework(this.submitForm.homeworkId, this.submitForm.content)
        ElMessage.success('提交成功')
        this.submitDialogVisible = false
        this.loadCourseHomeworks()
      } catch (error) {
        ElMessage.error('提交失败: ' + (error.message || '未知错误'))
      }
    },
    
    viewSubmission(homework) {
      ElMessage.info('查看作业功能')
    },
    
    viewStudentHomeworks(student) {
      ElMessage.info('查看学生作业功能')
    },
    
    // ========== 评论相关方法 ==========
    
    async loadComments() {
      try {
        const result = await api.getCourseComments(this.courseId)
        this.comments = result.data || []
      } catch (error) {
        console.error('加载评论失败:', error)
        ElMessage.error('加载评论失败')
      }
    },
    
    async handlePostComment() {
      if (!this.newComment.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      
      try {
        const commentData = {
          userId: this.userInfo.id,
          username: this.userInfo.username,
          userName: this.userInfo.name || this.userInfo.username,
          userRole: this.userInfo.role,
          content: this.newComment.trim()
        }
        
        await api.addComment(this.courseId, commentData)
        ElMessage.success('发布成功')
        this.newComment = ''
        this.loadComments()
      } catch (error) {
        ElMessage.error('发布失败: ' + (error.message || '未知错误'))
      }
    },
    
    showReplyInput(comment) {
      if (this.replyingTo === comment.id) {
        this.replyingTo = null
        this.replyContent = ''
      } else {
        this.replyingTo = comment.id
        this.replyContent = ''
      }
    },
    
    cancelReply() {
      this.replyingTo = null
      this.replyContent = ''
    },
    
    async handleReplyComment(parentId) {
      if (!this.replyContent.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }
      
      try {
        const commentData = {
          userId: this.userInfo.id,
          username: this.userInfo.username,
          userName: this.userInfo.name || this.userInfo.username,
          userRole: this.userInfo.role,
          parentId: parentId,
          content: this.replyContent.trim()
        }
        
        await api.addComment(this.courseId, commentData)
        ElMessage.success('回复成功')
        this.replyContent = ''
        this.replyingTo = null
        this.loadComments()
      } catch (error) {
        ElMessage.error('回复失败: ' + (error.message || '未知错误'))
      }
    },
    
    async handleDeleteComment(commentId) {
      try {
        await this.$confirm('确定要删除这条评论吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await api.deleteComment(commentId)
        ElMessage.success('删除成功')
        this.loadComments()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
    },
    
    getRoleTagType(role) {
      const typeMap = {
        'admin': 'danger',
        'teacher': 'success',
        'student': 'primary'
      }
      return typeMap[role] || 'info'
    },
    
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      const seconds = Math.floor(diff / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      const days = Math.floor(hours / 24)
      
      if (days > 0) return `${days}天前`
      if (hours > 0) return `${hours}小时前`
      if (minutes > 0) return `${minutes}分钟前`
      return '刚刚'
    },
    
    goBack() {
      this.$router.back()
    },
    
    handleLogout() {
      localStorage.removeItem('userInfo')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.course-detail {
  height: 100vh;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-header {
  background-color: #409EFF;
  color: white;
}

.el-main {
  padding: 20px;
  background-color: #f5f5f5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.course-description {
  margin-top: 20px;
}

.course-description h4 {
  margin-bottom: 10px;
}

.homework-card,
.students-card {
  margin-top: 20px;
}

.student-count {
  color: #909399;
}

.el-form {
  padding: 10px 0;
}

/* ========== 评论区样式 ========== */
.comments-card {
  margin-top: 20px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comments-header h4 {
  margin: 0;
}

.comment-count {
  color: #909399;
  font-size: 14px;
}

.comment-input-area {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions-row {
  display: flex;
  gap: 15px;
  align-items: center;
}

.reply-input-area {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.reply-actions {
  margin-top: 10px;
  text-align: right;
}

.replies-list {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-avatar {
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
  min-width: 0;
}
</style>
