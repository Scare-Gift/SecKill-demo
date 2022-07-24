package com.example.SecKill.dao;



import com.example.SecKill.domain.SKillGoods;
import com.example.SecKill.vo.GoodsVo;
import org.apache.ibatis.annotations.*;


import java.util.List;


@Mapper
public interface GoodsDao {


    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByDoodsId(@Param("goodsId") long goodsId);

    @Update("update seckill_goods set stock_count = stock_count-1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(SKillGoods g);
}
