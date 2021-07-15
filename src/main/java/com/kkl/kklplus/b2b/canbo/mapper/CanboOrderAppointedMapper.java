package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.entity.canbo.sd.CanboOrderAppointed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CanboOrderAppointedMapper {

    void insert(CanboOrderAppointed canboOrderAppointed);

    void updateProcessFlag(CanboOrderAppointed canboOrderAppointed);
}
