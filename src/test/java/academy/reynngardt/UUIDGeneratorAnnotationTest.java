package academy.reynngardt;

import academy.reyngardt.annotation.processor.UUIDGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UUIDGeneratorAnnotationTest {

    @UUIDGenerator
    private String id;

    @Test
    @DisplayName("Test that ID generate")
    public void generateIdTest() {
        Assertions.assertTrue(id.length() > 10);
    }
}
