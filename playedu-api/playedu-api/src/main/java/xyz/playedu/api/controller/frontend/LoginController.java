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
package xyz.playedu.api.controller.frontend;

import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.playedu.api.bus.LoginBus;
import xyz.playedu.api.cache.LoginLimitCache;
import xyz.playedu.api.cache.LoginLockCache;
import xyz.playedu.api.event.UserLogoutEvent;
import xyz.playedu.api.request.frontend.LoginLdapRequest;
import xyz.playedu.api.request.frontend.LoginPasswordRequest;
import xyz.playedu.common.context.FCtx;
import xyz.playedu.common.domain.User;
import xyz.playedu.common.exception.LimitException;
import xyz.playedu.common.exception.ServiceException;
import xyz.playedu.common.service.*;
import xyz.playedu.common.types.JsonResponse;
import xyz.playedu.common.types.LdapConfig;
import xyz.playedu.common.util.*;
import xyz.playedu.common.util.ldap.LdapTransformUser;
import xyz.playedu.common.util.ldap.LdapUtil;

@RestController
@RequestMapping("/api/v1/auth/login")
@Slf4j
public class LoginController {

    @Autowired private UserService userService;

    @Autowired private FrontendAuthService authService;

    @Autowired private ApplicationContext ctx;

    @Autowired private AppConfigService appConfigService;

    @Autowired private LoginBus loginBus;

    @Autowired private LoginLimitCache loginLimitCache;

    @Autowired private LoginLockCache loginLockCache;

    @Autowired private PasswordService passwordService;

    @PostMapping("/password")
    @SneakyThrows
    public JsonResponse password(@RequestBody @Validated LoginPasswordRequest req)
            throws LimitException {
        if (appConfigService.enabledLdapLogin()) {
            return JsonResponse.error("請使用LDAP登入");
        }

        String email = req.getEmail();

        User user = userService.find(email);
        if (user == null) {
            return JsonResponse.error("電子郵件或密碼錯誤");
        }

        loginLimitCache.check(email);

        if (!passwordService.matches(req.getPassword(), user.getPassword(), user.getSalt())) {
            return JsonResponse.error("電子郵件或密碼錯誤");
        }
        userService.upgradePasswordHash(user, req.getPassword());

        if (user.getIsLock() == 1) {
            return JsonResponse.error("當前學員已鎖定無法登入");
        }

        loginLimitCache.destroy(email);

        return JsonResponse.data(loginBus.tokenByUser(user));
    }

    @PostMapping("/ldap")
    @SneakyThrows
    public JsonResponse ldap(@RequestBody @Validated LoginLdapRequest req) {
        String username = req.getUsername();

        LdapConfig ldapConfig = appConfigService.ldapConfig();

        String mail = null;
        String uid = null;
        if (StringUtil.contains(username, "@")) {
            mail = username;
        } else {
            uid = username;
        }

        // 限流控制
        loginLimitCache.check(username);

        // 鎖控制-防止併發登入重複寫入數據
        if (!loginLockCache.apply(username)) {
            return JsonResponse.error("請稍候再試");
        }

        try {
            LdapTransformUser ldapTransformUser =
                    LdapUtil.loginByMailOrUid(ldapConfig, mail, uid, req.getPassword());
            if (ldapTransformUser == null) {
                return JsonResponse.error("登入失敗.請檢查帳號和密碼");
            }

            HashMap<String, Object> data = loginBus.tokenByLdapTransformUser(ldapTransformUser);

            // 刪除限流控制
            loginLimitCache.destroy(username);

            return JsonResponse.data(data);
        } catch (ServiceException e) {
            return JsonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("LDAP登入失敗", e);
            return JsonResponse.error("系統錯誤");
        } finally {
            loginLockCache.release(username);
        }
    }

    @PostMapping("/logout")
    public JsonResponse logout() {
        authService.logout();
        ctx.publishEvent(new UserLogoutEvent(this, FCtx.getId(), FCtx.getJwtJti()));
        return JsonResponse.success();
    }
}
