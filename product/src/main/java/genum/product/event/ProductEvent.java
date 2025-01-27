package genum.product.event;

import genum.product.model.Course;
import genum.shared.events.DomainEvent;
import genum.shared.product.DTO.CourseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class ProductEvent implements DomainEvent  {

    private CourseDTO course;
    private EventType eventType;
    private LocalDateTime time;
    private Map<String,?> data;
    @Override
    public LocalDateTime getTimeStamp() {
        return time;
    }
}
