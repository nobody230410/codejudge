package com.yanzy.codejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzy.codejudge.pojo.Problem;
import com.yanzy.codejudge.vo.ProblemInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {

    List<ProblemInfoVO> getProblemList();

}
