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
import java.util.List;
import xyz.playedu.common.domain.LdapSyncUserDetail;

/** LDAP使用者同步詳情服務接口 */
public interface LdapSyncUserDetailService extends IService<LdapSyncUserDetail> {

    /**
     * 批量建立使用者同步詳情記錄
     *
     * @param details 使用者同步詳情記錄列表
     */
    void batchCreate(List<LdapSyncUserDetail> details);

    /**
     * 根據同步記錄ID和操作類型獲取使用者同步詳情
     *
     * @param recordId 同步記錄ID
     * @param action 操作類型，1-新增，2-更新，3-刪除，4-無變化
     * @return 使用者同步詳情列表
     */
    List<LdapSyncUserDetail> getByRecordIdAndAction(Integer recordId, Integer action);
}
