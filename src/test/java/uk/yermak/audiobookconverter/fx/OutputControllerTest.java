package uk.yermak.audiobookconverter.fx;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class OutputControllerTest {

    @Test
    public void exposesAllOutputChoiceOptions() {
        assertEquals(OutputController.SPLIT_OPTIONS, List.of("parts", "chapters"));
        assertEquals(OutputController.FORCE_OPTIONS, List.of("Auto", "Always", "Avoid"));
    }
}
