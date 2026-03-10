# LDAP同步記錄功能

本文檔介紹了PlayEdu系統中LDAP同步記錄功能的使用方法和實現細節。

## 功能概述

LDAP同步記錄功能主要實現了以下功能：

1. 記錄每次LDAP數據同步的統計數據：
   - 同步的部門和使用者總數
   - 新增、更新、刪除的部門和使用者數量
2. 將同步的LDAP數據保存到S3存儲中，方便後續查詢和下載
3. 記錄執行同步操作的管理員ID
4. 通過狀態控制防止短時間內多次提交同步請求
5. 記錄每次同步中每個部門和使用者的詳細變更情況

## 資料庫表

### 主同步記錄表

系統添加了新的資料庫表 `ldap_sync_record`，其結構如下：

```sql
CREATE TABLE `ldap_sync_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` int(11) NOT NULL DEFAULT 0 COMMENT '執行同步的管理員ID，0表示系統自動執行',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '狀態：0-進行中，1-成功，2-失敗',
  `s3_file_path` varchar(255) DEFAULT NULL COMMENT 'S3存儲中的檔案路徑',
  `total_department_count` int(11) NOT NULL DEFAULT 0 COMMENT '總部門數量',
  `created_department_count` int(11) NOT NULL DEFAULT 0 COMMENT '新增部門數量',
  `updated_department_count` int(11) NOT NULL DEFAULT 0 COMMENT '更新部門數量',
  `deleted_department_count` int(11) NOT NULL DEFAULT 0 COMMENT '刪除部門數量',
  `total_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '總使用者數量',
  `created_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '新增使用者數量',
  `updated_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '更新使用者數量',
  `deleted_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '刪除使用者數量',
  `banned_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '被禁止的使用者數量',
  `error_message` text DEFAULT NULL COMMENT '錯誤信息',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LDAP同步記錄表';
```

### 部門同步詳情表

記錄每個部門在同步過程中的詳細變更情況：

```sql
CREATE TABLE `ldap_sync_department_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `record_id` int(11) NOT NULL COMMENT '關聯的同步記錄ID',
  `department_id` int(11) DEFAULT NULL COMMENT '關聯的部門ID',
  `uuid` varchar(255) NOT NULL COMMENT 'LDAP部門UUID',
  `dn` varchar(255) NOT NULL COMMENT 'LDAP部門DN',
  `name` varchar(255) NOT NULL COMMENT '部門名稱',
  `action` tinyint(4) NOT NULL COMMENT '操作：1-新增，2-更新，3-刪除，4-無變化',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `record_id` (`record_id`),
  KEY `department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LDAP部門同步詳情表';
```

### 使用者同步詳情表

記錄每個使用者在同步過程中的詳細變更情況：

```sql
CREATE TABLE `ldap_sync_user_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `record_id` int(11) NOT NULL COMMENT '關聯的同步記錄ID',
  `user_id` int(11) DEFAULT NULL COMMENT '關聯的使用者ID',
  `uuid` varchar(255) NOT NULL COMMENT 'LDAP使用者UUID',
  `dn` varchar(255) NOT NULL COMMENT 'LDAP使用者DN',
  `cn` varchar(255) NOT NULL COMMENT '使用者名稱',
  `uid` varchar(255) NOT NULL COMMENT '使用者ID/登入名',
  `email` varchar(255) DEFAULT NULL COMMENT '使用者電子郵件',
  `ou` text DEFAULT NULL COMMENT '使用者部門路徑',
  `action` tinyint(4) NOT NULL COMMENT '操作：1-新增，2-更新，3-刪除，4-無變化，5-禁止',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `record_id` (`record_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LDAP使用者同步詳情表';
