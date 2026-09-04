package com.donotmiss.backend.aiapplication.career;
import com.donotmiss.backend.aiapplication.capability.*; import com.fasterxml.jackson.core.type.TypeReference; import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.stereotype.Service; import java.util.*;
@Service public class PersistentSkillDictionary implements SkillDictionary{
 private final SkillDictionaryEntityRepository repository; private final ObjectMapper mapper;
 public PersistentSkillDictionary(SkillDictionaryEntityRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
 public Optional<SkillDefinition> findByCode(String code,String version){return repository.findBySkillCodeAndVersionAndActiveTrue(code,version).map(this::definition);}
 public Optional<SkillDefinition> resolveAlias(String text,String version){if(text==null||text.isBlank())return Optional.empty();String n=normalize(text);return repository.findByVersionAndActiveTrue(version).stream().filter(e->n.equals(normalize(e.getSkillCode()))||n.equals(normalize(e.getName()))||aliases(e).stream().anyMatch(a->n.equals(normalize(a)))).findFirst().map(this::definition);}
 private SkillDefinition definition(SkillDictionaryEntity e){return new SkillDefinition(e.getSkillCode(),e.getName(),e.getCategory(),e.getVersion(),aliases(e),new SkillRubric(e.getVersion(),List.of(),List.of(),List.of()));}
 private List<String> aliases(SkillDictionaryEntity e){try{return mapper.readValue(e.getAliasesJson(),new TypeReference<List<String>>(){});}catch(Exception ex){return List.of();}}
 private String normalize(String v){return v.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\p{IsHan}]","");}
}
