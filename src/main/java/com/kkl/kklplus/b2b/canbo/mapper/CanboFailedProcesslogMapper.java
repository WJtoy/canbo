package com.kkl.kklplus.b2b.canbo.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.canbo.entity.FailedProcessLog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 失败日志数据操作
 * @author chenxj
 * @date 2019/07/17
 */
@Mapper
public interface CanboFailedProcesslogMapper {

    /**
     * 查询日志是否存在
     * @param id
     * @return
     */
    Long getFailedLogById(@Param("id") Long id);

    /**
     * 更新处理次数
     * @param logId
     * @param processComment
     */
    void updateFailedLogTimes(@Param("id") Long logId,
                              @Param("processComment") String processComment);

    /**
     * 新增错误日志
     * @param failedLog
     */
    void insert(FailedProcessLog failedLog);

    /**
     * 分页查询错误日志
     * @param processLogSearchModel
     * @param code
     * @return
     */
    Page<B2BOrderProcesslog> getList(@Param("processLogSearchModel") B2BProcessLogSearchModel processLogSearchModel,
                                     @Param("code") String code);

    /**
     * 根据ID查询详细信息
     * @param id
     * @return
     */
    B2BOrderProcesslog getFailedLogMessageById(@Param("id") Long id);

    /**
     * 根据ID关闭日志
     * @param id
     */
    void closeFailedLogById(@Param("id")Long id);
}
