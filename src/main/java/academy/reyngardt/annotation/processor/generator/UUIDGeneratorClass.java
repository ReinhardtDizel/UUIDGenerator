package academy.reyngardt.annotation.processor.generator;

import java.util.UUID;

public class UUIDGeneratorClass {

    private String id;

    public UUIDGeneratorClass() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }
}
