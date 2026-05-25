package com.yanzy.codejudge.dto.resp;

import com.yanzy.codejudge.vo.ProblemInfoVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProblemListResponse {

    private List<ProblemInfoVO> problems;

    public ProblemListResponse(List<ProblemInfoVO> problems) {

        this.problems = (problems == null) ? new ArrayList<>() : new ArrayList<>(problems);

    }
}
