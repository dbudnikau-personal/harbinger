package com.harbinger;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class HarbingerApplicationTests {

    static final ApplicationModules MODULES = ApplicationModules.of(HarbingerApplication.class);

    @Test
    void modulesShouldBeCompliant() {
        MODULES.verify();
    }

    @Test
    void shouldGenerateDocumentation() {
        new Documenter(MODULES).writeDocumentation();
    }
}
