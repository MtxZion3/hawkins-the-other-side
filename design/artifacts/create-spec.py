import json, math, pathlib
out=pathlib.Path(__file__).resolve().parents[1]
roles='fillPrimary fillSecondary fillTertiary fillLight labelPrimaryLight labelPrimary labelSecondary labelTertiary labelQuaternary lightenHover lightenPressed error alert passable interaction dividerLine'.split()
colors={r:{'source':'vibrant','role':r} for r in roles}
for r,c in {'labelPrimaryLight':'#FFF6E6','error':'#F17179','alert':'#EFBE77','passable':'#AFDCC4','interaction':'#9FCBDD'}.items(): colors[r]={'source':'fixed','value':c}
brand={'midnight':'#162C40','cold-light':'#94C6DC','rift-red':'#A63844','signal-amber':'#E5B86B','plaster':'#B9AA85','wood':'#48392B','sofa':'#82614E','rug':'#622E36','ink':'#34282A','vine':'#361F29'}
meshes=[]
def box(id,pos,size,color,**kw): meshes.append(dict(id=id,type='box',position=pos,size=size,color=color,**kw))
def sphere(id,pos,radius,color,**kw): meshes.append(dict(id=id,type='sphere',position=pos,radius=radius,color=color,**kw))
def cyl(id,pos,top,bottom,height,color,**kw): meshes.append(dict(id=id,type='cylinder',position=pos,radiusTop=top,radiusBottom=bottom,height=height,color=color,**kw))
def tube(id,points,radius,color,**kw): meshes.append(dict(id=id,type='tube',points=points,radius=radius,color=color,**kw))
def label(id,text,pos,size,color,bg=None,**kw): meshes.append(dict(id=id,type='letter',text=text,position=pos,size=size,color=color,background=bg,**kw))
box('plinth',[0,-.055,0],[1.4,.11,.8],'#202A35')
box('floor',[0,.007,0],[1.35,.025,.75],brand['wood'])
for i in range(17):
    x=-.638+i*.08
    box('floor-board-'+str(i),[x,.023,0],[.077,.008,.745],['#67513C','#715B43','#5C4938'][i%3])
    for j in range(2): box('board-joint-'+str(i)+'-'+str(j),[x,.029,-.22+j*.38+(i%3)*.03],[.075,.002,.002],'#372C25')
box('back-wall',[0,.348,-.36],[1.36,.68,.035],brand['plaster'])
box('left-wall',[-.66,.348,-.09],[.03,.68,.52],brand['plaster'])
box('wainscot-back',[0,.10,-.335],[1.32,.14,.012],'#665442')
box('wainscot-left',[-.638,.10,-.09],[.014,.14,.52],'#665442')
for y in [.045,.178,.661]:
    box('back-trim-'+str(y),[0,y,-.333],[1.33,.014,.024],'#86735B')
    box('left-trim-'+str(y),[-.637,y,-.09],[.024,.014,.52],'#86735B')
for i in range(27):
    for j in range(7):
        x=-.63+i*.048; y=.215+j*.062
        sphere('wallpaper-'+str(i)+'-'+str(j),[x,y,-.34],.005,'#A29375',scale=[1,1.8,.15])
# Blue casement window, curtains, radiator: domain silhouette comes before ornament.
box('window-dark',[-.631,.432,-.073],[.01,.28,.267],'#1C3344')
for z in [-.205,.06]: box('window-frame-v-'+str(z),[-.618,.431,z],[.023,.30,.018],'#493D34')
for y in [.283,.432,.58]: box('window-frame-h-'+str(y),[-.614,y,-.073],[.027,.016,.282],'#493D34')
box('window-cross',[-.611,.43,-.073],[.035,.29,.014],'#493D34')
box('window-sill',[-.60,.275,-.073],[.105,.025,.32],'#958368')
for side in [-1,1]:
    for i in range(5): cyl('curtain-'+str(side)+'-'+str(i),[-.598,.43,-.073+side*.138+(i-2)*.012],.011,.011,.33,'#9E8B65',scale=[.4,1,1])
