package cn.edu.xmu.privilegegateway.privilegeservice.mapper;

import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import java.util.List;

public interface RolePrivilegePoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int deleteByExample(RolePrivilegePoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int insert(RolePrivilegePo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int insertSelective(RolePrivilegePo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    List<RolePrivilegePo> selectByExample(RolePrivilegePoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    RolePrivilegePo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(RolePrivilegePo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_role_privilege
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(RolePrivilegePo record);
}
