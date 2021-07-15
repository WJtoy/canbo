package com.kkl.kklplus.b2b.canbo.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

@Data
public class FailedProcessLog extends B2BBase<FailedProcessLog> {
    private String interfaceName;
    private String infoJson;
    private String resultJson;
}
