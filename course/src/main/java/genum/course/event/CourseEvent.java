package genum.course.event;

import genum.shared.events.DomainEvent;
import genum.shared.course.DTO.CourseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class CourseEvent implements DomainEvent  {

    private CourseDTO course;
    private EventType eventType;
    private LocalDateTime time;
    private Map<String,?> data;
}
