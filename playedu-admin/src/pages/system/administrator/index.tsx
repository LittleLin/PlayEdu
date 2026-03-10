import React, { useState, useEffect } from "react";
import { Typography, Input, Button, Space, Table, Modal, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { PlusOutlined, ExclamationCircleFilled } from "@ant-design/icons";
import { adminUser, adminRole } from "../../../api/index";
import { dateFormat } from "../../../utils/index";
import { useNavigate } from "react-router-dom";
import { TreeAdminroles, PerButton } from "../../../compenents";
import { SystemAdministratorCreate } from "./compenents/create";
import { SystemAdministratorUpdate } from "./compenents/update";
import { SystemAdminrolesCreate } from "../adminroles/compenents/create";
import { SystemAdminrolesUpdate } from "../adminroles/compenents/update";

const { confirm } = Modal;

interface DataType {
  id: React.Key;
  created_at: string;
  email: string;
  is_ban_login: number;
  login_at: string;
  login_ip: string;
  login_times: number;
  name: string;
  updated_at: string;
}

const SystemAdministratorPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [list, setList] = useState<DataType[]>([]);
  const [roles, setRoles] = useState<RolesModel>({});
  const [userRoleIds, setUserRoleIds] = useState<RoleIdsModel>({});
  const [total, setTotal] = useState(0);
  const [refresh, setRefresh] = useState(false);
  const [createVisible, setCreateVisible] = useState(false);
  const [updateVisible, setUpdateVisible] = useState(false);
  const [createRoleVisible, setCreateRoleVisible] = useState(false);
  const [updateRoleVisible, setUpdateRoleVisible] = useState(false);
  const [cid, setCid] = useState(0);
  const [role_ids, setRoleIds] = useState<number[]>([]);
  const [selLabel, setLabel] = useState("全部管理員");
  const [roleDelSuccess, setRoleDelSuccess] = useState(false);
  const [isSuper, setIsSuper] = useState(false);
  const [name, setName] = useState("");

  const columns: ColumnsType<DataType> = [
    {
      title: "管理員",
      dataIndex: "name",
      render: (text: string) => <span>{text}</span>,
    },
    {
      title: "角色",
      dataIndex: "id",
      render: (id: number) => (
        <div className="float-left">
          {userRoleIds[id] &&
            userRoleIds[id].map((item: any, index: number) => {
              return roles[item] ? (
                <span key={index}>
                  {index === userRoleIds[id].length - 1
                    ? roles[item][0].name
                    : roles[item][0].name + "、"}
                </span>
              ) : (
                ""
              );
            })}
        </div>
      ),
    },
    {
      title: "登入電子郵件",
      dataIndex: "email",
    },
    {
      title: "登入IP",
      dataIndex: "login_ip",
      render: (text: string) => <span>{text}</span>,
    },
    {
      title: "上次登入時間",
      dataIndex: "login_at",
      render: (text: string) => <span>{text && dateFormat(text)}</span>,
    },
    {
      title: "禁止登入",
      dataIndex: "is_ban_login",
      render: (text: number) =>
        text === 0 ? <span>否</span> : <span>是</span>,
    },
    {
      title: "操作",
      key: "action",
      fixed: "right",
      width: 160,
      render: (_, record) => (
        <Space size="small">
          <PerButton
            type="link"
            text="編輯"
            class="b-link c-red"
            icon={null}
            p="admin-user-cud"
            onClick={() => {
              setCid(Number(record.id));
              setUpdateVisible(true);
            }}
            disabled={null}
          />
          <div className="form-column"></div>
          <PerButton
            type="link"
            text="刪除"
            class="b-link c-red"
            icon={null}
            p="admin-user-cud"
            onClick={() => delUser(record.id)}
            disabled={null}
          />
        </Space>
      ),
    },
  ];

  useEffect(() => {
    getData();
  }, [refresh, page, size, role_ids]);

  const getData = () => {
    setLoading(true);
    adminUser.adminUserList(page, size, name, role_ids[0]).then((res: any) => {
      setList(res.data.data);
      setRoles(res.data.roles);
      setUserRoleIds(res.data.user_role_ids);
      setTotal(res.data.total);
      setLoading(false);
    });
  };

  const resetData = () => {
    setName("");
    setPage(1);
    setSize(10);
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
    setPage(page);
    setSize(pageSize);
  };

  const delUser = (id: any) => {
    if (id === 0) {
      return;
    }
    confirm({
      title: "操作確認",
      icon: <ExclamationCircleFilled />,
      content: "確認刪除此人員？",
      centered: true,
      okText: "確認",
      cancelText: "取消",
      onOk() {
        adminUser.destroyAdminUser(id).then((res: any) => {
          message.success("操作成功");
          setRefresh(!refresh);
        });
      },
      onCancel() {
        console.log("Cancel");
      },
    });
  };

  const delAdminRole = () => {
    if (role_ids.length === 0) {
      return;
    }
    confirm({
      title: "操作確認",
      icon: <ExclamationCircleFilled />,
      content: "刪除此角色會同時刪除管理員對應關聯權限，確認刪除？",
      centered: true,
      okText: "確認",
      cancelText: "取消",
      onOk() {
        adminRole.destroyAdminRole(role_ids[0]).then((res: any) => {
          message.success("操作成功");
          setRefresh(!refresh);
          setRoleDelSuccess(!roleDelSuccess);
        });
      },
      onCancel() {
        console.log("Cancel");
      },
    });
  };

  return (
    <>
      <div className="tree-main-body">
        <div className="left-box">
          <TreeAdminroles
            roleDelSuccess={roleDelSuccess}
            refresh={refresh}
            type=""
            text={"管理員"}
            onUpdate={(keys: any, title: any, isSuper: boolean) => {
              setRoleIds(keys);
              setLabel(title);
              setIsSuper(isSuper);
            }}
          />
        </div>
        <div className="right-box">
          <div className="d-flex playedu-main-title float-left mb-24">
            {selLabel}
          </div>
          <div className="float-left j-b-flex mb-24">
            <div className="d-flex">
              <PerButton
                type="primary"
                text="添加管理員"
                class="mr-16"
                icon={<PlusOutlined />}
                p="admin-user-cud"
                onClick={() => setCreateVisible(true)}
                disabled={null}
              />
              {role_ids.length === 0 && (
                <PerButton
                  text="新增角色"
                  icon={null}
                  class="mr-16"
                  type="default"
                  p="admin-role"
                  onClick={() => setCreateRoleVisible(true)}
                  disabled={null}
                />
              )}
              {!isSuper && role_ids.length > 0 && (
                <>
                  <PerButton
                    text="角色權限"
                    icon={null}
                    class="mr-16"
                    type="default"
                    p="admin-role"
                    onClick={() => {
                      setUpdateRoleVisible(true);
                    }}
                    disabled={null}
                  />
                  <PerButton
                    text="刪除角色"
                    icon={null}
                    class="mr-16"
                    type="default"
                    p="admin-role"
                    onClick={() => {
                      delAdminRole();
                    }}
                    disabled={null}
                  />
                </>
              )}
            </div>
            <div className="d-flex">
              <div className="d-flex mr-24">
                <Typography.Text>管理員姓名：</Typography.Text>
                <Input
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value);
                  }}
                  allowClear
                  style={{ width: 160 }}
                  placeholder="請輸入管理員姓名"
                />
              </div>
              <div className="d-flex">
                <Button className="mr-16" onClick={resetData}>
                  重 置
                </Button>
                <Button
                  type="primary"
                  onClick={() => {
                    setPage(1);
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
            <SystemAdministratorCreate
              refresh={refresh}
              roleId={role_ids[0]}
              open={createVisible}
              onCancel={() => {
                setCreateVisible(false);
                setRefresh(!refresh);
              }}
            />
            <SystemAdministratorUpdate
              id={cid}
              refresh={refresh}
              open={updateVisible}
              onCancel={() => {
                setUpdateVisible(false);
                setRefresh(!refresh);
              }}
            />
            <SystemAdminrolesCreate
              open={createRoleVisible}
              onCancel={() => {
                setCreateRoleVisible(false);
                setRefresh(!refresh);
              }}
            />
            <SystemAdminrolesUpdate
              id={role_ids[0]}
              open={updateRoleVisible}
              onCancel={() => {
                setUpdateRoleVisible(false);
                setRefresh(!refresh);
              }}
            />
          </div>
        </div>
      </div>
    </>
  );
};

export default SystemAdministratorPage;
