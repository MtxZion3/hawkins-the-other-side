import * as THREE from 'three';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';

export function inspectScene(scene, bindings) {
  const errors = [];
  const names = new Map();
  let meshes = 0, triangles = 0;
  const bounds = new THREE.Box3();
  const issue = (node, code) => errors.push({ node: node.name || node.type, code });
  scene.updateMatrixWorld(true);
  scene.traverse(node => {
    names.set(node.name, (names.get(node.name) ?? 0) + 1);
    if (!node.matrix.elements.every(Number.isFinite) || !node.matrixWorld.elements.every(Number.isFinite)) issue(node, 'NON_FINITE_TRANSFORM');
    if (node.scale.toArray().some(x => x <= 0)) issue(node, 'NON_POSITIVE_SCALE');
    for (const matrix of [node.matrix, node.matrixWorld]) {
      const cols = [0, 1, 2].map(i => new THREE.Vector3().setFromMatrixColumn(matrix, i));
      if (cols.some(c => c.length() < 1e-8)) issue(node, 'SINGULAR_TRANSFORM');
      cols.forEach(c => c.normalize());
      if (Math.abs(cols[0].dot(cols[1])) + Math.abs(cols[0].dot(cols[2])) + Math.abs(cols[1].dot(cols[2])) > 1e-5) issue(node, 'SHEAR_TRANSFORM');
    }
    if (!['Scene','Group','Object3D','Mesh'].includes(node.type) || node.isSkinnedMesh || node.isInstancedMesh) issue(node, 'UNSUPPORTED_NODE');
    if (node.animations.length || node.morphTargetInfluences?.length) issue(node, 'UNSUPPORTED_ANIMATION');
    if (!node.isMesh) return;
    meshes++;
    const geom = node.geometry;
    const pos = geom.getAttribute('position');
    if (!pos || !Array.from(pos.array).every(Number.isFinite)) issue(node, 'INVALID_VERTICES');
    const index = geom.index;
    if (index && Array.from(index.array).some(i => i < 0 || i >= pos.count)) issue(node, 'INVALID_INDICES');
    const count = index?.count ?? pos?.count ?? 0;
    if (count % 3 !== 0) issue(node, 'NON_TRIANGLE_GEOMETRY');
    triangles += count / 3;
    geom.computeBoundingBox();
    if (geom.boundingBox) bounds.union(geom.boundingBox.clone().applyMatrix4(node.matrixWorld));
    for (const mat of [].concat(node.material)) {
      if (mat.type !== 'MeshStandardMaterial' || mat.transparent || mat.opacity !== 1 || mat.alphaTest !== 0 || mat.side !== THREE.FrontSide) issue(node, 'UNSUPPORTED_MATERIAL');
      if (Object.values(mat).some(v => v?.isTexture)) issue(node, 'UNPROBED_TEXTURE');
      if (mat.type !== 'MeshStandardMaterial') continue;
      if (![mat.roughness, mat.metalness, mat.emissiveIntensity, ...mat.color.toArray(), ...mat.emissive.toArray()].every(Number.isFinite)) issue(node, 'NON_FINITE_MATERIAL');
      if (mat.emissiveIntensity !== 1) issue(node, 'UNPROBED_EMISSIVE_EXTENSION');
    }
  });
  for (const [id, name] of Object.entries(bindings.nodes)) {
    if (!/^[a-z][a-z0-9_]*$/.test(id) || names.get(name) !== 1) errors.push({ node: name, code: 'INVALID_BINDING_IDENTITY', id });
  }
  const ref = id => { if (!Object.hasOwn(bindings.nodes, id)) errors.push({ node: id, code: 'UNKNOWN_BINDING' }); };
  for (const state of Object.values(bindings.states)) [...state.visible, ...state.hidden].forEach(ref);
  bindings.signal.targets.forEach(ref);
  ref(bindings.rotation.target);
  if (!Number.isFinite(bindings.rotation.degreesPerSecond) || bindings.signal.stepMs <= 0 || !Number.isFinite(bindings.signal.stepMs)) errors.push({code:'INVALID_BEHAVIOR_PARAMETER'});
  if (bindings.schemaVersion !== 'threejs-spatial-bindings/v1' || !Object.hasOwn(bindings.states, bindings.initialState)) errors.push({code:'INVALID_BINDING_SCHEMA'});
  if (!meshes || bounds.isEmpty()) errors.push({code:'EMPTY_SCENE'});
  return { errors, nodes: [...names.values()].reduce((a,b)=>a+b,0), meshes, triangles, bounds: { min: bounds.min.toArray(), max: bounds.max.toArray() } };
}

export function readGLB(buffer) {
  const bytes = new Uint8Array(buffer);
  const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  if (bytes.length < 28 || view.getUint32(0,true) !== 0x46546c67 || view.getUint32(4,true) !== 2 || view.getUint32(8,true) !== bytes.length) throw new Error('INVALID_GLB_HEADER');
  let offset = 12, json, binaryLength = 0;
  while (offset < bytes.length) {
    const length = view.getUint32(offset,true), type = view.getUint32(offset+4,true);
    offset += 8;
    if (length % 4 || offset + length > bytes.length) throw new Error('INVALID_GLB_CHUNK');
    if (type === 0x4e4f534a) json = JSON.parse(new TextDecoder().decode(bytes.subarray(offset,offset+length)));
    if (type === 0x004e4942) binaryLength = length;
    offset += length;
  }
  if (!json || json.buffers?.length !== 1 || json.buffers[0].byteLength > binaryLength) throw new Error('INVALID_GLB_BUFFER');
  for (const v of json.bufferViews ?? []) if (v.buffer !== 0 || (v.byteOffset ?? 0) + v.byteLength > json.buffers[0].byteLength) throw new Error('INVALID_BUFFER_VIEW');
  const widths={SCALAR:1,VEC2:2,VEC3:3,VEC4:4,MAT4:16};
  const sizes={5120:1,5121:1,5122:2,5123:2,5125:4,5126:4};
  for (const a of json.accessors ?? []) {
    const v=json.bufferViews[a.bufferView], size=widths[a.type]*sizes[a.componentType];
    if (!v || !size || a.sparse || (a.byteOffset??0)+Math.max(0,a.count-1)*(v.byteStride??size)+size>v.byteLength) throw new Error('INVALID_ACCESSOR');
  }
  return json;
}

export async function exportScene(scene, bindings) {
  const report = inspectScene(scene, bindings);
  if (report.errors.length) throw Object.assign(new Error('PORTABILITY_CHECK_FAILED'), { report });
  // Official exporter requires only these two FileReader methods for texture-free GLB.
  globalThis.FileReader ??= class {
    readAsArrayBuffer(blob) { blob.arrayBuffer().then(value => {this.result=value; this.onloadend?.();}); }
    readAsDataURL(blob) { blob.arrayBuffer().then(value => {this.result='data:application/octet-stream;base64,'+Buffer.from(value).toString('base64'); this.onloadend?.();}); }
  };
  const binary = await new GLTFExporter().parseAsync(scene,{binary:true,onlyVisible:false,trs:true});
  const json = readGLB(binary);
  for (const [id,name] of Object.entries(bindings.nodes)) if (json.nodes.filter(n=>n.name===name).length!==1) throw new Error('EXPORTED_IDENTITY_MISMATCH:'+id);
  if (json.extensionsUsed?.length || json.animations?.length || json.textures?.length) throw new Error('UNPROBED_GLB_CAPABILITY');
  return { binary, json, report };
}
