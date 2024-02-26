package com.gxc.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxc.bo.UpdatedUserBO;
import com.gxc.grace.enums.UserInfoModifyType;
import com.gxc.grace.exception.GraceException;
import com.gxc.grace.result.ResponseStatusEnum;
import com.gxc.grace.result.Sex;
import com.gxc.grace.result.YesOrNo;
import com.gxc.grace.utils.DateUtil;
import com.gxc.grace.utils.DesensitizationUtil;
import com.gxc.grace.utils.IdWorker;

import com.gxc.mapper.UsersMapper;
import com.gxc.pojo.Users;
import com.gxc.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Green写代码
 * @date 2024-01-26 12:27
 */
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private UserService userService;
    @Override
    public Users queryMoblieIsExits(String email) {
        LambdaQueryWrapper<Users> query = new LambdaQueryWrapper<Users>();
        query.eq(email!= null, Users::getMobile, email);
        Users users = usersMapper.selectOne(query);
        return users;
    }

    @Transactional
    @Override
    public Users createUser(String email) {
        Long id = IdWorker.id();
        String userId = id.toString();
        Users user = new Users();
        user.setId(userId);
        user.setMobile(email);
        //脱敏处理 不暴露自己的手机/邮箱
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(email));
        user.setNum("用户：" + DesensitizationUtil.commonDisplay(email));
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setFace("https://www.baidu.com/img/flexible/logo/pc/result.png");
        user.setSex(Sex.secret.type);
        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanNumBeUpdated(YesOrNo.YES.type);
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);
        return user;
    }

    @Override
    public Users getUser(String userId) {
        return usersMapper.selectById(userId);
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        if(type.equals(UserInfoModifyType.NICKNAME.type)){
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<Users>();
            queryWrapper.eq(Users::getNickname, updatedUserBO.getNickname());
            Users users = usersMapper.selectOne(queryWrapper);
            if(users != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        if(type.equals(UserInfoModifyType.NUM.type)){
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<Users>();
            queryWrapper.eq(Users::getNum, updatedUserBO.getNum());
            Users users = usersMapper.selectOne(queryWrapper);
            if(users != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_NUM_ERROR);
            }
        }
        Users user = new Users();
        BeanUtils.copyProperties(updatedUserBO, user);
        int result = usersMapper.updateById(user);
        if(result != 1){
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }
        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
        Users users = new Users();
        BeanUtils.copyProperties(updatedUserBO, users);
        int result = usersMapper.updateById(users);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        return getUser(updatedUserBO.getId());
    }
}
