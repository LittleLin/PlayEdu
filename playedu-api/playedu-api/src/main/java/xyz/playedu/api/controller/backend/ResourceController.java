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

import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.playedu.api.request.backend.ResourceDestroyMultiRequest;
import xyz.playedu.api.request.backend.ResourceUpdateRequest;
import xyz.playedu.common.annotation.Log;
import xyz.playedu.common.bus.BackendBus;
import xyz.playedu.common.constant.BackendConstant;
import xyz.playedu.common.constant.BusinessTypeConstant;
import xyz.playedu.common.context.BCtx;
import xyz.playedu.common.domain.AdminUser;
import xyz.playedu.common.domain.Category;
import xyz.playedu.common.exception.NotFoundException;
import xyz.playedu.common.exception.ServiceException;
import xyz.playedu.common.service.AdminUserService;
import xyz.playedu.common.service.AppConfigService;
import xyz.playedu.common.service.CategoryService;
import xyz.playedu.common.types.JsonResponse;
import xyz.playedu.common.types.paginate.PaginationResult;
import xyz.playedu.common.types.paginate.ResourcePaginateFilter;
import xyz.playedu.common.util.S3Util;
import xyz.playedu.common.util.StringUtil;
import xyz.playedu.resource.domain.Resource;
import xyz.playedu.resource.domain.ResourceExtra;
import xyz.playedu.resource.service.ResourceExtraService;
import xyz.playedu.resource.service.ResourceService;

@RestController
@RequestMapping("/backend/v1/resource")
public class ResourceController {

    @Autowired private AdminUserService adminUserService;

    @Autowired private ResourceService resourceService;

    @Autowired private ResourceExtraService resourceExtraService;

    @Autowired private AppConfigService appConfigService;

    @Autowired private BackendBus backendBus;

    @Autowired private CategoryService categoryService;

