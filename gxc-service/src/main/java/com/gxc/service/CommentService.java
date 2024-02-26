package com.gxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxc.bo.CommentBO;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.pojo.Comment;
import com.gxc.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    public CommentVO createComment(CommentBO commentBO);
    public GraceJSONResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize);
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId);
    public Comment getComment(String id);
}
