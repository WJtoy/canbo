package com.kkl.kklplus.b2b.canbo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/06/11
 */
@Data
public class OrderMessage implements Serializable {
    private Long id;
    private String orderNo;
    private Long orderId;
}
