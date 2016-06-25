package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.junit.Assert;
import org.junit.Test;

public class TimeFrameTest extends TestBase {

    @Test
    public void getInterval() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        DateTime earliestStart = toDateTimeToday(23, 0, 0);
        DateTime latestEnd = toDateTimeTomorrow(1, 0, 0);
        assertDateTime(earliestStart, timeFrame.getInterval(toInstant(19, 30, 0)).getStart());
        assertDateTime(latestEnd, timeFrame.getInterval(toInstant(19, 30, 0)).getEnd());

        assertDateTime(earliestStart, timeFrame.getInterval(toInstantTomorrow(0, 30, 0)).getStart());
        assertDateTime(latestEnd, timeFrame.getInterval(toInstantTomorrow(0, 30, 0)).getEnd());
    }

    private void assertDateTime(DateTime dt1, DateTime dt2) {
        Assert.assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }
}
