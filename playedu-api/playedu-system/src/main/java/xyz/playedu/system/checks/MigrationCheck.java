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
package xyz.playedu.system.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.playedu.system.service.MigrationService;

@Order(10)
@Component
@Slf4j
public class MigrationCheck implements CommandLineRunner {

    public static final List<Map<String, String>> TABLE_SQL =
            new ArrayList<>() {
                {
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "migrations");
                                    put("name", "20231208_14_00_00_migrations");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `migrations` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `migration` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '變更記錄',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程章節表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_permissions");
                                    put("name", "20231208_14_00_00_admin_permissions");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_permissions` (
                                                      `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '類型[行爲:action,數據:data]',
                                                      `group_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '分組',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '權限名',
                                                      `slug` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'slug',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'SQL變更記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_logs");
                                    put("name", "20231208_14_00_00_admin_logs");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_logs`
                                                    (
                                                        `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                        `admin_id`       int(11) NOT NULL DEFAULT 0 COMMENT '管理員ID',
                                                        `admin_name`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理員姓名',
                                                        `module`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '模塊',
                                                        `title`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '請求方法標題',
                                                        `opt`            int(2) NOT NULL DEFAULT 0 COMMENT '操作指令（0其它 1新增 2修改 3刪除 4登入 5登出）',
                                                        `method`         varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '請求方法',
                                                        `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '請求方式POST,GET,PUT,DELETE',
                                                        `url`            varchar(266) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '請求URL',
                                                        `param`          mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '請求參數',
                                                        `result`         mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '返回參數',
                                                        `ip`             varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT 'IP',
                                                        `ip_area`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '地址',
                                                        `error_msg`      mediumtext COLLATE utf8mb4_unicode_ci COMMENT '錯誤消息',
                                                        `created_at`     timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                        PRIMARY KEY (`id`),
                                                        KEY              `a_m_o` (`admin_id`,`module`,`opt`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員操作日誌記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_role_permission");
                                    put("name", "20231208_14_00_00_admin_role_permission");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_role_permission` (
                                                      `role_id` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '角色ID',
                                                      `perm_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '權限ID',
                                                      KEY `role_id` (`role_id`),
                                                      KEY `perm_id` (`perm_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員角色權限關聯表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_roles");
                                    put("name", "20231208_14_00_00_admin_roles");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_roles` (
                                                      `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '角色名',
                                                      `slug` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'slug',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `slug` (`slug`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員角色表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_user_role");
                                    put("name", "20231208_14_00_00_admin_user_role");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_user_role` (
                                                      `admin_id` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '管理員ID',
                                                      `role_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '角色ID',
                                                      KEY `admin_id` (`admin_id`),
                                                      KEY `role_id` (`role_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員角色關聯表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "admin_users");
                                    put("name", "20231208_14_00_00_admin_users");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `admin_users` (
                                                      `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
                                                      `email` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '電子郵件',
                                                      `password` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密碼',
                                                      `salt` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'salt',
                                                      `login_ip` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '登入IP',
                                                      `login_at` timestamp NULL DEFAULT NULL COMMENT '登入時間',
                                                      `is_ban_login` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1禁止登入,0否',
                                                      `login_times` int(11) NOT NULL DEFAULT 0 COMMENT '登入次數',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改時間',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `administrators_email_unique` (`email`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "app_config");
                                    put("name", "20231208_14_00_00_app_config");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `app_config` (
                                                      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `group_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '分組',
                                                      `name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '名稱',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `field_type` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '欄位類型',
                                                      `key_name` varchar(188) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '鍵',
                                                      `key_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '值',
                                                      `option_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '可選值',
                                                      `is_private` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否私密信息',
                                                      `help` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '幫助信息',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `is_hidden` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1顯示,0否',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `app_config_key_unique` (`key_name`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '系統設定表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "course_attachment");
                                    put("name", "20231208_14_00_00_course_attachment");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `course_attachment`
                                                    (
                                                        `id`         int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                        `course_id`  int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                        `sort`       int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                        `title`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '附件名',
                                                        `type`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '附件類型',
                                                        `rid`        int(11) NOT NULL DEFAULT 0 COMMENT '資源ID',
                                                        `created_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                        PRIMARY KEY (`id`),
                                                        KEY          `course_id` (`course_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程附件表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "course_attachment_download_log");
                                    put("name", "20231208_14_00_00_course_attachment_download_log");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `course_attachment_download_log`
                                                    (
                                                        `id`                    int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                        `user_id`               int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                        `course_id`             int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                        `title`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '課程標題',
                                                        `courser_attachment_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程附件ID',
                                                        `rid`                   int(11) NOT NULL DEFAULT 0 COMMENT '資源ID',
                                                        `ip`                    varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '下載IP',
                                                        `created_at`            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                        PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程附件下載日誌記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "course_chapters");
                                    put("name", "20231208_14_00_00_course_chapters");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `course_chapters` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '章節名',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '管理員權限表';
                                                    """);
                                }
                            });
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "course_department_user");
                                    put("name", "20231208_14_00_00_course_department_user");
                                    put(
                                            "sql",
                                            """
                                                     CREATE TABLE `course_department_user` (
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `range_id` int(11) NOT NULL DEFAULT 0 COMMENT '指派範圍ID',
                                                      `type` int(11) NOT NULL DEFAULT 0 COMMENT '指派範圍類型[0:部門,1:學員]',
                                                      KEY `course_id` ( `course_id` ),
                                                    KEY `range_id` ( `range_id` )
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程指派範圍表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "course_hour");
                                    put("name", "20231208_14_00_00_course_hour");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `course_hour` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `chapter_id` int(11) NOT NULL DEFAULT 0 COMMENT '章節ID',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '課時名',
                                                      `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '課時類型',
                                                      `rid` int(11) NOT NULL DEFAULT 0 COMMENT '資源ID',
                                                      `duration` int(11) NOT NULL DEFAULT 0 COMMENT '時長[s]',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                      `deleted` tinyint(1) unsigned NULL DEFAULT 0 COMMENT '刪除標誌[0:存在,1:刪除]',
                                                      PRIMARY KEY (`id`),
                                                      KEY `course_id` (`course_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程課時表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "courses");
                                    put("name", "20230406_16_51_17_1111_courses");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `courses` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '課程標題',
                                                      `thumb` int(11) NOT NULL DEFAULT 0 COMMENT '封面',
                                                      `charge` int(11) NOT NULL DEFAULT 0 COMMENT '課程價格(分)',
                                                      `short_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '簡介',
                                                      `class_hour` int(11) NOT NULL DEFAULT 0 COMMENT '課時數',
                                                      `is_show` tinyint(4) NOT NULL DEFAULT 0 COMMENT '顯示[1:是,0:否]',
                                                      `is_required` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1:必修,0:選修',
                                                      `sort_at` timestamp NULL DEFAULT NULL COMMENT '排序時間',
                                                      `extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '其它規則[設置]',
                                                      `admin_id` int(11) NOT NULL DEFAULT 0 COMMENT '管理員ID',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      `deleted_at` timestamp NULL DEFAULT NULL COMMENT '刪除時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "departments");
                                    put("name", "20230406_16_51_17_1111_departments");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `departments` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '部門名',
                                                      `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父ID',
                                                      `parent_chain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '父鏈',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `from_scene` int(11) NOT NULL DEFAULT 0 COMMENT '來源[0:本地]',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員上傳圖片日誌記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "resource_categories");
                                    put("name", "20231208_14_00_00_resource_categories");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `resource_categories` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父ID',
                                                      `parent_chain` varchar(2550) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '父鏈',
                                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '分類名',
                                                      `sort` int(11) NOT NULL DEFAULT 0 COMMENT '升序',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '部門表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "resource");
                                    put("name", "20231208_14_00_00_resource");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `resource` (
                                                       `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                       `admin_id` int(11) NOT NULL DEFAULT 0 COMMENT '管理員ID',
                                                       `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '類型',
                                                       `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '資源名',
                                                       `extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '檔案類型',
                                                       `size` bigint(20) DEFAULT 0 COMMENT '大小[字節]',
                                                       `disk` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '存儲磁盤',
                                                       `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '相對地址',
                                                       `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '所屬素材',
                                                       `is_hidden` tinyint(4) NOT NULL DEFAULT 0 COMMENT '隱藏[0:否,1:是]',
                                                       `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                       PRIMARY KEY (`id`),
                                                       KEY `type` (`type`)
                                                   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '資源表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "resource_category");
                                    put("name", "20231208_14_00_00_resource_category");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `resource_category` (
                                                      `cid` int(11) NOT NULL DEFAULT 0 COMMENT '分類ID',
                                                      `rid` int(11) NOT NULL DEFAULT 0 COMMENT '資源ID',
                                                      KEY `cid` (`cid`),
                                                      KEY `rid` (`rid`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '資源分類關聯表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "resource_course_category");
                                    put("name", "20231208_14_00_00_resource_course_category");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `resource_course_category` (
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `category_id` int(11) NOT NULL DEFAULT 0 COMMENT '父級ID',
                                                      KEY `course_id` (`course_id`),
                                                      KEY `category_id` (`category_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '課程分類關聯表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "resource_extra");
                                    put("name", "20231208_14_00_00_resource_extra");
                                    put(
                                            "sql",
                                            """
                                                     CREATE TABLE `resource_extra`(
                                                       `id`         int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                       `rid`        int(11) unsigned NOT NULL DEFAULT 0 COMMENT '資源ID',
                                                       `poster`     int(11) unsigned NOT NULL DEFAULT 0 COMMENT '封面資源ID',
                                                       `duration`   int(10) unsigned NOT NULL DEFAULT 0 COMMENT '影片、音訊總時長,文檔總頁數',
                                                       `created_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                       PRIMARY KEY (`id`),
                                                       UNIQUE KEY `rid` (`rid`)
                                                       ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '資源詳細信息表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_course_hour_records");
                                    put("name", "20231208_14_00_00_user_course_hour_records");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_course_hour_records` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `hour_id` int(11) NOT NULL DEFAULT 0 COMMENT '課時ID',
                                                      `total_duration` int(11) NOT NULL DEFAULT 0 COMMENT '總時長',
                                                      `finished_duration` int(11) NOT NULL DEFAULT 0 COMMENT '已完成時長',
                                                      `real_duration` int(11) NOT NULL DEFAULT 0 COMMENT '實際觀看時長',
                                                      `is_finished` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否看完[1:是,0:否]',
                                                      `finished_at` timestamp NULL DEFAULT NULL COMMENT '看完時間',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`),
                                                      KEY `u_h_c_id` (`user_id`,`hour_id`,`course_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '線上課課時學員學習記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_course_records");
                                    put("name", "20231208_14_00_00_user_course_records");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_course_records` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `course_id` int(11) NOT NULL DEFAULT 0 COMMENT '課程ID',
                                                      `hour_count` int(11) NOT NULL DEFAULT 0 COMMENT '課時數量',
                                                      `finished_count` int(11) NOT NULL DEFAULT 0 COMMENT '已完成課時數',
                                                      `progress` int(11) NOT NULL DEFAULT 0 COMMENT '進度',
                                                      `is_finished` tinyint(4) NOT NULL DEFAULT 0 COMMENT '看完[1:是,0:否]',
                                                      `finished_at` timestamp NULL DEFAULT NULL COMMENT '看完時間',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '分類表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_department");
                                    put("name", "20231208_14_00_00_user_department");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_department` (
                                                      `user_id` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `dep_id` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '部門ID',
                                                      KEY `user_id` (`user_id`),
                                                      KEY `dep_id` (`dep_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員部門關聯表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_learn_duration_records");
                                    put("name", "20231208_14_00_00_user_learn_duration_records");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_learn_duration_records` (
                                                      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `created_date` date NOT NULL COMMENT '建立時間',
                                                      `duration` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '已學習時長[微秒]',
                                                      `start_at` timestamp NULL DEFAULT NULL COMMENT '開始時間',
                                                      `end_at` timestamp NULL DEFAULT NULL COMMENT '結束時間',
                                                      `from_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '來源ID',
                                                      `from_scene` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '記錄來源[線上課:COURSE,學習任務:STUDY]',
                                                      PRIMARY KEY (`id`),
                                                      KEY `u_d` (`user_id`,`created_date`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員學習時長表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_learn_duration_stats");
                                    put("name", "20231208_14_00_00_user_learn_duration_stats");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_learn_duration_stats` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `duration` bigint(20) NOT NULL DEFAULT 0 COMMENT '學習時長',
                                                      `created_date` date NOT NULL COMMENT '建立時間',
                                                      PRIMARY KEY (`id`),
                                                      KEY `u_d` (`user_id`,`created_date`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員學習時長記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_login_records");
                                    put("name", "20231208_14_00_00_user_login_records");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_login_records` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員ID',
                                                      `jti` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'JTI',
                                                      `ip` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '登入IP',
                                                      `ip_area` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'IP解析區域',
                                                      `browser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '瀏覽器',
                                                      `browser_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '瀏覽器版本',
                                                      `os` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '操作系統',
                                                      `expired` bigint(20) NOT NULL DEFAULT 0 COMMENT '過期時間',
                                                      `is_logout` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否註銷',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `jti` (`jti`),
                                                      KEY `user_id` (`user_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員登入記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "user_upload_image_logs");
                                    put("name", "20231208_14_00_00_user_upload_image_logs");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `user_upload_image_logs` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '學員時間',
                                                      `typed` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '圖片類型',
                                                      `scene` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '上傳場景',
                                                      `driver` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '驅動',
                                                      `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '相對路徑',
                                                      `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '訪問地址',
                                                      `size` bigint(20) NOT NULL DEFAULT 0 COMMENT '大小,單位:字節',
                                                      `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '檔案名',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '線上課學員學習記錄表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "users");
                                    put("name", "20231208_14_00_00_users");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `users` (
                                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
                                                      `email` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '郵件',
                                                      `name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '真實姓名',
                                                      `avatar` int(11) NOT NULL DEFAULT 0 COMMENT '頭像',
                                                      `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '密碼',
                                                      `salt` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'salt',
                                                      `id_card` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '身份證號',
                                                      `credit1` int(11) NOT NULL DEFAULT 0 COMMENT '學分',
                                                      `create_ip` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '註冊IP',
                                                      `create_city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '註冊城市',
                                                      `is_active` tinyint(4) NOT NULL DEFAULT 0 COMMENT '激活[1:是,0:否]',
                                                      `is_lock` tinyint(4) NOT NULL DEFAULT 0 COMMENT '鎖定[1:是,0:否]',
                                                      `is_verify` tinyint(4) NOT NULL DEFAULT 0 COMMENT '實名認證[1:是,0:否]',
                                                      `verify_at` timestamp NULL DEFAULT NULL COMMENT '實名認證時間',
                                                      `is_set_password` tinyint(4) NOT NULL DEFAULT 0 COMMENT '設置密碼[1:是,0:否]',
                                                      `login_at` timestamp NULL DEFAULT NULL COMMENT '登入時間',
                                                      `created_at` timestamp NULL DEFAULT NULL COMMENT '建立時間',
                                                      `updated_at` timestamp NULL DEFAULT NULL COMMENT '修改時間',
                                                      `from_scene` int(11) NOT NULL DEFAULT 0 COMMENT '來源[0:本地,1:企業微信,2:飛書]',
                                                      `deleted` tinyint(1) unsigned NULL DEFAULT 0 COMMENT '刪除標誌[0:存在,1:刪除]',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `email` (`email`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '學員表';
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "ldap_user");
                                    put("name", "20240322_17_29_17_ldap_user");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `ldap_user` (
                                                      `id` int unsigned NOT NULL AUTO_INCREMENT,
                                                      `uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '唯一特徵值',
                                                      `user_id` int(11) NOT NULL DEFAULT 0 COMMENT '使用者ID',
                                                      `cn` varchar(120) NOT NULL DEFAULT '' COMMENT 'cn',
                                                      `dn` varchar(120) NOT NULL DEFAULT '' COMMENT 'dn',
                                                      `ou` varchar(255) NOT NULL DEFAULT '' COMMENT 'ou',
                                                      `uid` varchar(120) NOT NULL DEFAULT '' COMMENT 'uid',
                                                      `email` varchar(120) NOT NULL DEFAULT '' COMMENT '電子郵件',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `unique_uuid` (`uuid`) USING BTREE
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                                                    """);
                                }
                            });
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "ldap_department");
                                    put("name", "20240322_17_29_30_ldap_department");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `ldap_department` (
                                                      `id` int unsigned NOT NULL AUTO_INCREMENT,
                                                      `uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '唯一特徵值',
                                                      `department_id` int(11) NOT NULL DEFAULT 0 COMMENT '部門ID',
                                                      `dn` varchar(120) NOT NULL DEFAULT '' COMMENT 'dn',
                                                      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `unique_uuid` (`uuid`) USING BTREE
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                                                    """);
                                }
                            });

                    add(
                            new HashMap<>() {
                                {
                                    put("table", "");
                                    put("name", "20250519_09_00_00_migrations-data-insert");
                                    put(
                                            "sql",
                                            """
                                                    INSERT INTO migrations (migration) VALUES
                                                    ('20231224_14_00_00_update_courses'),
                                                    ('20240126_15_00_00_course_add_admin_id'),
                                                    ('20240722_12_00_00_course_hour_add_deleted'),
                                                    ('20240815_15_00_00_user_deteled_column_add');
                                                    """);
                                }
                            });
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "ldap_sync_record");
                                    put("name", "20250517_13_23_ldap_sync_record");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `ldap_sync_record` (
                                                      `id` int NOT NULL AUTO_INCREMENT,
                                                      `admin_id` int NOT NULL DEFAULT '0' COMMENT '執行同步的管理員ID，0表示系統自動執行',
                                                      `status` tinyint NOT NULL DEFAULT '0' COMMENT '狀態：0-進行中，1-成功，2-失敗',
                                                      `s3_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'S3存儲中的檔案路徑',
                                                      `total_department_count` int NOT NULL DEFAULT '0' COMMENT '總部門數量',
                                                      `created_department_count` int NOT NULL DEFAULT '0' COMMENT '新增部門數量',
                                                      `updated_department_count` int NOT NULL DEFAULT '0' COMMENT '更新部門數量',
                                                      `deleted_department_count` int NOT NULL DEFAULT '0' COMMENT '刪除部門數量',
                                                      `total_user_count` int NOT NULL DEFAULT '0' COMMENT '總使用者數量',
                                                      `created_user_count` int NOT NULL DEFAULT '0' COMMENT '新增使用者數量',
                                                      `updated_user_count` int NOT NULL DEFAULT '0' COMMENT '更新使用者數量',
                                                      `deleted_user_count` int NOT NULL DEFAULT '0' COMMENT '刪除使用者數量',
                                                      `banned_user_count` int NOT NULL DEFAULT '0' COMMENT '被禁止的使用者數量',
                                                      `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '錯誤信息',
                                                      `created_at` datetime NOT NULL,
                                                      `updated_at` datetime NOT NULL,
                                                      PRIMARY KEY (`id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LDAP同步記錄表';
                                                    """);
                                }
                            });
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "ldap_sync_department_detail");
                                    put("name", "20250519_10_25_01_ldap_sync_department_detail");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `ldap_sync_department_detail` (
                                                      `id` int NOT NULL AUTO_INCREMENT,
                                                      `record_id` int NOT NULL COMMENT '關聯的同步記錄ID',
                                                      `department_id` int NOT NULL DEFAULT '0' COMMENT '關聯的部門ID',
                                                      `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LDAP部門UUID',
                                                      `dn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LDAP部門DN',
                                                      `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部門名稱',
                                                      `action` tinyint NOT NULL COMMENT '操作：1-新增，2-更新，3-刪除，4-無變化',
                                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (`id`),
                                                      KEY `record_id` (`record_id`),
                                                      KEY `department_id` (`department_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LDAP部門同步詳情表';
                                                    """);
                                }
                            });
                    add(
                            new HashMap<>() {
                                {
                                    put("table", "ldap_sync_user_detail");
                                    put("name", "20250519_10_25_02_ldap_sync_user_detail");
                                    put(
                                            "sql",
                                            """
                                                    CREATE TABLE `ldap_sync_user_detail` (
                                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                                      `record_id` int NOT NULL COMMENT '關聯的同步記錄ID',
                                                      `user_id` bigint NOT NULL DEFAULT '0' COMMENT '關聯的使用者ID',
                                                      `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LDAP使用者UUID',
                                                      `dn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LDAP使用者DN',
                                                      `cn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用者名稱',
                                                      `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用者ID/登入名',
                                                      `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用者電子郵件',
                                                      `ou` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用者部門路徑',
                                                      `action` tinyint NOT NULL COMMENT '操作：1-新增，2-更新，3-刪除，4-無變化，5-禁止',
                                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (`id`),
                                                      KEY `record_id` (`record_id`),
                                                      KEY `user_id` (`user_id`)
                                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='LDAP使用者同步詳情表';
                                                    """);
                                }
                            });
                }
            };

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private MigrationService migrationService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 資料庫已建立的表
            List<String> tables = jdbcTemplate.queryForList("show tables", String.class);
            // 已建立表的記錄
            List<String> migrations = new ArrayList<>();
            if (tables.contains("migrations")) {
                migrations = migrationService.all();
            }

            for (Map<String, String> tableItem : TABLE_SQL) {
                String migrationName = tableItem.get("name");
                if (migrations.contains(migrationName)) {
                    continue;
                }

                String tableName = tableItem.get("table");
                if (!tables.isEmpty() && tables.contains(tableName)) {
                    // 數據表已建立但是沒有建立記錄
                    // 需要保存建立記錄
                    migrationService.store(migrationName);
                    continue;
                }

                // 建立數據表
                jdbcTemplate.execute(tableItem.get("sql"));
                // 記錄寫入到migrations表中
                migrationService.store(migrationName);
            }

        } catch (Exception e) {
            log.error("資料庫遷移執行失敗,錯誤信息:" + e.getMessage());
        }
    }
}
