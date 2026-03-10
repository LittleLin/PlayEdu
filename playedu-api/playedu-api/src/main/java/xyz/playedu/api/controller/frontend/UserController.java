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

import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.playedu.api.request.frontend.ChangePasswordRequest;
import xyz.playedu.common.constant.CommonConstant;
import xyz.playedu.common.constant.FrontendConstant;
import xyz.playedu.common.context.FCtx;
import xyz.playedu.common.domain.Category;
import xyz.playedu.common.domain.Department;
import xyz.playedu.common.domain.User;
import xyz.playedu.common.domain.UserUploadImageLog;
import xyz.playedu.common.exception.ServiceException;
import xyz.playedu.common.service.*;
import xyz.playedu.common.types.JsonResponse;
import xyz.playedu.common.types.UploadFileInfo;
import xyz.playedu.common.types.config.S3Config;
import xyz.playedu.common.types.mapper.UserCourseHourRecordCourseCountMapper;
import xyz.playedu.common.util.StringUtil;
import xyz.playedu.course.domain.*;
import xyz.playedu.course.service.*;
import xyz.playedu.resource.domain.Resource;
import xyz.playedu.resource.service.ResourceService;
import xyz.playedu.resource.service.UploadService;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Autowired private UserService userService;

    @Autowired private DepartmentService departmentService;

    @Autowired private CourseService courseService;

    @Autowired private CourseHourService hourService;

    @Autowired private UserCourseRecordService userCourseRecordService;

    @Autowired private UserCourseHourRecordService userCourseHourRecordService;

    @Autowired private UserLearnDurationStatsService userLearnDurationStatsService;

    @Autowired private UploadService uploadService;

    @Autowired private CategoryService categoryService;

    @Autowired private AppConfigService appConfigService;

    @Autowired private ResourceService resourceService;

    @Autowired private UserUploadImageLogService userUploadImageLogService;

    @GetMapping("/detail")
    public JsonResponse detail() {
        User user = FCtx.getUser();
        List<Department> departments = new ArrayList<>();
        List<Integer> depIds = userService.getDepIdsByUserId(user.getId());
        if (depIds != null && !depIds.isEmpty()) {
            departments = departmentService.listByIds(depIds);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("departments", departments);

        // 獲取資源簽名url
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

    @PutMapping("/avatar")
    public JsonResponse changeAvatar(MultipartFile file) {
        // 校驗存儲設定是否完整
        S3Config s3Config = appConfigService.getS3Config();
        if (StringUtil.isEmpty(s3Config.getAccessKey())
                || StringUtil.isEmpty(s3Config.getSecretKey())
                || StringUtil.isEmpty(s3Config.getBucket())
                || StringUtil.isEmpty(s3Config.getEndpoint())
                || StringUtil.isEmpty(s3Config.getRegion())) {
            throw new ServiceException("存儲服務未設定");
        }

        UploadFileInfo info = uploadService.upload(s3Config, file, null);

        Resource resource =
                resourceService.create(
                        CommonConstant.ZERO,
                        null,
                        info.getResourceType(),
                        info.getOriginalName(),
                        info.getExtension(),
                        info.getSize(),
                        info.getDisk(),
                        info.getSavePath(),
                        CommonConstant.ZERO,
                        CommonConstant.ONE);

        // 學員頭像
        userService.changeAvatar(FCtx.getId(), resource.getId());

        // 學員上傳圖片記錄
        UserUploadImageLog log = new UserUploadImageLog();
        log.setUserId(FCtx.getId());
        log.setTyped(FrontendConstant.USER_UPLOAD_IMAGE_TYPE_AVATAR);
        log.setScene(FrontendConstant.USER_UPLOAD_IMAGE_SCENE_AVATAR);
        log.setSize(info.getSize());
        log.setDriver(info.getDisk());
        log.setPath(info.getSavePath());
        log.setName(info.getOriginalName());
        log.setCreatedAt(new Date());
        userUploadImageLogService.save(log);

        return JsonResponse.success();
    }

    @PutMapping("/password")
    public JsonResponse changePassword(@RequestBody @Validated ChangePasswordRequest req)
            throws ServiceException {
        userService.passwordChange(FCtx.getUser(), req.getOldPassword(), req.getNewPassword());
        return JsonResponse.success();
    }

    @SneakyThrows
    @GetMapping("/courses")
    public JsonResponse courses(@RequestParam HashMap<String, Object> params) {
        Integer depId = MapUtils.getInteger(params, "dep_id");
        if (depId == null || depId == 0) {
            return JsonResponse.error("請選擇部門");
        }

        Integer categoryId = MapUtils.getInteger(params, "category_id");

        List<Integer> userJoinDepIds = userService.getDepIdsByUserId(FCtx.getId());
        if (userJoinDepIds == null) {
            return JsonResponse.error("當前學員未加入任何部門");
        }
        if (!userJoinDepIds.contains(depId)) {
            return JsonResponse.error("當前學員未加入所選擇部門");
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("learn_course_records", new HashMap<>());

        // 查詢所有的父級部門ID
        List<Integer> allDepIds = new ArrayList<>();
        allDepIds.add(depId);
        Department department = departmentService.findOrFail(depId);
        String parentChain = department.getParentChain();
        if (StringUtil.isNotEmpty(parentChain)) {
            List<Integer> parentChainList =
                    Arrays.stream(parentChain.split(",")).map(Integer::parseInt).toList();
            if (StringUtil.isNotEmpty(parentChainList)) {
                allDepIds.addAll(parentChainList);
            }
        }

        // 獲取所有子分類ID
        List<Integer> allCategoryIds = new ArrayList<>();
        if (categoryId != null && categoryId > 0) {
            allCategoryIds.add(categoryId);
            // 查詢所有的子分類
            List<Category> categoryList = categoryService.getChildCategorysByParentId(categoryId);
            if (StringUtil.isNotEmpty(categoryList)) {
                for (Category category : categoryList) {
                    allCategoryIds.add(category.getId());
                }
            }
        }

        // -------- 讀取當前學員可以參加的課程 ----------
        List<Course> courses = new ArrayList<>();
        // 讀取部門課
        List<Course> depCourses = courseService.getDepCoursesAndShow(allDepIds, allCategoryIds);
        // 全部部門課
        List<Course> openCourses = courseService.getOpenCoursesAndShow(500, allCategoryIds);
        // 彙總到一個list中
        if (depCourses != null && !depCourses.isEmpty()) {
            courses.addAll(depCourses);
        }
        if (openCourses != null && !openCourses.isEmpty()) {
            courses.addAll(openCourses);
        }
        // 對結果進行排序->按照課程id倒序
        if (!courses.isEmpty()) {
            courses =
                    courses.stream()
                            .sorted(
                                    Comparator.comparing(
                                                    Course::getSortAt,
                                                    Comparator.nullsFirst(Date::compareTo))
                                            .reversed())
                            .toList();
        }

        data.put("courses", courses);

        List<Integer> courseIds = courses.stream().map(Course::getId).toList();

        // -------- 讀取學習進度 ----------
        Map<Integer, UserCourseRecord> learnCourseRecords = new HashMap<>();
        if (!courses.isEmpty()) {
            learnCourseRecords =
                    userCourseRecordService.chunk(FCtx.getId(), courseIds).stream()
                            .collect(Collectors.toMap(UserCourseRecord::getCourseId, e -> e));
        }
        data.put("learn_course_records", learnCourseRecords);

        int requiredCourseCount = 0;
        int nunRequiredCourseCount = 0;
        int requiredFinishedCourseCount = 0; // 已完成必修課
        int nunRequiredFinishedCourseCount = 0; // 已完成選修課
        int requiredHourCount = 0; // 必修課時
        int nunRequiredHourCount = 0; // 選修課時
        int requiredFinishedHourCount = 0; // 已完成必修課時
        int nunRequiredFinishedHourCount = 0; // 已完成選修課時
        Long todayLearnDuration =
                userLearnDurationStatsService.todayUserDuration(FCtx.getId()); // 今日學習時長
        Long learnDuration = userLearnDurationStatsService.userDuration(FCtx.getId()); // 學習總時長

        // -------- 學習數據統計 ----------
        if (!courses.isEmpty()) {
            for (Course courseItem : courses) {
                if (courseItem.getIsRequired() == 1) {
                    requiredHourCount += courseItem.getClassHour();
                    requiredCourseCount += 1;
                } else {
                    nunRequiredHourCount += courseItem.getClassHour();
                    nunRequiredCourseCount += 1;
                }
                UserCourseRecord learnRecord = learnCourseRecords.get(courseItem.getId());
                if (learnRecord == null) {
                    continue;
                }
                if (courseItem.getIsRequired() == 1) {
                    requiredFinishedHourCount += learnRecord.getFinishedCount();
                    if (learnRecord.getIsFinished() == 1) {
                        requiredFinishedCourseCount++;
                    }
                } else {
                    nunRequiredFinishedHourCount += learnRecord.getFinishedCount();
                    if (learnRecord.getIsFinished() == 1) {
                        nunRequiredFinishedCourseCount++;
                    }
                }
            }
        }
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("required_course_count", requiredCourseCount); // 必修課數量
        stats.put("nun_required_course_count", nunRequiredCourseCount); // 選修課數量
        stats.put("required_finished_course_count", requiredFinishedCourseCount); // 必修已完成線上課數
        stats.put(
                "nun_required_finished_course_count", nunRequiredFinishedCourseCount); // 選修已完成線上課數
        stats.put("required_hour_count", requiredHourCount); // 必修課時總數
        stats.put("nun_required_hour_count", nunRequiredHourCount); // 選修課時總數
        stats.put("required_finished_hour_count", requiredFinishedHourCount); // 必修已完成課時數
        stats.put("nun_required_finished_hour_count", nunRequiredFinishedHourCount); // 選修已完成課時數
        stats.put("today_learn_duration", todayLearnDuration); // 今日學習時長[單位:毫秒]
        stats.put("learn_duration", learnDuration); // 學習總時長[單位:毫秒]
        data.put("stats", stats);

        // 當前學員每個線上課的學習課時數量(只要學習了就算，不一定需要完成)
        data.put(
                "user_course_hour_count",
                userCourseHourRecordService
                        .getUserCourseHourCount(FCtx.getId(), courseIds, null)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        UserCourseHourRecordCourseCountMapper::getCourseId,
                                        UserCourseHourRecordCourseCountMapper::getTotal)));

        // 獲取簽名url
        data.put(
                "resource_url",
                resourceService.chunksPreSignUrlByIds(
                        courses.stream().map(Course::getThumb).toList()));
        return JsonResponse.data(data);
    }

    @GetMapping("/latest-learn")
    public JsonResponse latestLearn() {
        // 讀取當前學員最近100條學習的線上課
        List<UserCourseHourRecord> userCourseHourRecords =
                userCourseHourRecordService.getLatestCourseIds(FCtx.getId(), 100);
        if (userCourseHourRecords == null || userCourseHourRecords.isEmpty()) {
            return JsonResponse.data(new ArrayList<>());
        }

        List<Integer> courseIds =
                userCourseHourRecords.stream().map(UserCourseHourRecord::getCourseId).toList();
        List<Integer> hourIds =
                userCourseHourRecords.stream().map(UserCourseHourRecord::getHourId).toList();
        Map<Integer, UserCourseHourRecord> hour2Record =
                userCourseHourRecords.stream()
                        .collect(Collectors.toMap(UserCourseHourRecord::getHourId, e -> e));
        Map<Integer, Integer> course2hour =
                userCourseHourRecords.stream()
                        .collect(
                                Collectors.toMap(
                                        UserCourseHourRecord::getCourseId,
                                        UserCourseHourRecord::getHourId));

        // 線上課
        Map<Integer, Course> courses =
                courseService
                        .chunks(
                                courseIds,
                                new ArrayList<>() {
                                    {
                                        add("id");
                                        add("title");
                                        add("thumb");
                                        add("short_desc");
                                        add("class_hour");
                                        add("is_required");
                                    }
                                })
                        .stream()
                        .collect(Collectors.toMap(Course::getId, e -> e));

        // 線上課課時
        Map<Integer, CourseHour> hours =
                hourService.chunk(hourIds).stream()
                        .collect(Collectors.toMap(CourseHour::getId, e -> e));

        // 獲取學員的線上課進度
        Map<Integer, UserCourseRecord> records =
                userCourseRecordService.chunk(FCtx.getId(), courseIds).stream()
                        .collect(Collectors.toMap(UserCourseRecord::getCourseId, e -> e));
        List<UserLatestLearn> userLatestLearns = new ArrayList<>();
        List<Integer> rids = new ArrayList<>();
        for (Integer courseId : courseIds) {
            UserCourseRecord record = records.get(courseId); // 線上課學習進度
            Course tmpCourse = courses.get(courseId); // 線上課
            Integer tmpHourId = course2hour.get(courseId); // 最近學習的課時id
            UserCourseHourRecord tmpUserCourseHourRecord = hour2Record.get(tmpHourId); // 課時學習進度
            CourseHour tmpHour = hours.get(tmpHourId); // 課時

            if (StringUtil.isNotNull(tmpCourse)) {
                rids.add(tmpCourse.getThumb());
            }

            userLatestLearns.add(
                    new UserLatestLearn() {
                        {
                            setCourse(tmpCourse);
                            setUserCourseRecord(record);
                            setHourRecord(tmpUserCourseHourRecord);
                            setLastLearnHour(tmpHour);
                        }
                    });
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("user_latest_learns", userLatestLearns);
        // 獲取簽名url
        data.put("resource_url", resourceService.chunksPreSignUrlByIds(rids));

        return JsonResponse.data(data);
    }
}
