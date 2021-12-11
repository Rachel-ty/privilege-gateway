package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.GroupDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Group;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.GroupRelation;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserGroupPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class GroupService {
    private Collection<String> codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
    private List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile", "state", "departId", "level"));

    @Autowired
    private BaseCoder aesCoder;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private UserDao userDao;


    /**
     * 新增用户组
     * createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertGroup(GroupVo vo, Long did, Long loginUserId, String loginUserName) {
        Group group = (Group) Common.cloneVo(vo, Group.class);
        group.setState(Group.State.NORMAL.getCode().byteValue());
        group.setDepartId(did);
        ReturnObject<PageInfo<RetGroup>> ret = groupDao.getGroupsBydid(group.getDepartId(), 1, 10);

        ReturnObject<RetGroup> retObj = groupDao.insertGroup(group, loginUserId, loginUserName);
        return retObj;
    }

    /**
     * 查询某一部门的所有用户组
     * createdBy:  Weining Shi
     */
    public ReturnObject getAllgroups(Long did, Integer page, Integer pageSize) {
        ReturnObject<PageInfo<RetGroup>> ret = groupDao.getGroupsBydid(did, page, pageSize);
        return ret;
    }

    /**
     * 修改用户组的信息
     * createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject changeGroup(Long did, Long id, Group group, Long loginUserId, String loginUserName) {
        ReturnObject<PageInfo<RetGroup>> ret = groupDao.getGroupsBydid(did, 1, 10);
        return groupDao.updateGroup(id, group, loginUserId, loginUserName);
    }

    /**
     * 获得所有用户组状态
     *
     * @return createdBy:  Weining Shi
     */

    public ReturnObject getAllStates() {
        return groupDao.getAllStates();
    }

    /**
     * 删除用户组 删除用户组的角色，删除用户组的父子关系
     *
     * @param did
     * @param id
     * @return createdBy:  Weining Shi
     */

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteGroup(Long did, Long id) {
        ReturnObject<Group> retGroup = groupDao.getGroupByid(id);
        if (retGroup.getData() == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "该用户组不存在");
        }

        return groupDao.deleteGroup(id);
    }


    /**
     * 增加用户组pid的子用户组sid
     *
     * @param did
     * @param pid
     * @param sid
     * @param userId
     * @param userName
     * @return createdBy:  Weining Shi
     */

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertGroupRelation(Long did, Long pid, Long sid, Long userId, String userName) {
        GroupRelation groupRelation = new GroupRelation();


        groupRelation.setGroupPId(pid);
        groupRelation.setGroupSId(sid);

        ReturnObject<List<GroupRelation>> bos = groupDao.getGroupRelationBypidsid(pid, sid);
        ReturnObject<List<GroupRelation>> bos1 = groupDao.getGroupRelationBypidsid(sid, pid);

        if (!(bos.getData().isEmpty()) || !(bos1.getData().isEmpty()))
            return new ReturnObject(ReturnNo.GROUP_EXIST, "该pid与sid的父子关系已经存在");

        ReturnObject<GroupRelationVo> retObj = groupDao.addGroupRelation(groupRelation, userId, userName);
        return retObj;
    }

    /**
     * 取消用户组pid和用户组sid的父子关系
     *
     * @param did
     * @param pid
     * @param sid
     * @param userId
     * @param userName
     * @return createdBy:  Weining Shi
     */

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteGroupRelation(Long did, Long pid, Long sid, Long userId, String userName) {
        GroupRelation groupRelation = new GroupRelation();


        ReturnObject<List<GroupRelation>> pos = groupDao.findGroupRelationBypidsid(sid, pid);
        if ((pos.getData().isEmpty()))
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "该pid与sid的父子关系不存在");
        groupRelation = (GroupRelation) Common.cloneVo(pos.getData().get(0), GroupRelation.class);

        ReturnObject retObj = groupDao.deleteGroupRelation(groupRelation.getId(), userId, userName);
        return retObj;
    }

    /**
     * 获得用户组id的子用户组
     *
     * @param did
     * @param id
     * @param page
     * @param pageSize
     * @return createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject getsubGroup(Long did, Long id, Integer page, Integer pageSize) {
        ReturnObject<List<GroupRelation>> bos = groupDao.getGroupRelationBypidsid(null, id);//获得所有父id为id的关系记录
        if (bos.getCode().equals(ReturnNo.RESOURCE_FALSIFY.getCode()))
            return (ReturnObject) Common.decorateReturnObject(bos);
        if (bos.getData().isEmpty())
            return new ReturnObject(ReturnNo.OK);
        List<Long> subids = new ArrayList<Long>();
        for (GroupRelation groupRelation : bos.getData()) {
            Long subid = groupRelation.getGroupSId();
            subids.add(subid);
        }

        PageInfo<GroupRelationVo> groupPageInfo = new PageInfo<>();
        List<GroupRelationVo> groupRelationVos = new ArrayList<>();

        for (Long subid : subids) {
            Group group = groupDao.getGroupByid(subid).getData();
            if (group != null) {
                GroupRelationVo temp = (GroupRelationVo) Common.cloneVo(group, GroupRelationVo.class);
                temp.setSign((byte) 0);
                groupRelationVos.add(temp);
            }
        }

        if (groupRelationVos.size() == 0)//无结果
            return new ReturnObject(ReturnNo.OK);
        groupPageInfo.setList(groupRelationVos);
        ReturnObject<PageInfo<GroupRelationVo>> ret = new ReturnObject(groupPageInfo);
        return ret;
    }

    /**
     * 获得用户组id的父用户组
     *
     * @param did
     * @param id
     * @param page
     * @param pageSize
     * @return createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject getparGroup(Long did, Long id, Integer page, Integer pageSize) {
        ReturnObject<List<GroupRelation>> pos = groupDao.getGroupRelationBypidsid(id, null);//获得所有父id为id的关系记录
        if (pos.getCode().equals(ReturnNo.RESOURCE_FALSIFY.getCode()))
            return (ReturnObject) Common.decorateReturnObject(pos);
        if (pos.getData().isEmpty())
            return new ReturnObject(ReturnNo.OK);
        List<Long> subids = new ArrayList<Long>();
        for (GroupRelation groupRelation : pos.getData()) {
            Long subid = groupRelation.getId();
            subids.add(subid);
        }

        PageInfo<Group> groupPageInfo = new PageInfo<Group>();
        List<Group> groups = new ArrayList<>();

        for (Long subid : subids) {
            Group temp = groupDao.getGroupByid(subid).getData();
            if (temp != null)
                groups.add(temp);
        }

        if (groups.size() == 0)//无结果
            return new ReturnObject(ReturnNo.OK);
        groupPageInfo.setList(groups);
        ReturnObject<PageInfo<Group>> ret = new ReturnObject<>(groupPageInfo);
        return ret;
    }

    /**
     * 将用户加入组
     *
     * @param uid
     * @param id
     * @param did
     * @param userId
     * @param userName
     * @return createdBy:  Weining Shi
     */

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertUserGroup(Long uid, Long id, Long did, Long userId, String userName) {
        UserGroup userGroup = new UserGroup();

        ReturnObject<Group> retGroup = groupDao.getGroupByid(id);
        if (retGroup.getData() == null)
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "该组不存在");
        Group group = retGroup.getData();
        if (!did.equals(group.getDepartId()))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);

        UserPo retUser = userDao.findUserById(uid);

        if (retUser == null)
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "该用户不存在");
        if (!did.equals(retUser.getDepartId()))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);

        ReturnObject<List<Pair<UserGroupPo, Byte>>> pos = groupDao.getUserGroupByuidgid(uid, id);

        if (!(pos.getData().isEmpty()))
            return new ReturnObject(ReturnNo.OK);
        userGroup.setGroupId(id);
        userGroup.setUserId(uid);
        ReturnObject<UserGroup> retObj = groupDao.insertUserGroup(userGroup, userId, userName);
        return retObj;
    }

    /**
     * 将用户删除组
     *
     * @param uid
     * @param id
     * @param did
     * @param userId
     * @param userName
     * @return createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteUserGroup(Long uid, Long id, Long did, Long userId, String userName) {
        UserGroupPo userGroupPo = new UserGroupPo();

        ReturnObject<Group> retGroup = groupDao.getGroupByid(id);
        Group group = retGroup.getData();
        if (group!=null && !did.equals(group.getDepartId()))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        UserPo retUser = userDao.findUserById(uid);
        if (retUser!=null && !did.equals(retUser.getDepartId()))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        ReturnObject<List<Pair<UserGroupPo, Byte>>> pos = groupDao.getUserGroupByuidgid(uid, id);
        if (pos.getData().isEmpty())
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "该用户不在该用户组内");
        userGroupPo = pos.getData().get(0).getFirst();
        ReturnObject retObj = groupDao.deleteUserGroup(userGroupPo.getId(), userId, userName);
        return retObj;
    }


    /**
     * 禁用用户组
     *
     * @param did
     * @param id
     * @param loginUser
     * @param loginUsername
     * @return createdBy:  Weining Shi
     */

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject forbidGroup(Long did, Long id, Long loginUser, String loginUsername) {
        ReturnObject<Group> retGroup = groupDao.getGroupByid(id);
        if (retGroup.getData() == null)
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        Group group = retGroup.getData();
        if (!group.getDepartId().equals(did))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        group.setState(Group.State.FORBID.getCode().byteValue());

        return groupDao.updateGroup(id, group, loginUser, loginUsername);
    }

    /**
     * 解禁用户组
     *
     * @param did
     * @param id
     * @param loginUser
     * @param loginUsername
     * @return createdBy:  Weining Shi
     */


    @Transactional(rollbackFor = Exception.class)
    public ReturnObject releaseGroup(Long did, Long id, Long loginUser, String loginUsername) {

        ReturnObject<Group> retGroup = groupDao.getGroupByid(id);

        if (retGroup.getData() == null)
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);

        Group group = retGroup.getData();
        if (!group.getDepartId().equals(did))
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        group.setState(Group.State.NORMAL.getCode().byteValue());


        return groupDao.updateGroup(id, group, loginUser, loginUsername);
    }


    /**
     * 获得组里的所有用户
     *
     * @param did
     * @param id
     * @param page
     * @param pageSize
     * @return createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject getgroupsuser(Long did, Long id, Integer page, Integer pageSize) {

        ReturnObject<PageInfo<Object>> ret = groupDao.getusersBygid(did, id, page, pageSize);
        return ret;
    }

    /**
     * 获得用户的组
     *
     * @param did
     * @param id
     * @param page
     * @param pageSize
     * @return createdBy:  Weining Shi
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject getusersgroup(Long did, Long id, Integer page, Integer pageSize) {
        List<UserGroup> ret = groupDao.getUserGroupByUserId(id, page, pageSize);
        List<GroupRelationVo> vos = new ArrayList<>();
        for (UserGroup it : ret) {
            ReturnObject<Group> retgroup = groupDao.getGroupByid(it.getGroupId());
            Group group;
            if (retgroup.getData() == null)
                continue;
            else
                group = retgroup.getData();
            GroupRelationVo temp = (GroupRelationVo) Common.cloneVo(group, GroupRelationVo.class);
            temp.setSign((byte) 0);
            vos.add(temp);
        }
        if (vos == null)
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        else {
            PageInfo<GroupRelationVo> retdata = new PageInfo<>(vos);
            return new ReturnObject(retdata);
        }

    }
}


