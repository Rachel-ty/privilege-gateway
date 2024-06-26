package cn.edu.xmu.privilegegateway.privilegeservice.model.po;

import java.time.LocalDateTime;

public class NewUserPo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.user_name
     *
     * @mbg.generated
     */
    private String userName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.mobile
     *
     * @mbg.generated
     */
    private String mobile;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.email
     *
     * @mbg.generated
     */
    private String email;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.avatar
     *
     * @mbg.generated
     */
    private String avatar;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.open_id
     *
     * @mbg.generated
     */
    private String openId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.depart_id
     *
     * @mbg.generated
     */
    private Long departId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.password
     *
     * @mbg.generated
     */
    private String password;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.creator_id
     *
     * @mbg.generated
     */
    private Long creatorId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.gmt_create
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.modifier_id
     *
     * @mbg.generated
     */
    private Long modifierId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.id_number
     *
     * @mbg.generated
     */
    private String idNumber;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.passport_number
     *
     * @mbg.generated
     */
    private String passportNumber;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.signature
     *
     * @mbg.generated
     */
    private String signature;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.creator_name
     *
     * @mbg.generated
     */
    private String creatorName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_new_user.modifier_name
     *
     * @mbg.generated
     */
    private String modifierName;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.id
     *
     * @return the value of auth_new_user.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.id
     *
     * @param id the value for auth_new_user.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.user_name
     *
     * @return the value of auth_new_user.user_name
     *
     * @mbg.generated
     */
    public String getUserName() {
        return userName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.user_name
     *
     * @param userName the value for auth_new_user.user_name
     *
     * @mbg.generated
     */
    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.mobile
     *
     * @return the value of auth_new_user.mobile
     *
     * @mbg.generated
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.mobile
     *
     * @param mobile the value for auth_new_user.mobile
     *
     * @mbg.generated
     */
    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.email
     *
     * @return the value of auth_new_user.email
     *
     * @mbg.generated
     */
    public String getEmail() {
        return email;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.email
     *
     * @param email the value for auth_new_user.email
     *
     * @mbg.generated
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.name
     *
     * @return the value of auth_new_user.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.name
     *
     * @param name the value for auth_new_user.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.avatar
     *
     * @return the value of auth_new_user.avatar
     *
     * @mbg.generated
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.avatar
     *
     * @param avatar the value for auth_new_user.avatar
     *
     * @mbg.generated
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? null : avatar.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.open_id
     *
     * @return the value of auth_new_user.open_id
     *
     * @mbg.generated
     */
    public String getOpenId() {
        return openId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.open_id
     *
     * @param openId the value for auth_new_user.open_id
     *
     * @mbg.generated
     */
    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.depart_id
     *
     * @return the value of auth_new_user.depart_id
     *
     * @mbg.generated
     */
    public Long getDepartId() {
        return departId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.depart_id
     *
     * @param departId the value for auth_new_user.depart_id
     *
     * @mbg.generated
     */
    public void setDepartId(Long departId) {
        this.departId = departId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.password
     *
     * @return the value of auth_new_user.password
     *
     * @mbg.generated
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.password
     *
     * @param password the value for auth_new_user.password
     *
     * @mbg.generated
     */
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.creator_id
     *
     * @return the value of auth_new_user.creator_id
     *
     * @mbg.generated
     */
    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.creator_id
     *
     * @param creatorId the value for auth_new_user.creator_id
     *
     * @mbg.generated
     */
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.gmt_create
     *
     * @return the value of auth_new_user.gmt_create
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.gmt_create
     *
     * @param gmtCreate the value for auth_new_user.gmt_create
     *
     * @mbg.generated
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.modifier_id
     *
     * @return the value of auth_new_user.modifier_id
     *
     * @mbg.generated
     */
    public Long getModifierId() {
        return modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.modifier_id
     *
     * @param modifierId the value for auth_new_user.modifier_id
     *
     * @mbg.generated
     */
    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.gmt_modified
     *
     * @return the value of auth_new_user.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.gmt_modified
     *
     * @param gmtModified the value for auth_new_user.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.id_number
     *
     * @return the value of auth_new_user.id_number
     *
     * @mbg.generated
     */
    public String getIdNumber() {
        return idNumber;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.id_number
     *
     * @param idNumber the value for auth_new_user.id_number
     *
     * @mbg.generated
     */
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber == null ? null : idNumber.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.passport_number
     *
     * @return the value of auth_new_user.passport_number
     *
     * @mbg.generated
     */
    public String getPassportNumber() {
        return passportNumber;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.passport_number
     *
     * @param passportNumber the value for auth_new_user.passport_number
     *
     * @mbg.generated
     */
    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber == null ? null : passportNumber.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.signature
     *
     * @return the value of auth_new_user.signature
     *
     * @mbg.generated
     */
    public String getSignature() {
        return signature;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.signature
     *
     * @param signature the value for auth_new_user.signature
     *
     * @mbg.generated
     */
    public void setSignature(String signature) {
        this.signature = signature == null ? null : signature.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.creator_name
     *
     * @return the value of auth_new_user.creator_name
     *
     * @mbg.generated
     */
    public String getCreatorName() {
        return creatorName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.creator_name
     *
     * @param creatorName the value for auth_new_user.creator_name
     *
     * @mbg.generated
     */
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName == null ? null : creatorName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_new_user.modifier_name
     *
     * @return the value of auth_new_user.modifier_name
     *
     * @mbg.generated
     */
    public String getModifierName() {
        return modifierName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_new_user.modifier_name
     *
     * @param modifierName the value for auth_new_user.modifier_name
     *
     * @mbg.generated
     */
    public void setModifierName(String modifierName) {
        this.modifierName = modifierName == null ? null : modifierName.trim();
    }
}