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

export default {
  login(username, password, role) {
    return apiClient.post('/user/login', { username, password, role })
  },
  
  getUserInfo() {
    return apiClient.get('/user/info')
  },
  
  getCourses() {
    return apiClient.get('/course/list')
  },
  
  selectCourse(courseId) {
    return apiClient.post('/course/select', { courseId })
  },
  
  getMyCourses() {
    return apiClient.get('/course/my-courses')
  },
  
  getHomeworks() {
    return apiClient.get('/homework/list')
  },
  
  getMyHomeworks() {
    return apiClient.get('/homework/my-homeworks')
  },
  
  submitHomework(homeworkId, content) {
    return apiClient.post('/homework/submit', { homeworkId, content })
  },
  
  publishHomework(data) {
    return apiClient.post('/homework/publish', data)
  },
  
  getCourseStudents(courseId) {
    return apiClient.get(`/course/${courseId}/students`)
  },
  
  getHomeworkSubmissions(homeworkId) {
    return apiClient.get(`/homework/${homeworkId}/submissions`)
  }
}
