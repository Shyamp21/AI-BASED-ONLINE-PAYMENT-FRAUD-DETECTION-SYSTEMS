import fs from 'node:fs/promises';
import path from 'node:path';
import { Presentation, PresentationFile } from 'file:///C:/Users/yadha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs';

const ROOT = process.cwd();
const BUILD = path.join(ROOT, '.codex-pptx-build');
const OUT = path.join(ROOT, 'presentation');
const W=1280,H=720;
const C={bg:'#071A2B',bg2:'#0B2438',panel:'#102D43',panel2:'#15374F',teal:'#36D6C5',blue:'#62A8FF',white:'#F4F8FC',muted:'#A9BBCB',orange:'#FFB66D',red:'#F27B82',line:'#2C4A60',green:'#6CE0B2'};
const deck=Presentation.create({slideSize:{width:W,height:H}});
function shape(s,geom,x,y,w,h,fill='none',lineFill='none',lineW=0,name=''){
 return s.shapes.add({geometry:geom,name:name||undefined,position:{left:x,top:y,width:w,height:h},fill,line:{style:'solid',fill:lineFill,width:lineW}});
}
function txt(s,text,x,y,w,h,size=24,color=C.white,bold=false,opts={}){
 const a=shape(s,'textbox',x,y,w,h,'none'); a.text=text; a.text.style={fontSize:size,typeface:'Arial',color,bold,alignment:opts.align||'left',verticalAlignment:opts.valign||'top',autoFit:'shrinkText',wrap:'square',insets:opts.insets??0}; return a;
}
function base(title,section,num){
 const s=deck.slides.add(); s.background.fill=C.bg;
 shape(s,'rect',0,0,12,H,C.teal);
 txt(s,section.toUpperCase(),64,32,500,24,15,C.teal,true);
 txt(s,title,64,70,1150,60,36,C.white,true);
 shape(s,'rect',64,143,1152,2,C.line);
 txt(s,'FRAUDGUARD AI  •  PROJECT PRESENTATION',64,681,770,20,13,C.muted,true);
 txt(s,String(num).padStart(2,'0'),1170,678,45,24,14,C.teal,true,{align:'right'});
 return s;
}
function bullets(s,items,x=76,y=178,w=700,size=24,gap=85,accent=C.teal){
 items.forEach((item,i)=>{shape(s,'ellipse',x,y+i*gap+9,12,12,accent);txt(s,item,x+28,y+i*gap,w-28,gap-8,size,C.white,false);});
}
function label(s,head,body,x,y,w,h,accent=C.teal,bodySize=20){
 shape(s,'rect',x,y,4,h,accent);
 txt(s,head,x+18,y,w-18,28,20,accent,true);
 txt(s,body,x+18,y+34,w-24,h-36,bodySize,C.white,false);
}
function notes(s,t){s.speakerNotes.text=t;}

