package com.yanzy.codejudge.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TestCase {

    private Integer id;

    private Integer problemId;

    private String input;

    private String output;

    private String inputHash;

}
