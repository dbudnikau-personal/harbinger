package com.harbinger;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class HarbingerApplicationTests {

    static final ApplicationModules modules = ApplicationModules.of(HarbingerApplication.class);

    @Test
    void modulesShouldBeCompliant() {
        modules.verify();
    }

    @Test
    void shouldGenerateDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
