package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.b2b.canbo.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLogMapper {

    Integer insert(SysLog sysLog);

    SysLog get(Long id);
}
