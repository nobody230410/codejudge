package com.yanzy.codejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzy.codejudge.pojo.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {

    void batchInsert(@Param("testCases") List<TestCase> testCases);

}
