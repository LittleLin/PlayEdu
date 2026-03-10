import { useEffect, useState } from "react";
import {
  Button,
  Modal,
  Image,
  Table,
  Typography,
  Input,
  message,
  Space,
  Tabs,
  Dropdown,
} from "antd";
import { course } from "../../api";
import {
  PlusOutlined,
  DownOutlined,
  ExclamationCircleFilled,
} from "@ant-design/icons";
import type { MenuProps } from "antd";
import type { ColumnsType } from "antd/es/table";
import { dateFormat } from "../../utils/index";
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import { TreeDepartment, TreeCategory, PerButton } from "../../compenents";
import type { TabsProps } from "antd";
import { CourseCreate } from "./compenents/create";
import { CourseUpdate } from "./compenents/update";
import { CourseHourUpdate } from "./compenents/hour-update";
import { CourseAttachmentUpdate } from "./compenents/attachment-update";
import defaultThumb1 from "../../assets/thumb/thumb1.png";
import defaultThumb2 from "../../assets/thumb/thumb2.png";
import defaultThumb3 from "../../assets/thumb/thumb3.png";

const { confirm } = Modal;

interface DataType {
  id: React.Key;
  charge: number;
  class_hour: number;
  created_at: string;
  is_required: number;
  is_show: number;
  short_desc: string;
  thumb: string;
  title: string;
}

interface LocalSearchParamsInterface {
  page?: number;
  size?: number;
  title?: string;
}

