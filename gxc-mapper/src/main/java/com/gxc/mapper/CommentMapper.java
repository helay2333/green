package com.gxc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxc.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}