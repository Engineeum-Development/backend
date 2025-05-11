package genum.course.event;

import genum.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEventListener {

    private final CourseService courseService;

    @EventListener
    public void onProductEvent(CourseEvent domainEvent) {
        switch (domainEvent.getEventType()) {
            case COURSE_ENROLLED -> {
            }
            case PRICE_CHANGE -> {}
        }

    }
}
