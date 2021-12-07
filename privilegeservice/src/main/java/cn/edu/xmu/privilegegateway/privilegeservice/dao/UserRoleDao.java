package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserRolePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserRolePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserRolePoExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao.ROLEKEY;
@Repository
public class UserRoleDao {
    @Autowired
    private UserRolePoMapper userRolePoMapper;

    public ReturnObject<List<UserRolePo>> selectByRoleId(Long roleId){
        try{
            Set<String> resultSet=new HashSet<String>();
            UserRolePoExample example1=new UserRolePoExample();
            resultSet.add(String.format(ROLEKEY,roleId));
            UserRolePoExample.Criteria criteria1=example1.createCriteria();
            criteria1.andRoleIdEqualTo(roleId);
            List<UserRolePo> gList=userRolePoMapper.selectByExample(example1);
            return new ReturnObject<>(gList);
        }catch (Exception e){
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }
    /*
    author:张晖婧
     */
    public ReturnObject selectByUserId(Long userId){
        try{
            UserRolePoExample example = new UserRolePoExample();
            UserRolePoExample.Criteria criteria = example.createCriteria();
            criteria.andUserIdEqualTo(userId);

            List<UserRolePo> gList=userRolePoMapper.selectByExample(example);
            return new ReturnObject<>(gList);
        }catch (Exception e){
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }
    /*
    author:张晖婧
     */
    public int deleteByExample(Long userId,Long roleId){
        UserRolePoExample example = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = example.createCriteria();
        if(userId!=null)
            criteria.andUserIdEqualTo(userId);
        if(roleId!=null)
            criteria.andRoleIdEqualTo(roleId);

        int ret=userRolePoMapper.deleteByExample(example);
        return ret;
    }
    public int insertUserRolePo(UserRolePo userRolePo){
        return userRolePoMapper.insertSelective(userRolePo);
    }
}
