package com.kkl.kklplus.b2b.canbo.mapper;

import com.kkl.kklplus.b2b.canbo.entity.CanboOrderConfirm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CanboOrderConfirmedMapper {

    void insert(CanboOrderConfirm orderConfirm);

    void updateProcessFlag(@Param("orderConfirms") List<CanboOrderConfirm> orderConfirms,
                           @Param("processFlag")Integer processFlag,
                           @Param("processComment")String processComment,
                           @Param("updateDate") Long updateDate);
}
