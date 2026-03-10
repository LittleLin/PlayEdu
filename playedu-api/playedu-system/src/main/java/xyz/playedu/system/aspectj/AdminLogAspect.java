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
package xyz.playedu.system.aspectj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.playedu.common.annotation.Log;
import xyz.playedu.common.constant.SystemConstant;
import xyz.playedu.common.domain.AdminLog;
import xyz.playedu.common.domain.AdminUser;
import xyz.playedu.common.service.AdminLogService;
import xyz.playedu.common.service.AdminUserService;
import xyz.playedu.common.service.BackendAuthService;
import xyz.playedu.common.util.IpUtil;
import xyz.playedu.common.util.JsonUtils;
import xyz.playedu.common.util.RequestUtil;
import xyz.playedu.common.util.StringUtil;

@Aspect
@Component
@Slf4j
public class AdminLogAspect {

    @Autowired private BackendAuthService authService;

    @Autowired private AdminUserService adminUserService;

    @Autowired private AdminLogService adminLogService;

    /** 排除敏感屬性欄位 */
    public static final String EXCLUDE_PROPERTIES =
            "password,oldPassword,newPassword,confirmPassword,token";

    /** Controller層切點 註解攔截 */
    @Pointcut("@annotation(xyz.playedu.common.annotation.Log)")
    public void logPointCut() {}

    /**
     * 處理完請求後執行
     *
     * @param joinPoint 切點
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 攔截異常操作
     *
     * @param joinPoint 切點
     * @param e 異常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            // 獲取註解信息
            Log controllerLog = getAnnotationLog(joinPoint);
            if (null == controllerLog) {
                return;
            }

            AdminUser adminUser = adminUserService.findById(authService.userId());
            if (null == adminUser) {
                return;
            }

            // 日誌
            AdminLog adminLog = new AdminLog();
            adminLog.setAdminId(adminUser.getId());
            adminLog.setAdminName(adminUser.getName());
            adminLog.setModule("BACKEND");
            adminLog.setTitle(controllerLog.title());
            adminLog.setOpt(controllerLog.businessType().ordinal());

            // 設置方法名稱
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            adminLog.setMethod(className + "." + methodName + "()");

            HttpServletRequest request = RequestUtil.handler();
            if (null == request) {
                return;
            }
            adminLog.setRequestMethod(request.getMethod());
            adminLog.setUrl(request.getRequestURL().toString());
            String params = "";
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (StringUtil.isNotEmpty(parameterMap)) {
                params = JsonUtils.toJson(parameterMap);
            } else {
                Object[] args = joinPoint.getArgs();
                if (StringUtil.isNotNull(args)) {
                    params = StringUtil.arrayToString(args);
                }
            }
            if (StringUtil.isNotEmpty(params)) {
                JsonNode paramObj = excludeProperties(params);
                adminLog.setParam(JsonUtils.toJson(paramObj));
            }
            if (null != jsonResult) {
                jsonResult = excludeProperties(JsonUtils.toJson(jsonResult));
                adminLog.setResult(JsonUtils.toJson(jsonResult));
            }

            adminLog.setIp(IpUtil.getIpAddress());
            adminLog.setIpArea(IpUtil.getRealAddressByIP(IpUtil.getIpAddress()));

            if (null != e) {
                adminLog.setErrorMsg(e.getMessage());
            }
            adminLog.setCreatedAt(new Date());
            // 保存資料庫
            adminLogService.save(adminLog);
        } catch (Exception exp) {
            // 記錄本地異常日誌
            log.error("異常信息:" + exp.getMessage(), e);
        }
    }

    /** 是否存在註解，如果存在就獲取 */
    private Log getAnnotationLog(JoinPoint joinPoint) throws Exception {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(Log.class);
        }
        return null;
    }

    public JsonNode excludeProperties(String jsonData) throws Exception {
        ObjectNode result = JsonUtils.objectMapper().createObjectNode();
        if (!JsonUtils.isObject(jsonData)) {
            return result;
        }
        JsonNode jsonObject = JsonUtils.parse(jsonData);
        Iterator<Map.Entry<String, JsonNode>> fields = jsonObject.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isObject()) {
                result.set(key, excludeProperties(value.toString()));
            } else if (EXCLUDE_PROPERTIES.contains(key)) {
                result.put(key, SystemConstant.CONFIG_MASK);
            } else {
                result.set(key, value);
            }
        }
        return result;
    }
}
