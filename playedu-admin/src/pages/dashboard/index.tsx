import { useState, useEffect, useRef } from "react";
import styles from "./index.module.less";
import { Row, Col } from "antd";
import { Link, useNavigate } from "react-router-dom";
import banner from "../../assets/images/dashboard/img-a1.png";
import icon from "../../assets/images/dashboard/icon-more.png";
import iconN1 from "../../assets/images/dashboard/icon-n1.png";
import iconN2 from "../../assets/images/dashboard/icon-n2.png";
import iconN3 from "../../assets/images/dashboard/icon-n3.png";
import { Footer } from "../../compenents/footer";
import { dashboard } from "../../api/index";
import { timeFormat } from "../../utils/index";
import * as echarts from "echarts";

type BasicDataModel = {
  admin_user_total: number;
  course_total: number;
  department_total: number;
  resource_category_total: number;
  resource_image_total: number;
  resource_video_total: number;
  user_learn_today: number;
  user_learn_top10?: Top10Model[];
  user_learn_top10_users?: Top10UserModel;
  user_learn_yesterday: number;
  user_today: number;
  user_total: number;
  user_yesterday: number;
  version: string;
};

type Top10Model = {
  created_date: string;
  duration: number;
  id: number;
  user_id: number;
};

type Top10UserModel = {
  [key: number]: UserModel;
};

