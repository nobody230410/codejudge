package com.yanzy.codejudge.service;

import com.yanzy.codejudge.dto.resp.AdminSubmissionPageResponse;
import com.yanzy.codejudge.vo.AdminSubmissionDetailVO;

public interface AdminSubmissionService {

    AdminSubmissionPageResponse pageSubmissions(Integer userId, String username, Integer problemId, int page, int pageSize);

    AdminSubmissionDetailVO getSubmissionDetail(int submissionId);
}
