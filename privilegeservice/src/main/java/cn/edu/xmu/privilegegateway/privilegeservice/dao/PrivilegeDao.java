/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.PrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限DAO
 * @author Ming Qiu
 **/
@Repository
public class PrivilegeDao implements InitializingBean {

    private  static  final Logger logger = LoggerFactory.getLogger(PrivilegeDao.class);


    @Autowired
    private PrivilegePoMapper poMapper;
    @Autowired
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Autowired
    private RoleDao roleDao;


    /**
     * 将权限载入到本地缓存中
     * 如果未初始化，则初始话数据中的数据
     * @throws Exception
     * createdBy: Ming Qiu 2020-11-01 23:44
     * modifiedBy: Ming Qiu 2020-11-03 11:44
     *            将签名的认证改到Privilege对象中去完成
     *            Ming Qiu 2020-12-03 9:44
     *            将缓存放到redis中
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        PrivilegePoExample example = new PrivilegePoExample();
        List<PrivilegePo> privilegePos = poMapper.selectByExample(example);
        for (PrivilegePo po : privilegePos){
            Privilege priv = new Privilege(po);
            if (priv.authetic()) {
                logger.debug("afterPropertiesSet: key = " + priv.getKey() + " p = " + priv);
                redisTemplate.opsForHash().putIfAbsent("Priv", priv.getKey(), priv.getId());
            }else{
                logger.debug("afterPropertiesSet: id = " + priv.getId()+ ",Sign = "+priv.getSignature()+". cacuSign="+priv.getCacuSignature());
                logger.error("afterPropertiesSet: Wrong Signature(auth_privilege): id = " + priv.getId());
            }
        }
    }

    public void initialize() {
        PrivilegePoExample example = new PrivilegePoExample();
        List<PrivilegePo> privilegePos = poMapper.selectByExample(example);
        for (PrivilegePo po : privilegePos) {
            if (null == po.getSignature()) {
                Privilege priv = new Privilege(po);
                PrivilegePo newPo = new PrivilegePo();
                newPo.setId(po.getId());
                newPo.setSignature(priv.getCacuSignature());
                logger.debug("initialize: id = " + newPo.getId() + ",Sign = " + newPo.getSignature());
                poMapper.updateByPrimaryKeySelective(newPo);
            }
        }
    }

    /**
     * 以url和RequestType获得缓存的Privilege id
     * @param url: 访问链接
     * @param requestType: 访问类型
     * @return id Privilege id
     * createdBy: Ming Qiu 2020-11-01 23:44
     */
    public Long getPrivIdByKey(String url, Privilege.RequestType requestType){
        StringBuffer key = new StringBuffer(url);
        key.append("-");
        key.append(requestType.getCode());
        logger.info("getPrivIdByKey: key = "+key.toString());
        return (Long) this.redisTemplate.opsForHash().get("Priv", key.toString());
    }

    /**
     * 根据权限Id查询权限
     * @author yue hao
     * @param id 权限ID
     * @return 权限
     */
    public Privilege findPriv(Long id){
        PrivilegePo po = poMapper.selectByPrimaryKey(id);
        Privilege priv = new Privilege(po);
        if (priv.authetic()) {
            return priv;
        }
        else {
            logger.error("findPriv: Wrong Signature(auth_privilege): id =" + po.getId());
            return null;
        }
    }
    /**
     * 查询所有权限
     * @param page: 页码
     * @param pageSize : 每页数量
     * @return 权限列表
     */
    public ReturnObject<PageInfo<VoObject>> findAllPrivs(Integer page, Integer pageSize){
        PrivilegePoExample example = new PrivilegePoExample();
        PrivilegePoExample.Criteria criteria = example.createCriteria();
        PageHelper.startPage(page, pageSize);
        List<PrivilegePo> privilegePos = null;
        try {
            privilegePos = poMapper.selectByExample(example);
        }catch (DataAccessException e){
            logger.error("findAllPrivs: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }

        List<VoObject> ret = new ArrayList<>(privilegePos.size());
        for (PrivilegePo po : privilegePos) {
            Privilege priv = new Privilege(po);
            if (priv.authetic()) {
                logger.debug("findAllPrivs: key = " + priv.getKey() + " p = " + priv);
                ret.add(priv);
            }else{
                logger.error("findAllPrivs: 信息签名错误：id = "+po.getId());
            }

        }
        PageInfo<PrivilegePo> privPoPage = PageInfo.of(privilegePos);
        PageInfo<VoObject> privPage = new PageInfo<>(ret);
        privPage.setPages(privPoPage.getPages());
        privPage.setPageNum(privPoPage.getPageNum());
        privPage.setPageSize(privPoPage.getPageSize());
        privPage.setTotal(privPoPage.getTotal());
        return new ReturnObject<>(privPage);
    }

    /**
     * 修改权限
     * @modifiedBy 24320182203266
     * @param id: 权限id
     * @return ReturnObject
     */
    public ReturnObject changePriv(Long id, PrivilegeVo vo){
        PrivilegePo po = this.poMapper.selectByPrimaryKey(id);
        logger.debug("changePriv: vo = "+ vo  + " po = "+ po);
        /* 验证权限是否被篡改 */
        Privilege privilege = new Privilege(po);
        if(!privilege.getCacuSignature().equals(privilege.getSignature())){
            return new ReturnObject(ReturnNo.RESOURCE_FALSIFY, "该权限可能被篡改，请联系管理员处理");
        }
        /* 验证数据是否重复 */
        PrivilegePoExample example = new PrivilegePoExample();
        PrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andRequestTypeEqualTo(vo.getRequestType()).andUrlEqualTo(vo.getUrl());

        if(!poMapper.selectByExample(example).isEmpty()){
            return new ReturnObject(ReturnNo.URL_SAME, "URL和RequestType不得与已有的数据重复");
        }
        /* 开始更新 */
        PrivilegePo newPo = privilege.createUpdatePo(vo);
        try{
            newPo.setId(po.getId()); // 这里设置要更新的权限的Id
        } catch (Exception e) {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }

        this.poMapper.updateByPrimaryKeySelective(newPo);
        return new ReturnObject();
    }

    /**
     * 权限的影响力分析
     * 任务3-7
     * 删除和禁用某个权限时，删除所以影响的role，group和user的redisKey
     * @author zihan zhou 19720192203768
     * @param privId 权限id
     * @return 影响的role，group和user的redisKey
     */
    public List<String> privilegeImpact(Long privId){
        List<Long> roleIdList =findRoleId(privId);
        List<String>  resultList=new ArrayList<>();
        for (Long roleId : roleIdList) {
            List<String> roleImpact=roleDao.roleImpact(roleId);
            for (String s : roleImpact) {
                if(!resultList.contains(s)){
                    resultList.add(s);
                }
            }
        }
        return resultList;
    }

    public List<Long> findRoleId(Long privId) {
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andPrivilegeIdEqualTo(privId);
        List<RolePrivilegePo> gList = rolePrivilegePoMapper.selectByExample(example);
        List<Long> resultList = new ArrayList<>();
        for (RolePrivilegePo po : gList) {
            resultList.add(po.getRoleId());
        }
        return resultList;
    }

}
