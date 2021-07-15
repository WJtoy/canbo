package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.entity.canbo.sd.CanboOrderCancelled;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CanboOrderCancelledMapper {

    void insert(CanboOrderCancelled canboOrderCancelled);

    void updateProcessFlag(CanboOrderCancelled canboOrderCancelled);
}
