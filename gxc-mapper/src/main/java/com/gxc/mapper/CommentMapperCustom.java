package com.gxc.mapper;

import com.gxc.vo.CommentVO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {

    public List<CommentVO> getCommentList(String id);

}