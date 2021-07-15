package com.kkl.kklplus.b2b.canbo.mapper;


import com.github.pagehelper.Page;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 原始数据日志数据库操作
 * @author cxj
 * @date 2019/07/16
 */
@Mapper
public interface B2BProcesslogMapper {

    /**
     * 新增原始数据日志几率
     * @param b2BProcesslog log实体
     * @return 返回影响条数
     */
    Integer insert(B2BOrderProcesslog b2BProcesslog);

    /**
     * 更新原始数据日志处理结果
     * @param b2BProcesslog 操作实体
     */
    void updateProcessFlag(B2BOrderProcesslog b2BProcesslog);

    /**
     *  分页查询原始数据log
     * @param processLogSearchModel 查询实体
     * @param code  操作接口code
     * @return 返回log
     */
    Page<B2BOrderProcesslog> getList(
            @Param("processLogSearchModel") B2BProcessLogSearchModel processLogSearchModel,
            @Param("code") String code);

}
