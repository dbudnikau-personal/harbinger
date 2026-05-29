package com.harbinger.llm;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
class InputSanitizer {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore (all )?(previous|prior|above) instructions?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you are now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard (your )?(previous|prior|system|all)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("act as (a |an )?(?!assistant)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[SYSTEM\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\|.*?\\|>")
    );

    String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String result = input;
        for (Pattern pattern : INJECTION_PATTERNS) {
            result = pattern.matcher(result).replaceAll("[filtered]");
        }
        return result;
    }
}
