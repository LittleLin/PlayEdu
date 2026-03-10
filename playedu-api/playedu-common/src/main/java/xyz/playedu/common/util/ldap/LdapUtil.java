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
package xyz.playedu.common.util.ldap;

import java.io.IOException;
import java.util.*;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import lombok.extern.slf4j.Slf4j;
import xyz.playedu.common.exception.ServiceException;
import xyz.playedu.common.types.LdapConfig;
import xyz.playedu.common.util.StringUtil;

@Slf4j
public class LdapUtil {

    // person,posixAccount,inetOrgPerson,organizationalPerson => OpenLDAP的屬性
    // user => Window AD 域的屬性
    private static final String USER_OBJECT_CLASS =
            "(|(objectClass=person)(objectClass=posixAccount)(objectClass=inetOrgPerson)(objectClass=organizationalPerson)(objectClass=user))";

    private static final String[] USER_RETURN_ATTRS =
            new String[] {
                // OpenLDAP 的屬性
                "uid", // 使用者的唯一識別符號，全局唯一，可以看做使用者表的手機號，此欄位可用於配合密碼直接登入
                "cn", // CommonName -> 可以認作爲人的名字，比如：張三。在LDAP中此欄位是可以重複的,但是同一ou下不可重複
                "email", // 電子郵件，同上
                "entryUUID",

                // Window AD 域的屬性
                "name",
                "userPrincipalName",
                "distinguishedName",
                "sAMAccountName",
                "displayName",
                "uSNCreated", // AD域的唯一屬性
                "userAccountControl",

                // 公用屬性
                "mail",
            };
    private static final String[] OU_RETURN_ATTRS = new String[] {"ou", "usncreated"};

    // 514 - 禁用帳戶
    // 546 - 禁用帳戶 不需密碼
    // 66050 - 禁用帳戶 密碼未過期
    // 66080 - 禁用帳戶 密碼未過期且不需密碼
    // 66082 - 禁用帳戶 密碼未過期且不需密碼
    private static final String[] DISABLE_USER_ACCOUNT_CONTROL =
            new String[] {"514", "546", "66050", "66080", "66082"};

    public static LdapContext initContext(String url, String adminUser, String adminPass)
            throws NamingException {
        Hashtable<String, String> context = new Hashtable<>();
        context.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        context.put(Context.SECURITY_AUTHENTICATION, "simple");
        // 服務地址
        context.put(Context.PROVIDER_URL, url);
        // 管理員帳戶和密碼
        context.put(Context.SECURITY_PRINCIPAL, adminUser);
        context.put(Context.SECURITY_CREDENTIALS, adminPass);
        return new InitialLdapContext(context, null);
    }

    public static List<LdapTransformUser> users(LdapConfig ldapConfig, String filterScope)
            throws NamingException, IOException {
        LdapContext ldapContext =
                initContext(
                        ldapConfig.getUrl(), ldapConfig.getAdminUser(), ldapConfig.getAdminPass());

        int pageSize = 1000;
        List<LdapTransformUser> users = new ArrayList<>();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(USER_RETURN_ATTRS);
        controls.setReturningObjFlag(true);

        byte[] cookie = null;

        while (true) {
            try {
                if (cookie != null) {
                    ldapContext.setRequestControls(
                            new Control[] {
                                new PagedResultsControl(pageSize, cookie, false),
                            });
                } else {
                    ldapContext.setRequestControls(
                            new Control[] {new PagedResultsControl(pageSize, false)});
                }

                NamingEnumeration<SearchResult> result =
                        ldapContext.search(filterScope, USER_OBJECT_CLASS, controls);
                while (result.hasMoreElements()) {
                    SearchResult item = result.nextElement();
                    if (item != null) {
                        LdapTransformUser ldapTransformUser = parseTransformUser(item, filterScope);
                        if (ldapTransformUser != null) {
                            users.add(ldapTransformUser);
                        }
                    }
                }

                cookie = parseCookie(ldapContext.getResponseControls());
                if (cookie == null || cookie.length == 0) {
                    break;
                }
            } catch (NamingException e) {
                log.error("LDAP使用者查詢失敗", e);
                break;
            }
        }

        closeContext(ldapContext);

        if (users.isEmpty()) {
            log.info("LDAP服務中沒有使用者");
            return null;
        }

        return users;
    }

