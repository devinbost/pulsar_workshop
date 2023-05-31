import static org.mockito.Mockito.mock;
import com.example.pulsarworkshop.DeviceTS;
import com.example.pulsarworkshop.DeviceTSList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFunction {

    private List<DeviceTS> collection;
    @Test
    public void testAccumulator(){
        this.collection = new ArrayList<DeviceTS>();
        //when(mockContext.newOutputMessage(topic, schema))
        var mockdevice = new DeviceTS();
        var result = accumulate(mockdevice);
        Assertions.assertEquals(1, this.collection.size());
        Assertions.assertNull(result);
        for (int i = 0; i < 99; i++) {
            result = accumulate(mockdevice);
            Assertions.assertNull(result);
        }
        Assertions.assertEquals(100, collection.size());
        result = accumulate(mockdevice);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, collection.size());
    }
    public DeviceTSList accumulate(DeviceTS input){
        collection.add(input);
        if (collection.size() > 100) {
            var list = new DeviceTSList();
            list.setDeviceTSArray(collection);
            collection.clear();
            return list;
        }
        return null;
    }
}
