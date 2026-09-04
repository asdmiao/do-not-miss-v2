package com.donotmiss.backend.aiapplication.career.job;

import com.donotmiss.backend.aiapplication.async.*;
import java.time.Instant;

/** Message payload contract for a future Outbox -> RabbitMQ consumer; Phase 2B dispatch remains synchronous. */
public final class JobRequirementAsyncCommands {
    private static final String CONTRACT_VERSION = "job-retrieval-v1";
    private JobRequirementAsyncCommands() { }
    public static AsyncTaskCommand indexing(JobRequirementVersionEntity version) {
        String key="JOB_REQUIREMENT_INDEX:"+version.getId()+":"+version.getContentHash()+":"+CONTRACT_VERSION;
        return new AsyncTaskCommand(AsyncTaskType.JOB_REQUIREMENT_INDEX,key,"JOB_REQUIREMENT_VERSION",String.valueOf(version.getId()),version.getContentHash(),CONTRACT_VERSION,Instant.now());
    }
}
