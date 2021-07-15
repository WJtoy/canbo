package com.kkl.kklplus.b2b.canbo.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.canbo.entity.CanboOrderInfo;
import com.kkl.kklplus.b2b.canbo.entity.OrderMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderInfoMapper {

    /**
     * 分页查询康宝工单
     * @param workcardSearchModel
     * @return
     */
    Page<CanboOrderInfo> getList(B2BOrderSearchModel workcardSearchModel);

    void updateTransferResult(CanboOrderInfo orderInfo);

    void insert(CanboOrderInfo newOrderInfo);

    List<CanboOrderInfo> findOrdersProcessFlag(@Param("orders")Map<Integer, List<B2BOrderTransferResult>> orders);

    Long findOrderByOrderNoAndDataSource(@Param("orderNo")String orderNo,@Param("dataSource")Integer dataSource);

    void cancelledOrder(B2BOrderTransferResult workcardTransferResults);

    /**
     * 根据ID获取工单对应的处理状态
     * @param ids
     * @return
     */
    List<CanboOrderInfo> findOrdersProcessFlagByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID获取对应工单号
     * @param id
     * @return
     */
    String findOrderNoById(@Param("id")Long id);

    /**
     * 根据工单号获取id、快可立ID
     * @param orderNos
     * @param dataSource
     * @return
     */
    @MapKey("orderNo")
    Map<String, OrderMessage> findOrderByOrderNos(@Param("orderNos") List<String> orderNos,
                                                  @Param("dataSource") int dataSource);

    /**
     * 取消工单
     * @param remark
     * @param id
     * @param processFlag
     * @param updateDt
     */
    void cancelOrderFormB2B(@Param("processComment") String remark,
                            @Param("id") Long id,
                            @Param("processFlag") int processFlag,
                            @Param("updateDt") Long updateDt);
}
