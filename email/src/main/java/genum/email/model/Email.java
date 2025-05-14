package genum.email.model;

import genum.email.constant.EmailStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Builder
@Setter
@Getter
public class Email {
    @Id
    private String id;
    private String subject;
    private String to;
    private String body;
    private EmailStatus status;
    private int emailTryAttempts;
    private LocalDateTime timeStamp;
}
