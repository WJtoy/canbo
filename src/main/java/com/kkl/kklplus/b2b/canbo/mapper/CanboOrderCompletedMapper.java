package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CanboOrderCompletedMapper {

    void insert(CanboOrderCompleted canboOrderCompleted);

    void updateProcessFlag(CanboOrderCompleted canboOrderCompleted);
}
