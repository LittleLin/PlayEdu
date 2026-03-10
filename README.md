# PlayEdu

這個 repo 是 PlayEdu 2.0 的可本地開發版本，包含：

- `playedu-admin`：管理後台
- `playedu-pc`：PC 學員端
- `playedu-h5`：H5 學員端
- `playedu-api`：Spring Boot API
- `compose.yml`：整套 Docker 啟動方式

預設對外端口：

- 管理後台：`http://localhost:9900`
- PC 學員端：`http://localhost:9800`
- H5 學員端：`http://localhost:9801`
- API：`http://localhost:9700`

說明：

- `9700` 是後端 API 端口，不是前端頁面
- 直接打開 `http://localhost:9700`，正常會看到 `系統運作中...`
- 前端頁面請使用 `9900`、`9800`、`9801`

## 環境需求

如果你要用 Docker 啟動整套服務：

- Docker
- Docker Compose

如果你要本地分開開發前後端：

- Node.js 20
- `pnpm`
- Java 17
- Maven 或專案內 `./mvnw`
- MySQL 8

## 先準備環境變數

專案不再內建可直接使用的密碼與 JWT secret。啟動前先建立 `.env`：

```bash
cp .env.example .env
```

然後編輯 `.env`，至少填這兩個值：

```env
PLAYEDU_DB_PASSWORD=換成你的強密碼
PLAYEDU_AUTH_JWT_SECRET_KEY=換成至少32字元的隨機字串
```

`.env.example` 也包含可調整的 port 設定。

## 用 Docker 啟動整套服務

第一次或有 Dockerfile / 前後端 / API 變更時：

```bash
docker compose up -d --build
```

之後一般重啟：

```bash
docker compose up -d
```

查看服務狀態：

```bash
docker compose ps
```

查看日誌：

```bash
docker compose logs -f
```

停止服務：

```bash
docker compose down
```

如果要連資料庫，預設是：

- Host: `127.0.0.1`
- Port: `23307`
- Database: `playedu`
- Username: `root`
- Password: `.env` 內的 `PLAYEDU_DB_PASSWORD`

## 本地開發

### 1. 啟動 MySQL

你可以自行準備 MySQL 8，或只啟動 repo 內的 MySQL：

```bash
docker compose up -d mysql
```

### 2. 啟動 API

進入 API 專案：

```bash
cd playedu-api
```

編譯：

```bash
./mvnw -Dmaven.test.skip=true compile
```

啟動：

```bash
PLAYEDU_AUTH_JWT_SECRET_KEY=你的32字元以上secret \
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:mysql://127.0.0.1:23307/playedu?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true --spring.datasource.username=root --spring.datasource.password=你的資料庫密碼"
```

### 3. 啟動前端

管理後台：

```bash
cd playedu-admin
corepack enable
pnpm i
pnpm dev
```

PC 學員端：

```bash
cd playedu-pc
corepack enable
pnpm i
pnpm dev
```

H5 學員端：

```bash
cd playedu-h5
corepack enable
pnpm i
pnpm dev
```

如果需要讓前端 API 指向本地後端，請依各前端專案的 Vite 設定調整 `VITE_APP_URL`。

## 常用指令

重建整套 Docker 服務：

```bash
docker compose up -d --build --force-recreate
```

只看 API 日誌：

```bash
docker compose logs -f playedu
```

只看 MySQL 日誌：

```bash
docker compose logs -f mysql
```

API 健康檢查：

```bash
curl http://localhost:9700
```

## 安全提醒

- 不要把 `.env` 提交到 git
- `PLAYEDU_AUTH_JWT_SECRET_KEY` 請使用高強度隨機字串
- 如果這套服務會對外，請進一步收斂 CORS、升級密碼雜湊策略、避免把 token 存在 `localStorage`

## 目前已知狀態

這個 repo 目前已完成：

- 移除 aliyuncs image 依賴
- 介面文案改為繁體中文
- 移除 `hutool`
- 移除 `sa-token`，改用 JWT

如果你只是要快速跑起來，優先照「先準備環境變數」和「用 Docker 啟動整套服務」兩段操作即可。
