package com.example.SecKill.vo;


import com.example.SecKill.domain.User;

public class GoodsDetailVo {
	private int killStatus = 0;
	private int remainSeconds = 0;
	private GoodsVo goods ;
	private User user;

	public int getKillStatus() {
		return killStatus;
	}

	public void setKillStatus(int killStatus) {
		this.killStatus = killStatus;
	}

	public int getRemainSeconds() {
		return remainSeconds;
	}

	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}

	public GoodsVo getGoods() {
		return goods;
	}

	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
