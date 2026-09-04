package com.donotmiss.backend.aiapplication.retrieval.job;

import com.donotmiss.backend.ai.OpenAiCompatibleLlmClient;
import com.donotmiss.backend.aiapplication.career.job.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import java.time.Instant;
import java.util.*;

/** Isolated job index. It deliberately has no dependency on Event, Activity, or HybridEventRetrievalService. */
@Service
public class JobRequirementSearchIndexService {
    private static final Logger log=LoggerFactory.getLogger(JobRequirementSearchIndexService.class);
    private final OpenAiCompatibleLlmClient embeddingClient; private final RestClient restClient; private final boolean enabled; private final boolean vectorEnabled; private final int dimensions; private final String indexName;
    public JobRequirementSearchIndexService(OpenAiCompatibleLlmClient embeddingClient,
                                            @Value("${app.job-search.enabled:false}") boolean enabled,
                                            @Value("${app.job-search.vector-enabled:false}") boolean vectorEnabled,
                                            @Value("${app.ai.embedding-dimensions:1024}") int dimensions,
                                            @Value("${app.search.base-url:http://localhost:9200}") String baseUrl,
                                            @Value("${app.job-search.index-name:do_not_miss_job_requirements}") String indexName) {
        this.embeddingClient=embeddingClient; this.enabled=enabled; this.vectorEnabled=vectorEnabled; this.dimensions=Math.max(1,dimensions); this.indexName=indexName;
        this.restClient=RestClient.builder().baseUrl(trimSlash(baseUrl)).build();
    }
    public boolean isEnabled(){return enabled;} public boolean isVectorEnabled(){return enabled&&vectorEnabled&&embeddingClient.isEnabled();} public String indexName(){return indexName;}
    public void index(JobRequirementChunkEntity chunk,List<String> skillCodes) {
        if(chunk==null||chunk.getId()==null)return;
        if(!enabled){chunk.setIndexStatus(JobRequirementChunkIndexStatus.SKIPPED);return;}
        try { ensureIndex(); Map<String,Object> doc=document(chunk,skillCodes); restClient.put().uri("/{index}/_doc/{id}",indexName,chunk.getId()).contentType(MediaType.APPLICATION_JSON).body(doc).retrieve().toBodilessEntity(); chunk.setIndexStatus(JobRequirementChunkIndexStatus.INDEXED); chunk.setIndexedAt(Instant.now()); if(doc.get("embedding") instanceof List<?> vector){chunk.setEmbeddingModel(embeddingClient.embeddingModeLabel());chunk.setEmbeddingDimensions(vector.size());} }
        catch(RestClientException ex){chunk.setIndexStatus(JobRequirementChunkIndexStatus.FAILED);log.warn("Job chunk index failed. chunkId={}, reason={}",chunk.getId(),ex.getMessage());}
    }
    public List<RankedJobChunk> bm25Search(Long versionId,String query,String section,String skillCode,int limit){
        if(!enabled||versionId==null)return List.of(); try{ensureIndex();JsonNode response=restClient.post().uri("/{index}/_search",indexName).contentType(MediaType.APPLICATION_JSON).body(bm25Body(versionId,query,section,skillCode,limit)).retrieve().body(JsonNode.class);return hits(response);}catch(RestClientException ex){log.warn("Job BM25 retrieval unavailable. versionId={}, reason={}",versionId,ex.getMessage());return List.of();}
    }
    public List<RankedJobChunk> vectorSearch(Long versionId,String query,String section,String skillCode,int limit){
        if(!isVectorEnabled()||query==null||query.isBlank())return List.of();
        return embeddingClient.embedding(query).filter(this::dimensionsMatch).map(vector->vectorSearch(versionId,vector,section,skillCode,limit)).orElseGet(List::of);
    }
    private List<RankedJobChunk> vectorSearch(Long versionId,List<Double> vector,String section,String skillCode,int limit){try{ensureIndex();JsonNode response=restClient.post().uri("/{index}/_search",indexName).contentType(MediaType.APPLICATION_JSON).body(vectorBody(versionId,vector,section,skillCode,limit)).retrieve().body(JsonNode.class);return hits(response);}catch(RestClientException ex){log.warn("Job vector retrieval skipped. versionId={}, reason={}",versionId,ex.getMessage());return List.of();}}
    private Map<String,Object> document(JobRequirementChunkEntity chunk,List<String> skillCodes){Map<String,Object>d=new LinkedHashMap<>();d.put("chunkId",chunk.getId());d.put("jobRequirementVersionId",chunk.getJobRequirementVersionId());d.put("section",chunk.getSection().name());d.put("position",chunk.getPosition());d.put("content",chunk.getContent());d.put("contentHash",chunk.getContentHash());d.put("skillCodes",skillCodes==null?List.of():skillCodes);d.put("allText",chunk.getSection().name()+" "+chunk.getContent()+" "+String.join(" ",skillCodes==null?List.of():skillCodes));if(isVectorEnabled())embeddingClient.embedding(chunk.getContent()).filter(this::dimensionsMatch).ifPresent(v->d.put("embedding",v));return d;}
    private Map<String,Object> bm25Body(Long versionId,String query,String section,String skillCode,int limit){List<Map<String,Object>>filter=filters(versionId,section,skillCode);Map<String,Object>bool=new LinkedHashMap<>();bool.put("filter",filter);bool.put("must",query==null||query.isBlank()?List.of(Map.of("match_all",Map.of())):List.of(Map.of("multi_match",Map.of("query",query.trim(),"fields",List.of("content^4","allText^2","skillCodes^3","section^2"),"type","best_fields","operator","or","minimum_should_match","20%"))));return Map.of("size",bounded(limit),"query",Map.of("bool",bool));}
    private Map<String,Object> vectorBody(Long versionId,List<Double>vector,String section,String skillCode,int limit){Map<String,Object>filter=Map.of("bool",Map.of("filter",filters(versionId,section,skillCode)));Map<String,Object>knn=new LinkedHashMap<>();knn.put("embedding",Map.of("vector",vector,"k",bounded(limit),"filter",filter));return Map.of("size",bounded(limit),"query",Map.of("knn",knn));}
    private List<Map<String,Object>> filters(Long versionId,String section,String skillCode){List<Map<String,Object>>filters=new ArrayList<>();filters.add(Map.of("term",Map.of("jobRequirementVersionId",versionId)));if(section!=null&&!section.isBlank())filters.add(Map.of("term",Map.of("section",section.trim().toUpperCase(Locale.ROOT))));if(skillCode!=null&&!skillCode.isBlank())filters.add(Map.of("term",Map.of("skillCodes",skillCode.trim().toUpperCase(Locale.ROOT))));return filters;}
    private List<RankedJobChunk> hits(JsonNode response){List<RankedJobChunk> result=new ArrayList<>();JsonNode hits=response==null?null:response.path("hits").path("hits");if(hits!=null&&hits.isArray())for(JsonNode hit:hits){long id=hit.path("_source").path("chunkId").asLong(0);if(id>0)result.add(new RankedJobChunk(id,hit.path("_score").asDouble(0)));}return result;}
    private void ensureIndex(){try{restClient.head().uri("/{index}",indexName).retrieve().toBodilessEntity();}catch(RestClientException ex){createIndex();}}
    private void createIndex(){Map<String,Object>properties=new LinkedHashMap<>();properties.put("chunkId",Map.of("type","long"));properties.put("jobRequirementVersionId",Map.of("type","long"));properties.put("section",Map.of("type","keyword"));properties.put("position",Map.of("type","integer"));properties.put("content",textField());properties.put("contentHash",Map.of("type","keyword"));properties.put("skillCodes",Map.of("type","keyword"));properties.put("allText",textField());if(isVectorEnabled())properties.put("embedding",Map.of("type","knn_vector","dimension",dimensions,"method",Map.of("name","hnsw","space_type","cosinesimil","engine","lucene")));Map<String,Object>indexSettings=new LinkedHashMap<>();indexSettings.put("max_ngram_diff",2);if(isVectorEnabled())indexSettings.put("knn",true);Map<String,Object>settings=Map.of("index",indexSettings,"analysis",Map.of("tokenizer",Map.of("cjk_ngram_tokenizer",Map.of("type","ngram","min_gram",1,"max_gram",3,"token_chars",List.of("letter","digit"))),"analyzer",Map.of("cjk_ngram",Map.of("type","custom","tokenizer","cjk_ngram_tokenizer","filter",List.of("lowercase")))));restClient.put().uri("/{index}",indexName).header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE).body(Map.of("settings",settings,"mappings",Map.of("properties",properties))).retrieve().toBodilessEntity();}
    private Map<String,Object> textField(){return Map.of("type","text","analyzer","cjk_ngram","search_analyzer","cjk");} private boolean dimensionsMatch(List<Double>v){return v.size()==dimensions;} private int bounded(int n){return Math.min(Math.max(1,n),100);} private String trimSlash(String value){String v=value==null||value.isBlank()?"http://localhost:9200":value.trim();while(v.endsWith("/"))v=v.substring(0,v.length()-1);return v;}
}
