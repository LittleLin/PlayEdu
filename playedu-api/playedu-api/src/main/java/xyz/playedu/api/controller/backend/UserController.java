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
package xyz.playedu.api.controller.backend;

import cn.hutool.core.date.DateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.playedu.api.event.UserCourseHourRecordDestroyEvent;
import xyz.playedu.api.event.UserCourseRecordDestroyEvent;
import xyz.playedu.api.event.UserDestroyEvent;
import xyz.playedu.api.request.backend.UserImportRequest;
import xyz.playedu.api.request.backend.UserRequest;
import xyz.playedu.common.annotation.BackendPermission;
import xyz.playedu.common.annotation.Log;
import xyz.playedu.common.constant.*;
import xyz.playedu.common.context.BCtx;
import xyz.playedu.common.domain.*;
import xyz.playedu.common.exception.NotFoundException;
import xyz.playedu.common.service.*;
import xyz.playedu.common.service.UserDepartmentService;
import xyz.playedu.common.types.JsonResponse;
import xyz.playedu.common.types.mapper.UserCourseHourRecordCourseCountMapper;
import xyz.playedu.common.types.paginate.PaginationResult;
import xyz.playedu.common.types.paginate.UserCourseHourRecordPaginateFilter;
import xyz.playedu.common.types.paginate.UserCourseRecordPaginateFilter;
import xyz.playedu.common.types.paginate.UserPaginateFilter;
import xyz.playedu.common.util.HelperUtil;
import xyz.playedu.common.util.StringUtil;
import xyz.playedu.course.domain.*;
import xyz.playedu.course.service.*;
import xyz.playedu.resource.service.ResourceService;

/**
 * @Author 杭州白書科技有限公司
 *
 * @create 2023/2/23 09:48
 */
@RestController
@Slf4j
@RequestMapping("/backend/v1/user")
public class UserController {

    @Autowired private UserService userService;

    @Autowired private UserDepartmentService userDepartmentService;

    @Autowired private DepartmentService departmentService;

    @Autowired private ApplicationContext context;

    @Autowired private UserCourseHourRecordService userCourseHourRecordService;

    @Autowired private UserCourseRecordService userCourseRecordService;

    @Autowired private CourseHourService courseHourService;

    @Autowired private CourseService courseService;

    @Autowired private UserLearnDurationStatsService userLearnDurationStatsService;

    @Autowired private ApplicationContext ctx;

    @Autowired private ResourceService resourceService;

    @BackendPermission(slug = BPermissionConstant.USER_INDEX)
    @GetMapping("/index")
    @Log(title = "學員-列表", businessType = BusinessTypeConstant.GET)
    public JsonResponse index(@RequestParam HashMap<String, Object> params) {
        Integer page = MapUtils.getInteger(params, "page", 1);
        Integer size = MapUtils.getInteger(params, "size", 10);
        String sortField = MapUtils.getString(params, "sort_field");
        String sortAlgo = MapUtils.getString(params, "sort_algo");

        String name = MapUtils.getString(params, "name");
        String email = MapUtils.getString(params, "email");
        String idCard = MapUtils.getString(params, "id_card");
        Integer isActive = MapUtils.getInteger(params, "is_active");
        Integer isLock = MapUtils.getInteger(params, "is_lock");
        Integer isVerify = MapUtils.getInteger(params, "is_verify");
        Integer isSetPassword = MapUtils.getInteger(params, "is_set_password");
        String createdAt = MapUtils.getString(params, "created_at");
        String depIdsStr = MapUtils.getString(params, "dep_ids");
        List<Integer> depIds = null;
        if (StringUtil.isNotEmpty(depIdsStr)) {
            depIds = new ArrayList<>();
            if (!"0".equals(depIdsStr)) {
                List<Department> departmentList =
                        departmentService.chunk(
                                Arrays.stream(depIdsStr.split(",")).map(Integer::valueOf).toList());
                if (StringUtil.isNotEmpty(departmentList)) {
                    for (Department dep : departmentList) {
                        depIds.add(dep.getId());
                        String parentChain = "";
                        if (StringUtil.isEmpty(dep.getParentChain())) {
                            parentChain = dep.getId() + "";
                        } else {
                            parentChain = dep.getParentChain() + "," + dep.getId();
                        }
                        // 獲取所有子部門ID
                        List<Department> childDepartmentList =
                                departmentService.getChildDepartmentsByParentChain(
                                        dep.getId(), parentChain);
                        if (StringUtil.isNotEmpty(childDepartmentList)) {
                            depIds.addAll(
                                    childDepartmentList.stream().map(Department::getId).toList());
                        }
                    }
                }
            }
        }

        List<Integer> finalDepIds = depIds;
        UserPaginateFilter filter =
                new UserPaginateFilter() {
                    {
                        setName(name);
                        setEmail(email);
                        setIdCard(idCard);
                        setIsActive(isActive);
                        setIsLock(isLock);
                        setIsVerify(isVerify);
                        setIsSetPassword(isSetPassword);
                        setDepIds(finalDepIds);
                        setSortAlgo(sortAlgo);
                        setSortField(sortField);
                    }
                };

        if (createdAt != null && !createdAt.trim().isEmpty()) {
            filter.setCreatedAt(createdAt.split(","));
        }

        PaginationResult<User> result = userService.paginate(page, size, filter);

        HashMap<String, Object> data = new HashMap<>();
        data.put("data", result.getData());
        data.put("total", result.getTotal());
        data.put(
                "user_dep_ids",
                userService.getDepIdsGroup(result.getData().stream().map(User::getId).toList()));
        data.put("departments", departmentService.id2name());
        data.put("pure_total", userService.total());
        data.put("dep_user_count", departmentService.getDepartmentsUserCount());

        // 課程封面資源ID
        data.put(
                "resource_url",
                resourceService.chunksPreSignUrlByIds(
                        result.getData().stream().map(User::getAvatar).toList()));

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_STORE)
    @GetMapping("/create")
    @Log(title = "學員-新增", businessType = BusinessTypeConstant.GET)
    public JsonResponse create() {
        return JsonResponse.data(null);
    }

