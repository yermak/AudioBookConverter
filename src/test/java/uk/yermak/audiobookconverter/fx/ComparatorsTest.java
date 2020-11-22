package uk.yermak.audiobookconverter.fx;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComparatorsTest {

    private final Comparator<String> cmp = Comparators.comparingAlphaDecimal(Comparator.comparing(CharSequence::toString, String::compareToIgnoreCase));

    @DataProvider(name = "data-provider")
    public Object[][] dpMethod() {
        return new Object[][]{
                {List.of("p001", "p003", "p002"), List.of("p001", "p002", "p003")},
                {List.of("p-9", "p-10", "p-8"), List.of("p-8", "p-9", "p-10")},
                {List.of("P1C1", "P2C1", "P1C3", "P2", "P1"), List.of("P1", "P1C1", "P1C3", "P2", "P2C1")},
        };
    }

    @Test(dataProvider = "data-provider")
    public void testComparingAlphaDecimal(List<String> input, List<String> expected) {
        List<String> actual = new ArrayList<>(input);
        actual.sort(cmp);
        Assert.assertEquals(actual, expected);
    }
}
