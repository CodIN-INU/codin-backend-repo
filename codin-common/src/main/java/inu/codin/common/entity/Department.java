package inu.codin.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum Department {

    // ------------------------
    // 공통/기타
    // ------------------------
    STAFF("교직원", "교직원"),
    LIBERAL("교양", "교양"),
    TEACHING("교직", "교직"),
    GENERAL_ELECTIVE("일선", "일선"),
    OTHERS("타과대", "타대"),

    // ------------------------
    // 법학
    // ------------------------
    LAW("법학부", "법학"),

    // ------------------------
    // 융합자유전공 / IBE / 동북아
    // ------------------------
    FREE_MAJOR("자유전공학부", "자전"),
    IBE("IBE전공", "IBE"),
    SMART_LOGISTICS_ENGINEERING("스마트물류공학전공", "스물"),
    NORTHEAST_ASIA_TRADE("동북아국제통상전공", "동북"),

    // ------------------------
    // 생명과학기술대학 계열
    // ------------------------
    BIO_ENGINEERING_MAJOR("생명공학전공", "생공"),
    BIO_ENGINEERING("생명공학부", "생공"),
    MOLECULAR_LIFE_SCIENCE("분자의생명전공", "분생"),
    LIFE_SCIENCE_MAJOR("생명과학전공", "생과"),
    LIFE_SCIENCE("생명과학부", "생과"),
    NANO_BIO_ENGINEERING("나노바이오공학전공", "나바"),

    // ------------------------
    // 도시과학대학 계열
    // ------------------------
    URBAN_ARCHITECTURE_MAJOR("도시건축학전공", "도건"),
    ARCHITECTURAL_ENGINEERING_MAJOR("건축공학전공", "건공"),
    URBAN_ARCHITECTURE("도시건축학부", "도건"),
    URBAN_ENGINEERING("도시공학과", "도공"),
    ENVIRONMENTAL_ENGINEERING_MAJOR("환경공학전공", "환공"),
    CIVIL_ENVIRONMENTAL_ENGINEERING_MAJOR("건설환경공학전공", "건환"),
    URBAN_ENVIRONMENTAL_ENGINEERING("도시환경공학부", "도환"),
    URBAN_ADMINISTRATION("도시행정학과", "도행"),

    // ------------------------
    // 사범대학
    // ------------------------
    ETHICS_EDUCATION("윤리교육과", "윤교"),
    HISTORY_EDUCATION("역사교육과", "역교"),
    EARLY_CHILDHOOD_EDUCATION("유아교육과", "유교"),
    PHYSICAL_EDUCATION("체육교육과", "체교"),
    MATHEMATICS_EDUCATION("수학교육과", "수교"),
    JAPANESE_EDUCATION("일어교육과", "일교"),
    ENGLISH_EDUCATION("영어교육과", "영교"),
    KOREAN_EDUCATION("국어교육과", "국교"),

    // ------------------------
    // 예술체육대학/예체능 계열
    // ------------------------
    EXERCISE_HEALTH("운동건강학부", "운건"),
    SPORTS_SCIENCE("스포츠과학부", "스과"),
    PERFORMING_ARTS("공연예술학과", "공연"),
    DESIGN("디자인학부", "디자"),
    WESTERN_PAINTING("서양화전공", "서양"),
    KOREAN_PAINTING("한국화전공", "한국"),
    FINE_ARTS("조형예술학부", "조형"),

    // ------------------------
    // 경영대학/경영계열
    // ------------------------
    TAX_ACCOUNTING("세무회계학과", "세무"),
    DATA_SCIENCE("데이터과학과", "데과"),
    BUSINESS_ADMIN("경영학부", "경영"),

    // ------------------------
    // 정보기술대학
    // ------------------------
    COMPUTER_SCI("컴퓨터공학부", "컴공"),
    COMPUTER_SCI_NIGHT("컴퓨터공학부(야)", "컴공"),
    INFO_COMM("정보통신공학과", "정통"),
    EMBEDDED("임베디드시스템공학과", "임베"),

    // ------------------------
    // 공과대학(일부)
    // ------------------------
    BIO_ROBOT_SYSTEM_ENGINEERING("바이오-로봇시스템공학과", "바로"),
    ENERGY_CHEMICAL_ENGINEERING("에너지화학공학과", "에화"),
    SAFETY_ENGINEERING("안전공학과", "안전"),
    MATERIALS_SCIENCE_ENGINEERING("신소재공학과", "신소"),
    INDUSTRIAL_MANAGEMENT_ENGINEERING("산업경영공학과", "산경"),
    ELECTRONICS_ENGINEERING_MAJOR("전자공학전공", "전자"),
    ELECTRONICS_ENGINEERING("전자공학부", "전자"),
    ELECTRONICS_ENGINEERING_DEPT("전자공학과", "전자"),
    ELECTRICAL_ENGINEERING("전기공학과", "전기"),
    MECHANICAL_ENGINEERING("기계공학과", "기계"),

    // ------------------------
    // 글로벌정경/사회과학/인문/자연 일부
    // ------------------------
    CONSUMER_SCIENCE("소비자학과", "소비"),
    TRADE_NIGHT("무역학부(야)", "무역"),
    TRADE("무역학부", "무역"),
    ECONOMICS_NIGHT("경제학과(야)", "경제"),
    ECONOMICS("경제학과", "경제"),
    POLITICAL_SCIENCE_DIPLOMACY("정치외교학과", "정외"),
    PUBLIC_ADMINISTRATION("행정학과", "행정"),
    CREATIVE_HUMAN_RESOURCE_DEV("창의인재개발학과", "창인"),
    LIBRARY_INFORMATION_SCIENCE("문헌정보학과", "문정"),
    MEDIA_COMMUNICATION("미디어커뮤니케이션학과", "미컴"),
    SOCIAL_WELFARE("사회복지학과", "사복"),
    OCEAN_SCIENCE("해양학과", "해양"),
    FASHION_INDUSTRY("패션산업학과", "패션"),
    CHEMISTRY("화학과", "화학"),
    PHYSICS("물리학과", "물리"),
    MATHEMATICS("수학과", "수학"),
    CHINESE_LANGUAGE_LITERATURE("중어중국학과", "중문"),
    JAPANESE_CULTURE("일본지역문화학과", "일문"),
    FRENCH_LANGUAGE_LITERATURE("불어불문학과", "불문"),
    GERMAN_LANGUAGE_LITERATURE("독어독문학과", "독문"),
    ENGLISH_LANGUAGE_LITERATURE("영어영문학과", "영문"),
    KOREAN_LANGUAGE_LITERATURE("국어국문학과", "국문");

    private final String description;
    private final String abbreviation;

    @JsonCreator
    public static Department fromDescription(String description) {
        if (description == null) return OTHERS;
        String v = description.trim();
        if (v.isEmpty()) return OTHERS;

        // 공백 정규화
        v = v.replace('\u00A0', ' ');
        v = v.replaceAll("\\s+", " ");

        // 연계 전공 예외 처리
        if (v.contains("연계전공") || v.contains("(연계)") || v.contains("전공(연계)")) {
            return OTHERS;
        }

        for (Department d : Department.values()) {
            if (d.name().equalsIgnoreCase(v)) return d;
            if (d.getDescription().equals(v)) return d;
            if (d.getAbbreviation().equals(v)) return d;
        }

        log.warn("알 수 없는 학과/전공: {}", v);
        return OTHERS;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}