    @BackendPermission(slug = BPermissionConstant.USER_STORE)
    @PostMapping("/create")
    @Log(title = "學員-新增", businessType = BusinessTypeConstant.INSERT)
    public JsonResponse store(@RequestBody @Validated UserRequest req) {
        String email = req.getEmail();
        if (userService.emailIsExists(email)) {
            return JsonResponse.error("電子郵件已存在");
        }
        String password = req.getPassword();
        if (password.isEmpty()) {
            return JsonResponse.error("請輸入密碼");
        }
        userService.createWithDepIds(
                email,
                req.getName(),
                req.getAvatar(),
                req.getPassword(),
                req.getIdCard(),
                req.getDepIds());
        return JsonResponse.success();
    }

    @BackendPermission(slug = BPermissionConstant.USER_UPDATE)
    @GetMapping("/{id}")
    @Log(title = "學員-編輯", businessType = BusinessTypeConstant.GET)
    public JsonResponse edit(@PathVariable(name = "id") Integer id) throws NotFoundException {
        User user = userService.findOrFail(id);

        List<Integer> depIds = userService.getDepIdsByUserId(user.getId());

        HashMap<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("dep_ids", depIds);

        // 獲取簽名url
        data.put(
                "resource_url",
                resourceService.chunksPreSignUrlByIds(
                        new ArrayList<>() {
                            {
                                add(user.getAvatar());
                            }
                        }));

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_UPDATE)
    @PutMapping("/{id}")
    @Transactional
    @Log(title = "學員-編輯", businessType = BusinessTypeConstant.UPDATE)
    public JsonResponse update(
            @PathVariable(name = "id") Integer id, @RequestBody @Validated UserRequest req)
            throws NotFoundException {
        User user = userService.findOrFail(id);

        String email = req.getEmail();
        if (!email.equals(user.getEmail()) && userService.emailIsExists(email)) {
            return JsonResponse.error("電子郵件已存在");
        }

        userService.updateWithDepIds(
                user,
                email,
                req.getName(),
                req.getAvatar(),
                req.getPassword(),
                req.getIdCard(),
                req.getDepIds());
        return JsonResponse.success();
    }

    @BackendPermission(slug = BPermissionConstant.USER_DESTROY)
    @DeleteMapping("/{id}")
    @Log(title = "學員-刪除", businessType = BusinessTypeConstant.DELETE)
    public JsonResponse destroy(@PathVariable(name = "id") Integer id) throws NotFoundException {
        User user = userService.findOrFail(id);
        userService.removeById(user.getId());
        context.publishEvent(new UserDestroyEvent(this, user.getId()));
        return JsonResponse.success();
    }

