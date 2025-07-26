package Team3rd.DaeCar.DaeCar.domain.student.util;

import java.util.HashMap;
import java.util.Map;

public class SchoolDomainMapper {

    private static final Map<String, String> domainToSchool = new HashMap<>();
    private static final Map<String, String> schoolToDomain = new HashMap<>();

    static {
        domainToSchool.put("smail.kongju.ac.kr", "공주대학교");
        domainToSchool.put("bu.ac.kr", "백석대학교");
        domainToSchool.put("sangmyung.kr", "상명대학교");
        domainToSchool.put("vision.hoseo.edu", "호서대학교");
        domainToSchool.put("dankook.ac.kr", "단국대학교");

    }

    public static String getSchoolByEmail(String email) {
        if (email == null || !email.contains("@")) return null;

        String domain = email.substring(email.indexOf("@") + 1);
        return domainToSchool.get(domain);
    }

    public static String getDomainBySchool(String school) {
        return schoolToDomain.get(school);
    }

    public static boolean isSupported(String email) {
        return getSchoolByEmail(email) != null;
    }
}