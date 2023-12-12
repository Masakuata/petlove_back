package xatal.sharedz;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SharedzApplicationTests {
    Logger logger = LoggerFactory.getLogger(SharedzApplicationTests.class);

    @Test
    void contextLoads() throws Exception {
        logger.info("-- Context Loading --");
        assert (false);
    }
}