    private static byte[] parseCookie(Control[] controls) throws NamingException {
        if (controls != null) {
            for (Control control : controls) {
                if (control instanceof PagedResultsResponseControl) {
                    return ((PagedResultsResponseControl) control).getCookie();
                }
            }
        }
        return null;
    }

    public static List<LdapTransformDepartment> departments(LdapConfig ldapConfig, String baseDN)
            throws NamingException {
        LdapContext ldapContext =
                initContext(
                        ldapConfig.getUrl(), ldapConfig.getAdminUser(), ldapConfig.getAdminPass());

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(OU_RETURN_ATTRS);
        controls.setReturningObjFlag(true);

        String filter = "(objectClass=organizationalUnit)";
        NamingEnumeration<SearchResult> result = null;
        try {
            log.info("LDAP-部門查詢|條件[baseDN={},filter={}]", baseDN, filter);
            result = ldapContext.search(baseDN, filter, controls);
        } catch (NamingException e) {
            log.error("LDAP-部門查詢-失敗|errMsg={}", e.getMessage());
        } finally {
            closeContext(ldapContext);
        }

        if (result == null || !result.hasMoreElements()) {
            log.info("LDAP-部門查詢-結果爲空|條件[baseDN={},filter={}]", baseDN, filter);
            return null;
        }

        // baseDN中的ou作用域
        String ouScopesStr = baseDNOuScope(baseDN);

        List<LdapTransformDepartment> units = new ArrayList<>();
        while (result.hasMoreElements()) {
            SearchResult item = result.nextElement();
            if (item == null) {
                continue;
            }

            Attributes attributes = item.getAttributes();
            if (attributes == null) {
                continue;
            }

            // 唯一特徵值
            String uSNCreated = getAttribute(attributes, "uSNCreated");
            if (StringUtil.isEmpty(uSNCreated)) {
                continue;
            }

            // 組織DN
            String name = item.getName();
            if (name.isEmpty()) {
                name = ouScopesStr;
            } else {
                name = name + (ouScopesStr.isEmpty() ? "" : "," + ouScopesStr);
            }

            // 將DN反轉
            List<String> tmp = new ArrayList<>(List.of(name.split(",")));
            Collections.reverse(tmp);
            name = String.join(",", tmp);

            LdapTransformDepartment ldapDepartment = new LdapTransformDepartment();
            ldapDepartment.setUuid(uSNCreated);
            ldapDepartment.setDn(name.toLowerCase());

            units.add(ldapDepartment);
        }

        return units;
    }

    public static LdapTransformUser loginByMailOrUid(
            LdapConfig ldapConfig, String mail, String uid, String password)
            throws ServiceException, NamingException {
        if (StringUtil.isEmpty(mail) && StringUtil.isEmpty(uid)) {
            throw new ServiceException("mail和Uid不能同時爲空");
        }

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(USER_RETURN_ATTRS);
        controls.setReturningObjFlag(true);
        controls.setCountLimit(1);

        String userFilter = "";
        if (StringUtil.isNotEmpty(mail)) {
            userFilter =
                    String.format("(|(mail=%s)(email=%s)(userPrincipalName=%s))", mail, mail, mail);
        } else if (StringUtil.isNotEmpty(uid)) {
            userFilter = String.format("(|(uid=%s)(sAMAccountName=%s))", uid, uid);
        }

        String filter = String.format("(&%s%s)", userFilter, USER_OBJECT_CLASS);

        LdapContext ldapContext =
                initContext(
                        ldapConfig.getUrl(), ldapConfig.getAdminUser(), ldapConfig.getAdminPass());
        NamingEnumeration<SearchResult> result = null;
        try {
            result = ldapContext.search(ldapConfig.getBaseDN(), filter, controls);
        } catch (NamingException e) {
            log.error("LDAP-通過mail或uid登入失敗", e);
        } finally {
            closeContext(ldapContext);
        }

        if (result == null || !result.hasMoreElements()) {
            log.info("LDAP-使用者不存在");
            return null;
        }

        // 根據mail或uid查詢出來的使用者
        LdapTransformUser ldapUser =
                parseTransformUser(result.nextElement(), ldapConfig.getBaseDN());
        if (ldapUser == null) {
            log.info("LDAP-使用者不存在");
            return null;
        }

        // 使用使用者dn+提交的密碼去登入ldap系統
        // 登入成功則意味着密碼正確
        // 登入失敗則意味着密碼錯誤
        try {
            ldapContext =
                    initContext(
                            ldapConfig.getUrl(),
                            ldapUser.getDn() + "," + ldapConfig.getBaseDN(),
                            password);
            return ldapUser;
        } catch (Exception e) {
            // 無法登入->密碼錯誤
            log.error("LDAP-登入失敗", e);
            return null;
        } finally {
            ldapContext.close();
        }
    }

