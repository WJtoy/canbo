package com.kkl.kklplus.b2b.canbo.controller;

import com.kkl.kklplus.b2b.canbo.http.response.GetOrderResponseData;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.service.TestService;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("get")
    public ResponseBody<GetOrderResponseData> get() {
        testService.test2();
        return testService.test();
    }

    @PostMapping("/security")
    public MSResponse security(@RequestBody B2BOrder b2BOrder){
        return new MSResponse();
    }
}