for i in range(8): box('radiator-'+str(i),[-.605,.137,-.185+i*.031],[.07,.17,.018],'#978E7B')
# Rug, sofa, coffee table and a faithful analog living-room scale.
box('rug',[.08,.035,.085],[.86,.007,.44],brand['rug'])
for i in range(5):
    box('rug-border-'+str(i),[.08,.040+i*.0001,.085],[.82-i*.035,.001,.40-i*.033],['#9C7650','#483333','#AC885A','#5C363B','#813E43'][i])
box('sofa-base',[.04,.133,-.193],[.70,.12,.23],brand['sofa'])
box('sofa-back',[.04,.24,-.276],[.70,.23,.07],'#725441')
for x in [-.307,.387]: box('sofa-arm-'+str(x),[x,.208,-.178],[.072,.16,.25],'#957255')
for i in range(3):
    box('sofa-seat-'+str(i),[-.18+i*.22,.20,-.165],[.208,.046,.182],'#A17F5E')
    box('sofa-cushion-'+str(i),[-.18+i*.22,.286,-.226],[.204,.108,.057],'#AD8C68',rotation=[-.15,0,0])
for x in [-.26,.34]:
    for z in [-.26,-.08]: cyl('sofa-leg-'+str(x)+str(z),[x,.075,z],.018,.014,.06,'#332E28')
box('cushion-red',[-.208,.272,-.156],[.11,.11,.043],'#613B37',rotation=[-.18,0,.17])
box('cushion-green',[.28,.263,-.154],[.1,.10,.04],'#59614A',rotation=[-.18,0,-.18])
box('coffee-top',[.08,.137,.13],[.40,.022,.20],'#7D5335')
for x in [-.08,.24]:
    for z in [.062,.198]: box('coffee-leg-'+str(x)+str(z),[x,.083,z],[.018,.10,.018],'#503A29')
box('book-cover',[.13,.154,.122],[.095,.012,.071],'#29454A',rotation=[0,.12,0])
box('book-pages',[.13,.162,.122],[.09,.008,.065],'#C9BD9E',rotation=[0,.12,0])
cyl('coffee-cup',[-.035,.17,.152],.016,.013,.036,'#CCBE9A')
# Sideboard, radio, rotary phone, standing lamp.
box('sideboard',[.547,.128,-.17],[.20,.18,.19],'#684A34')
box('sideboard-top',[.547,.225,-.17],[.22,.021,.21],'#8B6442')
box('radio',[.546,.276,-.21],[.147,.083,.059],'#4F3A29')
box('radio-speaker',[.52,.277,-.178],[.074,.052,.004],'#302E29')
for x in [.573,.595]: sphere('radio-dial-'+str(x),[x,.263,-.175],.007,'#C5B893')
box('radio-scale',[.574,.29,-.176],[.041,.01,.004],'#BFAC72')
cyl('lamp-foot',[-.466,.046,-.19],.048,.051,.014,'#45382A')
cyl('lamp-stem',[-.466,.23,-.19],.006,.006,.36,'#9B8150')
cyl('lamp-shade',[-.466,.408,-.19],.044,.082,.11,'#E0C394',emissive='#A47735',emissiveIntensity=.35)
# Alphabet string: physical bulbs above hand-placed painted letters.
alphabet=['ABCDEFGH','IJKLMNOPQ','RSTUVWXYZ']
bulbColors=['#EFBE77','#B46863','#88AFBC','#A1B283']
for row,letters in enumerate(alphabet):
    y=.574-row*.087
    pts=[]
    for i,ch in enumerate(letters):
        x=-.42+i*.101
        wireY=y+.037-math.sin(i/len(letters)*math.pi)*.012
        pts.append([x,wireY,-.309])
        label('alphabet-'+ch,ch,[x,y,-.314],[.042,.052],brand['ink'],rotation=[0,0,math.sin(i*7+row)*.1])
        cyl('bulb-cap-'+ch,[x,wireY-.007,-.308],.004,.004,.012,'#454037')
        sphere('bulb-'+ch,[x,wireY-.017,-.305],.009,bulbColors[(i+row)%4],emissive=bulbColors[(i+row)%4],emissiveIntensity=.55,signalLetter=ch)
    tube('wire-'+str(row),pts,.0017,'#3A3430')
