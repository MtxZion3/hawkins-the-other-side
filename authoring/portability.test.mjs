import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import * as T from 'three';
import {createScene} from './scene.mjs';
import {inspectScene,exportScene,readGLB} from './portability.mjs';
const font=JSON.parse(await readFile(new URL('./node_modules/three/examples/fonts/helvetiker_regular.typeface.json',import.meta.url)));
test('export keeps both worlds and every hidden signal binding',async()=>{
 const {scene,bindings}=createScene(font);const {binary,json,report}=await exportScene(scene,bindings);
 assert.equal(report.errors.length,0);assert.ok(binary.byteLength>10000);
 for(const name of Object.values(bindings.nodes))assert.equal(json.nodes.filter(n=>n.name===name).length,1);
 assert.ok(report.triangles<30000);assert.equal(json.extensionsUsed,undefined);
});
test('duplicate imported identity is rejected',()=>{const {scene,bindings}=createScene(font);scene.children[0].add(Object.assign(new T.Group(),{name:'normal_world'}));assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='INVALID_BINDING_IDENTITY'));});
test('unknown behavior targets are rejected',()=>{const {scene,bindings}=createScene(font);bindings.signal.targets.push('missing');assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='UNKNOWN_BINDING'));});
test('custom shader is not silently converted',()=>{const {scene,bindings}=createScene(font);scene.getObjectByName('h_surface_0').material=new T.ShaderMaterial();assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='UNSUPPORTED_MATERIAL'));});
test('negative scale and shear are rejected',()=>{const {scene,bindings}=createScene(font);scene.children[0].scale.x=-1;assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='NON_POSITIVE_SCALE'));scene.children[0].scale.set(2,1,1);scene.getObjectByName('normal_world').rotation.z=.3;assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='SHEAR_TRANSFORM'));});
test('NaN geometry is rejected',()=>{const {scene,bindings}=createScene(font);scene.getObjectByName('h_surface_0').geometry.attributes.position.array[0]=NaN;const old=console.error;console.error=()=>{};try{assert.ok(inspectScene(scene,bindings).errors.some(e=>e.code==='INVALID_VERTICES'));}finally{console.error=old;}});
test('truncated GLB is rejected',async()=>{const {scene,bindings}=createScene(font);const {binary}=await exportScene(scene,bindings);assert.throws(()=>readGLB(binary.slice(0,-4)),/INVALID_GLB_HEADER/);});
