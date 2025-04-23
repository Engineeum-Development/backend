package genum.dataset.domain;

import genum.dataset.enums.PendingActionEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PendingActions {

    public static final Set<PendingAction> pendingActions = generatePendingActions();

    private static Set<PendingAction> generatePendingActions() {
        return Arrays.stream(PendingActionEnum.values())
                .map(pendingActionEnum -> new PendingAction(pendingActionEnum.getName(), pendingActionEnum.getDescription()))
                .collect(Collectors.toSet());
    }
}
