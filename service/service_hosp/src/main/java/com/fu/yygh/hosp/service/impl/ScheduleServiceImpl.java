package com.fu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fu.yygh.cmn.client.DictFeignClient;
import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.hosp.repository.ScheduleRepository;
import com.fu.yygh.hosp.service.DepartmentService;
import com.fu.yygh.hosp.service.HospitalService;
import com.fu.yygh.hosp.service.ScheduleService;
import com.fu.yygh.model.hosp.BookingRule;
import com.fu.yygh.model.hosp.Department;
import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.model.hosp.Schedule;
import com.fu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.fu.yygh.vo.hosp.ScheduleOrderVo;
import com.fu.yygh.vo.hosp.ScheduleQueryVo;
import com.fu.yygh.vo.order.OrderMqVo;
import org.aspectj.weaver.ast.Var;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.sql.rowset.spi.SyncResolver;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> stringObjectMap) {
        //把stringObjectMap转换成schedule对象
        String stringObjectMapString = JSONObject.toJSONString(stringObjectMap);
        Schedule schedule = JSONObject.parseObject(stringObjectMapString, Schedule.class);
        //查询数据库中是否存在排班信息
        Schedule scheduleExist=scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(scheduleExist!=null){
            //有就更新
            schedule.setId(scheduleExist.getId());
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setIsDeleted(scheduleExist.getIsDeleted());
            schedule.setStatus(1);

            scheduleRepository.save(schedule);
        }else {
            //没有就添加
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);

        }

    }

    //带条件查询的分页
    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);

        Pageable pageable = PageRequest.of(page-1, limit, Sort.by(Sort.Direction.ASC,"createTime"));
        Example<Schedule> example = Example.of(schedule);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);


        return all;
    }

    @Override
    public void removeScheduleByHospIdAndSheId(Map<String, Object> paramMap) {
       // paramMap转换成department对象
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap),Schedule.class);
        Schedule scheduleByHoscodeAndHosScheduleId = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if(null!=scheduleByHoscodeAndHosScheduleId){
            scheduleRepository.deleteById(scheduleByHoscodeAndHosScheduleId.getId());
        }
    }

    @Override
    public Map<String, Object> getSchedulePage(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> map=new HashMap<String,Object>();
        //1.获取当前页列表
        //第一个参数：聚合对象
        //第二个参数：输入类型,与集合名称对应的pojo类的字节码
        //第三个参数：输出类型,集合之后要把聚合的字段封装到哪个字段中
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合的条件
                Aggregation.group("workDate")//根据workDate分组，group相当于传统数据库中的group by
                        .first("workDate").as("workDate")//查询workDate字段并起别名
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")//对总的可预约数求和
                        .sum("availableNumber").as("availableNumber"),//对剩余的可预约数求和
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        for (BookingScheduleRuleVo mappedResult : mappedResults) {
            Date workDate = mappedResult.getWorkDate();
            DateTime dateTime = new DateTime(workDate);
            String dayOfWeek = this.getDayOfWeek(dateTime);
            mappedResult.setDayOfWeek(dayOfWeek);
        }
        map.put("bookingScheduleRuleList",mappedResults);


        //2.获取总记录数






        Criteria criteria1 = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation agg1 = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合的条件
                Aggregation.group("workDate")//根据workDate分组，group相当于传统数据库中的group by
        );

        AggregationResults<BookingScheduleRuleVo> aggregate1 = mongoTemplate.aggregate(agg1, Schedule.class, BookingScheduleRuleVo.class);
        int total = aggregate1.getMappedResults().size();//符合条件的总记录数
        map.put("total",total);

        //根据医院编号获取医院名称
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        //其他基础数据
        Map<String,Object> baseMap=new HashMap<String,Object>();
        baseMap.put("hosname",hospital.getHosname());
        map.put("baseMap",baseMap);
        return map;
    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode,new DateTime(workDate).toDate() );
        return list;
    }

    @Override
    public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        //1.为了拿到预约规则，先根据医院id获取当前医院信息
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(hospital==null){
            throw new YYGHException(20001,"没有相关医院信息");
        }
        //获取医院规则
        BookingRule bookingRule = hospital.getBookingRule();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page datePage=this.getListDate(page,limit,bookingRule);

        //获取当前页时间列表
        List<Date> records = datePage.getRecords();

        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(records);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合的条件
                Aggregation.group("workDate")//根据workDate分组，group相当于传统数据库中的group by
                        .first("workDate").as("workDate")//查询workDate字段并起别名
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")//对总的可预约数求和
                        .sum("availableNumber").as("availableNumber")//对剩余的可预约数求和
        );


        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        Map<Date, BookingScheduleRuleVo> collect = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));

        List<BookingScheduleRuleVo> newList=new ArrayList<>();


        for (int i=0;i<records.size();i++){
            Date date = records.get(i);//当天日期
            BookingScheduleRuleVo bookingScheduleRuleVo = collect.get(date);
            if(bookingScheduleRuleVo==null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊人数
                bookingScheduleRuleVo.setDocCount(0);
                //-1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }

            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);


            //最后一页最后一条记录为即将预约， 状态：0：正常，1：即将放号，-1：当天停止放号
            int len=records.size();
            if(i==len-1&&page==datePage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //判断第一页第一条的时间是否已经过了当天预约放号时间
            if(i==0&&page==1){
                //获取当天停止挂号时间
                DateTime dateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(dateTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            newList.add(bookingScheduleRuleVo);
        }


        Map<String,Object> result=new HashMap<>();

        //可预约日期规则数据
        result.put("bookingScheduleList", newList);
        result.put("total", datePage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);


        return result;
    }

    @Override
    public Schedule getScheduleList(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        this.packgeSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //排班信息
        Schedule schedule = this.getScheduleList(scheduleId);
        if(null == schedule) {
            throw new YYGHException();
        }

        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new YYGHException();
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YYGHException();
        }

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname((String) schedule.getParam().get("hosname"));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname((String) schedule.getParam().get("depname"));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    @Override
    public void update(OrderMqVo orderMqVo) {
        Schedule schedule= null;
        if(orderMqVo.getAvailableNumber()!=null){
            schedule = scheduleRepository.findById(orderMqVo.getScheduleId()).get();
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());

        }else {
            schedule=scheduleRepository.findByhosScheduleId(orderMqVo.getScheduleId());
            schedule.setAvailableNumber(schedule.getAvailableNumber()+1);
        }
        scheduleRepository.save(schedule);

    }

    private void packgeSchedule(Schedule schedule) {
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        schedule.getParam().put("hosname",hospital.getHosname());
        Department department = departmentService.getDepartment(hospital.getHoscode(), schedule.getDepcode());
        schedule.getParam().put("depname",department.getDepname());
        Date workDate = schedule.getWorkDate();
        String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
        schedule.getParam().put("dayOfWeek",dayOfWeek);
    }

    private com.baomidou.mybatisplus.extension.plugins.pagination.Page getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取今天的放号时间
        DateTime dateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //如果当前时间已经过了当天放号时间。就把预约周期+1
        Integer cycle = bookingRule.getCycle();//预约周期
        if(dateTime.isBeforeNow()){
            cycle=cycle+1;
        }
        List<Date> dateList=new ArrayList<>();
        for (int i=0;i<cycle;i++){
            DateTime tmpTime = new DateTime().plusDays(i);
            String string = tmpTime.toString("yyyy-MM-dd");
            Date date = new DateTime(string).toDate();
            dateList.add(date);
        }

        int start=(page-1)*limit;
        int end=start+limit;
        if(end>dateList.size()) end=dateList.size();

        List<Date> currentDatePageList = new ArrayList<>();
        for (int j=start;j<end;j++){
            Date date = dateList.get(j);
            currentDatePageList.add(date);
        }




        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> resultPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
        resultPage.setRecords(currentDatePageList);
        return resultPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
