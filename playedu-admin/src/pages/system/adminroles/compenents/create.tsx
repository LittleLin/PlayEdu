import React, { useState, useEffect } from "react";
import {
  Drawer,
  TreeSelect,
  Space,
  Button,
  Form,
  Input,
  message,
  Spin,
} from "antd";
import styles from "./create.module.less";
import { adminRole } from "../../../../api/index";

interface PropInterface {
  open: boolean;
  onCancel: () => void;
}

interface Option {
  value: string | number;
  title: string;
  children?: Option[];
}

export const SystemAdminrolesCreate: React.FC<PropInterface> = ({
  open,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [init, setInit] = useState(true);
  const [loading, setLoading] = useState(false);
  const [permissions, setPermissions] = useState<Option[]>([]);
  const [actions, setActions] = useState<Option[]>([]);

  useEffect(() => {
    setInit(true);
    if (open) {
      getParams();
    }
  }, [open]);

  useEffect(() => {
    form.setFieldsValue({
      name: "",
      permission_ids: [],
      action_ids: [],
    });
  }, [form, open]);

  const getParams = () => {
    adminRole.createAdminRole().then((res: any) => {
      const arr: any = [];
      const arr2: any = [];
      let actions = res.data.perm_action.action;
      let permissions = res.data.perm_action.data;
      for (let i = 0; i < permissions.length; i++) {
        const key = arr.findIndex(
          (it: any) => it.title === permissions[i].group_name
        );
        if (key >= 0) {
          arr[key].children.push({
            title: permissions[i].name,
            value: permissions[i].id,
          });
        } else {
          arr.push({
            title: permissions[i].group_name,
            value: permissions[i].group_name + "-n",
            children: [
              {
                title: permissions[i].name,
                value: permissions[i].id,
              },
            ],
          });
        }
      }
      for (let j = 0; j < actions.length; j++) {
        const key = arr2.findIndex(
          (it: any) => it.title === actions[j].group_name
        );
        if (key >= 0) {
          arr2[key].children.push({
            title: actions[j].name,
            value: actions[j].id,
          });
        } else {
          arr2.push({
            title: actions[j].group_name,
            value: actions[j].group_name + "-n",
            children: [
              {
                title: actions[j].name,
                value: actions[j].id,
              },
            ],
          });
        }
      }
      setPermissions(arr);
      setActions(arr2);
      setInit(false);
    });
  };

  const onFinish = (values: any) => {
    if (loading) {
      return;
    }
    let pids = [];
    let aids = [];
    if (values.permission_ids.length === 0 && values.action_ids.length === 0) {
      message.error("必須選擇至少一個權限！");
      return;
    }
    if (values.permission_ids) {
      pids = values.permission_ids;
    }
    if (values.action_ids) {
      aids = values.action_ids;
    }
    setLoading(true);
    const params = aids.concat(pids);
    adminRole
      .storeAdminRole(values.name, params)
      .then((res: any) => {
        setLoading(false);
        message.success("保存成功！");
        onCancel();
      })
      .catch((e) => {
        setLoading(false);
      });
  };

  const onFinishFailed = (errorInfo: any) => {
    console.log("Failed:", errorInfo);
  };

  return (
    <>
      {open ? (
        <Drawer
          title="新增角色"
          onClose={onCancel}
          maskClosable={false}
          open={true}
          footer={
            <Space className="j-r-flex">
              <Button onClick={() => onCancel()}>取 消</Button>
              <Button
                loading={loading}
                onClick={() => form.submit()}
                type="primary"
              >
                確 認
              </Button>
            </Space>
          }
          width={634}
        >
          {init && (
            <div className="float-left text-center mt-30">
              <Spin></Spin>
            </div>
          )}
          <div
            className="float-left mt-24"
            style={{ display: init ? "none" : "block" }}
          >
            <Form
              form={form}
              name="adminroles-create"
              labelCol={{ span: 5 }}
              wrapperCol={{ span: 19 }}
              initialValues={{ remember: true }}
              onFinish={onFinish}
              onFinishFailed={onFinishFailed}
              autoComplete="off"
            >
              <Form.Item
                label="角色名稱"
                name="name"
                rules={[{ required: true, message: "請輸入角色名稱!" }]}
              >
                <Input
                  style={{ width: 424 }}
                  placeholder="請在此處輸入角色名稱"
                  allowClear
                />
              </Form.Item>
              <Form.Item label="操作權限" name="action_ids">
                <TreeSelect
                  listHeight={600}
                  style={{ width: 424 }}
                  treeCheckable={true}
                  placeholder="請選擇權限"
                  multiple
                  allowClear
                  treeData={actions}
                />
              </Form.Item>
              <Form.Item label="數據權限" name="permission_ids">
                <TreeSelect
                  listHeight={600}
                  style={{ width: 424 }}
                  treeCheckable={true}
                  placeholder="請選擇權限"
                  multiple
                  allowClear
                  treeData={permissions}
                />
              </Form.Item>
            </Form>
          </div>
        </Drawer>
      ) : null}
    </>
  );
};
