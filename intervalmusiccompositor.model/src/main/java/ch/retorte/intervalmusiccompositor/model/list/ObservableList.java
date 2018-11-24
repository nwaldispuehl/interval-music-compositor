package ch.retorte.intervalmusiccompositor.model.list;

import ch.retorte.intervalmusiccompositor.model.util.ChangeListener;

import java.util.ArrayList;
import java.util.Collection;

public class ObservableList<E> extends ArrayList<E> {

  private Collection<ChangeListener<? super E>> changeListeners = new ArrayList<>();

  public void addListener(ChangeListener<? super E> changeListener) {
    changeListeners.add(changeListener);
  }

  public void onChange() {
    // TODO
  }


//  @Override
//  public boolean add(E e) {
//    return super.add(e);
//  }
}
