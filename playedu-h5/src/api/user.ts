import client from "./internal/httpClient";

export function detail() {
  return client.get("/api/v1/user/detail", {});
}

// 修改密碼
export function password(oldPassword: string, newPassword: string) {
  return client.put("/api/v1/user/password", {
    old_password: oldPassword,
    new_password: newPassword,
  });
}

// 學員課程
export function coursesCategories() {
  return client.get("/api/v1/category/all", {});
}
export function courses(depId: number, categoryId: number) {
  return client.get("/api/v1/user/courses", {
    dep_id: depId,
    category_id: categoryId,
  });
}

// 修改頭像
export function avatar(params: any) {
  return client.put("/api/v1/user/avatar", params);
}
