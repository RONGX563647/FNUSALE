package com.fnusale.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀活动 Mapper
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    /**
     * 获取当前进行中和即将开始的秒杀活动
     */
    @Select("SELECT * FROM t_seckill_activity WHERE activity_status IN ('NOT_START', 'ON_GOING') ORDER BY start_time ASC")
    List<SeckillActivity> selectActiveActivities();

    /**
     * 获取今日秒杀活动
     */
    @Select("SELECT * FROM t_seckill_activity WHERE DATE(start_time) = CURDATE() OR DATE(end_time) = CURDATE() ORDER BY start_time ASC")
    List<SeckillActivity> selectTodayActivities();

    /**
     * 分页查询秒杀活动
     */
    @Select("<script>" +
            "SELECT * FROM t_seckill_activity WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND activity_status = #{status}</if>" +
            " ORDER BY start_time DESC" +
            "</script>")
    IPage<SeckillActivity> selectActivityPage(Page<SeckillActivity> page, @Param("status") String status);

    /**
     * 更新活动状态为进行中
     */
    @Update("UPDATE t_seckill_activity SET activity_status = 'ON_GOING', update_time = NOW() WHERE activity_status = 'NOT_START' AND start_time <= NOW()")
    int updateToOngoing();

    /**
     * 更新活动状态为已结束
     */
    @Update("UPDATE t_seckill_activity SET activity_status = 'END', update_time = NOW() WHERE activity_status = 'ON_GOING' AND end_time <= NOW()")
    int updateToEnded();

    /**
     * 扣减库存（乐观锁）
     */
    @Update("UPDATE t_seckill_activity SET remain_stock = remain_stock - 1, update_time = NOW() WHERE id = #{activityId} AND remain_stock > 0")
    int deductStock(@Param("activityId") Long activityId);

    /**
     * 查询即将开始的活动（30分钟内）
     */
    @Select("SELECT * FROM t_seckill_activity WHERE activity_status = 'NOT_START' AND start_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 30 MINUTE)")
    List<SeckillActivity> selectStartingSoon();

    /**
     * 查询5分钟内开始的活动
     */
    @Select("SELECT * FROM t_seckill_activity WHERE activity_status = 'NOT_START' AND start_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 5 MINUTE)")
    List<SeckillActivity> selectStartingIn5Minutes();

    /**
     * 获取秒杀时段列表
     */
    @Select("SELECT DISTINCT DATE_FORMAT(start_time, '%H:%i') as time_slot FROM t_seckill_activity WHERE DATE(start_time) = CURDATE() ORDER BY start_time ASC")
    List<String> selectTimeSlots();

    /**
     * 根据时段获取活动
     */
    @Select("SELECT * FROM t_seckill_activity WHERE DATE(start_time) = CURDATE() AND DATE_FORMAT(start_time, '%H:%i') = #{timeSlot} ORDER BY start_time ASC")
    List<SeckillActivity> selectByTimeSlot(@Param("timeSlot") String timeSlot);
}