package com.fu.yygh.orders.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.common.result.R;
import com.fu.yygh.enums.OrderStatusEnum;
import com.fu.yygh.hospital.client.HospitalFeignClient;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.model.order.PaymentInfo;
import com.fu.yygh.model.user.Patient;
import com.fu.yygh.orders.mapper.OrderInfoMapper;
import com.fu.yygh.orders.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.yygh.orders.service.PaymentService;
import com.fu.yygh.orders.service.WeChatService;
import com.fu.yygh.orders.utils.HttpRequestHelper;
import com.fu.yygh.rabbit.MqConst;
import com.fu.yygh.rabbit.RabbitService;
import com.fu.yygh.user.client.PatientFeignClient;
import com.fu.yygh.vo.hosp.ScheduleOrderVo;
import com.fu.yygh.vo.msm.MsmVo;
import com.fu.yygh.vo.order.OrderCountQueryVo;
import com.fu.yygh.vo.order.OrderCountVo;
import com.fu.yygh.vo.order.OrderMqVo;
import com.fu.yygh.vo.order.OrderQueryVo;
import com.google.common.collect.Ordering;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.rowset.spi.SyncResolver;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-07-07
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public Long saveOrder(String scheduleId, Integer patientId) {
        //1.先根据scheduleId获取医生排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        //2.根据patientId获取就诊人信息
        R r = patientFeignClient.getPatientInfo(patientId);
        Patient patient = JSONObject.parseObject(JSONObject.toJSONString(r.getData().get("patient")), Patient.class);
        //3.平台系统调用第三方医院系统。确认还能不能挂号
        Map<String,Object> paramMap=new HashMap<String,Object>();


        //医院端需要的数据
        paramMap.put("hoscode",scheduleOrderVo.getHoscode());
        paramMap.put("depcode",scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate",new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount",scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", "");

        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        int code = jsonObject.getInteger("code");
        if(code==200){//可以预约挂号，需要保存订单信息
            //3.1 不能挂号，抛出异常
            //3.2 可以挂号
            //3.2.1把上面得到的数据插入到数据库中
            OrderInfo orderInfo = new OrderInfo();


            //医院返回的数据
            JSONObject data = jsonObject.getJSONObject("data");
            //预约记录唯一标识
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");
            //取号时间
            String fetchTime = data.getString("fetchTime");
            //取号地址
            String fetchAddress = data.getString("fetchAddress");

            //用户id
            orderInfo.setUserId(patient.getUserId());
            //订单号
            String outTradeNo=System.currentTimeMillis()+""+new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setHoscode(scheduleOrderVo.getHoscode());
            orderInfo.setHosname(scheduleOrderVo.getHosname());
            orderInfo.setDepcode(scheduleOrderVo.getDepcode());
            orderInfo.setDepname(scheduleOrderVo.getDepname());
            orderInfo.setTitle(scheduleOrderVo.getTitle());
            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
            orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
            orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
            orderInfo.setPatientId(patientId.longValue());
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            orderInfo.setAmount(scheduleOrderVo.getAmount());
            orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            baseMapper.insert(orderInfo);



            //3.2.2更新排班数据中的剩余预约人数
            Integer reservedNumber = data.getInteger("reservedNumber");//剩余预约
            Integer availableNumber = data.getInteger("availableNumber");//总的预约

            OrderMqVo orderMqVo=new OrderMqVo();
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setScheduleId(scheduleId);

            MsmVo msmVo=new MsmVo();
            msmVo.setPhone(patient.getPhone());
            msmVo.setTemplateCode("已经成功预约");
            orderMqVo.setMsmVo(msmVo);

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);


            //3.2.3预约成功给就诊人发送短信提醒

            //4.返回订单id
            return orderInfo.getId();
        }else {
            throw new YYGHException(20001,"挂号异常");
        }

    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        String name = orderQueryVo.getKeyword();//医院名称
        Long patientId = orderQueryVo.getPatientId();//就诊人id
        String orderStatus = orderQueryVo.getOrderStatus();//订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对值进行非空判断
        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper<>();

        if(!StringUtils.isEmpty(name)){
            queryWrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)){
            queryWrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)){
            queryWrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)){
            queryWrapper.eq("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.eq("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.eq("create_time",createTimeEnd);
        }

        Page<OrderInfo> pages = baseMapper.selectPage(pageParam, queryWrapper);
        pages.getRecords().stream().forEach(item->{
            this.packOrderInfo(item);
        });
        return pages;
    }

    @Override
    public OrderInfo getOrders(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        //1.先判断当前时间是否已经过了平台规定的退号截至时间
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        Date quitTime = orderInfo.getQuitTime();
        DateTime dateTime = new DateTime(quitTime);
        if(dateTime.isBeforeNow()){
            throw new YYGHException(2001,"已过退号截止时间");
        }


        Map<String,Object> map=new HashMap<>();
        map.put("hoscode",orderInfo.getHoscode());
        map.put("hosRecordId",orderInfo.getHosRecordId());


        JSONObject jsonObject = HttpRequestHelper.sendRequest(map, "http://localhost:9998/order/updateCancelStatus");
        if(jsonObject.getInteger("code")!=200){
            throw new YYGHException(2001,"不能取消");//2.1 如果医院返回不能取消，抛出异常
        }else {//2.如果没过，平台系统调用医院系统，确认能否取消预约
            if(orderInfo.getOrderStatus().intValue()==OrderStatusEnum.PAID.getStatus().intValue()) {
                boolean flag = weChatService.refund(orderId);
                if (!flag) {
                    throw new YYGHException(2001, "退款失败");
                }
            }
                //3.更新订单表订单状态，
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                this.updateById(orderInfo);
                //更新支付记录表的支付状态
                QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<>();
                queryWrapper.eq("order_id",orderId);

                PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
                paymentInfo.setPaymentStatus(-1);//退款
                paymentInfo.setUpdateTime(new Date());
                paymentService.updateById(paymentInfo);

                //4.更新排班数据（可预约数+1） 发送短信信息
                //发送mq信息更新预约数，我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减一
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId());
                //短信提示
                MsmVo msmVo = new MsmVo();
                msmVo.setPhone(orderInfo.getPatientPhone());
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
                return true;
            }

        }

    @Override
    public void patientTips() {
        String string = new DateTime().toString("yyyy-MM-dd");
        QueryWrapper<OrderInfo> queryWrapper=new QueryWrapper();
        queryWrapper.eq("reserve_date",string);
        queryWrapper.eq("order_status",OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> list = baseMapper.selectList(queryWrapper);
        for (OrderInfo orderInfo : list) {
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }

    }

    @Override
    public   Map<String,Object> countOrderInfoByQuery(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> orderCountVos = baseMapper.countOrderInfoByQuery(orderCountQueryVo);
        List<String> collectDate = orderCountVos.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> collectCount = orderCountVos.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        Map<String,Object> map=new HashMap<>();
        map.put("dateList",collectDate);
        map.put("countList",collectCount);
        return map;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo){
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