const DashboardPage = () => {
  let chartRef = useRef(null);
  const navigate = useNavigate();
  const [basicData, setBasicData] = useState<BasicDataModel | null>(null);

  const getData = () => {
    dashboard.dashboardList().then((res: any) => {
      setBasicData(res.data);
      renderPieView({
        videos_count: res.data.resource_video_total,
        images_count: res.data.resource_image_total,
        courseware_count: res.data.resource_file_total,
      });
      return () => {
        window.onresize = null;
      };
    });
  };

  useEffect(() => {
    getData();
  }, []);

  const renderPieView = (params: any) => {
    let num =
      params.videos_count + params.images_count + params.courseware_count;
    let data = [
      {
        name: "影片數",
        value: params.videos_count,
      },
      {
        name: "圖片數",
        value: params.images_count,
      },
      {
        name: "課件數",
        value: params.courseware_count,
      },
    ];
    let dom: any = chartRef.current;
    let myChart = echarts.init(dom);
    myChart.setOption({
      title: {
        textAlign: "center",
        x: "49.5%",
        y: "29%",
        text: num, //主標題
        subtext: "總資源數", //副標題
        textStyle: {
          //標題樣式
          fontSize: 24,
          fontWeight: "bolder",
          color: "#333",
        },
        subtextStyle: {
          //副標題樣式
          fontSize: 14,
          fontWeight: "bolder",
          color: "rgba(0, 0, 0, 0.45)",
          formatter: "",
        },
      },
      legend: [
        {
          selectedMode: true, // 圖例選擇的模式，控制是否可以通過點選圖例改變系列的顯示狀態。預設開啓圖例選擇，可以設成 false 關閉。
          bottom: "10%",
          left: "center",
          textStyle: {
            // 圖例的公用文本樣式。
            fontSize: 14,
            color: " #333333",
          },
          data: ["影片數", "圖片數", "課件數"],
        },
      ],
      tooltip: {
        trigger: "item",
        formatter: " {b}: {c} ",
      },
      label: {
        formatter: " {b}: {c} ",
        rich: {
          per: {
            color: "#000",
          },
        },
      },
      series: [
        {
          type: "pie",
          radius: ["40%", "60%"], // 環比 圈的大小
          center: ["50%", "40%"], // 圖形在整個canvas中的位置
          color: ["#FE8650", "#FFB504", "#00cc66"], // item的取色盤
          avoidLabelOverlap: true,
          itemStyle: {
            borderColor: "#fff", // 白邊
            borderWidth: 2,
          },
          emphasis: {
            // 高亮item的樣式
            disabled: true,
          },
          label: {
            normal: {
              show: true,
              color: "#4c4a4a",
              formatter: "{active|{c}}\n\r{total| {b} }",
              rich: {
                total: {
                  fontSize: 15,
                  color: "#454c5c",
                },
                active: {
                  fontSize: 15,
                  color: "#6c7a89",
                  lineHeight: 30,
                },
              },
            },
          },
          data: data,
        },
      ],
    });
    window.onresize = () => {
      myChart.resize();
    };
  };

  const compareNum = (today: number, yesterday: number) => {
    let num = today - yesterday || 0;
    if (num < 0) {
      return (
        <span className="c-green">
          <i className={styles["down"]}>&#9660;</i>
          {Math.abs(num)}
        </span>
      );
    }
    return (
      <span className="c-red">
        <i className={styles["up"]}>&#9650;</i>
        {Math.abs(num)}
      </span>
    );
  };

  return (
    <>
      <Row gutter={24}>
        <Col span={12}>
          <div className="playedu-main-top">
            <div className="j-b-flex">
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>今日學習學員</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>
                    {basicData?.user_learn_today}
                  </div>
                  {basicData && (
                    <div className={styles["compare"]}>
                      <span className="mr-5">較昨日</span>
                      {compareNum(
                        basicData.user_learn_today,
                        basicData.user_learn_yesterday
                      )}
                    </div>
                  )}
                </div>
              </div>
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>總學員數</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>{basicData?.user_total}</div>
                  {basicData && (
                    <div className={styles["compare"]}>
                      <span className="mr-5">較昨日</span>
                      {compareNum(basicData.user_today, 0)}
                    </div>
                  )}
                </div>
              </div>
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>線上課數</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>{basicData?.course_total}</div>
                </div>
              </div>
            </div>
          </div>
          <div className="playedu-main-top mt-24">
            <div className={styles["large-title"]}>快捷操作</div>
            <div className={styles["mode-box"]}>
              <div
                className={styles["link-mode"]}
                onClick={() => {
                  navigate("/member/index");
                }}
              >
                <i
                  className="iconfont icon-adduser"
                  style={{ color: "#FF9F32", fontSize: 36 }}
                ></i>
                <span>添加學員</span>
              </div>
              <div
                className={styles["link-mode"]}
                onClick={() => {
                  navigate("/videos");
                }}
              >
                <i
                  className="iconfont icon-upvideo"
                  style={{ color: "#419FFF", fontSize: 36 }}
                ></i>
                <span>上傳影片</span>
              </div>
              <div
                className={styles["link-mode"]}
                onClick={() => {
                  navigate("/course");
                }}
              >
                <i
                  className="iconfont icon-onlinelesson"
                  style={{ color: "#B284FF", fontSize: 36 }}
                ></i>
                <span>線上課</span>
              </div>
              <div
                className={styles["link-mode"]}
                onClick={() => {
                  navigate("/department");
                }}
              >
                <i
                  className="iconfont icon-department"
                  style={{ color: "#21C785", fontSize: 36 }}
                ></i>
                <span>新增部門</span>
              </div>
            </div>
          </div>
          <div className="playedu-main-top mt-24" style={{ minHeight: 376 }}>
            <div className={styles["large-title"]}>今日學習排行</div>
            <div className={styles["rank-list"]}>
              {basicData?.user_learn_top10 && (
                <div className={styles["half-list"]}>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <img
                        className={styles["item-icon"]}
                        src={iconN1}
                        alt=""
                      />
                      {basicData.user_learn_top10[0] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[0].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[0] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[0].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <img
                        className={styles["item-icon"]}
                        src={iconN2}
                        alt=""
                      />
                      {basicData.user_learn_top10[1] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[1].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[1] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[1].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <img
                        className={styles["item-icon"]}
                        src={iconN3}
                        alt=""
                      />
                      {basicData.user_learn_top10[2] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[2].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[2] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[2].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>4</div>
                      {basicData.user_learn_top10[3] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[3].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[3] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[3].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>5</div>
                      {basicData.user_learn_top10[4] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[4].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[4] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[4].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                </div>
              )}
              {basicData?.user_learn_top10 && (
                <div className={styles["half-list"]}>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>6</div>
                      {basicData.user_learn_top10[5] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[5].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[5] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[5].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>7</div>
                      {basicData.user_learn_top10[6] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[6].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[6] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[6].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>8</div>
                      {basicData.user_learn_top10[7] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[7].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[7] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[7].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>9</div>
                      {basicData.user_learn_top10[8] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[8].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[8] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[8].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                  <div className={styles["rank-item"]}>
                    <div className={styles["left-item"]}>
                      <div className={styles["item-num"]}>10</div>
                      {basicData.user_learn_top10[9] &&
                        basicData.user_learn_top10_users && (
                          <div className={styles["item-name"]}>
                            {
                              basicData.user_learn_top10_users[
                                basicData.user_learn_top10[9].user_id
                              ]?.name
                            }
                          </div>
                        )}
                    </div>
                    {basicData.user_learn_top10[9] && (
                      <div className={styles["item-time"]}>
                        {timeFormat(
                          Number(basicData.user_learn_top10[9].duration) / 1000
                        )}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </Col>
        <Col span={12}>
          <div className="playedu-main-top">
            <div className="j-b-flex">
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>部門數</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>
                    {basicData?.department_total}
                  </div>
                </div>
              </div>
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>分類數</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>
                    {basicData?.resource_category_total}
                  </div>
                </div>
              </div>
              <div className={styles["label-item"]}>
                <div className={styles["label"]}>管理員</div>
                <div className={styles["info"]}>
                  <div className={styles["num"]}>
                    {basicData?.admin_user_total}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="playedu-main-top mt-24">
            <div className={styles["large-title"]}>產品文檔</div>
            <div className={styles["usage-guide"]}>
              <img className={styles["banner"]} src={banner} alt="" />
              <Link
                to="https://faq.playeduos.com/opensource-maintenance-handbook/article/t08o2iHfLR"
                target="blank"
                className={styles["link"]}
              >
                點選查看產品文檔，快速玩轉Playedu！
                <img className={styles["icon"]} src={icon} alt="" />
              </Link>
            </div>
          </div>
          <div className="playedu-main-top mt-24">
            <div className={styles["large-title"]}>資源統計</div>
            <div className={styles["charts"]}>
              <div
                ref={chartRef}
                style={{ width: "100%", height: 280, position: "relative" }}
              ></div>
            </div>
          </div>
        </Col>
        <Footer></Footer>
      </Row>
    </>
  );
};

export default DashboardPage;
