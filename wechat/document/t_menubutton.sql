/*
Navicat MySQL Data Transfer

Source Server         : 192.168.1.88
Source Server Version : 50525
Source Host           : 192.168.1.88:3306
Source Database       : wechat

Target Server Type    : MYSQL
Target Server Version : 50525
File Encoding         : 65001

Date: 2017-01-18 10:26:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_menubutton
-- ----------------------------
DROP TABLE IF EXISTS `t_menubutton`;
CREATE TABLE `t_menubutton` (
  `id` bigint(20) NOT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `btn_list` tinyint(4) DEFAULT NULL,
  `btn_name` varchar(50) NOT NULL,
  `btn_type` varchar(50) DEFAULT NULL,
  `btn_url` varchar(200) DEFAULT NULL,
  `btn_key` varchar(50) DEFAULT NULL,
  `btn_state` tinyint(4) DEFAULT '1',
  `btn_order` int(10) DEFAULT NULL,
  `ctime` datetime DEFAULT NULL,
  `utime` datetime DEFAULT NULL,
  `btn_media_id` varchar(100) DEFAULT NULL,
  `btn_media_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_menubutton
-- ----------------------------
INSERT INTO `t_menubutton` VALUES ('2014435173294080', null, '1', '测试', null, null, null, '1', '1', '2017-01-18 10:05:22', '2017-01-18 10:05:22', '', '');
INSERT INTO `t_menubutton` VALUES ('2014437445165056', '2014435173294080', '0', '测试入口', 'view', 'http://pweixin.tunnel.qydev.com/wechat/wxpay/test/index.ajax', null, '1', '1', '2017-01-18 10:07:40', '2017-01-18 10:07:40', '', '');
