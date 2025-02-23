package ch.retorte.intervalmusiccompositor.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.reverse;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;


/**
 * @author nw
 */
public class ArrayHelper {

    private static final String ITEM_DELIMITER = "-";

    static List<Integer> arrayToList(int[] array) {
        List<Integer> result = new ArrayList<>();
        for (Integer e : array) {
            result.add(e);
        }
        return result;
    }

    /**
     * Merges two byte arrays -- source and destination -- together by summing field-wise where a field is a 16bit word, which is
     * two of these array bytes. The result is written into the destination array.
     */
    public static void arrayMerge16bit(byte[] sourceArray, int sourceStart, byte[] destinationArray, int destinationStart, int length) {

        if (sourceArray.length - sourceStart < length || destinationArray.length - destinationStart < length) {
            throw new IllegalStateException("Length too large for input arrays.");
        }

        for (int i = 0; i < length; i = i + 2) {

            int srcValue = (sourceArray[sourceStart + i] & 0xFF) | (sourceArray[sourceStart + i + 1] << 8);
            int dstValue = (destinationArray[destinationStart + i] & 0xFF) | (destinationArray[destinationStart + i + 1] << 8);

            int sum = srcValue + dstValue;

            if (32768 <= sum) {
                sum = 32768;
            }

            if (sum <= -32767) {
                sum = -32767;
            }

            destinationArray[destinationStart + i] = (byte) (sum & 0xFF);
            destinationArray[destinationStart + i + 1] = (byte) (sum >> 8);
        }
    }

    public static byte[] extract(byte[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public static void insertAt(byte[] array, byte[] insertee, int start) {
        if (array.length < start + insertee.length) {
            throw new IllegalArgumentException("If inserted at position " + start + ", the inserted array of length " + insertee.length + " exceeds the length of the host array: " + array.length);
        }
        System.arraycopy(insertee, 0, array, start, insertee.length);
    }

    /**
     * We prepare a list of indices for removal by reversing the ordered list, thus removing the item with the highest index first.
     * This prevents that the indices get out of order.
     */
    public static List<Integer> prepareListForRemoval(int[] list) {
        List<Integer> integers = arrayToList(list);
        sort(integers);
        reverse(integers);
        return integers;
    }

    /**
     * Calculates the average difference between the last n timestamp intervals of the input array. The assumption is that the list of timestamps is increasing.
     *
     * @param tapTimestamps the list of timestamps
     * @param n             the number of timestamp intervals we'd like to use for the calculation.
     * @return the average interval between the considered values
     */
    public static long getAverageInterval(List<Long> tapTimestamps, int n) {

        long result = 0;
        int start = tapTimestamps.size() - (n + 1);

        if (start < 0) {
            start = 0;
            n = tapTimestamps.size() - 1;
        }

        if (n == 0) {
            return 0;
        }

        for (int i = start; i < tapTimestamps.size() - 1; i++) {
            result += Math.abs(tapTimestamps.get(i + 1) - tapTimestamps.get(i));
        }

        return (result / n);
    }

    /**
     * Goes through the array and determines if the entries differ by at most eps pairwise. If yes (meaning if the difference is always smaller than eps), then
     * the values are considered convergent.
     *
     * @param list List of values to be checked
     * @param eps  The difference two consecutive values may have at most
     * @return True is the difference of any two consecutive entries is smaller or equal to eps
     */
    public static boolean isConvergent(List<Double> list, int range, double eps) {

        boolean result = true;

        if (list.size() < 2 || list.size() - 1 < range) {
            return false;
        }


        for (int i = list.size() - range; i < list.size(); i++) {
            double difference = Math.abs(list.get(i - 1) - list.get(i));
            if (eps < difference) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Takes a list of integers and returns a conveniently formatted string. We
     * need this to create filenames with the elements of the lists in them.
     *
     * @param list the list of items to be pretty printed.
     * @return String representation of the elements of said list
     */
    static String prettyPrintList(List<Integer> list) {
        return list.stream().map(Object::toString).collect(joining(ITEM_DELIMITER));
    }
}