// 1 Cover
{
 const s=deck.slides.add();s.background.fill=C.bg;
 shape(s,'rect',0,0,14,H,C.teal);
 txt(s,'FRAUDGUARD AI',76,66,620,28,18,C.teal,true);
 txt(s,'Online Payment Fraud\nDetection System',76,135,720,150,48,C.white,true);
 txt(s,'Ensemble machine learning with a secure, auditable full-stack platform',78,312,680,70,24,C.muted,false);
 shape(s,'rect',78,424,570,2,C.line);
 txt(s,'Project presentation',78,450,400,34,20,C.white,true);
 txt(s,'AI • PaySim • CatBoost + XGBoost • Soft Voting',78,492,570,42,18,C.muted,false);
 txt(s,'Authors listed in supplied brief: Yan Liu, Jing Li, Jeevanthi Srikanth, Hema Purnima Jethanil',78,598,760,45,16,C.muted,false);
 // editable abstract network motif
 shape(s,'ellipse',865,143,280,280,'none',C.line,2);shape(s,'ellipse',905,183,200,200,'none',C.teal,3);
 shape(s,'ellipse',966,244,78,78,C.panel2,C.blue,2);txt(s,'FG',966,260,78,40,25,C.white,true,{align:'center',valign:'middle'});
 [[884,269],[1098,180],[1110,362],[929,410],[1005,115]].forEach(([x,y],i)=>{shape(s,'ellipse',x,y,20,20,[C.teal,C.blue,C.orange,C.green,C.red][i]);});
 txt(s,'PROJECT OVERVIEW',865,470,320,24,15,C.teal,true,{align:'center'});
 notes(s,'Project details are based on the supplied workspace README and PROJECT_REPORT.md. Author names and journal details were supplied in the request; bibliographic accuracy should be checked before academic submission.');
}
// 2 Outline
{
 const s=base('Presentation outline','Roadmap',2);
 const items=['Problem Statement','Abstract','Introduction','Existing System','Proposed System','Architecture Diagram','Language & Software Requirements','Modules','Conclusion','References'];
 items.forEach((v,i)=>{const col=i<5?0:1,row=i%5,x=90+col*560,y=186+row*88;txt(s,String(i+1).padStart(2,'0'),x,y,48,30,18,C.teal,true);txt(s,v,x+58,y-1,450,40,23,C.white,false);shape(s,'rect',x+58,y+48,430,1,C.line);});
}
// 3 Problem
{
 const s=base('Problem statement','01 · Problem',3);
 txt(s,'Fraud patterns change faster than static controls.',76,183,700,52,28,C.white,true);
 bullets(s,['Rule-based checks are rigid and require constant manual updates.','Standalone classifiers can overfit and vary across changing transaction patterns.','Fraud is rare: the brief describes a class share below 0.15%.','False alarms burden investigators; missed fraud creates direct loss.'],80,260,690,22,78,C.orange);
 shape(s,'rect',850,194,330,380,C.panel);
 txt(s,'THE DATA CHALLENGE',880,222,270,30,17,C.teal,true);
 txt(s,'< 0.15%',880,276,270,72,42,C.white,true);
 txt(s,'fraudulent transactions in the supplied problem context',880,356,255,76,20,C.muted,false);
 shape(s,'rect',880,448,260,2,C.line);
 txt(s,'A detector must learn rare patterns while keeping evaluation leakage-free.',880,473,260,78,20,C.white,false);
 notes(s,'The class imbalance figure and problem framing are supplied in the project abstract.');
}
// 4 Abstract
{
 const s=base('Abstract','02 · Abstract',4);
 txt(s,'FraudGuard AI combines imbalance-aware training, two boosted-tree models, and secure transaction workflows.',76,180,1070,72,26,C.white,true);
 const cards=[['DATA','PaySim mobile payment benchmark'],['LEARNING','CatBoost + XGBoost probabilities'],['ENSEMBLE','Soft voting combines model scores'],['PLATFORM','Python inference + Java core + dashboard']];
 cards.forEach((c,i)=>{const x=76+i*285;shape(s,'rect',x,292,255,150,C.panel);txt(s,c[0],x+20,314,220,26,16,C.teal,true);txt(s,c[1],x+20,355,218,68,21,C.white,false);});
 txt(s,'Project-reported test metrics',76,490,400,29,20,C.muted,true);
 const ms=[['Accuracy','99.91%'],['Precision','98.92%'],['Recall','97.45%'],['F1','0.9818'],['ROC–AUC','0.9994']];
 ms.forEach((m,i)=>{const x=76+i*222;txt(s,m[0],x,535,190,24,16,C.muted,false);txt(s,m[1],x,563,200,38,26,C.teal,true);});
 txt(s,'Reported values are from the supplied project brief/report; validate against the saved evaluation output before defense.',76,620,1050,30,15,C.muted,false);
 notes(s,'Metrics are project-reported, not independently recalculated in this presentation. The brief states evaluation on an isolated test partition of PaySim.');
}
// 5 Intro
{
 const s=base('Introduction','03 · Context',5);
 txt(s,'Digital payments create speed and reach—and a larger attack surface.',76,184,1000,45,28,C.white,true);
 label(s,'PAYMENT FLOW','Transactions arrive continuously across mobile and online channels.',78,270,330,140,C.teal,21);
 label(s,'RISK SIGNALS','Amount, type, timing, and account balance changes can reveal suspicious behavior.',474,270,330,140,C.blue,21);
 label(s,'DECISION SUPPORT','Risk scores and explanations help analysts prioritize review.',870,270,330,140,C.orange,21);
 txt(s,'Project objective',78,482,250,26,18,C.teal,true);
 txt(s,'Detect suspicious payments in near real time, record decisions, and present understandable risk drivers to authorized users.',78,520,1050,72,26,C.white,false);
 txt(s,'PaySim is a synthetic mobile-money benchmark; results do not by themselves establish live-bank performance.',78,622,1050,30,16,C.muted,false);
}
// 6 Existing system
{
 const s=base('Existing system','04 · Baseline',6);
 const heads=['Rule-based controls','Single ML classifier','Fraud operations'];
 const bod=['Transparent rules are easy to explain, but need manual updates and can miss new patterns.','Can learn nonlinear relationships, yet remains sensitive to imbalance, overfitting, and threshold choice.','Analysts need practical prioritization, traceability, and enough context to review alerts.'];
 heads.forEach((h,i)=>label(s,h,bod[i],80+i*370,215,320,208,[C.orange,C.blue,C.teal][i],21));
 txt(s,'Design gap',80,484,180,25,18,C.teal,true);
 txt(s,'Join stronger detection with disciplined evaluation, secure access, audit history, and readable explanations.',80,523,1040,70,27,C.white,true);
 notes(s,'This slide summarizes the project report’s description of conventional rule systems, standalone classifiers, and analyst workflow needs.');
}
// 7 Proposed system
{
 const s=base('Proposed system','05 · Approach',7);
 txt(s,'A leakage-aware ensemble supports a complete review workflow.',76,180,1050,40,27,C.white,true);
 const steps=[['1','Split','Stratified train / test'],['2','Prepare','PCA/scaling fit on train'],['3','Balance','SMOTE on train only'],['4','Train','CatBoost + XGBoost'],['5','Combine','Soft voting of probabilities'],['6','Explain','Risk score + human-readable drivers']];
 steps.forEach((a,i)=>{const x=72+i*198;shape(s,'rect',x,282,172,210,C.panel);txt(s,a[0],x+18,302,34,36,24,C.teal,true);txt(s,a[1],x+18,353,140,30,20,C.white,true);txt(s,a[2],x+18,396,138,76,18,C.muted,false);if(i<5){shape(s,'rightArrow',x+176,365,22,28,C.teal);}});
 txt(s,'Inference path',76,546,180,25,18,C.teal,true);
 txt(s,'Transaction → model probabilities → ensemble score → thresholded alert → analyst review + audit record',76,585,1100,40,21,C.white,false);
 notes(s,'The project report describes train-only fitting and SMOTE on the training partition. It also lists soft voting with equal default weights and a calibrated threshold.');
}
// 8 Architecture diagram
{
 const s=base('System architecture','06 · Architecture',8);
 const blocks=[
  {x:62,w:230,title:'PRESENTATION',sub:'HTML5 · CSS3 · Vanilla JS\nChart.js dashboard',color:C.teal},
  {x:341,w:290,title:'CORE BACKEND',sub:'Java 21 · Spring Boot 3.x\nSecurity · JWT · RBAC · REST',color:C.blue},
  {x:690,w:262,title:'ML SERVICE',sub:'Python · Flask / FastAPI\nPreprocess · CatBoost · XGBoost\nSoft voting · explanations',color:C.orange},
  {x:1010,w:210,title:'DATA STORE',sub:'MySQL 8.x\nUsers · transactions\nalerts · audit logs',color:C.green}
 ];
 for(const b of blocks){shape(s,'rect',b.x,230,b.w,215,C.panel,b.color,2);txt(s,b.title,b.x+16,252,b.w-32,28,18,b.color,true);shape(s,'rect',b.x+16,292,b.w-32,2,C.line);txt(s,b.sub,b.x+16,317,b.w-32,105,20,C.white,false);}
 // editable directional arrows
 shape(s,'rightArrow',298,319,40,32,C.teal);shape(s,'rightArrow',638,319,48,32,C.blue);shape(s,'rightArrow',955,319,50,32,C.orange);
 shape(s,'rect',92,520,1090,74,C.bg2,C.line,1);txt(s,'REQUEST',114,543,150,24,16,C.teal,true);txt(s,'Score → save prediction → return risk and explanation → surface alert in dashboard',265,538,875,40,20,C.white,false);
 txt(s,'JWT-secured APIs  •  Role-based access  •  Audit logging',92,620,1020,26,18,C.muted,false);
 notes(s,'Architecture summarized from the workspace README and PROJECT_REPORT.md. The project describes the ML service as Flask/FastAPI and the Java service as Spring Boot with JWT/RBAC; persistence is MySQL with an embedded H2 option in local mode.');
}
// 9 Requirements
{
 const s=base('Language and software requirements','07 · Technology',9);
 const rows=[
 ['Frontend','HTML5 · CSS3 · Vanilla JavaScript · Chart.js'],
 ['Core backend','Java 21 · Spring Boot 3.x · Spring Security 6'],
 ['ML service','Python 3.11+ · Flask / FastAPI · CatBoost · XGBoost'],
 ['Data & persistence','PaySim CSV · MySQL 8.x (H2 local option)'],
 ['Build & API','Maven · Docker Compose · REST / JSON · JWT']
 ];
 rows.forEach((r,i)=>{const y=192+i*84;txt(s,r[0],84,y,238,32,20,C.teal,true);shape(s,'rect',320,y-2,2,54,C.line);txt(s,r[1],350,y,790,50,22,C.white,false);});
 txt(s,'Deployment split',84,625,220,25,17,C.muted,true);txt(s,'Python handles model inference; Java manages application workflows and security.',310,620,840,34,19,C.muted,false);
}
// 10 Modules
{
 const s=base('System modules','08 · Modules',10);
 const list=[
 ['Authentication & roles','JWT login, Spring Security, role checks'],
 ['Transaction intake','Single-entry scoring and CSV batch upload'],
 ['ML inference','Feature preparation, two models, soft-vote score'],
 ['Risk explanation','Risk level and natural-language driver summary'],
 ['Alerts & review','Fraud alert status and analyst remarks'],
 ['Dashboard & audit','Metrics, trends, prediction history, audit logs']
 ];
 list.forEach((r,i)=>{const col=i%2,row=Math.floor(i/2),x=82+col*560,y=185+row*150;txt(s,String(i+1).padStart(2,'0'),x,y,50,30,18,C.teal,true);txt(s,r[0],x+62,y,420,28,22,C.white,true);txt(s,r[1],x+62,y+42,430,56,18,C.muted,false);shape(s,'rect',x+62,y+113,425,1,C.line);});
 notes(s,'Module names and functions are drawn from the README and PROJECT_REPORT.md.');
}
// 11 Conclusion
{
 const s=base('Conclusion','09 · Summary',11);
 txt(s,'FraudGuard AI connects model scoring to a reviewable application workflow.',76,184,1080,52,29,C.white,true);
 bullets(s,['CatBoost and XGBoost contribute complementary fraud probabilities.','Train-only SMOTE and preprocessing preserve a separated test evaluation.','JWT/RBAC, persistence, alerts, and audit logs support operational review.','Human-readable drivers make risk scores easier to investigate.'],82,282,1000,23,72,C.teal);
 shape(s,'rect',78,592,1080,2,C.line);
 txt(s,'Next steps: reproduce the reported metrics, verify the cited paper details, and assess drift and real-world generalization.',78,618,1080,42,18,C.muted,false);
 notes(s,'The project is intended as an analytical aid. The report explicitly describes analyst review and notes synthetic-data and explainability limitations.');
}
// 12 References
{
 const s=base('References','10 · Sources',12);
 const refs=[
 'Chen, T. & Guestrin, C. (2016). XGBoost: A Scalable Tree Boosting System. KDD. DOI: 10.1145/2939672.2939785',
 'Prokhorenkova, L. et al. (2018). CatBoost: Unbiased Boosting with Categorical Features. NeurIPS 31.',
 'Chawla, N. V. et al. (2002). SMOTE: Synthetic Minority Over-sampling Technique. JAIR 16, 321–357. DOI: 10.1613/jair.953',
 'Lopez-Rojas, E. A., Elmir, A. & Axelsson, S. (2016). PaySim: A Financial Mobile Money Simulator for Fraud Detection. EMSS.',
 'Project materials: README.md and PROJECT_REPORT.md in the FraudGuard AI workspace.',
 'Citation supplied in prompt: Yan Liu, Jing Li, Jeevanthi Srikanth & Hema Purnima Jethanil; International Journal of Computer Applications in Technology; “Volume 475.” Verify title, volume, issue, pages, and DOI before submission.'
 ];
 refs.forEach((r,i)=>{const y=176+i*76;txt(s,String(i+1).padStart(2,'0'),76,y,42,26,16,C.teal,true);txt(s,r,126,y-2,1070,64,i===5?17:18,i===5?C.orange:C.white,false);});
 txt(s,'Bibliographic details are reproduced from the project report or supplied brief; unresolved details are flagged above.',76,648,1110,20,14,C.muted,false);
 notes(s,'References 1–4 are listed in the project report. The author/journal/volume citation in the prompt was retained as user-supplied and requires verification.');
}

await fs.mkdir(BUILD,{recursive:true});await fs.mkdir(OUT,{recursive:true});
for(let i=0;i<deck.slides.items.length;i++){
 const png=await deck.export({slide:deck.slides.items[i],format:'png',scale:1});
 await fs.writeFile(path.join(BUILD,`slide-${String(i+1).padStart(2,'0')}.png`),new Uint8Array(await png.arrayBuffer()));
}
const pptx=await PresentationFile.exportPptx(deck);await pptx.save(path.join(BUILD,'FraudGuard_AI_draft.pptx'));
console.log(`slides=${deck.slides.items.length}`);



