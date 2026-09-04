package com.donotmiss.backend.aiapplication.career.gap;
import java.math.BigDecimal;
public record GapSemanticAssessment(boolean supported,EvidenceStrength evidenceStrength,BigDecimal confidence,String reason) { }
