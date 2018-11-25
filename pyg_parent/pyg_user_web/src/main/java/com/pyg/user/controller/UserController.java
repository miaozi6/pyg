package com.pyg.user.controller;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import com.pyg_user.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbUser;


import entity.PageResult;
import entity.Result;

import utils.PhoneFormatCheckUtils;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){
		return userService.findAll();
	}


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){
		return userService.findPage(page, rows);
	}

	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		try {
			//1.首先校验验证码是否输入正确
			boolean success = userService.checkCode(user.getPhone(),code);
			if(!success){
				return new Result(false, "验证码输入有误,请重新输入!");
			}

			//2.验证成功,才进行用户注册操作
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}


	/**
	 * 发送短信验证码的方法
	 * @param phone : 手机号
	 * @return
	 */
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){

		try {
			//1. 校验手机号是否法,不合法,直接返回
			boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
			if(!phoneLegal){
				return new Result(false,"手机号不合法");
			}
			//2. 校验成功,调用userService方法,发送短信验证码
			userService.sendCode(phone);
			return new Result(true,"发送验证码成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"验证码发送失败,请重试!");
		}

	}

	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbUser findOne(Long id){
		return userService.findOne(id);
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);
	}

}
