package ca.uwo20.ui.suggestion;

import lombok.Value;

/**
 * Author: toropov
 * Date: 3/12/2019
 */
@Value
public class Suggestion {
    private final String suggestion;
    private final double hourSavings;
    private final Difficulty difficulty;

    public String getSavings(SavingsPeriod displayPeriod, boolean postfix) {
        return displayPeriod.format(hourSavings, postfix);
    }
}
