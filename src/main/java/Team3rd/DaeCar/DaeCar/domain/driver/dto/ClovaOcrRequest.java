package Team3rd.DaeCar.DaeCar.domain.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClovaOcrRequest {
    private String templateId;
    private List<Map<String, String>> images;
}