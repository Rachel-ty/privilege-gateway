package cn.edu.xmu.privilegegateway.privilegeservice.mapper;

import cn.edu.xmu.privilegegateway.privilegeservice.model.po.GroupRelationPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.GroupRelationPoExample;
import java.util.List;

public interface GroupRelationPoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    int insert(GroupRelationPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    int insertSelective(GroupRelationPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    List<GroupRelationPo> selectByExample(GroupRelationPoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    GroupRelationPo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(GroupRelationPo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table auth_group_relation
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(GroupRelationPo record);
}