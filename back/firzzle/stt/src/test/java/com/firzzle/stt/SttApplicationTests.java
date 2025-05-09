package com.firzzle.stt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SttApplicationTests {

    @Test
    @Disabled("컨텍스트 로딩이 외부 의존성 없이 불가능하므로 비활성화")
    void contextLoads() {
    }

}
