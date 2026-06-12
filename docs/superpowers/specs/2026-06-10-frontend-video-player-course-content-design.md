# Frontend Video Player Course Content Design

## Purpose

Add course content management and video learning features to the existing course assignment system:

- Students can watch course videos with resume playback, playback speed controls, and optional shared danmaku.
- Teachers can create courses, manage chapter structure, add video URLs, add courseware links, and publish announcements.
- The system uses a frontend `VideoPlayer` component based on browser video playback. This phase does not implement video file upload, server-side storage, or transcoding.

## Confirmed Decisions

- Do not use Aliyun VOD in this phase.
- Implement playback through a frontend `VideoPlayer` component.
- Keep this feature inside `course-service` instead of adding a new microservice.
- Store courseware as metadata plus URL in this phase; do not introduce OSS upload.
- Store videos as metadata plus playable URL in this phase; do not upload video files through this system.
- Use shared danmaku stored in the backend database.
- Store video progress in the backend database by `studentId + videoId` so resume playback works across browsers and devices.

## Architecture

The feature extends the current shared-database architecture:

- `common` defines the JPA entities used by `course-service`.
- `course-service` owns course content APIs, video URL metadata, student progress, and danmaku.
- `gateway` keeps routing `/api/course/**` to `course-service`; no new gateway route is required.
- `frontend` extends the existing Vue 3 and Element Plus student and teacher views.

Video playback boundary:

- Backend stores course videos as URL-backed content records.
- Frontend plays the selected `videoUrl` with a reusable `VideoPlayer` component.
- Playback speed, seeking, pause, progress events, and fullscreen use browser video capabilities.
- The application does not receive video file bytes, store local media files, or transcode media.
- The video URL must be directly playable by the browser or by the selected player component.

## Data Model

Add these entities in `common/src/main/java/com/teaching/common/entity` and reflect them in `sql/init.sql`.

In API paths, `videoId` means the local `CourseVideo.id`.

### CourseChapter

Represents a two-level chapter tree.

- `id`
- `courseId`
- `parentId`
- `title`
- `sortOrder`
- `createdAt`
- `updatedAt`

`parentId` is nullable. A null parent is a top-level chapter; a non-null parent is a child section.

### CourseVideo

Represents a URL-backed lesson video attached to a chapter.

- `id`
- `courseId`
- `chapterId`
- `title`
- `description`
- `videoUrl`
- `duration`
- `sortOrder`
- `status`
- `createdAt`
- `updatedAt`

`status` should support at least `draft`, `published`, and `hidden`.

The system stores a playable video URL and business metadata only. It does not store the media file.

### CourseMaterial

Represents courseware or reference material.

- `id`
- `courseId`
- `chapterId`
- `title`
- `fileType`
- `resourceUrl`
- `sortOrder`
- `createdAt`
- `updatedAt`

`resourceUrl` can point to a document, slide deck, external resource, or future OSS object.

### CourseAnnouncement

Represents a teacher-published course announcement.

- `id`
- `courseId`
- `title`
- `content`
- `publishedAt`
- `createdAt`
- `updatedAt`

### VideoProgress

Represents resume playback state.

- `id`
- `studentId`
- `videoId`
- `lastPosition`
- `duration`
- `updatedAt`

Add a unique index on `studentId + videoId`, where `videoId` references local `CourseVideo.id`.

### VideoDanmaku

Represents shared danmaku attached to a video timeline.

- `id`
- `courseId`
- `videoId`
- `studentId`
- `studentName`
- `timeSeconds`
- `content`
- `color`
- `status`
- `createdAt`

`status` should support at least `visible` and `hidden` for future moderation.

## Backend API Design

All endpoints live in `course-service` under the existing `/course` controller path.

### Course Management

- `POST /course/create`
  - Reuse and strengthen the existing course creation endpoint.
  - The teacher ID and teacher name should come from the request data for this project phase, matching current project patterns.

- `PUT /course/{courseId}`
  - Edit course basics such as name, code, description, credit, max students, category, department, cover image, syllabus, semester, and status.

### Chapter Management

- `POST /course/{courseId}/chapters`
  - Create a chapter or child section.

- `PUT /course/chapters/{chapterId}`
  - Edit chapter title, parent, or ordering.

- `DELETE /course/chapters/{chapterId}`
  - Delete one explicit chapter record.
  - Implementation must avoid prohibited batch deletion commands. Cascading behavior should be handled by database/JPA relationships or explicit single-record repository operations.

### Material Management

- `POST /course/{courseId}/materials`
  - Create a courseware URL record.

- `PUT /course/materials/{id}`
  - Edit material metadata.

- `DELETE /course/materials/{id}`
  - Delete one material record.

### Announcement Management

- `POST /course/{courseId}/announcements`
  - Publish an announcement.

- `PUT /course/announcements/{id}`
  - Edit an announcement.

- `DELETE /course/announcements/{id}`
  - Delete one announcement.

### Video Management