    private static LdapTransformUser parseTransformUser(SearchResult item, String baseDN)
            throws NamingException {
        Attributes attributes = item.getAttributes();
        if (attributes == null) {
            return null;
        }

        LdapTransformUser ldapUser = new LdapTransformUser();
        ldapUser.setDn(item.getName());

        if (attributes.get("userAccountControl") != null) {
            String userAccountControl = (String) attributes.get("userAccountControl").get();
            for (String s : DISABLE_USER_ACCOUNT_CONTROL) {
                if (s.equals(userAccountControl)) {
                    ldapUser.setBan(true);
                    break;
                }
            }
        }

        // name解析
        String displayName = getAttribute(attributes, "displayName");
        if (StringUtil.isEmpty(displayName)) {
            displayName = getAttribute(attributes, "cn");
        }
        ldapUser.setCn(displayName);

        // 電子郵件解析
        String email = getAttribute(attributes, "mail");
        if (StringUtil.isEmpty(email)) {
            getAttribute(attributes, "email");
        }
        ldapUser.setEmail(email);

        if (attributes.get("uSNCreated") != null) {
            // Window AD域
            ldapUser.setId((String) attributes.get("uSNCreated").get());
            ldapUser.setUid((String) attributes.get("sAMAccountName").get());
        } else {
            // OpenLDAP
            ldapUser.setId((String) attributes.get("entryUUID").get());
            ldapUser.setUid((String) attributes.get("uid").get());
        }

        // ou計算
        String baseDNOuScope = baseDNOuScope(baseDN);
        String[] rdnList =
                (baseDNOuScope.isEmpty()
                                ? ldapUser.getDn().toLowerCase()
                                : ldapUser.getDn().toLowerCase() + "," + baseDNOuScope)
                        .split(",");
        List<String> ou = new ArrayList<>();
        for (String s : rdnList) {
            if (StringUtil.startsWith(s, "ou=")) {
                ou.add(s.replace("ou=", ""));
            }
        }
        Collections.reverse(ou);
        ldapUser.setOu(ou);

        return ldapUser;
    }

    private static String getAttribute(Attributes attributes, String keyName)
            throws NamingException {
        Attribute attribute = attributes.get(keyName);
        if (attribute == null) {
            return null;
        }
        return (String) attribute.get();
    }

    private static String baseDNOuScope(String baseDN) {
        List<String> ouScopes = new ArrayList<>();
        String[] rdnList = baseDN.toLowerCase().split(",");
        for (String s : rdnList) {
            if (s.startsWith("ou=")) {
                ouScopes.add(s);
            }
        }
        return String.join(",", ouScopes);
    }

    private static void closeContext(LdapContext ldapCtx) {
        if (ldapCtx == null) {
            return;
        }
        try {
            ldapCtx.close();
        } catch (NamingException e) {
            log.error("LDAP-資源釋放失敗", e);
        }
    }
}
