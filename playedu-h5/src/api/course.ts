import client from "./internal/httpClient";

// 線上課詳情
export function detail(id: number) {
  return client.get(`/api/v1/course/${id}`, {});
}

// 線上課課時詳情
export function play(courseId: number, id: number) {
  return client.get(`/api/v1/course/${courseId}/hour/${id}`, {});
}

// 獲取播放地址
export function playUrl(courseId: number, hourId: number) {
  return client.get(`/api/v1/course/${courseId}/hour/${hourId}/play`, {});
}

// 記錄學員觀看時長
export function record(courseId: number, hourId: number, duration: number) {
  return client.post(`/api/v1/course/${courseId}/hour/${hourId}/record`, {
    duration,
  });
}

//觀看ping
export function playPing(courseId: number, hourId: number) {
  return client.post(`/api/v1/course/${courseId}/hour/${hourId}/ping`, {});
}

//最近學習課程
export function latestLearn() {
  return client.get(`/api/v1/user/latest-learn`, {});
}

//下載課件
export function downloadAttachment(courseId: number, id: number) {
  return client.get(`/api/v1/course/${courseId}/attach/${id}/download`, {});
}
