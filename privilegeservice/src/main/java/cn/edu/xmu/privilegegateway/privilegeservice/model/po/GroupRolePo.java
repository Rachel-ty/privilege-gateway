package cn.edu.xmu.privilegegateway.privilegeservice.model.po;

import java.time.LocalDateTime;

public class GroupRolePo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.role_id
     *
     * @mbg.generated
     */
    private Long roleId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.group_id
     *
     * @mbg.generated
     */
    private Long groupId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.signature
     *
     * @mbg.generated
     */
    private String signature;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.creator_id
     *
     * @mbg.generated
     */
    private Long creatorId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.gmt_create
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.modifier_id
     *
     * @mbg.generated
     */
    private Long modifierId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.creator_name
     *
     * @mbg.generated
     */
    private String creatorName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_group_role.modifier_name
     *
     * @mbg.generated
     */
    private String modifierName;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.id
     *
     * @return the value of auth_group_role.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.id
     *
     * @param id the value for auth_group_role.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.role_id
     *
     * @return the value of auth_group_role.role_id
     *
     * @mbg.generated
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.role_id
     *
     * @param roleId the value for auth_group_role.role_id
     *
     * @mbg.generated
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.group_id
     *
     * @return the value of auth_group_role.group_id
     *
     * @mbg.generated
     */
    public Long getGroupId() {
        return groupId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.group_id
     *
     * @param groupId the value for auth_group_role.group_id
     *
     * @mbg.generated
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.signature
     *
     * @return the value of auth_group_role.signature
     *
     * @mbg.generated
     */
    public String getSignature() {
        return signature;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.signature
     *
     * @param signature the value for auth_group_role.signature
     *
     * @mbg.generated
     */
    public void setSignature(String signature) {
        this.signature = signature == null ? null : signature.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.creator_id
     *
     * @return the value of auth_group_role.creator_id
     *
     * @mbg.generated
     */
    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.creator_id
     *
     * @param creatorId the value for auth_group_role.creator_id
     *
     * @mbg.generated
     */
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.gmt_create
     *
     * @return the value of auth_group_role.gmt_create
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.gmt_create
     *
     * @param gmtCreate the value for auth_group_role.gmt_create
     *
     * @mbg.generated
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.modifier_id
     *
     * @return the value of auth_group_role.modifier_id
     *
     * @mbg.generated
     */
    public Long getModifierId() {
        return modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.modifier_id
     *
     * @param modifierId the value for auth_group_role.modifier_id
     *
     * @mbg.generated
     */
    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.gmt_modified
     *
     * @return the value of auth_group_role.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.gmt_modified
     *
     * @param gmtModified the value for auth_group_role.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.creator_name
     *
     * @return the value of auth_group_role.creator_name
     *
     * @mbg.generated
     */
    public String getCreatorName() {
        return creatorName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.creator_name
     *
     * @param creatorName the value for auth_group_role.creator_name
     *
     * @mbg.generated
     */
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName == null ? null : creatorName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_group_role.modifier_name
     *
     * @return the value of auth_group_role.modifier_name
     *
     * @mbg.generated
     */
    public String getModifierName() {
        return modifierName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_group_role.modifier_name
     *
     * @param modifierName the value for auth_group_role.modifier_name
     *
     * @mbg.generated
     */
    public void setModifierName(String modifierName) {
        this.modifierName = modifierName == null ? null : modifierName.trim();
    }
}