const CoursePage = () => {
  const result = new URLSearchParams(useLocation().search);

  const [searchParams, setSearchParams] = useSearchParams({
    page: "1",
    size: "10",
    title: "",
  });
  const page = parseInt(searchParams.get("page") || "1");
  const size = parseInt(searchParams.get("size") || "10");
  const title = searchParams.get("title");

  const navigate = useNavigate();
  const [list, setList] = useState<DataType[]>([]);
  const [total, setTotal] = useState(0);
  const [refresh, setRefresh] = useState(false);
  const [loading, setLoading] = useState(true);
  const [category_ids, setCategoryIds] = useState<number[]>([]);
  const [dep_ids, setDepIds] = useState<number[]>([]);
  const [selLabel, setLabel] = useState<string>(
    result.get("label") ? String(result.get("label")) : "全部分類"
  );
  const [selDepLabel, setDepLabel] = useState<string>(
    result.get("label") ? String(result.get("label")) : "全部部門"
  );
  const [course_category_ids, setCourseCategoryIds] =
    useState<CategoryIdsModel>({});
  const [course_dep_ids, setCourseDepIds] = useState<DepIdsModel>({});
  const [categories, setCategories] = useState<CategoriesModel>({});
  const [resourceUrl, setResourceUrl] = useState<ResourceUrlModel>({});
  const [departments, setDepartments] = useState<DepartmentsModel>({});
  const [tabKey, setTabKey] = useState(result.get("did") ? "2" : "1");
  const [adminUsers, setAdminUsers] = useState<any>({});
  const [createVisible, setCreateVisible] = useState(false);
  const [updateVisible, setUpdateVisible] = useState(false);
  const [updateHourVisible, setHourUpdateVisible] = useState(false);
  const [updateAttachmentVisible, setUpdateAttachmentVisible] = useState(false);
  const [cid, setCid] = useState(0);
  const [cateId, setCateId] = useState(Number(result.get("cid")));
  const [did, setDid] = useState(Number(result.get("did")));

  useEffect(() => {
    getList();
  }, [category_ids, dep_ids, refresh, page, size, tabKey]);

  useEffect(() => {
    setCateId(Number(result.get("cid")));
    if (Number(result.get("cid"))) {
      let arr = [];
      arr.push(Number(result.get("cid")));
      setCategoryIds(arr);
    }
    setDid(Number(result.get("did")));
    if (Number(result.get("did"))) {
      let arr = [];
      arr.push(Number(result.get("did")));
      setDepIds(arr);
    }
  }, [result.get("cid"), result.get("did")]);

  const items: TabsProps["items"] = [
    {
      key: "1",
      label: `分類`,
      children: (
        <div className="float-left">
          <TreeCategory
            selected={category_ids}
            type=""
            text={"分類"}
            onUpdate={(keys: any, title: any) => {
              resetLocalSearchParams({
                page: 1,
              });
              setCategoryIds(keys);
              if (typeof title === "string") {
                setLabel(title);
              } else {
                setLabel(title.props.children[0]);
              }
            }}
          />
        </div>
      ),
    },
    {
      key: "2",
      label: `部門`,
      children: (
        <div className="float-left">
          <TreeDepartment
            selected={dep_ids}
            showNum={false}
            refresh={refresh}
            text={"部門"}
            onUpdate={(keys: any, title: any) => {
              resetLocalSearchParams({
                page: 1,
              });
              setDepIds(keys);
              setDepLabel(title);
            }}
          />
        </div>
      ),
    },
  ];

  const columns: ColumnsType<DataType> = [
    {
      title: "課程名稱",
      width: 350,
      render: (_, record: any) => (
        <div className="d-flex">
          <Image
            preview={false}
            width={80}
            height={60}
            style={{ borderRadius: 6 }}
            src={
              record.thumb === -1
                ? defaultThumb1
                : record.thumb === -2
                ? defaultThumb2
                : record.thumb === -3
                ? defaultThumb3
                : resourceUrl[record.thumb]
            }
          ></Image>
          <span className="ml-8">{record.title}</span>
        </div>
      ),
    },
    {
      title: "課程分類",
      dataIndex: "id",
      render: (id: number) => (
        <div className="float-left">
          {course_category_ids[id].map((item: any, index: number) => {
            return (
              <span key={index}>
                {index === course_category_ids[id].length - 1
                  ? categories[item]
                  : categories[item] + "、"}
              </span>
            );
          })}
        </div>
      ),
    },
    {
      title: "指派部門",
      dataIndex: "id",
      render: (id: number) => (
        <div className="float-left">
          {course_dep_ids[id] &&
            course_dep_ids[id].map((item: any, index: number) => {
              return (
                <span key={index}>
                  {index === course_dep_ids[id].length - 1
                    ? departments[item]
                    : departments[item] + "、"}
                </span>
              );
            })}
          {!course_dep_ids[id] && <span>全部部門</span>}
        </div>
      ),
    },
    {
      title: "必修/選修",
      dataIndex: "is_required",
      render: (is_required: number) => (
        <span>{is_required === 1 ? "必修課" : "選修課"}</span>
      ),
    },
    {
      title: "建立人",
      dataIndex: "admin_id",
      render: (text: number) =>
        adminUsers && JSON.stringify(adminUsers) !== "{}" ? (
          <span>{adminUsers[text]}</span>
        ) : (
          <span>-</span>
        ),
    },
    {
      title: "上架時間",
      dataIndex: "sort_at",
      render: (text: string) => <span>{dateFormat(text)}</span>,
    },
    {
      title: "操作",
      key: "action",
      fixed: "right",
      width: 160,
      render: (_, record: any) => {
        const items: MenuProps["items"] = [
          {
            key: "1",
            label: (
              <Button
                type="link"
                size="small"
                className="b-n-link c-red"
                onClick={() => {
                  setCid(Number(record.id));
                  setUpdateVisible(true);
                }}
              >
                編輯
              </Button>
            ),
          },
          {
            key: "2",
            label: (
              <Button
                style={{ verticalAlign: "middle" }}
                type="link"
                size="small"
                className="b-n-link c-red"
                onClick={() => {
                  setCid(Number(record.id));
                  setHourUpdateVisible(true);
                }}
              >
                課時
              </Button>
            ),
          },
          {
            key: "3",
            label: (
              <Button
                style={{ verticalAlign: "middle" }}
                type="link"
                size="small"
                className="b-n-link c-red"
                onClick={() => {
                  setCid(Number(record.id));
                  setUpdateAttachmentVisible(true);
                }}
              >
                課件
              </Button>
            ),
          },
          {
            key: "4",
            label: (
              <Button
                type="link"
                size="small"
                className="b-n-link c-red"
                onClick={() => delItem(record.id)}
              >
                刪除
              </Button>
            ),
          },
        ];

        return (
          <Space size="small">
            <PerButton
              type="link"
              text="學員"
              class="b-link c-red"
              icon={null}
              p="course"
              onClick={() => {
                setCid(Number(record.id));
                navigate(
                  "/course/user/" + Number(record.id) + "?title=" + record.title
                );
              }}
              disabled={null}
            />
            <div className="form-column"></div>
            <Dropdown menu={{ items }}>
              <Button
                type="link"
                className="b-link c-red"
                onClick={(e) => e.preventDefault()}
              >
                <Space size="small" align="center">
                  更多
                  <DownOutlined />
                </Space>
              </Button>
            </Dropdown>
          </Space>
        );
      },
    },
  ];

  // 刪除課程
  const delItem = (id: number) => {
    if (id === 0) {
      return;
    }
    confirm({
      title: "操作確認",
      icon: <ExclamationCircleFilled />,
      content: "確認刪除此課程？",
      centered: true,
      okText: "確認",
      cancelText: "取消",
      onOk() {
        course.destroyCourse(id).then(() => {
          message.success("刪除成功");
          resetList();
        });
      },
      onCancel() {
        console.log("Cancel");
      },
    });
  };

  // 獲取列表
  const getList = () => {
    setLoading(true);
    let categoryIds = "";
    let depIds = "";
    if (tabKey === "1") {
      categoryIds = category_ids.join(",");
    } else {
      depIds = dep_ids.join(",");
    }
    course
      .courseList(page, size, "", "", title ? title : "", depIds, categoryIds)
      .then((res: any) => {
        setTotal(res.data.total);
        setList(res.data.data);
        setCourseCategoryIds(res.data.course_category_ids);
        setCourseDepIds(res.data.course_dep_ids);
        setCategories(res.data.categories);
        setDepartments(res.data.departments);
        setAdminUsers(res.data.admin_users);
        setResourceUrl(res.data.resource_url)
        setLoading(false);
      })
      .catch((err: any) => {
        console.log("錯誤,", err);
      });
  };
  // 重置列表
  const resetList = () => {
    resetLocalSearchParams({
      page: 1,
      size: 10,
      title: "",
    });
    setList([]);
    setRefresh(!refresh);
  };

  const paginationProps = {
    current: page, //當前頁碼
    pageSize: size,
    total: total, // 總條數
    onChange: (page: number, pageSize: number) =>
      handlePageChange(page, pageSize), //改變頁碼的函數
    showSizeChanger: true,
  };

  const handlePageChange = (page: number, pageSize: number) => {
    resetLocalSearchParams({
      page: page,
      size: pageSize,
    });
  };

  const resetLocalSearchParams = (params: LocalSearchParamsInterface) => {
    setSearchParams(
      (prev) => {
        if (typeof params.title !== "undefined") {
          prev.set("title", params.title);
        }
        if (typeof params.page !== "undefined") {
          prev.set("page", params.page + "");
        }
        if (typeof params.size !== "undefined") {
          prev.set("size", params.size + "");
        }
        return prev;
      },
      { replace: true }
    );
  };

  const onChange = (key: string) => {
    setTabKey(key);
  };

  return (
    <>
      <div className="tree-main-body">
        <div className="left-box">
          <Tabs
            defaultActiveKey={tabKey}
            centered
            tabBarGutter={55}
            items={items}
            onChange={onChange}
          />
        </div>
        <div className="right-box">
          <div className="playedu-main-title float-left mb-24">
            線上課 | {tabKey === "1" ? selLabel : selDepLabel}
          </div>
          <div className="float-left j-b-flex mb-24">
            <div className="d-flex">
              <PerButton
                type="primary"
                text="新增課程"
                class="mr-16"
                icon={<PlusOutlined />}
                p="course"
                onClick={() => setCreateVisible(true)}
                disabled={null}
              />
            </div>
            <div className="d-flex">
              <div className="d-flex mr-24">
                <Typography.Text>課程名稱：</Typography.Text>
                <Input
                  value={title || ""}
                  onChange={(e) => {
                    resetLocalSearchParams({
                      title: e.target.value,
                    });
                  }}
                  allowClear
                  style={{ width: 160 }}
                  placeholder="請輸入名稱關鍵字"
                />
              </div>
              <div className="d-flex">
                <Button className="mr-16" onClick={resetList}>
                  重 置
                </Button>
                <Button
                  type="primary"
                  onClick={() => {
                    resetLocalSearchParams({
                      page: 1,
                    });
                    setRefresh(!refresh);
                  }}
                >
                  查 詢
                </Button>
              </div>
            </div>
          </div>
          <div className="float-left">
            <Table
              columns={columns}
              dataSource={list}
              loading={loading}
              pagination={paginationProps}
              rowKey={(record) => record.id}
            />
            <CourseCreate
              cateIds={tabKey === "1" ? category_ids : []}
              // depIds={tabKey === "2" ? dep_ids : []}
              open={createVisible}
              onCancel={() => {
                setCreateVisible(false);
                setRefresh(!refresh);
              }}
            />
            <CourseHourUpdate
              id={cid}
              open={updateHourVisible}
              onCancel={() => {
                setHourUpdateVisible(false);
                setRefresh(!refresh);
              }}
            />
            <CourseUpdate
              id={cid}
              open={updateVisible}
              onCancel={() => {
                setUpdateVisible(false);
                setRefresh(!refresh);
              }}
            />
            <CourseAttachmentUpdate
              id={cid}
              open={updateAttachmentVisible}
              onCancel={() => {
                setUpdateAttachmentVisible(false);
                setRefresh(!refresh);
              }}
            />
          </div>
        </div>
      </div>
    </>
  );
};

export default CoursePage;
