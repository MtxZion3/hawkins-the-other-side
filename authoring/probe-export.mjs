import * as T from 'three';
import {exportScene} from './portability.mjs';
import {mkdir,writeFile} from 'node:fs/promises';
const scene=new T.Scene();scene.name='probe_scene';
const parent=new T.Group();parent.name='probe_parent';scene.add(parent);
for(const [name,x,color]of [['probe_left',-.12,0x336699],['probe_right',.12,0xa93344]]){const m=new T.Mesh(new T.BoxGeometry(.16,.16,.16),new T.MeshStandardMaterial({color}));m.name=name;m.position.x=x;parent.add(m);}
const bindings={schemaVersion:'threejs-spatial-bindings/v1',nodes:{root:'probe_parent',left:'probe_left',right:'probe_right'},initialState:'normal',states:{normal:{visible:['root'],hidden:[]}},rotation:{target:'root',degreesPerSecond:0},signal:{targets:[],stepMs:100}};
const{binary}=await exportScene(scene,bindings);
const dir=new URL('../../probe/app/src/main/assets/',import.meta.url);await mkdir(dir,{recursive:true});await writeFile(new URL('identity.glb',dir),Buffer.from(binary));
