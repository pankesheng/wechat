package com.pks.wechat.service;

import java.util.List;
import java.util.Map;

import com.pks.wechat.entity.User;
import com.zcj.web.mybatis.service.BasicService;

public interface UserService extends BasicService<User, Long> {

	List<Integer> findHourses(Map<String, Object> qbuilder);

	List<Integer> findRooms(Map<String, Object> qbuilder);

}
