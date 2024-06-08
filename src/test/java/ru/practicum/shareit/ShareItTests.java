package ru.practicum.shareit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItTests {

    @Test
    void contextLoads() {
        Assertions.assertDoesNotThrow(() -> ShareItApp.main(new String[]{}));
    }

}
