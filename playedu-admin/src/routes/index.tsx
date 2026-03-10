import { lazy } from "react";
import { RouteObject } from "react-router-dom";
import { login, system } from "../api";

import { getToken } from "../utils";
import KeepAlive from "../compenents/keep-alive";
// 頁面載入
import InitPage from "../pages/init";
import LoginPage from "../pages/login";
import WithHeaderWithoutFooter from "../pages/layouts/with-header-without-footer";
import WithoutHeaderWithoutFooter from "../pages/layouts/without-header-without-footer";

//首頁
const DashboardPage = lazy(() => import("../pages/dashboard"));
//修改密碼頁面
const ChangePasswordPage = lazy(() => import("../pages/change-password"));
//資源管理相關
const ResourceCategoryPage = lazy(
  () => import("../pages/resource/resource-category")
);
const ResourceImagesPage = lazy(() => import("../pages/resource/images"));
const ResourceVideosPage = lazy(() => import("../pages/resource/videos"));
const ResourceCoursewarePage = lazy(
  () => import("../pages/resource/courseware")
);
//課程相關
const CoursePage = lazy(() => import("../pages/course/index"));
const CourseUserPage = lazy(() => import("../pages/course/user"));
//學員相關
const MemberPage = lazy(() => import("../pages/member"));
const MemberImportPage = lazy(() => import("../pages/member/import"));
const MemberLearnPage = lazy(() => import("../pages/member/learn"));
const MemberDepartmentProgressPage = lazy(
  () => import("../pages/member/departmentUser")
);
//系統相關
const SystemConfigPage = lazy(() => import("../pages/system/config"));
const SystemAdministratorPage = lazy(
  () => import("../pages/system/administrator")
);
const SystemAdminrolesPage = lazy(() => import("../pages/system/adminroles"));
const SystemLogPage = lazy(() => import("../pages/system/adminlog"));
//部門頁面
const DepartmentPage = lazy(() => import("../pages/department"));
//測試
const TestPage = lazy(() => import("../pages/test"));
//錯誤頁面
const ErrorPage = lazy(() => import("../pages/error"));
//使用許可頁面
const LicensingPage = lazy(() => import("../pages/licensing/index"));

import PrivateRoute from "../compenents/private-route";

// const LoginPage = lazy(() => import("../pages/login"));

let RootPage: any = null;
if (getToken()) {
  RootPage = lazy(async () => {
    return new Promise<any>(async (resolve) => {
      try {
        let configRes: any = await system.getSystemConfig();
        let userRes: any = await login.getUser();

        resolve({
          default: (
            <InitPage configData={configRes.data} loginData={userRes.data} />
          ),
        });
      } catch (e) {
        console.error("系統初始化失敗", e);
        resolve({
          default: <ErrorPage />,
        });
      }
    });
  });
} else {
  RootPage = <InitPage />;
}

const routes: RouteObject[] = [
  {
    path: "/",
    element: RootPage,
    children: [
      {
        path: "/",
        element: <PrivateRoute Component={<WithHeaderWithoutFooter />} />,
        children: [
          {
            path: "/",
            element: <PrivateRoute Component={<DashboardPage />} />,
          },
          {
            path: "/change-password",
            element: <PrivateRoute Component={<ChangePasswordPage />} />,
          },
          {
            path: "/resource-category",
            element: <PrivateRoute Component={<ResourceCategoryPage />} />,
          },
          {
            path: "/images",
            element: <PrivateRoute Component={<ResourceImagesPage />} />,
          },
          {
            path: "/videos",
            element: <PrivateRoute Component={<ResourceVideosPage />} />,
          },
          {
            path: "/courseware",
            element: <PrivateRoute Component={<ResourceCoursewarePage />} />,
          },
          {
            path: "/course",
            element: <PrivateRoute Component={<CoursePage />} />,
          },
          {
            path: "/course/user/:courseId",
            element: <PrivateRoute Component={<CourseUserPage />} />,
          },
          {
            path: "/member",
            element: <KeepAlive />,
            children: [
              {
                path: "/member/index",
                element: <PrivateRoute Component={<MemberPage />} />,
              },
              {
                path: "/member/import",
                element: <PrivateRoute Component={<MemberImportPage />} />,
              },
              {
                path: "/member/learn",
                element: <PrivateRoute Component={<MemberLearnPage />} />,
              },
              {
                path: "/member/departmentUser",
                element: (
                  <PrivateRoute Component={<MemberDepartmentProgressPage />} />
                ),
              },
            ],
          },
          {
            path: "/system/config/index",
            element: <PrivateRoute Component={<SystemConfigPage />} />,
          },
          {
            path: "/system/administrator",
            element: <PrivateRoute Component={<SystemAdministratorPage />} />,
          },
          {
            path: "/system/adminroles",
            element: <PrivateRoute Component={<SystemAdminrolesPage />} />,
          },
          {
            path: "/system/adminlog",
            element: <PrivateRoute Component={<SystemLogPage />} />,
          },
          {
            path: "/department",
            element: <PrivateRoute Component={<DepartmentPage />} />,
          },
          {
            path: "/licensing",
            element: <PrivateRoute Component={<LicensingPage />} />,
          },
        ],
      },
      {
        path: "/",
        element: <WithoutHeaderWithoutFooter />,
        children: [
          {
            path: "/login",
            element: <LoginPage />,
          },
          {
            path: "/test",
            element: <TestPage />,
          },
          {
            path: "/error",
            element: <ErrorPage />,
          },
          {
            path: "*",
            element: <ErrorPage />,
          },
        ],
      },
    ],
  },
];

export default routes;
