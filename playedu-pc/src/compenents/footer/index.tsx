import React from "react";
import { Layout } from "antd";
import { Link } from "react-router-dom";

export const Footer: React.FC = () => {
  return (
    <Layout.Footer
      style={{
        backgroundColor: "#333333",
        height: 90,
        textAlign: "center",
        marginTop: 80,
        boxSizing: "border-box",
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
