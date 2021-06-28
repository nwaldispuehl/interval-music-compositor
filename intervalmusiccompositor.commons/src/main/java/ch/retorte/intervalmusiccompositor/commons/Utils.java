package ch.retorte.intervalmusiccompositor.commons;

import java.util.*;

/**
 * General purpose utils.
 */
public class Utils {

  @SafeVarargs
  public static <E> ArrayList<E> newArrayList(E... elements) {
    ArrayList<E> list = new ArrayList<>();
    Collections.addAll(list, elements);
    return list;
  }

  public static <E> ArrayList<E> newArrayList(List<E> list) {
    return new ArrayList<>(list);
  }

  public static <E> ArrayList<E> newArrayList(Collection<E> list) {
    return new ArrayList<>(list);
  }

}
