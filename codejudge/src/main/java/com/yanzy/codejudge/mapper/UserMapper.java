package com.yanzy.codejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzy.codejudge.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
