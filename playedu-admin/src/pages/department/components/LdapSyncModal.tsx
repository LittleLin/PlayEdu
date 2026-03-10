import React, { useState, useEffect } from "react";
import { Modal, Table, Button, message } from "antd";
import { EyeOutlined, DownloadOutlined, SyncOutlined } from "@ant-design/icons";
import { department, ldap } from "../../../api";
import { LdapSyncDetailModal } from ".";
import { dateFormat } from "../../../utils/index";

interface LdapSyncModalProps {
  open: boolean;
  onCancel: () => void;
}

export const LdapSyncModal: React.FC<LdapSyncModalProps> = ({
  open,
  onCancel,
}) => {
  const [loading, setLoading] = useState(false);
  const [syncLoading, setSyncLoading] = useState(false);
  const [list, setList] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<any>(null);

  useEffect(() => {
    if (open) {
      loadData();
    }
  }, [open, page, size]);

  const loadData = () => {
    setLoading(true);
    ldap
      .getSyncRecords({ page, size })
      .then((res: any) => {
        setList(res.data.data);
        setTotal(res.data.total);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  };

  const handleSync = () => {
    if (syncLoading) {
      message.warning("正在同步，請稍後...");
      return;
    }
    setSyncLoading(true);
    department
      .ldapSync()
      .then(() => {
        message.success("同步觸發成功");
        setSyncLoading(false);
        // 刷新數據列表
        loadData();
      })
      .catch(() => {
        setSyncLoading(false);
      });
  };

  const handleDownload = (id: number) => {
    ldap
      .downloadSyncRecord(id)
      .then((res: any) => {
        window.open(res.data.url, "_blank");
      })
      .catch((e) => {
        message.error("下載失敗");
      });
  };

  const handleDetail = (record: any) => {
    setCurrentRecord(record);
    setDetailVisible(true);
  };

  const columns = [
    { title: "ID", dataIndex: "id", key: "id" },
    {
      title: "同步狀態",
      dataIndex: "status",
      key: "status",
      render: (status: number) => {
        if (status === 0)
          return <span style={{ color: "#faad14" }}>進行中</span>;
        if (status === 1) return <span style={{ color: "#52c41a" }}>成功</span>;
        return <span style={{ color: "#f5222d" }}>失敗</span>;
      },
    },
    {
      title: "部門總數",
      dataIndex: "total_department_count",
      key: "total_department_count",
    },
    {
      title: "使用者總數",
      dataIndex: "total_user_count",
      key: "total_user_count",
    },
    {
      title: "同步時間",
      dataIndex: "created_at",
      key: "created_at",
      render: (text: string) => <span>{dateFormat(text)}</span>,
    },
    {
      title: "操作",
      key: "action",
      render: (_: any, record: any) => (
        <div className="d-flex">
          <Button
            type="link"
            className="b-link c-red mr-8"
            icon={<EyeOutlined />}
            onClick={() => handleDetail(record)}
          >
            詳情
          </Button>
          <Button
            type="link"
            className="b-link c-red"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record.id)}
            disabled={!record.s3_file_path}
          >
            下載
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <Modal
        title="LDAP同步記錄"
        open={open}
        onCancel={onCancel}
        width={900}
        footer={null}
      >
        <div className="mb-24">
          <Button
            type="primary"
            icon={<SyncOutlined />}
            loading={syncLoading}
            onClick={handleSync}
          >
            建立同步任務
          </Button>
        </div>
        <Table
          columns={columns}
          dataSource={list}
          rowKey="id"
          loading={loading}
          pagination={{
            total,
            current: page,
            pageSize: size,
            onChange: (page, pageSize) => {
              setPage(page);
              setSize(pageSize || 10);
            },
            showSizeChanger: true,
          }}
        />
      </Modal>

      {currentRecord && (
        <LdapSyncDetailModal
          record={currentRecord}
          open={detailVisible}
          onCancel={() => setDetailVisible(false)}
        />
      )}
    </>
  );
};