- `POST /course/{courseId}/videos`
  - Teacher sends `title`, `description`, `chapterId`, `videoUrl`, `duration`, `sortOrder`, and `status`.
  - Backend verifies the teacher owns the course.
  - Backend creates a `CourseVideo` record.
  - Backend validates that `videoUrl` is not empty and has an HTTP or HTTPS scheme.

- `PUT /course/videos/{videoId}`
  - Edit video metadata, URL, chapter assignment, ordering, or status.

- `DELETE /course/videos/{videoId}`
  - Delete one explicit video metadata record.

### Video Playback Access

- `GET /course/videos/{videoId}/play-info`
  - Backend verifies that the caller is either the course teacher or a student selected into the course.
  - Response returns local `videoId`, `title`, `videoUrl`, `duration`, and available playback metadata.

### Student Learning

- `GET /course/{courseId}/learning-content`
  - Returns course chapters, videos, materials, and announcements.
  - The frontend builds the chapter tree from these records.

- `GET /course/videos/{videoId}/progress?studentId=...`
  - Returns the current student's resume playback position.

- `POST /course/videos/{videoId}/progress`
  - Saves `studentId`, `lastPosition`, and `duration`.
  - Upserts by `studentId + videoId`.

- `GET /course/videos/{videoId}/danmaku`
  - Returns visible shared danmaku ordered by timeline position.

- `POST /course/videos/{videoId}/danmaku`
  - Creates a shared danmaku record.
  - Reject empty content, overlong content, non-existent videos, and users without course access.

## Configuration

No Aliyun VOD configuration is required in this phase.

If a later phase adds server-side file storage or cloud video services, that integration should be designed separately instead of hidden inside the `VideoPlayer` component.

## Frontend Design

### Student Course Detail

Extend `frontend/src/views/CourseDetail.vue` with a course learning tab:

- Show latest announcements near the learning area.
- Show a chapter tree with videos and materials.
- Selecting a video fetches play info and passes `videoUrl` into `VideoPlayer`.
- `VideoPlayer` wraps browser video playback and exposes playback speed controls.
- Load saved progress before playback and seek to the last position.
- Save progress periodically during playback, on pause, and before leaving the page.
- Provide a danmaku switch.
- When danmaku is enabled, load shared danmaku and allow sending content at the current play time.

Create `frontend/src/components/VideoPlayer.vue`:

- Props: `src`, `poster`, `initialTime`, `danmakuList`, `danmakuEnabled`.
- Emits: `ready`, `timeupdate`, `pause`, `ended`, `progress-save`, and `send-danmaku`.
- Controls: play/pause, timeline, current time, duration, volume, fullscreen, and playback speed options such as `0.75x`, `1x`, `1.25x`, `1.5x`, and `2x`.
- Danmaku overlay renders shared comments according to video time when enabled.

### Teacher Course Management

Extend `frontend/src/views/TeacherDashboard.vue`:

- Add a course management entry.
- Allow creating and editing courses.
- Continue linking to teacher course detail pages.

Extend teacher view in `CourseDetail.vue` with a content management tab:

- Manage chapter and section records.
- Add, edit, publish, hide, and delete video URL records.
- Add and edit courseware URL records.
- Publish and edit announcements.

Use the existing Element Plus layout conventions. Avoid redesigning unrelated pages.

## Error Handling

- Invalid or empty video URL: reject on the backend and show a teacher-facing validation message.
- Browser playback error: show a student-facing message that the video cannot be played and keep the learning page usable.
- Progress save failure: do not stop playback; retry or show a low-noise warning.
- Danmaku validation failure: return and display a clear message.

## Permissions

- Teachers can manage only their own courses.
- Students can play videos only for courses they selected.
- Students can send danmaku only for selected courses.
- Teachers can view learning content for their own courses.
- Announcement creation and course content management are teacher-only.

The current project does not have full token validation at the service layer. This feature should follow the existing pattern of passing user IDs from the frontend, but isolate permission checks in service methods so real auth can replace this later.

## Verification

Run backend compilation:

```bash
mvn -pl common,course-service -am package -DskipTests
```

Run frontend build:

```bash
cd frontend
npm run build
```

Because the repository currently has no test directories or test dependencies, this phase relies on compilation, frontend build, and manual checks of the main API flows.

Manual checks:

- Teacher can create a course.
- Teacher can create chapters and attach materials.
- Teacher can add a playable video URL to a chapter.
- Teacher can edit, publish, hide, and delete a video metadata record.
- Student can open learning content for a selected course.
- Student cannot request play info for an unselected course.
- Student can play a URL-backed video through `VideoPlayer`.
- Student playback resumes from saved progress.
- Student can toggle danmaku and send shared danmaku.

## Out of Scope

- Local video upload, local media storage, server-side transcoding, or a custom media engine.
- Aliyun VOD integration.
- OSS-backed courseware upload.
- Danmaku moderation UI.
- Full JWT/service-layer authentication overhaul.
- New microservice creation.
- Adding a full automated test framework.
