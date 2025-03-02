package genum.learn.dto;

import java.util.Set;

public record VideoUploadRequest(String lessonId, String description, String title, int videoNumber, Set<String> tags) {}
