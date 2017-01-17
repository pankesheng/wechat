package com.pks.wechat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pks.wechat.entity.MenuButton;
import com.pks.wechat.mapper.MenuButtonMapper;
import com.zcj.web.mybatis.service.BasicServiceImpl;

@Component(value="menuButtonService")
public class MenuButtonServiceImpl extends BasicServiceImpl<MenuButton, Long, MenuButtonMapper> implements MenuButtonService{

	@Autowired
	private MenuButtonMapper menuButtonMapper;
	@Override
	protected MenuButtonMapper getMapper() {
		return menuButtonMapper;
	}

}
