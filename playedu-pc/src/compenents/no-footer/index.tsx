import React from "react";
import { Layout } from "antd";
import { Link } from "react-router-dom";

export const NoFooter: React.FC = () => {
  return (
    <Layout.Footer
      style={{
        backgroundColor: "#ffffff",
        height: 130,
        textAlign: "center",
        paddingBottom: 100,
      }}
    >
      <Link to="https://www.playeduos.com/" target="_blank">
        {/* 此處爲版權標識，嚴禁刪改 */}
        <i
          style={{ fontSize: 30, color: "#cccccc" }}
          className="iconfont icon-waterprint"
        ></i>
      </Link>
    </Layout.Footer>
  );
};
