package genum.email.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EmailTemplateUtil {
    private static String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try(Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }

    }

    public static String processTemplate(String templatePath, Map<String, String> replacements) throws IOException {
        String template = loadTemplate(templatePath);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }
}
