/* 与3-6和3-5的测试数据一样,如果已经导入3-6和3-5的测试数据,就不用再导入这个
 */
INSERT INTO privilege_gateway.auth_group_relation (id, group_p_id, group_s_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (1, 10, 11, '', null, '2021-12-02 19:48:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_group_relation (id, group_p_id, group_s_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (2, 10, 12, '', null, '2021-12-02 19:48:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_group_relation (id, group_p_id, group_s_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (3, 11, 13, '', null, '2021-12-02 19:59:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_user_group (id, user_id, group_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (1, 51, 10, '', null, '2021-12-02 19:49:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_user_group (id, user_id, group_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (2, 60, 11, '', null, '2021-12-02 19:49:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_user_group (id, user_id, group_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (3, 49, 12, '', null, '2021-12-02 19:49:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_user_group (id, user_id, group_id, signature, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name) VALUES (4, 51, 13, '', null, '2021-12-02 19:49:00', null, null, null, null);
INSERT INTO privilege_gateway.auth_user_proxy (id, user_id, proxy_user_id, begin_date, end_date, signature, valid, depart_id, creator_id, gmt_create, modifier_id, gmt_modified, creator_name, modifier_name, user_name, proxy_user_name) VALUES (6, 60, 51, '2020-05-03 18:54:00', '2020-07-03 18:54:00', '7b6bef43e290a29c964a4d5bad7208309ca4b583ae029579d2ce3b5a70e5c6ec', 1, 0, null, '2020-11-03 18:54:00', null, null, null, null, null, null);
INSERT INTO `privilege_gateway`.`auth_user_proxy` (`id`, `user_id`, `proxy_user_id`, `begin_date`, `end_date`, `valid`, `depart_id`, `gmt_create`) VALUES ('7', '60', '49', '2020-05-03 18:54:00', '2020-07-03 18:54:00', '1', '0', '2020-11-03 18:54:00');
INSERT INTO privilege_gateway.auth_role_inherited (id, role_id, role_c_id,gmt_create) VALUES ('1', '23', '2','2021-12-02 19:48:00');
INSERT INTO privilege_gateway.auth_role_inherited (id, role_id, role_c_id,gmt_create) VALUES ('2', '23', '3','2021-12-02 19:48:00');
INSERT INTO privilege_gateway.auth_role_inherited (id, role_id, role_c_id,gmt_create) VALUES ('4', '2', '5','2021-12-02 19:48:00');
INSERT INTO privilege_gateway.auth_role_inherited (id, role_id, role_c_id,gmt_create) VALUES ('5', '2', '6','2021-12-02 19:48:00');
INSERT INTO privilege_gateway.auth_group_role (id, role_id, group_id, gmt_create) VALUES ('1', '23', '10', '2021-12-02 19:48:00');