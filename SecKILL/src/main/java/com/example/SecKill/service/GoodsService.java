package com.example.SecKill.service;


import com.example.SecKill.dao.GoodsDao;
import com.example.SecKill.domain.SKillGoods;
import com.example.SecKill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;
    public List<GoodsVo> listGoodsvo(){
        return goodsDao.listGoodsVo();
    }


    public GoodsVo getGoodsVoByDoodsId(long goodsId) {
        return goodsDao.getGoodsVoByDoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {
        SKillGoods g = new SKillGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        return ret > 0;
    }
}
