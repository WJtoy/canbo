package com.kkl.kklplus.b2b;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.canbo.http.request.GetSendOrderResultRequestParam;
import com.kkl.kklplus.b2b.canbo.http.request.OrderFinishRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderResponseData;
import com.kkl.kklplus.b2b.canbo.mapper.CanboOrderCompletedMapper;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BOrderMQSender;
import com.kkl.kklplus.b2b.canbo.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.canbo.service.TestService;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BShopEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@ActiveProfiles("dev")
public class B2BCanboApplicationTests {



    //@Autowired
    //private B2BProcesslogService b2BProcesslogService;
    @Test
    public void test() {
        //根据时间范围加分片加接口名称更新原始log的b2bOrderNo
//        b2BProcesslogService.updateLogOrderNo(1546272000000L,1554048000000L,"20191","toAssignEngineer.jhtml");
        intToStringList(2);
    }
    public static List<String> intToStringList(int num){
        List<String> list = Lists.newArrayList();
        //if(num<=0){
        //    return list;
        //}
        String binaryStr =Integer.toBinaryString(num);//57->111001 = 2^5 + 2^4 + 2^3 + 2^0
        binaryStr = StringUtils.reverse(binaryStr);
        for(int i=0,size=binaryStr.length();i<size;i++){
            if(binaryStr.charAt(i)!='0'){
                list.add(String.valueOf(i));
            }
        }
        return list;
    }
}
