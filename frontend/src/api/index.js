import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use(
  config => {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        if (user && user.token) {
          config.headers.Authorization = `Bearer ${user.token}`
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
        localStorage.removeItem('userInfo')
      }
    }
    return config
  },
  error => Promise.reject(error)
)

apiClient.interceptors.response.use(
  response => {
    const result = response.data
    if (result && result.code === 200) {
      return result
    } else {
      return Promise.reject(new Error(result?.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error.response?.data?.message || error.message || '网络错误')
  }
)

function getLocalUser() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch (e) {
    console.error('瑙ｆ瀽鐢ㄦ埛淇℃伅澶辫触:', e)
    return {}
  }
}

export default {
  login(username, password, role) {
    return apiClient.post('/user/login', { username, password, role })
  },
  
  getUserInfo() {
    return apiClient.get('/user/info')
  },
  
  getCourses(params = {}) {
    return apiClient.get('/course/list', { params })
  },

  getCourseDetail(courseId) {
    return apiClient.get(`/course/${courseId}`)
  },

  createCourse(data) {
    return apiClient.post('/course/create', data)
  },

  updateCourse(courseId, data) {
    return apiClient.put(`/course/${courseId}`, data)
  },

  getLearningContent(courseId) {
    const user = getLocalUser()
    return apiClient.get(`/course/${courseId}/learning-content`, {
      params: { userId: user.id, role: user.role }
    })
  },

  getCourseCategories() {
    return apiClient.get('/course/categories')
  },

  getCourseDepartments() {
    return apiClient.get('/course/departments')
  },

  getCourseSemesters() {
    return apiClient.get('/course/semesters')
  },

  getTeacherCourses() {
    return apiClient.get('/course/teacher-courses', {
      params: { teacherId: getLocalUser().id }
    })
  },

  selectCourse(courseId) {
    const userInfo = getLocalUser()
    return apiClient.post('/course/select', {
      courseId,
      studentId: userInfo.id
    })
  },

  dropCourse(courseId) {
    const userInfo = getLocalUser()
    return apiClient.post('/course/drop', {
      courseId,
      studentId: userInfo.id
    })
  },

  getMyCourses() {
    return apiClient.get('/course/my-courses', {
      params: { studentId: getLocalUser().id }
    })
  },
  
  getHomeworks() {
    return apiClient.get('/homework/list')
  },
  
  getMyHomeworks() {
    return apiClient.get('/homework/my-homeworks', {
      params: { studentId: getLocalUser().id }
    })
  },
  
  submitHomework(homeworkId, content) {
    const userInfo = getLocalUser()
    return apiClient.post('/homework/submit', { 
      homeworkId, 
      content,
      studentId: userInfo.id 
    })
  },
  
  publishHomework(data) {
    return apiClient.post('/homework/publish', data)
  },
  
  getCourseStudents(courseId) {
    return apiClient.get(`/course/${courseId}/students`)
  },
  
  // Course content APIs
  createChapter(courseId, data) {
    const user = getLocalUser()
    return apiClient.post(`/course/${courseId}/chapters`, data, {
      params: { teacherId: user.id }
    })
  },

  updateChapter(chapterId, data) {
    const user = getLocalUser()
    return apiClient.put(`/course/chapters/${chapterId}`, data, {
      params: { teacherId: user.id }
    })
  },

  deleteChapter(chapterId) {
    const user = getLocalUser()
    return apiClient.delete(`/course/chapters/${chapterId}`, {
      params: { teacherId: user.id }
    })
  },

  createVideo(courseId, data) {
    const user = getLocalUser()
    return apiClient.post(`/course/${courseId}/videos`, data, {
      params: { teacherId: user.id }
    })
  },

  updateVideo(videoId, data) {
    const user = getLocalUser()
    return apiClient.put(`/course/videos/${videoId}`, data, {
      params: { teacherId: user.id }
    })
  },

  deleteVideo(videoId) {
    const user = getLocalUser()
    return apiClient.delete(`/course/videos/${videoId}`, {
      params: { teacherId: user.id }
    })
  },

  getVideoPlayInfo(videoId) {
    const user = getLocalUser()
    return apiClient.get(`/course/videos/${videoId}/play-info`, {
      params: { userId: user.id, role: user.role }
    })
  },

  saveVideoProgress(videoId, lastPosition, duration) {
    const user = getLocalUser()
    return apiClient.post(`/course/videos/${videoId}/progress`, {
      lastPosition,
      duration
    }, {
      params: { studentId: user.id }
    })
  },

  getVideoProgress(videoId) {
    const user = getLocalUser()
    return apiClient.get(`/course/videos/${videoId}/progress`, {
      params: { studentId: user.id }
    })
  },

  getVideoDanmaku(videoId) {
    const user = getLocalUser()
    return apiClient.get(`/course/videos/${videoId}/danmaku`, {
      params: { userId: user.id, role: user.role }
    })
  },

  sendVideoDanmaku(videoId, data) {
    const user = getLocalUser()
    return apiClient.post(`/course/videos/${videoId}/danmaku`, {
      ...data,
      studentId: user.id,
      studentName: user.name || user.username
    })
  },

  createMaterial(courseId, data) {
    const user = getLocalUser()
    return apiClient.post(`/course/${courseId}/materials`, data, {
      params: { teacherId: user.id }
    })
  },

  updateMaterial(id, data) {
    const user = getLocalUser()
    return apiClient.put(`/course/materials/${id}`, data, {
      params: { teacherId: user.id }
    })
  },

  deleteMaterial(id) {
    const user = getLocalUser()
    return apiClient.delete(`/course/materials/${id}`, {
      params: { teacherId: user.id }
    })
  },

  createAnnouncement(courseId, data) {
    const user = getLocalUser()
    return apiClient.post(`/course/${courseId}/announcements`, data, {
      params: { teacherId: user.id }
    })
  },

  updateAnnouncement(id, data) {
    const user = getLocalUser()
    return apiClient.put(`/course/announcements/${id}`, data, {
      params: { teacherId: user.id }
    })
  },

  deleteAnnouncement(id) {
    const user = getLocalUser()
    return apiClient.delete(`/course/announcements/${id}`, {
      params: { teacherId: user.id }
    })
  },

  // 评论相关
  getCourseComments(courseId) {
    return apiClient.get(`/course/${courseId}/comments`)
  },
  
  addComment(courseId, commentData) {
    return apiClient.post(`/course/${courseId}/comments`, commentData)
  },
  
  deleteComment(commentId) {
    return apiClient.delete(`/course/comments/${commentId}`)
  },
  
  getHomeworkSubmissions(homeworkId) {
    return apiClient.get(`/homework/${homeworkId}/submissions`)
  }
}
