import * as T from 'three';
import { FontLoader } from 'three/addons/loaders/FontLoader.js';
import { TextGeometry } from 'three/addons/geometries/TextGeometry.js';
import { mergeGeometries } from 'three/addons/utils/BufferGeometryUtils.js';

export function createScene(fontData) {
  const scene = new T.Scene(); scene.name='hawkins_scene';
  const pivot = new T.Group(); pivot.name='diorama'; scene.add(pivot);
  const font = new FontLoader().parse(fontData);
  const nodes={diorama:'diorama',normal_world:'normal_world',alternate_world:'alternate_world'};
  const signals=[];
  for (const alternate of [false,true]) {
    const world=new T.Group(); world.name=alternate?'alternate_world':'normal_world'; pivot.add(world);
    const p=alternate?'u':'h';
    const mat=(color,emissive=0)=>new T.MeshStandardMaterial({color,roughness:.86,metalness:.05,emissive,emissiveIntensity:1});
    const wood=mat(alternate?0x232b35:0x765b41),wall=mat(alternate?0x34454e:0xb3a18b),trim=mat(alternate?0x172b39:0x544134);
    const red=mat(alternate?0x69262c:0x86493c),dark=mat(0x151b27),cream=mat(alternate?0x778d98:0xe9d3a2);
    const batches=new Map();
    function mesh(geometry,material,x=0,y=0,z=0,rz=0) {
      const m=new T.Mesh(geometry,material);m.position.set(x,y,z);m.rotation.z=rz;m.updateMatrix();
      geometry.applyMatrix4(m.matrix);
      const list=batches.get(material)??[];list.push(geometry);batches.set(material,list);return m;
    }
    const box=(w,h,d,m,x,y,z)=>mesh(new T.BoxGeometry(w,h,d),m,x,y,z);
    const sphere=(r,m,x,y,z)=>mesh(new T.SphereGeometry(r,10,7),m,x,y,z);
    function tube(points,r,m) {const curve=new T.CatmullRomCurve3(points.map(a=>new T.Vector3(...a)));mesh(new T.TubeGeometry(curve,Math.max(8,points.length*4),r,5,false),m);}
    function text(value,size,m,x,y,z){const g=new TextGeometry(value,{font,size,depth:.0015,curveSegments:2,bevelEnabled:false});g.computeBoundingBox();g.translate(-g.boundingBox.max.x/2,0,0);mesh(g,m,x,y,z);}
    // Open-front miniature room: all geometry is authored in meters, front +Z.
    box(1.23,.045,.67,trim,0,-.22,0);
    for(let i=0;i<17;i++)box(.069,.008,.61,wood,-.56+i*.07,-.193,0);
    box(1.2,.50,.026,wall,0,.055,-.32);
    box(.025,.50,.65,wall,-.6,.055,0);
    box(1.2,.025,.04,trim,0,-.18,-.30);
    box(1.23,.025,.05,trim,0,.307,-.32);
    for(let i=0;i<21;i++)box(.005,.47,.001,cream,-.56+i*.056,.055,-.305);
    // Rug and sofa establish lived-in scale, with asymmetric side objects.
    box(.67,.009,.34,red,.04,-.183,.045);
    for(let i=0;i<5;i++)box(.59-i*.085,.001,.004,cream,.04,-.177,.18-i*.029);
    box(.55,.082,.16,red,-.01,-.11,-.18);box(.57,.15,.045,red,-.01,-.049,-.262);
    for(const x of [-.292,.272])box(.042,.11,.205,red,x,-.071,-.179);
    for(const x of [-.183,0,.183])box(.167,.034,.135,red,x-.01,-.05,-.164);
    box(.074,.06,.033,cream,-.20,.001,-.213);box(.082,.063,.027,wood,.187,.004,-.217);
    box(.33,.018,.16,wood,.01,-.091,.105);
    for(const x of [-.13,.15])for(const z of [.05,.16])box(.014,.082,.014,trim,x,-.14,z);
    box(.052,.009,.042,cream,-.057,-.076,.105);box(.059,.007,.043,red,-.052,-.067,.108);
    sphere(.014,cream,.088,-.061,.108);
    // Floor lamp, old television and window are static exportable meshes.
    const gold=mat(0x8b7443),lamp=mat(alternate?0x705b61:0xe0bd75,alternate?0x371526:0x574017);
    mesh(new T.CylinderGeometry(.038,.045,.01,12),dark,-.405,-.18,-.16);
    mesh(new T.CylinderGeometry(.004,.004,.29,8),gold,-.405,-.035,-.16);
    mesh(new T.CylinderGeometry(.045,.071,.072,14),lamp,-.405,.105,-.16);
    box(.14,.104,.09,wood,.445,-.09,-.16);box(.111,.074,.006,dark,.43,-.087,-.111);
    const screen=mat(alternate?0x64373f:0x83a9b7,alternate?0x381118:0x233d45);
    box(.086,.053,.004,screen,.426,-.083,-.106);
    for(const x of [.401,.489])box(.012,.053,.014,trim,x,-.157,-.17);
    tube([[.425,-.031,-.17],[.401,.049,-.19]],.002,gold);tube([[.437,-.031,-.17],[.475,.035,-.19]],.002,gold);
    box(.20,.18,.012,dark,.438,.178,-.296);box(.18,.157,.01,screen,.438,.178,-.284);
    box(.008,.157,.012,cream,.438,.178,-.275);box(.18,.008,.012,cream,.438,.178,-.275);
    // Alphabet wall: real mesh letters, no canvas textures or font network fetch.
    const letters=['ABCDEFGH','IJKLMNOPQ','RSTUVWXYZ'];
    const bulbs=[0xd9805b,0x8fa94d,0xd9b752,0x659db1,0xc45d59];
    let n=0;
    for(let row=0;row<3;row++){
      const chars=letters[row],y=.235-row*.077;
      const cable=[];
      for(let i=0;i<chars.length;i++){
        const x=-.31+i*.078-(row===0?0:.025);
        text(chars[i],.032,dark,x,y-.045,-.278);
        const color=bulbs[n++%bulbs.length];const b=mat(alternate?0x6e8193:color,alternate?0x122330:color);
        sphere(.009,b,x,y,-.266);box(.005,.011,.007,dark,x,y+.009,-.269);
        cable.push([x,y+.019+(i%2?.004:0),-.277]);
        if('RUN'.includes(chars[i])){
          const id=p+'_signal_'+chars[i].toLowerCase();nodes[id]=id;
          const glow=new T.Mesh(new T.SphereGeometry(.015,12,8),mat(0xffd57c,0xffcb63));
          glow.name=id;glow.position.set(x,y,-.264);glow.visible=false;world.add(glow);signals.push(id);
        }
      }
      tube(cable,.002,dark);
    }
    // World names belong to the localized native UI, not baked English geometry.
    text(String(1983),.025,cream,0,-.232,.342);
    if(alternate){
      const vine=mat(0x49232d),vein=mat(0x974044,0x420d13);
      for(let i=0;i<10;i++){
        const x=-.53+i*.114;
        tube([[x,-.175,.27],[x+.06,-.163,.12],[x-.035,-.155,-.09],[x+.02,-.06,-.269],[x-.025,.10,-.266],[x+.025,.285,-.275]],.007+(i%3)*.0015,vine);
      }
      tube([[.28,-.17,-.273],[.24,-.07,-.27],[.30,.02,-.27],[.26,.09,-.27],[.32,.19,-.27],[.29,.29,-.27]],.013,vein);
      for(let i=0;i<28;i++){const x=Math.sin(i*12.37)*.53,y=-.12+(i%9)*.043,z=Math.cos(i*7.13)*.27;sphere(.0025,cream,x,y,z);}
    }
    let i=0;for(const [material,geometries]of batches){const merged=mergeGeometries(geometries.map(g=>g.index?g.toNonIndexed():g),false);const m=new T.Mesh(merged,material);m.name=p+'_surface_'+i++;world.add(m);}
    world.visible=!alternate;
  }
  const bindings={schemaVersion:'threejs-spatial-bindings/v1',asset:'scene.glb',units:'meters',upAxis:'+Y',frontAxis:'+Z',nodes,initialState:'normal',states:{normal:{visible:['normal_world'],hidden:['alternate_world']},alternate:{visible:['alternate_world'],hidden:['normal_world']}},rotation:{target:'diorama',mode:'bounded-sweep',amplitudeDegrees:14,degreesPerSecond:8,fitScale:0.78},signal:{targets:signals,letters:['r','u','n'],stepMs:650},actions:[{id:'toggle_world',kind:'toggle-state'},{id:'play_signal',kind:'signal'},{id:'toggle_rotation',kind:'toggle-rotation'},{id:'reset',kind:'reset'}]};
  return {scene,bindings};
}
