package com.pyg.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private IdWorker idWorker;


	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 生成订单的逻辑
	 */
	@Override
	public void add(TbOrder order) {
		//1.根据当前登录人获取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		double totalFee = 0.0;
		List<String> ids = new ArrayList<>();

		//2. 遍历购物车列表,生成订单和订单明细
		for (Cart cart : cartList) {
			//一个cart,生成一个订单记录
			//TODO 雪花算法
			Long orderId = idWorker.nextId();

			//累计子订单id
			ids.add(orderId + "");

			//订单的id
			order.setOrderId(orderId);
			order.setCreateTime(new Date());
			order.setUpdateTime(new Date());
			//状态:未支付
			order.setStatus("1");
			order.setSellerId(cart.getSellerId());
			double payment = 0.0;

			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				//一个购物明细生成一个订单明细记录
				Long id = idWorker.nextId();
				orderItem.setId(id);
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());

				//计算一个商家的总金额
				payment += orderItem.getTotalFee().doubleValue();
				//将订单明细插入数据库
				orderItemMapper.insert(orderItem);
			}

			//总金额的累计
			totalFee += payment;
			//一个订单实付金额
			order.setPayment(new BigDecimal(payment));
			orderMapper.insert(order);

		}

		//添加的逻辑: 生成父订单,将父订单放到redis中
		if(order.getPaymentType().equals("1")){//如果是微信支付
			TbPayLog payLog = new TbPayLog();
			//主键
			payLog.setOutTradeNo(idWorker.nextId() + "");
			//订单的状态:未支付
			payLog.setTradeState("0");
			payLog.setCreateTime(new Date());
			//用户id
			payLog.setUserId(order.getUserId());
			payLog.setPayType(order.getPaymentType());
			//总金额
			payLog.setTotalFee((long)(totalFee * 100));
			String orderString = ids.toString().replace("[","").replace("]","").replace(" ","");
			//子订单的id集合
			payLog.setOrderList(orderString);


			//保存到数据库中
			payLogMapper.insert(payLog);

			//在redis中存放一份
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

		}


		//3. 删除当前登录人的购物车列表
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}
	}


	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();

		if(order!=null){
			if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}

		}

		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据当前登录人,获取支付日志信息
	 *
	 * @param userId ; 当前登录人
	 * @return 支付日志
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 支付成功,修改订单的支付状态,记录微信交易流水,删除redis中的支付日志
	 *
	 * @param out_trade_no
	 * @param transaction_id
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1. 修改父订单的状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//修改成已支付
		payLog.setTradeState("1");
		payLog.setPayTime(new Date());
		//3. 保存支付时间和微信交易流水
		//微信的交易流水
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(payLog);
		//2. 修改子订单的状态
		String orderList = payLog.getOrderList();
		String[] ids = orderList.split(",");
		for (String id : ids) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(id));

			//已支付
			order.setStatus("2");
			order.setPaymentTime(new Date());
			orderMapper.updateByPrimaryKey(order);
		}

		//4. 销毁redis中的父订单(支付日志)
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

	}

}
