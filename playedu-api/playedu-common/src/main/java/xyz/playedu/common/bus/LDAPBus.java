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
package xyz.playedu.common.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.naming.NamingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.playedu.common.domain.Department;
import xyz.playedu.common.domain.LdapDepartment;
import xyz.playedu.common.domain.LdapSyncDepartmentDetail;
import xyz.playedu.common.domain.LdapSyncRecord;
import xyz.playedu.common.domain.LdapSyncUserDetail;
import xyz.playedu.common.domain.LdapUser;
import xyz.playedu.common.domain.User;
import xyz.playedu.common.exception.NotFoundException;
import xyz.playedu.common.service.*;
import xyz.playedu.common.types.LdapConfig;
import xyz.playedu.common.types.config.S3Config;
import xyz.playedu.common.util.HelperUtil;
import xyz.playedu.common.util.S3Util;
import xyz.playedu.common.util.ldap.LdapTransformDepartment;
import xyz.playedu.common.util.ldap.LdapTransformUser;
import xyz.playedu.common.util.ldap.LdapUtil;

@Component
@Slf4j
public class LDAPBus {

    @Autowired private AppConfigService appConfigService;

    @Autowired private DepartmentService departmentService;

    @Autowired private LdapDepartmentService ldapDepartmentService;

    @Autowired private LdapUserService ldapUserService;

    @Autowired private UserService userService;

    @Autowired private LdapSyncRecordService ldapSyncRecordService;

    @Autowired private LdapSyncDepartmentDetailService ldapSyncDepartmentDetailService;

    @Autowired private LdapSyncUserDetailService ldapSyncUserDetailService;

    public boolean enabledLDAP() {
        return appConfigService.enabledLdapLogin();
    }

    /** 檢查是否有進行中的同步任務 */
    public boolean hasSyncInProgress() {
        return ldapSyncRecordService.hasSyncInProgress();
    }

    /**
     * 執行LDAP同步並記錄同步數據
     *
     * @param adminId 執行同步的管理員ID，0爲系統自動執行
     * @return 同步記錄ID
     */
    public Integer syncAndRecord(Integer adminId)
            throws NamingException, IOException, NotFoundException {
        // 檢查是否有進行中的同步任務
        if (hasSyncInProgress()) {
            throw new RuntimeException("有正在進行的LDAP同步任務，請稍後再試");
        }

        // 建立同步記錄
        LdapSyncRecord record = ldapSyncRecordService.create(adminId);

        try {
            // 獲取LDAP設定
            LdapConfig ldapConfig = appConfigService.ldapConfig();

            // 查詢LDAP數據（只查詢一次）
            List<LdapTransformDepartment> departments =
                    LdapUtil.departments(ldapConfig, ldapConfig.getBaseDN());
            List<LdapTransformUser> users = LdapUtil.users(ldapConfig, ldapConfig.getBaseDN());

            // 使用查詢的數據進行統計
            Map<String, Object> result = collectSyncStatistics(departments, users);

            // 將同步數據保存到S3
            String s3FilePath = saveDataToS3(result, record.getId());

            // 收集部門和使用者的詳細同步信息
            List<LdapSyncDepartmentDetail> departmentDetails =
                    collectDepartmentSyncDetails(record.getId(), departments);
            List<LdapSyncUserDetail> userDetails = collectUserSyncDetails(record.getId(), users);

            // 使用同樣的數據執行實際同步
            departmentSync(departments);
            userSync(users);

            // 保存部門和使用者的詳細同步信息
            ldapSyncDepartmentDetailService.batchCreate(departmentDetails);
            ldapSyncUserDetailService.batchCreate(userDetails);

            // 更新同步記錄
            ldapSyncRecordService.updateSyncResult(
                    record.getId(),
                    1, // 成功
                    s3FilePath,
                    (Integer) result.get("totalDepartmentCount"),
                    (Integer) result.get("createdDepartmentCount"),
                    (Integer) result.get("updatedDepartmentCount"),
                    (Integer) result.get("deletedDepartmentCount"),
                    (Integer) result.get("totalUserCount"),
                    (Integer) result.get("createdUserCount"),
                    (Integer) result.get("updatedUserCount"),
                    (Integer) result.get("deletedUserCount"),
                    (Integer) result.get("bannedUserCount"));

            return record.getId();
        } catch (Exception e) {
            // 記錄同步失敗
            ldapSyncRecordService.updateSyncFailed(record.getId(), e.getMessage());
            log.error("LDAP同步失敗", e);
            throw e;
        }
    }

