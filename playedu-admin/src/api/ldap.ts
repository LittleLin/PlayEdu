import client from "./internal/httpClient";

// 獲取同步記錄列表
export function getSyncRecords(params: { page?: number; size?: number }) {
  return client.get("/backend/v1/ldap/sync-records", params);
}

// 獲取單條同步記錄詳情
export function getSyncRecordDetail(id: number) {
  return client.get(`/backend/v1/ldap/sync-records/${id}`, {});
}

// 獲取同步記錄的詳細項目
export function getSyncRecordDetails(id: number, params: { 
  type: 'department' | 'user'; 
  action?: number; 
  page?: number; 
  size?: number 
}) {
  return client.get(`/backend/v1/ldap/sync-records/${id}/details`, params);
}

// 下載同步記錄數據
export function downloadSyncRecord(id: number) {
  return client.get(`/backend/v1/ldap/sync-records/${id}/download`, {});
} 