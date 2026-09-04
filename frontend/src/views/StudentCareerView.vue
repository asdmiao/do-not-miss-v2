<script setup lang="ts">
import { FileText, LoaderCircle, Sparkles } from "@lucide/vue";
import { onMounted, ref } from "vue";
import { careerApi } from "@/api/career";
import type { CandidateEvidence, CandidateProfile, ResumeVersion } from "@/types/career";

const content = ref("");
const versions = ref<ResumeVersion[]>([]);
const selected = ref<ResumeVersion | null>(null);
const profile = ref<CandidateProfile | null>(null);
const evidences = ref<CandidateEvidence[]>([]);
const busy = ref(false);
const message = ref("");

onMounted(load);
async function load() { try { versions.value = await careerApi.versions(); selected.value = versions.value[0] ?? null; if (selected.value) await loadOutput(); } catch { message.value = "简历版本加载失败"; } }
async function createAndParse() { if (!content.value.trim() || busy.value) return; busy.value = true; message.value = ""; try { const version = await careerApi.create(content.value); versions.value = [version, ...versions.value]; selected.value = version; profile.value = await careerApi.parse(version.id); evidences.value = await careerApi.evidence(version.id); message.value = "已创建版本并完成解析"; } catch (error) { message.value = error instanceof Error ? error.message : "简历解析失败"; } finally { busy.value = false; } }
async function choose(version: ResumeVersion) { selected.value = version; await loadOutput(); }
async function loadOutput() { if (!selected.value) return; profile.value = null; evidences.value = []; if (selected.value.parseStatus !== "PARSED") return; try { [profile.value, evidences.value] = await Promise.all([careerApi.profile(selected.value.id), careerApi.evidence(selected.value.id)]); } catch { message.value = "候选人画像读取失败"; } }
function shortId(value: string) { return value.slice(0, 8); }
function shortHash(value: string) { return value.slice(0, 12); }
function formatCreatedAt(value: string) { return new Date(value).toLocaleString(); }
</script>

<template>
  <section class="career-workspace">
    <header class="module-page-heading"><div><p class="eyebrow">Candidate Profile</p><h2>简历解析与候选人画像</h2><p>文本简历会生成不可变版本，并输出可追溯的技能证据。</p></div></header>
    <section class="career-grid">
      <article class="career-input"><h3><FileText :size="18" /> 输入简历文本</h3><textarea v-model="content" rows="14" maxlength="50000" placeholder="粘贴纯文本简历。当前阶段不处理 PDF、DOCX 或 OCR。"></textarea><button type="button" :disabled="busy || !content.trim()" @click="createAndParse"><LoaderCircle v-if="busy" class="spin" :size="17" /><Sparkles v-else :size="17" />{{ busy ? "解析中" : "创建版本并解析" }}</button><p v-if="message" class="module-error">{{ message }}</p></article>
      <article class="career-versions"><h3>简历版本</h3><button v-for="version in versions" :key="version.id" type="button" :class="{ 'is-active': selected?.id === version.id }" @click="choose(version)"><strong>Resume · v{{ version.version }}</strong><span>{{ version.parseStatus }} · ID {{ shortId(version.resumeId) }}</span><small>Hash {{ shortHash(version.contentHash) }}… · {{ formatCreatedAt(version.createdAt) }}</small></button><p v-if="!versions.length">还没有简历版本。</p></article>
    </section>
    <section v-if="profile" class="career-output"><header><div><p class="eyebrow">Structured Profile</p><h3>Candidate Profile v{{ profile.profileVersion }}</h3></div><small>{{ profile.schemaVersion }}</small></header><div class="career-profile-columns"><article><h4>技能</h4><span v-for="skill in profile.profile.skills" :key="skill.name">{{ skill.name }} · {{ skill.level }}</span></article><article><h4>项目</h4><div v-for="project in profile.profile.projects" :key="project.id"><strong>{{ project.name }}</strong><p>{{ project.summary }}</p></div></article><article><h4>工作经历</h4><div v-for="work in profile.profile.workExperience" :key="work.id"><strong>{{ work.role }}</strong><p>{{ work.summary }}</p></div></article></div></section>
    <section v-if="selected?.parseStatus === 'PARSED'" class="career-output"><header><div><p class="eyebrow">Evidence</p><h3>可追溯候选人证据</h3></div><small>{{ evidences.length }} 条</small></header><div v-if="!evidences.length" class="module-empty">当前简历尚未提取到可支撑的项目、工作或成就证据。</div><article v-for="evidence in evidences" :key="evidence.id" class="career-evidence"><div><strong>{{ evidence.skillCode }}</strong><span>{{ evidence.sourceReference }} · {{ evidence.sourceId }}</span></div><p>{{ evidence.claim }}</p><small>{{ evidence.evidenceText }}</small></article></section>
  </section>
</template>
