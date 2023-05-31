import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.example.pulsarworkshop.DeviceTS;
import com.example.pulsarworkshop.DeviceTSList;

public class GroupTest {
    @Test
    public void testAccumulator(){
        var device1 = DeviceTS
                .newBuilder()
                .setTagId("1")
                .setDataQuality(10)
                .setEventTime("2023-05-24T00:00:00Z")
                .setEventValue(19.8)
                .build();
        var device2 = DeviceTS
                .newBuilder()
                .setTagId("2")
                .setDataQuality(10)
                .setEventTime("2023-05-24T00:00:00Z")
                .setEventValue(19.8)
                .build();
        var device3 = DeviceTS
                .newBuilder()
                .setTagId("1")
                .setDataQuality(15)
                .setEventTime("2023-05-24T00:01:00Z")
                .setEventValue(22.5)
                .build();
        var device4 = DeviceTS
                .newBuilder()
                .setTagId("5")
                .setDataQuality(15)
                .setEventTime("2023-05-24T00:01:00Z")
                .setEventValue(22.5)
                .build();
        var device5 = DeviceTS
                .newBuilder()
                .setTagId("2")
                .setDataQuality(5)
                .setEventTime("2023-05-23T00:01:00Z")
                .setEventValue(22.5)
                .build();
        var deviceList = new DeviceTSList();
        var mylist = new ArrayList<DeviceTS>();
        mylist.add(device1);
        mylist.add(device2);
        mylist.add(device3);
        mylist.add(device4);
        mylist.add(device5);
        deviceList.setDeviceTSArray(mylist);

        Map<CharSequence, Optional<DeviceTS>> result = deviceList.getDeviceTSArray().stream()
                .collect(Collectors.groupingBy(DeviceTS::getTagId,
                        Collectors.maxBy(Comparator.comparing(o -> Instant.parse(o.getEventTime().toString())))));
        List<DeviceTS> flatList = result.values().stream()
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
        Assertions.assertEquals(3, flatList.size());
        var hasMostRecentTagOf1 = flatList.stream()
                .anyMatch(t -> t.getTagId()=="1" && t.getEventTime() == "2023-05-24T00:01:00Z");
        Assertions.assertTrue(hasMostRecentTagOf1);

        var hasMostRecentTagOf2 = flatList.stream()
                .anyMatch(t -> t.getTagId()=="2" && t.getEventTime() == "2023-05-24T00:00:00Z");
        Assertions.assertTrue(hasMostRecentTagOf2);

        var hasMostRecentTagOf5 = flatList.stream()
                .anyMatch(t -> t.getTagId()=="5" && t.getEventTime() == "2023-05-24T00:01:00Z");
        Assertions.assertTrue(hasMostRecentTagOf5);

    }
}
