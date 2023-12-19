package info.kgeorgiy.ja.pulnikova.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> arrayList;
    private final Comparator<T> comparator;

    private ArraySet(List<T> list, Comparator<T> comparator){
        this.arrayList = list;
        this.comparator = comparator;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<T> comparator) {
        this(new ArrayList<>(), comparator);
    }

    public ArraySet(Collection<T> collections) {
        this(collections, null);
    }

    public ArraySet(Collection<T> collections, Comparator<T> comparator) {
        TreeSet<T> tree = new TreeSet<>(comparator);
        tree.addAll(collections);
        arrayList = List.copyOf(tree);
        this.comparator = comparator;
    }

    private T getIndex(int index) {
        if (isEmpty()) {
            throw new NoSuchElementException("The set is empty");
        } else {
            return arrayList.get(index);
        }
    }

    private int defineIndex(T element) {
        int index = Collections.binarySearch(arrayList, element, comparator);
        if (index < 0) {
            return -(index + 1);
        }
        return index;
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        int firstIndex = defineIndex(fromElement);
        if (comparator != null && comparator.compare(toElement, fromElement) < 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        int lastIndex = defineIndex(toElement);
        return new ArraySet<>(arrayList.subList(firstIndex, lastIndex), comparator);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        int index = defineIndex(toElement);
        return new ArraySet<>(arrayList.subList(0, index), comparator);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        int index = defineIndex(fromElement);
        return new ArraySet<>(arrayList.subList(index, size()), comparator);
    }

    @Override
    public T first() {
        return getIndex(0);
    }

    @Override
    public T last() {
        return getIndex(arrayList.size() - 1);
    }

    @Override
    public int size() {
        return arrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return arrayList.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (isEmpty()) {
            return false;
        }
        return Collections.binarySearch(arrayList, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return arrayList.iterator();
    }
}
