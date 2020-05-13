package dca_ijoc;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
    void testApp() {
        assertEquals(1, 1);
        Random generator = new Random(25);
        for (int i = 0 ; i < 10 ; i++) {
            System.out.println(generator.nextDouble());
        }
    }
}
