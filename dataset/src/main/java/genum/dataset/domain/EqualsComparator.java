package genum.dataset.domain;

import java.util.Comparator;

public class EqualsComparator<T> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        return bothAreEqual(o1, o2);
    }
    private int bothAreEqual(T o1, T o2) {
        return o1.equals(o2)? 0: 1;
    }
}
