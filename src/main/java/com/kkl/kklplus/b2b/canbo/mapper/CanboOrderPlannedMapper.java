package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.entity.canbo.sd.CanboOrderPlanned;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CanboOrderPlannedMapper {

    Integer insert(CanboOrderPlanned canboOrderPlanned);

    void updateProcessFlag(CanboOrderPlanned canboOrderPlanned);
}
