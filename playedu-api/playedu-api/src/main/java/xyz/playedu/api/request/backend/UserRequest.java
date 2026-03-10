/*
 * Copyright (C) 2023 杭州白書科技有限公司
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.playedu.api.request.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author 杭州白書科技有限公司
 *
 * @create 2023/2/23 13:56
 */
@Data
public class UserRequest {

    @NotBlank(message = "請輸入電子郵件")
    @Email(message = "請輸入正確的電子郵件")
    private String email;

    @NotBlank(message = "請輸入姓名")
    @Length(min = 1, max = 20, message = "姓名長度在1-20個字符之間")
    private String name;

    @NotNull(message = "請上傳頭像")
    private Integer avatar;

    @NotNull(message = "password參數不存在")
    private String password;

    @JsonProperty("id_card")
    private String idCard;

    @JsonProperty("dep_ids")
    private Integer[] depIds;
}
