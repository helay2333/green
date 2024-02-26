package com.gxc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxc.pojo.Fans;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface FansMapper extends BaseMapper<Fans> {
}