(function(root){
  const locked=(s,c)=>Math.abs(s.frequency-c.targetFrequency)<=c.tolerance;
  const strength=(s,c)=>Math.floor(Math.max(0,1-Math.abs(s.frequency-c.targetFrequency)/12)*100);
  const fresh=c=>({frequency:c.initialFrequency,reply:'',solved:false,wrong:false,received:false,scanSeconds:0});
  const tune=(s,value,c)=>!s.geometryAvailable||s.busy||s.solved||!Number.isFinite(value)?s:value===s.frequency?s:{...s,frequency:Math.max(c.minFrequency,Math.min(c.maxFrequency,value)),received:false,reply:'',wrong:false};
  const enter=(s,letter,c)=>!s.geometryAvailable||!s.received||s.busy||s.solved||s.reply.length>=c.replyLength||!c.keys.includes(letter)?s:{...s,reply:s.reply+letter,wrong:false};
  const submit=(s,c,letters)=>!s.geometryAvailable||!s.received||s.busy||s.solved||s.reply.length!==c.replyLength?s:s.reply===letters.join('').toUpperCase()?{...s,solved:true,wrong:false,rotating:false,angle:0}:{...s,wrong:true};
  const project=(s,c,letters)=>{
    const unavailable=!s.geometryAvailable;
    const phase=s.solved?'solved':s.busy?'listening':s.wrong?'wrong':s.received?'reply':locked(s,c)?'locked':'search';
    return {frequency:s.frequency,frequencyLabel:s.frequency.toFixed(1)+c.labels.frequencySuffix,strengthLabel:strength(s,c)+c.labels.strengthSuffix,
      status:c.phaseCopy[phase][1],signalCaption:c.phaseCopy[phase][0],phase,
      signalReadout:s.received||s.solved?Array.from({length:c.replyLength},(_,i)=>s.reply[i]||'_').join(' '):s.busy?letters.map((_,i)=>i<=s.signalIndex?'●':'·').join(' '):s.frequency.toFixed(1),
      playLabel:c.labels[s.solved?'solved':s.busy?'listening':s.received?'replay':'listen'],
      worldDisabled:unavailable||s.solved,motionDisabled:unavailable||s.busy||s.solved,tunerDisabled:unavailable||s.busy||s.solved,
      listenDisabled:unavailable||s.busy||s.solved||!locked(s,c),keyDisabled:unavailable||!s.received||s.busy||s.solved||s.reply.length>=c.replyLength,
      eraseDisabled:unavailable||!s.reply.length||s.busy||s.solved,submitDisabled:unavailable||!s.received||s.reply.length!==c.replyLength||s.busy||s.solved};
  };
  const api={locked,strength,fresh,tune,enter,submit,project};root.HawkinsReceiverRules=api;if(typeof module!=='undefined')module.exports=api;
})(globalThis);