    @PostMapping("/store-batch")
    @Transactional
    @Log(title = "學員-批量導入", businessType = BusinessTypeConstant.INSERT)
    public JsonResponse batchStore(@RequestBody @Validated UserImportRequest req) {
        List<UserImportRequest.UserItem> users = req.getUsers();
        if (users.isEmpty()) {
            return JsonResponse.error("數據爲空");
        }
        if (users.size() > 1000) {
            return JsonResponse.error("一次最多導入1000條數據");
        }

        // 導入表格的有效數據起始行-用於錯誤提醒
        Integer startLine = req.getStartLine();

        // 預設的學員頭像
        int defaultAvatar = CommonConstant.MINUS_ONE;
        String defaultAvatarConfig = BCtx.getConfig().get(ConfigConstant.MEMBER_DEFAULT_AVATAR);
        if (StringUtil.isNotEmpty(defaultAvatarConfig)) {
            defaultAvatar = Integer.parseInt(defaultAvatarConfig);
        }

        List<String[]> errorLines = new ArrayList<>();
        errorLines.add(new String[] {"錯誤行", "錯誤信息"}); // 錯誤表-表頭

        // 讀取存在的部門
        List<Department> departments = departmentService.all();
        Map<Integer, String> depId2Name =
                departments.stream()
                        .collect(Collectors.toMap(Department::getId, Department::getName));
        HashMap<String, Integer> depChainNameMap = new HashMap<>();
        for (Department tmpDepItem : departments) {
            // 一級部門
            if (tmpDepItem.getParentChain() == null || tmpDepItem.getParentChain().isEmpty()) {
                depChainNameMap.put(tmpDepItem.getName(), tmpDepItem.getId());
                continue;
            }

            // 多級部門
            String[] tmpChainIds = tmpDepItem.getParentChain().split(",");
            List<String> tmpChainNames = new ArrayList<>();
            for (int i = 0; i < tmpChainIds.length; i++) {
                String tmpName = depId2Name.get(Integer.valueOf(tmpChainIds[i]));
                if (tmpName == null) {
                    continue;
                }
                tmpChainNames.add(tmpName);
            }
            tmpChainNames.add(tmpDepItem.getName());
            depChainNameMap.put(String.join("-", tmpChainNames), tmpDepItem.getId());
        }

        // 電子郵件輸入重複檢測 || 部門存在檢測
        HashMap<String, Integer> emailRepeat = new HashMap<>();
        HashMap<String, Integer[]> depMap = new HashMap<>();
        List<String> emails = new ArrayList<>();
        List<User> insertUsers = new ArrayList<>();
        int i = -1;

        for (UserImportRequest.UserItem userItem : users) {
            i++; // 索引值

            if (userItem.getEmail() == null || userItem.getEmail().trim().isEmpty()) {
                errorLines.add(new String[] {"第" + (i + startLine) + "行", "未輸入電子郵件帳號"});
            } else {
                // 電子郵件重複判斷
                Integer repeatLine = emailRepeat.get(userItem.getEmail());
                if (repeatLine != null) {
                    errorLines.add(
                            new String[] {
                                "第" + (i + startLine) + "行", "與第" + repeatLine + "行電子郵件重複"
                            });
                } else {
                    emailRepeat.put(userItem.getEmail(), i + startLine);
                }
                emails.add(userItem.getEmail());
            }

            // 部門數據檢測
            if (userItem.getDeps() == null || userItem.getDeps().trim().isEmpty()) {
                errorLines.add(new String[] {"第" + (i + startLine) + "行", "未選擇部門"});
            } else {
                String[] tmpDepList = userItem.getDeps().trim().split("\\|");
                Integer[] tmpDepIds = new Integer[tmpDepList.length];
                for (int j = 0; j < tmpDepList.length; j++) {
                    // 獲取部門id
                    Integer tmpDepId = depChainNameMap.get(tmpDepList[j]);
                    // 判斷部門id是否存在
                    if (tmpDepId == null || tmpDepId == 0) {
                        errorLines.add(
                                new String[] {
                                    "第" + (i + startLine) + "行", "部門『" + tmpDepList[j] + "』不存在"
                                });
                        continue;
                    }
                    tmpDepIds[j] = tmpDepId;
                }
                depMap.put(userItem.getEmail(), tmpDepIds);
            }

            // 姓名爲空檢測
            String tmpName = userItem.getName();
            if (tmpName == null || tmpName.trim().isEmpty()) {
                errorLines.add(new String[] {"第" + (i + startLine) + "行", "暱稱爲空"});
            }

            // 密碼爲空檢測
            String tmpPassword = userItem.getPassword();
            if (tmpPassword == null || tmpPassword.trim().isEmpty()) {
                errorLines.add(new String[] {"第" + (i + startLine) + "行", "密碼爲空"});
            }

            // 待插入數據
            User tmpInsertUser = new User();
            String tmpSalt = HelperUtil.randomString(6);
            tmpInsertUser.setEmail(userItem.getEmail());
            tmpInsertUser.setPassword(HelperUtil.MD5(tmpPassword + tmpSalt));
            tmpInsertUser.setSalt(tmpSalt);
            tmpInsertUser.setName(tmpName);
            tmpInsertUser.setAvatar(defaultAvatar);
            tmpInsertUser.setIdCard(userItem.getIdCard());
            tmpInsertUser.setCreateIp(SystemConstant.INTERNAL_IP);
            tmpInsertUser.setCreateCity(SystemConstant.INTERNAL_IP_AREA);
            tmpInsertUser.setCreatedAt(new Date());
            tmpInsertUser.setUpdatedAt(new Date());

            insertUsers.add(tmpInsertUser);
        }

        if (errorLines.size() > 1) {
            return JsonResponse.error("導入數據有誤", errorLines);
        }

        // 電子郵件是否註冊檢測
        List<String> existsEmails = userService.existsEmailsByEmails(emails);
        if (!existsEmails.isEmpty()) {
            for (String tmpEmail : existsEmails) {
                errorLines.add(new String[] {"第" + emailRepeat.get(tmpEmail) + "行", "電子郵件已註冊"});
            }
        }
        if (errorLines.size() > 1) {
            return JsonResponse.error("導入數據有誤", errorLines);
        }

        userService.saveBatch(insertUsers);

        // 部門關聯
        List<UserDepartment> insertUserDepartments = new ArrayList<>();
        for (User tmpUser : insertUsers) {
            Integer[] tmpDepIds = depMap.get(tmpUser.getEmail());
            if (tmpDepIds == null) {
                continue;
            }
            for (Integer tmpDepId : tmpDepIds) {
                insertUserDepartments.add(
                        new UserDepartment() {
                            {
                                setUserId(tmpUser.getId());
                                setDepId(tmpDepId);
                            }
                        });
            }
        }
        userDepartmentService.saveBatch(insertUserDepartments);

        return JsonResponse.success();
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN)
    @GetMapping("/{id}/learn-hours")
    @SneakyThrows
    @Log(title = "學員-已學習課時列表", businessType = BusinessTypeConstant.GET)
    public JsonResponse learnHours(
            @PathVariable(name = "id") Integer id, @RequestParam HashMap<String, Object> params) {
        Integer page = MapUtils.getInteger(params, "page", 1);
        Integer size = MapUtils.getInteger(params, "size", 10);
        String sortField = MapUtils.getString(params, "sort_field");
        String sortAlgo = MapUtils.getString(params, "sort_algo");
        Integer isFinished = MapUtils.getInteger(params, "is_finished");

        UserCourseHourRecordPaginateFilter filter = new UserCourseHourRecordPaginateFilter();
        filter.setSortAlgo(sortAlgo);
        filter.setSortField(sortField);
        filter.setUserId(id);
        filter.setIsFinished(isFinished);

        PaginationResult<UserCourseHourRecord> result =
                userCourseHourRecordService.paginate(page, size, filter);

        HashMap<String, Object> data = new HashMap<>();
        data.put("data", result.getData());
        data.put("total", result.getTotal());
        data.put(
                "hours",
                courseHourService
                        .chunk(
                                result.getData().stream()
                                        .map(UserCourseHourRecord::getHourId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(CourseHour::getId, e -> e)));

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN)
    @GetMapping("/{id}/learn-courses")
    @Log(title = "學員-已學習課程列表", businessType = BusinessTypeConstant.GET)
    public JsonResponse latestLearnCourses(
            @PathVariable(name = "id") Integer id, @RequestParam HashMap<String, Object> params) {
        Integer page = MapUtils.getInteger(params, "page", 1);
        Integer size = MapUtils.getInteger(params, "size", 10);
        String sortField = MapUtils.getString(params, "sort_field");
        String sortAlgo = MapUtils.getString(params, "sort_algo");
        Integer isFinished = MapUtils.getInteger(params, "is_finished");

        UserCourseRecordPaginateFilter filter = new UserCourseRecordPaginateFilter();
        filter.setSortAlgo(sortAlgo);
        filter.setSortField(sortField);
        filter.setUserId(id);
        filter.setIsFinished(isFinished);

        PaginationResult<UserCourseRecord> result =
                userCourseRecordService.paginate(page, size, filter);

        List<Course> courseList =
                courseService.chunks(
                        result.getData().stream().map(UserCourseRecord::getCourseId).toList());

        HashMap<String, Object> data = new HashMap<>();
        data.put("data", result.getData());
        data.put("total", result.getTotal());
        data.put("courses", courseList.stream().collect(Collectors.toMap(Course::getId, e -> e)));

        // 獲取簽名url
        data.put(
                "resource_url",
                resourceService.chunksPreSignUrlByIds(
                        courseList.stream().map(Course::getThumb).toList()));

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN)
    @GetMapping("/{id}/all-courses")
    @Log(title = "學員-課程", businessType = BusinessTypeConstant.GET)
    public JsonResponse allCourses(@PathVariable(name = "id") Integer id) {
        // 讀取學員關聯的部門
        List<Integer> depIds = userService.getDepIdsByUserId(id);
        List<Department> departments = new ArrayList<>();
        HashMap<Integer, List<Course>> depCourses = new HashMap<>();
        List<Integer> courseIds = new ArrayList<>();
        List<Integer> rids = new ArrayList<>();

        if (depIds != null && !depIds.isEmpty()) {
            departments = departmentService.chunk(depIds);
            Map<Integer, Department> departmentMap = new HashMap<>();
            if (StringUtil.isNotEmpty(departments)) {
                departmentMap =
                        departments.stream().collect(Collectors.toMap(Department::getId, e -> e));
            }
            Map<Integer, Department> finalDepartmentMap = departmentMap;
            depIds.forEach(
                    (depId) -> {
                        // 查詢所有的父級部門ID
                        List<Integer> allDepIds = new ArrayList<>();
                        allDepIds.add(depId);
                        Department department = finalDepartmentMap.get(depId);
                        String parentChain = department.getParentChain();
                        if (StringUtil.isNotEmpty(parentChain)) {
                            List<Integer> parentChainList =
                                    Arrays.stream(parentChain.split(","))
                                            .map(Integer::parseInt)
                                            .toList();
                            if (StringUtil.isNotEmpty(parentChainList)) {
                                allDepIds.addAll(parentChainList);
                            }
                        }
                        List<Course> tmpCourses = courseService.getDepCoursesAndShow(allDepIds);
                        depCourses.put(depId, tmpCourses);

                        if (tmpCourses != null && !tmpCourses.isEmpty()) {
                            courseIds.addAll(tmpCourses.stream().map(Course::getId).toList());
                            rids.addAll(tmpCourses.stream().map(Course::getThumb).toList());
                        }
                    });
        }

        // 未關聯部門課程
        List<Course> openCourses = courseService.getOpenCoursesAndShow(1000);
        if (openCourses != null && !openCourses.isEmpty()) {
            courseIds.addAll(openCourses.stream().map(Course::getId).toList());
            rids.addAll(openCourses.stream().map(Course::getThumb).toList());
        }

        // 讀取學員的線上課學習記錄
        List<UserCourseRecord> userCourseRecords = new ArrayList<>();
        if (!courseIds.isEmpty()) {
            userCourseRecords = userCourseRecordService.chunk(id, courseIds);
        }

        // 獲取學員線上課的課時學習數量(只要學習了就算，不一定需要已完成)
        Map<Integer, Integer> userCourseHourCount =
                userCourseHourRecordService.getUserCourseHourCount(id, courseIds, null).stream()
                        .collect(
                                Collectors.toMap(
                                        UserCourseHourRecordCourseCountMapper::getCourseId,
                                        UserCourseHourRecordCourseCountMapper::getTotal));

        // 獲取學員每個課程最早的學習課時記錄
        List<UserCourseHourRecord> perCourseEarliestRecords =
                userCourseHourRecordService.getUserPerCourseEarliestRecord(id);

        HashMap<String, Object> data = new HashMap<>();
        data.put("open_courses", openCourses);
        data.put("departments", departments);
        data.put("dep_courses", depCourses);
        data.put(
                "user_course_records",
                userCourseRecords.stream()
                        .collect(Collectors.toMap(UserCourseRecord::getCourseId, e -> e)));
        data.put("user_course_hour_count", userCourseHourCount);
        data.put(
                "per_course_earliest_records",
                perCourseEarliestRecords.stream()
                        .collect(Collectors.toMap(UserCourseHourRecord::getCourseId, e -> e)));
        // 獲取簽名url
        data.put("resource_url", resourceService.chunksPreSignUrlByIds(rids));
        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN)
    @GetMapping("/{id}/learn-course/{courseId}")
    @SneakyThrows
    @Log(title = "學員-單個課程的學習記錄", businessType = BusinessTypeConstant.GET)
    public JsonResponse learnCourseDetail(
            @PathVariable(name = "id") Integer id,
            @PathVariable(name = "courseId") Integer courseId) {
        // 讀取線上課下的所有課時
        List<CourseHour> hours = courseHourService.getHoursByCourseId(courseId);
        // 讀取學員的課時學習記錄
        List<UserCourseHourRecord> records = userCourseHourRecordService.getRecords(id, courseId);

        HashMap<String, Object> data = new HashMap<>();
        data.put("hours", hours);
        data.put(
                "learn_records",
                records.stream()
                        .collect(Collectors.toMap(UserCourseHourRecord::getHourId, e -> e)));

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN)
    @GetMapping("/{id}/learn-stats")
    @SneakyThrows
    @Log(title = "學員-學習統計", businessType = BusinessTypeConstant.GET)
    public JsonResponse learn(@PathVariable(name = "id") Integer id) {
        // 最近一個月的每天學習時長
        String todayStr = DateTime.now().toDateStr();
        String startDateStr = DateTime.of(DateTime.now().getTime() - 86400000L * 30).toDateStr();
        long startTime = new DateTime(startDateStr).getTime();
        long endTime = new DateTime(todayStr).getTime();

        List<UserLearnDurationStats> monthRecords =
                userLearnDurationStatsService.dateBetween(id, startDateStr, todayStr);
        Map<String, Long> date2duration =
                monthRecords.stream()
                        .collect(
                                Collectors.toMap(
                                        e -> DateTime.of(e.getCreatedDate()).toDateStr(),
                                        UserLearnDurationStats::getDuration));

        @Data
        class StatsItem {
            private String key;
            private Long value;
        }

        List<StatsItem> data = new ArrayList<>();

        while (startTime <= endTime) {
            String dateKey = DateTime.of(startTime).toDateStr();

            Long duration = 0L;
            if (date2duration.get(dateKey) != null) {
                duration = date2duration.get(dateKey);
            }

            StatsItem tmpItem = new StatsItem();
            tmpItem.setKey(dateKey);
            tmpItem.setValue(duration);

            data.add(tmpItem);

            startTime += 86400000;
        }

        return JsonResponse.data(data);
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN_DESTROY)
    @DeleteMapping("/{id}/learn-course/{courseId}")
    @SneakyThrows
    @Log(title = "學員-線上課學習記錄刪除", businessType = BusinessTypeConstant.DELETE)
    public JsonResponse destroyUserCourse(
            @PathVariable(name = "id") Integer id,
            @PathVariable(name = "courseId") Integer courseId) {
        userCourseRecordService.destroy(id, courseId);
        ctx.publishEvent(new UserCourseRecordDestroyEvent(this, id, courseId));
        return JsonResponse.success();
    }

    @BackendPermission(slug = BPermissionConstant.USER_LEARN_DESTROY)
    @DeleteMapping("/{id}/learn-course/{courseId}/hour/{hourId}")
    @SneakyThrows
    @Log(title = "學員-線上課課時學習記錄刪除", businessType = BusinessTypeConstant.DELETE)
    public JsonResponse destroyUserHour(
            @PathVariable(name = "id") Integer id,
            @PathVariable(name = "courseId") Integer courseId,
            @PathVariable(name = "hourId") Integer hourId) {
        userCourseHourRecordService.remove(id, courseId, hourId);
        ctx.publishEvent(new UserCourseHourRecordDestroyEvent(this, id, courseId, hourId));
        return JsonResponse.success();
    }
}
