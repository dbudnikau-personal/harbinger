package com.harbinger.context;

import com.harbinger.domain.Project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProjectContextLoader {

    public Optional<String> loadClaudeMd(Project project) {
        Path claudeMd = Path.of(project.path()).resolve("CLAUDE.md");
        if (!Files.exists(claudeMd)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(claudeMd));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
