package com.donotmiss.backend.aiapplication.career.gap;
import java.util.Locale;
/** Stable ordering for explicitly supplied evidence/requirement levels. Unspecified is never treated as sufficient. */
public enum CapabilityLevel {
 UNKNOWN(0), JUNIOR(1), MID(2), SENIOR(3);
 private final int rank; CapabilityLevel(int rank){this.rank=rank;} public int rank(){return rank;}
 public static CapabilityLevel from(String value){if(value==null||value.isBlank())return UNKNOWN;String n=value.trim().toLowerCase(Locale.ROOT);if(n.matches(".*(senior|expert|高级|资深).*"))return SENIOR;if(n.matches(".*(mid|intermediate|中级).*"))return MID;if(n.matches(".*(junior|basic|初级|入门).*"))return JUNIOR;return UNKNOWN;}
}
