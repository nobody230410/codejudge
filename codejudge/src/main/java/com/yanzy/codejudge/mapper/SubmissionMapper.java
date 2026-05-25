package com.yanzy.codejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzy.codejudge.pojo.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

}
