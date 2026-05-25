package com.yanzy.codejudge.dto.resp;

import com.yanzy.codejudge.vo.AdminSubmissionListItemVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubmissionPageResponse {

    private List<AdminSubmissionListItemVO> records;
    private long total;
    private int page;
    private int pageSize;
}
