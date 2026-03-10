import styles from "./index.module.less";
import { Button, Tooltip } from "antd";
import fangIcon from "../../assets/images/commen/fanghu.png";
import ex1Icon from "../../assets/images/commen/icon5.png";
import ex2Icon from "../../assets/images/commen/icon6.png";

const LicensingPage = () => {
  return (
    <>
      <div className="playedu-main-top">
        <div className={styles["main-title"]}>當前版本信息</div>
        <div className="float-left mt-24">
          <div className={styles["persion"]}>PlayEdu開源版 v2.0</div>
        </div>
        <div className="float-left mt-16">
          <div className={styles["content"]}>
            1.版權歸屬：PlayEdu開源版版權歸杭州白書科技有限公司所有，保留全部使用權。
          </div>
          <div className={styles["content"]}>
            2.代碼修改：允許在遵守開源協議的前提下修改代碼，但需在修改處添加明確備註，詳細說明修改內容。
          </div>
          <div className={styles["content"]}>
            3.版權保護：任何場景下均需保留
            PlayEdu開源版頁面及代碼中的原有版權信息（如 “Designed By PlayEdu”
            標識、官網連結、開源說明等），嚴禁刪除、修改或篡改，違者需承擔法律責任及賠償。
          </div>
        </div>
        <div className="float-left mt-24">
          <Button
            type="primary"
            onClick={() => {
              window.open("https://www.playeduos.com/");
            }}
          >
            採購企業版本
          </Button>
        </div>
      </div>
      <div className="playedu-main-top mt-24" style={{ position: "relative" }}>
        <div className={styles["main-title"]}>版本功能對比</div>
        <div className="float-left mt-24">
          <div className={styles["contrast-box1"]}>
            <div className={styles["name"]}>功能特性</div>
            <div className={styles["ex"]}>開源版</div>
            <div className={styles["ex2"]}>企業版</div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>系統支持</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>功能支持</span>
            </div>
            <div className={styles["ex"]}>基礎功能</div>
            <div className={styles["ex2"]}>全部功能</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>技術支持</span>
            </div>
            <div className={styles["ex"]}>無</div>
            <Tooltip
              className={styles["ex2"]}
              title="專屬售後羣以及遠程排障服務"
            >
              <img src={fangIcon} className={styles["icon"]} />
              7*10h專業技術服務
            </Tooltip>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>安全認證</span>
            </div>
            <div className={styles["ex"]}>無</div>
            <div className={styles["ex2"]}>CMA國家資質安全認證</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>部署服務</span>
            </div>
            <div className={styles["ex"]}>無</div>
            <div className={styles["ex2"]}>內外網單機及集羣部署</div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>性能負載</span>
            </div>
            <div className={styles["ex"]}>低</div>
            <div className={styles["ex2"]}>企業級全鏈路高性能場景</div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>資源類型</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>影片庫</span>
            </div>
            <div className={styles["ex"]}>MP4(H264)</div>
            <div className={styles["ex2"]}>MP4|MOV|AVI|WMV|FLV</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>圖片庫</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>附件庫</span>
              <span className={styles["sp"]}>（僅支持上傳下載）</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>文檔庫</span>
              <span className={styles["sp"]}>
                （支持Word、PPT、PDF在線預覽）
              </span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>音訊庫</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>證書庫</span>
              <span className={styles["sp"]}>（支持自定義證書設計）</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>講師資料</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>考試中心</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>試題庫</span>
              <span className={styles["sp"]}>
                （支持六大題型，支持批量導入）
              </span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>試卷庫</span>
              <span className={styles["sp"]}>（支持手動組卷，隨機組卷）</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>考試中心</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>線上課</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>線下課</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>學習任務</span>
              <span className={styles["sp"]}>
                （支持指派部門以及獨立學員、支持多階段學習、支持闖關模式、支持關聯證書獎勵）
              </span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>考試任務</span>
              <span className={styles["sp"]}>
                （支持指派部門以及獨立學員、支持試題/選項亂序、支持補考、支持關聯證書獎勵）
              </span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>指派方式</span>
            </div>
            <div className={styles["ex"]}>部門</div>
            <div className={styles["ex2"]}>部門|學員|使用者組</div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>積分激勵</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>積分規則</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>積分調整</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>積分排行</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>數據統計</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>數據概覽</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>學習排行</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>資源統計</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>學員信息導出</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>課程學習導出</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>學員學習導出</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>部門學習導出</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>學習任務統計</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>考試任務統計</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>考生答卷導出</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>防作弊</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>防拖拽</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>防掛機</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>防切屏</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>系統設定</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>網站設置</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>播放設置</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>學員設置</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>單點登入</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>LDAP</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>企業微信</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>釘釘</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>飛書</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>雲之家（金蝶）</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>CAS</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>泛微</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>免費支持</div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>用友</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>免費支持</div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>存儲方案</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>MinIO(私有化)</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>阿里雲oss</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>騰訊雲cos</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>安全保護</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>影片跑馬燈</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>HLS影片加密</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>影片防嗅探下載</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>文檔水印</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>考試水印</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>試題防複製</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>多終端</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>PC獨立網站</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>移動端H5</span>
            </div>
            <div className={styles["ex"]}>
              <img src={ex1Icon} className={styles["pic"]} />
            </div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>版權信息</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>代碼協議檔案</span>
            </div>
            <div className={styles["ex"]}>不可移除</div>
            <div className={styles["ex2"]}>企業授權</div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>頁腳版權連結</span>
            </div>
            <div className={styles["ex"]}>不可移除</div>
            <div className={styles["ex2"]}>企業授權</div>
          </div>
        </div>
        <div className="float-left mt-30">
          <div className={styles["contrast-box2"]}>
            <div className={styles["name"]}>售後服務</div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>安裝部署</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>使用手冊</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div className={styles["contrast-box3"]}>
            <div className={styles["name"]}>
              <strong></strong>
              <span>系統升級</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
          <div
            className={styles["contrast-box3"]}
            style={{ borderRadius: "0px 0px 16px 16px" }}
          >
            <div className={styles["name"]}>
              <strong></strong>
              <span>專屬服務羣</span>
            </div>
            <div className={styles["ex"]}></div>
            <div className={styles["ex2"]}>
              <img src={ex2Icon} className={styles["pic"]} />
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default LicensingPage;
