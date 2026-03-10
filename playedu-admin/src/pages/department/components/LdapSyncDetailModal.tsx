import React, { useState, useEffect } from "react";
import { Modal, Card, Row, Col, Statistic, Divider } from "antd";
import { ldap } from "../../../api";
import { LdapSyncItemsModal } from ".";
import { dateFormat } from "../../../utils/index";

interface LdapSyncDetailModalProps {
  record: any;
  open: boolean;
  onCancel: () => void;
}

export const LdapSyncDetailModal: React.FC<LdapSyncDetailModalProps> = ({ 
  record, 
  open, 
  onCancel 
}) => {
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<any>(null);
  const [itemsVisible, setItemsVisible] = useState(false);
  const [itemsType, setItemsType] = useState<"department" | "user">("department");
  const [itemsAction, setItemsAction] = useState<number>(0);

  useEffect(() => {
    if (open && record) {
      loadDetail();
    }
  }, [open, record]);

  const loadDetail = () => {
    setLoading(true);
    ldap.getSyncRecordDetail(record.id).then((res: any) => {
      setDetail(res.data);
      setLoading(false);
    }).catch(() => {
      setLoading(false);
    });
  };

  const showItems = (type: "department" | "user", action: number) => {
    setItemsType(type);
    setItemsAction(action);
    setItemsVisible(true);
  };

  return (
    <>
      <Modal
        title="同步詳情"
        open={open}
        onCancel={onCancel}
        width={888}
        footer={null}
      >
        {detail && (
          <>
            <Card title="基本信息" loading={loading}>
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic title="同步ID" value={detail.id} />
                </Col>
                <Col span={8}>
                  <Statistic 
                    title="同步狀態" 
                    value={
                      detail.status === 0 ? "進行中" : 
                      detail.status === 1 ? "成功" : "失敗"
                    } 
                  />
                </Col>
                <Col span={8}>
                  <Statistic title="同步時間" value={dateFormat(detail.created_at)} />
                </Col>
              </Row>
            </Card>

            <Divider />

            <Card title="部門同步統計" loading={loading}>
              <Row gutter={16}>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("department", 0)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="總部門數" 
                      value={detail.total_department_count} 
                    />
                  </div>
                </Col>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("department", 1)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="新增部門" 
                      value={detail.created_department_count}
                    />
                  </div>
                </Col>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("department", 2)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="更新部門" 
                      value={detail.updated_department_count}
                    />
                  </div>
                </Col>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("department", 3)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="刪除部門" 
                      value={detail.deleted_department_count}
                    />
                  </div>
                </Col>
              </Row>
            </Card>

            <Divider />

            <Card title="使用者同步統計" loading={loading}>
              <Row gutter={16}>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("user", 0)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="總使用者數" 
                      value={detail.total_user_count}
                    />
                  </div>
                </Col>
                <Col span={4}>
                  <div 
                    onClick={() => showItems("user", 1)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="新增使用者" 
                      value={detail.created_user_count}
                    />
                  </div>
                </Col>
                <Col span={4}>
                  <div 
                    onClick={() => showItems("user", 2)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="更新使用者" 
                      value={detail.updated_user_count}
                    />
                  </div>
                </Col>
                <Col span={4}>
                  <div 
                    onClick={() => showItems("user", 3)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="刪除使用者" 
                      value={detail.deleted_user_count}
                    />
                  </div>
                </Col>
                <Col span={6}>
                  <div 
                    onClick={() => showItems("user", 5)}
                    className="clickable-stat"
                  >
                    <Statistic 
                      title="禁止使用者" 
                      value={detail.banned_user_count}
                    />
                  </div>
                </Col>
              </Row>
            </Card>

            {detail.error_message && (
              <>
                <Divider />
                <Card title="錯誤信息" loading={loading}>
                  <pre>{detail.error_message}</pre>
                </Card>
              </>
            )}
          </>
        )}
      </Modal>

      {detail && (
        <LdapSyncItemsModal
          recordId={detail.id}
          type={itemsType}
          action={itemsAction}
          open={itemsVisible}
          onCancel={() => setItemsVisible(false)}
        />
      )}
    </>
  );
}; 