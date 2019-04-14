package ca.uwo20.ui.suggestion;

import lombok.RequiredArgsConstructor;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Author: toropov
 * Date: 3/12/2019
 */
@RequiredArgsConstructor
public enum SavingsPeriod {
    HOUR("an hour", 1),
    DAY("a day", 24),
    WEEK("a week", 168),
    MONTH("a month", 720),
    YEAR("a year", 8760);

    private final static NumberFormat format = new DecimalFormat("$###,##0.00");

    private final String period;
    private final double multiplier;

    public String format(double hourSavings, boolean postfix) {
        return format.format(hourSavings * multiplier) + (postfix ? " " + period : "");
    }
}
