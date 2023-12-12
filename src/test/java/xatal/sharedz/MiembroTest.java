package xatal.sharedz;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xatal.sharedz.controllers.MiembroController;

@SpringBootTest
public class MiembroTest {
    Logger logger = LoggerFactory.getLogger(MiembroTest.class);
    @Autowired
    private MiembroController controller;

    @Test
    void contextLoads() {
        assert (controller != null);
    }
}
