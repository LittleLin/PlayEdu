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
package xyz.playedu.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {}

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object value) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    public static JsonNode parse(String value) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(value);
    }

    public static <T> T parse(String value, TypeReference<T> typeReference)
            throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(value, typeReference);
    }

    public static boolean isObject(String value) {
        try {
            return parse(value).isObject();
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
