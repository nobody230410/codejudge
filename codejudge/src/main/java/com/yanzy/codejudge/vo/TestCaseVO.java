package com.yanzy.codejudge.vo;

import lombok.Data;

@Data
public class TestCaseVO {

    /** 已有测试用例 id，新建时可不传 */
    private Integer id;

    private int problemId;

    private String input;

    private String output;

}
