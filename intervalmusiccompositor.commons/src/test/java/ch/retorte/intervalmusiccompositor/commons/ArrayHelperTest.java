package ch.retorte.intervalmusiccompositor.commons;

import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.arrayMerge16bit;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.getAverageInterval;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.isConvergent;
import static ch.retorte.intervalmusiccompositor.commons.ArrayHelper.prepareListForRemoval;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author nw
 */
public class ArrayHelperTest {

  @Test
  public void shouldArrayToList() {
    // given
    int[] intArray = new int[] { 1, 2, 3 };

    // when
    List<Integer> intList = ArrayHelper.arrayToList(intArray);

    // then
    assertThat(arr(intList), is(arr(1, 2, 3)));
  }

  @Test
  public void shouldPrepareListForRemoval() {
    // given
    int[] intArray = new int[] { 2, 4, 1, 3 };

    // when
    List<Integer> preparedList = prepareListForRemoval(intArray);

    // then
    assertThat(arr(preparedList), is(arr(4, 3, 2, 1)));
  }

  @Test
  public void shouldArrayMerge16Bit() {
    // given
    byte[] source = hexStringToByteArray("00 03  f0 03  00 03");
    byte[] target = hexStringToByteArray("01 00  00 03  0f 00");

    // when
    arrayMerge16bit(source, 0, target, 0, 6);

    // then
    assertThat(target, is(hexStringToByteArray("01 03  f0 06  0f 03")));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailOnTooLargeValues() {
    // given
    byte[] source = hexStringToByteArray("00 03  00 03");
    byte[] target = hexStringToByteArray("00 00  00 03  00 00  00 00");

    // when
    arrayMerge16bit(source, 2, target, 0, 8);
  }

  @Test
  public void shouldGetAverageIntervalOfZeroForEmptyInput() {
    // when
    long averageInterval = getAverageInterval(newArrayList(new Long[0]), 5);

    // then
    assertThat(averageInterval, is(0L));
  }

  @Test
  public void shouldGetAverageInterval() {
    // given
    List<Long> tapEvents1 = newArrayList(1L, 2L, 3L, 4L, 5L);
    List<Long> tapEvents2 = newArrayList(1L, 2L, 4L, 8L, 16L, 32L);
    List<Long> tapEvents3 = newArrayList(1L, 2L, 4L, 8L, 16L, 32L);
    List<Long> tapEvents4 = newArrayList(10L, 10L, 10L, 10L, 20L, 20L, 20L, 20L, 20L, 20L, 20L);

    // when
    long averageInterval1 = getAverageInterval(tapEvents1, 10);
    long averageInterval2 = getAverageInterval(tapEvents2, 10);
    long averageInterval3 = getAverageInterval(tapEvents3, 2);
    long averageInterval4 = getAverageInterval(tapEvents4, 10);

    // then
    assertThat(averageInterval1, is(1L));
    assertThat(averageInterval2, is(6L));
    assertThat(averageInterval3, is(12L));
    assertThat(averageInterval4, is(1L));
  }

  @Test
  public void shouldDetermineIfConvergent() {
    // given
    List<Double> list1 = Lists.newArrayList(0.5, 1.0, 1.5, 2.0, 2.5);
    List<Double> list2 = Lists.newArrayList(0.5, 1.0, 1.5, 2.0, 2.5);
    List<Double> list3 = Lists.newArrayList(1., 2., 3., 4., 5.);


    // when
    boolean converges1 = isConvergent(list1, 3, 0.51);
    boolean converges2 = isConvergent(list2, 3, 0.49);
    boolean converges3 = isConvergent(list3, 4, 1);

    // then
    assertTrue(converges1);
    assertFalse(converges2);
    assertTrue(converges3);
  }

  @Test
  public void shouldNotConvergeIfListTooShort() {
    assertFalse(isConvergent(Lists.newArrayList(0.1), 2, 1));

  }

  @Test
  public void shouldPrettyPrintList() {
    // given
    List<Integer> list = Lists.newArrayList(1, 2, 3);

    // when
    String prettyPrintedList = ArrayHelper.prettyPrintList(list);

    // then
    assertThat(prettyPrintedList, is("1-2-3"));
  }

  @Test
  public void shouldPrettyPrintSingleNumber() {
    // given
    List<Integer> list = Lists.newArrayList(9);

    // when
    String prettyPrintedList = ArrayHelper.prettyPrintList(list);

    // then
    assertThat(prettyPrintedList, is("9"));
  }

  private Integer[] arr(Integer... integers) {
    return integers;
  }
  
  private Integer[] arr(List<Integer> integerList) {
    return integerList.toArray(new Integer[integerList.size()]);
  }

  /**
   * Taken from http://stackoverflow.com/questions/11208479/how-do-i-initialize-a-byte-array-in-java/11208685#11208685
   */
  private byte[] hexStringToByteArray(String s) {
    s = s.replaceAll(" ", "");
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

}