    /**
     * 收集部門同步詳情
     *
     * @param recordId 同步記錄ID
     * @param departments LDAP部門數據
     * @return 部門同步詳情列表
     */
    private List<LdapSyncDepartmentDetail> collectDepartmentSyncDetails(
            Integer recordId, List<LdapTransformDepartment> departments) throws NotFoundException {
        List<LdapSyncDepartmentDetail> details = new ArrayList<>();
        Date now = new Date();

        // 讀取已經同步的記錄
        Map<String, LdapDepartment> ldapDepartments =
                ldapDepartmentService.all().stream()
                        .collect(Collectors.toMap(LdapDepartment::getUuid, e -> e));

        // 記錄新增和更新的部門
        for (LdapTransformDepartment dept : departments) {
            LdapDepartment existingDept = ldapDepartments.get(dept.getUuid());
            LdapSyncDepartmentDetail detail = new LdapSyncDepartmentDetail();
            detail.setRecordId(recordId);
            detail.setUuid(dept.getUuid());
            detail.setDn(dept.getDn());

            // 從DN中提取部門名稱
            String[] parts = dept.getDn().split(",");
            String name = parts[parts.length - 1].replace("ou=", "");
            detail.setName(name);
            detail.setCreatedAt(now);

            if (existingDept == null) {
                // 新增部門
                detail.setAction(1);
            } else if (!existingDept.getDn().equals(dept.getDn())) {
                // 更新部門
                detail.setDepartmentId(existingDept.getDepartmentId());
                detail.setAction(2);
            } else {
                // 無變化
                detail.setDepartmentId(existingDept.getDepartmentId());
                detail.setAction(4);
            }

            details.add(detail);
        }

        // 記錄刪除的部門
        List<String> uuidList = departments.stream().map(LdapTransformDepartment::getUuid).toList();
        List<LdapDepartment> deletedDepts = ldapDepartmentService.notChunkByUUIDList(uuidList);
        if (deletedDepts != null && !deletedDepts.isEmpty()) {
            for (LdapDepartment dept : deletedDepts) {
                LdapSyncDepartmentDetail detail = new LdapSyncDepartmentDetail();
                detail.setRecordId(recordId);
                detail.setDepartmentId(dept.getDepartmentId());
                detail.setUuid(dept.getUuid());
                detail.setDn(dept.getDn());

                // 獲取部門名稱
                Department department = departmentService.findOrFail(dept.getDepartmentId());
                detail.setName(department.getName());

                detail.setAction(3); // 刪除
                detail.setCreatedAt(now);
                details.add(detail);
            }
        }

        return details;
    }

