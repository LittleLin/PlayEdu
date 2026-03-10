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

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.playedu.common.constant.BackendConstant;
import xyz.playedu.common.constant.ConfigConstant;
import xyz.playedu.common.domain.AppConfig;
import xyz.playedu.common.service.AppConfigService;

@Component
@Order(100)
public class AppConfigCheck implements CommandLineRunner {

    private static final HashMap<String, AppConfig[]> configs =
            new HashMap<>() {
                {
                    // 系統設定
                    put(
                            "系統",
                            new AppConfig[] {
                                new AppConfig() {
                                    {
                                        setName("網站名");
                                        setSort(10);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_INPUT);
                                        setKeyName(ConfigConstant.SYSTEM_NAME);
                                        setKeyValue("");
                                        setHelp("請輸入網站名");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("Logo");
                                        setSort(20);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_IMAGE);
                                        setKeyName(ConfigConstant.SYSTEM_LOGO);
                                        setKeyValue("");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("PC端口訪問地址");
                                        setSort(40);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_INPUT);
                                        setKeyName(ConfigConstant.SYSTEM_PC_URL);
                                        setKeyValue("");
                                        setHelp("請輸入PC端訪問地址");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("H5端口訪問地址");
                                        setSort(50);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_INPUT);
                                        setKeyName(ConfigConstant.SYSTEM_H5_URL);
                                        setKeyValue("");
                                        setHelp("請輸入H5端訪問地址");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("網站頁腳");
                                        setSort(60);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_INPUT);
                                        setKeyName("system.pc_index_footer_msg");
                                        setKeyValue("");
                                        setHelp("自定義一句話顯示在前台頁腳");
                                    }
                                },
                            });
                    // 播放設定
                    put(
                            "播放設定",
                            new AppConfig[] {
                                new AppConfig() {
                                    {
                                        setName("播放器封面");
                                        setSort(10);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_IMAGE);
                                        setKeyName("player.poster");
                                        setKeyValue("");
                                        setHelp("播放器封面在學員觀看影片時預設顯示");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("啓用跑馬燈");
                                        setSort(20);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_SWITCH);
                                        setKeyName("player.is_enabled_bullet_secret");
                                        setKeyValue("0");
                                        setHelp("開啓之後影片播放器將會隨機顯示學員信息");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("跑馬燈內容");
                                        setSort(30);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName("player.bullet_secret_text");
                                        setKeyValue("");
                                        setHelp("請設定跑馬燈顯示的內容模板");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("跑馬燈顏色");
                                        setSort(40);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName("player.bullet_secret_color");
                                        setKeyValue("");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("跑馬燈透明度");
                                        setSort(50);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName("player.bullet_secret_opacity");
                                        setKeyValue("1");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("禁止拖拽播放");
                                        setSort(60);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_SWITCH);
                                        setKeyName("player.disabled_drag");
                                        setKeyValue("0");
                                    }
                                },
                            });
                    put(
                            "學員設定",
                            new AppConfig[] {
                                new AppConfig() {
                                    {
                                        setName("預設頭像");
                                        setSort(10);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_IMAGE);
                                        setKeyName(ConfigConstant.MEMBER_DEFAULT_AVATAR);
                                        setKeyValue("");
                                    }
                                },
                            });
                    put(
                            "S3存儲",
                            new AppConfig[] {
                                new AppConfig() {
                                    {
                                        setName("AccessKey");
                                        setSort(10);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.S3_ACCESS_KEY);
                                        setKeyValue("");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("SecretKey");
                                        setSort(20);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.S3_SECRET_KEY);
                                        setKeyValue("");
                                        setIsPrivate(1);
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("Bucket");
                                        setSort(30);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.S3_BUCKET);
                                        setKeyValue("");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("Region");
                                        setSort(35);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.S3_REGION);
                                        setKeyValue("");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("Endpoint");
                                        setSort(40);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.S3_ENDPOINT);
                                        setKeyValue("");
                                    }
                                },
                            });
                    put(
                            "LDAP設定",
                            new AppConfig[] {
                                new AppConfig() {
                                    {
                                        setName("啓用");
                                        setSort(10);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_SWITCH);
                                        setKeyName(ConfigConstant.LDAP_ENABLED);
                                        setKeyValue("0");
                                        setHelp("注意：1.支持Window AD域和OpenLDAP 2.啓用以後系統只能使用LDAP帳號登入");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("服務地址");
                                        setSort(20);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.LDAP_URL);
                                        setKeyValue("");
                                        setHelp("LDAP的對外服務地址。例如：ldap://ldap.example.com:389");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("使用者名");
                                        setSort(40);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.LDAP_ADMIN_USER);
                                        setKeyValue("");
                                        setHelp("使用者登入到LDAP。如：cn=admin,dc=example,dc=com");
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("密碼");
                                        setSort(50);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.LDAP_ADMIN_PASS);
                                        setKeyValue("");
                                        setIsPrivate(1);
                                    }
                                },
                                new AppConfig() {
                                    {
                                        setName("基本DN");
                                        setSort(60);
                                        setFieldType(BackendConstant.APP_CONFIG_FIELD_TYPE_TEXT);
                                        setKeyName(ConfigConstant.LDAP_BASE_DN);
                                        setKeyValue("");
                                        setHelp("從LDAP根節點搜索使用者");
                                    }
                                },
                            });
                }
            };

    @Autowired private AppConfigService configService;

    @Override
    public void run(String... args) throws Exception {
        Map<String, Long> keys = configService.allKeys();
        List<AppConfig> list = new ArrayList<>();
        Date now = new Date();

        configs.forEach(
                (groupNameValue, items) -> {
                    for (int i = 0; i < items.length; i++) {
                        AppConfig configItem = items[i];

                        if (keys.get(configItem.getKeyName()) != null) {
                            continue;
                        }

                        configItem.setGroupName(groupNameValue);
                        configItem.setCreatedAt(now);
                        list.add(configItem);
                    }
                });

        if (!list.isEmpty()) {
            configService.saveBatch(list);
        }
    }
}
