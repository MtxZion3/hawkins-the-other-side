(() => {
  const spec = JSON.parse(document.querySelector('#pico-design-spec').textContent);
  const mount = document.querySelector('#app');
  const vibrant = PicoTheme.vibrantColorScheme();
  const cs = Object.fromEntries(Object.entries(spec.theme.colorScheme).map(([key, val]) => [key, val.source === 'fixed' ? (/^#[0-9a-f]{8}$/i.test(val.value)?'#'+val.value.slice(3)+val.value.slice(1,3):val.value) : vibrant[val.role]]));
  PicoTheme.install({scheme: spec.theme.scheme, colorScheme: cs});
  const color = value => spec.theme.brandColors[value] || cs[value] || value;
  const defs = new Map(spec.nodes.map(node => [node.id, node]));
  const elements = new Map();
  let stateId = spec.initialStateId;
  let data = structuredClone(spec.dataCases.normal);
  let caseId = 'normal';
  let sequenceTimer;
  let sceneView;
  const resolvePath = (obj,path) => path.split('.').reduce((v,k) => v?.[k],obj);
  const sceneNode = spec.nodes.find(n => n.rendererKey === 'miniature-room');
  const sceneProps = sceneNode.props;
  const puzzle = defs.get('control-panel').props.puzzle;
  const rules = HawkinsReceiverRules;
  Object.assign(data,rules.fresh(puzzle));
  const getModel = () => ({...data,...rules.project(data,puzzle,sceneProps.signal.letters),world:sceneProps.worlds[stateId],worldIndex:stateId===spec.initialStateId?0:1,controlsDisabled:!data.geometryAvailable,...(!data.geometryAvailable?{status:spec.dataCases.error.status,signalCaption:spec.dataCases.error.signalCaption}:{})});
  const surface = spec.surfaces[0];
  const preview = surface.placement.previewViewportDp;
  document.title = spec.metadata.projectName;
  mount.dataset.designSpecRevision = spec.metadata.revision;
  mount.dataset.previewScope = 'web_design_validation_only';
  mount.style.maxWidth = preview.defaultSizeDp[0]+'px';
  mount.style.fontFamily = spec.theme.typography.body.family;
  mount.style.color = cs.labelPrimary;
  function styleLayout(el, layout) {
    el.style.display = layout.mode === 'grid' ? 'grid' : 'flex';
    el.style.flexDirection = ['column','scroll'].includes(layout.mode) ? 'column' : 'row';
    if (layout.mode === 'stack' || layout.mode === 'overlay') { el.style.position='relative'; el.style.display='block'; }
    el.style.gridTemplateColumns = layout.columns ? 'repeat('+layout.columns+', minmax(0,1fr))' : '';
    el.style.alignItems = layout.align ? ({start:'flex-start',end:'flex-end'}[layout.align]||layout.align) : '';
    el.style.justifyContent = layout.justify ? ({start:'flex-start',end:'flex-end'}[layout.justify]||layout.justify) : '';
    if(layout.gapDp!==undefined) el.style.gap=layout.gapDp+'px';
    if(layout.paddingDp!==undefined) el.style.padding=typeof layout.paddingDp==='number' ? layout.paddingDp+'px' : ['top','right','bottom','left'].map(k=>layout.paddingDp[k]+'px').join(' ');
    for(const [key,value] of Object.entries(layout)) {
      if(['widthDp','heightDp','minWidthDp','maxWidthDp','minHeightDp','maxHeightDp'].includes(key)) el.style[key.replace('Dp','')]=value+'px';
    }
    if(layout.grow!==undefined) el.style.flexGrow=layout.grow; el.style.minWidth='0';
    if(layout.overflow) el.style.overflow=layout.overflow;
  }
  function createNode(id) {
    const n=defs.get(id);
    const el=document.createElement(n.kind==='spatialui'?n.component:n.kind==='text'?'p':'div');
    el.id=n.id; el.dataset.nodeId=n.id; el.dataset.kind=n.kind;
    elements.set(id,el);
    if(n.accessibilityLabel) el.setAttribute('aria-label',n.accessibilityLabel);
    if(n.kind==='text') {
      el.textContent=n.content;
      const t=spec.theme.typography[n.textStyle.typography];
      Object.assign(el.style,{fontFamily:t.family,fontSize:t.sizeDp+'px',lineHeight:t.lineHeightDp+'px',fontWeight:t.weight,color:color(n.textStyle.color),textAlign:n.textStyle.align||'start'});
    }
    if(n.kind==='spatialui') {
      for(const [k,v] of Object.entries(n.props||{})) { if(typeof v==='object')continue; if(typeof v==='boolean') el.toggleAttribute(k,v); else el.setAttribute(k,String(v)); }
      for(const [event,actionId] of Object.entries(n.events||{})) el.addEventListener(event,e=>perform(actionId,e.detail||{}));
    }
    if(n.layout) styleLayout(el,n.layout);
    if(n.appearance) {
      const a=n.appearance;
      if(a.material&&a.material!=='none') el.classList.add('material-'+a.material);
      if(a.radiusDp!==undefined) el.style.borderRadius=a.radiusDp+'px';
      if(a.fill) el.style.background=color(a.fill);
      if(a.opacity!==undefined) el.style.opacity=a.opacity;
    }
    for(const child of n.children||[]) el.appendChild(createNode(child));
    return el;
  }
  function applyResponsive() {
    const width=mount.clientWidth;
    const changes=new Map();
    for(const rule of spec.responsiveRules) if((!rule.when.maxWidthDp||width<=rule.when.maxWidthDp)&&(!rule.when.minWidthDp||width>=rule.when.minWidthDp)) for(const override of rule.overrides) changes.set(override.nodeId,override);
    for(const n of spec.nodes) {
      const el=elements.get(n.id); if(!el) continue;
      const change=changes.get(n.id);
      if(n.layout) styleLayout(el,{...n.layout,...change?.layout});
      el.hidden=Boolean(change?.hidden)||Boolean(n.visibleInStates&&!n.visibleInStates.includes(stateId));
    }
    mount.style.minHeight=(width<=preview.minSizeDp[0]?preview.minSizeDp[1]:preview.defaultSizeDp[1])+'px';
    sceneView?.resize();
  }
  function update() {
    mount.dataset.state=stateId; mount.dataset.dataCase=caseId;
    const model=getModel();
    for(const n of spec.nodes) {
      const el=elements.get(n.id); if(!el)continue;
      for(const [prop,path] of Object.entries(n.bindings||{})) {
        const value=resolvePath(model,path);
        if(prop==='content') el.textContent=value??n.content??'';
        else if(typeof value==='boolean') el.toggleAttribute(prop,value);
        else if(el.getAttribute(prop)!==String(value??''))el.setAttribute(prop,String(value??''));
      }
    }
    sceneView?.update(model);
  }
  function clearSequence() {clearTimeout(sequenceTimer); data.busy=!data.geometryAvailable; data.signalIndex=-1; data.playLabel=spec.dataCases.normal.playLabel;}
  function followTransition(id) {const t=spec.transitions.find(t=>t.id===id);if(t&&t.from===stateId)stateId=t.to;}
  function perform(actionId,detail={}) {
    const a=spec.actions.find(a=>a.id===actionId); if(!a)return;
    if(a.payload?.operation==='tune-frequency'){data=rules.tune(data,Number(detail.value),puzzle);}
    else if(a.payload?.operation==='reply-letter'){data=rules.enter(data,a.payload.letter,puzzle);}
    else if(a.payload?.operation==='erase-reply'){if(data.received&&!data.busy&&!data.solved){data.reply=data.reply.slice(0,-1);data.wrong=false;}}
    else if(a.payload?.operation==='submit-reply'){data=rules.submit(data,puzzle,sceneProps.signal.letters);if(data.solved){stateId=spec.initialStateId;data.worldId=stateId;sceneView?.reset();}}
    else if(a.type==='toggle') {if(!data.geometryAvailable||data.busy||data.solved)return;data[a.binding]=typeof detail.checked==='boolean'?detail.checked:!data[a.binding];data.rotationLabel=a.payload.labels[String(data[a.binding])];}
    else if(a.payload?.operation==='world') {
      if(!data.geometryAvailable||data.solved)return;const target=a.payload.targetStates[detail.index];if(target===stateId)return;
      clearSequence();
      const t=spec.transitions.find(t=>a.payload.transitionIds.includes(t.id)&&t.from===stateId&&(!target||t.to===target));if(t)followTransition(t.id);
      data.received=false;data.reply='';data.wrong=false;data.worldId=stateId;data.status=data.geometryAvailable?sceneProps.worlds[stateId].status:spec.dataCases.error.status;data.signalCaption=spec.dataCases[caseId].signalCaption;
    } else if(a.payload?.operation==='reset') {
      clearSequence();followTransition(a.payload.transitionId);
      stateId=a.payload.targetStateId;data={...structuredClone(spec.dataCases.normal),...rules.fresh(puzzle)};caseId='normal';sceneView?.reset();
    } else if(a.payload?.operation==='sequence') {
      if(data.busy||!data.geometryAvailable||data.solved||!rules.locked(data,puzzle))return;
      clearSequence();data.busy=true;data.received=false;data.reply='';data.wrong=false;data.rotating=false;data.playLabel=a.payload.playLabel;data.status=a.payload.playingStatus;
      const cfg=resolvePath(defs.get(a.payload.configNodeId).props,a.payload.configPath);
      let index=0;
      const step=()=>{
        data.signalIndex=index;
        data.signalCaption=index<cfg.letters.length?cfg.letters.slice(0,index+1).join(' · '):a.payload.completedText;
        if(index>=cfg.letters.length) {data.received=true;data.busy=false;data.playLabel=a.payload.idleLabel;data.status=sceneProps.worlds[stateId].status;data.signalIndex=-1;update();return;}
        update();index++;sequenceTimer=setTimeout(step,index===cfg.letters.length?cfg.holdMs:cfg.stepMs);
      };step();return;
    }
    update();
  }
  function makeRoom(host,p) {
    const scene=new THREE.Scene();
    const camera=new THREE.PerspectiveCamera(p.camera.fov,1,p.camera.near,p.camera.far);
    camera.position.fromArray(p.camera.position); camera.lookAt(...p.camera.target);
    const r=new THREE.WebGLRenderer({antialias:p.render.antialias,alpha:p.render.alpha,preserveDrawingBuffer:true});
    r.setPixelRatio(Math.min(devicePixelRatio,p.render.pixelRatioMax));r.toneMapping=THREE[p.render.toneMapping+'ToneMapping'];r.toneMappingExposure=p.render.exposure;
    r.shadowMap.enabled=true; r.shadowMap.type=THREE.PCFSoftShadowMap;
    host.appendChild(r.domElement);
    const group=new THREE.Group();scene.add(group);
    scene.add(new THREE.AmbientLight(p.ambient.color,p.ambient.intensity));
    const lights=p.lights.map((l,i)=>{const light=new THREE.DirectionalLight(l.color,l.intensity);light.position.fromArray(l.position);if(i===0){light.castShadow=true;light.shadow.mapSize.set(2048,2048);light.shadow.camera.left=-2;light.shadow.camera.right=2;light.shadow.camera.top=2;light.shadow.camera.bottom=-2;light.shadow.normalBias=.004;}scene.add(light);return light;});
    const objects=[];
    let imported;
    const asset=spec.assets.find(a=>a.id===sceneNode.assetId);
    const bytes=Uint8Array.from(atob(asset.src.split(',')[1]),c=>c.charCodeAt(0));
    new THREE.GLTFLoader().parse(bytes.buffer,'',result=>{
      imported=result.scene;group.add(imported);imported.scale.setScalar(p.uniformScale);
      imported.traverse(mesh=>{if(mesh.isMesh){mesh.castShadow=true;mesh.receiveShadow=true;objects.push(mesh);}});
      sceneView?.update(getModel());
    },error=>{caseId='error';data={...structuredClone(spec.dataCases.error),...rules.fresh(puzzle)};update();console.error(error);});
    let last=performance.now(),direction=1,pointerX=null;
    const reduced=matchMedia('(prefers-reduced-motion: reduce)');
    function animate(now){
      const delta=Math.min((now-last)/1000,.1);last=now;
      if(data.rotating&&!reduced.matches){data.angle+=delta*p.rotationRadiansPerSecond*direction;if(data.angle>p.rotationRange[1]||data.angle<p.rotationRange[0]){direction*=-1;data.angle=Math.max(p.rotationRange[0],Math.min(p.rotationRange[1],data.angle));}group.rotation.y=data.angle;}
      if(data.geometryAvailable&&!data.busy&&!data.received&&!data.solved){data.scanSeconds=(data.scanSeconds+delta)%60;sceneView?.update(getModel());}
      r.render(scene,camera);requestAnimationFrame(animate);
    }requestAnimationFrame(animate);
    const resize=()=>{const w=host.clientWidth,h=host.clientHeight;r.setSize(w,h);camera.aspect=w/h;camera.updateProjectionMatrix();};
    new ResizeObserver(resize).observe(host);resize();
    return {resize,reset(){group.rotation.y=p.rotationInitial;},update(model){
      group.visible=model.geometryAvailable;lights[1].color.set(model.world.light);
      if(imported){
        for(const [key,name] of Object.entries(p.nodeBindings)){
          if(typeof name==='string'&&p.worlds[key]){const world=imported.getObjectByName(name);if(world)world.visible=key===model.worldId;}
        }
        for(const [world,names] of Object.entries(p.nodeBindings.signal)) names.forEach((name,index)=>{const mesh=imported.getObjectByName(name);if(mesh)mesh.visible=world===model.worldId&&(model.solved||model.busy&&index===model.signalIndex||!model.received&&!model.busy&&(rules.locked(data,puzzle)||index===Math.floor(data.scanSeconds/puzzle.noiseStepSeconds)%(p.signal.letters.length+2)));});
      }
    },inspect(){return {meshes:objects.length,assetLoaded:Boolean(imported),world:stateId,angle:data.angle,geometryVisible:group.visible,signalLetter:p.signal.letters[data.signalIndex]||null};}};
  }
  mount.appendChild(createNode(surface.rootNodeId));
  try {sceneView=makeRoom(elements.get(sceneNode.id),sceneProps);} catch(error){data={...structuredClone(spec.dataCases.error),...rules.fresh(puzzle)};caseId='error';console.error(error);}
  applyResponsive();update();new ResizeObserver(applyResponsive).observe(mount);
  document.addEventListener('visibilitychange',()=>{if(document.hidden){clearSequence();data.rotating=false;update();}});
  window.designReview={spec,perform,getState:()=>({stateId,caseId,...data,scene:sceneView?.inspect()}),setDataCase(id){if(!spec.dataCases[id])throw Error('Unknown case');clearSequence();caseId=id;data={...structuredClone(spec.dataCases[id]),...rules.fresh(puzzle)};stateId=spec.initialStateId;update();},registered:()=>spec.nodes.filter(n=>n.kind==='spatialui').map(n=>({id:n.id,tag:n.component,registered:Boolean(customElements.get(n.component))}))};
})();