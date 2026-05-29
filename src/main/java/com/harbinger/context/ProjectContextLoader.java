package com.harbinger.context;

import com.harbinger.domain.Project;
import com.harbinger.domain.SecretLeakException;
import com.harbinger.domain.SecretPatternMatcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectContextLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectContextLoader.class);

    public Optional<String> loadClaudeMd(Project project) {
        Path claudeMd = Path.of(project.path()).resolve("CLAUDE.md");
        if (!Files.exists(claudeMd)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(claudeMd);
            if (SecretPatternMatcher.containsSecret(content)) {
                LOG.warn("Secret-like pattern detected in CLAUDE.md of project '{}' — context excluded from LLM prompt",
                        project.name());
                throw new SecretLeakException(
                        "CLAUDE.md for project '" + project.name() + "' contains sensitive data — refusing to send to LLM"
                );
            }
            return Optional.of(content);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
