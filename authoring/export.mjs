import {readFile,mkdir,writeFile,rename} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {createScene} from './scene.mjs';
import {exportScene} from './portability.mjs';
const hash=x=>createHash('sha256').update(x).digest('hex');
const fontPath=new URL('./node_modules/three/examples/fonts/helvetiker_regular.typeface.json',import.meta.url);
const font=await readFile(fontPath);
const {scene,bindings}=createScene(JSON.parse(font));
const output=new URL('../delivery/',import.meta.url);await mkdir(output,{recursive:true});
try{
  const {binary,report}=await exportScene(scene,bindings);
  const bytes=Buffer.from(binary);
  const sourceHashes={};for(const file of ['scene.mjs','portability.mjs','export.mjs','package-lock.json'])sourceHashes[file]=hash(await readFile(new URL(file,import.meta.url)));sourceHashes.font=hash(font);
  bindings.assetSha256=hash(bytes);
  const bindingText=JSON.stringify(bindings,null,2);
  const exportReport={schemaVersion:'threejs-spatial-export/v1',status:'passed',targetCompatibility:'unverified',threeVersion:'0.180.0',exporter:'three/addons/exporters/GLTFExporter.js',settings:{binary:true,onlyVisible:false,trs:true},assetSha256:hash(bytes),bindingsSha256:hash(bindingText),sourceHashes,...report};
  // Unique revision directories prevent a failed export from replacing a usable delivery.
  const revision='revision-'+hash(bytes).slice(0,12)+'-'+hash(bindingText).slice(0,8);
  const dir=new URL(revision+'/',output);await mkdir(dir,{recursive:true});
  await writeFile(new URL('scene.glb',dir),bytes);await writeFile(new URL('bindings.json',dir),bindingText);await writeFile(new URL('export-report.json',dir),JSON.stringify(exportReport,null,2));
  await writeFile(new URL('latest.tmp.json',output),JSON.stringify({revision,assetSha256:hash(bytes)},null,2));await rename(new URL('latest.tmp.json',output),new URL('latest.json',output));
  console.log(JSON.stringify({revision,bytes:bytes.length,...exportReport},null,2));
}catch(error){await writeFile(new URL('failed-attempt.json',output),JSON.stringify({status:'failed',message:error.message,report:error.report},null,2));throw error;}
