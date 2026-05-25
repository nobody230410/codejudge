package com.yanzy.codejudge.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Envoy {@code POST /run} 响应（与契约一致）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeRunResponse {

    private String output;

    private String status;

    private Double time;

    /** KB；Envoy 可能以整数或小数返回 */
    private Double memory;
}