    /**
     * 收集使用者同步詳情
     *
     * @param recordId 同步記錄ID
     * @param users LDAP使用者數據
     * @return 使用者同步詳情列表
     */
    private List<LdapSyncUserDetail> collectUserSyncDetails(
            Integer recordId, List<LdapTransformUser> users) {
        List<LdapSyncUserDetail> details = new ArrayList<>();
        Date now = new Date();

        // 處理新增和更新的使用者
        for (LdapTransformUser user : users) {
            LdapSyncUserDetail detail = new LdapSyncUserDetail();
            detail.setRecordId(recordId);
            detail.setUuid(user.getId());
            detail.setDn(user.getDn());
            detail.setCn(user.getCn());
            detail.setUid(user.getUid());
            detail.setEmail(user.getEmail());
            detail.setOu(String.join(",", user.getOu()));
            detail.setCreatedAt(now);

            // 查找現有使用者
            LdapUser existingUser = ldapUserService.findByUUID(user.getId());

            // 檢查使用者是否被禁止
            if (user.isBan()) {
                // 標記爲禁止的使用者
                detail.setAction(5); // 5-禁止的使用者
                if (existingUser != null) {
                    detail.setUserId(existingUser.getUserId());
                }
                details.add(detail);
                continue;
            }

            if (existingUser == null) {
                // 新增使用者
                detail.setAction(1);
            } else {
                // 檢查是否有變更
                boolean hasChanges = false;
                if (!user.getCn().equals(existingUser.getCn())) {
                    hasChanges = true;
                }

                String newOU = String.join(",", user.getOu());
                if (!newOU.equals(existingUser.getOu())) {
                    hasChanges = true;
                }

                // 設置使用者ID
                detail.setUserId(existingUser.getUserId());

                if (hasChanges) {
                    // 更新使用者
                    detail.setAction(2);
                } else {
                    // 無變化
                    detail.setAction(4);
                }
            }

            details.add(detail);
        }

        // 處理刪除的使用者
        List<String> uuidList =
                users.stream().filter(u -> !u.isBan()).map(LdapTransformUser::getId).toList();

        // 獲取所有現有的LDAP使用者記錄
        List<LdapUser> allLdapUsers = ldapUserService.list();

        // 過濾出不在當前LDAP使用者列表中的使用者
        List<LdapUser> deletedUsers =
                allLdapUsers.stream().filter(lu -> !uuidList.contains(lu.getUuid())).toList();

        for (LdapUser deletedUser : deletedUsers) {
            LdapSyncUserDetail detail = new LdapSyncUserDetail();
            detail.setRecordId(recordId);
            detail.setUserId(deletedUser.getUserId());
            detail.setUuid(deletedUser.getUuid());
            detail.setDn(deletedUser.getDn());
            detail.setCn(deletedUser.getCn());
            detail.setUid(deletedUser.getUid());
            detail.setEmail(deletedUser.getEmail());
            detail.setOu(deletedUser.getOu());
            detail.setAction(3); // 刪除
            detail.setCreatedAt(now);

            details.add(detail);
        }

        return details;
    }

    /** 收集同步統計數據 */
    private Map<String, Object> collectSyncStatistics(
            List<LdapTransformDepartment> departments, List<LdapTransformUser> users) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> syncData = new HashMap<>();

        // 部門同步統計
        int totalDepartmentCount = 0;
        int createdDepartmentCount = 0;
        int updatedDepartmentCount = 0;
        int deletedDepartmentCount = 0;

        // 使用者同步統計
        int totalUserCount = 0;
        int createdUserCount = 0;
        int updatedUserCount = 0;
        int deletedUserCount = 0;
        int bannedUserCount = 0;

        // 處理部門數據
        if (departments != null && !departments.isEmpty()) {
            syncData.put("departments", departments);
            totalDepartmentCount = departments.size();

            // 讀取已經同步的記錄
            Map<String, LdapDepartment> ldapDepartments =
                    ldapDepartmentService.all().stream()
                            .collect(Collectors.toMap(LdapDepartment::getUuid, e -> e));

            // 計算新增和更新的部門
            for (LdapTransformDepartment dept : departments) {
                LdapDepartment existingDept = ldapDepartments.get(dept.getUuid());
                if (existingDept == null) {
                    createdDepartmentCount++;
                } else if (!existingDept.getDn().equals(dept.getDn())) {
                    updatedDepartmentCount++;
                }
            }

            // 計算刪除的部門
            List<String> uuidList =
                    departments.stream().map(LdapTransformDepartment::getUuid).toList();
            List<LdapDepartment> ldapDepartmentList =
                    ldapDepartmentService.notChunkByUUIDList(uuidList);
            deletedDepartmentCount = ldapDepartmentList != null ? ldapDepartmentList.size() : 0;
        }

        // 處理使用者數據
        if (users != null && !users.isEmpty()) {
            syncData.put("users", users);
            totalUserCount = users.size();

            // 計算被禁止的使用者數量
            bannedUserCount = (int) users.stream().filter(LdapTransformUser::isBan).count();

            // 計算新增、更新的使用者
            for (LdapTransformUser user : users) {
                if (user.isBan()) {
                    continue;
                }

                LdapUser existingUser = ldapUserService.findByUUID(user.getId());
                if (existingUser == null) {
                    createdUserCount++;
                } else {
                    // 檢查使用者信息是否有變化
                    boolean hasChanges = false;
                    if (!user.getCn().equals(existingUser.getCn())) {
                        hasChanges = true;
                    }

                    String newOU = String.join(",", user.getOu());
                    if (!newOU.equals(existingUser.getOu())) {
                        hasChanges = true;
                    }

                    if (hasChanges) {
                        updatedUserCount++;
                    }
                }
            }
        }

