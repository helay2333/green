package com.gxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxc.bo.UpdatedUserBO;
import com.gxc.pojo.Users;
import org.apache.catalina.User;

/** 用户主页
 * @author zzzy0
 */
public interface UserService extends IService<Users> {
    public Users queryMoblieIsExits(String mobile);
    public Users createUser(String email);
    public Users getUser(String userId);
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);
    public Users updateUserInfo(UpdatedUserBO updatedUserBO);


}
