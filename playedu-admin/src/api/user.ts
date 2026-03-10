import client from "./internal/httpClient";

//params可選值如下：
// name - 姓名
// nickname - 暱稱
// email - 電子郵件
// id_card - 身份證號
// is_active - 是否激活[1:是,0:否]
// is_lock - 是否鎖定[1:是,0:否]
// is_verify - 是否完成實名認證[1:是,0:否]
// is_set_password - 是否設置密碼[1:是,0:否]
// created_at - 註冊時間區間過濾 - 格式(字符串): "開始時間,結束時間"
// dep_ids - 部門id字符串 - 格式(字符串): 1,2,3
// sort_field - 排序欄位(預設值:id) 可選值：id,created_at
// sort_algo - 排序算法(預設值:desc) 可選值：asc,desc
export function userList(page: number, size: number, params: object) {
  return client.get("/backend/v1/user/index", {
    page,
    size,
    ...params,
  });
}

export function createUser() {
  return client.get("/backend/v1/user/create", {});
}

export function storeUser(
  email: string,
  name: string,
  avatar: string,
  password: string,
  idCard: string,
  depIds: number[]
) {
  return client.post("/backend/v1/user/create", {
    email,
    name,
    avatar,
    password,
    id_card: idCard,
    dep_ids: depIds,
  });
}

export function user(id: number) {
  return client.get(`/backend/v1/user/${id}`, {});
}

export function updateUser(
  id: number,
  email: string,
  name: string,
  avatar: string,
  password: string,
  idCard: string,
  depIds: number[]
) {
  return client.put(`/backend/v1/user/${id}`, {
    email,
    name,
    avatar,
    password,
    id_card: idCard,
    dep_ids: depIds,
  });
}

export function destroyUser(id: number) {
  return client.destroy(`/backend/v1/user/${id}`);
}

//startline是表格真是數據的起始行號-用於提示哪一行數據存在問題
//users是一個二維字符串數組，每個數組的元素如下：[部門ids字符串,電子郵件,暱稱,密碼,姓名,身份證]
export function storeBatch(startLine: number, users: string[][]) {
  return client.post("/backend/v1/user/store-batch", {
    start_line: startLine,
    users: users,
  });
}

export function learnStats(id: number) {
  return client.get(`/backend/v1/user/${id}/learn-stats`, {});
}

export function learnHours(
  id: number,
  page: number,
  size: number,
  params: object
) {
  return client.get(`/backend/v1/user/${id}/learn-hours`, {
    page,
    size,
    ...params,
  });
}

export function learnCourses(
  id: number,
  page: number,
  size: number,
  params: object
) {
  return client.get(`/backend/v1/user/${id}/learn-courses`, {
    page,
    size,
    ...params,
  });
}

export function learnAllCourses(id: number) {
  return client.get(`/backend/v1/user/${id}/all-courses`, {});
}

export function departmentProgress(
  id: number,
  page: number,
  size: number,
  params: object
) {
  return client.get(`/backend/v1/department/${id}/users`, {
    page,
    size,
    ...params,
  });
}

export function learnCoursesProgress(
  id: number,
  courseId: number,
  params: any
) {
  return client.get(`/backend/v1/user/${id}/learn-course/${courseId} `, params);
}

export function destroyAllUserLearned(id: number, courseId: number) {
  return client.destroy(`/backend/v1/user/${id}/learn-course/${courseId}`);
}

export function destroyUserLearned(
  id: number,
  courseId: number,
  hourId: number
) {
  return client.destroy(
    `/backend/v1/user/${id}/learn-course/${courseId}/hour/${hourId}`
  );
}