```

## API接口

系統提供了以下API接口用於LDAP同步操作和記錄管理：

### 1. 手動觸發LDAP同步

- **URL**: `/backend/v1/ldap/sync`
- **方法**: POST
- **權限**: `ldap:sync`
- **返回數據**:
  ```json
  {
    "code": 0,
    "data": {
      "record_id": 1 // 同步記錄ID
    },
    "message": "success"
  }
  ```

### 2. 獲取LDAP同步記錄列表

- **URL**: `/backend/v1/ldap/sync-records`
- **方法**: GET
- **權限**: `ldap:sync:records`
- **參數**:
  - `page`: 頁碼，預設1
  - `size`: 每頁條數，預設10
- **返回數據**:
  ```json
  {
    "code": 0,
    "data": {
      "data": [
        {
          "id": 1,
          "admin_id": 0,
          "status": 1,
          "s3_file_path": "ldap/sync/ldap_sync_1_1620000000000.json",
          "total_department_count": 10,
          "created_department_count": 5,
          "updated_department_count": 3,
          "deleted_department_count": 2,
          "total_user_count": 100,
          "created_user_count": 50,
          "updated_user_count": 30,
          "deleted_user_count": 20,
          "banned_user_count": 0,
          "error_message": null,
          "created_at": "2023-08-01 12:00:00",
          "updated_at": "2023-08-01 12:01:00"
        }
      ],
      "total": 100
    },
    "message": "success"
  }
  ```

### 3. 獲取LDAP同步記錄詳情

- **URL**: `/backend/v1/ldap/sync-records/{id}`
- **方法**: GET
- **權限**: `ldap:sync:records`
- **返回數據**:
  ```json
  {
    "code": 0,
    "data": {
      "id": 1,
      "admin_id": 0,
      "status": 1,
      "s3_file_path": "ldap/sync/ldap_sync_1_1620000000000.json",
      "total_department_count": 10,
      "created_department_count": 5,
      "updated_department_count": 3,
      "deleted_department_count": 2,
      "total_user_count": 100,
      "created_user_count": 50,
      "updated_user_count": 30,
      "deleted_user_count": 20,
      "banned_user_count": 0,
      "error_message": null,
      "created_at": "2023-08-01 12:00:00",
      "updated_at": "2023-08-01 12:01:00"
    },
    "message": "success"
  }
  ```

### 4. 獲取同步詳情

- **URL**: `/backend/v1/ldap/sync-records/{id}/details`
- **方法**: GET
- **權限**: `ldap:sync:records`
- **參數**:
  - `type`: 詳情類型，`department`=部門，`user`=使用者
  - `action`: 操作類型，當type=department時：0=全部，1=新增，2=更新，3=刪除，4=無變化；當type=user時：0=全部，1=新增，2=更新，3=刪除，4=無變化，5=禁止，預設0
  - `page`: 頁碼，預設1
  - `size`: 每頁條數，預設10
- **返回數據**:
  ```json
  {
    "code": 0,
    "data": {
      "records": [
        {
          // 當type=department時返回部門詳情
          "id": 1,
          "record_id": 1,
          "department_id": 10,
          "uuid": "12345678-1234-1234-1234-123456789012",
          "dn": "ou=HR,dc=example,dc=com",
          "name": "HR",
          "action": 1,
          "created_at": "2023-08-01 12:00:00"
        }
        // 或當type=user時返回使用者詳情
        {
          "id": 1,
          "record_id": 1,
          "user_id": 100,
          "uuid": "12345678-1234-1234-1234-123456789012",
          "dn": "cn=John Doe,ou=HR,dc=example,dc=com",
          "cn": "John Doe",
          "uid": "johndoe",
          "email": "john.doe@example.com",
          "ou": "HR",
          "action": 1,
          "created_at": "2023-08-01 12:00:00"
        }
      ],
      "total": 100,
      "size": 10,
      "current": 1,
      "pages": 10
    },
    "message": "success"
  }
  ```

### 5. 下載LDAP同步記錄數據

- **URL**: `/backend/v1/ldap/sync-records/{id}/download`
- **方法**: GET
- **權限**: `ldap:sync:records`
- **返回數據**:
  ```json
  {
    "code": 0,
    "data": {
      "url": "https://your-s3-domain.com/ldap/sync/ldap_sync_1_1620000000000.json"
    },
    "message": "success"
  }
  ```

## 定時同步

系統會每小時自動執行一次LDAP同步，同步過程和統計數據會記錄到 `ldap_sync_record` 表中。自動同步時 `admin_id` 欄位爲0。

## 權限說明

爲了使用LDAP同步記錄功能，需要爲管理員角色分配以下權限：

- `ldap:sync`: 允許手動觸發LDAP同步
- `ldap:sync:records`: 允許查看LDAP同步記錄

## 同步狀態說明

LDAP同步記錄的狀態欄位（`status`）有以下值：

- `0`: 進行中
- `1`: 成功
- `2`: 失敗

當狀態爲2（失敗）時，錯誤信息會記錄在 `error_message` 欄位中。

## 操作類型說明

詳細同步記錄中的操作類型（`action`）有以下值：

### 部門操作類型：
- `1`: 新增 - 首次在LDAP中發現的部門
- `2`: 更新 - 已存在但信息發生變化的部門
- `3`: 刪除 - 在LDAP中不再存在的部門
- `4`: 無變化 - 已存在且信息未發生變化的部門

### 使用者操作類型：
- `1`: 新增 - 首次在LDAP中發現的使用者
- `2`: 更新 - 已存在但信息發生變化的使用者
- `3`: 刪除 - 在LDAP中不再存在的使用者
- `4`: 無變化 - 已存在且信息未發生變化的使用者
- `5`: 禁止 - 在LDAP中被標記爲禁止狀態的使用者

## S3存儲

同步的LDAP數據會以JSON格式保存到S3存儲中，路徑格式爲：

```
ldap/sync/ldap_sync_{記錄ID}_{時間戳}.json
```

JSON檔案包含完整的同步數據，包括所有部門和使用者的信息。

## 實現細節

系統在每次LDAP同步時都會進行以下操作：

1. 建立主同步記錄，初始狀態爲"進行中"
2. 獲取LDAP設定信息並查詢所有LDAP部門和使用者數據
3. 收集統計信息並保存到S3
4. 收集每個部門和使用者的詳細變更情況
5. 執行實際的同步操作，包括部門同步和使用者同步
6. 保存部門和使用者的詳細變更記錄
7. 更新主同步記錄狀態爲"成功"

如果同步過程中出現異常，系統會捕獲異常信息並更新主同步記錄狀態爲"失敗"。 