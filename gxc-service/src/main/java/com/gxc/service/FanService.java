package com.gxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxc.pojo.Fans;

public interface FanService extends IService<Fans> {
    void doFollow(String myId, String vlogerId);

    void doCancel(String myId, String vlogerId);

    boolean queryDoIFollowVloger(String myId, String vlogerId);

    Object queryMyFollows(String myId, Integer page, Integer pageSize);

    Object queryMyFans(String myId, Integer page, Integer pageSize);
}
