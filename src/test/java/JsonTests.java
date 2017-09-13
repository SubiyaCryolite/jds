import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AddressBook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonTests {
    @Test
    public void serialization() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String output = om.writeValueAsString(new AddressBook());
        Assertions.assertNotNull(output, "Something went bonkers");
    }
}