    @GetMapping("/index")
    @Log(title = "資源-列表", businessType = BusinessTypeConstant.GET)
    public JsonResponse index(@RequestParam HashMap<String, Object> params) {
        Integer page = MapUtils.getInteger(params, "page", 1);
        Integer size = MapUtils.getInteger(params, "size", 10);
        String sortField = MapUtils.getString(params, "sort_field");
        String sortAlgo = MapUtils.getString(params, "sort_algo");
        String name = MapUtils.getString(params, "name");
        String type = MapUtils.getString(params, "type");
        String categoryIds = MapUtils.getString(params, "category_ids");

        if (type == null || type.trim().isEmpty()) {
            return JsonResponse.error("請選擇資源類型");
        }

        // 獲取所有子類
        Set<Integer> allCategoryIdsSet = new HashSet<>();
        if (StringUtil.isNotEmpty(categoryIds)) {
            String[] categoryIdArr = categoryIds.split(",");
            if (StringUtil.isNotEmpty(categoryIdArr)) {
                for (String categoryIdStr : categoryIdArr) {
                    Integer categoryId = Integer.parseInt(categoryIdStr);
                    allCategoryIdsSet.add(categoryId);
                    // 查詢所有的子分類
                    List<Category> categoryList =
                            categoryService.getChildCategorysByParentId(categoryId);
                    if (StringUtil.isNotEmpty(categoryList)) {
                        for (Category category : categoryList) {
                            allCategoryIdsSet.add(category.getId());
                        }
                    }
                }
            }
        }

        List<Integer> allCategoryIds = new ArrayList<>();
        if ("0".equals(categoryIds)) {
            allCategoryIds.add(0);
        }
        if (StringUtil.isNotEmpty(allCategoryIdsSet)) {
            allCategoryIds.addAll(allCategoryIdsSet);
        }

        ResourcePaginateFilter filter = new ResourcePaginateFilter();
        filter.setSortAlgo(sortAlgo);
        filter.setSortField(sortField);
        filter.setType(type);
        filter.setCategoryIds(allCategoryIds);
        filter.setName(name);

        if (!backendBus.isSuperAdmin()) { // 非超管只能讀取它自己上傳的資源
            filter.setAdminId(BCtx.getId());
        }

        PaginationResult<Resource> result = resourceService.paginate(page, size, filter);

        HashMap<String, Object> data = new HashMap<>();
        data.put("result", result);

        List<Integer> ids = result.getData().stream().map(Resource::getId).toList();
        if (StringUtil.isNotEmpty(ids)) {
            if (type.equals(BackendConstant.RESOURCE_TYPE_VIDEO)) {
                List<ResourceExtra> resourceExtras = resourceExtraService.chunksByRids(ids);
                Map<Integer, ResourceExtra> resourceVideosExtra =
                        resourceExtras.stream()
                                .collect(Collectors.toMap(ResourceExtra::getRid, e -> e));
                data.put("videos_extra", resourceVideosExtra);
            }

            // 獲取資源簽名url
            data.put("resource_url", resourceService.chunksPreSignUrlByIds(ids));
        }

        // 操作人
        data.put("admin_users", new HashMap<>());
        if (!result.getData().isEmpty()) {
            Map<Integer, String> adminUsers =
                    adminUserService
                            .chunks(result.getData().stream().map(Resource::getAdminId).toList())
                            .stream()
                            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getName));
            data.put("admin_users", adminUsers);
        }

        if (!type.equals(BackendConstant.RESOURCE_TYPE_VIDEO)
                && !type.equals(BackendConstant.RESOURCE_TYPE_IMAGE)) {
            filter.setType(BackendConstant.RESOURCE_TYPE_ATTACHMENT);
            data.put("existing_types", resourceService.paginateType(filter));
        }
        return JsonResponse.data(data);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @SneakyThrows
    @Log(title = "資源-刪除", businessType = BusinessTypeConstant.DELETE)
    public JsonResponse destroy(@PathVariable(name = "id") Integer id) throws NotFoundException {
        Resource resource = resourceService.findOrFail(id);

        if (!backendBus.isSuperAdmin()) {
            if (!resource.getAdminId().equals(BCtx.getId())) {
                throw new ServiceException("無權限");
            }
        }

        // 刪除檔案
        S3Util s3Util = new S3Util(appConfigService.getS3Config());
        s3Util.removeByPath(resource.getPath());
        // 如果是影片資源檔案則刪除對應的時長關聯記錄
        if (BackendConstant.RESOURCE_TYPE_VIDEO.equals(resource.getType())) {
            resourceExtraService.removeByRid(resource.getId());
        }
        // 刪除資源記錄
        resourceService.removeById(resource.getId());
        return JsonResponse.success();
    }

    @PostMapping("/destroy-multi")
    @SneakyThrows
    @Log(title = "資源-批量列表", businessType = BusinessTypeConstant.DELETE)
    public JsonResponse multiDestroy(@RequestBody ResourceDestroyMultiRequest req) {
        if (req.getIds() == null || req.getIds().isEmpty()) {
            return JsonResponse.error("請選擇需要刪除的資源");
        }

        List<Resource> resources = resourceService.chunks(req.getIds());
        if (resources == null || resources.isEmpty()) {
            return JsonResponse.success();
        }

        S3Util s3Util = new S3Util(appConfigService.getS3Config());

        for (Resource resourceItem : resources) {
            // 權限校驗
            if (!backendBus.isSuperAdmin()) {
                if (!resourceItem.getAdminId().equals(BCtx.getId())) {
                    throw new ServiceException("無權限");
                }
            }

            // 刪除資源源檔案
            s3Util.removeByPath(resourceItem.getPath());
            // 如果是影片資源的話還需要刪除影片的關聯資源，如: 封面截圖
            if (BackendConstant.RESOURCE_TYPE_VIDEO.equals(resourceItem.getType())) {
                resourceExtraService.removeByRid(resourceItem.getId());
            }
            // 刪除資料庫的記錄
            resourceService.removeById(resourceItem.getId());
        }
        return JsonResponse.success();
    }

    @GetMapping("/{id}")
    @SneakyThrows
    @Log(title = "資源-編輯", businessType = BusinessTypeConstant.GET)
    public JsonResponse edit(@PathVariable(name = "id") Integer id) {
        Resource resource = resourceService.findOrFail(id);

        if (!backendBus.isSuperAdmin()) {
            if (!resource.getAdminId().equals(BCtx.getId())) {
                throw new ServiceException("無權限");
            }
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("resources", resource);
        data.put("category_ids", resourceService.categoryIds(id));
        // 獲取資源簽名url
        data.put(
                "resource_url",
                resourceService.chunksPreSignUrlByIds(
                        new ArrayList<>() {
                            {
                                add(id);
                            }
                        }));
        return JsonResponse.data(data);
    }

    @PutMapping("/{id}")
    @SneakyThrows
    @Log(title = "資源-編輯", businessType = BusinessTypeConstant.UPDATE)
    public JsonResponse update(
            @RequestBody @Validated ResourceUpdateRequest req,
            @PathVariable(name = "id") Integer id) {
        Resource resource = resourceService.findOrFail(id);

        if (!backendBus.isSuperAdmin()) {
            if (!resource.getAdminId().equals(BCtx.getId())) {
                throw new ServiceException("無權限");
            }
        }

        resourceService.updateNameAndCategoryId(
                resource.getId(), req.getName(), req.getCategoryId());
        return JsonResponse.success();
    }
}