# A handful of physical tendrils appears only in the other world.
for i in range(12):
    x=-.60+i*.109
    pts=[[x,.03,.30],[x+.09*math.sin(i),.07,.10],[x-.055,.13,-.15],[x+.02,.30,-.312],[x+.08*math.sin(i*2),.52,-.309],[x-.01,.66,-.315]]
    tube('vine-'+str(i),pts,.007+(.002*(i%3)),brand['vine'],world='upside-down')
for i in range(18):
    x=-.62+(i*31%127)/100; z=-.29+(i*19%61)/100
    sphere('spore-'+str(i),[x,.06+(i%5)*.091,z],.0035,'#BAC6D0',world='upside-down',emissive='#8AA4B4',emissiveIntensity=.3)
label('plinth-label','HAWKINS  /  1983',[0,-.047,.402],[.47,.04],'#BEC8C9',font='600 46px monospace')
scene=dict(meshes=meshes,roughness=.85,metalness=0,physicalBoundsM=[1.4,.8,.8],camera=dict(position=[1.42,1.0,1.75],target=[0,.27,0],fov=34,near=.01,far=20),ambient=dict(color='#B9C6D0',intensity=2.1),lights=[dict(color='#F2D3A4',intensity=4,position=[-.7,1.4,1.1]),dict(color='#87BBDC',intensity=3,position=[-1.2,.7,-.3]),dict(color='#C8D2DC',intensity=1.5,position=[1,.8,.4])],worlds={'hawkins':dict(label='HAWKINS, INDIANA',eyebrow='NOVEMBER 1983',status='A quiet room. A message waiting.',button='Enter the Upside Down',description='Look closer. Some things are still here.',tint='#FFFFFF',light='#87BBDC',vineVisible=False),'upside-down':dict(label='THE UPSIDE DOWN',eyebrow='THE OTHER SIDE',status='Same room. Something has changed.',button='Return to Hawkins',description='Follow the lights. Find a way back.',tint='#78505B',light='#BC3347',vineVisible=True)},rotationRadiansPerSecond=.12,rotationInitial=0,rotationRange=[-.62,.62],dragRadiansPerPixel=.005,signal=dict(letters=['R','U','N'],stepMs=850,holdMs=1000,emissiveIntensity=5),textTexture=dict(width=256,height=128,font='600 98px Georgia',color=brand['ink']),render=dict(toneMapping='ACESFilmic',exposure=1.15,antialias=True,alpha=True,pixelRatioMax=2),preview=dict(defaultSizeDp=[1120,868],minSizeDp=[640,816],maxSizeDp=[1440,1020]))
nodes=[]
def node(id,kind,**kw): nodes.append(dict(id=id,kind=kind,**kw))
def text(id,content,style='body',color='labelPrimary',**kw): node(id,'text',content=content,textStyle=dict(typography=style,color=color),**kw)
node('room-root','layout',layout=dict(mode='column',paddingDp=32,gapDp=16),appearance=dict(material='regular',radiusDp=32),children=['header','scene','control-panel'])
node('header','layout',layout=dict(mode='row',justify='space-between',align='center',gapDp=24),children=['identity','world-summary'])
node('identity','layout',layout=dict(mode='column',gapDp=0),children=['eyebrow','title','subtitle'])
text('eyebrow','','label','cold-light',bindings={'content':'world.eyebrow'})
text('title','HAWKINS','display')
text('subtitle','THE OTHER SIDE','subtitle','labelSecondary')
node('world-summary','layout',layout=dict(mode='column',gapDp=8,align='end'),children=['world-name','world-caption'])
text('world-name','','label','cold-light',bindings={'content':'world.label'},textStyle=dict(typography='label',color='cold-light')) if False else None
text('world-name','','label','cold-light',bindings={'content':'world.label'})
text('world-caption','','body','labelSecondary',bindings={'content':'world.description'})
node('scene','domain_visual',rendererKey='miniature-room',purpose='Inspect the Byers living room in depth; locate the alphabet bulbs and watch RUN.',props=scene,assetId='room-geometry',layout=dict(mode='stack',heightDp=506,minHeightDp=350,grow=1),accessibilityLabel='A miniature Byers living room with alphabet lights, sofa, radio and a blue casement window. Drag to turn the room.')
node('control-panel','layout',layout=dict(mode='column',paddingDp=24,gapDp=16),appearance=dict(material='regular',radiusDp=32),children=['status-row','actions-row'],props={'nativePanel':{'defaultSizeDp':[824,176],'minSizeDp':[608,248],'maxSizeDp':[1008,176],'placementM':[0,-.12,.44],'containedIn':'diorama','facesUser':True}})
node('status-row','layout',layout=dict(mode='row',justify='space-between',align='center',gapDp=16),children=['status','signal-caption'])
text('status','','body','labelPrimary',bindings={'content':'status'})
text('signal-caption','','label','signal-amber',bindings={'content':'signalCaption'})
node('actions-row','layout',layout=dict(mode='row',gapDp=8,justify='space-between',align='center'),children=['world-toggle','play-signal','rotation-toggle','reset-view'])
for id,caption,action in [('world-toggle','','switch-world'),('play-signal','Play RUN','play-run'),('rotation-toggle','','toggle-rotation'),('reset-view','Reset','reset-scene')]:
    p={'size':'max','text':caption}; bindings={}
    if id=='world-toggle': bindings={'text':'world.button'}
    if id=='rotation-toggle': bindings={'text':'rotationLabel'}
    if id=='play-signal': bindings={'text':'playLabel','disabled':'busy'}; p.update({'container-color':brand['signal-amber'],'content-color':brand['ink']})
    node(id,'spatialui',component='sui-button',purpose={'world-toggle':'Compare the same miniature in two worlds.','play-signal':'Transmit the fixed RUN alphabet-light sequence.','rotation-toggle':'Slowly turn the miniature or stop it at the current angle.','reset-view':'Restore Hawkins, the original viewing angle, and idle lights.'}[id],props=p,bindings=bindings,events={'click-action':action},accessibilityLabel=caption or id.replace('-',' '))
