package cn.timmy.logic.common;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Author zxx
 * Description 通用mapper
 * Date Created on 2018/6/12
 */
public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T> {

}
