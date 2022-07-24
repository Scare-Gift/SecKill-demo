package com.example.SecKill.dao;

import com.example.SecKill.domain.OrderInfo;
import com.example.SecKill.domain.SKillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SKillOrderDao {

    @Select("select * from seckill_order where user_id = #{userid} and goods_id= #{goodsId}" )
    SKillOrder getMiaoshaOrderByUserIdGoodsId(@Param("userid") Long userid, @Param("goodsId")long goodsId);



    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values( #{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    public long insert(OrderInfo orderInfo);
    @Insert("insert into seckill_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
    public int insertsKillOrderDao(SKillOrder sKillOrder);

    @Select("select * from order_info where id = #{orderId}")
    OrderInfo getOrderByid(@Param("orderId") long orderId);
}
