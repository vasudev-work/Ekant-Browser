package com.aekantkit

import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * Lets a user tap a part of the page and keep it hidden.
 *
 * The page half highlights whatever is under the finger and works out a CSS
 * selector for it; the native half stores that selector per host. Apply
 * [cssFor] on every page load and the element stays gone.
 */
object ElementPicker {

    /** Injects the picker. Call [apply] afterwards to re-apply saved rules. */
    fun start(webView: WebView) {
        try { webView.evaluateJavascript(PICKER_JS, null) } catch (_: Exception) { }
    }

    /** Re-applies every saved selector for a host without a reload. */
    fun apply(webView: WebView, context: android.content.Context, host: String) {
        val css = ElementBlocks.cssFor(context, host)
        if (css.isEmpty()) return
        val js = "try{var s=document.getElementById('aekantkit-elem');" +
            "if(!s){s=document.createElement('style');s.id='aekantkit-elem';" +
            "document.documentElement.appendChild(s);}" +
            "s.textContent=" + org.json.JSONObject.quote(css) + ";}catch(e){}"
        try { webView.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    /** Register this on the WebView: addJavascriptInterface(PickerBridge(ctx), "AekantBlock") */
    class PickerBridge(private val context: android.content.Context) {
        @JavascriptInterface
        fun add(host: String, selector: String) {
            ElementBlocks.add(context, host, selector)
        }
    }

    const val PICKER_JS = """(function(){try{
if(window.__aekantPick){window.__aekantPick=0;return;}
window.__aekantPick=1;
var box=document.createElement('div');
box.style.cssText='position:fixed;z-index:2147483647;pointer-events:none;border:2px solid #FF6600;background:rgba(255,102,0,.18);border-radius:4px';
document.documentElement.appendChild(box);
function esc(s){return (window.CSS&&CSS.escape)?CSS.escape(s):String(s).replace(/[^a-zA-Z0-9_-]/g,'_');}
function sel(el){
 if(el.id)return '#'+esc(el.id);
 var parts=[];
 while(el&&el.nodeType===1&&parts.length<4){
  var p=el.nodeName.toLowerCase();
  if(el.className&&typeof el.className==='string'){
   var c=el.className.trim().split(/[ ]+/).filter(function(x){return x&&x.length<25&&!/^[0-9]/.test(x);}).slice(0,2);
   if(c.length)p+='.'+c.map(esc).join('.');
  }
  var par=el.parentNode;
  if(par){
   var same=[];
   for(var i=0;i<par.children.length;i++){if(par.children[i].nodeName===el.nodeName)same.push(par.children[i]);}
   if(same.length>1)p+=':nth-of-type('+(same.indexOf(el)+1)+')';
  }
  parts.unshift(p);
  el=par;
 }
 return parts.join(' > ');
}
function move(e){
 var t=e.touches?e.touches[0]:e;
 var el=document.elementFromPoint(t.clientX,t.clientY);
 if(!el||el===box)return;
 var r=el.getBoundingClientRect();
 box.style.left=r.left+'px';box.style.top=r.top+'px';box.style.width=r.width+'px';box.style.height=r.height+'px';
}
function done(e){
 var t=e.changedTouches?e.changedTouches[0]:e;
 var el=document.elementFromPoint(t.clientX,t.clientY);
 if(el&&el!==box&&window.AekantBlock){try{window.AekantBlock.add(location.hostname,sel(el));}catch(x){}}
 cleanup();
 if(e.cancelable)e.preventDefault();
}
function cleanup(){
 window.__aekantPick=0;
 try{box.remove();}catch(x){}
 document.removeEventListener('mousemove',move,true);
 document.removeEventListener('touchmove',move,true);
 document.removeEventListener('click',done,true);
 document.removeEventListener('touchend',done,true);
}
document.addEventListener('mousemove',move,true);
document.addEventListener('touchmove',move,true);
document.addEventListener('click',done,true);
document.addEventListener('touchend',done,true);
}catch(e){}})()"""
}
