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
        <!-- 课程头部信息 -->
        <div v-if="course" class="course-hero">
          <div class="hero-cover">
            <img v-if="course.coverImage" :src="course.coverImage" alt="封面" />
            <div v-else class="cover-placeholder">
              <span>{{ course.name?.charAt(0) || '课' }}</span>
            </div>
          </div>
          <div class="hero-info">
            <div class="hero-tags">
              <el-tag v-if="course.category" type="warning">{{ course.category }}</el-tag>
              <el-tag v-if="course.department" type="info">{{ course.department }}</el-tag>
              <el-tag :type="course.status === 'open' ? 'success' : 'danger'">
                {{ course.status === 'open' ? '进行中' : '已结束' }}
              </el-tag>
            </div>
            <h2 class="hero-name">{{ course.name }}</h2>
            <p class="hero-meta">
              <span>👨‍🏫 {{ course.teacherName }}</span>
              <span>📚 {{ course.credit }} 学分</span>
              <span>👥 {{ course.currentStudents || 0 }}/{{ course.maxStudents }} 人</span>
              <span>📅 {{ course.semester }}</span>
            </p>
            <div class="hero-rating">
              <StarRating :modelValue="course.avgRating || 0" :readonly="true" :size="20" showText />
              <span class="review-count">({{ course.reviewCount || 0 }} 条评价)</span>
            </div>
          </div>
        </div>

        <!-- Tab 切换 -->
        <el-tabs v-if="course" v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="课程介绍" name="intro">
            <el-card>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="课程代码">{{ course.code || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="课程分类">{{ course.category || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="开课院系">{{ course.department || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="学分">{{ course.credit }}</el-descriptions-item>
                <el-descriptions-item label="选课人数">{{ course.currentStudents || 0 }}/{{ course.maxStudents }}</el-descriptions-item>
                <el-descriptions-item label="学期">{{ course.semester || '暂无' }}</el-descriptions-item>
              </el-descriptions>
              <div class="section-block">
                <h4>课程简介</h4>
                <p class="description-text">{{ course.description || '暂无课程简介' }}</p>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="课程大纲" name="syllabus">
            <el-card>
              <div v-if="course.syllabus" class="syllabus-content">
                <pre class="syllabus-text">{{ course.syllabus }}</pre>
              </div>
              <el-empty v-else description="暂无课程大纲" />
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="讲师信息" name="instructor">
            <el-card>
              <div v-if="course.teacherInfo" class="instructor-info">
                <div class="instructor-header">
                  <el-avatar :size="64" icon="UserFilled" />
                  <div class="instructor-detail">
                    <h3>{{ course.teacherInfo.name }}</h3>
                    <el-tag type="success">教师</el-tag>
                  </div>
                </div>
                <el-descriptions :column="2" border class="instructor-desc">
                  <el-descriptions-item label="所属院系">{{ course.teacherInfo.department || '暂无' }}</el-descriptions-item>
                  <el-descriptions-item label="邮箱">{{ course.teacherInfo.email || '暂无' }}</el-descriptions-item>
                  <el-descriptions-item label="电话">{{ course.teacherInfo.phone || '暂无' }}</el-descriptions-item>
                  <el-descriptions-item label="用户名">{{ course.teacherInfo.username }}</el-descriptions-item>
                </el-descriptions>
              </div>
              <el-empty v-else description="暂无讲师信息" />
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="课程评价" name="reviews">
            <!-- 评分概览 -->
            <el-card class="rating-overview">
              <div class="rating-summary">
                <div class="rating-score">
                  <span class="score-number">{{ course.avgRating || 0 }}</span>
                  <span class="score-total">/ 5</span>
                </div>
                <div class="rating-detail">
                  <StarRating :modelValue="course.avgRating || 0" :readonly="true" :size="24" />
                  <span class="review-count-text">{{ course.reviewCount || 0 }} 条评价</span>
                </div>
              </div>
            </el-card>

            <!-- 发表评价 -->
            <el-card class="review-input-card">
              <h4>发表评价</h4>
              <div class="review-rating-row">
                <span class="rating-label">评分：</span>
                <StarRating v-model="reviewRating" :readonly="false" :size="22" />
                <span class="rating-hint" v-if="reviewRating > 0">{{ reviewRating }} 分</span>
              </div>
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="分享你的学习体验..."
              />
              <div class="comment-actions">
                <el-button type="primary" @click="handlePostComment" :disabled="!newComment.trim()">
                  发布评价
                </el-button>
              </div>
            </el-card>

            <!-- 评价/讨论列表 -->
            <el-card class="comments-card">
              <template #header>
                <h4>全部评价与讨论 ({{ comments.length }})</h4>
              </template>
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
                        <StarRating v-if="comment.rating" :modelValue="comment.rating" :readonly="true" :size="12" />
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
          </el-tab-pane>

          <el-tab-pane v-if="isStudent" label="课程学习" name="learning">
            <div v-if="studentProgress" class="progress-board">
              <div class="progress-main">
                <div>
                  <span class="progress-label">学习完成度</span>
                  <strong>{{ studentProgress.completionRate || 0 }}%</strong>
                </div>
                <el-progress :percentage="studentProgress.completionRate || 0" :stroke-width="10" />
              </div>
              <div class="progress-metric">
                <span>已完成</span>
                <strong>{{ studentProgress.completedVideos || 0 }}/{{ studentProgress.totalVideos || 0 }}</strong>
              </div>
              <div class="progress-metric">
                <span>最近学习</span>
                <strong>{{ formatDateTime(studentProgress.lastLearnedAt) || '暂无' }}</strong>
              </div>
            </div>
            <div class="learning-layout">
              <aside class="learning-sidebar">
                <div v-if="latestAnnouncement" class="announcement-strip">
                  <strong>{{ latestAnnouncement.title }}</strong>
                  <p>{{ latestAnnouncement.content }}</p>
                </div>
                <div v-for="chapter in chapterTree" :key="chapter.id" class="chapter-block">
                  <h4>{{ chapter.title }}</h4>
                  <button
                    v-for="video in chapter.videos"
                    :key="video.id"
                    class="lesson-row"
                    :class="{ active: selectedVideo && selectedVideo.id === video.id }"
                    type="button"
                    @click="selectVideo(video)"
                  >
                    <span class="lesson-title">{{ video.title }}</span>
                    <el-tag size="small" :type="getProgressTagType(getVideoProgressInfo(video.id).status)">
                      {{ getProgressStatusLabel(getVideoProgressInfo(video.id).status) }}
                    </el-tag>
                  </button>
                  <a
                    v-for="material in chapter.materials"
                    :key="material.id"
                    class="material-row"
                    :href="material.resourceUrl"
                    target="_blank"
                    rel="noreferrer"
                  >
                    {{ material.title }}
                  </a>
                  <div v-for="child in chapter.children" :key="child.id" class="child-chapter">
                    <h5>{{ child.title }}</h5>
                    <button
                      v-for="video in child.videos"
                      :key="video.id"
                      class="lesson-row"
                      :class="{ active: selectedVideo && selectedVideo.id === video.id }"
                      type="button"
                      @click="selectVideo(video)"
                    >
                      <span class="lesson-title">{{ video.title }}</span>
                      <el-tag size="small" :type="getProgressTagType(getVideoProgressInfo(video.id).status)">
                        {{ getProgressStatusLabel(getVideoProgressInfo(video.id).status) }}
                      </el-tag>
                    </button>
                    <a
                      v-for="material in child.materials"
                      :key="material.id"
                      class="material-row"
                      :href="material.resourceUrl"
                      target="_blank"
                      rel="noreferrer"
                    >
                      {{ material.title }}
                    </a>
                  </div>
                </div>
                <el-empty v-if="chapterTree.length === 0" description="暂无学习内容" />
              </aside>
              <section class="learning-player">
                <VideoPlayer
                  v-if="selectedPlayInfo"
                  :key="selectedPlayInfo.videoId || selectedVideo.id"
                  :src="selectedPlayInfo.videoUrl"
                  :poster="selectedPlayInfo.poster"
                  :initial-time="videoInitialTime"
                  :danmaku-list="danmakuList"
                  :danmaku-enabled="danmakuEnabled"
                  @timeupdate="handleVideoTimeUpdate"
                  @pause="handleProgressSave"
                  @ended="handleProgressSave"
                  @progress-save="handleProgressSave"
                  @send-danmaku="handlePlayerDanmaku"
                  @error="handleVideoError"
                  @modechange="handleVideoModeChange"
                />
                <el-empty v-else description="请选择视频开始学习" />
                <div v-if="selectedPlayInfo && selectedPlaybackMode === 'native'" class="danmaku-panel">
                  <el-switch v-model="danmakuEnabled" active-text="弹幕" />
                  <el-input
                    v-model="danmakuInput"
                    maxlength="200"
                    show-word-limit
                    placeholder="发送弹幕"
                    aria-label="发送弹幕"
                    @keyup.enter="sendDanmaku"
                  />
                  <el-button type="primary" @click="sendDanmaku">发送</el-button>
                </div>
              </section>
            </div>
          </el-tab-pane>

          <!-- 学生端：课程作业 -->
          <el-tab-pane v-if="isStudent" label="课程作业" name="homeworks">
            <el-card>
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
                    <el-button v-else type="info" size="small" @click="viewSubmission(scope.row)">
                      查看
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="courseHomeworks.length === 0" description="暂无作业" />
            </el-card>
          </el-tab-pane>

          <el-tab-pane v-if="isTeacher" label="内容管理" name="content-manage">
            <div class="manage-grid">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <h4>章节</h4>
                    <el-button type="primary" size="small" @click="showChapterDialog()">新增章节</el-button>
                  </div>
                </template>
                <el-table :data="learningContent.chapters" border>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="parentId" label="父章节" width="100" />
                  <el-table-column prop="sortOrder" label="排序" width="90" />
                  <el-table-column label="操作" width="160">
                    <template #default="scope">
                      <el-button size="small" @click="showChapterDialog(scope.row)">编辑</el-button>
                      <el-button type="danger" size="small" @click="deleteChapter(scope.row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>

              <el-card>
                <template #header>
                  <div class="card-header">
                    <h4>视频</h4>
                    <el-button type="primary" size="small" @click="showVideoDialog()">新增视频</el-button>
                  </div>
                </template>
                <el-table :data="learningContent.videos" border>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="status" label="状态" width="100" />
                  <el-table-column prop="duration" label="时长" width="90" />
                  <el-table-column label="操作" width="160">
                    <template #default="scope">
                      <el-button size="small" @click="showVideoDialog(scope.row)">编辑</el-button>
                      <el-button type="danger" size="small" @click="deleteVideo(scope.row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>

              <el-card>
                <template #header>
                  <div class="card-header">
                    <h4>课件</h4>
                    <el-button type="primary" size="small" @click="showMaterialDialog()">新增课件</el-button>
                  </div>
                </template>
                <el-table :data="learningContent.materials" border>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="fileType" label="类型" width="100" />
                  <el-table-column prop="resourceUrl" label="地址" show-overflow-tooltip />
                  <el-table-column label="操作" width="160">
                    <template #default="scope">
                      <el-button size="small" @click="showMaterialDialog(scope.row)">编辑</el-button>
                      <el-button type="danger" size="small" @click="deleteMaterial(scope.row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>

              <el-card>
                <template #header>
                  <div class="card-header">
                    <h4>公告</h4>
                    <el-button type="primary" size="small" @click="showAnnouncementDialog()">发布公告</el-button>
                  </div>
                </template>
                <el-table :data="learningContent.announcements" border>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="publishedAt" label="发布时间" width="180" />
                  <el-table-column label="操作" width="160">
                    <template #default="scope">
                      <el-button size="small" @click="showAnnouncementDialog(scope.row)">编辑</el-button>
                      <el-button type="danger" size="small" @click="deleteAnnouncement(scope.row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 教师端：选课学生 -->
          <el-tab-pane v-if="isTeacher" label="选课学生" name="students">
            <el-card>
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
          </el-tab-pane>

          <el-tab-pane v-if="isTeacher" label="学习统计" name="learning-progress">
            <div v-if="teacherProgressOverview" class="teacher-progress">
              <div class="progress-overview-grid">
                <div class="overview-cell">
                  <span>平均完成度</span>
                  <strong>{{ teacherProgressOverview.averageCompletionRate || 0 }}%</strong>
                </div>
                <div class="overview-cell">
                  <span>已完成学生</span>
                  <strong>{{ teacherProgressOverview.completedStudents || 0 }}</strong>
                </div>
                <div class="overview-cell">
                  <span>学习中</span>
                  <strong>{{ teacherProgressOverview.inProgressStudents || 0 }}</strong>
                </div>
                <div class="overview-cell">
                  <span>未开始</span>
                  <strong>{{ teacherProgressOverview.notStartedStudents || 0 }}</strong>
                </div>
              </div>
              <el-card class="progress-table-card">
                <template #header>
                  <div class="card-header">
                    <h4>学生学习进度</h4>
                    <span class="student-count">共 {{ teacherProgressStudents.length }} 人</span>
                  </div>
                </template>
                <el-table :data="teacherProgressStudents" border>
                  <el-table-column label="姓名" min-width="120">
                    <template #default="scope">
                      {{ scope.row.name || scope.row.username || ('ID ' + scope.row.studentId) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="完成度" min-width="220">
                    <template #default="scope">
                      <div class="table-progress">
                        <el-progress :percentage="scope.row.completionRate || 0" />
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="已完成" width="120">
                    <template #default="scope">
                      {{ scope.row.completedVideos || 0 }}/{{ scope.row.totalVideos || 0 }}
                    </template>
                  </el-table-column>
                  <el-table-column label="最近学习" width="180">
                    <template #default="scope">
                      {{ formatDateTime(scope.row.lastLearnedAt) || '暂无' }}
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="teacherProgressStudents.length === 0" description="暂无学习进度" />
              </el-card>
            </div>
            <el-empty v-else description="暂无学习统计" />
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 提交作业对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交作业" width="500px">
      <el-form :model="submitForm">
        <el-form-item label="作业标题">
          <span>{{ submitForm.title }}</span>
        </el-form-item>
        <el-form-item label="作业内容">
          <el-input v-model="submitForm.content" type="textarea" :rows="6" placeholder="请输入作业内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitHomework">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="chapterDialogVisible" :title="editingChapter.id ? '编辑章节' : '新增章节'" width="520px">
      <el-form :model="editingChapter" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="editingChapter.title" placeholder="请输入章节标题" />
        </el-form-item>
        <el-form-item label="父章节">
          <el-select v-model="editingChapter.parentId" clearable placeholder="顶级章节">
            <el-option
              v-for="chapter in rootChapterOptions"
              :key="chapter.id"
              :label="chapter.title"
              :value="chapter.id"
              :disabled="chapter.id === editingChapter.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editingChapter.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveChapter">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="videoDialogVisible" :title="editingVideo.id ? '编辑视频' : '新增视频'" width="640px">
      <el-form :model="editingVideo" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="editingVideo.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="editingVideo.chapterId" clearable placeholder="请选择章节">
            <el-option v-for="chapter in chapterOptions" :key="chapter.id" :label="chapter.title" :value="chapter.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频地址">
          <el-input v-model="editingVideo.videoUrl" placeholder="支持视频直链或可嵌入的视频页面链接" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingVideo.description" type="textarea" :rows="3" placeholder="请输入视频简介" />
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="editingVideo.duration" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editingVideo.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editingVideo.status">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="隐藏" value="hidden" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="videoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVideo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="materialDialogVisible" :title="editingMaterial.id ? '编辑课件' : '新增课件'" width="560px">
      <el-form :model="editingMaterial" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="editingMaterial.title" placeholder="请输入课件标题" />
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="editingMaterial.chapterId" clearable placeholder="请选择章节">
            <el-option v-for="chapter in chapterOptions" :key="chapter.id" :label="chapter.title" :value="chapter.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="editingMaterial.fileType" placeholder="PDF / PPT / DOC / URL" />
        </el-form-item>
        <el-form-item label="资源地址">
          <el-input v-model="editingMaterial.resourceUrl" placeholder="https://example.com/material.pdf" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editingMaterial.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMaterial">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="announcementDialogVisible" :title="editingAnnouncement.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="editingAnnouncement" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="editingAnnouncement.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editingAnnouncement.content" type="textarea" :rows="5" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="announcementDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import api from '../api'
import StarRating from '../components/StarRating.vue'
import VideoPlayer from '../components/VideoPlayer.vue'

export default {
  name: 'CourseDetail',
  components: { StarRating, VideoPlayer },
  data() {
    return {
      loading: false,
      userInfo: {},
      course: null,
      activeTab: 'intro',
      courseHomeworks: [],
      courseStudents: [],
      submitDialogVisible: false,
      submitForm: { homeworkId: null, title: '', content: '' },
      // 评论相关
      comments: [],
      newComment: '',
      reviewRating: 0,
      replyContent: '',
      replyingTo: null,
      learningContent: {
        chapters: [],
        videos: [],
        materials: [],
        announcements: []
      },
      progressSummary: null,
      selectedVideo: null,
      selectedPlayInfo: null,
      selectedPlaybackMode: 'native',
      videoInitialTime: 0,
      currentVideoTime: 0,
      danmakuEnabled: true,
      danmakuList: [],
      danmakuInput: '',
      progressSaving: false,
      chapterDialogVisible: false,
      videoDialogVisible: false,
      materialDialogVisible: false,
      announcementDialogVisible: false,
      editingChapter: { title: '', parentId: null, sortOrder: 0 },
      editingVideo: {
        title: '',
        description: '',
        chapterId: null,
        videoUrl: '',
        duration: 0,
        sortOrder: 0,
        status: 'draft'
      },
      editingMaterial: { title: '', fileType: '', resourceUrl: '', chapterId: null, sortOrder: 0 },
      editingAnnouncement: { title: '', content: '' }
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
    },
    latestAnnouncement() {
      return this.learningContent.announcements[0] || null
    },
    studentProgress() {
      return this.progressSummary?.student || null
    },
    teacherProgressOverview() {
      return this.progressSummary?.overview || null
    },
    teacherProgressStudents() {
      return this.progressSummary?.students || []
    },
    studentVideoProgressMap() {
      const videos = this.studentProgress?.videos || []
      return videos.reduce((map, item) => {
        map[item.videoId] = item
        return map
      }, {})
    },
    rootChapterOptions() {
      return this.learningContent.chapters.filter(chapter => !chapter.parentId)
    },
    chapterOptions() {
      const rootIds = new Set(this.rootChapterOptions.map(chapter => chapter.id))
      return this.learningContent.chapters.map(chapter => ({
        ...chapter,
        title: chapter.parentId && rootIds.has(chapter.parentId) ? `  ${chapter.title}` : chapter.title
      }))
    },
    chapterTree() {
      const videos = this.learningContent.videos || []
      const materials = this.learningContent.materials || []
      return (this.learningContent.chapters || [])
        .filter(chapter => !chapter.parentId)
        .map(chapter => ({
          ...chapter,
          children: (this.learningContent.chapters || [])
            .filter(child => child.parentId === chapter.id)
            .map(child => ({
              ...child,
              videos: videos.filter(video => video.chapterId === child.id && video.status === 'published'),
              materials: materials.filter(material => material.chapterId === child.id)
            })),
          videos: videos.filter(video => video.chapterId === chapter.id && video.status === 'published'),
          materials: materials.filter(material => material.chapterId === chapter.id)
        }))
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadCourseDetail()
    this.loadComments()
    window.addEventListener('beforeunload', this.saveCurrentVideoProgress)
  },
  beforeUnmount() {
    window.removeEventListener('beforeunload', this.saveCurrentVideoProgress)
    this.saveCurrentVideoProgress()
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
        const result = await api.getCourseDetail(this.courseId)
        this.course = result.data
        if (!this.course) {
          ElMessage.error('课程不存在')
          this.goBack()
        }
        // 加载角色相关内容
        if (this.isTeacher) this.loadCourseStudents()
        if (this.isStudent) this.loadCourseHomeworks()
        if (this.isStudent || this.isTeacher) this.loadLearningContent()
        if (this.isStudent || this.isTeacher) this.loadProgressSummary()
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
      }
    },

    async loadCourseHomeworks() {
      try {
        const result = await api.getMyHomeworks()
        const allHomeworks = result.data || []
        this.courseHomeworks = allHomeworks.filter(h => h.courseName === this.course?.name)
      } catch (error) {
        console.error('加载作业列表失败:', error)
      }
    },

    async loadLearningContent() {
      try {
        const result = await api.getLearningContent(this.courseId)
        this.learningContent = result.data || { chapters: [], videos: [], materials: [], announcements: [] }
      } catch (error) {
        console.error('加载课程学习内容失败:', error)
        ElMessage.error('加载课程学习内容失败')
      }
    },

    async loadProgressSummary() {
      try {
        const result = await api.getCourseProgressSummary(this.courseId)
        this.progressSummary = result.data || null
      } catch (error) {
        console.error('加载学习进度统计失败:', error)
        this.progressSummary = null
      }
    },

    async selectVideo(video) {
      try {
        await this.saveCurrentVideoProgress()
        this.selectedVideo = video
        this.selectedPlayInfo = null
        this.selectedPlaybackMode = 'native'
        this.videoInitialTime = 0
        this.currentVideoTime = 0
        const playRes = await api.getVideoPlayInfo(video.id)
        this.selectedPlayInfo = playRes.data
        const progressRes = await api.getVideoProgress(video.id)
        this.videoInitialTime = progressRes.data?.lastPosition || 0
        this.currentVideoTime = this.videoInitialTime
        const danmakuRes = await api.getVideoDanmaku(video.id)
        this.danmakuList = danmakuRes.data || []
      } catch (error) {
        console.error('加载视频失败:', error)
        ElMessage.error('加载视频失败')
      }
    },

    handleVideoTimeUpdate(payload = {}) {
      this.currentVideoTime = payload.currentTime || 0
    },

    handleVideoModeChange(payload = {}) {
      this.selectedPlaybackMode = payload.mode || 'native'
    },

    async handleProgressSave(payload = {}) {
      if (!this.selectedVideo || this.progressSaving || this.selectedPlaybackMode !== 'native') return
      const currentTime = payload.currentTime ?? this.currentVideoTime ?? 0
      const duration = payload.duration ?? this.selectedPlayInfo?.duration ?? this.selectedVideo.duration ?? 0
      this.currentVideoTime = currentTime
      this.progressSaving = true
      try {
        await api.saveVideoProgress(this.selectedVideo.id, Math.floor(currentTime), Math.floor(duration))
        if (this.isStudent) {
          await this.loadProgressSummary()
        }
      } catch (error) {
        console.warn('保存播放进度失败:', error)
      } finally {
        this.progressSaving = false
      }
    },

    saveCurrentVideoProgress() {
      if (!this.selectedVideo || this.selectedPlaybackMode !== 'native') return Promise.resolve()
      return this.handleProgressSave({
        currentTime: this.currentVideoTime,
        duration: this.selectedPlayInfo?.duration || this.selectedVideo.duration || 0
      })
    },

    handlePlayerDanmaku(payload = {}) {
      return this.sendDanmaku(payload)
    },

    async sendDanmaku(payload = {}) {
      const content = (payload.content || this.danmakuInput || '').trim()
      if (!this.selectedVideo || !content) return
      try {
        await api.sendVideoDanmaku(this.selectedVideo.id, {
          content,
          timeSeconds: Math.floor(payload.timeSeconds ?? this.currentVideoTime ?? 0),
          color: payload.color || '#ffffff'
        })
        this.danmakuInput = ''
        const result = await api.getVideoDanmaku(this.selectedVideo.id)
        this.danmakuList = result.data || []
      } catch (error) {
        console.error('发送弹幕失败:', error)
        ElMessage.error('发送弹幕失败')
      }
    },

    handleVideoError(message) {
      ElMessage.error(message || '视频无法播放，请检查地址或在新窗口打开')
    },

    showChapterDialog(chapter) {
      this.editingChapter = chapter ? { ...chapter } : { title: '', parentId: null, sortOrder: 0 }
      this.chapterDialogVisible = true
    },

    async saveChapter() {
      if (!this.editingChapter.title?.trim()) {
        ElMessage.warning('请输入章节标题')
        return
      }
      try {
        if (this.editingChapter.id) {
          await api.updateChapter(this.editingChapter.id, this.editingChapter)
        } else {
          await api.createChapter(this.courseId, this.editingChapter)
        }
        ElMessage.success('保存成功')
        this.chapterDialogVisible = false
        this.loadLearningContent()
      } catch (error) {
        ElMessage.error('保存章节失败')
      }
    },

    async deleteChapter(id) {
      try {
        await this.$confirm('确定删除这个章节吗？', '提示', { type: 'warning' })
        await api.deleteChapter(id)
        ElMessage.success('删除成功')
        this.loadLearningContent()
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除章节失败')
      }
    },

    showVideoDialog(video) {
      this.editingVideo = video
        ? { ...video }
        : { title: '', description: '', chapterId: null, videoUrl: '', duration: 0, sortOrder: 0, status: 'draft' }
      this.videoDialogVisible = true
    },

    async saveVideo() {
      if (!this.editingVideo.title?.trim() || !this.editingVideo.videoUrl?.trim()) {
        ElMessage.warning('请输入视频标题和地址')
        return
      }
      try {
        if (this.editingVideo.id) {
          await api.updateVideo(this.editingVideo.id, this.editingVideo)
        } else {
          await api.createVideo(this.courseId, this.editingVideo)
        }
        ElMessage.success('保存成功')
        this.videoDialogVisible = false
        this.loadLearningContent()
      } catch (error) {
        ElMessage.error('保存视频失败')
      }
    },

    async deleteVideo(id) {
      try {
        await this.$confirm('确定删除这个视频吗？', '提示', { type: 'warning' })
        await api.deleteVideo(id)
        ElMessage.success('删除成功')
        this.loadLearningContent()
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除视频失败')
      }
    },

    showMaterialDialog(material) {
      this.editingMaterial = material ? { ...material } : { title: '', fileType: '', resourceUrl: '', chapterId: null, sortOrder: 0 }
      this.materialDialogVisible = true
    },

    async saveMaterial() {
      if (!this.editingMaterial.title?.trim() || !this.editingMaterial.resourceUrl?.trim()) {
        ElMessage.warning('请输入课件标题和资源地址')
        return
      }
      try {
        if (this.editingMaterial.id) {
          await api.updateMaterial(this.editingMaterial.id, this.editingMaterial)
        } else {
          await api.createMaterial(this.courseId, this.editingMaterial)
        }
        ElMessage.success('保存成功')
        this.materialDialogVisible = false
        this.loadLearningContent()
      } catch (error) {
        ElMessage.error('保存课件失败')
      }
    },

    async deleteMaterial(id) {
      try {
        await this.$confirm('确定删除这个课件吗？', '提示', { type: 'warning' })
        await api.deleteMaterial(id)
        ElMessage.success('删除成功')
        this.loadLearningContent()
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除课件失败')
      }
    },

    showAnnouncementDialog(announcement) {
      this.editingAnnouncement = announcement ? { ...announcement } : { title: '', content: '' }
      this.announcementDialogVisible = true
    },

    async saveAnnouncement() {
      if (!this.editingAnnouncement.title?.trim() || !this.editingAnnouncement.content?.trim()) {
        ElMessage.warning('请输入公告标题和内容')
        return
      }
      try {
        if (this.editingAnnouncement.id) {
          await api.updateAnnouncement(this.editingAnnouncement.id, this.editingAnnouncement)
        } else {
          await api.createAnnouncement(this.courseId, this.editingAnnouncement)
        }
        ElMessage.success('保存成功')
        this.announcementDialogVisible = false
        this.loadLearningContent()
      } catch (error) {
        ElMessage.error('保存公告失败')
      }
    },

    async deleteAnnouncement(id) {
      try {
        await this.$confirm('确定删除这条公告吗？', '提示', { type: 'warning' })
        await api.deleteAnnouncement(id)
        ElMessage.success('删除成功')
        this.loadLearningContent()
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除公告失败')
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

    viewSubmission() {
      ElMessage.info('查看作业功能')
    },

    viewStudentHomeworks() {
      ElMessage.info('查看学生作业功能')
    },

    // ========== 评论/评价方法 ==========

    async loadComments() {
      try {
        const result = await api.getCourseComments(this.courseId)
        this.comments = result.data || []
      } catch (error) {
        console.error('加载评论失败:', error)
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
        if (this.reviewRating > 0) {
          commentData.rating = this.reviewRating
        }
        await api.addComment(this.courseId, commentData)
        ElMessage.success('发布成功')
        this.newComment = ''
        this.reviewRating = 0
        this.loadComments()
        // 刷新课程详情以更新评分
        this.loadCourseDetail()
      } catch (error) {
        ElMessage.error('发布失败: ' + (error.message || '未知错误'))
      }
    },

    showReplyInput(comment) {
      this.replyingTo = this.replyingTo === comment.id ? null : comment.id
      this.replyContent = ''
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
        this.loadCourseDetail()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
    },

    getRoleTagType(role) {
      return { admin: 'danger', teacher: 'success', student: 'primary' }[role] || 'info'
    },

    getVideoProgressInfo(videoId) {
      return this.studentVideoProgressMap[videoId] || { status: 'not_started', watchRate: 0 }
    },

    getProgressStatusLabel(status) {
      return {
        completed: '已完成',
        in_progress: '学习中',
        not_started: '未开始'
      }[status] || '未开始'
    },

    getProgressTagType(status) {
      return {
        completed: 'success',
        in_progress: 'warning',
        not_started: 'info'
      }[status] || 'info'
    },

    formatDateTime(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hour}:${minute}`
    },

    formatTime(timeStr) {
      if (!timeStr) return ''
      const diff = new Date() - new Date(timeStr)
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
.course-detail { height: 100vh; }
.header-content { display: flex; justify-content: space-between; align-items: center; height: 100%; }
.header-left { display: flex; align-items: center; gap: 15px; }
.el-header { background-color: #409EFF; color: white; }
.el-main { padding: 20px; background-color: #f5f5f5; }

/* 课程头部 */
.course-hero { display: flex; gap: 24px; background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.hero-cover { width: 200px; height: 140px; border-radius: 8px; overflow: hidden; flex-shrink: 0; background: linear-gradient(135deg, #667eea, #764ba2); }
.hero-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.cover-placeholder span { font-size: 56px; color: rgba(255,255,255,0.5); font-weight: bold; }
.hero-info { flex: 1; }
.hero-tags { display: flex; gap: 8px; margin-bottom: 8px; }
.hero-name { margin: 0 0 8px 0; font-size: 24px; color: #303133; }
.hero-meta { display: flex; gap: 20px; flex-wrap: wrap; margin: 0 0 10px 0; color: #606266; font-size: 14px; }
.hero-rating { display: flex; align-items: center; gap: 8px; }
.review-count { color: #909399; font-size: 13px; }

/* Tabs */
.detail-tabs { background: white; border-radius: 8px; padding: 8px 16px 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.section-block { margin-top: 20px; }
.section-block h4 { margin-bottom: 10px; color: #303133; }
.description-text { color: #606266; line-height: 1.8; white-space: pre-wrap; }
.syllabus-content { padding: 10px 0; }
.syllabus-text { white-space: pre-wrap; font-family: inherit; color: #606266; line-height: 1.8; margin: 0; }

/* 讲师 */
.instructor-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.instructor-detail h3 { margin: 0 0 6px 0; }
.instructor-desc { margin-top: 16px; }

/* 评分 */
.rating-overview { margin-bottom: 16px; }
.rating-summary { display: flex; align-items: center; gap: 24px; }
.rating-score { display: flex; align-items: baseline; gap: 2px; }
.score-number { font-size: 48px; font-weight: bold; color: #f7ba2a; }
.score-total { font-size: 18px; color: #909399; }
.review-count-text { color: #909399; font-size: 13px; margin-left: 8px; }

/* 发表评价 */
.review-input-card { margin-bottom: 16px; }
.review-rating-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.rating-label { color: #606266; }
.rating-hint { color: #f7ba2a; font-weight: 600; }
.comment-actions { margin-top: 10px; text-align: right; }

/* 评论列表 */
.comments-card { margin-top: 16px; }
.comments-header { display: flex; justify-content: space-between; align-items: center; }
.comments-header h4 { margin: 0; }
.comment-count { color: #909399; font-size: 14px; }
.comments-list { margin-top: 0; }
.comment-item { padding: 15px 0; border-bottom: 1px solid #ebeef5; }
.comment-item:last-child { border-bottom: none; }
.comment-main { display: flex; gap: 12px; }
.comment-avatar { flex-shrink: 0; }
.comment-content { flex: 1; min-width: 0; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.comment-author { font-weight: 600; color: #303133; }
.comment-time { color: #909399; font-size: 12px; }
.comment-text { color: #606266; line-height: 1.6; margin-bottom: 8px; }
.comment-actions-row { display: flex; gap: 15px; align-items: center; }
.reply-input-area { margin-top: 10px; padding: 10px; background-color: #f5f7fa; border-radius: 4px; }
.reply-actions { margin-top: 10px; text-align: right; }
.replies-list { margin-top: 15px; padding-left: 20px; border-left: 2px solid #e4e7ed; }
.reply-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f7fa; }
.reply-item:last-child { border-bottom: none; }
.reply-avatar { flex-shrink: 0; }
.reply-content { flex: 1; min-width: 0; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h4 { margin: 0; }
.student-count { color: #909399; }

.progress-board { display: grid; grid-template-columns: minmax(220px, 1fr) 150px 180px; gap: 12px; margin-bottom: 16px; }
.progress-main, .progress-metric, .overview-cell { padding: 14px; border: 1px solid #e4e7ed; border-radius: 6px; background: #fff; }
.progress-main > div { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 10px; }
.progress-main strong, .progress-metric strong, .overview-cell strong { color: #303133; font-size: 24px; line-height: 1.1; }
.progress-label, .progress-metric span, .overview-cell span { display: block; color: #909399; font-size: 13px; margin-bottom: 8px; }
.progress-metric { display: flex; flex-direction: column; justify-content: center; }
.progress-metric strong { font-size: 18px; }
.progress-overview-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.progress-table-card { margin-top: 0; }
.table-progress { min-width: 180px; }

.learning-layout { display: grid; grid-template-columns: 280px 1fr; gap: 16px; }
.learning-sidebar { background: #fff; border: 1px solid #ebeef5; border-radius: 6px; padding: 12px; max-height: 620px; overflow: auto; }
.announcement-strip { border-left: 3px solid #409EFF; padding-left: 10px; margin-bottom: 14px; color: #303133; }
.announcement-strip p { margin: 6px 0 0; color: #606266; font-size: 13px; line-height: 1.5; }
.chapter-block { margin-bottom: 14px; }
.chapter-block h4 { margin: 0 0 8px; color: #303133; }
.lesson-row, .material-row { display: block; width: 100%; text-align: left; padding: 8px 10px; margin-bottom: 6px; border: 1px solid #e4e7ed; border-radius: 4px; background: #f9fafb; color: #303133; text-decoration: none; cursor: pointer; }
.lesson-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; min-height: 38px; }
.lesson-title { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lesson-row.active { border-color: #409EFF; color: #409EFF; background: #ecf5ff; }
.material-row { color: #606266; }
.child-chapter { margin-left: 12px; padding-left: 10px; border-left: 2px solid #ebeef5; }
.child-chapter h5 { margin: 8px 0 6px; color: #606266; }
.learning-player { min-width: 0; }
.danmaku-panel { display: grid; grid-template-columns: auto 1fr auto; gap: 10px; align-items: center; margin-top: 12px; }
.manage-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.manage-grid .el-card { min-width: 0; }

@media (max-width: 900px) {
  .progress-board, .progress-overview-grid { grid-template-columns: 1fr; }
  .learning-layout { grid-template-columns: 1fr; }
  .learning-sidebar { max-height: none; }
  .manage-grid { grid-template-columns: 1fr; }
  .danmaku-panel { grid-template-columns: 1fr; }
}
</style>
