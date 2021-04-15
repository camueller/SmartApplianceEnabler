package de.avanux.smartapplianceenabler.webservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceBuilder;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.util.FileHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {SaeController.class})
@WebMvcTest
public class SaeControllerTest {

    private final static String SCHEDULE_RUNTIME_REQUEST_DAY_TIMEFRAME =
            "<Schedules xmlns=\"" + ApplianceManager.SCHEMA_LOCATION + "\">\n" +
            "  <Schedule>\n" +
            "    <RuntimeRequest min=\"7200\" max=\"10800\" />\n" +
            "    <DayTimeframe>\n" +
            "      <Start hour=\"10\" minute=\"0\" second=\"0\"/>\n" +
            "      <End hour=\"14\" minute=\"0\" second=\"0\"/>\n" +
            "      <DayOfWeek>6</DayOfWeek>\n" +
            "      <DayOfWeek>7</DayOfWeek>\n" +
            "    </DayTimeframe>\n" +
            "  </Schedule>\n" +
            "</Schedules>\n"
            ;

    private  final static String SCHEDULE_CONSECUTIVE_DAYS_TIMEFRAME =
            "<Schedules xmlns=\"" + ApplianceManager.SCHEMA_LOCATION + "\">\n" +
            "  <Schedule>\n" +
            "    <RuntimeRequest min=\"36000\" max=\"43200\" />\n" +
            "    <ConsecutiveDaysTimeframe>\n" +
            "      <Start dayOfWeek=\"5\" hour=\"16\" minute=\"0\" second=\"0\" />\n" +
            "      <End dayOfWeek=\"7\" hour=\"20\" minute=\"0\" second=\"0\" />\n" +
            "    </ConsecutiveDaysTimeframe>\n" +
            "  </Schedule>\n" +
            "</Schedules>\n"
            ;

    private final static String SCHEDULE_ENERGY_REQUEST_DAY_TIMEFRAME =
            "<Schedules xmlns=\"" + ApplianceManager.SCHEMA_LOCATION + "\">\n" +
            "  <Schedule>\n" +
            "    <EnergyRequest min=\"1380\" max=\"7360\" />\n" +
            "    <DayTimeframe>\n" +
            "      <Start hour=\"10\" minute=\"0\" second=\"0\"/>\n" +
            "      <End hour=\"14\" minute=\"0\" second=\"0\"/>\n" +
            "      <DayOfWeek>6</DayOfWeek>\n" +
            "      <DayOfWeek>7</DayOfWeek>\n" +
            "    </DayTimeframe>\n" +
            "  </Schedule>\n" +
            "</Schedules>\n"
            ;

    private MediaType contentType = new MediaType(MediaType.APPLICATION_XML.getType(),
            MediaType.APPLICATION_XML.getSubtype(),
            Charset.forName("utf8"));
    private LocalDateTime now;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() throws Exception {
        System.setProperty(FileHandler.SAE_HOME, System.getProperty("java.io.tmpdir"));
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.now = LocalDateTime.now();

        Logger root = (Logger) LoggerFactory.getLogger("de.avanux");
        root.setLevel(Level.DEBUG);
    }

    @Test
    public void setSchedules_RuntimeRequest_DayTimeframe() throws Exception {
        Appliance appliance = runTest(SaeController.SCHEDULES_URL, SCHEDULE_RUNTIME_REQUEST_DAY_TIMEFRAME);

        List<Schedule> schedules = appliance.getSchedules();
        assertEquals(1, schedules.size());
        Schedule schedule = schedules.get(0);

        assertEquals(7200, schedule.getRequest().getMin(now).intValue());
        assertEquals(10800, schedule.getRequest().getMax(now).intValue());

        Timeframe timeframe = schedule.getTimeframe();
        assertTrue(timeframe instanceof DayTimeframe);
        DayTimeframe dayTimeframe = (DayTimeframe) timeframe;

        assertTrue(schedule.getRequest() instanceof RuntimeRequest);
        assertEquals(new TimeOfDay(10, 0 ,0), dayTimeframe.getStart());
        assertEquals(new TimeOfDay(14, 0 ,0), dayTimeframe.getEnd());

        List<Integer> daysOfWeekValues = dayTimeframe.getDaysOfWeekValues();
        assertEquals(2, daysOfWeekValues.size());
        assertEquals(6, daysOfWeekValues.get(0).intValue());
        assertEquals(7, daysOfWeekValues.get(1).intValue());
    }

    @Test
    public void setSchedules_RuntimeRequest_ConsecutiveDaysTimeframe() throws Exception {
        Appliance appliance = runTest(SaeController.SCHEDULES_URL, SCHEDULE_CONSECUTIVE_DAYS_TIMEFRAME);

        List<Schedule> schedules = appliance.getSchedules();
        assertEquals(1, schedules.size());
        Schedule schedule = schedules.get(0);

        assertTrue(schedule.getRequest() instanceof RuntimeRequest);
        assertEquals(36000, schedule.getRequest().getMin(now).intValue());
        assertEquals(43200, schedule.getRequest().getMax(now).intValue());

        Timeframe timeframe = schedule.getTimeframe();
        assertTrue(timeframe instanceof ConsecutiveDaysTimeframe);

        ConsecutiveDaysTimeframe consecutiveDaysTimeframe = (ConsecutiveDaysTimeframe) timeframe;
        assertEquals(new TimeOfDayOfWeek(5, 16, 0, 0), consecutiveDaysTimeframe.getStart());
        assertEquals(new TimeOfDayOfWeek(7, 20, 0, 0), consecutiveDaysTimeframe.getEnd());
    }

    @Test
    public void setSchedules_EnergyRequest_DayTimeframe() throws Exception {
        Appliance appliance = runTest(SaeController.SCHEDULES_URL, SCHEDULE_ENERGY_REQUEST_DAY_TIMEFRAME);

        List<Schedule> schedules = appliance.getSchedules();
        assertEquals(1, schedules.size());
        Schedule schedule = schedules.get(0);

        assertTrue(schedule.getRequest() instanceof EnergyRequest);
        assertEquals(1380, schedule.getRequest().getMin(now).intValue());
        assertEquals(7360, schedule.getRequest().getMax(now).intValue());

        Timeframe timeframe = schedule.getTimeframe();
        assertTrue(timeframe instanceof DayTimeframe);
        DayTimeframe dayTimeframe = (DayTimeframe) timeframe;

        assertEquals(new TimeOfDay(10, 0 ,0), dayTimeframe.getStart());
        assertEquals(new TimeOfDay(14, 0 ,0), dayTimeframe.getEnd());

        List<Integer> daysOfWeekValues = dayTimeframe.getDaysOfWeekValues();
        assertEquals(2, daysOfWeekValues.size());
        assertEquals(6, daysOfWeekValues.get(0).intValue());
        assertEquals(7, daysOfWeekValues.get(1).intValue());
    }

    private Appliance runTest(String url, String content) throws Exception {
        String applianceId = "F-00000001-000000000001-00";
        Appliance appliance = new ApplianceBuilder(applianceId)
                .build(true);

        this.mockMvc.perform(post(url)
                .param("id", applianceId)
                .contentType(contentType)
                .content(content))
                .andExpect(status().isOk());

        return appliance;
    }
}
