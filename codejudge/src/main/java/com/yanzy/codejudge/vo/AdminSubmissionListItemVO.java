package com.yanzy.codejudge.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubmissionListItemVO {

    private int id;
    private int userId;
    private String username;
    private int problemId;
    private String problemTitle;
    private String language;
    private String status;
    private LocalDateTime submitTime;
}
