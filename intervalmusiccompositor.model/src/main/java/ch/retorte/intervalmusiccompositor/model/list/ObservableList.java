package ch.retorte.intervalmusiccompositor.model.list;

import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * List which can be observed.
 *
 * @param <E> any type of object.
 */
public class ObservableList<E> extends ArrayList<E> {

    //---- Fields

    private final Collection<ChangeListener<? super E>> changeListeners = new ArrayList<>();


    //---- Methods

    public void addListener(ChangeListener<? super E> changeListener) {
        changeListeners.add(changeListener);
    }

    @SuppressWarnings({"unchecked"})
    private void onChange(Object e) {
        for (ChangeListener<? super E> cl : changeListeners) {
            cl.changed((E) e);
        }
    }


    //---- Augment interface with event handlers

    @Override
    public boolean remove(Object o) {
        final boolean result = super.remove(o);
        onChange(o);
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        onChange(element);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        onChange(null);
    }

    @Override
    public E set(int index, E element) {
        final E result = super.set(index, element);
        onChange(element);
        return result;
    }
}