        // 將同步結果數據存儲到結果對象中
        result.put("data", syncData);
        result.put("totalDepartmentCount", totalDepartmentCount);
        result.put("createdDepartmentCount", createdDepartmentCount);
        result.put("updatedDepartmentCount", updatedDepartmentCount);
        result.put("deletedDepartmentCount", deletedDepartmentCount);
        result.put("totalUserCount", totalUserCount);
        result.put("createdUserCount", createdUserCount);
        result.put("updatedUserCount", updatedUserCount);
        result.put("deletedUserCount", deletedUserCount);
        result.put("bannedUserCount", bannedUserCount);

        return result;
    }

    /** 將同步數據保存到S3 */
    private String saveDataToS3(Map<String, Object> data, Integer recordId) throws IOException {
        // 將數據轉換爲JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);
        byte[] jsonBytes = jsonData.getBytes(StandardCharsets.UTF_8);

        // 生成保存路徑
        String filename = "ldap_sync_" + recordId + "_" + new Date().getTime() + ".json";
        String savePath = "ldap/sync/" + filename;

        // 保存到S3
        S3Config s3Config = appConfigService.getS3Config();
        S3Util s3Util = new S3Util(s3Config);
        s3Util.saveBytes(jsonBytes, savePath, "application/json");

        return savePath;
    }

    /**
     * 執行部門同步 - 提供現有的LDAP部門數據
     *
     * @param ouList 已獲取的LDAP部門數據
     */
    public void departmentSync(List<LdapTransformDepartment> ouList) throws NotFoundException {
        if (ouList == null || ouList.isEmpty()) {
            return;
        }

        // 讀取已經同步的記錄
        Map<String, LdapDepartment> ldapDepartments =
                ldapDepartmentService.all().stream()
                        .collect(Collectors.toMap(LdapDepartment::getUuid, e -> e));

        // 本地緩存表
        HashMap<String, Integer> depIdKeyByName = new HashMap<>();

        // 全局排序計數
        Integer sort = 0;

        // 新增+編輯的處理
        for (LdapTransformDepartment ldapTransformDepartment : ouList) {
            String uuid = ldapTransformDepartment.getUuid();
            String dn = ldapTransformDepartment.getDn();
            String[] tmpChains = dn.replace("ou=", "").split(",");
            String prevName = "";

            log.info("#####START#####[dn:{},uuid:{}]", dn, uuid);

            // 同步記錄
            LdapDepartment tmpLdapDepartment = ldapDepartments.get(uuid);
            if (tmpLdapDepartment != null && tmpLdapDepartment.getDn().equals(dn)) {
                // 當前部門已經同步 && 未發生改變
                log.info("LDAP-部門同步處理-未發生改變|dn:{}", dn);
                continue;
            }

            // 執行到這裏的有兩種情況：
            // 1.部門未同步
            // 2.部門已同步，但是發生了變化
            // |-2.1 部門名稱修改
            // |-2.2 部門上級名稱修改
            // |-2.3 層級發生變動(增加層級|減少層級)

            int length = tmpChains.length;

            for (int i = 0; i < length; i++) {
                sort++;
                int parentId = 0;

                String tmpName = tmpChains[i];

                // 部門的鏈名=>父部門1,父部門2,子部門
                String fullName = tmpName;
                if (!prevName.isEmpty()) {
                    fullName = prevName + "," + tmpName;
                    // 取父級ID
                    parentId = depIdKeyByName.get(prevName);
                }

                log.info(
                        "LDAP-部門同步處理-鏈處理|ctx=[dn={},fullName:{},tmpName:{},parentId:{},sort:{}]",
                        dn,
                        fullName,
                        tmpName,
                        parentId,
                        sort);

                if (i + 1 == length && tmpLdapDepartment != null) {
                    // OU鏈發生了改變
                    // 1.部門名改變
                    // 2.上級部門名改變
                    // 3.層級改變

                    log.info("LDAP-部門同步處理-OU鏈發生改變|ctx=[新:{},舊:{}]", dn, tmpLdapDepartment.getDn());

                    Department tmpDepartment =
                            departmentService.findOrFail(tmpLdapDepartment.getDepartmentId());
                    if (!tmpDepartment.getName().equals(tmpName)
                            || !tmpLdapDepartment
                                    .getDn()
                                    .replace("ou=" + tmpName, "")
                                    .equals(dn.replaceAll("ou=" + tmpName, ""))) {
                        departmentService.update(tmpDepartment, tmpName, parentId, sort);
                    }

                    // 更新同步記錄
                    tmpLdapDepartment.setDn(dn); // 最新的DN
                    ldapDepartmentService.updateDnById(tmpLdapDepartment.getId(), dn);
                    // 更新本地緩存
                    ldapDepartments.put(uuid, tmpLdapDepartment);
                    // 更新本地緩存
                    depIdKeyByName.put(fullName, tmpDepartment.getId());
                } else {
                    // 檢查本地是否有緩存
                    Integer depId = depIdKeyByName.get(fullName);
                    log.info("LDAP-部門同步處理-從緩存查詢depId|ctx=[fullName:{},depId:{}]", fullName, depId);
                    if (depId == null) {
                        Department tmpDep = departmentService.findByName(tmpName, parentId);
                        if (tmpDep != null) {
                            depId = tmpDep.getId();
                            log.info(
                                    "LDAP-部門同步處理-從資料庫查詢depId|ctx=[fullName:{},depId:{}]",
                                    fullName,
                                    depId);
                        } else {
                            depId = departmentService.create(tmpName, parentId, sort);
                            log.info(
                                    "LDAP-部門同步處理-新增部門|ctx=[fullName:{},depId:{}]", fullName, depId);
                        }

                        // 寫入本地緩存
                        depIdKeyByName.put(fullName, depId);
                    }
                }

                if (i + 1 == length && tmpLdapDepartment == null) {
                    Integer tmpDepId = depIdKeyByName.get(fullName);
                    // 建立同步記錄
                    ldapDepartmentService.create(tmpDepId, uuid, dn);

                    // 寫入本地緩存
                    LdapDepartment storedLdapDepartment = new LdapDepartment();
                    storedLdapDepartment.setUuid(uuid);
                    storedLdapDepartment.setDn(dn);
                    storedLdapDepartment.setDepartmentId(tmpDepId);
                    ldapDepartments.put(uuid, storedLdapDepartment);
                }

                // 父級疊加
                prevName = fullName;
            }
        }

        // 刪除的處理
        List<String> uuidList = ouList.stream().map(LdapTransformDepartment::getUuid).toList();
        List<LdapDepartment> ldapDepartmentList =
                ldapDepartmentService.notChunkByUUIDList(uuidList);
        if (ldapDepartmentList != null && !ldapDepartmentList.isEmpty()) {
            for (LdapDepartment ldapDepartment : ldapDepartmentList) {
                // 刪除本地部門
                departmentService.destroy(ldapDepartment.getDepartmentId());
                // 刪除同步記錄
                ldapDepartmentService.destroy(ldapDepartment.getId());
            }
        }
    }

    /**
     * 執行使用者同步 - 提供現有的LDAP使用者數據
     *
     * @param userList 已獲取的LDAP使用者數據
     */
    public void userSync(List<LdapTransformUser> userList) {
        if (userList == null || userList.isEmpty()) {
            return;
        }

        Integer defaultAvatar = appConfigService.defaultAvatar();

        for (LdapTransformUser ldapTransformUser : userList) {
            if (ldapTransformUser.isBan()) {
                // 檢查使用者是否已在系統中存在
                LdapUser existingLdapUser = ldapUserService.findByUUID(ldapTransformUser.getId());
                if (existingLdapUser == null) {
                    // 對於新的被禁止使用者，不同步到系統
                    log.info(
                            "LDAP-使用者同步-新使用者被禁止不同步|ctx=[dn:{},uuid={}]",
                            ldapTransformUser.getDn(),
                            ldapTransformUser.getId());
                    continue;
                }
            }

            singleUserSync(ldapTransformUser, defaultAvatar);
        }
    }

    public User singleUserSync(LdapTransformUser ldapTransformUser, Integer defaultAvatar) {
        log.info(
                "*****START*****LDAP-使用者同步-開始|ctx=[dn:{},uuid:{}]",
                ldapTransformUser.getDn(),
                ldapTransformUser.getId());

        // LDAP使用者的名字
        String ldapUserName = ldapTransformUser.getCn();

        // 將LDAP使用者所屬的部門同步到本地
        Integer depId = departmentService.createWithChainList(ldapTransformUser.getOu());
        Integer[] depIds = depId == 0 ? null : new Integer[] {depId};

        User user;

        // LDAP同步記錄
        LdapUser ldapUser = ldapUserService.findByUUID(ldapTransformUser.getId());

        // 計算將LDAP使用者關聯到本地users表的email欄位值
        String localUserEmail = ldapTransformUser.getUid();

        if (ldapUser == null) {
            // 檢測localUserEmail是否存在
            if (userService.find(localUserEmail) != null) {
                log.info("LDAP-使用者同步-email重複|ctx=[email:{}]", localUserEmail);
                return null;
            }

            // 建立同步記錄
            ldapUser = ldapUserService.store(ldapTransformUser);

            // 建立本地user
            user =
                    userService.createWithDepIds(
                            localUserEmail,
                            ldapUserName,
                            defaultAvatar,
                            HelperUtil.randomString(10),
                            "",
                            depIds);

            // 將LDAP緩存數據與本地user關聯
            ldapUserService.updateUserId(ldapUser.getId(), user.getId());

            log.info(
                    "LDAP-使用者同步-錄入數據|ctx=[userId:{},ldapUserId:{}]", user.getId(), ldapUser.getId());
        } else {
            log.info(
                    "LDAP-使用者同步-檢測變化值|ctx=[新dn:{},舊dn:{}]",
                    ldapTransformUser.getDn(),
                    ldapUser.getDn());

            user = userService.find(ldapUser.getUserId());

            if (user == null) {
                // 同步記錄建立了，但是user卻沒建立
                log.info(
                        "LDAP-使用者同步-同步記錄存在但user不存在|ctx=[dn:{},ldapUserId:{}]",
                        ldapTransformUser.getDn(),
                        ldapUser.getId());
                user =
                        userService.createWithDepIds(
                                localUserEmail,
                                ldapUserName,
                                defaultAvatar,
                                HelperUtil.randomString(10),
                                "",
                                depIds);
            }

            // 帳號修改[帳號有可能是email也有可能是uid]
            if (!localUserEmail.equals(user.getEmail())) {
                // 檢測localUserEmail是否存在
                if (userService.find(localUserEmail) != null) {
                    localUserEmail = HelperUtil.randomString(5) + "_" + localUserEmail;
                }
                userService.updateEmail(user.getId(), localUserEmail);
            }

            // ldap-email的變化
            if (!ldapUser.getEmail().equals(ldapTransformUser.getEmail())) {
                ldapUserService.updateEmail(ldapUser.getId(), ldapTransformUser.getEmail());
            }

            // ldap-uid的變化
            if (!ldapUser.getUid().equals(ldapTransformUser.getUid())) {
                ldapUserService.updateUid(ldapUser.getId(), ldapTransformUser.getUid());
            }

            // 名字同步修改
            if (!ldapUserName.equals(ldapUser.getCn())) {
                userService.updateName(user.getId(), ldapUserName);
                ldapUserService.updateCN(ldapUser.getId(), ldapUserName);
            }

            // 部門修改同步
            String newOU = String.join(",", ldapTransformUser.getOu());
            if (!newOU.equals(ldapUser.getOu())) {
                userService.updateDepId(user.getId(), depIds);
                ldapUserService.updateOU(ldapUser.getId(), newOU);

                if (ldapTransformUser.isBan()) {
                    log.info("LDAP-使用者同步-被禁止使用者部門已更新|ctx=[userId:{},新OU:{}]", user.getId(), newOU);
                }
            }

            // DN變化
            if (!ldapTransformUser.getDn().equals(ldapUser.getDn())) {
                ldapUserService.updateDN(ldapUser.getId(), ldapTransformUser.getDn());
            }
        }

        return user;
    }
}
