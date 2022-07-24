package com.example.SecKill.dao;

import com.example.SecKill.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")//select * from user where id = #{id}
    public User getById(@Param("id")long id);
}
