/*
 * Copyright (C) 2023 杭州白書科技有限公司
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.playedu.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.playedu.common.domain.LdapSyncRecord;
import xyz.playedu.common.types.paginate.PaginationResult;

public interface LdapSyncRecordService extends IService<LdapSyncRecord> {
    // 建立同步記錄
    LdapSyncRecord create(Integer adminId);

    // 更新同步結果
    void updateSyncResult(
            Integer id,
            Integer status,
            String s3FilePath,
            Integer totalDepartmentCount,
            Integer createdDepartmentCount,
            Integer updatedDepartmentCount,
            Integer deletedDepartmentCount,
            Integer totalUserCount,
            Integer createdUserCount,
            Integer updatedUserCount,
            Integer deletedUserCount,
            Integer bannedUserCount);

    // 更新同步狀態爲失敗並記錄錯誤信息
    void updateSyncFailed(Integer id, String errorMessage);

    // 分頁查詢
    PaginationResult<LdapSyncRecord> paginate(Integer page, Integer size);

    // 檢查是否有進行中的同步任務
    boolean hasSyncInProgress();

    // 獲取最近一次同步記錄
    LdapSyncRecord getLatestRecord();
}
