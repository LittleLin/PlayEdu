import { useEffect, useState } from "react";
import { Button, Result } from "antd";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import styles from "./index.module.less";

const ErrorPage = () => {
  const navigate = useNavigate();
  const result = new URLSearchParams(useLocation().search);
  const [code, setCode] = useState(Number(result.get("code")));
  const [error, setError] = useState("");

  useEffect(() => {
    setCode(Number(result.get("code")));
  }, [result.get("code")]);

  useEffect(() => {
    if (code === 403) {
      setError("無權限操作");
    } else if (code === 404) {
      setError("URL或資源不存在");
    } else if (code === 429) {
      setError("請求次數過多，請稍後再試");
    } else {
      setError("系統錯誤");
    }
  }, [code]);

  return (
    <Result
      status="404"
      title={code}
      subTitle={error}
      className={styles["main"]}
      extra={
        <Button
          type="primary"
          onClick={() => {
            navigate("/", { replace: true });
          }}
        >
          返回首頁
        </Button>
      }
    />
  );
};

export default ErrorPage;