data=dict(worldId='hawkins',rotating=False,rotationLabel='Rotate',playLabel='Play RUN',busy=False,status=scene['worlds']['hawkins']['status'],signalCaption='LIGHTS READY',signalIndex=-1,angle=0,geometryAvailable=True,detailAvailable=True)
spec={'schemaVersion':'1.0','metadata':dict(projectName='Hawkins: The Other Side',sourceMode='intent_only',revision=1),'experience':dict(subject='A miniature Byers living room inspired by Stranger Things',user='A fan inspecting a small atmospheric scene',context='Seated or standing in Shared Space, roughly 1.5–2m from the model',singleJob='Inspect one room in two worlds and reveal a short light message.',space='shared',form='volumetric',signature='A painted alphabet wall sends RUN above a physical sofa.'),'theme':dict(scheme='vibrant',rootMaterial='regular',colorScheme=colors,brandColors=brand,typography={'display':dict(family='Georgia, serif',sizeDp=52,lineHeightDp=56,weight=700),'subtitle':dict(family='Georgia, serif',sizeDp=20,lineHeightDp=26,weight=500),'body':dict(family='Arial, sans-serif',sizeDp=16,lineHeightDp=24,weight=500),'label':dict(family='Arial, sans-serif',sizeDp=12,lineHeightDp=18,weight=600)},spacing=dict(small=8,regular=16,medium=24,large=32)),'surfaces':[dict(id='diorama',type='volumetric',defaultSize=dict(width=1.4,height=.8,depth=.8,unit='m'),minSize=dict(width=1.12,height=.64,depth=.64,unit='m'),maxSize=dict(width=1.68,height=.96,depth=.96,unit='m'),rootNodeId='room-root',placement={'viewerDistanceM':1.75,'resizePolicy':'uniform scale; keep control hit targets >=56dp via native panel layout','previewViewportDp':scene['preview'],'axisConvention':'+X right, +Y up, +Z toward viewer'})],'initialStateId':'hawkins','states':[dict(id=id,surfaceRoots={'diorama':'room-root'}) for id in ['hawkins','upside-down']],'transitions':[dict(id='enter-other-side',**{'from':'hawkins','to':'upside-down'},trigger=dict(nodeId='world-toggle',event='click-action')),dict(id='return-home',**{'from':'upside-down','to':'hawkins'},trigger=dict(nodeId='world-toggle',event='click-action')),dict(id='reset-home',**{'from':'upside-down','to':'hawkins'},trigger=dict(nodeId='reset-view',event='click-action'))],'actions':[dict(id='switch-world',type='custom',purpose='Switch the room world while preserving inspection angle; cancel any active message.',payload={'operation':'world','transitionIds':['enter-other-side','return-home']}),dict(id='play-run',type='custom',purpose='Run R, U, N in order and restore idle bulbs.',payload={'operation':'sequence','configNodeId':'scene','configPath':'signal','playingText':'Sending RUN…','completedText':'RUN · MESSAGE RECEIVED','readyText':'LIGHTS READY','playingStatus':'Watch the alphabet wall.','playLabel':'Sending…','idleLabel':'Play RUN'}),dict(id='toggle-rotation',type='toggle',purpose='Turn the miniature slowly or freeze it without moving the camera.',binding='rotating',payload={'labels':{'true':'Pause','false':'Rotate'}}),dict(id='reset-scene',type='custom',purpose='Cancel signal; restore Hawkins, angle zero, and paused motion. Nothing is deleted.',payload={'operation':'reset','targetStateId':'hawkins','transitionId':'reset-home'})],'dataCases':{'normal':data,'fallback':dict(data,detailAvailable=False,status='A quieter scene. The lights still work.',signalCaption='ROOM READY'),'error':dict(data,geometryAvailable=False,status='The room could not appear. Reset to try again.',signalCaption='SCENE UNAVAILABLE',busy=True)},'nodes':nodes,'responsiveRules':[dict(id='compact-controls',surfaceId='diorama',when={'maxWidthDp':760},overrides=[dict(nodeId='room-root',layout=dict(mode='column',paddingDp=16,gapDp=16)),dict(nodeId='world-summary',hidden=True),dict(nodeId='scene',layout=dict(mode='stack',heightDp=430,minHeightDp=350,grow=1)),dict(nodeId='control-panel',layout=dict(mode='column',paddingDp=16,gapDp=16)),dict(nodeId='actions-row',layout=dict(mode='grid',columns=2,gapDp=8)),dict(nodeId='status-row',layout=dict(mode='column',align='start',gapDp=8))])],'assets':[dict(id='room-geometry',kind='model',src='generated:scene.props.meshes',purpose='Design-time procedural miniature; downstream authors offline opaque PBR GLB geometry from the same spatial composition.',alt='Byers living room miniature, 1.4 metres wide, with alphabet bulb wall.')]}
(out/'design-spec.json').write_text(json.dumps(spec,indent=2)+'\n')
print(f'Wrote design-spec.json first: {len(nodes)} nodes, {len(meshes)} geometry objects.')
