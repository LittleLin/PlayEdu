import axios, { Axios, AxiosResponse } from "axios";
import { Toast } from "antd-mobile";
import { getToken, clearToken } from "../../utils/index";

const GoLogin = () => {
  clearToken();
  window.location.href = "/login";
};

export class HttpClient {
  axios: Axios;

  constructor(url: string) {
    this.axios = axios.create({
      baseURL: url,
      timeout: 15000,
      withCredentials: false,
      headers: {
        Accept: "application/json",
      },
    });

    //攔截器註冊
    this.axios.interceptors.request.use(
      (config) => {
        const token = getToken();
        token && (config.headers.Authorization = "Bearer " + token);
        return config;
      },
      (err) => {
        return Promise.reject(err);
      }
    );

    this.axios.interceptors.response.use(
      (response: AxiosResponse) => {
        let code = response.data.code; //業務返回代碼
        let msg = response.data.msg; //錯誤消息

        if (code === 0) {
          return Promise.resolve(response);
        } else {
          Toast.show({
            content: msg,
          });
        }
        return Promise.reject(response);
      },
      // 當http的狀態碼非0
      (error) => {
        let status = error.response.status;
        if (status === 401) {
          Toast.show({
            content: "請重新登入",
          });
          GoLogin();
        } else if (status === 404) {
          // 跳轉到404頁面
          GoLogin();
        } else if (status === 403) {
          // 跳轉到無權限頁面
        } else if (status === 500) {
          // 跳轉到500異常頁面
        }
        return Promise.reject(error.response);
      }
    );
  }

  get(url: string, params: object) {
    return new Promise((resolve, reject) => {
      this.axios
        .get(url, {
          params: params,
        })
        .then((res) => {
          resolve(res.data);
        })
        .catch((err) => {
          reject(err.data);
        });
    });
  }

  destroy(url: string) {
    return new Promise((resolve, reject) => {
      this.axios
        .delete(url)
        .then((res) => {
          resolve(res.data);
        })
        .catch((err) => {
          reject(err.data);
        });
    });
  }

  post(url: string, params: object) {
    return new Promise((resolve, reject) => {
      this.axios
        .post(url, params)
        .then((res) => {
          resolve(res.data);
        })
        .catch((err) => {
          reject(err.data);
        });
    });
  }

  put(url: string, params: object) {
    return new Promise((resolve, reject) => {
      this.axios
        .put(url, params)
        .then((res) => {
          resolve(res.data);
        })
        .catch((err) => {
          reject(err.data);
        });
    });
  }

  request(config: object) {
    return new Promise((resolve, reject) => {
      this.axios
        .request(config)
        .then((res) => {
          resolve(res.data);
        })
        .catch((err) => {
          reject(err.data);
        });
    });
  }
}

const APP_URL = import.meta.env.VITE_APP_URL || "";

const client = new HttpClient(APP_URL);

export default client;
