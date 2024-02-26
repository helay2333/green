package com.gxc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxc.bo.CommentBO;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.YesOrNo;
import com.gxc.grace.utils.IdWorker;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.mapper.CommentMapper;
import com.gxc.mapper.CommentMapperCustom;
import com.gxc.pojo.Comment;
import com.gxc.service.CommentService;
import com.gxc.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gxc.grace.utils.Base.*;

/**
 * @author Green写代码
 * @date 2024-01-28 00:06
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private RedisOperator redisOperator;

    @Override
    public CommentVO createComment(CommentBO commentBO) {

        String commentId = String.valueOf(IdWorker.id());

        Comment comment = new Comment();
        comment.setId(commentId);

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());
        // redis操作放在service中，评论总数的累加
        redisOperator.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
        //留言后的最新评论需要返回给前端进行展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment,commentVO);
        commentMapper.insert(comment);
        return commentVO;
    }
    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Override
    public GraceJSONResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize) {


        Page<CommentVO>pageInfo = new Page<CommentVO>(page, pageSize);
        List<CommentVO> list = commentMapperCustom.getCommentList(vlogId);

        for (CommentVO cv:list) {
            String commentId = cv.getCommentId();

            // 当前短视频的某个评论的点赞总数
            String countsStr = redisOperator.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);

            // 判断当前用户是否点赞过该评论
            String doILike = redisOperator.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }
        pageInfo.setRecords(list);

        return GraceJSONResult.ok(pageInfo);
    }

    @Override
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId) {

        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);

        commentMapper.deleteById(pendingDelete);

        // 评论总数的累减
        redisOperator.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectById(id);
    }
}
