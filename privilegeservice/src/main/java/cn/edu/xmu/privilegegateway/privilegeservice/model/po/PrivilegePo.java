package cn.edu.xmu.privilegegateway.privilegeservice.model.po;

import java.time.LocalDateTime;

public class PrivilegePo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.url
     *
     * @mbg.generated
     */
    private String url;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.request_type
     *
     * @mbg.generated
     */
    private Byte requestType;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.signature
     *
     * @mbg.generated
     */
    private String signature;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.creator_id
     *
     * @mbg.generated
     */
    private Long creatorId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.gmt_create
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.modifier_id
     *
     * @mbg.generated
     */
    private Long modifierId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column auth_privilege.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.id
     *
     * @return the value of auth_privilege.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.id
     *
     * @param id the value for auth_privilege.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.name
     *
     * @return the value of auth_privilege.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.name
     *
     * @param name the value for auth_privilege.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.url
     *
     * @return the value of auth_privilege.url
     *
     * @mbg.generated
     */
    public String getUrl() {
        return url;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.url
     *
     * @param url the value for auth_privilege.url
     *
     * @mbg.generated
     */
    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.request_type
     *
     * @return the value of auth_privilege.request_type
     *
     * @mbg.generated
     */
    public Byte getRequestType() {
        return requestType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.request_type
     *
     * @param requestType the value for auth_privilege.request_type
     *
     * @mbg.generated
     */
    public void setRequestType(Byte requestType) {
        this.requestType = requestType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.signature
     *
     * @return the value of auth_privilege.signature
     *
     * @mbg.generated
     */
    public String getSignature() {
        return signature;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.signature
     *
     * @param signature the value for auth_privilege.signature
     *
     * @mbg.generated
     */
    public void setSignature(String signature) {
        this.signature = signature == null ? null : signature.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.creator_id
     *
     * @return the value of auth_privilege.creator_id
     *
     * @mbg.generated
     */
    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.creator_id
     *
     * @param creatorId the value for auth_privilege.creator_id
     *
     * @mbg.generated
     */
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.gmt_create
     *
     * @return the value of auth_privilege.gmt_create
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.gmt_create
     *
     * @param gmtCreate the value for auth_privilege.gmt_create
     *
     * @mbg.generated
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.modifier_id
     *
     * @return the value of auth_privilege.modifier_id
     *
     * @mbg.generated
     */
    public Long getModifierId() {
        return modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.modifier_id
     *
     * @param modifierId the value for auth_privilege.modifier_id
     *
     * @mbg.generated
     */
    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column auth_privilege.gmt_modified
     *
     * @return the value of auth_privilege.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column auth_privilege.gmt_modified
     *
     * @param gmtModified the value for auth_privilege.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }
}