package Team3rd.DaeCar.DaeCar.domain.student.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class SchoolDomainMapper {

    private static final Map<String, String> domainToSchool = new HashMap<>();
    private static final Map<String, String> schoolToDomain = new HashMap<>();

    static {
        domainToSchool.put("smail.kongju.ac.kr", "공주대학교");
        domainToSchool.put("bu.ac.kr", "백석대학교");
        domainToSchool.put("sangmyung.kr", "상명대학교");
        domainToSchool.put("vision.hoseo.edu", "호서대학교");
        domainToSchool.put("dankook.ac.kr", "단국대학교");
        
        schoolToDomain.put("공주대학교", "smail.kongju.ac.kr");
        schoolToDomain.put("백석대학교", "bu.ac.kr");
        schoolToDomain.put("상명대학교", "sangmyung.kr");
        schoolToDomain.put("호서대학교", "vision.hoseo.edu");
        schoolToDomain.put("단국대학교", "dankook.ac.kr");
    }

    public String getSchoolByEmail(String email) {
        if (email == null || !email.contains("@")) return null;

        String domain = email.substring(email.indexOf("@") + 1);
        return domainToSchool.get(domain);
    }

    public String getDomainBySchool(String school) {
        return schoolToDomain.get(school);
    }

    public boolean isSupported(String email) {
        return getSchoolByEmail(email) != null;
    }
    
    public boolean isValidEmailForUniversity(String email, String universityName) {
        if (email == null || universityName == null) {
            return false;
        }
        
        String expectedDomain = getDomainBySchool(universityName);
        if (expectedDomain == null) {
            return false;
        }
        
        String domain = email.substring(email.indexOf("@") + 1);
        return expectedDomain.equals(domain);
    }
}